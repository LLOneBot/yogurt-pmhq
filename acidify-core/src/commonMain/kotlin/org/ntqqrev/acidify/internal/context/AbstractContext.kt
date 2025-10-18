package org.ntqqrev.acidify.internal.context

import org.ntqqrev.acidify.internal.LagrangeClient

internal abstract class AbstractContext(
    internal val client: LagrangeClient
) {
    open suspend fun postOnline() {}
    open suspend fun preOffline() {}
}