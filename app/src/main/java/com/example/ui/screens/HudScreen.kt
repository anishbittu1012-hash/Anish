package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorCore
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.util.AmbientLightingState
import com.example.util.VoiceLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HudScreen(
    viewModel: JarvisViewModel,
    onRequestRecordAudioPermission: () -> Unit,
    hasRecordAudioPermission: Boolean,
    onLaunchSystemVoiceDialog: () -> Unit = {},
    onToggleBackgroundService: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val statusText by viewModel.statusText.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val reactorPower by viewModel.reactorPower.collectAsState()
    val activeProtocolBanner by viewModel.activeProtocolBanner.collectAsState()
    val activeLanguage by viewModel.activeLanguage.collectAsState()

    val isListening by viewModel.speechRecognizerHelper.isListening.collectAsState()
    val rmsDb by viewModel.speechRecognizerHelper.rmsDb.collectAsState()
    val liveHypothesis by viewModel.speechRecognizerHelper.liveHypothesis.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val isBackgroundRunning by viewModel.isBackgroundServiceRunning.collectAsState()

    val recognizedCommand by viewModel.recognizedCommand.collectAsState()
    val speechError by viewModel.speechError.collectAsState()
    val ambientLightingState by viewModel.ambientLightingState.collectAsState()

    val suitTheme by viewModel.suitTheme.collectAsState()
    val animSpeed by viewModel.animationSpeedMultiplier.collectAsState()
    val scanlinesEnabled by viewModel.isHologramScanlinesEnabled.collectAsState()
    val particleIntensity by viewModel.particleIntensity.collectAsState()
    val assistantPersona by viewModel.assistantPersona.collectAsState()

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Active Protocol Alert Banner
        AnimatedVisibility(
            visible = activeProtocolBanner != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisAmber.copy(alpha = 0.15f))
                    .border(1.dp, JarvisAmber, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("protocol_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[PROTOCOL ENGAGED]: ${activeProtocolBanner?.uppercase()}",
                        color = JarvisAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SEC-AUTH: 1701-D",
                        color = JarvisAmber.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Language Mode Bar (English / Bengali / Auto)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(JarvisDarkSurface)
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("language_selector_bar"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Voice Language",
                    tint = JarvisCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RECOGNITION:",
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    VoiceLanguage.AUTO to "AUTO",
                    VoiceLanguage.ENGLISH to "ENG",
                    VoiceLanguage.HINDI to "हिंदी",
                    VoiceLanguage.BENGALI to "বাংলা"
                ).forEach { (lang, label) ->
                    val isSelected = activeLanguage == lang
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) JarvisCyan else JarvisSurfaceElevated)
                            .clickable { viewModel.setLanguage(lang) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("lang_btn_${label.lowercase()}")
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

        Spacer(modifier = Modifier.height(10.dp))

        // Microphone Permission Warning Banner (if permission not yet granted)
        if (!hasRecordAudioPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2E1500))
                    .border(1.dp, JarvisAmber, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .testTag("mic_permission_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Mic Denied",
                            tint = JarvisAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "MICROPHONE ACCESS REQUIRED",
                                color = JarvisAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Grant authorization to receive English & বাংলা commands",
                                color = Color(0xFFFFE0B2),
                                fontSize = 9.sp
                            )
                        }
                    }

                    Button(
                        onClick = onRequestRecordAudioPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("grant_mic_permission_button")
                    ) {
                        Text(text = "GRANT", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Futuristic Arc Reactor Core Visualizer with Dynamic Ambient Light Tuning
        ArcReactorCore(
            isListening = isListening,
            isSpeaking = isSpeaking,
            isThinking = isThinking,
            rmsDb = rmsDb,
            powerPercent = reactorPower,
            brightnessMultiplier = ambientLightingState.brightnessMultiplier,
            glowIntensity = ambientLightingState.glowIntensity,
            animationSpeedMultiplier = animSpeed,
            particleIntensity = particleIntensity,
            holographicSweepEnabled = scanlinesEnabled,
            primaryColor = suitTheme.primaryColor,
            secondaryColor = suitTheme.secondaryColor,
            accentColor = suitTheme.accentColor,
            ambientLux = ambientLightingState.currentLux,
            onCoreClick = {
                if (hasRecordAudioPermission) {
                    viewModel.toggleVoiceListening()
                } else {
                    onRequestRecordAudioPermission()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Cybernetic Audio Equalizer Waveform Bars
        AudioWaveformVisualizer(
            isActive = isListening || isSpeaking,
            rmsDb = rmsDb,
            tintColor = if (isListening) Color(0xFF10B981) else suitTheme.primaryColor,
            accentColor = suitTheme.accentColor,
            animationSpeedMultiplier = animSpeed,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Dedicated High-Fidelity JARVIS Microphone Input Button Console
        com.example.ui.components.JarvisMicInputButton(
            viewModel = viewModel,
            onLaunchSystemVoiceDialog = onLaunchSystemVoiceDialog,
            modifier = Modifier.testTag("voice_control_console")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Ambient Lighting Sensor Telemetry Card
        var showLuxCalibrator by remember { mutableStateOf(false) }
        var manualLuxSlider by remember { mutableFloatStateOf(ambientLightingState.currentLux) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (ambientLightingState.isDynamicThemeEnabled) JarvisCyan.copy(alpha = 0.5f) else JarvisCardBorder,
                    RoundedCornerShape(10.dp)
                )
                .testTag("ambient_lighting_card"),
            colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (ambientLightingState.isDynamicThemeEnabled) Color(0xFF382300) else JarvisSurfaceElevated
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Ambient Light Sensor",
                                tint = if (ambientLightingState.isDynamicThemeEnabled) Color(0xFFFDE047) else JarvisTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AMBIENT OPTICAL SENSOR",
                                    color = JarvisTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${ambientLightingState.currentLux.toInt()} LX",
                                    color = if (ambientLightingState.isDynamicThemeEnabled) Color(0xFFFDE047) else JarvisCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "LUMINESCENCE: ${ambientLightingState.statusDescription.uppercase()}",
                                color = JarvisTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showLuxCalibrator = !showLuxCalibrator },
                            modifier = Modifier.size(28.dp).testTag("toggle_lux_calibration_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Calibrate Lux",
                                tint = JarvisSky,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (ambientLightingState.isDynamicThemeEnabled) JarvisCyan else JarvisSurfaceElevated
                                )
                                .clickable { viewModel.toggleDynamicLightingTheme() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("toggle_dynamic_theme_button")
                        ) {
                            Text(
                                text = if (ambientLightingState.isDynamicThemeEnabled) "ADAPTIVE ON" else "FIXED",
                                color = if (ambientLightingState.isDynamicThemeEnabled) Color.Black else JarvisTextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Expandable Lux Calibration / Testing Slider
                if (showLuxCalibrator) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF04192B))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CALIBRATION / SENSOR SIMULATION",
                                color = JarvisSky,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${manualLuxSlider.toInt()} lx (Multiplier: ${(ambientLightingState.brightnessMultiplier * 100).toInt()}%)",
                                color = JarvisCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = manualLuxSlider,
                            onValueChange = {
                                manualLuxSlider = it
                                viewModel.setSimulationLux(it)
                            },
                            valueRange = 0f..2500f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = JarvisSurfaceElevated
                            ),
                            modifier = Modifier.testTag("lux_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "0 lx (Stealth Night)", color = JarvisTextMuted, fontSize = 8.sp)
                            Text(text = "400 lx (Indoor)", color = JarvisTextMuted, fontSize = 8.sp)
                            Text(text = "2500 lx (Direct Sun)", color = JarvisTextMuted, fontSize = 8.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // System Telemetry Status HUD Indicator
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(JarvisSurfaceElevated)
                .border(1.dp, if (isListening) Color(0xFF10B981) else JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("hud_status_badge")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFF10B981) else if (isSpeaking) JarvisCyan else JarvisSky)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText.uppercase(),
                    color = JarvisTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // Live transcription hypothesis when speaking in real-time
        if (isListening && liveHypothesis.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF042033))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "TRANSCRIPTION: \"$liveHypothesis...\"",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Speech Error Feedback Banner (if error occurred)
        if (speechError != null && !isListening) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF280B0B))
                    .border(1.dp, JarvisRedAlert.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
                    .testTag("speech_error_banner")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Error", tint = JarvisRedAlert, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = speechError ?: "",
                                color = Color(0xFFFECACA),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleVoiceListening() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = JarvisCyan, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onLaunchSystemVoiceDialog,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.SettingsVoice, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OPEN GOOGLE VOICE SEARCH DIALOG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Synthesized Robotic Personality & Android TTS Engine Console
        com.example.ui.components.JarvisVoiceSynthesizerCard(
            viewModel = viewModel,
            modifier = Modifier.testTag("hud_voice_synthesizer_card")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Background Service Run Protocol Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isBackgroundRunning) Color(0xFF042033) else JarvisDarkSurface.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isBackgroundRunning) JarvisCyan.copy(alpha = 0.8f) else JarvisCardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleBackgroundService() }
                .testTag("background_service_toggle_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isBackgroundRunning) Color(0xFF10B981) else Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BACKGROUND PROTOCOL",
                            color = JarvisTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isBackgroundRunning) "RUNNING IN BACKGROUND (ACTIVE)" else "APPS BACKGROUND RUN (TAP TO ENGAGE)",
                            color = if (isBackgroundRunning) JarvisCyan else JarvisTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Toggle Background",
                    tint = if (isBackgroundRunning) JarvisCyan else JarvisTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Vocal Directives Palette (Instant Voice Testing)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "DIRECT VOCAL OVERRIDE / QUICK COMMANDS",
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            val quickCommands = listOf(
                "JARVIS, open Spotify",
                "JARVIS, open YouTube",
                "Status report",
                "Hello JARVIS",
                "স্পটিফাই খোলো",
                "Battery level",
                "Toggle flashlight",
                "Security protocol"
            )
            val quickScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(quickScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cmd in quickCommands) {
                    SuggestionChip(
                        onClick = { viewModel.processCommand(cmd) },
                        label = {
                            Text(
                                text = cmd,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = JarvisTextPrimary
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = JarvisCyan,
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = JarvisDarkSurface
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = JarvisCyan.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prominent Recognized Command Card (Shows deciphered command in Jarvis UI)
        if (recognizedCommand != null) {
            val cmd = recognizedCommand!!
            val timeStr = remember(cmd.timestamp) {
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(cmd.timestamp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .testTag("recognized_command_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF041829)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RECOGNIZED DIRECTIVE",
                                color = JarvisCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(JarvisCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = cmd.languageLabel,
                                    color = JarvisCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = "STATUS: ${cmd.status}",
                            color = when (cmd.status) {
                                "CONFIRMED", "APP_LAUNCHED", "EXECUTED" -> Color(0xFF10B981)
                                "ABORTED" -> JarvisAmber
                                else -> JarvisSky
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"${cmd.text}\"",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "TIMESTAMP: $timeStr • MARK LXXXV RECEPTOR",
                        color = JarvisTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Holographic Speech Response Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(14.dp))
                .testTag("jarvis_response_card"),
            colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "J.A.R.V.I.S. VOCAL RELAY",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (isSpeaking) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• TRANSMITTING",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.replayResponse(lastResponse) },
                            modifier = Modifier.size(28.dp).testTag("replay_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Replay Speech",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("JARVIS Response", lastResponse)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Telemetry copied to clipboard, Sir.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp).testTag("copy_response_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = JarvisTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = lastResponse,
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Tactical Voice Command Chips (English & Bengali)
        Text(
            text = "TACTICAL VOICE DIRECTIVES (ENG & বাংলা)",
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        val promptChips = listOf(
            "হাউজ পার্টি প্রোটোকল",
            "ওয়াইফাই খোলো",
            "কেমন আছো জারভিস?",
            "ক্যামেরা চালু করো",
            "House Party Protocol",
            "Full Diagnostics",
            "সিস্টেমের কি অবস্থা?",
            "ব্লুটুথ সেটিংস",
            "টনি স্টার্ক কে?",
            "Overcharge Core",
            "Open Settings"
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            promptChips.forEach { chipText ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(JarvisSurfaceElevated)
                        .border(1.dp, JarvisCardBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.processCommand(chipText) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("chip_${chipText.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = chipText,
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        }

        if (scanlinesEnabled) {
            HolographicScanlineOverlay(color = suitTheme.primaryColor, animSpeed = animSpeed)
        }
    }
}

@Composable
private fun HolographicScanlineOverlay(
    color: Color,
    animSpeed: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val sweepY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (4000 / animSpeed.coerceAtLeast(0.2f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepY"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .testTag("holographic_scanlines_overlay")
    ) {
        val w = size.width
        val h = size.height

        // Moving horizontal laser sweep beam
        val currentY = sweepY * h
        val beamHeight = 40.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = 0.04f),
                    color.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.18f),
                    color.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                startY = currentY - beamHeight / 2f,
                endY = currentY + beamHeight / 2f
            ),
            topLeft = Offset(0f, (currentY - beamHeight / 2f).coerceAtLeast(0f)),
            size = Size(w, beamHeight)
        )
    }
}
