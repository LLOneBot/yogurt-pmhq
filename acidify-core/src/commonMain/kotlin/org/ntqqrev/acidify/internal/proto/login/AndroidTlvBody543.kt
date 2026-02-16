package org.ntqqrev.acidify.internal.proto.login

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
internal class AndroidTlvBody543(
    @ProtoNumber(60) val buttonInfo: ButtonInfo = ButtonInfo(),
) {
    @Serializable
    class ButtonInfo(
        @ProtoNumber(1) val type: Int = 0,
        @ProtoNumber(7) val title: String = "",
        @ProtoNumber(8) val msg: String = "",
        @ProtoNumber(9) val actions: List<Action> = emptyList(),
    ) {
        @Serializable
        class Action(
            @ProtoNumber(1) val title: String = "",
            @ProtoNumber(2) val ordinal: Int = 0,
            @ProtoNumber(3) val url: String? = null,
        )
    }
}