package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.BackgroundAtmosphere
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

data class OnboardingStep(
    val titleEn: String,
    val titleFa: String,
    val descEn: String,
    val descFa: String,
    val statLabelEn: String,
    val statLabelFa: String,
    val statValEn: String,
    val statValFa: String,
    val accentColor: Color
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    isPersianFlow: Boolean,
    onLangToggle: (Boolean) -> Unit
) {
    var stepIndex by remember { mutableStateOf(0) }

    val steps = listOf(
        OnboardingStep(
            titleEn = "Track Real Net Worth",
            titleFa = "سنجش ارگانیک مال و دارایی",
            descEn = "Understand the combined purchasing power of your bank accounts, physical gold, real estate, and digital crypto assets.",
            descFa = "محاسبه خودکار کل ارزش واقعی ثروت شما حاصل جمع سپرده‌های بانکی، مسکوکات طلا، املاک مسکونی و دارایی‌های دیجیتال.",
            statLabelEn = "Net Worth",
            statLabelFa = "ارزش خالص واقعی",
            statValEn = "$12,450 USD",
            statValFa = "۸۴۰,۰۰۰,۰۰۰ تومان",
            accentColor = NeonPurple
        ),
        OnboardingStep(
            titleEn = "Beat Currency Devaluation",
            titleFa = "محافظت در برابر تورم و افت ارزش پول",
            descEn = "In high-inflation economies, cash melts away. Monitor daily degradation of bank cards and track secure hedging instruments.",
            descFa = "در فضا‌های تورمی، ریال نقدی روز‌به‌روز ذوب می‌شود. ضرر روزانه سپرده خود را مشاهده کنید و دارایی‌های امن را شناسایی کنید.",
            statLabelEn = "Annual Inflation",
            statLabelFa = "تورم فرضی سالانه",
            statValEn = "45% Loss of Cash",
            statValFa = "۴۵٪ کاهش ارزش پول نقد",
            accentColor = RedNegative
        ),
        OnboardingStep(
            titleEn = "Live Debt Reminder System",
            titleFa = "سیستم طلب‌کاری و تسویه هوشمند",
            descEn = "Add loans & receivables in any currency with automated WhatsApp templates to remind debtors elegantly.",
            descFa = "طلب‌ها و بدهی‌های خود را در بسترهای چندارزی ثبت کنید و یادآورهای حرفه‌ای جهت ارسال در واتس‌اپ تولید کنید.",
            statLabelEn = "Active Liquidity",
            statLabelFa = "نقدینگی کل فعلی",
            statValEn = "Safe Assets First",
            statValFa = "تمرکز روی ارزش طلا و تتر",
            accentColor = NeonYellowGreen
        )
    )

    val currentStep = steps[stepIndex]

    BackgroundAtmosphere {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row Header (Language Selector)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPersianFlow) "به دارایی زنده خوش آمدید" else "Welcome to Live Wealth",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { onLangToggle(!isPersianFlow) },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("lang_toggle_onboarding")
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = NeonYellowGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersianFlow) "English" else "فارسی",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Central Glass Card Display (Animated Slide Content)
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { if (isPersianFlow) -it else it }
                    ) with fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                        animationSpec = tween(400),
                        targetOffsetX = { if (isPersianFlow) it else -it }
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 24.dp),
                label = "SlideTransition"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .wrapContentHeight(),
                        glowColor = step.accentColor
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isPersianFlow) step.titleFa else step.titleEn,
                                color = TextPrimary,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isPersianFlow) step.descFa else step.descEn,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Highlight Stat Block inside Glass
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceGlassHeavy,
                                modifier = Modifier.widthIn(min = 200.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isPersianFlow) step.statLabelFa else step.statLabelEn,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isPersianFlow) step.statValFa else step.statValEn,
                                        color = step.accentColor,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Indicators & Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0..2) {
                        Box(
                            modifier = Modifier
                                .size(if (i == stepIndex) 20.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (i == stepIndex) NeonPurple else SurfaceGlassHeavy)
                        )
                    }
                }

                // Forward and back navigation
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (stepIndex > 0) {
                        IconButton(
                            onClick = { stepIndex-- },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceGlass)
                        ) {
                            Icon(
                                imageVector = if (isPersianFlow) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (stepIndex < 2) {
                                stepIndex++
                            } else {
                                navController.navigate("dashboard") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text(
                            text = if (stepIndex == 2) {
                                if (isPersianFlow) "تایید و ورود" else "Enter Station"
                            } else {
                                if (isPersianFlow) "بعدی" else "Next"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
