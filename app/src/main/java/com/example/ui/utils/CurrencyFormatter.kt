package com.example.ui.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "IDR" -> {
                val formatted = String.format(Locale("id", "ID"), "%,d", amount.toLong()).replace(',', '.')
                "Rp $formatted"
            }
            "EUR" -> "€%.2f".format(amount)
            "GBP" -> "£%.2f".format(amount)
            "JPY" -> "¥%,d".format(amount.toLong())
            "AUD" -> "A$%.2f".format(amount)
            "SGD" -> "S$%.2f".format(amount)
            "MYR" -> "RM %.2f".format(amount)
            "CAD" -> "C$%.2f".format(amount)
            "BRL" -> "R$%.2f".format(amount)
            else -> "$%.2f".format(amount)
        }
    }

    fun getSymbol(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "IDR" -> "Rp"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "AUD" -> "A$"
            "SGD" -> "S$"
            "MYR" -> "RM"
            "CAD" -> "C$"
            "BRL" -> "R$"
            else -> "$"
        }
    }
}
