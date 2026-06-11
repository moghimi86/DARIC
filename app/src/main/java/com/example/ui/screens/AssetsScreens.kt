package com.example.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.entity.AssetEntity
import com.example.data.model.CurrencyType
import com.example.ui.components.BackgroundAtmosphere
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.util.NumberFormatter
import com.example.ui.viewmodel.WealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsListScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val usePersianDigits by viewModel.usePersianDigits.collectAsState()
    val assetsList by viewModel.assets.collectAsState()
    val exchangeRatesMap by viewModel.exchangeRatesMap.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredAssets = remember(assetsList, searchQuery, isPersian) {
        assetsList.filter {
            val name = if (isPersian) it.nameFa else it.nameEn
            name.contains(searchQuery, ignoreCase = true) || it.currencyType.contains(searchQuery, ignoreCase = true)
        }
    }

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isPersian) "کیف پول دارایی‌ها" else "Asset Securities Wallet",
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
                        IconButton(onClick = { navController.navigate("add_asset") }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = NeonYellowGreen)
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
                // Search field
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (isPersian) "جستجو در دارایی‌ها..." else "Search holdings...",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("search_assets_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceGlass,
                        unfocusedContainerColor = SurfaceGlass,
                        disabledContainerColor = SurfaceGlass,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                if (filteredAssets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = "No asset",
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersian) "هیچ دارایی یافت نشد." else "No assets registered yet.",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredAssets, key = { it.id }) { asset ->
                            // Calculate single asset value in Toman
                            val valueInToman = if (asset.isManualTomanOverride) {
                                asset.currentValueInToman * asset.quantity
                            } else {
                                val rate = exchangeRatesMap[asset.currencyType] ?: 1.0
                                asset.quantity * rate
                            }

                            AssetGlassRow(
                                asset = asset,
                                resolvedValueToman = valueInToman,
                                isPersian = isPersian,
                                usePersianDigits = usePersianDigits,
                                onDelete = { viewModel.removeAsset(asset) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetGlassRow(
    asset: AssetEntity,
    resolvedValueToman: Double,
    isPersian: Boolean,
    usePersianDigits: Boolean,
    onDelete: () -> Unit
) {
    val currencyType = remember(asset.currencyType) {
        try { CurrencyType.valueOf(asset.currencyType) } catch (e: Exception) { CurrencyType.OTHER }
    }

    val iconSymbol = when (currencyType) {
        CurrencyType.GOLD -> Icons.Default.Circle
        CurrencyType.SILVER -> Icons.Default.Circle
        CurrencyType.BTC, CurrencyType.ETH, CurrencyType.USDT -> Icons.Default.CurrencyBitcoin
        CurrencyType.REAL_ESTATE -> Icons.Default.Home
        else -> Icons.Default.AccountBalanceWallet
    }

    val themeColor = when (currencyType) {
        CurrencyType.GOLD -> GoldYellow
        CurrencyType.SILVER -> SilverGray
        CurrencyType.BTC, CurrencyType.ETH -> CryptoOrange
        CurrencyType.USDT, CurrencyType.USD -> GreenPrimary
        CurrencyType.REAL_ESTATE -> NeonYellowGreen
        else -> NeonPurple
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth().testTag("asset_item_${asset.id}")
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
                // Colored Asset Badge Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColor.copy(alpha = 0.15f))
                        .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconSymbol,
                        contentDescription = "Asset Icon",
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isPersian) asset.nameFa else asset.nameEn,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${NumberFormatter.formatDecimal(asset.quantity, usePersianDigits)} ${currencyType.getDisplayName(isPersian)}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Toman equivalence + Delete Trigger
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = NumberFormatter.formatAmount(resolvedValueToman, isPersian, usePersianDigits),
                        color = NeonYellowGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (asset.isManualTomanOverride) {
                        Text(
                            text = if (isPersian) "نرخ دستی" else "Manual override",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedNegative.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// --- ADD ASSET FORM SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val usePersianDigits by viewModel.usePersianDigits.collectAsState()

    var nameEn by remember { mutableStateOf("") }
    var nameFa by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("") }
    var customValueTomanStr by remember { mutableStateOf("") }

    var selectedType by remember { mutableStateOf(CurrencyType.TOMAN) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var isOverrideRate by remember { mutableStateOf(false) }

    val isValid = remember(nameEn, nameFa, quantityStr) {
        nameEn.isNotBlank() && nameFa.isNotBlank() && quantityStr.toDoubleOrNull() != null
    }

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isPersian) "ثبت دارایی جدید" else "Register Asset Securities",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // Asset Title Inputs
                item {
                    Text(
                        text = if (isPersian) "نام و عنوان دارایی" else "Asset Description Names",
                        color = NeonYellowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // English title field
                    TextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text(text = "Asset Title (English)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .testTag("add_asset_name_en"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceGlass,
                            unfocusedContainerColor = SurfaceGlass,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Persian title field
                    TextField(
                        value = nameFa,
                        onValueChange = { nameFa = it },
                        label = { Text(text = "عنوان دارایی (فارسی)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .testTag("add_asset_name_fa"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceGlass,
                            unfocusedContainerColor = SurfaceGlass,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Currency Type selection Dropdown
                item {
                    Text(
                        text = if (isPersian) "نوع دارایی و واحد مالی" else "Asset Classification / Coin Block",
                        color = NeonYellowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = true },
                            cornerRadius = 10.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedType.name} (${selectedType.getDisplayName(isPersian)})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(DeepDarkBackground)
                                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                        ) {
                            CurrencyType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${type.name} - ${type.getDisplayName(isPersian)}",
                                            color = Color.White
                                        )
                                    },
                                    onClick = {
                                        selectedType = type
                                        dropdownExpanded = false
                                        // Auto adjust override settings based on asset selection
                                        if (type == CurrencyType.REAL_ESTATE || type == CurrencyType.OTHER) {
                                            isOverrideRate = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Quantity / Amount Input
                item {
                    Text(
                        text = if (isPersian) "مقدار / تعداد موجودی" else "Holdings Quantity / Weight",
                        color = NeonYellowGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text(text = if (isPersian) "تعداد عددی" else "Numeric quantity") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .testTag("add_asset_quantity"),
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

                // Custom Rate Option / Checkbox
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isOverrideRate = !isOverrideRate },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isOverrideRate,
                            onCheckedChange = { isOverrideRate = it },
                            colors = CheckboxDefaults.colors(checkedColor = NeonPurple, uncheckedColor = TextSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPersian) "تعیین ارزش دستی بر اساس تومان" else "Custom override rates (per unit)",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    AnimatedVisibility(visible = isOverrideRate) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = if (isPersian) "ارزش هر واحد (به تومان)" else "Custom Toman price per single unit",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            TextField(
                                value = customValueTomanStr,
                                onValueChange = { customValueTomanStr = it },
                                label = { Text(text = if (isPersian) "مبلغ مالی به تومان" else "Toman Amount") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .testTag("add_asset_custom_rate"),
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
                }

                // Register Action button
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val qty = quantityStr.toDoubleOrNull() ?: 1.0
                            val customToman = customValueTomanStr.toDoubleOrNull() ?: 0.0
                            viewModel.addAsset(
                                nameEn = nameEn,
                                nameFa = nameFa,
                                quantity = qty,
                                currencyType = selectedType,
                                customValueToman = customToman,
                                isOverride = isOverrideRate
                            )
                            navController.navigateUp()
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPurple,
                            disabledContainerColor = SurfaceGlassHeavy
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_asset_btn_submit")
                    ) {
                        Text(
                            text = if (isPersian) "ثبت قطعی دارایی" else "Submit Security Asset",
                            fontWeight = FontWeight.Bold,
                            color = if (isValid) Color.White else TextSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
