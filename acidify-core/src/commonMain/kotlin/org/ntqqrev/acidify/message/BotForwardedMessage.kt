package org.ntqqrev.acidify.message

import kotlin.js.JsExport

/**
 * 合并转发消息中的单条消息
 * @property sequence 消息序列号
 * @property senderName 发送者名称
 * @property avatarUrl 发送者头像 URL
 * @property timestamp 消息发送的 Unix 时间戳（秒）
 * @property segments 消息内容
 */
@JsExport
data class BotForwardedMessage internal constructor(
    val sequence: Long,
    val senderName: String,
    val avatarUrl: String,
    val timestamp: Long,
    val segments: List<BotIncomingSegment>
)

