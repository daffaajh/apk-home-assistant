package com.dapa.homeassist.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dapa.homeassist.network.ApiClient
import com.dapa.homeassist.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    isDarkMode: Boolean,
    onAuthSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("home_assist", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()
    
    var isLoginMode by remember { mutableStateOf(true) }
    
    var identifier by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf("") }
    
    var ipDialogVisible by remember { mutableStateOf(false) }
    var tempIp by remember { mutableStateOf(sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp) }
    var tempPort by remember { mutableStateOf(sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort) }

    val infiniteTransition = rememberInfiniteTransition()
    val phaseX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
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
            val circle1X = width * 0.7f + (width * 0.12f * Math.cos(radX)).toFloat()
            val circle1Y = height * 0.3f + (height * 0.08f * Math.sin(radX)).toFloat()
            val circle2X = width * 0.3f + (width * 0.12f * Math.sin(radX * 1.3)).toFloat()
            val circle2Y = height * 0.7f + (height * 0.08f * Math.cos(radX * 1.3)).toFloat()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonBlue.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(circle1X, circle1Y),
                    radius = width * 0.65f
                ),
                center = Offset(circle1X, circle1Y),
                radius = width * 0.65f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LightBlue.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(circle2X, circle2Y),
                    radius = width * 0.65f
                ),
                center = Offset(circle2X, circle2Y),
                radius = width * 0.65f
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
                Text(
                    text = if (isLoginMode) "Masuk Akun" else "Daftar Baru",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isLoginMode) "Gunakan username/email kamu untuk masuk" else "Buat akun Home Assistant Dapa baru",
                    fontSize = 13.sp,
                    color = textSecColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoginMode) {
                            OutlinedTextField(
                                value = identifier,
                                onValueChange = { identifier = it },
                                label = { Text("Username atau Email") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonBlue) },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedLabelColor = NeonBlue,
                                    unfocusedLabelColor = textSecColor,
                                    focusedBorderColor = NeonBlue,
                                    unfocusedBorderColor = cardBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonBlue) },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedLabelColor = NeonBlue,
                                    unfocusedLabelColor = textSecColor,
                                    focusedBorderColor = NeonBlue,
                                    unfocusedBorderColor = cardBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonBlue) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedLabelColor = NeonBlue,
                                    unfocusedLabelColor = textSecColor,
                                    focusedBorderColor = NeonBlue,
                                    unfocusedBorderColor = cardBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonBlue) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = textSecColor
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedLabelColor = NeonBlue,
                                unfocusedLabelColor = textSecColor,
                                focusedBorderColor = NeonBlue,
                                unfocusedBorderColor = cardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = errorMessage,
                                color = NeonRed,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                if (isLoginMode) {
                                    if (identifier.isBlank() || password.isBlank()) {
                                        errorMessage = "Semua kolom wajib diisi!"
                                    } else {
                                        val localUName = sharedPrefs.getString("local_username", "") ?: ""
                                        val localEmail = sharedPrefs.getString("local_email", "") ?: ""
                                        val localPass = sharedPrefs.getString("local_password", "") ?: ""
                                        
                                        if ((identifier == localUName || identifier == localEmail) && password == localPass) {
                                            onAuthSuccess(localUName)
                                        } else {
                                            ApiClient.login(identifier, password,
                                                onSuccess = { cleanUname ->
                                                    coroutineScope.launch(Dispatchers.Main) {
                                                        sharedPrefs.edit()
                                                            .putString("local_username", cleanUname)
                                                            .putString("local_password", password)
                                                            .apply()
                                                        onAuthSuccess(cleanUname)
                                                    }
                                                },
                                                onError = {
                                                    coroutineScope.launch(Dispatchers.Main) {
                                                        errorMessage = "Gagal login! Periksa koneksi server."
                                                    }
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    if (username.isBlank() || email.isBlank() || password.isBlank()) {
                                        errorMessage = "Semua kolom wajib diisi!"
                                    } else if (!email.contains("@")) {
                                        errorMessage = "Format email tidak valid!"
                                    } else {
                                        sharedPrefs.edit()
                                            .putString("local_username", username.trim())
                                            .putString("local_email", email.trim())
                                            .putString("local_password", password.trim())
                                            .putBoolean("sync_pending", true)
                                            .apply()
                                            
                                        ApiClient.register(username.trim(), email.trim(), password.trim(),
                                            onSuccess = {
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    sharedPrefs.edit().putBoolean("sync_pending", false).apply()
                                                    onAuthSuccess(username.trim())
                                                }
                                            },
                                            onError = {
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    onAuthSuccess(username.trim())
                                                }
                                            }
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(LightBlue, NeonBlue)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Text(
                                text = if (isLoginMode) "Masuk" else "Daftar",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isLoginMode) "Belum punya akun? " else "Sudah punya akun? ",
                                color = textSecColor,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isLoginMode) "Daftar sekarang" else "Masuk sekarang",
                                color = NeonBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    isLoginMode = !isLoginMode
                                    errorMessage = ""
                                }
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
