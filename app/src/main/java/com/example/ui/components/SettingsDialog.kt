package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import kotlinx.coroutines.launch
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
    onLaunchCamera: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Vocal, 1: API & Search, 2: Advance Suit

    val isTtsAuto by viewModel.isTtsAutoEnabled.collectAsState()
    val activeLang by viewModel.activeLanguage.collectAsState()
    val activePreset by viewModel.ttsPreset.collectAsState()
    val isSoundEffectsEnabled by viewModel.isSoundEffectsEnabled.collectAsState()
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

    // API & Web Search Configuration State
    val savedGeminiKey by viewModel.customGeminiApiKey.collectAsState()
    val savedSearchKey by viewModel.customSearchApiKey.collectAsState()
    var geminiKeyInput by remember(savedGeminiKey) { mutableStateOf(savedGeminiKey) }
    var searchKeyInput by remember(savedSearchKey) { mutableStateOf(savedSearchKey) }
    var isGeminiKeyVisible by remember { mutableStateOf(false) }
    var geminiTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingGemini by remember { mutableStateOf(false) }
    var searchTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingSearch by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                                imageVector = when (selectedTab) {
                                    0 -> Icons.Default.Tune
                                    1 -> Icons.Default.Key
                                    else -> Icons.Default.AutoAwesome
                                },
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
                                text = when (selectedTab) {
                                    0 -> "Vocal & Matrix Controls"
                                    1 -> "Gemini LLM & Web Search API"
                                    else -> "Advance Suit & Neural Settings"
                                },
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
                    listOf("VOCAL MATRIX", "API & SEARCH", "ADVANCE SUIT").forEachIndexed { index, tabTitle ->
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
                                fontSize = 9.5.sp,
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

                        // 2. Voice Personality Profile (Natural & Non-Robotic)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Voice Profile",
                                    tint = accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Natural Voice Profile (Non-Robotic)",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    com.example.util.RoboticVoicePreset.NATURAL_FRIENDLY,
                                    com.example.util.RoboticVoicePreset.WARM_COMPANION,
                                    com.example.util.RoboticVoicePreset.CALM_GENTLE,
                                    com.example.util.RoboticVoicePreset.CRISP_TACTICAL
                                ).forEach { preset ->
                                    val isSelected = activePreset == preset
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) accent else currentSuitTheme.surfaceElevated)
                                            .clickable { viewModel.setVoicePreset(preset) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = preset.displayName,
                                            color = if (isSelected) Color.Black else JarvisTextPrimary,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Sound Effects (Audio Chimes) Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sound Effects & Audio Chimes",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Melodic feedback when listening, sending commands, and replying",
                                    color = JarvisTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = isSoundEffectsEnabled,
                                onCheckedChange = { viewModel.toggleSoundEffects() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accent,
                                    checkedTrackColor = accent.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.testTag("toggle_sound_effects")
                            )
                        }

                        // 4. Auto-TTS Voice Replies Toggle
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
                                    text = "Synthesizes responses automatically via natural audio engine",
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

                        // 5. Background Execution Toggle
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

                        // 6. Speech Cadence Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Vocal Cadence (Speed)",
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

                        // 7. Speech Resonance Slider
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
                                    VoiceLanguage.HINDI -> "नमस्ते! मैं आपका वॉइस एআই असिस्टेंट हूँ। मैं हिंदी, बंगाली और अंग्रेजी में आपसे स्वाभाविक अंदाज़ में बात कर सकता हूँ।"
                                    VoiceLanguage.BENGALI -> "নমস্কার! আমি আপনার ভয়েস এআই সহকারী। আমি বাংলা, হিন্দি ও ইংরেজিতে সম্পূর্ণ স্বাভাবিক ও বন্ধুত্বপূর্ণ কণ্ঠে কথা বলতে পারি।"
                                    else -> "Hello! I am your natural voice AI assistant. I understand and respond in English, Bengali, and Hindi with friendly human speech."
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
                                text = "TEST NATURAL VOICE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                    } else if (selectedTab == 1) {
                        // TAB 2: API KEY & WEB SEARCH CONFIGURATION
                        val savedGeminiKey by viewModel.customGeminiApiKey.collectAsState()
                        val savedSearchKey by viewModel.customSearchApiKey.collectAsState()
                        var geminiKeyInput by remember(savedGeminiKey) { mutableStateOf(savedGeminiKey) }
                        var searchKeyInput by remember(savedSearchKey) { mutableStateOf(savedSearchKey) }
                        var isGeminiKeyVisible by remember { mutableStateOf(false) }
                        var geminiTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
                        var isTestingGemini by remember { mutableStateOf(false) }
                        var searchTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
                        var isTestingSearch by remember { mutableStateOf(false) }
                        val coroutineScope = rememberCoroutineScope()

                        val effectiveKey = viewModel.getEffectiveGeminiApiKey()
                        val hasKey = effectiveKey.isNotBlank()

                        // 1. Google Gemini LLM API Card
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("gemini_api_config_card"),
                            colors = CardDefaults.cardColors(containerColor = currentSuitTheme.surfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
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
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "GEMINI LLM API",
                                            color = accent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (hasKey) Color(0xFF10B981).copy(alpha = 0.2f) else JarvisAmber.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (hasKey) "ACTIVE: GEMINI 3.5 FLASH" else "KEY REQUIRED",
                                            color = if (hasKey) Color(0xFF10B981) else JarvisAmber,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Drives conversational intelligence in Bengali, Hindi, & English with multi-turn memory.",
                                    color = JarvisTextSecondary,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = geminiKeyInput,
                                    onValueChange = { geminiKeyInput = it },
                                    label = { Text("Gemini API Key", fontSize = 10.sp) },
                                    placeholder = { Text("Enter AIzaSy... or use Secrets Panel", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("gemini_api_key_input"),
                                    singleLine = true,
                                    visualTransformation = if (isGeminiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isGeminiKeyVisible = !isGeminiKeyVisible }) {
                                            Icon(
                                                imageVector = if (isGeminiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle Visibility",
                                                tint = JarvisTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accent,
                                        unfocusedBorderColor = accent.copy(alpha = 0.4f),
                                        focusedTextColor = JarvisTextPrimary,
                                        unfocusedTextColor = JarvisTextPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.saveApiConfiguration(geminiKeyInput, searchKeyInput)
                                            Toast.makeText(context, "API Key saved successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("save_gemini_key_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SAVE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                isTestingGemini = true
                                                geminiTestResult = null
                                                geminiTestResult = viewModel.testGeminiConnection(geminiKeyInput)
                                                isTestingGemini = false
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f).testTag("test_gemini_connection_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, accent)
                                    ) {
                                        if (isTestingGemini) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = accent, strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("TESTING...", color = accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        } else {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = accent)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("TEST CONNECTION", color = accent, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                if (geminiTestResult != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val (success, message) = geminiTestResult!!
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (success) Color(0xFF10B981).copy(alpha = 0.15f) else JarvisAmber.copy(alpha = 0.15f))
                                            .border(1.dp, if (success) Color(0xFF10B981) else JarvisAmber, RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                                contentDescription = null,
                                                tint = if (success) Color(0xFF10B981) else JarvisAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = message,
                                                color = if (success) Color(0xFF10B981) else JarvisAmber,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Real-Time Web Search & Weather Configuration Card
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("web_search_config_card"),
                            colors = CardDefaults.cardColors(containerColor = currentSuitTheme.surfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSky.copy(alpha = 0.5f)),
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
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = JarvisSky,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "WEB SEARCH & WEATHER",
                                            color = JarvisSky,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "REAL-TIME READY",
                                            color = Color(0xFF10B981),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Built-in Open-Meteo & Live Web Knowledge feeds current news, facts, and live weather worldwide directly into Gemini.",
                                    color = JarvisTextSecondary,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = searchKeyInput,
                                    onValueChange = { searchKeyInput = it },
                                    label = { Text("Custom Search API Key (Optional)", fontSize = 10.sp) },
                                    placeholder = { Text("Default: Built-in Live World Engine", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("custom_search_key_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JarvisSky,
                                        unfocusedBorderColor = JarvisSky.copy(alpha = 0.4f),
                                        focusedTextColor = JarvisTextPrimary,
                                        unfocusedTextColor = JarvisTextPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.saveApiConfiguration(geminiKeyInput, searchKeyInput)
                                            Toast.makeText(context, "Search configuration saved", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("save_search_config_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSky)
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SAVE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                isTestingSearch = true
                                                searchTestResult = null
                                                searchTestResult = viewModel.testWebSearchConnection()
                                                isTestingSearch = false
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f).testTag("test_web_search_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSky)
                                    ) {
                                        if (isTestingSearch) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = JarvisSky, strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("TESTING...", color = JarvisSky, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        } else {
                                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp), tint = JarvisSky)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("TEST SEARCH", color = JarvisSky, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                if (searchTestResult != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val (success, message) = searchTestResult!!
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (success) Color(0xFF10B981).copy(alpha = 0.15f) else JarvisAmber.copy(alpha = 0.15f))
                                            .border(1.dp, if (success) Color(0xFF10B981) else JarvisAmber, RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                                contentDescription = null,
                                                tint = if (success) Color(0xFF10B981) else JarvisAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = message,
                                                color = if (success) Color(0xFF10B981) else JarvisAmber,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Camera & Optical Visual Sensor Telemetry Card
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("camera_telemetry_card"),
                            colors = CardDefaults.cardColors(containerColor = currentSuitTheme.surfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
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
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = accent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "OPTICAL CAMERA SENSOR",
                                            color = accent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Live optical camera input for real-time visual telemetry and diagnostic scanning.",
                                    color = JarvisTextSecondary,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = onLaunchCamera,
                                    modifier = Modifier.fillMaxWidth().testTag("launch_camera_scan_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, accent)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = accent)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ACTIVATE CAMERA / VISUAL SCAN",
                                        color = accent,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
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
