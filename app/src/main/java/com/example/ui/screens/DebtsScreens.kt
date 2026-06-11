package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.entity.DebtEntity
import com.example.data.model.CurrencyType
import com.example.ui.components.BackgroundAtmosphere
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.util.NumberFormatter
import com.example.ui.viewmodel.WealthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsListScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val usePersianDigits by viewModel.usePersianDigits.collectAsState()
    val debtsAll by viewModel.debts.collectAsState()

    var showReceivables by remember { mutableStateOf(true) } // True: Receivables, False: Debts
    val context = LocalContext.current

    val filteredList = remember(debtsAll, showReceivables) {
        debtsAll.filter { it.isReceivable == showReceivables }
    }

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isPersian) "مدیریت حساب‌ها و مطالبات" else "Ledger Debts & Receivables",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("add_debt") }) {
                            Icon(imageVector = Icons.Default.AddCard, contentDescription = "Add Debt", tint = NeonYellowGreen)
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
                    .padding(horizontal = 16.dp)
            ) {
                // Glassmorphic Tab Toggle Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGlass)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (showReceivables) NeonPurple else Color.Transparent)
                            .clickable { showReceivables = true }
                            .padding(vertical = 10.dp)
                            .testTag("tab_receivables"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPersian) "طلَب‌ها (مطالبات)" else "Receivables",
                            color = if (showReceivables) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!showReceivables) NeonPurple else Color.Transparent)
                            .clickable { showReceivables = false }
                            .padding(vertical = 10.dp)
                            .testTag("tab_debts"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPersian) "بدهی‌ها (تعهدات)" else "Debts",
                            color = if (!showReceivables) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (showReceivables) Icons.Default.ArrowCircleUp else Icons.Default.ArrowCircleDown,
                                contentDescription = "No ledger",
                                tint = TextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersian) "آرشیوی ثبت نشده است." else "No entries registered in this list.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            DebtGlassCard(
                                item = item,
                                isPersian = isPersian,
                                usePersianDigits = usePersianDigits,
                                onToggleStatus = { viewModel.toggleDebtStatus(item) },
                                onDelete = { viewModel.removeDebt(item) },
                                onShareWhatsApp = {
                                    val template = makeWhatsAppTemplate(item, isPersian, usePersianDigits)
                                    sendWhatsAppIntent(context, template)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DebtGlassCard(
    item: DebtEntity,
    isPersian: Boolean,
    usePersianDigits: Boolean,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val currencyType = remember(item.currencyType) {
        try { CurrencyType.valueOf(item.currencyType) } catch (e: Exception) { CurrencyType.TOMAN }
    }

    val isPaid = item.status == "PAID"
    val dateStr = remember(item.dueDate) {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        sdf.format(Date(item.dueDate))
    }

    val currencySymbol = currencyType.getDisplayName(isPersian)
    val amountFormatted = NumberFormatter.formatAmount(
        amount = item.amount,
        isPersian = isPersian,
        usePersianDigits = usePersianDigits,
        suffix = currencySymbol
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("debt_card_${item.id}"),
        glowColor = if (isPaid) GreenPrimary else if (item.isReceivable) NeonYellowGreen else RedNegative
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Circle Status Icon Indicator
                    IconButton(
                        onClick = onToggleStatus,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isPaid) GreenPrimary.copy(alpha = 0.2f) else SoftNeonBg())
                    ) {
                        Icon(
                            imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = "Toggle Status",
                            tint = if (isPaid) GreenPrimary else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = item.debtorCreditorName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isPaid) TextDecoration.LineThrough else null
                    )
                }

                // Delete Action
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedNegative.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Money Sum display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPersian) "مبلغ قرارداد:" else "Transaction value:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = amountFormatted,
                    color = if (isPaid) GreenPrimary else if (item.isReceivable) NeonYellowGreen else RedNegative,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Due Date Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPersian) "سررسید پرداخت:" else "Settlement date:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = if (usePersianDigits) NumberFormatter.toPersianDigits(dateStr) else dateStr,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Notes Block (if any)
            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = SurfaceGlass,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.notes,
                        color = TextSecondary.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // WhatsApp Remind action row
            if (!isPaid && item.isReceivable) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenPrimary.copy(alpha = 0.15f))
                        .clickable(onClick = onShareWhatsApp)
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SendToMobile,
                        contentDescription = "WhatsApp",
                        tint = GreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersian) "ارسال یادآور تسویه در واتس‌اپ" else "Export Payment Reminder to WhatsApp",
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SoftNeonBg(): Color {
    return SurfaceGlass
}

