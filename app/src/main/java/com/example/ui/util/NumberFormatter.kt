package com.example.ui.util

import java.text.DecimalFormat

object NumberFormatter {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(text: String): String {
        return text.map { char ->
            if (char in '0'..'9') {
                persianDigits[char - '0']
            } else {
                char
            }
        }.joinToString("")
    }

    /**
     * Formats double values into readable strings with optional currency indicator
     */
    fun formatAmount(
        amount: Double,
        isPersian: Boolean,
        usePersianDigits: Boolean,
        includeCurrency: Boolean = true,
        suffix: String? = null
    ): String {
        val df = DecimalFormat("#,###")
        val formattedNumber = df.format(amount)
        
        val digitsAdapted = if (usePersianDigits) {
            toPersianDigits(formattedNumber)
        } else {
            formattedNumber
        }

        if (!includeCurrency) {
            return suffix?.let { "$digitsAdapted $it" } ?: digitsAdapted
        }

        val currencyLabel = suffix ?: (if (isPersian) "تومان" else "Toman")
        return if (isPersian) {
            "$digitsAdapted $currencyLabel"
        } else {
            "$digitsAdapted $currencyLabel"
        }
    }

    fun formatDecimal(
        amount: Double,
        usePersianDigits: Boolean,
        decimals: Int = 3
    ): String {
        val formatStr = if (decimals == 2) "#,##0.00" else "#,##0.###"
        val df = DecimalFormat(formatStr)
        val formattedNumber = df.format(amount)
        return if (usePersianDigits) {
            toPersianDigits(formattedNumber)
        } else {
            formattedNumber
        }
    }
}
