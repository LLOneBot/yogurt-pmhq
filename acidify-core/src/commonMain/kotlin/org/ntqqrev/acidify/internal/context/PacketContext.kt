package org.ntqqrev.acidify.internal.context

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.ntqqrev.acidify.common.PmhqCallResponse
import org.ntqqrev.acidify.common.SsoResponse
import org.ntqqrev.acidify.internal.AbstractClient
import org.ntqqrev.acidify.internal.LagrangeClient
import org.ntqqrev.acidify.internal.json.pmhq.*
import org.ntqqrev.acidify.internal.pmhq.PmhqListener
import org.ntqqrev.acidify.internal.pmhq.PmhqService
import org.ntqqrev.acidify.internal.pmhq.pmhqJson
import org.ntqqrev.acidify.internal.proto.system.SsoSecureInfo
import org.ntqqrev.acidify.internal.service.EncryptType
import org.ntqqrev.acidify.internal.service.RequestType
import org.ntqqrev.acidify.internal.service.system.Heartbeat
import org.ntqqrev.acidify.internal.util.ensureLagrange

internal class PacketContext(client: AbstractClient) : AbstractContext(client) {
    private val pmhqWsUrl: String
        get() = client.ensureLagrange().pmhqUrl

    private val websocketClient = HttpClient {
        install(WebSockets)
    }
    private var currentSession: DefaultClientWebSocketSession? = null
    private var connectionReady = CompletableDeferred<Unit>()
    private val pendingPackets = mutableMapOf<String, CompletableDeferred<SsoResponse>>()
    private val pendingCalls = mutableMapOf<String, CompletableDeferred<PmhqCallResponse>>()
    private val sendPacketMutex = Mutex()
    private val mapQueryMutex = Mutex()
    private val connectLoopMutex = Mutex()
    private val eventListenerMutex = Mutex()
    private val eventListeners = mutableMapOf<String, PmhqListener>()
    private var startConnectLoopJob: Job? = null
    private var heartbeatJob: Job? = null
    private var closeRequested = false

    init {
        require(client is LagrangeClient) { "PMHQ transport only supports PCQQ clients" }
    }

    override suspend fun postOnline() {
        client.callService(Heartbeat)
    }

    override suspend fun preOffline() {
    }

    suspend fun addEventListener(listener: PmhqListener): String {
        val listenerId = "pmhq-event-${client.ssoSequence++}"
        eventListenerMutex.withLock { eventListeners[listenerId] = listener }
        return listenerId
    }

    suspend fun removeEventListener(listenerId: String) {
        eventListenerMutex.withLock { eventListeners.remove(listenerId) }
    }

    suspend fun callFunction(
        function: String,
        args: List<JsonElement> = emptyList(),
        timeoutMillis: Long = 10_000L,
    ): JsonElement? {
        ensureConnectionStarted()
        connectionReady.await()
        val echo = "$function-${client.ssoSequence++}"
        val deferred = CompletableDeferred<PmhqCallResponse>()
        mapQueryMutex.withLock { pendingCalls[echo] = deferred }
        try {
            sendFrame(
                PmhqCallFrame(
                    data = PmhqCallData(
                        echo = echo,
                        func = function,
                        args = args,
                    )
                )
            )
            logger.v { "[echo=$echo] -> call $function" }
            val response = withTimeout(timeoutMillis) { deferred.await() }
            if (response.code != 0) throw IOException("PMHQ call $function failed: ${response.message}")
            return response.result
        } catch (e: Exception) {
            mapQueryMutex.withLock { pendingCalls.remove(echo) }
            throw e
        }
    }

    suspend fun <T, R> callService(service: PmhqService<T, R>, payload: T, timeoutMillis: Long = 10_000L): R =
        service.parse(
            client,
            callFunction(
                function = service.func,
                args = service.build(client, payload),
                timeoutMillis = timeoutMillis,
            )
        )

    suspend fun <R> callService(service: PmhqService<Unit, R>, timeoutMillis: Long = 10_000L): R =
        callService(service, Unit, timeoutMillis)

    suspend fun startConnectLoop() {
        var isReconnect = false
        var shouldRestoreOnlineContexts = false
        while (currentCoroutineContext().isActive) {
            try {
                closeRequested = false
                connect()
                connectionReady.complete(Unit)
                if (isReconnect && shouldRestoreOnlineContexts) {
                    client.doPostOnlineLogic()
                    logger.i { "PMHQ 连接已恢复" }
                    shouldRestoreOnlineContexts = false
                }
                handleReceiveLoop()
                break
            } catch (_: CancellationException) {
                break
            } catch (e: Exception) {
                if (closeRequested) break
                logger.e(e) {
                    if (isReconnect || connectionReady.isCompleted) {
                        "PMHQ 连接出现错误，5s 后尝试重新连接"
                    } else {
                        "连接 PMHQ 失败，5s 后尝试重连"
                    }
                }
                cleanupPendingRequests(e)
                if (isReconnect || connectionReady.isCompleted) {
                    shouldRestoreOnlineContexts = heartbeatJob != null
                    client.doPreOfflineLogic()
                }
                closeConnectionInternal()
                connectionReady = CompletableDeferred()
                delay(5000)
                isReconnect = true
            }
        }
    }

    suspend fun closeConnection() {
        closeRequested = true
        startConnectLoopJob?.cancel()
        cleanupPendingRequests(IOException("PMHQ connection closed by client"))
        closeConnectionInternal()
    }

