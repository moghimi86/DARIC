package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AssetDao
import com.example.data.dao.DebtDao
import com.example.data.dao.ExchangeRateDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.AssetEntity
import com.example.data.entity.DebtEntity
import com.example.data.entity.ExchangeRateEntity
import com.example.data.entity.TransactionEntity

@Database(
    entities = [
        AssetEntity::class,
        DebtEntity::class,
        ExchangeRateEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun debtDao(): DebtDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "live_wealth_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
