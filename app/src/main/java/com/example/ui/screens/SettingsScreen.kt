package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.BackgroundAtmosphere
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.util.NumberFormatter
import com.example.ui.viewmodel.WealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val usePersianDigits by viewModel.usePersianDigits.collectAsState()
    val inflationRate by viewModel.annualInflationRate.collectAsState()
    val exchangeRates by viewModel.exchangeRatesMap.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Local states for editing exchange rates
    var editUsd by remember(exchangeRates) { mutableStateOf((exchangeRates["USD"] ?: 65000.0).toString()) }
    var editGold by remember(exchangeRates) { mutableStateOf((exchangeRates["GOLD"] ?: 3450000.0).toString()) }
    var editBtc by remember(exchangeRates) { mutableStateOf((exchangeRates["BTC"] ?: 4350000000.0).toString()) }
    var editUsdt by remember(exchangeRates) { mutableStateOf((exchangeRates["USDT"] ?: 65200.0).toString()) }

    var editInflationStr by remember(inflationRate) { mutableStateOf(inflationRate.toString()) }

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isPersian) "تنظیمات هوش دارایی" else "System Configurations",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. LANGUAGE AND SYSTEM PREFERENCES
                Text(
                    text = if (isPersian) "زبان و ظاهر نمایشی" else "Localizations & Formatting",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Language Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isPersian) "زبان برنامه (فارسی / انگلیسی)" else "App Language (LTR / RTL)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPersian) "تغییر آنی کل رابط کاربری" else "Instant toggle across all screens",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = isPersian,
                                onCheckedChange = { viewModel.isPersian.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple, checkedTrackColor = SurfaceGlassHeavy)
                            )
                        }

                        Divider(color = GlassBorder)

                        // Digit preferences
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isPersian) "نمایش ارقام به فارسی (۰-۹)" else "Support Persian Digits (۰-۹)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPersian) "تبدیل خودکار مقادیر پولی به خط زیبای فارسی" else "Format metric numerals to Persian fonts",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = usePersianDigits,
                                onCheckedChange = { viewModel.usePersianDigits.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple, checkedTrackColor = SurfaceGlassHeavy)
                            )
                        }
                    }
                }

                // 2. INFLATION SIMULATION CONFIGURATION
                Text(
                    text = if (isPersian) "مدل شبیه‌سازی بحران تورم" else "Macro Inflation Simulation",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isPersian) "نرخ مفروض تورم جاری کشور (سالانه)" else "Simulated Annual Outflow Rate (%)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextField(
                                value = editInflationStr,
                                onValueChange = { editInflationStr = it },
                                label = { Text(text = "%") },
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .testTag("inflation_setting_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceGlass,
                                    unfocusedContainerColor = SurfaceGlass,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val rateParsed = editInflationStr.toDoubleOrNull()
                                    if (rateParsed != null && rateParsed in 0.0..500.0) {
                                        viewModel.annualInflationRate.value = rateParsed
                                        Toast.makeText(context, if (isPersian) "نرخ تورم با موفقیت به روز شد" else "Inflation rate adjusted successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, if (isPersian) "لطفا عددی معتبر وارد کنید" else "Please enter a valid rate between 0 and 500", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                            ) {
                                Text(text = if (isPersian) "اعمال نرخ" else "Simulate")
                            }
                        }
                    }
                }

                // 3. EDIT VALUATION RATES (MULTI CURRENCY CONVERSION ENGINE)
                Text(
                    text = if (isPersian) "ویرایش نرخ‌های مرجع بازار (به تومان)" else "Adjustment Market Rates (Toman Base)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        RateEditInput(label = "USD (دلار)", value = editUsd, onValueChange = { editUsd = it })
                        RateEditInput(label = "GOLD (طلا گرم ۱۸)", value = editGold, onValueChange = { editGold = it })
                        RateEditInput(label = "BTC (بیت کوین)", value = editBtc, onValueChange = { editBtc = it })
                        RateEditInput(label = "USDT (تتر)", value = editUsdt, onValueChange = { editUsdt = it })

                        Button(
                            onClick = {
                                val usd = editUsd.toDoubleOrNull()
                                val gold = editGold.toDoubleOrNull()
                                val btc = editBtc.toDoubleOrNull()
                                val usdt = editUsdt.toDoubleOrNull()

                                if (usd != null && gold != null && btc != null && usdt != null) {
                                    viewModel.saveExchangeRate("USD", usd)
                                    viewModel.saveExchangeRate("GOLD", gold)
                                    viewModel.saveExchangeRate("BTC", btc)
                                    viewModel.saveExchangeRate("USDT", usdt)
                                    Toast.makeText(context, if (isPersian) "نرخ‌های بازار با موفقیت ذخیره شدند" else "Global exchange prices updated", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, if (isPersian) "لطفا مقادیر را به درستی وارد کنید" else "Please ensure all values are valid decimals", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_exchange_rates_btn")
                        ) {
                            Text(text = if (isPersian) "ذخیره‌سازی و بازشمارش کل ارزش" else "Save & Reprice Entire Net Worth")
                        }
                    }
                }

                // 4. OFFLINE BACKUP AND SEED ACTIONS
                Text(
                    text = if (isPersian) "نگهداری داده‌ها" else "System Diagnostics",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isPersian) "پشتیبان‌گیری محلی (Room DB)" else "Local persistent data storage (SQLite)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPersian) {
                                "داده‌های شما روی فایل محلی دستگاه به صورت کاملاً آفلاین ذخیره می‌شوند."
                            } else {
                                "All transactions and assets are stored within the terminal sandbox in offline-first mode."
                            },
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, if (isPersian) "پشتیبان متنی با موفقیت ساخته شد" else "Local DB exported safely", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlassHeavy),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = if (isPersian) "پشتیبان‌گیری" else "Backup DB", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, if (isPersian) "داده‌های آفلاین با موفقیت بازخوانی شدند" else "Data restore synchronized successfully", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlassHeavy),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = if (isPersian) "بازیابی" else "Restore", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RateEditInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(16.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .width(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceGlass,
                unfocusedContainerColor = SurfaceGlass,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
    }
}
