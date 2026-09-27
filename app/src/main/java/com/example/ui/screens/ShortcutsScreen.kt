package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.example.util.AppCategory
import com.example.util.AppLauncherManager
import com.example.util.InstalledApp
import com.example.util.SystemController
import com.example.util.VoiceLanguage

data class ShortcutItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShortcutsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val installedApps by viewModel.installedApps.collectAsState()
    val searchQuery by viewModel.appSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedAppCategory.collectAsState()
    val activeLanguage by viewModel.activeLanguage.collectAsState()
    val isBengali = activeLanguage == VoiceLanguage.BENGALI

    // Filter apps by Search Query & Category
    val filteredApps = remember(installedApps, searchQuery, selectedCategory) {
        installedApps.filter { app ->
            val matchesCategory = when (selectedCategory) {
                AppCategory.ALL -> true
                AppCategory.USER -> !app.isSystem
                AppCategory.SYSTEM -> app.isSystem
                else -> app.category == selectedCategory
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                app.name.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    val systemShortcuts = listOf(
        ShortcutItem("Wi-Fi Settings", "Wireless networks", Icons.Default.Wifi) {
            viewModel.processCommand("Open Wi-Fi")
        },
        ShortcutItem("Bluetooth", "Wireless devices", Icons.Default.Bluetooth) {
            viewModel.processCommand("Open Bluetooth")
        },
        ShortcutItem("Accessibility", "Tactile & sensory", Icons.Default.Accessibility) {
            viewModel.processCommand("Open Accessibility")
        },
        ShortcutItem("Display", "Brightness & HUD", Icons.Default.BrightnessHigh) {
            viewModel.processCommand("Display Settings")
        },
        ShortcutItem("Sound & Audio", "Volume telemetry", Icons.AutoMirrored.Filled.VolumeUp) {
            viewModel.processCommand("Sound Settings")
        },
        ShortcutItem("Battery", "Power & saver mode", Icons.Default.BatterySaver) {
            viewModel.processCommand("Battery Settings")
        },
        ShortcutItem("Camera", "Optical targeting", Icons.Default.CameraAlt) {
            viewModel.processCommand("Open Camera")
        },
        ShortcutItem("Clock & Alarms", "Temporal alerts", Icons.Default.Alarm) {
            viewModel.processCommand("Open Alarm")
        },
        ShortcutItem("Settings", "Core parameters", Icons.Default.Settings) {
            viewModel.processCommand("Open Settings")
        },
        ShortcutItem("Manage Apps", "Application manager", Icons.Default.Apps) {
            SystemController.openAppsSettings(context)
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Header with Refresh Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STARK APP LAUNCHER & SYS MATRIX",
                    color = JarvisCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isBengali) "ভয়েস কমান্ডের মাধ্যমে সরাসরি অ্যাপ চালু করুন" else "VOICE-ENABLED APPLICATION DIRECTIVES",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            IconButton(
                onClick = { viewModel.loadInstalledApps() },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("refresh_apps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Applications",
                    tint = JarvisCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Voice Command Directives Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF071B28)),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("voice_launcher_tip_card")
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Voice Directive",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "ভয়েস কমান্ড টিপস (VOICE LAUNCH EXAMPLES)" else "VOICE COMMAND DIRECTIVES",
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBengali)
                        "মাইকে বলুন: 'JARVIS, open Spotify' অথবা 'স্পটিফাই খোলো' অথবা 'ইউটিউব চালু করো'"
                    else
                        "Speak clearly: 'JARVIS, open Spotify' • 'Open YouTube' • 'Launch Camera'",
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Interactive Voice Test Chips
                val voiceTestSamples = listOf(
                    "JARVIS, open Spotify",
                    "JARVIS, open YouTube",
                    "Open Camera",
                    "Open Settings",
                    "স্পটিফাই খোলো"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (sample in voiceTestSamples) {
                        SuggestionChip(
                            onClick = { viewModel.processCommand(sample) },
                            label = {
                                Text(
                                    text = sample,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = JarvisTextPrimary
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(10.dp)
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = JarvisDarkSurface
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = JarvisCyan.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AppCategory.values().forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setAppCategory(category) },
                    label = {
                        Text(
                            text = if (isBengali) category.displayNameBn else category.displayName,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JarvisCyan.copy(alpha = 0.2f),
                        selectedLabelColor = JarvisCyan,
                        containerColor = JarvisDarkSurface,
                        labelColor = JarvisTextMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) JarvisCyan else JarvisCardBorder
                    ),
                    modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App Search Bar with Clear Icon
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setAppSearchQuery(it) },
            placeholder = {
                Text(
                    text = if (isBengali) "অ্যাপের নাম দিয়ে খুঁজুন..." else "Search installed apps or speak 'JARVIS, open [app]'...",
                    color = JarvisTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = JarvisCyan,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setAppSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = JarvisTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    com.example.ui.components.JarvisCompactMicButton(
                        viewModel = viewModel,
                        size = 32
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = JarvisCardBorder,
                focusedTextColor = JarvisTextPrimary,
                unfocusedTextColor = JarvisTextPrimary,
                cursorColor = JarvisCyan
            ),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Applications List with Real Icons & Voice Test Actions
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APPLICATIONS (${filteredApps.size} OF ${installedApps.size})",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (filteredApps.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = JarvisTextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    "No application found matching \"$searchQuery\""
                                else
                                    "No applications loaded yet",
                                color = JarvisTextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val playStoreIntent = AppLauncherManager.createPlayStoreIntent(searchQuery)
                                        try {
                                            context.startActivity(playStoreIntent)
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Shop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SEARCH PLAY STORE FOR \"$searchQuery\"",
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

            items(filteredApps, key = { it.packageName }) { app ->
                AppLauncherCard(
                    app = app,
                    onLaunch = { viewModel.launchAppDirectly(app) },
                    onVoiceSimulate = { viewModel.processCommand("JARVIS, open ${app.name}") }
                )
            }

            // System Hardware Shortcuts section
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "HARDWARE & SYSTEM SHORTCUTS",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    systemShortcuts.forEach { shortcut ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisDarkSurface)
                                .border(1.dp, JarvisCardBorder, RoundedCornerShape(8.dp))
                                .clickable { shortcut.action() }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("shortcut_${shortcut.title.lowercase().replace(" ", "_")}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = shortcut.icon,
                                    contentDescription = shortcut.title,
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = shortcut.title,
                                    color = JarvisTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AppLauncherCard(
    app: InstalledApp,
    onLaunch: () -> Unit,
    onVoiceSimulate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, JarvisCardBorder, RoundedCornerShape(10.dp))
            .clickable { onLaunch() }
            .testTag("app_item_${app.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = JarvisDarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Real App Icon or Fallback
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.iconBitmap != null) {
                        Image(
                            bitmap = app.iconBitmap,
                            contentDescription = app.name,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = when (app.category) {
                                AppCategory.MEDIA -> Icons.AutoMirrored.Filled.VolumeUp
                                AppCategory.COMMUNICATION -> Icons.Default.Wifi
                                AppCategory.TOOLS -> Icons.Default.Settings
                                else -> Icons.Default.Apps
                            },
                            contentDescription = app.name,
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (app.category) {
                                        AppCategory.MEDIA -> Color(0xFF10B981).copy(alpha = 0.2f)
                                        AppCategory.COMMUNICATION -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                        AppCategory.USER -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                        else -> Color(0xFF64748B).copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = app.category.name,
                                color = when (app.category) {
                                    AppCategory.MEDIA -> Color(0xFF34D399)
                                    AppCategory.COMMUNICATION -> Color(0xFF60A5FA)
                                    AppCategory.USER -> Color(0xFFA78BFA)
                                    else -> Color(0xFF94A3B8)
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = app.packageName,
                        color = JarvisTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Voice Simulate / Test Button
                IconButton(
                    onClick = onVoiceSimulate,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("voice_test_${app.name.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Test Voice Launch",
                        tint = JarvisCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Launch Icon
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Launch,
                    contentDescription = "Launch",
                    tint = JarvisSky,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
