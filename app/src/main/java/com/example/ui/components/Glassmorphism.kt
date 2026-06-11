package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .drawBehind {
                if (glowColor != null) {
                    // Soft glowing spotlight behind the card
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = 0.15f), Color.Transparent),
                            radius = size.maxDimension / 2f
                        ),
                        radius = size.maxDimension * 0.45f,
                        center = this.center
                    )
                }
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceGlass,
                        SurfaceGlass.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                border = BorderStroke(
                    borderWidth,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassBorder,
                            GlassBorder.copy(alpha = 0.03f)
                        )
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .then(clickableModifier)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ActiveGlowPill(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonPurple
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeonPillGlow")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaGlow"
    )

    Box(
        modifier = modifier
            .width(8.dp)
            .height(8.dp)
            .drawBehind {
                drawCircle(
                    color = glowColor.copy(alpha = alphaAnim * 0.4f),
                    radius = size.maxDimension * 2.2f
                )
                drawCircle(
                    color = glowColor,
                    radius = size.maxDimension * 0.8f
                )
            }
    )
}

@Composable
fun BackgroundAtmosphere(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepDarkBackground)
            .drawBehind {
                // Large radial glows at top-left and bottom-right to enrich the dark theme
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.12f), Color.Transparent),
                        radius = size.maxDimension * 0.6f
                    ),
                    center = androidx.compose.ui.geometry.Offset(x = size.width * 0.1f, y = size.height * 0.1f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GreenPrimary.copy(alpha = 0.09f), Color.Transparent),
                        radius = size.maxDimension * 0.5f
                    ),
                    center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.8f)
                )
            }
    ) {
        content()
    }
}
