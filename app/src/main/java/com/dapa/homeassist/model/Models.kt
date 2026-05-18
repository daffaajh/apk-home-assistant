package com.dapa.homeassist.model

data class AcState(
    val systemActive: Boolean = true,
    val power: Boolean = false,
    val temp: Int = 24,
    val mode: String = "cool",
    val fan: String = "high"
)

data class ControlRequest(
    val systemActive: Boolean? = null,
    val power: Boolean? = null,
    val temp: Int? = null,
    val mode: String? = null,
    val fan: String? = null
)

data class TemperatureLog(
    val timestamp: String,
    val temperature: Float,
    val humidity: Float
)

data class AiSuggestion(
    val type: String,
    val text: String
)

data class StatusResponse(
    val success: Boolean,
    val acState: AcState,
    val currentPower: Float,
    val temperatureHistory: List<TemperatureLog>,
    val aiSuggestions: List<AiSuggestion>
)

data class ChatMessage(
    val role: String,
    val content: String
)
