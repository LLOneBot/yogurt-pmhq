package org.ntqqrev.acidify.internal.protobuf.dataview

internal sealed class DataToken(val wireType: Int)

internal class Varint(val value: Long): DataToken(WireType.VARINT)

internal class LengthDelimited(val dataBlock: ByteArray): DataToken(WireType.LENGTH_DELIMITED)

internal class Fixed32(val value: Int) : DataToken(WireType.FIXED32)

internal class Fixed64(val value: Long) : DataToken(WireType.FIXED64)