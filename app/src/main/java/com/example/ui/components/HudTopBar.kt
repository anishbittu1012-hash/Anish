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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkSurface
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSky
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.util.AmbientLightingState
import com.example.util.DeviceTelemetry

@Composable
fun HudTopBar(
    telemetry: DeviceTelemetry,
    reactorPower: Int,
    ambientLightingState: AmbientLightingState,
    persona: String = "JARVIS",
    suitTitle: String = "MK-85 PROTOCOL",
    primaryColor: Color = JarvisCyan,
    onSettingsClick: () -> Unit,
    onAmbientBadgeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(JarvisDarkSurface.copy(alpha = 0.95f))
            .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("hud_top_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stark / Persona Brand Identity
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val formattedPersona = when (persona.uppercase()) {
                        "JARVIS" -> "J.A.R.V.I.S."
                        "FRIDAY" -> "F.R.I.D.A.Y."
                        "EDITH" -> "E.D.I.T.H."
                        "KAREN" -> "K.A.R.E.N."
                        else -> persona.uppercase()
                    }
                    Text(
                        text = formattedPersona,
                        color = primaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (reactorPower > 100) JarvisAmber else primaryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (reactorPower > 100) "$suitTitle: OVERCHARGE" else suitTitle,
                            color = if (reactorPower > 100) Color.Black else primaryColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = "STARK INDUSTRIES NEURAL MATRIX",
                    color = JarvisTextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Real-time Telemetry Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Ambient Light Sensor Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF061A2B))
                        .border(1.dp, if (ambientLightingState.isDynamicThemeEnabled) JarvisCyan.copy(alpha = 0.4f) else JarvisCardBorder, RoundedCornerShape(6.dp))
                        .clickable { onAmbientBadgeClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("ambient_lux_badge")
                ) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = "Ambient Light",
                        tint = if (ambientLightingState.isDynamicThemeEnabled) Color(0xFFFDE047) else JarvisTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${ambientLightingState.currentLux.toInt()} lx",
                        color = JarvisTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Arc Reactor Core Output Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF061A2B))
                        .border(1.dp, if (reactorPower > 100) JarvisGold else JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Power",
                        tint = if (reactorPower > 100) JarvisGold else JarvisCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$reactorPower%",
                        color = if (reactorPower > 100) JarvisGold else JarvisTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Battery Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF061A2B))
                        .border(1.dp, JarvisCardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = if (telemetry.isCharging) JarvisGold else JarvisCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${telemetry.batteryPercent}%",
                        color = JarvisTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Settings icon button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
