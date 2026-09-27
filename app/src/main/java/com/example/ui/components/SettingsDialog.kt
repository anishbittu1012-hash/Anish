package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import com.example.BuildConfig
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.SuitTheme
import com.example.util.VoiceLanguage

@Composable
fun SettingsDialog(
    viewModel: JarvisViewModel,
    hasRecordAudioPermission: Boolean,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Vocal, 1: Advance Settings

    val isTtsAuto by viewModel.isTtsAutoEnabled.collectAsState()
    val activeLang by viewModel.activeLanguage.collectAsState()
    var rate by remember { mutableFloatStateOf(viewModel.ttsManager.speechRate) }
    var pitch by remember { mutableFloatStateOf(viewModel.ttsManager.speechPitch) }

    // Advance Settings States
    val currentSuitTheme by viewModel.suitTheme.collectAsState()
    val animSpeed by viewModel.animationSpeedMultiplier.collectAsState()
    val scanlinesEnabled by viewModel.isHologramScanlinesEnabled.collectAsState()
    val particleIntensity by viewModel.particleIntensity.collectAsState()
    val audioReactivity by viewModel.audioReactivityLevel.collectAsState()
    val persona by viewModel.assistantPersona.collectAsState()
    val fastResponse by viewModel.isFastResponseMode.collectAsState()
    val hapticEnabled by viewModel.isHapticEnabled.collectAsState()

    val accent = currentSuitTheme.primaryColor

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                .testTag("settings_dialog"),
            colors = CardDefaults.cardColors(containerColor = currentSuitTheme.darkSurface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Tune else Icons.Default.AutoAwesome,
                                contentDescription = "Settings",
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "STARK SYSTEM PROTOCOLS",
                                color = accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (selectedTab == 0) "Vocal & Matrix Controls" else "Advance Suit & Neural Settings",
                                color = JarvisTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = JarvisTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs (Vocal vs Advance Settings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(currentSuitTheme.surfaceElevated)
                        .padding(3.dp)
                ) {
                    listOf("VOCAL MATRIX", "ADVANCE SETTINGS").forEachIndexed { index, tabTitle ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accent else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabTitle,
                                color = if (isSelected) Color.Black else JarvisTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedTab == 0) {
                        // TAB 1: VOCAL MATRIX CONTROLS

                        // 1. Language Preference
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Acoustic Language Array",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    VoiceLanguage.AUTO to "Auto",
                                    VoiceLanguage.ENGLISH to "English",
                                    VoiceLanguage.HINDI to "हिंदी",
                                    VoiceLanguage.BENGALI to "বাংলা"
                                ).forEach { (lang, label) ->
                                    val isSelected = activeLang == lang
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) accent else currentSuitTheme.surfaceElevated)
                                            .clickable { viewModel.setLanguage(lang) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.Black else JarvisTextPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Auto-TTS Voice Replies Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatic Vocal Readout",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Synthesizes responses automatically via audio engine",
                                    color = JarvisTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = isTtsAuto,
                                onCheckedChange = { viewModel.toggleTtsAuto() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accent,
                                    checkedTrackColor = accent.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.testTag("toggle_auto_tts")
                            )
                        }

                        // 3. Background Execution Toggle
                        val isBackgroundRunning by viewModel.isBackgroundServiceRunning.collectAsState()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Background Execution Protocol",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Keep active and listening when minimized",
                                    color = JarvisTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = isBackgroundRunning,
                                onCheckedChange = { viewModel.toggleBackgroundProtocol() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accent,
                                    checkedTrackColor = accent.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.testTag("toggle_background_service")
                            )
                        }

                        // 4. Speech Cadence Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Vocal Cadence (Rate)",
                                    color = JarvisTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("%.2fx", rate),
                                    color = accent,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = rate,
                                onValueChange = {
                                    rate = it
                                    viewModel.updateSpeechRate(it)
                                },
                                valueRange = 0.7f..1.4f,
                                colors = SliderDefaults.colors(
                                    thumbColor = accent,
                                    activeTrackColor = accent,
                                    inactiveTrackColor = currentSuitTheme.surfaceElevated
                                ),
                                modifier = Modifier.testTag("slider_speech_rate")
                            )
                        }

                        // 5. Speech Resonance Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Vocal Resonance (Pitch)",
                                    color = JarvisTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("%.2fx", pitch),
                                    color = accent,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = pitch,
                                onValueChange = {
                                    pitch = it
                                    viewModel.updateSpeechPitch(it)
                                },
                                valueRange = 0.7f..1.3f,
                                colors = SliderDefaults.colors(
                                    thumbColor = accent,
                                    activeTrackColor = accent,
                                    inactiveTrackColor = currentSuitTheme.surfaceElevated
                                ),
                                modifier = Modifier.testTag("slider_speech_pitch")
                            )
                        }

                        // Test Audio Button
                        Button(
                            onClick = {
                                val testMsg = when (activeLang) {
                                    VoiceLanguage.HINDI -> "नमस्ते सर, मैं जार्विस हूँ। वोकल टेलीमेट्री और स्पीच सिंथेसिस पूर्ण रूप से सक्रिय हैं।"
                                    VoiceLanguage.BENGALI -> "নমস্কার স্যার, আমি জারভিস। ভোকাল টেলিমেট্রি সম্পূর্ণ স্বাভাবিক এবং কার্যকর আছে।"
                                    else -> "Good evening, Sir. Vocal telemetry is online and calibrated to your exact specifications."
                                }
                                viewModel.replayResponse(testMsg)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("test_voice_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentSuitTheme.surfaceElevated,
                                contentColor = accent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Test Voice",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TEST VOCAL SYNTHESIS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                    } else {
                        // TAB 2: ADVANCE SETTINGS (उन्नत सेटिंग्स)

                        // 1. Armor Suit Theme Selector
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Suit Theme",
                                    tint = accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stark Armor Suit Theme",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            SuitTheme.values().forEach { suit ->
                                val isSelected = currentSuitTheme == suit
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) suit.primaryColor.copy(alpha = 0.2f) else currentSuitTheme.surfaceElevated)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) suit.primaryColor else currentSuitTheme.cardBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setSuitTheme(suit) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(suit.primaryColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = suit.title,
                                                color = JarvisTextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = suit.subtitle,
                                                color = JarvisTextSecondary,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Text(
                                            text = "ENGAGED",
                                            color = suit.primaryColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        // 2. Animation & Particle Speed Multiplier
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Animation Speed",
                                        tint = accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Animation & Arc Speed",
                                        color = JarvisTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = String.format("%.1fx", animSpeed),
                                    color = accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    0.5f to "0.5x Calm",
                                    1.0f to "1.0x Std",
                                    1.5f to "1.5x Warp",
                                    2.0f to "2.0x Over"
                                ).forEach { (factor, label) ->
                                    val isSelected = animSpeed == factor
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) accent else currentSuitTheme.surfaceElevated)
                                            .clickable { viewModel.setAnimationSpeed(factor) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.Black else JarvisTextPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Holographic Scanlines / CRT laser sweep
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Holographic HUD Scanlines",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Animated sci-fi sweep beam and holographic grid",
                                    color = JarvisTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = scanlinesEnabled,
                                onCheckedChange = { viewModel.toggleHologramScanlines() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accent,
                                    checkedTrackColor = accent.copy(alpha = 0.35f)
                                )
                            )
                        }

                        // 4. Quantum Orbital Nanite Particles Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Orbital Nanite Particle Intensity",
                                    color = JarvisTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("%.1fx", particleIntensity),
                                    color = accent,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = particleIntensity,
                                onValueChange = { viewModel.setParticleIntensity(it) },
                                valueRange = 0.0f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = accent,
                                    activeTrackColor = accent,
                                    inactiveTrackColor = currentSuitTheme.surfaceElevated
                                )
                            )
                        }

                        // 5. Audio-Reactive Core Sensitivity
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Audio Decibel Reactivity",
                                    color = JarvisTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("%.1fx", audioReactivity),
                                    color = accent,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = audioReactivity,
                                onValueChange = { viewModel.setAudioReactivityLevel(it) },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = accent,
                                    activeTrackColor = accent,
                                    inactiveTrackColor = currentSuitTheme.surfaceElevated
                                )
                            )
                        }

                        // 6. Assistant Persona Selector
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Persona",
                                    tint = accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Neural Persona Selector",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("JARVIS", "FRIDAY", "EDITH", "KAREN").forEach { name ->
                                    val isSelected = persona == name
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) accent else currentSuitTheme.surfaceElevated)
                                            .clickable { viewModel.setAssistantPersona(name) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            color = if (isSelected) Color.Black else JarvisTextPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // 7. Fast Response & Haptic Feedback Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ultra-Fast Offline Response",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Instant local Stark conversational engine",
                                    color = JarvisTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = fastResponse,
                                onCheckedChange = { viewModel.toggleFastResponseMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accent,
                                    checkedTrackColor = accent.copy(alpha = 0.35f)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tactile Haptic Feedback",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Mechanical vibration pulses during actions",
                                    color = JarvisTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = hapticEnabled,
                                onCheckedChange = { viewModel.toggleHaptic() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accent,
                                    checkedTrackColor = accent.copy(alpha = 0.35f)
                                )
                            )
                        }
                    }

                    // Security & Permission Telemetry Info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(currentSuitTheme.surfaceElevated.copy(alpha = 0.7f))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Mic Status",
                                    tint = if (hasRecordAudioPermission) Color(0xFF10B981) else JarvisAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasRecordAudioPermission) "Microphone: AUTHORIZED" else "Microphone: AUTHORIZATION REQUIRED",
                                    color = if (hasRecordAudioPermission) Color(0xFF10B981) else JarvisAmber,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            val hasKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "API Key",
                                    tint = if (hasKey) Color(0xFF10B981) else accent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasKey) "Gemini Neural Core: ACTIVE" else "Neural Core: LOCAL STARK MATRIX",
                                    color = if (hasKey) Color(0xFF10B981) else accent,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
