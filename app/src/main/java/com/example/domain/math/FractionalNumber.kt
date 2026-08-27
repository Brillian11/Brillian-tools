package com.example.domain.math

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Handles fractional inch parsing, arithmetic, and conversions.
 * E.g., parses "15 3/64", "3 1/8", "5.25" and formats back to fractions.
 */
data class FractionalNumber(
    val numerator: Long,
    val denominator: Long = 1L
) {
    init {
        require(denominator != 0L) { "Denominator cannot be zero" }
    }

    val decimalValue: Double get() = numerator.toDouble() / denominator.toDouble()
    val mmValue: Double get() = decimalValue * 25.4

    operator fun plus(other: FractionalNumber): FractionalNumber {
        val num = this.numerator * other.denominator + other.numerator * this.denominator
        val den = this.denominator * other.denominator
        return createReduced(num, den)
    }

    operator fun minus(other: FractionalNumber): FractionalNumber {
        val num = this.numerator * other.denominator - other.numerator * this.denominator
        val den = this.denominator * other.denominator
        return createReduced(num, den)
    }

    operator fun times(other: FractionalNumber): FractionalNumber {
        return createReduced(this.numerator * other.numerator, this.denominator * other.denominator)
    }

    operator fun div(other: FractionalNumber): FractionalNumber {
        require(other.numerator != 0L) { "Cannot divide by zero" }
        return createReduced(this.numerator * other.denominator, this.denominator * other.numerator)
    }

    fun toFractionString(maxDenominator: Int = 64): String {
        val dec = decimalValue
        val sign = if (dec < 0) "-" else ""
        val absDec = abs(dec)

        val wholePart = absDec.toLong()
        val fractionalPart = absDec - wholePart

        if (fractionalPart < 1.0 / (maxDenominator * 2)) {
            return if (wholePart == 0L && sign.isNotEmpty()) "0\"" else "$sign$wholePart\""
        }

        val roundedNum = (fractionalPart * maxDenominator).roundToInt()
        if (roundedNum == maxDenominator) {
            return "$sign${wholePart + 1}\""
        }

        val gcdVal = gcd(roundedNum.toLong(), maxDenominator.toLong())
        val num = roundedNum / gcdVal
        val den = maxDenominator / gcdVal

        return if (wholePart == 0L) {
            "$sign$num/$den\""
        } else {
            "$sign$wholePart $num/$den\""
        }
    }

    companion object {
        fun fromDecimal(decimalInches: Double): FractionalNumber {
            val scale = 1000000L
            val num = (decimalInches * scale).toLong()
            return createReduced(num, scale)
        }

        fun fromMm(mm: Double): FractionalNumber {
            return fromDecimal(mm / 25.4)
        }

        fun parse(input: String): FractionalNumber {
            val cleaned = input.trim().replace("\"", "").replace("in", "").trim()
            if (cleaned.isEmpty()) return FractionalNumber(0)

            val parts = cleaned.split(" ").filter { it.isNotEmpty() }
            return when (parts.size) {
                1 -> {
                    val str = parts[0]
                    if (str.contains("/")) {
                        val fractionParts = str.split("/")
                        val num = fractionParts[0].toLongOrNull() ?: 0L
                        val den = fractionParts.getOrNull(1)?.toLongOrNull() ?: 1L
                        createReduced(num, den)
                    } else {
                        val d = str.toDoubleOrNull() ?: 0.0
                        fromDecimal(d)
                    }
                }
                2 -> {
                    val whole = parts[0].toLongOrNull() ?: 0L
                    val fracStr = parts[1]
                    if (fracStr.contains("/")) {
                        val fractionParts = fracStr.split("/")
                        val num = fractionParts[0].toLongOrNull() ?: 0L
                        val den = fractionParts.getOrNull(1)?.toLongOrNull() ?: 1L
                        val sign = if (whole < 0 || strHasMinus(parts[0])) -1 else 1
                        val totalNum = (abs(whole) * den + num) * sign
                        createReduced(totalNum, den)
                    } else {
                        fromDecimal(cleaned.toDoubleOrNull() ?: 0.0)
                    }
                }
                else -> {
                    val d = cleaned.toDoubleOrNull() ?: 0.0
                    fromDecimal(d)
                }
            }
        }

        private fun strHasMinus(str: String) = str.startsWith("-")

        private fun gcd(a: Long, b: Long): Long {
            var x = abs(a)
            var y = abs(b)
            while (y != 0L) {
                val temp = y
                y = x % y
                x = temp
            }
            return if (x == 0L) 1L else x
        }

        private fun createReduced(num: Long, den: Long): FractionalNumber {
            var d = den
            var n = num
            if (d < 0) {
                n = -n
                d = -d
            }
            val g = gcd(n, d)
            return FractionalNumber(n / g, d / g)
        }
    }
}
