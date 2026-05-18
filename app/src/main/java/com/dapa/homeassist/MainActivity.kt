package com.dapa.homeassist

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dapa.homeassist.network.ApiClient
import com.dapa.homeassist.screens.AuthScreen
import com.dapa.homeassist.screens.MainLayout
import com.dapa.homeassist.screens.WelcomeScreen
import com.dapa.homeassist.theme.HomeAssistantDapaTheme

import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }
            val context = LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("home_assist", Context.MODE_PRIVATE) }
            
            var currentScreen by remember { 
                val activeUser = sharedPrefs.getString("active_username", "")
                if (!activeUser.isNullOrEmpty()) {
                    mutableStateOf("main")
                } else {
                    mutableStateOf("welcome")
                }
            }
            var loggedInUser by remember { 
                mutableStateOf(sharedPrefs.getString("active_username", "") ?: "") 
            }

            LaunchedEffect(Unit) {
                ApiClient.backendIp = sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp
                ApiClient.backendPort = sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort
                
                val permissionsToRequest = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }

            HomeAssistantDapaTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            "welcome" -> WelcomeScreen(
                                isDarkMode = isDarkMode,
                                onNavigateToAuth = { currentScreen = "auth" }
                            )
                            "auth" -> AuthScreen(
                                isDarkMode = isDarkMode,
                                onAuthSuccess = { username ->
                                    loggedInUser = username
                                    sharedPrefs.edit().putString("active_username", username).apply()
                                    currentScreen = "main"
                                }
                            )
                            "main" -> MainLayout(
                                username = loggedInUser,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { isDarkMode = !isDarkMode },
                                onLogout = {
                                    sharedPrefs.edit().remove("active_username").apply()
                                    loggedInUser = ""
                                    currentScreen = "auth"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
