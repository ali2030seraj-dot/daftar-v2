package com.daftar.v2.security.license

import java.math.BigInteger

/**
 * ترميز وفك ترميز Base58.
 * نفس الخوارزمية المستخدمة في KeyGen.
 */
object Base58 {

    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val BASE = BigInteger.valueOf(58)

    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)

        var num = BigInteger.ZERO
        for (c in input) {
            val index = ALPHABET.indexOf(c)
            if (index < 0) throw IllegalArgumentException("Invalid Base58 char: $c")
            num = num.multiply(BASE).add(BigInteger.valueOf(index.toLong()))
        }

        var bytes = num.toByteArray()

        // إزالة 0x00 البادئة التي يضيفها BigInteger
        if (bytes.size > 1 && bytes[0] == 0.toByte()) {
            bytes = bytes.copyOfRange(1, bytes.size)
        }

        // إضافة أصفار لحرف '1' في البداية
        var leadingZeros = 0
        for (c in input) {
            if (c == '1') leadingZeros++
            else break
        }

        if (leadingZeros > 0) {
            val result = ByteArray(leadingZeros + bytes.size)
            System.arraycopy(bytes, 0, result, leadingZeros, bytes.size)
            return result
        }

        return bytes
    }

    fun encode(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        var num = BigInteger(1, bytes)
        val sb = StringBuilder()

        while (num > BigInteger.ZERO) {
            val divRem = num.divideAndRemainder(BASE)
            sb.append(ALPHABET[divRem[1].toInt()])
            num = divRem[0]
        }

        for (b in bytes) {
            if (b == 0.toByte()) sb.append('1')
            else break
        }

        return sb.reverse().toString()
    }
}