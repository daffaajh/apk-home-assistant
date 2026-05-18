package com.dapa.homeassist.screens

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
import com.dapa.homeassist.service.GeofencingService
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
    val scrollState = rememberScrollState()

    var tempIp by remember { mutableStateOf(sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp) }
    var tempPort by remember { mutableStateOf(sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    // Geofencing states
    var geofenceEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("geofence_enabled", false)) }
    var geofenceLat by remember { mutableStateOf(sharedPrefs.getFloat("geofence_lat", -6.2000f).toString()) }
    var geofenceLng by remember { mutableStateOf(sharedPrefs.getFloat("geofence_lng", 106.8166f).toString()) }
    var geofenceRadius by remember { mutableStateOf(sharedPrefs.getFloat("geofence_radius", 500f)) }

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
                .verticalScroll(scrollState)
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

            // Server Config Panel
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

                    Spacer(modifier = Modifier.height(20.dp))

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

            // Geofencing Control Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-On/Off Geofencing", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("Nyalakan AC saat mendekati rumah.", fontSize = 12.sp, color = textSecColor)
                        }
                        Switch(
                            checked = geofenceEnabled,
                            onCheckedChange = { checked ->
                                geofenceEnabled = checked
                                sharedPrefs.edit().putBoolean("geofence_enabled", checked).apply()
                                
                                val intent = Intent(context, GeofencingService::class.java)
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                    Toast.makeText(context, "Pemantauan Geofencing latar belakang aktif!", Toast.LENGTH_SHORT).show()
                                } else {
                                    context.stopService(intent)
                                    Toast.makeText(context, "Geofencing dinonaktifkan.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonBlue, checkedTrackColor = NeonBlue.copy(alpha = 0.5f))
                        )
                    }

                    if (geofenceEnabled) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = cardBorder)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Latitude
                        OutlinedTextField(
                            value = geofenceLat,
                            onValueChange = { 
                                geofenceLat = it
                                it.toFloatOrNull()?.let { lat -> sharedPrefs.edit().putFloat("geofence_lat", lat).apply() }
                            },
                            label = { Text("Latitude Rumah") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = NeonBlue, unfocusedBorderColor = cardBorder)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Longitude
                        OutlinedTextField(
                            value = geofenceLng,
                            onValueChange = { 
                                geofenceLng = it
                                it.toFloatOrNull()?.let { lng -> sharedPrefs.edit().putFloat("geofence_lng", lng).apply() }
                            },
                            label = { Text("Longitude Rumah") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = NeonBlue, unfocusedBorderColor = cardBorder)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Get Location Button
                        Button(
                            onClick = {
                                try {
                                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                    val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                    
                                    if (loc != null) {
                                        geofenceLat = loc.latitude.toString()
                                        geofenceLng = loc.longitude.toString()
                                        sharedPrefs.edit()
                                            .putFloat("geofence_lat", loc.latitude.toFloat())
                                            .putFloat("geofence_lng", loc.longitude.toFloat())
                                            .apply()
                                        Toast.makeText(context, "Koordinat rumah berhasil dideteksi!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Nyalakan GPS HP kamu terlebih dahulu ya!", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: SecurityException) {
                                    Toast.makeText(context, "Izin akses lokasi ditolak. Aktifkan izin GPS di setelan HP.", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                            modifier = Modifier.fillMaxWidth().border(1.dp, NeonBlue, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = NeonBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gunakan Lokasi Saat Ini", color = NeonBlue, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Radius config slider
                        Text("Radius Geofence: ${geofenceRadius.toInt()} meter", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Slider(
                            value = geofenceRadius,
                            onValueChange = { 
                                geofenceRadius = it
                                sharedPrefs.edit().putFloat("geofence_radius", it).apply()
                            },
                            valueRange = 50f..2000f,
                            steps = 39, // Increments of 50m
                            colors = SliderDefaults.colors(thumbColor = NeonBlue, activeTrackColor = NeonBlue)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
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
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
