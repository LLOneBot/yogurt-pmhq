package org.ntqqrev.acidify.internal.packet.oidb

import org.ntqqrev.acidify.internal.protobuf.*

internal object SetMemberMuteReq : PbSchema() {
    val groupCode = PbInt64[1]
    val type = PbInt32[2]
    val body = Body[3]

    internal object Body : PbSchema() {
        val targetUid = PbString[1]
        val duration = PbInt32[2]
    }
}

