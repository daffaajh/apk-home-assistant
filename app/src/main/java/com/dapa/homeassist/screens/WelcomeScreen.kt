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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dapa.homeassist.network.ApiClient
import com.dapa.homeassist.theme.*
import com.dapa.homeassist.components.LiquidGlassButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    isDarkMode: Boolean,
    onNavigateToAuth: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("home_assist", Context.MODE_PRIVATE) }
    
    var ipDialogVisible by remember { mutableStateOf(false) }
    var tempIp by remember { mutableStateOf(sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp) }
    var tempPort by remember { mutableStateOf(sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort) }

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
                    colors = listOf(NeonBlue.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(circle1X, circle1Y),
                    radius = width * 0.6f
                ),
                center = Offset(circle1X, circle1Y),
                radius = width * 0.6f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LightBlue.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(circle2X, circle2Y),
                    radius = width * 0.7f
                ),
                center = Offset(circle2X, circle2Y),
                radius = width * 0.7f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            IconButton(
                onClick = { ipDialogVisible = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 24.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(cardBg)
                    .border(1.dp, cardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = NeonBlue,
                        modifier = Modifier.size(45.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(32.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selamat Datang di Home Assistant Dapa",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Kendalikan AC LG pintar kamu, pantau suhu ruangan, dan dapatkan saran efisiensi energi berbasis kecerdasan buatan dalam satu genggaman.",
                            fontSize = 13.sp,
                            color = textSecColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        LiquidGlassButton(
                            onClick = onNavigateToAuth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Mulai Sekarang",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (ipDialogVisible) {
        AlertDialog(
            onDismissRequest = { ipDialogVisible = false },
            title = { Text("Konfigurasi Server IP", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tempIp,
                        onValueChange = { tempIp = it },
                        label = { Text("IP Server Backend") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempPort,
                        onValueChange = { tempPort = it },
                        label = { Text("Port Server") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sharedPrefs.edit()
                            .putString("server_ip", tempIp.trim())
                            .putString("server_port", tempPort.trim())
                            .apply()
                        ApiClient.backendIp = tempIp.trim()
                        ApiClient.backendPort = tempPort.trim()
                        ipDialogVisible = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { ipDialogVisible = false }) {
                    Text("Batal", color = textSecColor)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
