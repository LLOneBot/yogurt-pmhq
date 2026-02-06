package org.ntqqrev.acidify.internal.util

import dev.karmakrafts.kompress.Deflater
import dev.karmakrafts.kompress.Inflater

private const val GZIP_ID1: Int = 0x1f
private const val GZIP_ID2: Int = 0x8b
private const val GZIP_CM_DEFLATE: Int = 0x08

// FLG bits
private const val FTEXT = 1 shl 0
private const val FHCRC = 1 shl 1
private const val FEXTRA = 1 shl 2
private const val FNAME = 1 shl 3
private const val FCOMMENT = 1 shl 4

/**
 * gzip compress: header + raw deflate + trailer(crc32, isize)
 */
fun gzipCompress(input: ByteArray): ByteArray {
    val crc = Crc32()
    crc.update(input)
    val crcValue = crc.value()

    val deflated = Deflater.deflate(input)

    val out = ByteArrayOutput(10 + deflated.size + 8)

    // ---- Header (10 bytes, minimal) ----
    out.writeU8(GZIP_ID1)
    out.writeU8(GZIP_ID2)
    out.writeU8(GZIP_CM_DEFLATE)
    out.writeU8(0)          // FLG = 0 (no extra fields)
    out.writeLE32(0)        // MTIME = 0
    out.writeU8(0)          // XFL = 0
    out.writeU8(255)        // OS = 255 (unknown)

    // ---- Body ----
    out.writeBytes(deflated)

    // ---- Trailer ----
    out.writeLE32(crcValue)
    out.writeLE32(input.size) // ISIZE mod 2^32; Int is fine for ByteArray size

    return out.toByteArray()
}

/**
 * gzip uncompress: parse header, raw inflate body, verify trailer
 *
 * Only supports a single gzip member.
 * (If you need concatenated members, you can loop: while (pos < n && looksLikeGzipAt(pos)) ...)
 */
fun gzipUncompress(gz: ByteArray): ByteArray {
    val r = ByteArrayReader(gz)

    // ---- Header ----
    val id1 = r.readU8()
    val id2 = r.readU8()
    if (id1 != GZIP_ID1 || id2 != GZIP_ID2) {
        throw IllegalArgumentException("Not a gzip stream (bad magic)")
    }

    val cm = r.readU8()
    if (cm != GZIP_CM_DEFLATE) {
        throw IllegalArgumentException("Unsupported compression method: $cm")
    }

    val flg = r.readU8()
    r.skip(4) // MTIME
    r.skip(1) // XFL
    r.skip(1) // OS

    // Optional fields
    if ((flg and FEXTRA) != 0) {
        val xlen = r.readLE16()
        r.skip(xlen)
    }
    if ((flg and FNAME) != 0) {
        r.readZeroTerminated()
    }
    if ((flg and FCOMMENT) != 0) {
        r.readZeroTerminated()
    }
    if ((flg and FHCRC) != 0) {
        // header CRC16 present (we don't validate; just consume)
        r.skip(2)
    }

    // ---- Body + Trailer ----
    // Trailer is last 8 bytes: CRC32 + ISIZE (both LE)
    // We need the deflate payload: from current position to (end - 8).
    val payloadStart = r.position
    val payloadEndExclusive = gz.size - 8
    if (payloadEndExclusive < payloadStart) {
        throw IllegalArgumentException("Truncated gzip stream (no trailer)")
    }

    val deflatePayload = gz.copyOfRange(payloadStart, payloadEndExclusive)
    val uncompressed = Inflater.inflate(deflatePayload)

    // Read trailer
    r.position = payloadEndExclusive
    val expectedCrc = r.readLE32()
    val expectedISize = r.readLE32()

    // Verify CRC32
    val crc = Crc32()
    crc.update(uncompressed)
    val actualCrc = crc.value()
    if (actualCrc != expectedCrc) {
        throw IllegalArgumentException(
            "GZip CRC32 mismatch: expected=0x${expectedCrc.toUInt().toString(16)}, actual=0x${actualCrc.toUInt().toString(16)}"
        )
    }

    // Verify ISIZE (mod 2^32)
    if (uncompressed.size != expectedISize) {
        throw IllegalArgumentException(
            "GZip ISIZE mismatch: expected=$expectedISize, actual=${uncompressed.size}"
        )
    }

    return uncompressed
}

/* ----------------------------- Helpers ----------------------------- */

private class ByteArrayOutput(initialCapacity: Int = 32) {
    private var buf: ByteArray = ByteArray(initialCapacity)
    private var size: Int = 0

    fun writeU8(v: Int) {
        ensure(1)
        buf[size++] = (v and 0xFF).toByte()
    }

    fun writeBytes(bytes: ByteArray) {
        ensure(bytes.size)
        bytes.copyInto(buf, destinationOffset = size)
        size += bytes.size
    }

    fun writeLE16(v: Int) {
        writeU8(v)
        writeU8(v ushr 8)
    }

    fun writeLE32(v: Int) {
        writeU8(v)
        writeU8(v ushr 8)
        writeU8(v ushr 16)
        writeU8(v ushr 24)
    }

    fun toByteArray(): ByteArray = buf.copyOf(size)

    private fun ensure(extra: Int) {
        val needed = size + extra
        if (needed <= buf.size) return
        var newCap = buf.size.coerceAtLeast(1)
        while (newCap < needed) newCap = newCap * 2
        buf = buf.copyOf(newCap)
    }
}

private class ByteArrayReader(private val buf: ByteArray) {
    var position: Int = 0

    fun readU8(): Int {
        if (position >= buf.size) throw IllegalArgumentException("Unexpected EOF")
        return buf[position++].toInt() and 0xFF
    }

    fun readLE16(): Int {
        val b0 = readU8()
        val b1 = readU8()
        return b0 or (b1 shl 8)
    }

    fun readLE32(): Int {
        val b0 = readU8()
        val b1 = readU8()
        val b2 = readU8()
        val b3 = readU8()
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    fun skip(n: Int) {
        val newPos = position + n
        if (newPos < 0 || newPos > buf.size) throw IllegalArgumentException("Unexpected EOF")
        position = newPos
    }

    fun readZeroTerminated() {
        while (true) {
            val b = readU8()
            if (b == 0) return
        }
    }
}

/**
 * CRC32 (IEEE 802.3) implementation.
 * Polynomial: 0xEDB88320 (reflected). Initial: 0xFFFFFFFF. Final xor: 0xFFFFFFFF.
 */
private class Crc32 {
    private var crc: Int = -1

    fun update(data: ByteArray) {
        var c = crc
        for (b in data) {
            val idx = (c xor (b.toInt() and 0xFF)) and 0xFF
            c = (c ushr 8) xor TABLE[idx]
        }
        crc = c
    }

    fun value(): Int = crc xor -1

    companion object {
        private val TABLE: IntArray = IntArray(256).also { table ->
            for (n in 0 until 256) {
                var c = n
                for (k in 0 until 8) {
                    c = if ((c and 1) != 0) 0xEDB88320.toInt() xor (c ushr 1) else (c ushr 1)
                }
                table[n] = c
            }
        }
    }
}