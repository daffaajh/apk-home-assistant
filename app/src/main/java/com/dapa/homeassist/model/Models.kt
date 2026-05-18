package com.dapa.homeassist.model

data class AcState(
    val power: Boolean,
    val temp: Int,
    val mode: String,
    val fan: String
)

data class TempLog(
    val timestamp: String,
    val temperature: Float,
    val humidity: Float
)

data class PowerLog(
    val timestamp: String,
    val watts: Float
)

data class AiSuggestion(
    val id: String,
    val timestamp: String,
    val text: String,
    val type: String
)

data class StatusResponse(
    val success: Boolean,
    val acState: AcState,
    val currentPower: Float,
    val temperatureHistory: List<TempLog>,
    val powerHistory: List<PowerLog>,
    val aiSuggestions: List<AiSuggestion>
)

data class ControlRequest(
    val power: Boolean? = null,
    val temp: Int? = null,
    val mode: String? = null,
    val fan: String? = null
)
