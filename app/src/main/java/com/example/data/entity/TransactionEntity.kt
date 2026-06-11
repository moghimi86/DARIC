package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetId: Int? = null,
    val descriptionEn: String,
    val descriptionFa: String,
    val amount: Double,
    val currencyType: String,
    val type: String, // e.g., "BUY", "SELL", "UPDATE", "REPAYMENT"
    val timestamp: Long = System.currentTimeMillis()
)
