package org.ntqqrev.acidify.internal.proto.login

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
internal class AndroidTlvBody542(
    @ProtoNumber(9) val field9: Field9 = Field9(),
    @ProtoNumber(17) val field17: Int = 0,
) {
    @Serializable
    class Field9(
        @ProtoNumber(12) val field12: Int = 0,
        @ProtoNumber(15) val field15: Int = 0,
    )
}