package org.ntqqrev.acidify.internal.packet.oidb

import org.ntqqrev.acidify.internal.protobuf.PbInt32
import org.ntqqrev.acidify.internal.protobuf.PbInt64
import org.ntqqrev.acidify.internal.protobuf.PbSchema
import org.ntqqrev.acidify.internal.protobuf.get

internal object SetGroupWholeMuteReq : PbSchema() {
    val groupCode = PbInt64[1]
    val state = State[2]

    internal object State : PbSchema() {
        val isMute = PbInt32[17]
    }
}

