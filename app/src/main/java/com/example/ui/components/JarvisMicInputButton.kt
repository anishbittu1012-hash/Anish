package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.util.SoundFxGenerator
import com.example.util.VoiceLanguage

/**
 * High-fidelity, permission-aware Microphone Input Button for JARVIS.
 * Handles:
 * - Manifest.permission.RECORD_AUDIO runtime permission check & request flow
 * - Engaging SpeechRecognizer voice-to-text processing
 * - Live audio level / pulse animation while recording
 * - Informative permission explanation dialog if permission is denied
 * - Fallback to native Google Voice Search intent
 */
@Composable
fun JarvisMicInputButton(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier,
    onLaunchSystemVoiceDialog: () -> Unit = {}
) {
    val context = LocalContext.current
    val isListening by viewModel.speechRecognizerHelper.isListening.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val rmsDb by viewModel.speechRecognizerHelper.rmsDb.collectAsState()
    val liveHypothesis by viewModel.speechRecognizerHelper.liveHypothesis.collectAsState()
    val activeLanguage by viewModel.activeLanguage.collectAsState()
    val isBengali = activeLanguage == VoiceLanguage.BENGALI

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            viewModel.startListening()
        } else {
            val msg = if (isBengali) "মাইক্রোফোন ব্যবহারের অনুমতি প্রয়োজন।" else "Microphone permission required."
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            showPermissionRationale = true
        }
    }

    fun handleMicClick() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            SoundFxGenerator.playAcknowledgeBeep()
            viewModel.toggleVoiceListening()
        }
    }

    // Pulse animation while listening
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isListening) 0.15f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    val primaryThemeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val buttonBgColor by animateColorAsState(
        targetValue = when {
            isListening -> Color(0xFF10B981) // Emerald when listening
            isThinking -> JarvisAmber // Amber when processing
            !hasAudioPermission -> Color(0xFF7F1D1D) // Dark red if permission denied
            else -> primaryThemeColor
        },
        label = "button_color"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Live Partial Voice Hypothesis (Preview of voice-to-text as user speaks)
        if (isListening && liveHypothesis.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF042033)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.7f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("live_hypothesis_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "\"$liveHypothesis\"",
                        color = JarvisTextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Main Microphone Control Console
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main Microphone FAB Button with Glowing Pulse Rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                // Outer Pulse Ring when listening
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = ringAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF34D399).copy(alpha = 0.8f), CircleShape)
                    )
                }

                // Core Microphone Button (meets & exceeds 48dp touch target: 60dp)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    buttonBgColor,
                                    buttonBgColor.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .border(
                            2.dp,
                            if (isListening) Color(0xFF34D399) else JarvisSky,
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = { handleMicClick() }
                        )
                        .testTag("jarvis_main_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            !hasAudioPermission -> Icons.Default.MicOff
                            isListening -> Icons.Default.GraphicEq
                            isThinking -> Icons.Default.GraphicEq
                            else -> Icons.Default.Mic
                        },
                        contentDescription = "Trigger JARVIS Voice Command",
                        tint = if (!hasAudioPermission) Color.White else Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action & Status Pill
            Column {
                // Tap to Speak Button Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisDarkSurface)
                        .border(1.dp, if (isListening) Color(0xFF10B981) else JarvisCardBorder, RoundedCornerShape(12.dp))
                        .clickable { handleMicClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("voice_pill_trigger")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) Color(0xFF10B981)
                                    else if (!hasAudioPermission) JarvisAmber
                                    else JarvisCyan
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when {
                                    !hasAudioPermission -> if (isBengali) "মাইক্রোফোন অ্যাক্সেস প্রয়োজন" else "MIC PERMISSION REQUIRED"
                                    isListening -> if (isBengali) "শুনছি... (ট্যাপ করে থামান)" else "LISTENING... (TAP TO STOP)"
                                    isThinking -> if (isBengali) "কমান্ড বিশ্লেষণ হচ্ছে..." else "PROCESSING COMMAND..."
                                    else -> if (isBengali) "ভয়েস কমান্ড: কথা বলুন" else "TAP TO SPEAK COMMAND"
                                },
                                color = if (isListening) Color(0xFF34D399) else JarvisTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = when {
                                    !hasAudioPermission -> "Tap to grant audio permission"
                                    isListening -> "Say 'JARVIS, open Spotify' or speak..."
                                    else -> "Triggers speech-to-text processing"
                                },
                                color = JarvisTextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Secondary actions row: Native System Voice Search & TTS Mute
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // System Voice Search Dialog Button
                    OutlinedButton(
                        onClick = {
                            if (hasAudioPermission) {
                                onLaunchSystemVoiceDialog()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("system_voice_dialog_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsVoice,
                            contentDescription = "System Voice Dialog",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VOICE SEARCH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // TTS Mute button if JARVIS is talking
                    if (isSpeaking) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { viewModel.stopSpeaking() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF334155),
                                contentColor = JarvisCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("mute_speech_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Mute",
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "MUTE", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }

    // Permission Rationale / Settings Dialog
    if (showPermissionRationale) {
        JarvisMicPermissionDialog(
            isBengali = isBengali,
            onRequestAgain = {
                showPermissionRationale = false
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onOpenSettings = {
                showPermissionRationale = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            onDismiss = { showPermissionRationale = false }
        )
    }
}

/**
 * Compact Microphone Input Button for input fields, headers, and toolbars.
 */
@Composable
fun JarvisCompactMicButton(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val context = LocalContext.current
    val isListening by viewModel.speechRecognizerHelper.isListening.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.toggleVoiceListening()
        }
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(if (isListening) Color(0xFF10B981) else JarvisCyan)
            .clickable {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    SoundFxGenerator.playAcknowledgeBeep()
                    viewModel.toggleVoiceListening()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            .testTag("compact_mic_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
            contentDescription = "Voice-to-Text Input",
            tint = Color.Black,
            modifier = Modifier.size((size * 0.5f).dp)
        )
    }
}

@Composable
fun JarvisMicPermissionDialog(
    isBengali: Boolean,
    onRequestAgain: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MicOff,
                    contentDescription = null,
                    tint = JarvisAmber,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "মাইক্রোফোন অনুমতি প্রয়োজন" else "MICROPHONE PERMISSION NEEDED",
                    color = JarvisAmber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Text(
                text = if (isBengali)
                    "জারভিসের ভয়েস-টু-টেক্সট প্রসেসিং এবং কমান্ড (যেমন 'JARVIS, open Spotify') শোনার জন্য মাইক্রোফোন অ্যাক্সেসের অনুমতি প্রয়োজন।"
                else
                    "J.A.R.V.I.S. requires microphone audio access to trigger voice-to-text processing for voice commands like 'JARVIS, open Spotify' and manage protocols.",
                color = JarvisTextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        },
        confirmButton = {
            Button(
                onClick = onRequestAgain,
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
            ) {
                Text("GRANT PERMISSION", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenSettings) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SETTINGS", color = JarvisSky, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = JarvisTextMuted, fontFamily = FontFamily.Monospace)
                }
            }
        },
        containerColor = JarvisDarkSurface,
        shape = RoundedCornerShape(14.dp)
    )
}
