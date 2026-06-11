package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.NumberFormatter
import com.example.ui.viewmodel.WealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val usePersianDigits by viewModel.usePersianDigits.collectAsState()
    val inflationRate by viewModel.annualInflationRate.collectAsState()

    val totalNetWorth by viewModel.totalNetWorthInToman.collectAsState()
    val totalAssets by viewModel.totalAssetsValueInToman.collectAsState()
    val totalDebts by viewModel.totalDebtsValueInToman.collectAsState()
    val totalReceivables by viewModel.totalReceivablesValueInToman.collectAsState()

    val assetsList by viewModel.assets.collectAsState()
    val exchangeRates by viewModel.exchangeRatesMap.collectAsState()

    val scrollState = rememberScrollState()

    // Mock initial 6-months Wealth History values to draw the change graph
    val monthlyHistory = remember(totalNetWorth) {
        listOf(
            totalNetWorth * 0.75,
            totalNetWorth * 0.82,
            totalNetWorth * 0.88,
            totalNetWorth * 0.94,
            totalNetWorth * 0.92,
            totalNetWorth
        )
    }

    // Daily & annual cash losses math
    val fiatCashTotal = remember(assetsList, exchangeRates) {
        assetsList.filter { it.currencyType == "TOMAN" || it.currencyType == "CASH" }
            .sumOf { if (it.isManualTomanOverride) it.currentValueInToman * it.quantity else it.quantity * (exchangeRates[it.currencyType] ?: 1.0) }
    }
    val dailyLoss = (fiatCashTotal * (inflationRate / 100.0)) / 365.0

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // Integrated elegant glass navigation bar at the bottom
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavigationTabItem(
                            imageVector = Icons.Default.Dashboard,
                            label = if (isPersian) "پیشخوان" else "Dashboard",
                            active = true,
                            onClick = {}
                        )
                        NavigationTabItem(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            label = if (isPersian) "کیف دارایی" else "Assets",
                            onClick = { navController.navigate("assets_list") }
                        )
                        NavigationTabItem(
                            imageVector = Icons.Default.TrendingDown,
                            label = if (isPersian) "تعهدات" else "Debts",
                            onClick = { navController.navigate("debts_list") }
                        )
                        NavigationTabItem(
                            imageVector = Icons.Default.Psychology,
                            label = if (isPersian) "هوش مالی" else "AI Advice",
                            onClick = { navController.navigate("ai_insights") }
                        )
                        NavigationTabItem(
                            imageVector = Icons.Default.Settings,
                            label = if (isPersian) "تنظیمات" else "Settings",
                            onClick = { navController.navigate("settings") }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("add_asset") },
                    containerColor = NeonPurple,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("quick_add_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Asset")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // App Title & Welcome Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPersian) "خوش آمدید | WELCOME" else "خوش آمدید | WELCOME",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPersian) "سینا مرادی" else "Sina Moradi",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Language Selector Capsule
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.isPersian.value = !isPersian
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "FA",
                                color = if (isPersian) NeonYellowGreen else Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(10.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            )
                            Text(
                                text = "EN",
                                color = if (!isPersian) NeonYellowGreen else Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Profile indicator SM with linear gradient sweep border
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(GreenPrimary, NeonPurple, GreenPrimary)
                                    )
                                )
                                .padding(1.5.dp) // Border thickness
                                .clip(CircleShape)
                                .background(DeepDarkBackground)
                                .clickable {
                                    navController.navigate("settings")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SM",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 1. MASTER WEALTH HERO DISPLAY WITH GLOW
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    glowColor = NeonPurple
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Top-right background decorative circle
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-10).dp)
                                .size(80.dp)
                                .border(3.dp, NeonPurple.copy(alpha = 0.1f), CircleShape)
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top Row: Label and Plus Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ActiveGlowPill(glowColor = NeonPurple)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPersian) "خالص دارایی | NET WORTH" else "NET WORTH",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GreenPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "+12.4%",
                                        color = GreenPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Price and Currency Row (Start-Aligned)
                            Column(horizontalAlignment = Alignment.Start) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = NumberFormatter.formatAmount(
                                            amount = totalNetWorth,
                                            isPersian = isPersian,
                                            usePersianDigits = usePersianDigits,
                                            includeCurrency = false
                                        ),
                                        color = Color.White,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.testTag("net_worth_hero_label")
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPersian) "تومان" else "Toman",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Light,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Equivalent USD and Gold text
                                val usdRate = exchangeRates["USD"] ?: 50000.0
                                val goldRateGrams = exchangeRates["GOLD"] ?: 3000000.0
                                val totalNetWorthUsd = if (usdRate > 0) totalNetWorth / usdRate else 0.0
                                val totalNetWorthGoldGrams = if (goldRateGrams > 0) totalNetWorth / goldRateGrams else 0.0

                                val usdFormatted = NumberFormatter.formatAmount(
                                    amount = totalNetWorthUsd,
                                    isPersian = isPersian,
                                    usePersianDigits = usePersianDigits,
                                    includeCurrency = false
                                )
                                val goldFormatted = if (usePersianDigits) {
                                    NumberFormatter.toPersianDigits(String.format("%.2f", totalNetWorthGoldGrams / 1000.0))
                                } else {
                                    String.format("%.2f", totalNetWorthGoldGrams / 1000.0)
                                }

                                Text(
                                    text = if (isPersian) {
                                        "≈ $$usdFormatted USD  •  $goldFormatted کیلوگرم طلا"
                                    } else {
                                        "≈ $$usdFormatted USD  •  $goldFormatted kg Gold"
                                    },
                                    color = TextSecondary.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Compact alternates micro aggregates
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MiniAggregateField(
                                    textLabel = if (isPersian) "دارایی‌های ناخالص" else "Gross Assets",
                                    valueText = NumberFormatter.formatAmount(totalAssets, isPersian, usePersianDigits),
                                    colorAccent = GreenPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                VerticalDivider(color = GlassBorder, modifier = Modifier.height(30.dp))
                                Spacer(modifier = Modifier.width(1.dp))
                                MiniAggregateField(
                                    textLabel = if (isPersian) "مطالبات" else "Receivables",
                                    valueText = NumberFormatter.formatAmount(totalReceivables, isPersian, usePersianDigits),
                                    colorAccent = NeonYellowGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                VerticalDivider(color = GlassBorder, modifier = Modifier.height(30.dp))
                                Spacer(modifier = Modifier.width(1.dp))
                                MiniAggregateField(
                                    textLabel = if (isPersian) "کل تعهدات" else "Debts Owed",
                                    valueText = NumberFormatter.formatAmount(totalDebts, isPersian, usePersianDigits),
                                    colorAccent = RedNegative,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Micro bar graph matching HTML exactly!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color.White.copy(alpha = 0.05f)))
                                Box(modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color.White.copy(alpha = 0.12f)))
                                Box(modifier = Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(NeonPurple.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.weight(1f).height(14.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color.White.copy(alpha = 0.12f)))
                                Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(NeonPurple))
                            }
                        }
                    }
                }

                // 2. INFLATION LOSS ESTIMATION PANEL (Styled exactly like the HTML banner)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonYellowGreen.copy(alpha = 0.1f))
                        .border(1.dp, NeonYellowGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonYellowGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Inflation trend",
                            tint = DeepDarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPersian) "هوش مصنوعی | AI Insight" else "AI Insight",
                            color = NeonYellowGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPersian) {
                                "تورم قدرت خرید ریال را حدوداً ۴.۸٪ در ماه اخیر کاهش داده است. موجودی نقد شما روزانه معادل **${NumberFormatter.formatAmount(dailyLoss, true, usePersianDigits)}** افت پی‌درپی می‌یابد."
                            } else {
                                "Inflation eroded raw fiat purchasing power by 4.8% last month. Your idle cash loses **${NumberFormatter.formatAmount(dailyLoss, false, usePersianDigits)}** daily."
                            },
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // 3. GRAPH SHEET (NET WORTH CHANGE IN 6-MONTH)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isPersian) "روند زمانی دارایی خالص (۶ ماه گذشته)" else "Wealth Trajectory (Last 6 Months)",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        WealthTrendGraph(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            monthlyNetWorths = monthlyHistory,
                            isPersian = isPersian
                        )
                    }
                }

                // 3.5. ASSET BREAKDOWN (Styled exactly like the HTML)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPersian) "دارایی‌ها | Assets" else "Holdings | Assets",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isPersian) "مشاهده همه" else "View All",
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate("assets_list")
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val goldGrams = remember(assetsList) {
                        assetsList.filter { it.currencyType == "GOLD" }.sumOf { it.quantity }
                    }
                    val btcAmount = remember(assetsList) {
                        assetsList.filter { it.currencyType == "BTC" }.sumOf { it.quantity }
                    }

                    // Gold Card
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 16.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD700))
                                    )
                                }
                                Text(
                                    text = if (isPersian) "طلا | Gold" else "Gold | طلا",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val formattedGoldGrams = if (usePersianDigits) NumberFormatter.toPersianDigits(String.format("%.1f", goldGrams)) else String.format("%.1f", goldGrams)
                            Text(
                                text = if (isPersian) "$formattedGoldGrams گرم" else "$formattedGoldGrams grams",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPersian) "+۲.۱٪ رشد جهانی" else "+2.1% Global Up",
                                color = GreenPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Crypto Card
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 16.dp,
                        glowColor = NeonPurple.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(NeonPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NeonPurple)
                                    )
                                }
                                Text(
                                    text = if (isPersian) "کریپتو | Crypto" else "Crypto | کریپتو",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val formattedBtc = if (usePersianDigits) NumberFormatter.toPersianDigits(String.format("%.4f", btcAmount)) else String.format("%.4f", btcAmount)
                            Text(
                                text = "$formattedBtc BTC",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPersian) "+۵.۸٪ تغییر هفتگی" else "+5.8% Weekly change",
                                color = GreenPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 3.8. DEBT WIDGET (Styled exactly like the HTML widget)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    cornerRadius = 16.dp,
                    onClick = { navController.navigate("debts_list") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPersian) "بدهی‌ها | Total Debt" else "Total Debt | بدهی‌ها",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = NumberFormatter.formatAmount(totalDebts, isPersian, usePersianDigits),
                                color = RedNegative,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Go to Debts",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // 4. FUNCTIONAL PORTALS (GRID SHORTCUTS)
                Text(
                    text = if (isPersian) "درگاه‌های مدیریتی" else "Wealth Operations",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardShortcutTile(
                        icon = Icons.Default.Wallet,
                        title = if (isPersian) "کیف دارایی" else "Wallet Assets",
                        subtitle = if (isPersian) "${assetsList.size} قلم دارایی" else "${assetsList.size} Holdings",
                        accentColor = GreenPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("assets_list") }
                    )
                    DashboardShortcutTile(
                        icon = Icons.Default.Handshake,
                        title = if (isPersian) "مطالبات و بدهی" else "Loans & Debts",
                        subtitle = if (isPersian) "ثبت بدهکاران" else "Lenders & Debtors",
                        accentColor = NeonYellowGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("debts_list") }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardShortcutTile(
                        icon = Icons.Default.Analytics,
                        title = if (isPersian) "تحلیل و آمار" else "Deep Analytics",
                        subtitle = if (isPersian) "نمودار توزیع" else "All Graphs",
                        accentColor = NeonPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("reports_analytics") }
                    )
                    DashboardShortcutTile(
                        icon = Icons.Default.AutoAwesome,
                        title = if (isPersian) "هوش مالی AI" else "AI Consultant",
                        subtitle = if (isPersian) "سیگنال مصون‌سازی" else "Asset Protection",
                        accentColor = NeonPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("ai_insights") }
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationTabItem(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = label,
            tint = if (active) NeonPurple else TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (active) NeonPurple else TextSecondary.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun MiniAggregateField(
    textLabel: String,
    valueText: String,
    colorAccent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = textLabel,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = valueText,
            color = colorAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DashboardShortcutTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        onClick = onClick,
        glowColor = accentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier
                    .size(28.dp)
                    .padding(bottom = 6.dp)
            )
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
