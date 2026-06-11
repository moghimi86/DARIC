package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.BackgroundAtmosphere
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0.4f) }
    val alphaText = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Run staggered animations
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alphaText.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(800, easing = EaseOutCubic)
        )
        // Hold on splash for 2s
        delay(2000)
        navController.navigate("onboarding") {
            popUpTo("splash") { inclusive = true }
        }
    }

    BackgroundAtmosphere {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(scale.value)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.35f), Color.Transparent),
                        )
                    )
                }
                
                // Tech shield icon
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "App Logo",
                    tint = GreenPrimary,
                    modifier = Modifier.size(75.dp)
                )

                // Neon center accent
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "App Logo Glow",
                    tint = NeonPurple.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(45.dp)
                        .scale(0.85f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(alphaText.value)
            ) {
                Text(
                    text = "دارایی زنده",
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "LIVE WEALTH TRACKER",
                    color = NeonYellowGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "AI-Powered Wealth Intelligence",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}
