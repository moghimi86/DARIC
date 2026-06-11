package com.example.data.model

enum class CurrencyType(val symbolEn: String, val symbolFa: String, val isCrypto: Boolean = false, val isMetal: Boolean = false) {
    TOMAN("Toman", "تومان"),
    USD("USD", "دلار"),
    EUR("EUR", "یورو"),
    GBP("GBP", "پوند"),
    GOLD("g Gold", "گرم طلا", isMetal = true),
    SILVER("g Silver", "گرم نقره", isMetal = true),
    BTC("BTC", "بیت‌کوین", isCrypto = true),
    ETH("ETH", "اتریوم", isCrypto = true),
    USDT("USDT", "تتر", isCrypto = true),
    REAL_ESTATE("Real Estate", "املاک"),
    CASH("Cash", "پول نقد"),
    OTHER("Asset", "دارایی");

    fun getDisplayName(isPersian: Boolean): String {
        return if (isPersian) symbolFa else symbolEn
    }
}
