package health.ere.ps.zeta.utils

import kotlin.io.encoding.Base64

object EncodingUtils {

    @JvmStatic
    fun p1363ToDer(sig: ByteArray): ByteArray {
        require(sig.size % 2 == 0) { "Expected r||s with even length" }
        val n = sig.size / 2
        val r = sig.copyOfRange(0, n)
        val s = sig.copyOfRange(n, 2 * n)

        fun toUnsignedIntBytes(x: ByteArray): ByteArray {
            var v = x.dropWhile { it == 0.toByte() }.toByteArray()
            if (v.isEmpty()) {
                v = byteArrayOf(0)
            }
            // if MSB set, prepend 0x00 to keep INTEGER positive
            if ((v[0].toInt() and 0x80) != 0) {
                v = byteArrayOf(0) + v
            }
            return v
        }

        val rInt = toUnsignedIntBytes(r)
        val sInt = toUnsignedIntBytes(s)

        val seqLen = 2 + rInt.size + 2 + sInt.size
        val out = ByteArray(2 + seqLen)
        var i = 0
        out[i++] = 0x30
        out[i++] = seqLen.toByte()
        out[i++] = 0x02
        out[i++] = rInt.size.toByte()
        rInt.copyInto(out, i);
        i += rInt.size
        out[i++] = 0x02
        out[i++] = sInt.size.toByte()
        sInt.copyInto(out, i)
        return out
    }

    @JvmStatic
    fun base64Encode(input: ByteArray): String {
        return Base64.encode(input)
    }

    @JvmStatic
    fun base64EncodeWithAbsentPadding(input: ByteArray): String {
        return Base64.withPadding(Base64.PaddingOption.ABSENT).encode(input)
    }

    @JvmStatic
    fun base64DecodeWithAbsentPadding(input: String): ByteArray {
        return Base64.withPadding(Base64.PaddingOption.ABSENT).decode(input)
    }
}