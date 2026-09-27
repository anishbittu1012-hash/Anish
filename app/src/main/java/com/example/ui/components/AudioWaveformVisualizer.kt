package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSky
import kotlin.math.abs
import kotlin.math.sin

/**
 * Advanced Neon Spectrum Audio Equalizer with animated floating peak caps,
 * symmetric frequency harmonics, and reactive audio decibel response.
 */
@Composable
fun AudioWaveformVisualizer(
    isActive: Boolean,
    rmsDb: Float,
    barCount: Int = 24,
    tintColor: Color = JarvisCyan,
    accentColor: Color = JarvisGold,
    animationSpeedMultiplier: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val speed = animationSpeedMultiplier.coerceIn(0.5f, 2.5f)
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (1000 / speed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val normalizedRms = (rmsDb / 11f).coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val totalBars = barCount
        val barWidth = 3.5.dp.toPx()
        val spacing = ((width - (totalBars * barWidth)) / (totalBars - 1)).coerceAtLeast(2f)

        for (i in 0 until totalBars) {
            val progress = i.toFloat() / (totalBars - 1)
            // Gaussian bell curve multiplier for audio spectrum shape
            val bellFactor = sin(progress * Math.PI.toFloat())
            val offset = progress * 3.14f * 2f

            val animatedHeight = if (isActive) {
                val wave = abs(sin(wavePhase + offset))
                val dynamicHeight = (4.dp.toPx() + wave * 14.dp.toPx() * bellFactor + normalizedRms * 16.dp.toPx() * bellFactor)
                dynamicHeight.coerceIn(4.dp.toPx(), height * 0.95f)
            } else {
                3.dp.toPx()
            }

            val x = i * (barWidth + spacing)
            val topY = centerY - (animatedHeight / 2f)

            // Neon gradient brush
            val barBrush = if (isActive) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        tintColor,
                        JarvisSky,
                        tintColor.copy(alpha = 0.7f)
                    ),
                    startY = topY,
                    endY = topY + animatedHeight
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        tintColor.copy(alpha = 0.25f),
                        tintColor.copy(alpha = 0.15f)
                    )
                )
            }

            // Draw bar
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, topY),
                size = Size(barWidth, animatedHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // Floating peak cap when active
            if (isActive && animatedHeight > 8.dp.toPx()) {
                val capY = (topY - 3.dp.toPx()).coerceAtLeast(1f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = barWidth * 0.45f,
                    center = Offset(x + barWidth / 2f, capY)
                )
            }
        }
    }
}