    private suspend fun closeConnectionInternal() {
        try {
            currentSession?.close()
            currentSession = null
            logger.d { "已关闭 PMHQ 连接" }
        } catch (e: Exception) {
            logger.w(e) { "关闭 PMHQ 连接时出现错误" }
        }
    }

    private suspend fun connect() {
        val newSession = websocketClient.webSocketSession(urlString = pmhqWsUrl)
        if (!newSession.isActive) {
            throw IOException("PMHQ websocket session is not active")
        }
        currentSession = newSession
        logger.d { "已连接到 PMHQ websocket: $pmhqWsUrl" }
    }

    suspend fun sendPacket(
        command: String,
        sequence: Int,
        payload: ByteArray,
        ssoReservedMsgType: Int,
        timeoutMillis: Long = 10_000L,
        requestType: RequestType = RequestType.D2Auth,
        encryptType: EncryptType = EncryptType.WithD2Key,
        ssoSecureInfo: SsoSecureInfo? = null,
    ): SsoResponse {
        ensureConnectionStarted()
        connectionReady.await()
        val echo = sequence.toString()
        val deferred = CompletableDeferred<SsoResponse>()
        mapQueryMutex.withLock { pendingPackets[echo] = deferred }
        try {
            sendFrame(
                PmhqSendFrame(
                    data = PmhqSendData(
                        echo = echo,
                        cmd = command,
                        pb = payload.toHexString(),
                    )
                )
            )
            logger.v { "[seq=$sequence] -> $command" }
            return withTimeout(timeoutMillis) { deferred.await() }
        } catch (e: Exception) {
            mapQueryMutex.withLock { pendingPackets.remove(echo) }
            throw e
        }
    }

    private suspend inline fun <reified T> sendFrame(payload: T) {
        val session = currentSession ?: throw IOException("PMHQ websocket is not connected")
        sendPacketMutex.withLock {
            session.send(Frame.Text(pmhqJson.encodeToString(payload)))
        }
    }

    private suspend fun ensureConnectionStarted() {
        if (startConnectLoopJob?.isActive == true) return
        connectLoopMutex.withLock {
            if (startConnectLoopJob?.isActive == true) return
            closeRequested = false
            connectionReady = CompletableDeferred()
            startConnectLoopJob = client.launch {
                startConnectLoop()
            }
        }
    }

    private suspend fun handleReceiveLoop() {
        val session = currentSession ?: throw IOException("PMHQ websocket is not connected")
        for (frame in session.incoming) {
            when (frame) {
                is Frame.Text -> handleIncomingText(frame.readText())
                is Frame.Close -> throw IOException("PMHQ websocket closed")

                else -> {}
            }
        }
        throw IOException("PMHQ websocket closed")
    }

    private suspend fun handleIncomingText(text: String) {
        val packet = pmhqJson.decodeFromString<PmhqIncomingFrame>(text)
        when (packet.type) {
            "recv" -> handleRecv(packet)
            "call" -> handleCall(packet)
            else -> handleEvent(packet, text)
        }
    }

    private suspend fun handleRecv(packet: PmhqIncomingFrame) {
        val data = packet.data?.let { pmhqJson.decodeFromJsonElement<PmhqRecvData>(it) } ?: return
        val echo = data.echo
        val command = data.cmd
        val payload = data.pb.hexToByteArray()
        val retCode = packet.code
        val message = packet.message
        val sequence = echo?.toIntOrNull() ?: 0
        val sso = if (retCode == 0) {
            SsoResponse(retCode, command, payload, sequence)
        } else {
            SsoResponse(retCode, command, payload, sequence, message)
        }
        logger.v { "[seq=${sso.sequence}] <- ${sso.command} (code=${sso.retCode})" }
        val pending = if (echo != null) {
            mapQueryMutex.withLock { pendingPackets.remove(echo) }
        } else {
            null
        }
        if (pending != null) {
            pending.complete(sso)
        } else {
            client.pushChannel.send(sso)
        }
    }

    private suspend fun handleCall(packet: PmhqIncomingFrame) {
        val data = packet.data?.let { pmhqJson.decodeFromJsonElement<PmhqCallResultData>(it) } ?: return
        val echo = data.echo ?: return
        val response = PmhqCallResponse(
            code = packet.code,
            message = packet.message,
            result = data.result,
        )
        mapQueryMutex.withLock { pendingCalls.remove(echo) }?.complete(response)
    }

    private suspend fun handleEvent(packet: PmhqIncomingFrame, rawText: String) {
        val event = packet.data?.let {
            runCatching { pmhqJson.decodeFromJsonElement<PmhqEventData>(it) }.getOrNull()
        }
        if (event == null) {
            logger.v { "收到未知的 PMHQ 消息：$rawText" }
            return
        }
        val listeners = eventListenerMutex.withLock { eventListeners.values.toList() }
        listeners.forEach { listener ->
            client.launch {
                listener.handle(packet.type, event)
            }
        }
    }

    private suspend fun cleanupPendingRequests(error: Throwable) {
        mapQueryMutex.withLock {
            val packetCount = pendingPackets.size
            val callCount = pendingCalls.size
            if (packetCount + callCount > 0) {
                logger.w { "清理 ${packetCount + callCount} 个待处理的 PMHQ 请求" }
            }
            val wrapped = IOException("PMHQ connection disconnected: ${error.message}", error)
            pendingPackets.values.forEach { it.completeExceptionally(wrapped) }
            pendingCalls.values.forEach { it.completeExceptionally(wrapped) }
            pendingPackets.clear()
            pendingCalls.clear()
        }
    }
}
