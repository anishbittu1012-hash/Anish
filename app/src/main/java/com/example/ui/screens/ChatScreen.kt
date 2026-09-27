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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.local.JarvisLogEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    viewModel: JarvisViewModel,
    onRequestRecordAudioPermission: () -> Unit,
    hasRecordAudioPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logs by viewModel.conversationLogs.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val isListening by viewModel.speechRecognizerHelper.isListening.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message on new entries
    LaunchedEffect(logs.size, isThinking) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val quickLanguagePrompts = listOf(
        "বাংলায় তোমার ক্ষমতা সম্পর্কে বলো",
        "हिंदी में क्वांटम फिजिक्स समझाओ",
        "Explain Stark Mark 85 armor specs",
        "What can you do for me, Jarvis?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("modern_chat_screen")
    ) {
        // 1. Terminal Header with Status & Clear Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JARVIS NEURAL DIALOGUE",
                        color = primaryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "BILINGUAL COGNITIVE CORE • MULTI-TURN CONTEXT MEMORY",
                    color = JarvisTextSecondary,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (logs.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("clear_chat_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Chat",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Multilingual Suggestion Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quickLanguagePrompts.forEach { chipPrompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisDarkSurface)
                        .border(1.dp, primaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.processCommand(chipPrompt) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = chipPrompt,
                        color = JarvisTextPrimary,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Message Bubbles Container
        Box(modifier = Modifier.weight(1f)) {
            if (logs.isEmpty() && !isThinking) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.12f))
                                .border(1.dp, primaryColor.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "NO CONVERSATION LOGS",
                            color = primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Speak or enter a query in Bengali, Hindi, or English, Sir.",
                            color = JarvisTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    reverseLayout = true // modern messaging flow: newest at bottom
                ) {
                    // Thinking status indicator bubble
                    if (isThinking) {
                        item {
                            ThinkingBubble(primaryColor = primaryColor)
                        }
                    }

                    // Conversation Turn Bubbles
                    items(logs, key = { it.id }) { log ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // User Message Bubble (Right-aligned)
                            UserMessageBubble(
                                text = log.query,
                                timestamp = log.timestamp,
                                primaryColor = primaryColor,
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Query", log.query))
                                    Toast.makeText(context, "Copied query", Toast.LENGTH_SHORT).show()
                                }
                            )

                            // Assistant Message Bubble (Left-aligned)
                            AssistantMessageBubble(
                                log = log,
                                primaryColor = primaryColor,
                                onSpeak = { viewModel.replayResponse(log.response) },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Reply", log.response))
                                    Toast.makeText(context, "Copied reply", Toast.LENGTH_SHORT).show()
                                },
                                onRegenerate = {
                                    viewModel.regenerateResponse(log)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Message Input Console (Voice + Text + Send)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(JarvisDarkSurface)
                .border(1.dp, primaryColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = if (isListening) "Listening (শুনছি...)..." else "Ask JARVIS in English, বাংলা, हिंदी...",
                        color = JarvisTextMuted,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    cursorColor = primaryColor
                ),
                singleLine = true
            )

            // Dedicated Compact Mic Button with Audio Permission Handling
            com.example.ui.components.JarvisCompactMicButton(
                viewModel = viewModel,
                size = 38
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        val query = textInput
                        textInput = ""
                        viewModel.processCommand(query)
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
                    .testTag("chat_send_button")
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

@Composable
private fun UserMessageBubble(
    text: String,
    timestamp: Long,
    primaryColor: Color,
    onCopy: () -> Unit
) {
    val dateStr = remember(timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.22f),
                            JarvisSurfaceElevated
                        )
                    )
                )
                .border(1.dp, primaryColor.copy(alpha = 0.45f), RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "YOU",
                            color = primaryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = dateStr,
                        color = JarvisTextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = text,
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AssistantMessageBubble(
    log: JarvisLogEntity,
    primaryColor: Color,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit
) {
    val dateStr = remember(log.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JarvisDarkSurface,
                            JarvisSurfaceElevated.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(1.dp, JarvisCardBorder, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(10.dp)
                .testTag("assistant_message_bubble_${log.id}")
        ) {
            Column {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "J",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "J.A.R.V.I.S.",
                            color = primaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "• $dateStr",
                            color = JarvisTextMuted,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Action buttons: Speak, Copy, Regenerate
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        // TTS Audio Playback
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("bubble_speak_${log.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen",
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Copy Text
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("bubble_copy_${log.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = JarvisSky,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Regenerate with context memory
                        IconButton(
                            onClick = onRegenerate,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("bubble_regenerate_${log.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate Response",
                                tint = JarvisAmber,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Assistant Response Body Text
                Text(
                    text = log.response,
                    color = JarvisTextPrimary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ThinkingBubble(primaryColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(JarvisDarkSurface)
                .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = primaryColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SYNCHRONIZING WITH GEMINI NEURAL CORE...",
                    color = primaryColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
