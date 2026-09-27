package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.util.ProtocolManager
import com.example.util.VoiceLanguage

@Composable
fun ProtocolsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val activeBanner by viewModel.activeProtocolBanner.collectAsState()
    val reactorPower by viewModel.reactorPower.collectAsState()
    val activeLanguage by viewModel.activeLanguage.collectAsState()

    val isBengali = activeLanguage == VoiceLanguage.BENGALI

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Section Header
        Text(
            text = "STARK TACTICAL PROTOCOLS",
            color = JarvisCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "AUTHORIZATION: TONY STARK (PRIMARY DIRECTIVE)",
            color = JarvisTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ProtocolManager.protocols, key = { it.id }) { protocol ->
                val isActive = activeBanner?.contains(protocol.name, ignoreCase = true) == true ||
                        activeBanner?.contains(protocol.nameBn, ignoreCase = true) == true
                val isOverchargeActive = protocol.id == "overcharge" && reactorPower > 100

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isActive || isOverchargeActive) JarvisAmber else JarvisCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("protocol_item_${protocol.id}"),
                    colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (protocol.id) {
                                                "house_party" -> JarvisAmber.copy(alpha = 0.2f)
                                                "overcharge" -> JarvisGold.copy(alpha = 0.2f)
                                                "sentry_mode" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                else -> JarvisCyan.copy(alpha = 0.2f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (protocol.id) {
                                            "house_party" -> Icons.Default.GroupWork
                                            "sentry_mode" -> Icons.Default.Security
                                            "overcharge" -> Icons.Default.Bolt
                                            "stealth" -> Icons.Default.VisibilityOff
                                            "clean_slate" -> Icons.Default.CleaningServices
                                            else -> Icons.Default.Speed
                                        },
                                        contentDescription = protocol.name,
                                        tint = when (protocol.id) {
                                            "house_party" -> JarvisAmber
                                            "overcharge" -> JarvisGold
                                            "sentry_mode" -> Color(0xFF10B981)
                                            else -> JarvisCyan
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isBengali) protocol.nameBn else protocol.name,
                                            color = JarvisTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (protocol.isSensitive) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Sensitive",
                                                tint = JarvisAmber,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "CODE: ${protocol.codeName}",
                                        color = JarvisSky,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.requestProtocolExecution(protocol.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActive || isOverchargeActive) JarvisAmber else JarvisCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("execute_${protocol.id}")
                            ) {
                                Text(
                                    text = if (isActive || isOverchargeActive) "ACTIVE" else if (protocol.isSensitive) "AUTHORIZE" else "ENGAGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isBengali) protocol.descriptionBn else protocol.description,
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        if (protocol.isSensitive) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(JarvisAmber.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isBengali) "⚠ সংবেদনশীল ক্রিয়াকলাপ: ভয়েস বা ম্যানুয়াল নিশ্চিতকরণ প্রয়োজন"
                                    else "⚠ SENSITIVE OPERATION: Explicit voice or tactile confirmation required",
                                    color = JarvisAmber,
                                    fontSize = 9.sp,
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