fun makeWhatsAppTemplate(item: DebtEntity, isPersian: Boolean, usePersianDigits: Boolean): String {
    val currencyLabel = try { CurrencyType.valueOf(item.currencyType).getDisplayName(isPersian) } catch (e: Exception) { "تومان" }
    val money = NumberFormatter.formatAmount(item.amount, isPersian, usePersianDigits, suffix = currencyLabel)
    
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val rawDate = sdf.format(Date(item.dueDate))
    val dateStr = if (usePersianDigits) NumberFormatter.toPersianDigits(rawDate) else rawDate

    return if (isPersian) {
        "سلام جناب ${item.debtorCreditorName}، وقت عالی متعالی.\n" +
        "یک یادآوری محترمانه و دوستانه بابت تسویه حساب تعهدی مالی به مبلغ **$money** با موعد سررسید **$dateStr** خدمت شما ارسال شد.\n" +
        "ممنون میشم در زمان مقرر نسبت به کارسازی اقدام بفرمایید. ارادت با احترام 🌸"
    } else {
        "Hello ${item.debtorCreditorName},\n" +
        "This is a soft friendly reminder regarding our due settlement of **$money** which is set on **$dateStr**.\n" +
        "Please confirm or check your scheduler. Thank you very much!"
    }
}

fun sendWhatsAppIntent(context: Context, text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            `package` = "com.whatsapp"
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to standard generic share sheet
        try {
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Share Reminder"))
        } catch (e2: Exception) {
            Toast.makeText(context, "No sharing programs discovered", Toast.LENGTH_SHORT).show()
        }
    }
}

