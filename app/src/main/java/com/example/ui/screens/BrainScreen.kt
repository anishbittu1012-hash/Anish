package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrainThought
import com.example.data.model.GeminiBrainMode
import com.example.ui.JarvisViewModel
import com.example.ui.components.GeminiBrainVisualizer
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrainScreen(
    viewModel: JarvisViewModel,
    onRequestRecordAudioPermission: () -> Unit,
    hasRecordAudioPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telemetry by viewModel.geminiBrainTelemetry.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val isListening by viewModel.speechRecognizerHelper.isListening.collectAsState()
    val thoughts by viewModel.brainThoughts.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    val sampleInquiries = remember {
        listOf(
            "Explain quantum nanotech suit assembly",
            "Calculate Arc Reactor energy output vs Vibranium core",
            "বাংলায় কৃত্রিম বুদ্ধিমত্তা ও ভবিষ্যৎ প্রযুক্তি ব্যাখ্যা করো",
            "Simulate tactical perimeter defenses against hostile drones",
            "Synthesize new vibranium isotope molecular structure",
            "বাংলায় টনি স্টার্কের উক্তি অনুবাদ করো"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("brain_screen")
    ) {
        // 1. Brain Matrix Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Brain",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GEMINI NEURAL BRAIN MATRIX",
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "POWERED BY GOOGLE GEMINI 3.5 FLASH",
                    color = JarvisTextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isThinking) JarvisAmber.copy(alpha = 0.2f) else JarvisGreen.copy(alpha = 0.2f))
                    .border(1.dp, if (isThinking) JarvisAmber else JarvisGreen, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isThinking) "SYNAPSES FIRING" else "ONLINE",
                    color = if (isThinking) JarvisAmber else JarvisGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Holographic Brain Visualizer & Telemetry Stats
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisDarkSurface.copy(alpha = 0.85f))
                .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GeminiBrainVisualizer(
                    isThinking = isThinking,
                    activityLevel = telemetry.neuralActivityLevel,
                    sizeDp = 160.dp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Telemetry Data Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BrainStatCol(
                        label = "MODE",
                        value = telemetry.activeMode.name,
                        tint = primaryColor
                    )
                    BrainStatCol(
                        label = "SYNAPSES",
                        value = "${telemetry.synapsesFiredCount}",
                        tint = JarvisSky
                    )
                    BrainStatCol(
                        label = "LATENCY",
                        value = if (telemetry.lastLatencyMs > 0) "${telemetry.lastLatencyMs}ms" else "READY",
                        tint = JarvisAmber
                    )
                    BrainStatCol(
                        label = "MODEL",
                        value = "GEMINI-3.5",
                        tint = JarvisGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Cognitive Brain Mode Selector Tabs
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
                            if (isSelected) primaryColor.copy(alpha = 0.25f)
                            else JarvisDarkSurface
                        )
                        .border(
                            1.dp,
                            if (isSelected) primaryColor else JarvisCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.setBrainMode(mode) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = mode.name,
                            color = if (isSelected) primaryColor else JarvisTextMuted,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = mode.titleBn,
                            color = if (isSelected) primaryColor.copy(alpha = 0.8f) else JarvisTextMuted.copy(alpha = 0.6f),
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Scrollable Cognitive Thought Stream & Queries
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quick Stimuli Prompts Section
            item {
                Text(
                    text = "NEURAL STIMULI (QUICK QUERIES):",
                    color = JarvisTextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleInquiries.forEach { inquiry ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(JarvisDarkSurface)
                                .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable {
                                    inputPrompt = inquiry
                                    viewModel.queryGeminiBrain(inquiry)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = inquiry,
                                color = JarvisTextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Latest Active Thought Result Card
            if (lastResponse.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("latest_brain_thought_card"),
                        colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GEMINI COGNITION STREAM",
                                        color = primaryColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            viewModel.speakResponse(lastResponse)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Read Aloud",
                                            tint = primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Response", lastResponse))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = JarvisSky,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = lastResponse,
                                color = JarvisTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Past Brain Thoughts
            if (thoughts.isNotEmpty()) {
                item {
                    Text(
                        text = "PAST SYNAPTIC THOUGHTS (${thoughts.size}):",
                        color = JarvisTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                items(thoughts) { thought ->
                    BrainThoughtRow(
                        thought = thought,
                        onSpeak = { viewModel.speakResponse(thought.response) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Thought", thought.response))
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Brain Query Input Console (Text + Voice)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(JarvisDarkSurface)
                .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Input Button
            IconButton(
                onClick = {
                    if (!hasRecordAudioPermission) {
                        onRequestRecordAudioPermission()
                    } else {
                        viewModel.toggleVoiceListening()
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isListening) Color(0xFF10B981) else primaryColor.copy(alpha = 0.15f))
                    .testTag("brain_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isListening) Color.White else primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Query Text Field
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("brain_input_field"),
                placeholder = {
                    Text(
                        text = if (isListening) "Listening to speech..." else "Transmit to Gemini Brain...",
                        color = JarvisTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary
                ),
                singleLine = true
            )

            // Submit Button
            if (isThinking) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(28.dp)
                        .padding(4.dp),
                    color = primaryColor,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = {
                        if (inputPrompt.isNotBlank()) {
                            val textToSend = inputPrompt
                            inputPrompt = ""
                            viewModel.queryGeminiBrain(textToSend)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .testTag("brain_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BrainStatCol(
    label: String,
    value: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = JarvisTextMuted,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = tint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun BrainThoughtRow(
    thought: BrainThought,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface.copy(alpha = 0.9f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q: \"${thought.query}\"",
                    color = JarvisSky,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = JarvisCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = JarvisTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = thought.response,
                color = JarvisTextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MODE: ${thought.mode.name}",
                    color = JarvisTextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${thought.latencyMs}ms",
                    color = JarvisAmber,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
