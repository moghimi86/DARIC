package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AssetRingChart(
    modifier: Modifier = Modifier,
    cashAmount: Double,
    hardAssetsAmount: Double,
    debtsAmount: Double,
    receivablesAmount: Double,
    isPersian: Boolean,
    totalLabel: String
) {
    val total = (cashAmount + hardAssetsAmount + debtsAmount + receivablesAmount).coerceAtLeast(1.0)
    
    val pCash = (cashAmount / total).toFloat()
    val pHard = (hardAssetsAmount / total).toFloat()
    val pDebts = (debtsAmount / total).toFloat()
    val pRecs = (receivablesAmount / total).toFloat()

    val segments = listOf(
        pCash to GreenPrimary,
        pHard to GoldYellow,
        pRecs to NeonYellowGreen,
        pDebts to NeonPurple
    ).filter { it.first > 0f }

    val animateSweep = remember { Animatable(0f) }
    LaunchedEffect(segments) {
        animateSweep.animateTo(
            targetValue = 360f,
            animationSpec = tween(1200, easing = EaseInOutCubic)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            var startAngle = -90f
            val strokeWidth = 14.dp.toPx()
            
            for (seg in segments) {
                val sweep = seg.first * animateSweep.value
                drawArc(
                    color = seg.second,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                )
                startAngle += seg.first * 360f
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isPersian) "ساختار مالی" else "Balance Allocation",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = totalLabel,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WealthTrendGraph(
    modifier: Modifier = Modifier,
    monthlyNetWorths: List<Double>, // 6 historical values
    isPersian: Boolean
) {
    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(monthlyNetWorths) {
        animateProgress.animateTo(1f, animationSpec = tween(1400, easing = EaseInOutQuart))
    }

    val maxVal = monthlyNetWorths.maxOrNull()?.coerceAtLeast(1.0) ?: 100.0
    val minVal = monthlyNetWorths.minOrNull() ?: 0.0
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    val pointsCount = monthlyNetWorths.size

    val monthsEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthsFa = listOf("دی", "بهمن", "اسفند", "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر")

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val width = size.width
            val height = size.height

            if (pointsCount < 2) return@Canvas

            val stepX = width / (pointsCount - 1)
            val linePath = Path()
            val fillPath = Path()

            val getCoordinates: (Int) -> Pair<Float, Float> = { index ->
                val x = index * stepX
                val normalizedY = (monthlyNetWorths[index] - minVal) / range
                val y = height - (normalizedY.toFloat() * height * 0.8f) - (height * 0.1f)
                x to y
            }

            // Move to first coordinate
            val (firstX, firstY) = getCoordinates(0)
            linePath.moveTo(firstX, firstY)
            fillPath.moveTo(firstX, height)
            fillPath.lineTo(firstX, firstY)

            for (i in 1 until pointsCount) {
                val (px, py) = getCoordinates(i)
                // Interpolated drawing progress
                val currentDrawProgress = (animateProgress.value * (pointsCount - 1))
                if (i <= currentDrawProgress) {
                    linePath.lineTo(px, py)
                    fillPath.lineTo(px, py)
                } else {
                    val prevIndex = i - 1
                    val (prevX, prevY) = getCoordinates(prevIndex)
                    val segmentProgress = currentDrawProgress - prevIndex
                    if (segmentProgress > 0f) {
                        val interpolatedX = prevX + (px - prevX) * segmentProgress
                        val interpolatedY = prevY + (py - prevY) * segmentProgress
                        linePath.lineTo(interpolatedX, interpolatedY)
                        fillPath.lineTo(interpolatedX, interpolatedY)
                    }
                    break
                }
            }

            // Cap off fill path
            val lastDrawnIndex = Math.min((animateProgress.value * (pointsCount - 1)).toInt() + 1, pointsCount - 1)
            val currentProgressX = if (animateProgress.value >= 1f) width else {
                val prevIdx = Math.max(0, lastDrawnIndex - 1)
                val (prevX, _) = getCoordinates(prevIdx)
                val (nextX, _) = getCoordinates(lastDrawnIndex)
                val progressOffset = (animateProgress.value * (pointsCount - 1)) - prevIdx
                prevX + (nextX - prevX) * progressOffset
            }
            fillPath.lineTo(currentProgressX, height)
            fillPath.close()

            // Draw beautiful translucent gradient fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(NeonPurple.copy(alpha = 0.28f), Color.Transparent)
                )
            )

            // Draw glowing smooth neon graph line
            drawPath(
                path = linePath,
                color = NeonPurple,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw vertices
            for (i in 0 until pointsCount) {
                if (i <= animateProgress.value * (pointsCount - 1)) {
                    val (px, py) = getCoordinates(i)
                    drawCircle(
                        color = NeonYellowGreen,
                        radius = 4.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }

        // Draw dynamic month timeline along the horizontal axis
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0 until pointsCount) {
                val label = if (isPersian) monthsFa[i % 12] else monthsEn[i % 12]
                Text(
                    text = label,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
