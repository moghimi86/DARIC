package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.AssetEntity
import com.example.data.entity.DebtEntity
import com.example.data.entity.ExchangeRateEntity
import com.example.data.entity.TransactionEntity
import com.example.data.model.CurrencyType
import com.example.data.repository.WealthRepository
import com.example.data.api.GeminiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WealthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = WealthRepository(
        database.assetDao(),
        database.debtDao(),
        database.exchangeRateDao(),
        database.transactionDao()
    )

    // --- Localization Settings Configuration ---
    val isPersian = MutableStateFlow(true)
    val usePersianDigits = MutableStateFlow(true)
    val annualInflationRate = MutableStateFlow(45.0)

    // --- Core Model Streams ---
    val assets: StateFlow<List<AssetEntity>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exchangeRates: StateFlow<List<ExchangeRateEntity>> = repository.allExchangeRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Calculation Aggregates (Live Combiners) ---
    val exchangeRatesMap: StateFlow<Map<String, Double>> = exchangeRates
        .map { list -> list.associate { it.currencyCode to it.rateInToman } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalAssetsValueInToman: StateFlow<Double> = combine(assets, exchangeRatesMap) { assetList, rates ->
        assetList.sumOf { asset ->
            if (asset.isManualTomanOverride) {
                asset.currentValueInToman * asset.quantity
            } else {
                val rate = rates[asset.currencyType] ?: 1.0
                asset.quantity * rate
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDebtsValueInToman: StateFlow<Double> = combine(debts, exchangeRatesMap) { debtList, rates ->
        debtList.filter { !it.isReceivable && it.status == "PENDING" }.sumOf { debt ->
            val rate = rates[debt.currencyType] ?: 1.0
            debt.amount * rate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalReceivablesValueInToman: StateFlow<Double> = combine(debts, exchangeRatesMap) { debtList, rates ->
        debtList.filter { it.isReceivable && it.status == "PENDING" }.sumOf { rec ->
            val rate = rates[rec.currencyType] ?: 1.0
            rec.amount * rate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalNetWorthInToman: StateFlow<Double> = combine(
        totalAssetsValueInToman,
        totalDebtsValueInToman,
        totalReceivablesValueInToman
    ) { assetsVal, debtsVal, recVal ->
        assetsVal + recVal - debtsVal
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- AI/Helper Responses ---
    val aiResponse = MutableStateFlow<String?>(null)
    val isAiLoading = MutableStateFlow(false)

    init {
        // Trigger default seeding
        viewModelScope.launch {
            repository.seedInitialRatesAndAssets()
        }
    }

    // --- Currency Conversion Utility Helper ---
    fun convertValue(amount: Double, fromCurrency: String, toCurrency: String = "TOMAN"): Double {
        val rates = exchangeRatesMap.value
        val fromRate = rates[fromCurrency] ?: 1.0
        val toRate = rates[toCurrency] ?: 1.0
        val valueInToman = amount * fromRate
        return valueInToman / toRate
    }

    // --- Mutation Actions ---
    fun addAsset(
        nameEn: String,
        nameFa: String,
        quantity: Double,
        currencyType: CurrencyType,
        customValueToman: Double = 0.0,
        isOverride: Boolean = false
    ) {
        viewModelScope.launch {
            val asset = AssetEntity(
                nameEn = nameEn,
                nameFa = nameFa,
                quantity = quantity,
                currencyType = currencyType.name,
                currentValueInToman = customValueToman,
                isManualTomanOverride = isOverride
            )
            val descEn = "Added asset '$nameEn': $quantity ${currencyType.name}"
            val descFa = "ثبت دارایی جدید '$nameFa': $quantity ${currencyType.getDisplayName(true)}"
            repository.insertAsset(asset, descEn, descFa)
        }
    }

    fun removeAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset.id, asset.nameEn, asset.nameFa)
        }
    }

    fun addDebt(
        debtorCreditorName: String,
        amount: Double,
        currencyType: CurrencyType,
        dueDate: Long,
        isReceivable: Boolean,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val debt = DebtEntity(
                debtorCreditorName = debtorCreditorName,
                amount = amount,
                currencyType = currencyType.name,
                dueDate = dueDate,
                isReceivable = isReceivable,
                notes = notes
            )
            repository.insertDebt(debt)
        }
    }

    fun removeDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt.id)
        }
    }

    fun toggleDebtStatus(debt: DebtEntity) {
        viewModelScope.launch {
            repository.toggleDebtStatus(debt.id)
        }
    }

    fun saveExchangeRate(code: String, rateInToman: Double) {
        viewModelScope.launch {
            repository.updateExchangeRate(code, rateInToman)
        }
    }

    fun refreshAiInsights() {
        viewModelScope.launch {
            isAiLoading.value = true
            aiResponse.value = null
            
            // Gather statistics
            val netWorth = totalNetWorthInToman.value
            val debtsTotal = totalDebtsValueInToman.value
            val recsTotal = totalReceivablesValueInToman.value
            val rates = exchangeRatesMap.value

            val cashAndBasicMelli = assets.value.filter { 
                it.currencyType == "TOMAN" || it.currencyType == "CASH" 
            }.sumOf { if (it.isManualTomanOverride) it.currentValueInToman * it.quantity else it.quantity * (rates[it.currencyType] ?: 1.0) }

            val safeGoldCryptoProperty = assets.value.filter { 
                it.currencyType != "TOMAN" && it.currencyType != "CASH" 
            }.sumOf { if (it.isManualTomanOverride) it.currentValueInToman * it.quantity else it.quantity * (rates[it.currencyType] ?: 1.0) }

            val textResult = GeminiClient.generateWealthInsights(
                isPersian = isPersian.value,
                totalNetWorthToman = netWorth,
                cashToman = cashAndBasicMelli,
                safeAssetsToman = safeGoldCryptoProperty,
                debtsToman = debtsTotal,
                receivablesToman = recsTotal,
                inflationRate = annualInflationRate.value
            )

            isAiLoading.value = false
            aiResponse.value = textResult
        }
    }

    // --- Factory Provider ---
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WealthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WealthViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
