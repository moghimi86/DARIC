package com.example.data.repository

import com.example.data.dao.AssetDao
import com.example.data.dao.DebtDao
import com.example.data.dao.ExchangeRateDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.AssetEntity
import com.example.data.entity.DebtEntity
import com.example.data.entity.ExchangeRateEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.CurrencyType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class WealthRepository(
    private val assetDao: AssetDao,
    private val debtDao: DebtDao,
    private val exchangeRateDao: ExchangeRateDao,
    private val transactionDao: TransactionDao
) {
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssets()
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()
    val allExchangeRates: Flow<List<ExchangeRateEntity>> = exchangeRateDao.getAllExchangeRates()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    // --- Asset Operations ---
    suspend fun insertAsset(asset: AssetEntity, descEn: String, descFa: String) {
        val assetId = assetDao.insertAsset(asset)
        // Log transaction
        val txn = TransactionEntity(
            assetId = assetId.toInt(),
            descriptionEn = descEn,
            descriptionFa = descFa,
            amount = asset.quantity,
            currencyType = asset.currencyType,
            type = "ADD"
        )
        transactionDao.insertTransaction(txn)
    }

    suspend fun deleteAsset(id: Int, nameEn: String, nameFa: String) {
        val asset = assetDao.getAssetById(id)
        if (asset != null) {
            assetDao.deleteAssetById(id)
            val txn = TransactionEntity(
                assetId = null,
                descriptionEn = "Removed asset: $nameEn",
                descriptionFa = "حذف دارایی: $nameFa",
                amount = asset.quantity,
                currencyType = asset.currencyType,
                type = "DELETE"
            )
            transactionDao.insertTransaction(txn)
        }
    }

    // --- Debt Operations ---
    suspend fun insertDebt(debt: DebtEntity) {
        val typeStr = if (debt.isReceivable) "طلب" else "بدهی"
        val wordEn = if (debt.isReceivable) "Receivable" else "Debt"
        val idStr = debtDao.insertDebt(debt)
        
        transactionDao.insertTransaction(
            TransactionEntity(
                assetId = null,
                descriptionEn = "Added $wordEn: ${debt.debtorCreditorName}",
                descriptionFa = "ثبت $typeStr جدید: ${debt.debtorCreditorName}",
                amount = debt.amount,
                currencyType = debt.currencyType,
                type = if (debt.isReceivable) "RECEIVABLE_ADD" else "DEBT_ADD"
            )
        )
    }

    suspend fun deleteDebt(id: Int) {
        val debt = debtDao.getDebtById(id)
        if (debt != null) {
            debtDao.deleteDebtById(id)
            val typeStr = if (debt.isReceivable) "طلب" else "بدهی"
            val wordEn = if (debt.isReceivable) "Receivable" else "Debt"
            transactionDao.insertTransaction(
                TransactionEntity(
                    assetId = null,
                    descriptionEn = "Removed $wordEn: ${debt.debtorCreditorName}",
                    descriptionFa = "حذف $typeStr: ${debt.debtorCreditorName}",
                    amount = debt.amount,
                    currencyType = debt.currencyType,
                    type = "DEBT_DELETE"
                )
            )
        }
    }

    suspend fun toggleDebtStatus(id: Int) {
        val debt = debtDao.getDebtById(id)
        if (debt != null) {
            val newStatus = if (debt.status == "PAID") "PENDING" else "PAID"
            val updated = debt.copy(status = newStatus)
            debtDao.insertDebt(updated)
            
            // Recipient or payment log
            val labelEn = if (newStatus == "PAID") "Settled" else "Reopened"
            val labelFa = if (newStatus == "PAID") "تسویه شده" else "مجددا باز شده"
            transactionDao.insertTransaction(
                TransactionEntity(
                    assetId = null,
                    descriptionEn = "$labelEn ${if (debt.isReceivable) "receivable from" else "debt to"} ${debt.debtorCreditorName}",
                    descriptionFa = "$labelFa ${if (debt.isReceivable) "طلب از" else "بدهی به"} ${debt.debtorCreditorName}",
                    amount = debt.amount,
                    currencyType = debt.currencyType,
                    type = "DEBT_UPDATE"
                )
            )
        }
    }

    // --- Exchange Rate Operations ---
    suspend fun updateExchangeRate(code: String, rate: Double) {
        exchangeRateDao.insertExchangeRate(
            ExchangeRateEntity(
                currencyCode = code,
                rateInToman = rate,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    // --- Seeding ---
    suspend fun seedInitialRatesAndAssets() {
        val currentRates = exchangeRateDao.getAllExchangeRates().first()
        if (currentRates.isEmpty()) {
            val defaultRates = listOf(
                ExchangeRateEntity("TOMAN", 1.0),
                ExchangeRateEntity("USD", 65000.0),
                ExchangeRateEntity("EUR", 70000.0),
                ExchangeRateEntity("GBP", 82000.0),
                ExchangeRateEntity("GOLD", 345000.0), // 3,450,000 Toman per gram, let's keep it as 1g Gold = 3.45M (or 3450000.0)
                ExchangeRateEntity("SILVER", 45000.0),
                ExchangeRateEntity("BTC", 4350000000.0), // 4.35 Billion Tomans
                ExchangeRateEntity("ETH", 230000000.0), // 230 Million Tomans
                ExchangeRateEntity("USDT", 65200.0)
            )
            exchangeRateDao.insertExchangeRates(defaultRates)

            // Seed initial trial assets
            val assets = listOf(
                AssetEntity(1, "Bank Melli Deposit", "حساب سپرده بانک ملی", 150000000.0, "CASH", 150000000.0, true),
                AssetEntity(2, "Gold Coins (Emami)", "سکه‌ طلا امامی", 5.0, "GOLD", 3450000.0, false),
                AssetEntity(3, "US Dollar Savings", "پس‌انداز دلار نقدی", 1200.0, "USD", 65000.0, false),
                AssetEntity(4, "Bitcoin Wallet", "کیف پول بیت‌کوین", 0.045, "BTC", 4350000000.0, false),
                AssetEntity(5, "Tehran Apartment Share", "سهم آپارتمان تهران", 1.0, "REAL_ESTATE", 1850000000.0, true)
            )
            for (a in assets) {
                assetDao.insertAsset(a)
            }

            // Seed initial debts and receivables
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 10)
            val tenDaysLater = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, -20)
            val tenDaysAgo = cal.timeInMillis

            val debts = listOf(
                DebtEntity(1, "Reza Ahmadi", 45000000.0, "TOMAN", tenDaysLater, true, "PENDING", "For business startup laptop loan"),
                DebtEntity(2, "Sina Jafari", 300.0, "USD", tenDaysAgo, false, "PENDING", "Travel expense split balance"),
                DebtEntity(3, "Maryam Sadat", 15000000.0, "GOLD", tenDaysLater, false, "PENDING", "Borrowed 5 grams of 18k gold")
            )
            for (d in debts) {
                debtDao.insertDebt(d)
            }

            // Seed initial transactions
            val txns = listOf(
                TransactionEntity(id = 0, assetId = 1, descriptionEn = "Initial Deposit Setup", descriptionFa = "راه‌اندازی حساب سپرده", amount = 150000000.0, currencyType = "CASH", type = "ADD"),
                TransactionEntity(id = 0, assetId = 2, descriptionEn = "Purchased 5 Gold Coins", descriptionFa = "خرید ۵ عدد مسکوکات طلا", amount = 5.0, currencyType = "GOLD", type = "ADD"),
                TransactionEntity(id = 0, assetId = 3, descriptionEn = "Exchanged Rials to USD", descriptionFa = "تبدیل ریال به دلار آمریکا", amount = 1200.0, currencyType = "USD", type = "ADD")
            )
            for (t in txns) {
                transactionDao.insertTransaction(t)
            }
        }
    }
}
