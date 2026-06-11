package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val currencyCode: String, // e.g. "USD", "EUR", "GOLD", "SILVER", "BTC", "ETH"
    val rateInToman: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
