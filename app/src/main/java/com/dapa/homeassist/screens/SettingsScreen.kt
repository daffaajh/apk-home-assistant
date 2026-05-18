package com.dapa.homeassist.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dapa.homeassist.network.ApiClient
import com.dapa.homeassist.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("home_assist", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var tempIp by remember { mutableStateOf(sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp) }
    var tempPort by remember { mutableStateOf(sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val phaseX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val cardBg = if (isDarkMode) DarkCard else Color(0x33FFFFFF)
    val cardBorder = if (isDarkMode) DarkBorder else LightBorder
    val textColor = if (isDarkMode) TextWhite else TextDark
    val textSecColor = if (isDarkMode) TextGray else TextDarkGray

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            drawRect(
                brush = Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(Color(0xFF0B0F19), Color(0xFF161D30))
                    } else {
                        listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                    }
                )
            )

            val radX = Math.toRadians(phaseX.toDouble())
            val circle1X = width * 0.3f + (width * 0.15f * Math.cos(radX)).toFloat()
            val circle1Y = height * 0.4f + (height * 0.1f * Math.sin(radX)).toFloat()
            val circle2X = width * 0.7f + (width * 0.15f * Math.sin(radX * 1.5)).toFloat()
            val circle2Y = height * 0.6f + (height * 0.1f * Math.cos(radX * 1.5)).toFloat()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonBlue.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(circle1X, circle1Y),
                    radius = width * 0.65f
                ),
                center = Offset(circle1X, circle1Y),
                radius = width * 0.65f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Pengaturan",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Konfigurasi Server", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Masukkan alamat IPv4 lokal dari server backend.", fontSize = 12.sp, color = textSecColor)
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = tempIp,
                        onValueChange = { tempIp = it },
                        label = { Text("IP Server Backend") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempPort,
                        onValueChange = { tempPort = it },
                        label = { Text("Port Server") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            sharedPrefs.edit()
                                .putString("server_ip", tempIp.trim())
                                .putString("server_port", tempPort.trim())
                                .apply()
                            ApiClient.backendIp = tempIp.trim()
                            ApiClient.backendPort = tempPort.trim()
                            showSaveSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showSaveSuccess) "Tersimpan!" else "Simpan Pengaturan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, NeonRed, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = NeonRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar (Logout)", color = NeonRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}
