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

import com.dapa.homeassist.components.LiquidBottomTabs
import com.dapa.homeassist.components.LiquidBottomTab
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    username: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val navBg = if (isDarkMode) Color(0xFF0B0F19) else Color(0xFFE2E8F0)
    val selectedIconColor = NeonBlue
    val unselectedIconColor = if (isDarkMode) TextGray else TextDarkGray

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(navBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                LiquidBottomTabs(
                    selectedTabIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    tabsCount = 3,
                    activeColor = NeonBlue
                ) {
                    LiquidBottomTab(onClick = { selectedTab = 0 }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = if (selectedTab == 0) selectedIconColor else unselectedIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Beranda",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (selectedTab == 0) selectedIconColor else unselectedIconColor
                        )
                    }
                    LiquidBottomTab(onClick = { selectedTab = 1 }) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "AI",
                            tint = if (selectedTab == 1) selectedIconColor else unselectedIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Tanya AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (selectedTab == 1) selectedIconColor else unselectedIconColor
                        )
                    }
                    LiquidBottomTab(onClick = { selectedTab = 2 }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (selectedTab == 2) selectedIconColor else unselectedIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Pengaturan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (selectedTab == 2) selectedIconColor else unselectedIconColor
                        )
                    }
                }
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
