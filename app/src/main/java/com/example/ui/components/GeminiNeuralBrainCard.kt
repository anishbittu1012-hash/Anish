package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeminiBrainMode
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiNeuralBrainCard(
    viewModel: JarvisViewModel,
    onOpenBrainScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.geminiBrainTelemetry.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary

    val quickCognitivePrompts = listOf(
        "Analyze Mark LXXXV power matrix",
        "বাংলায় মহাবিশ্বের সৃষ্টি সম্পর্কে বলো",
        "Quantum superposition explanation",
        "Run threat perimeter diagnostic"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JarvisDarkSurface.copy(alpha = 0.94f),
                        JarvisSurfaceElevated.copy(alpha = 0.96f)
                    )
                )
            )
            .border(1.dp, primaryColor.copy(alpha = 0.38f), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("gemini_neural_brain_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Brand Identity & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, primaryColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Gemini Brain",
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GEMINI NEURAL BRAIN",
                                color = primaryColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(primaryColor.copy(alpha = 0.18f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "3.5 FLASH",
                                    color = primaryColor,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = "STARK COGNITIVE SYNAPSE MATRIX",
                            color = JarvisTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Synapse Pulse Status Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isThinking) JarvisAmber.copy(alpha = 0.18f)
                            else JarvisGreen.copy(alpha = 0.18f)
                        )
                        .border(
                            1.dp,
                            if (isThinking) JarvisAmber.copy(alpha = 0.5f)
                            else JarvisGreen.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isThinking) JarvisAmber else JarvisGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isThinking) "FIRING SYNAPSES" else "ONLINE",
                            color = if (isThinking) JarvisAmber else JarvisGreen,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle Visualizer + Live Telemetry Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Holographic Brain
                GeminiBrainVisualizer(
                    isThinking = isThinking,
                    activityLevel = telemetry.neuralActivityLevel,
                    sizeDp = 96.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Synaptic Metrics Grid
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "ACTIVE MODE",
                            value = telemetry.activeMode.title.split(" ").firstOrNull() ?: "BUTLER",
                            tint = primaryColor
                        )
                        MetricItem(
                            label = "SYNAPSES",
                            value = "${telemetry.synapsesFiredCount}",
                            tint = JarvisSky
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "LATENCY",
                            value = if (telemetry.lastLatencyMs > 0) "${telemetry.lastLatencyMs}ms" else "READY",
                            tint = JarvisAmber
                        )
                        MetricItem(
                            label = "CORE TEMP",
                            value = "${(telemetry.activeMode.temperature * 100).toInt()}%",
                            tint = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cognitive Modes Selector Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GeminiBrainMode.values().forEach { mode ->
                    val isSelected = telemetry.activeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) primaryColor.copy(alpha = 0.22f)
                                else JarvisDarkSurface
                            )
                            .border(
                                1.dp,
                                if (isSelected) primaryColor else JarvisTextMuted.copy(alpha = 0.25f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setBrainMode(mode) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name,
                            color = if (isSelected) primaryColor else JarvisTextMuted,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Prompt Chips
            Text(
                text = "NEURAL STIMULI (QUICK COGNITION)",
                color = JarvisTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickCognitivePrompts.forEach { chipPrompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(JarvisDarkSurface)
                            .border(1.dp, primaryColor.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.queryGeminiBrain(chipPrompt) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("brain_chip_${chipPrompt.take(10).replace(" ", "_")}")
                    ) {
                        Text(
                            text = chipPrompt,
                            color = JarvisTextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Open Full Neural Matrix Button
            OutlinedButton(
                onClick = onOpenBrainScreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_neural_brain_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryColor
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OPEN FULL GEMINI NEURAL MATRIX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    tint: Color
) {
    Column {
        Text(
            text = label,
            color = JarvisTextMuted,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
