package com.dapa.homeassist.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dapa.homeassist.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    username: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val navBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val selectedIconColor = NeonBlue
    val unselectedIconColor = if (isDarkMode) TextGray else TextDarkGray

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = navBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Beranda", fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        selectedTextColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        unselectedTextColor = unselectedIconColor,
                        indicatorColor = selectedIconColor.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "AI") },
                    label = { Text("Tanya AI", fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        selectedTextColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        unselectedTextColor = unselectedIconColor,
                        indicatorColor = selectedIconColor.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Pengaturan", fontWeight = FontWeight.Bold) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        selectedTextColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        unselectedTextColor = unselectedIconColor,
                        indicatorColor = selectedIconColor.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        username = username,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = onToggleDarkMode,
                        onLogout = onLogout
                    )
                    1 -> AiChatScreen(
                        isDarkMode = isDarkMode
                    )
                    2 -> SettingsScreen(
                        isDarkMode = isDarkMode,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
