package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class SynapseNode(
    val id: Int,
    val relX: Float, // Relative -1f to 1f from center
    val relY: Float, // Relative -1f to 1f from center
    val radiusDp: Float,
    val isCore: Boolean = false
)

data class ElectricalSpark(
    val origin: Offset,
    val target: Offset,
    val progress: Float
)

@Composable
fun GeminiBrainVisualizer(
    isThinking: Boolean,
    activityLevel: Float = 0.5f,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 220.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "brain_anim")

    // Continuous synaptic pulse
    val brainPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = if (isThinking) 1.12f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 450 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brainPulse"
    )

    // Action potential phase 0f..1f
    val signalPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 800 else 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "signalPhase"
    )

    // Rotation phase for quantum orbital ring around brain core
    val coreRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 2500 else 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "coreRotate"
    )

    val coroutineScope = rememberCoroutineScope()
    val touchRipples = remember { mutableStateListOf<Offset>() }

    // Predefined synaptic cluster nodes (normalized to unit hemisphere coordinates)
    val nodes = remember {
        listOf(
            // Quantum Neural Core (Center)
            SynapseNode(0, 0f, 0f, 10f, isCore = true),
            // Left Hemisphere
            SynapseNode(1, -0.42f, -0.55f, 5.5f), // Frontal
            SynapseNode(2, -0.72f, -0.22f, 6f),   // Pre-frontal
            SynapseNode(3, -0.58f, 0.18f, 5.5f),  // Temporal
            SynapseNode(4, -0.28f, 0.52f, 6.5f),  // Occipital
            SynapseNode(5, -0.26f, -0.28f, 5f),   // Parietal Inner
            SynapseNode(6, -0.80f, 0.35f, 4.5f),  // Cortex Outer Left
            // Right Hemisphere
            SynapseNode(7, 0.42f, -0.55f, 5.5f),  // Frontal
            SynapseNode(8, 0.72f, -0.22f, 6f),    // Pre-frontal
            SynapseNode(9, 0.58f, 0.18f, 5.5f),   // Temporal
            SynapseNode(10, 0.28f, 0.52f, 6.5f),  // Occipital
            SynapseNode(11, 0.26f, -0.28f, 5f),   // Parietal Inner
            SynapseNode(12, 0.80f, 0.35f, 4.5f),  // Cortex Outer Right
            // Bridge Synapses (Corpus Callosum)
            SynapseNode(13, -0.15f, -0.48f, 4f),
            SynapseNode(14, 0.15f, -0.48f, 4f),
            SynapseNode(15, -0.12f, 0.28f, 4.5f),
            SynapseNode(16, 0.12f, 0.28f, 4.5f)
        )
    }

    // Connectome pairs (synaptic axon pathways)
    val axonConnections = remember {
        listOf(
            0 to 5, 0 to 11, 0 to 15, 0 to 16, 0 to 13, 0 to 14,
            1 to 2, 2 to 3, 3 to 4, 1 to 5, 5 to 3, 2 to 6, 6 to 4,
            7 to 8, 8 to 9, 9 to 10, 7 to 11, 11 to 9, 8 to 12, 12 to 10,
            1 to 13, 7 to 14, 13 to 14, 4 to 15, 10 to 16, 15 to 16
        )
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("gemini_brain_visualizer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        touchRipples.add(offset)
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val scaleRadius = (size.minDimension / 2f) * 0.78f * brainPulse

            // 1. Ambient Background Neural Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (isThinking) 0.32f else 0.16f),
                        secondaryColor.copy(alpha = if (isThinking) 0.15f else 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = scaleRadius * 1.35f
                ),
                center = center,
                radius = scaleRadius * 1.35f
            )

            // 2. Dual Hemisphere Cortical Silhouette Outlines
            // Left Hemisphere Lobes Path
            val leftLobePath = Path().apply {
                moveTo(center.x - 4f, center.y - scaleRadius * 0.88f)
                cubicTo(
                    center.x - scaleRadius * 0.65f, center.y - scaleRadius * 0.95f,
                    center.x - scaleRadius * 1.08f, center.y - scaleRadius * 0.35f,
                    center.x - scaleRadius * 0.95f, center.y + scaleRadius * 0.10f
                )
                cubicTo(
                    center.x - scaleRadius * 1.05f, center.y + scaleRadius * 0.45f,
                    center.x - scaleRadius * 0.65f, center.y + scaleRadius * 0.90f,
                    center.x - 4f, center.y + scaleRadius * 0.78f
                )
            }

            // Right Hemisphere Lobes Path
            val rightLobePath = Path().apply {
                moveTo(center.x + 4f, center.y - scaleRadius * 0.88f)
                cubicTo(
                    center.x + scaleRadius * 0.65f, center.y - scaleRadius * 0.95f,
                    center.x + scaleRadius * 1.08f, center.y - scaleRadius * 0.35f,
                    center.x + scaleRadius * 0.95f, center.y + scaleRadius * 0.10f
                )
                cubicTo(
                    center.x + scaleRadius * 1.05f, center.y + scaleRadius * 0.45f,
                    center.x + scaleRadius * 0.65f, center.y + scaleRadius * 0.90f,
                    center.x + 4f, center.y + scaleRadius * 0.78f
                )
            }

            val strokeWidth = 1.8.dp.toPx()
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = signalPhase * 40f)

            drawPath(
                path = leftLobePath,
                color = primaryColor.copy(alpha = 0.55f),
                style = Stroke(width = strokeWidth, pathEffect = dashedEffect, cap = StrokeCap.Round)
            )
            drawPath(
                path = rightLobePath,
                color = primaryColor.copy(alpha = 0.55f),
                style = Stroke(width = strokeWidth, pathEffect = dashedEffect, cap = StrokeCap.Round)
            )

            // 3. Central Synaptic Fissure (Longitudinal line)
            drawLine(
                color = primaryColor.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - scaleRadius * 0.85f),
                end = Offset(center.x, center.y + scaleRadius * 0.75f),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )

            // Map node IDs to screen pixel offsets
            val nodeOffsets = nodes.associate { node ->
                val px = center.x + node.relX * scaleRadius
                val py = center.y + node.relY * scaleRadius
                node.id to Offset(px, py)
            }

            // 4. Draw Axon Pathways (Interconnecting Synapses)
            axonConnections.forEach { (fromId, toId) ->
                val p1 = nodeOffsets[fromId] ?: return@forEach
                val p2 = nodeOffsets[toId] ?: return@forEach

                // Synapse filament line
                drawLine(
                    color = primaryColor.copy(alpha = if (isThinking) 0.45f else 0.22f),
                    start = p1,
                    end = p2,
                    strokeWidth = (if (isThinking) 1.6f else 1.1f).dp.toPx()
                )

                // Action Potential Surge (Moving Particle along axon)
                val t = (signalPhase + (fromId * 0.17f)) % 1f
                val sparkX = p1.x + (p2.x - p1.x) * t
                val sparkY = p1.y + (p2.y - p1.y) * t
                val sparkAlpha = sin(t * Math.PI.toFloat()).coerceIn(0f, 1f)

                drawCircle(
                    color = Color.White.copy(alpha = sparkAlpha * 0.85f),
                    radius = (if (isThinking) 2.6f else 1.8f).dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = sparkAlpha * 0.45f),
                    radius = (if (isThinking) 5.5f else 3.5f).dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
            }

            // 5. Draw Synapse Nodes
            nodes.forEach { node ->
                val pos = nodeOffsets[node.id] ?: return@forEach
                val baseRadius = node.radiusDp.dp.toPx()

                if (node.isCore) {
                    // Quantum Core Node: Multi-layered pulsing halo
                    val coreRadius = baseRadius * (if (isThinking) 1.25f else 1.05f) * brainPulse
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                primaryColor,
                                secondaryColor.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = pos,
                            radius = coreRadius * 2.2f
                        ),
                        center = pos,
                        radius = coreRadius * 2.2f
                    )
                    drawCircle(
                        color = Color.White,
                        radius = coreRadius * 0.65f,
                        center = pos
                    )

                    // Quantum Orbital Gyro Ring
                    val rad = Math.toRadians(coreRotate.toDouble())
                    val ringRadius = coreRadius * 1.6f
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.7f),
                        radius = ringRadius,
                        center = pos,
                        style = Stroke(width = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                    val orbX = pos.x + ringRadius * cos(rad).toFloat()
                    val orbY = pos.y + ringRadius * sin(rad).toFloat()
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(orbX, orbY)
                    )
                } else {
                    // Peripheral Synapse Node
                    val pulse = if (isThinking) (1f + 0.3f * sin(signalPhase * 6.28f + node.id)) else 1f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = pos,
                            radius = baseRadius * 1.8f * pulse
                        ),
                        center = pos,
                        radius = baseRadius * 1.8f * pulse
                    )
                    drawCircle(
                        color = if (isThinking) Color.White else primaryColor,
                        radius = baseRadius * 0.65f * pulse,
                        center = pos
                    )
                }
            }

            // 6. Draw Interactive Touch Spark Ripples
            touchRipples.removeAll { ripple ->
                drawCircle(
                    color = primaryColor.copy(alpha = 0.4f),
                    radius = 22.dp.toPx(),
                    center = ripple,
                    style = Stroke(width = 2.dp.toPx())
                )
                true // remove after drawing one burst
            }
        }
    }
}
