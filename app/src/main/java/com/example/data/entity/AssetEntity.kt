package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameEn: String,
    val nameFa: String,
    val quantity: Double,
    val currencyType: String, // Maps to CurrencyType enum
    val currentValueInToman: Double = 0.0, // Manual custom total worth, or per-unit rate
    val isManualTomanOverride: Boolean = false, // If true, ignore automatic exchange conversion
    val timestamp: Long = System.currentTimeMillis()
)
