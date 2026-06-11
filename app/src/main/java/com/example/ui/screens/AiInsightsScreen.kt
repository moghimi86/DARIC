package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.api.GeminiClient
import com.example.ui.components.BackgroundAtmosphere
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.WealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInsightsScreen(
    navController: NavController,
    viewModel: WealthViewModel
) {
    val isPersian by viewModel.isPersian.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()

    val assetsList by viewModel.assets.collectAsState()
    val debtsAll by viewModel.debts.collectAsState()
    val netWorth by viewModel.totalNetWorthInToman.collectAsState()

    val isKeyActive = remember { GeminiClient.isKeyAvailable() }

    val scrollState = rememberScrollState()

    // Run first analysis automatically
    LaunchedEffect(key1 = true) {
        if (aiResponse == null) {
            viewModel.refreshAiInsights()
        }
    }

    BackgroundAtmosphere {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isPersian) "هوش مالی و بهینه‌سازی ثروت" else "AI Wealth Intelligence Desk",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. ALERT WARNINGS DECK (ALWAYS RENDER KEY THREAT FLAGS FIRST)
                Text(
                    text = if (isPersian) "هشدارهای ریسک قدرت خرید" else "Purchasing Power Asset Security Alerts",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                AlertHeuristicCard(
                    title = if (isPersian) "سقوط ارزش ریال جاری" else "Fiat cash losing value faster than assets",
                    desc = if (isPersian) {
                        "سرمایه گذاری در سپرده های مدت دار بانکی بدون پشتوانه طلا، با سرعت ۴۵٪ سالانه در حال فرسایش شدید قرار دارد."
                    } else {
                        "With 45% local inflation, unhedged cash and savings accounts are decaying at a rapid daily pace."
                    },
                    isDanger = true
                )

                AlertHeuristicCard(
                    title = if (isPersian) "مواجه شدن شدید با ریسک فیات" else "Overexposed to fiat currency",
                    desc = if (isPersian) {
                        "بخش بزرگی از سبد شما هنوز مقاوم‌سازی نشده است. تبدیل به طلا یا ارزهای با ثبات توصیه می‌شود."
                    } else {
                        "Cash ratios above recommended levels increase risk exposure. Safe hedge swaps are suggested."
                    },
                    isDanger = true
                )

                // 2. AI LIVE ENGINE ADVICE SHEET
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonPurple
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = NeonYellowGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPersian) "مشاوره اختصاصی هوش مصنوعی" else "Generative AI Tailored Audit",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Indicate state
                            Surface(
                                color = if (isKeyActive) GreenPrimary.copy(alpha = 0.15f) else NeonPurple.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isKeyActive) {
                                        if (isPersian) "مدل آنلاین فعال" else "LLM Model Online"
                                    } else {
                                        if (isPersian) "مدل تحلیل محلی" else "Local Inference Mode"
                                    },
                                    color = if (isKeyActive) GreenPrimary else NeonPurple,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isAiLoading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = NeonPurple)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (isPersian) "هوش مالی در حال ارزیابی سبد شما..." else "Analyzing asset ratios...",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text(
                                text = aiResponse ?: "",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.testTag("ai_insights_output_box")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.refreshAiInsights() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("refresh_ai_insights_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Compute")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (isPersian) "بروزرسانی تحلیل و مصون‌سازی" else "Refresh Portfolio Security Analysis")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertHeuristicCard(
    title: String,
    desc: String,
    isDanger: Boolean
) {
    val themeColor = if (isDanger) RedNegative else NeonYellowGreen
    val icon = if (isDanger) Icons.Default.Warning else Icons.Default.Lightbulb

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        borderWidth = 0.5.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = "Alert", tint = themeColor, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
