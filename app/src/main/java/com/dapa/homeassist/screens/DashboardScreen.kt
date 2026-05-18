package com.dapa.homeassist.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dapa.homeassist.model.AiSuggestion
import com.dapa.homeassist.model.ControlRequest
import com.dapa.homeassist.network.ApiClient
import com.google.gson.Gson
import com.dapa.homeassist.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    username: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("home_assist", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()
    
    var ipDialogVisible by remember { mutableStateOf(false) }
    var tempIp by remember { mutableStateOf(sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp) }
    var tempPort by remember { mutableStateOf(sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort) }
    
    var isServerOnline by remember { mutableStateOf(false) }
    var roomTemp by remember { mutableStateOf(26.5f) }
    var roomHumid by remember { mutableStateOf(60f) }
    
    var acPower by remember { mutableStateOf(false) }
    var acTargetTemp by remember { mutableStateOf(24) }
    var acMode by remember { mutableStateOf("cool") }
    var acFan by remember { mutableStateOf("high") }
    var estimatedWatts by remember { mutableStateOf(0f) }
    
    var aiSuggestions by remember { mutableStateOf<List<AiSuggestion>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition()
    val phaseX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val cardBg = if (isDarkMode) DarkCard else Color(0x33FFFFFF)
    val cardBorder = if (isDarkMode) DarkBorder else LightBorder
    val textColor = if (isDarkMode) TextWhite else TextDark
    val textSecColor = if (isDarkMode) TextGray else TextDarkGray

    fun syncOfflineRegister() {
        val syncPending = sharedPrefs.getBoolean("sync_pending", false)
        if (syncPending) {
            val u = sharedPrefs.getString("local_username", "") ?: ""
            val e = sharedPrefs.getString("local_email", "") ?: ""
            val p = sharedPrefs.getString("local_password", "") ?: ""
            
            if (u.isNotEmpty() && e.isNotEmpty() && p.isNotEmpty()) {
                ApiClient.register(u, e, p,
                    onSuccess = {
                        sharedPrefs.edit().putBoolean("sync_pending", false).apply()
                    },
                    onError = {}
                )
            }
        }
    }

    fun connectWebSocket() {
        ApiClient.connectWebSocket(
            onOpen = {
                isServerOnline = true
                syncOfflineRegister()
            },
            onMessage = { text ->
                coroutineScope.launch(Dispatchers.Main) {
                    try {
                        val gson = Gson()
                        val map = gson.fromJson(text, Map::class.java)
                        val type = map["type"] as? String
                        
                        if (type == "status_sync") {
                            val payloadJson = gson.toJson(map["payload"])
                            val payload = gson.fromJson(payloadJson, Map::class.java)
                            acPower = payload["power"] as? Boolean ?: false
                            acTargetTemp = (payload["temp"] as? Double)?.toInt() ?: 24
                            acMode = payload["mode"] as? String ?: "cool"
                            acFan = payload["fan"] as? String ?: "high"
                            
                            if (acPower) {
                                val base = 600
                                val diff = maxOf(0, 30 - acTargetTemp)
                                estimatedWatts = (base + (diff * 35)).toFloat()
                            } else {
                                estimatedWatts = 0f
                            }
                        } else if (type == "ac_state_update") {
                            val payloadJson = gson.toJson(map["payload"])
                            val payload = gson.fromJson(payloadJson, Map::class.java)
                            acPower = payload["power"] as? Boolean ?: false
                            acTargetTemp = (payload["temp"] as? Double)?.toInt() ?: 24
                            acMode = payload["mode"] as? String ?: "cool"
                            acFan = payload["fan"] as? String ?: "high"
                            
                            val wattVal = map["currentPower"] as? Double
                            estimatedWatts = wattVal?.toFloat() ?: 0f
                        } else if (type == "live_data_update") {
                            val payloadJson = gson.toJson(map["payload"])
                            val payload = gson.fromJson(payloadJson, Map::class.java)
                            
                            val tempLogJson = gson.toJson(payload["temperatureLog"])
                            val tempLog = gson.fromJson(tempLogJson, Map::class.java)
                            roomTemp = (tempLog["temperature"] as? Double)?.toFloat() ?: roomTemp
                            roomHumid = (tempLog["humidity"] as? Double)?.toFloat() ?: roomHumid
                            
                            val wattVal = map["currentPower"] as? Double ?: payload["currentPower"] as? Double
                            estimatedWatts = wattVal?.toFloat() ?: estimatedWatts
                        } else if (type == "ai_update") {
                            val payloadJson = gson.toJson(map["payload"])
                            val listType = object : com.google.gson.reflect.TypeToken<List<AiSuggestion>>() {}.type
                            aiSuggestions = gson.fromJson(payloadJson, listType)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            onClose = {
                isServerOnline = false
            },
            onError = {
                isServerOnline = false
            }
        )
    }

    fun syncData() {
        ApiClient.fetchStatus(
            onSuccess = { response ->
                coroutineScope.launch(Dispatchers.Main) {
                    isServerOnline = true
                    syncOfflineRegister()
                    acPower = response.acState.power
                    acTargetTemp = response.acState.temp
                    acMode = response.acState.mode
                    acFan = response.acState.fan
                    estimatedWatts = response.currentPower
                    
                    if (response.temperatureHistory.isNotEmpty()) {
                        val lastLog = response.temperatureHistory.last()
                        roomTemp = lastLog.temperature
                        roomHumid = lastLog.humidity
                    }
                    
                    aiSuggestions = response.aiSuggestions
                }
            },
            onError = {
                coroutineScope.launch(Dispatchers.Main) {
                    isServerOnline = false
                }
            }
        )
    }

    fun sendControlCommand() {
        val req = ControlRequest(
            power = acPower,
            temp = acTargetTemp,
            mode = acMode,
            fan = acFan
        )
        ApiClient.sendControl(req,
            onSuccess = {},
            onError = {
                isServerOnline = false
            }
        )
    }

    LaunchedEffect(key1 = true) {
        ApiClient.backendIp = sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp
        ApiClient.backendPort = sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort
        tempIp = ApiClient.backendIp
        tempPort = ApiClient.backendPort
        
        while (true) {
            val now = Calendar.getInstance().time
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(now)
            delay(1000)
        }
    }

    LaunchedEffect(key1 = ApiClient.backendIp, key2 = ApiClient.backendPort) {
        syncData()
        connectWebSocket()
        while (true) {
            delay(10000)
            if (!isServerOnline) {
                syncData()
                connectWebSocket()
            }
        }
    }

    DisposableEffect(key1 = true) {
        onDispose {
            ApiClient.disconnectWebSocket()
        }
    }

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
            val circle1X = width * 0.2f + (width * 0.18f * Math.cos(radX)).toFloat()
            val circle1Y = height * 0.3f + (height * 0.12f * Math.sin(radX)).toFloat()
            val circle2X = width * 0.8f + (width * 0.18f * Math.sin(radX * 1.4)).toFloat()
            val circle2Y = height * 0.6f + (height * 0.12f * Math.cos(radX * 1.4)).toFloat()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonBlue.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(circle1X, circle1Y),
                    radius = width * 0.7f
                ),
                center = Offset(circle1X, circle1Y),
                radius = width * 0.7f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LightBlue.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(circle2X, circle2Y),
                    radius = width * 0.7f
                ),
                center = Offset(circle2X, circle2Y),
                radius = width * 0.7f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Halo, $username 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = currentDate,
                        fontSize = 12.sp,
                        color = textSecColor
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, cardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { ipDialogVisible = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, cardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(1.dp, cardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Waktu Sekarang",
                                    fontSize = 12.sp,
                                    color = textSecColor
                                )
                                Text(
                                    text = currentTime,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isServerOnline) NeonGreen else NeonRed)
                                )
                                Text(
                                    text = if (isServerOnline) "SERVER ONLINE" else "SERVER OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isServerOnline) NeonGreen else NeonRed
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.Thermostat, contentDescription = null, tint = NeonBlue)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Suhu Kamar", fontSize = 12.sp, color = textSecColor)
                                Text("${String.format("%.1f", roomTemp)}°C", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = LightBlue)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Kelembaban", fontSize = 12.sp, color = textSecColor)
                                Text("${roomHumid.toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Kontrol AC LG", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(
                                        text = if (acPower) "Kondisi Nyala • ${estimatedWatts.toInt()} W" else "Kondisi Mati • 0 W",
                                        fontSize = 12.sp,
                                        color = if (acPower) NeonGreen else textSecColor
                                    )
                                }

                                Switch(
                                    checked = acPower,
                                    onCheckedChange = {
                                        acPower = it
                                        sendControlCommand()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = NeonBlue,
                                        uncheckedThumbColor = TextGray,
                                        uncheckedTrackColor = Color.Transparent
                                    )
                                )
                            }

                            if (acPower) {
                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (acTargetTemp > 16) {
                                                acTargetTemp--
                                                sendControlCommand()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(cardBg)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = textColor)
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Suhu Target", fontSize = 11.sp, color = textSecColor)
                                        Text("$acTargetTemp°C", fontSize = 32.sp, fontWeight = FontWeight.Black, color = textColor)
                                    }

                                    IconButton(
                                        onClick = {
                                            if (acTargetTemp < 30) {
                                                acTargetTemp++
                                                sendControlCommand()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(cardBg)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = textColor)
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text("Mode AC", fontSize = 12.sp, color = textSecColor)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("cool", "dry", "fan").forEach { mode ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (acMode == mode) NeonBlue else cardBg)
                                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    acMode = mode
                                                    sendControlCommand()
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = mode.uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (acMode == mode) Color.White else textColor
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Kecepatan Kipas", fontSize = 12.sp, color = textSecColor)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("low", "medium", "high").forEach { fan ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (acFan == fan) LightBlue else cardBg)
                                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    acFan = fan
                                                    sendControlCommand()
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = fan.uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (acFan == fan) Color.White else textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = NeonBlue)
                                    Text("AI Energy Assistant", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                                }

                                Button(
                                    onClick = {
                                        isAnalyzing = true
                                        ApiClient.triggerAnalyze(
                                            onSuccess = { list ->
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    aiSuggestions = list
                                                    isAnalyzing = false
                                                }
                                            },
                                            onError = {
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    isAnalyzing = false
                                                }
                                            }
                                        )
                                    },
                                    enabled = !isAnalyzing && isServerOnline,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(LightBlue, NeonBlue)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Text(
                                        text = if (isAnalyzing) "Proses..." else "Tanya AI",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (aiSuggestions.isEmpty()) {
                                Text(
                                    text = "Belum ada rekomendasi. Klik tombol 'Tanya AI' untuk menganalisis efisiensi AC kamu saat ini.",
                                    fontSize = 13.sp,
                                    color = textSecColor,
                                    lineHeight = 18.sp
                                )
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    aiSuggestions.forEach { suggestion ->
                                        val indicatorColor = when (suggestion.type) {
                                            "warning" -> NeonRed
                                            "success" -> NeonGreen
                                            else -> NeonBlue
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(cardBg)
                                                .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .offset(y = 5.dp)
                                                    .clip(CircleShape)
                                                    .background(indicatorColor)
                                            )
                                            Text(
                                                text = suggestion.text,
                                                fontSize = 13.sp,
                                                color = textColor,
                                                lineHeight = 18.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
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
                            syncData()
                            connectWebSocket()
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
}
