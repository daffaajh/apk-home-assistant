package com.dapa.homeassist.network

import com.dapa.homeassist.model.AiSuggestion
import com.dapa.homeassist.model.ControlRequest
import com.dapa.homeassist.model.StatusResponse
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ApiClient {
    var backendIp: String = "192.168.1.100"
    var backendPort: String = "3000"

    private val client = OkHttpClient()
    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    
    private var webSocket: WebSocket? = null
    private var wsListener: WsListener? = null

    val httpUrl: String
        get() = "http://$backendIp:$backendPort"

    val wsUrl: String
        get() = "ws://$backendIp:$backendPort"

    fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val payload = mapOf(
            "username" to username,
            "email" to email,
            "password" to password
        )
        val jsonStr = gson.toJson(payload)
        val requestBody = jsonStr.toRequestBody(JSON)
        val request = Request.Builder()
            .url("$httpUrl/api/register")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(IOException("Gagal registrasi: " + response.code))
                }
            }
        })
    }

    fun login(
        identifier: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val payload = mapOf(
            "identifier" to identifier,
            "password" to password
        )
        val jsonStr = gson.toJson(payload)
        val requestBody = jsonStr.toRequestBody(JSON)
        val request = Request.Builder()
            .url("$httpUrl/api/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val result = gson.fromJson(body, Map::class.java)
                        val uname = result["username"] as? String ?: identifier
                        onSuccess(uname)
                    } else {
                        onError(IOException("Gagal login: " + response.code))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    fun fetchStatus(onSuccess: (StatusResponse) -> Unit, onError: (Exception) -> Unit) {
        val request = Request.Builder()
            .url("$httpUrl/api/status")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val result = gson.fromJson(body, StatusResponse::class.java)
                        onSuccess(result)
                    } else {
                        onError(IOException("Failed to fetch status"))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    fun sendControl(controlRequest: ControlRequest, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val jsonStr = gson.toJson(controlRequest)
        val requestBody = jsonStr.toRequestBody(JSON)
        val request = Request.Builder()
            .url("$httpUrl/api/control")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(IOException("Failed to control AC"))
                }
            }
        })
    }

    fun triggerAnalyze(onSuccess: (List<AiSuggestion>) -> Unit, onError: (Exception) -> Unit) {
        val requestBody = "".toRequestBody(JSON)
        val request = Request.Builder()
            .url("$httpUrl/api/analyze")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        val map = gson.fromJson(body, Map::class.java)
                        val suggestionsJson = gson.toJson(map["suggestions"])
                        val listType = object : com.google.gson.reflect.TypeToken<List<AiSuggestion>>() {}.type
                        val suggestions = gson.fromJson<List<AiSuggestion>>(suggestionsJson, listType)
                        onSuccess(suggestions)
                    } else {
                        onError(IOException("Failed to analyze"))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }
        })
    }

    fun connectWebSocket(
        onOpen: () -> Unit,
        onMessage: (String) -> Unit,
        onClose: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        disconnectWebSocket()

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        wsListener = WsListener(onOpen, onMessage, onClose, onError)
        webSocket = client.newWebSocket(request, wsListener!!)
    }

    fun disconnectWebSocket() {
        webSocket?.close(1000, "Goodbye")
        webSocket = null
        wsListener = null
    }

    private class WsListener(
        private val onOpen: () -> Unit,
        private val onMessage: (String) -> Unit,
        private val onClose: () -> Unit,
        private val onError: (Throwable) -> Unit
    ) : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send(gson.toJson(mapOf("type" to "identify", "clientType" to "app")))
            onOpen()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            onMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            onClose()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            onError(t)
        }
    }
}
