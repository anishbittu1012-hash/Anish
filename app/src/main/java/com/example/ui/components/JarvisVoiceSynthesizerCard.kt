package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.util.RoboticVoicePreset
import com.example.util.VoiceLanguage

/**
 * Robotic Personality & TextToSpeech Synthesizer Console.
 * Allows the user to tune JARVIS's synthesized robotic vocal responses:
 * - Robotic Voice Profile Presets (Classic J.A.R.V.I.S., Cybernetic Android, Deep Neural Core, Quantum Vocal Synth)
 * - Fine-grained Pitch (Harmonic Tone) & Speech Rate (Machine Cadence) sliders
 * - Electronic Vocoder Chime toggle
 * - Instant test vocalization
 */
@Composable
fun JarvisVoiceSynthesizerCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val activePreset by viewModel.ttsPreset.collectAsState()
    val speechPitch by viewModel.ttsPitchFlow.collectAsState()
    val speechRate by viewModel.ttsRateFlow.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val roboticChirpEnabled by viewModel.roboticChirpEnabled.collectAsState()
    val isTtsAutoEnabled by viewModel.isTtsAutoEnabled.collectAsState()
    val activeLanguage by viewModel.activeLanguage.collectAsState()
    val isBengali = activeLanguage == VoiceLanguage.BENGALI

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isSpeaking) Color(0xFF10B981) else JarvisCardBorder, RoundedCornerShape(12.dp))
            .testTag("voice_synthesizer_console_card"),
        colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with speaking indicator & expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) Color(0xFF10B981).copy(alpha = 0.25f) else JarvisSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.RecordVoiceOver,
                            contentDescription = "Robotic Synthesizer",
                            tint = if (isSpeaking) Color(0xFF34D399) else JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NATURAL VOICE ASSISTANT",
                                color = JarvisCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            if (isSpeaking) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.3f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "SPEAKING",
                                        color = Color(0xFF34D399),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        Text(
                            text = "PRESET: ${activePreset.displayName.uppercase()} (${String.format("%.2f", speechPitch)}x / ${String.format("%.2f", speechRate)}x)",
                            color = JarvisTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("toggle_synthesizer_expand_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tune Synthesizer",
                            tint = if (isExpanded) JarvisCyan else JarvisTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Presets Selection Horizontal Chips Row
            Text(
                text = "SYNTHESIZED PERSONALITY PROFILES",
                color = JarvisTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RoboticVoicePreset.values().forEach { preset ->
                    val isSelected = activePreset == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setVoicePreset(preset) },
                        label = {
                            Text(
                                text = preset.displayName,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JarvisCyan.copy(alpha = 0.2f),
                            selectedLabelColor = JarvisCyan,
                            containerColor = JarvisSurfaceElevated,
                            labelColor = JarvisTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) JarvisCyan else JarvisCardBorder
                        ),
                        modifier = Modifier.testTag("preset_chip_${preset.id}")
                    )
                }
            }

            // Description of current preset
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = activePreset.description,
                color = JarvisSky,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Expandable Granular Harmonic & Pitch Tuning Sliders
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(JarvisCardBorder)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Harmonic Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "HARMONIC PITCH (ROBOTIC TIMBRE)",
                            color = JarvisTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${String.format("%.2f", speechPitch)}x ${if (speechPitch < 0.8f) "(DEEP BARITONE)" else if (speechPitch > 1.1f) "(HIGH RESONANCE)" else "(STANDARD)"}",
                            color = JarvisCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = { viewModel.updateSpeechPitch(it) },
                        valueRange = 0.50f..1.60f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisCardBorder
                        ),
                        modifier = Modifier.testTag("synthesizer_pitch_slider")
                    )

                    // Speech Cadence / Rate Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MACHINE CADENCE (SPEED RATE)",
                            color = JarvisTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${String.format("%.2f", speechRate)}x ${if (speechRate > 1.2f) "(RAPID COMPUTATION)" else if (speechRate < 0.85f) "(DELIBERATE)" else "(BALANCED)"}",
                            color = JarvisCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { viewModel.updateSpeechRate(it) },
                        valueRange = 0.50f..1.80f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisCardBorder
                        ),
                        modifier = Modifier.testTag("synthesizer_rate_slider")
                    )

                    // Electronic Vocoder Chime Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ELECTRONIC VOCODER CHIME",
                                color = JarvisTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Acoustic synthesizer frequency blip before speech",
                                color = JarvisTextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Switch(
                            checked = roboticChirpEnabled,
                            onCheckedChange = { viewModel.toggleRoboticChirp() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = JarvisCyan,
                                uncheckedTrackColor = JarvisSurfaceElevated
                            ),
                            modifier = Modifier.testTag("robotic_chirp_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Vocal Test & Mute Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Vocal Synthesis Button
                Button(
                    onClick = { viewModel.testRoboticVoiceSynthesis() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("test_vocal_synthesis_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test Voice",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "টেস্ট রোবোটিক ভয়েস" else "TEST VOCAL SYNTHESIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Stop / Mute Button if Speaking
                if (isSpeaking) {
                    Button(
                        onClick = { viewModel.stopSpeaking() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = JarvisCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("synthesizer_mute_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Mute",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MUTE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
