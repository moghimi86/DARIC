package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.entity.TransactionEntity
import com.example.ui.components.AssetRingChart
import com.example.ui.components.BackgroundAtmosphere
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.util.NumberFormatter
import com.example.ui.viewmodel.WealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsAnalyticsScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val usePersianDigits by viewModel.usePersianDigits.collectAsState()

    val assetsList by viewModel.assets.collectAsState()
    val debtsAll by viewModel.debts.collectAsState()
    val exchangeRatesMap by viewModel.exchangeRatesMap.collectAsState()

    val totalNetWorth by viewModel.totalNetWorthInToman.collectAsState()

    // Classify cash vs safe assets
    val cashTotal = remember(assetsList, exchangeRatesMap) {
        assetsList.filter { it.currencyType == "TOMAN" || it.currencyType == "CASH" }
            .sumOf { if (it.isManualTomanOverride) it.currentValueInToman * it.quantity else it.quantity * (exchangeRatesMap[it.currencyType] ?: 1.0) }
    }

    val hardAssetsTotal = remember(assetsList, exchangeRatesMap) {
        assetsList.filter { it.currencyType != "TOMAN" && it.currencyType != "CASH" }
            .sumOf { if (it.isManualTomanOverride) it.currentValueInToman * it.quantity else it.quantity * (exchangeRatesMap[it.currencyType] ?: 1.0) }
    }

    val totalDebtsVal = viewModel.totalDebtsValueInToman.collectAsState()
    val totalRecsVal = viewModel.totalReceivablesValueInToman.collectAsState()

    val cashRatio = remember(cashTotal, totalNetWorth) {
        if (totalNetWorth > 0) (cashTotal / totalNetWorth) * 100 else 0.0
    }
    val safeRatio = remember(hardAssetsTotal, totalNetWorth) {
        if (totalNetWorth > 0) (hardAssetsTotal / totalNetWorth) * 100 else 0.0
    }

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isPersian) "گزارش‌گیری و تخصیص منابع" else "Financial Reports & Ratios",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("transactions_list") }) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "Tx History", tint = NeonPurple)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                // DONUT DISTRIBUTION CHART
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isPersian) "توزیع ساختار کلی سرمایه" else "Total Asset Matrix Allocation",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            AssetRingChart(
                                modifier = Modifier.padding(vertical = 12.dp),
                                cashAmount = cashTotal,
                                hardAssetsAmount = hardAssetsTotal,
                                debtsAmount = totalDebtsVal.value,
                                receivablesAmount = totalRecsVal.value,
                                isPersian = isPersian,
                                totalLabel = NumberFormatter.formatAmount(totalNetWorth, isPersian, usePersianDigits)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Color Legend Indexes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                LegendIndicator(color = GreenPrimary, text = if (isPersian) "پول نقد ریالی" else "Fiat Cash")
                                LegendIndicator(color = GoldYellow, text = if (isPersian) "طلا و فلزات" else "Gold/Metal")
                                LegendIndicator(color = NeonYellowGreen, text = if (isPersian) "طلب‌ها" else "Receivables")
                                LegendIndicator(color = NeonPurple, text = if (isPersian) "بدهی‌ها" else "Debts")
                            }
                        }
                    }
                }

                // SECURITY INDEXES & RATIOS
                item {
                    Text(
                        text = if (isPersian) "ضریب مقاومت در برابر سقوط پول" else "Inflation Hedge Matrix & Risk Metrics",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // Ratio Indicators
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Risk Indicator 1: Cash Exposure
                            RatioLineProgress(
                                title = if (isPersian) "مواجه با سقوط ارزش (پول نقد ریالی)" else "Inflation Exposure (Fiat Cash)",
                                ratioPercent = cashRatio,
                                colorAccent = RedNegative,
                                isPersian = isPersian,
                                usePersianDigits = usePersianDigits
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Risk Indicator 2: Safe Shield Ratio
                            RatioLineProgress(
                                title = if (isPersian) "سپر مصونیت (طلا، ملک و کریپتو)" else "Anti-Inflation Shield (Gold, Property, Crypto)",
                                ratioPercent = safeRatio,
                                colorAccent = GreenPrimary,
                                isPersian = isPersian,
                                usePersianDigits = usePersianDigits
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceGlass)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = NeonYellowGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isPersian) {
                                        "بر اساس فرمول‌های نوین ثروت، زنده نگه‌داشتن همیشگی سپر مصونیت بالای ۶۰٪ ضامن حفاظت سرمایه شما در تورم‌های بالای ۴۰٪ است."
                                    } else {
                                        "Keeping your Anti-Inflation Shield above 60% ensures capital preservation in economies with inflation rates above 40%."
                                    },
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { navController.navigate("transactions_list") },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlassHeavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "Audit History", tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = if (isPersian) "مشاهده ریز تراکنش‌ها و تاریخچه" else "Review Transaction Audit Log", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun LegendIndicator(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RatioLineProgress(
    title: String,
    ratioPercent: Double,
    colorAccent: Color,
    isPersian: Boolean,
    usePersianDigits: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            val percentText = "${String.format("%.1f", ratioPercent)}%"
            Text(
                text = if (usePersianDigits) NumberFormatter.toPersianDigits(percentText) else percentText,
                color = colorAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Progress Bar Line Tracker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(SurfaceGlass)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((ratioPercent / 100).toFloat().coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(colorAccent)
            )
        }
    }
}

// --- HISTORICAL TRANSACTIONS LIST ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsHistoryScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val usePersianDigits by viewModel.usePersianDigits.collectAsState()
    val txList by viewModel.transactions.collectAsState()

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isPersian) "ریزدفتر تراکنش‌ها" else "Transaction Audit Logs",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            if (txList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = "None", tint = TextSecondary.copy(alpha = 0.4f), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = if (isPersian) "تاریخچه‌ای وجود ندارد." else "No operations logged yet.", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(txList) { tx ->
                        TxGlassRow(tx = tx, isPersian = isPersian, usePersianDigits = usePersianDigits)
                    }
                }
            }
        }
    }
}

@Composable
fun TxGlassRow(tx: TransactionEntity, isPersian: Boolean, usePersianDigits: Boolean) {
    val dateStr = remember(tx.timestamp) {
        val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        sdf.format(Date(tx.timestamp))
    }

    val typeLabel = tx.type
    val isAddition = typeLabel.contains("ADD") || typeLabel.contains("RECEIVABLE_ADD")

    val accent = if (isAddition) GreenPrimary else if (typeLabel.contains("DELETE")) RedNegative else NeonPurple

    GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("tx_item_${tx.id}"),
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Colored status indicator pill
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAddition) Icons.Default.ArrowUpward else if (typeLabel.contains("DELETE")) Icons.Default.DeleteOutline else Icons.Default.Loop,
                        contentDescription = "Tx Icon",
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isPersian) tx.descriptionFa else tx.descriptionEn,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (usePersianDigits) NumberFormatter.toPersianDigits(dateStr) else dateStr,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Delta Value
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isAddition) "+" else "-"} ${NumberFormatter.formatDecimal(tx.amount, usePersianDigits, 1)} ${tx.currencyType}",
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
