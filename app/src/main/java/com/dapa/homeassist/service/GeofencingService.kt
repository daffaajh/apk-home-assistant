package com.dapa.homeassist.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dapa.homeassist.MainActivity
import com.dapa.homeassist.R
import com.dapa.homeassist.model.ControlRequest
import com.dapa.homeassist.network.ApiClient

class GeofencingService : Service() {

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var isInsideHome = false

    companion object {
        const val CHANNEL_ID = "geofencing_channel"
        const val NOTIFICATION_ID = 99
        const val ALERT_CHANNEL_ID = "home_assist_alerts"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, getForegroundNotification())
        setupLocationTracking()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pemantauan Geofencing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Saluran untuk memantau jarak ke rumah di latar belakang"
            }
            
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Notifikasi Pintar AC",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi penting saat AC dinyalakan/dimatikan otomatis"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            manager?.createNotificationChannel(alertChannel)
        }
    }

    private fun getForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Home Assistant Geofencing Aktif")
            .setContentText("Memantau jarak Anda ke rumah...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun setupLocationTracking() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                checkGeofence(location)
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            // Request updates from GPS and Network
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                15000L, // 15 seconds
                10f,    // 10 meters change
                locationListener!!
            )
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                15000L,
                10f,
                locationListener!!
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun checkGeofence(currentLocation: Location) {
        val sharedPrefs = getSharedPreferences("home_assist", Context.MODE_PRIVATE)
        val enabled = sharedPrefs.getBoolean("geofence_enabled", false)
        if (!enabled) {
            stopSelf()
            return
        }

        val homeLat = sharedPrefs.getFloat("geofence_lat", -6.2000f).toDouble()
        val homeLng = sharedPrefs.getFloat("geofence_lng", 106.8166f).toDouble()
        val radius = sharedPrefs.getFloat("geofence_radius", 500f) // in meters

        val homeLocation = Location("").apply {
            latitude = homeLat
            longitude = homeLng
        }

        val distance = currentLocation.distanceTo(homeLocation) // in meters
        val currentlyInside = distance <= radius

        // Detect transitions
        if (currentlyInside && !isInsideHome) {
            // Transition: Outside -> Inside (Approaching home)
            isInsideHome = true
            triggerAcControl(power = true, message = "Anda sedang mendekati rumah! AC LG telah dinyalakan secara otomatis.")
        } else if (!currentlyInside && isInsideHome) {
            // Transition: Inside -> Outside (Leaving home)
            isInsideHome = false
            triggerAcControl(power = false, message = "Anda telah meninggalkan area rumah. AC LG telah dimatikan otomatis.")
        }
    }

    private fun triggerAcControl(power: Boolean, message: String) {
        // Load API IP
        val sharedPrefs = getSharedPreferences("home_assist", Context.MODE_PRIVATE)
        ApiClient.backendIp = sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp
        ApiClient.backendPort = sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort

        ApiClient.sendControl(
            ControlRequest(power = power),
            onSuccess = {
                sendNotification(message)
            },
            onError = {
                // Keep trying or fail silently
            }
        )
    }

    private fun sendNotification(contentText: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("Sistem Notifikasi Pintar")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(101, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        locationListener?.let {
            locationManager?.removeUpdates(it)
        }
    }
}
