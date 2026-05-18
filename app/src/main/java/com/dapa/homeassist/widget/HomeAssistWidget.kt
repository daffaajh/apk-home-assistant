package com.dapa.homeassist.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dapa.homeassist.MainActivity
import com.dapa.homeassist.R
import com.dapa.homeassist.model.ControlRequest
import com.dapa.homeassist.network.ApiClient

class HomeAssistWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_POWER = "com.dapa.homeassist.widget.TOGGLE_POWER"
        const val ACTION_TEMP_UP = "com.dapa.homeassist.widget.TEMP_UP"
        const val ACTION_TEMP_DOWN = "com.dapa.homeassist.widget.TEMP_DOWN"

        private var localPower = false
        private var localTemp = 24
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Setup API client IP/Port first
        val sharedPrefs = context.getSharedPreferences("home_assist", Context.MODE_PRIVATE)
        ApiClient.backendIp = sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp
        ApiClient.backendPort = sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort

        when (intent.action) {
            ACTION_TOGGLE_POWER -> {
                localPower = !localPower
                ApiClient.sendControl(
                    ControlRequest(power = localPower),
                    onSuccess = { updateAllWidgets(context) },
                    onError = { updateAllWidgets(context) }
                )
            }
            ACTION_TEMP_UP -> {
                if (localTemp < 30) {
                    localTemp++
                    ApiClient.sendControl(
                        ControlRequest(temp = localTemp),
                        onSuccess = { updateAllWidgets(context) },
                        onError = { updateAllWidgets(context) }
                    )
                }
            }
            ACTION_TEMP_DOWN -> {
                if (localTemp > 16) {
                    localTemp--
                    ApiClient.sendControl(
                        ControlRequest(temp = localTemp),
                        onSuccess = { updateAllWidgets(context) },
                        onError = { updateAllWidgets(context) }
                    )
                }
            }
        }
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, HomeAssistWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Update baseline display text
        views.setTextViewText(R.id.widget_status, "AC: ${if (localPower) "ON" else "OFF"}")
        views.setTextViewText(R.id.widget_temp_val, "${localTemp}°C")

        // Setup pending intents for buttons
        views.setOnClickPendingIntent(R.id.widget_btn_power, getPendingSelfIntent(context, ACTION_TOGGLE_POWER))
        views.setOnClickPendingIntent(R.id.widget_btn_temp_up, getPendingSelfIntent(context, ACTION_TEMP_UP))
        views.setOnClickPendingIntent(R.id.widget_btn_temp_down, getPendingSelfIntent(context, ACTION_TEMP_DOWN))

        // Open main activity when tapping logo header
        val headerIntent = Intent(context, MainActivity::class.java)
        val headerPendingIntent = PendingIntent.getActivity(
            context, 0, headerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.header, headerPendingIntent)

        // Asynchronously fetch status to update stats
        val sharedPrefs = context.getSharedPreferences("home_assist", Context.MODE_PRIVATE)
        ApiClient.backendIp = sharedPrefs.getString("server_ip", ApiClient.backendIp) ?: ApiClient.backendIp
        ApiClient.backendPort = sharedPrefs.getString("server_port", ApiClient.backendPort) ?: ApiClient.backendPort

        ApiClient.fetchStatus(
            onSuccess = { response ->
                localPower = response.acState.power
                localTemp = response.acState.temp
                
                val lastLog = response.temperatureHistory.lastOrNull()
                val roomTemp = lastLog?.temperature ?: 24.0
                val roomHumid = lastLog?.humidity ?: 60.0
                
                views.setTextViewText(R.id.widget_status, "AC: ${if (localPower) "ON" else "OFF"}")
                views.setTextViewText(R.id.widget_temp_val, "${localTemp}°C")
                views.setTextViewText(R.id.widget_room_stats, String.format("Kamar: %.1f°C | %.0f%%", roomTemp, roomHumid))
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
            },
            onError = {
                views.setTextViewText(R.id.widget_room_stats, "Kamar: Offline")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, HomeAssistWidget::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
