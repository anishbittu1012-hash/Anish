package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun TelemetryScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val reactorPower by viewModel.reactorPower.collectAsState()
    val diagnosticsState by viewModel.diagnosticsState.collectAsState()
    val ambientLightingState by viewModel.ambientLightingState.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYSTEM TELEMETRY & DIAGNOSTICS",
                    color = JarvisCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "MARK LXXXV HARDWARE SENSOR ARRAY",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Diagnostic Scan Button & Progress Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (diagnosticsState.isRunning) JarvisAmber else JarvisCardBorder, RoundedCornerShape(12.dp))
                .testTag("diagnostics_card"),
            colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DIAGNOSTICS PROTOCOL",
                            color = if (diagnosticsState.isRunning) JarvisAmber else JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (diagnosticsState.isRunning) diagnosticsState.currentStep else "Status: Ready for integral scan",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { viewModel.runFullDiagnostics() },
                        enabled = !diagnosticsState.isRunning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("run_diagnostics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Scan",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (diagnosticsState.isRunning) "SCANNING" else "RUN SCAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (diagnosticsState.isRunning) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { diagnosticsState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = JarvisCyan,
                        trackColor = JarvisSurfaceElevated
                    )
                }

                if (diagnosticsState.completedReport != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF04192B))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = diagnosticsState.completedReport ?: "",
                            color = JarvisTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of Telemetry Metrics
        // 1. Arc Reactor Core
        TelemetryMetricCard(
            title = "ARC REACTOR OUTPUT",
            value = "$reactorPower%",
            subtitle = if (reactorPower > 100) "Vibrium-Palladium Overcharge" else "Nominal 3.0 GigaWatts",
            icon = Icons.Default.Bolt,
            accentColor = if (reactorPower > 100) JarvisGold else JarvisCyan,
            progress = (reactorPower / 300f).coerceIn(0f, 1f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Battery & Power Storage
        TelemetryMetricCard(
            title = "POWER MATRIX (BATTERY)",
            value = "${telemetry.batteryPercent}%",
            subtitle = "${if (telemetry.isCharging) "Charging (AC Surge)" else "Operating on Discharge"} • ${telemetry.batteryHealth}",
            icon = Icons.Default.BatteryChargingFull,
            accentColor = if (telemetry.batteryPercent < 20) JarvisAmber else JarvisCyan,
            progress = telemetry.batteryPercent / 100f
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Thermal Telemetry
        TelemetryMetricCard(
            title = "THERMAL SIGNATURE",
            value = "${telemetry.batteryTemperatureC} °C",
            subtitle = if (telemetry.batteryTemperatureC > 45f) "High Thermal Warning" else "Cryo-Cooling Nominal",
            icon = Icons.Default.Thermostat,
            accentColor = if (telemetry.batteryTemperatureC > 42f) JarvisAmber else JarvisSky,
            progress = (telemetry.batteryTemperatureC / 60f).coerceIn(0f, 1f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Memory (RAM) Telemetry
        TelemetryMetricCard(
            title = "NEURAL MEMORY MATRIX (RAM)",
            value = "${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB (${telemetry.ramPercent}%)",
            subtitle = "Active neural nodes and buffer cache",
            icon = Icons.Default.Memory,
            accentColor = JarvisCyan,
            progress = telemetry.ramPercent / 100f
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Internal Storage Telemetry
        TelemetryMetricCard(
            title = "CRYPTO DATA STORAGE",
            value = String.format("%.1f GB Free / %.1f GB Total", telemetry.storageFreeGb, telemetry.storageTotalGb),
            subtitle = "${telemetry.storagePercentUsed}% Allocated for Stark Schematics",
            icon = Icons.Default.SdCard,
            accentColor = JarvisSky,
            progress = telemetry.storagePercentUsed / 100f
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Network Link Telemetry
        TelemetryMetricCard(
            title = "SATELLITE & NETWORK UPLINK",
            value = telemetry.networkType,
            subtitle = "Status: ${telemetry.networkStatus} • Encrypted Stark Network",
            icon = Icons.Default.Wifi,
            accentColor = Color(0xFF10B981),
            progress = 1.0f
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 7. Ambient Optical Sensor & Dynamic Theme
        TelemetryMetricCard(
            title = "AMBIENT OPTICAL SENSOR (LUX)",
            value = "${ambientLightingState.currentLux.toInt()} LX",
            subtitle = if (ambientLightingState.isDynamicThemeEnabled)
                "Dynamic Arc Reactor Luminescence: ${ambientLightingState.statusDescription}"
            else
                "Adaptive Theme Disengaged (Fixed Luminescence)",
            icon = Icons.Default.LightMode,
            accentColor = if (ambientLightingState.isDynamicThemeEnabled) Color(0xFFFDE047) else JarvisCyan,
            progress = (ambientLightingState.currentLux / 1500f).coerceIn(0.05f, 1.0f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 8. Robotic Voice Synthesizer & Speech Engine Telemetry
        com.example.ui.components.JarvisVoiceSynthesizerCard(
            viewModel = viewModel,
            modifier = Modifier.testTag("telemetry_voice_synthesizer_card")
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            color = JarvisTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = value,
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = JarvisSurfaceElevated
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
