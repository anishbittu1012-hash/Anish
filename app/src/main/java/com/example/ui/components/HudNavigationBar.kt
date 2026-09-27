package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisScreen
import com.example.ui.theme.JarvisTextMuted

data class NavItem(
    val screen: JarvisScreen,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun HudNavigationBar(
    currentScreen: JarvisScreen,
    onScreenSelected: (JarvisScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(JarvisScreen.HUD, "CORE", Icons.Default.Hub, "nav_core"),
        NavItem(JarvisScreen.CHAT, "TERMINAL", Icons.AutoMirrored.Filled.Chat, "nav_terminal"),
        NavItem(JarvisScreen.TELEMETRY, "DIAGNOSTICS", Icons.Default.Speed, "nav_telemetry"),
        NavItem(JarvisScreen.SHORTCUTS, "APPS & SYS", Icons.Default.Apps, "nav_shortcuts"),
        NavItem(JarvisScreen.PROTOCOLS, "PROTOCOLS", Icons.Default.Security, "nav_protocols")
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(surfaceColor.copy(alpha = 0.98f))
            .border(1.dp, outlineColor, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag("hud_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.screen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onScreenSelected(item.screen) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag(item.tag)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) primaryColor else JarvisTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = item.label,
                        color = if (isSelected) primaryColor else JarvisTextMuted,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