// --- ADD DEBT SCREEN FORM ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val usePersianDigits by viewModel.usePersianDigits.collectAsState()

    var debtorName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isReceivable by remember { mutableStateOf(true) } // True: Receivable, False: Debt

    var selectedCurrency by remember { mutableStateOf(CurrencyType.TOMAN) }
    var currencyExpanded by remember { mutableStateOf(false) }

    // Date estimation
    val calendar = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableStateOf(calendar.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateStr = remember(selectedDateMillis) {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        sdf.format(Date(selectedDateMillis))
    }

    val isValid = remember(debtorName, amountStr) {
        debtorName.isNotBlank() && amountStr.toDoubleOrNull() != null
    }

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isPersian) "ثبت تعهد مالی جدید" else "Add Financial Ledger Item",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // Selector Row for Receivable vs Debt
                item {
                    Text(
                        text = if (isPersian) "ماهیت تراکنش تعهدی" else "Transaction Nature",
                        color = NeonYellowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceGlass)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isReceivable) NeonPurple else Color.Transparent)
                                .clickable { isReceivable = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPersian) "دیگران به من بدهکارند (طلب)" else "Others Owe Me (Receivable)",
                                color = if (isReceivable) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isReceivable) RedNegative.copy(alpha = 0.8f) else Color.Transparent)
                                .clickable { isReceivable = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isPersian) "من به دیگران بدهکارم (بدهی)" else "I Owe Others (Payable)",
                                color = if (!isReceivable) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Contact Name
                item {
                    Text(
                        text = if (isPersian) "نام طرف معامله (بدهکار/بستانکار)" else "Contact / Party Name",
                        color = NeonYellowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = debtorName,
                        onValueChange = { debtorName = it },
                        label = { Text(text = if (isPersian) "نام کامل" else "Full Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .testTag("add_debt_contact_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceGlass,
                            unfocusedContainerColor = SurfaceGlass,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Amount and currency Selection
                item {
                    Text(
                        text = if (isPersian) "مبلغ و ارز جاری" else "Contract Amount & Currency",
                        color = NeonYellowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text(text = if (isPersian) "مبلغ عددی" else "Amount Value") },
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(10.dp))
                                .testTag("add_debt_amount_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceGlass,
                                unfocusedContainerColor = SurfaceGlass,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        // Currency block selector
                        Box(modifier = Modifier.weight(1f)) {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clickable { currencyExpanded = true },
                                cornerRadius = 10.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedCurrency.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                                }
                            }

                            DropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false },
                                modifier = Modifier
                                    .background(DeepDarkBackground)
                                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            ) {
                                CurrencyType.values().forEach { cur ->
                                    DropdownMenuItem(
                                        text = { Text(text = cur.name, color = Color.White) },
                                        onClick = {
                                            selectedCurrency = cur
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Settlement Date Picker
                item {
                    Text(
                        text = if (isPersian) "سررسید بازپرداخت" else "Maturity Date",
                        color = NeonYellowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        cornerRadius = 10.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Date", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (usePersianDigits) NumberFormatter.toPersianDigits(dateStr) else dateStr,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(imageVector = Icons.Default.EditCalendar, contentDescription = "Edit Date", tint = NeonPurple)
                        }
                    }

                    // Simulated simple date selection (Add 30 / 60 / 90 days shortcuts instead of generic heavy picker)
                    if (showDatePicker) {
                        AlertDialog(
                            onDismissRequest = { showDatePicker = false },
                            containerColor = DeepDarkBackground,
                            title = {
                                Text(
                                    text = if (isPersian) "تعیین زمان سررسید سریع" else "Select Maturity Period",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val cal = Calendar.getInstance()
                                            cal.add(Calendar.DAY_OF_YEAR, 10)
                                            selectedDateMillis = cal.timeInMillis
                                            showDatePicker = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = if (isPersian) "۱۰ روز" else "10 Days", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = {
                                            val cal = Calendar.getInstance()
                                            cal.add(Calendar.MONTH, 1)
                                            selectedDateMillis = cal.timeInMillis
                                            showDatePicker = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = if (isPersian) "۳۰ روز" else "30 Days", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = {
                                            val cal = Calendar.getInstance()
                                            cal.add(Calendar.MONTH, 3)
                                            selectedDateMillis = cal.timeInMillis
                                            showDatePicker = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = if (isPersian) "۹۰ روز" else "90 Days", fontSize = 11.sp)
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text(text = if (isPersian) "بستن" else "Close", color = NeonPurple)
                                }
                            }
                        )
                    }
                }

                // Notes Description
                item {
                    Text(
                        text = if (isPersian) "توضیحات تکمیلی" else "Notes / Ledger References",
                        color = NeonYellowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(text = if (isPersian) "یادداشت..." else "Context details...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .testTag("add_debt_notes_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceGlass,
                            unfocusedContainerColor = SurfaceGlass,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = false
                    )
                }

                // Action Submission
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            viewModel.addDebt(
                                debtorCreditorName = debtorName,
                                amount = amt,
                                currencyType = selectedCurrency,
                                dueDate = selectedDateMillis,
                                isReceivable = isReceivable,
                                notes = notes
                            )
                            navController.navigateUp()
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReceivable) NeonPurple else RedNegative,
                            disabledContainerColor = SurfaceGlassHeavy
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_debt_submit_btn")
                    ) {
                        Text(
                            text = if (isPersian) "ثبت در دفتر حساب تعهدی" else "Submit Outstanding Ledger",
                            fontWeight = FontWeight.Bold,
                            color = if (isValid) Color.White else TextSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
