package com.dapa.homeassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dapa.homeassist.screens.AuthScreen
import com.dapa.homeassist.screens.DashboardScreen
import com.dapa.homeassist.screens.WelcomeScreen
import com.dapa.homeassist.theme.HomeAssistantDapaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var systemDarkTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemDarkTheme) }
            var loggedInUser by remember { mutableStateOf("") }
            
            val navController = rememberNavController()

            HomeAssistantDapaTheme(darkTheme = isDarkMode) {
                NavHost(navController = navController, startDestination = "welcome") {
                    composable("welcome") {
                        WelcomeScreen(
                            isDarkMode = isDarkMode,
                            onNavigateToAuth = {
                                navController.navigate("auth")
                            }
                        )
                    }
                    composable("auth") {
                        AuthScreen(
                            isDarkMode = isDarkMode,
                            onAuthSuccess = { username ->
                                loggedInUser = username
                                navController.navigate("dashboard") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard") {
                        DashboardScreen(
                            username = loggedInUser,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = {
                                isDarkMode = !isDarkMode
                            },
                            onLogout = {
                                loggedInUser = ""
                                navController.navigate("auth") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
