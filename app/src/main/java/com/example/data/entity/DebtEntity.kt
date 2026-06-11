package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val debtorCreditorName: String,
    val amount: Double,
    val currencyType: String, // Maps to CurrencyType
    val dueDate: Long,
    val isReceivable: Boolean, // True = Receivables (What others owe you), False = Debt (What you owe)
    val status: String = "PENDING", // PENDING, PAID
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
