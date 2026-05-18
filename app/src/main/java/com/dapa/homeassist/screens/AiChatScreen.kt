package com.dapa.homeassist.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dapa.homeassist.model.ChatMessage
import com.dapa.homeassist.network.ApiClient
import com.dapa.homeassist.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    isDarkMode: Boolean
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
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
            val circle1X = width * 0.7f + (width * 0.15f * Math.cos(radX)).toFloat()
            val circle1Y = height * 0.2f + (height * 0.1f * Math.sin(radX)).toFloat()
            val circle2X = width * 0.3f + (width * 0.15f * Math.sin(radX * 1.5)).toFloat()
            val circle2Y = height * 0.8f + (height * 0.1f * Math.cos(radX * 1.5)).toFloat()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LightBlue.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(circle1X, circle1Y),
                    radius = width * 0.65f
                ),
                center = Offset(circle1X, circle1Y),
                radius = width * 0.65f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonBlue.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(circle2X, circle2Y),
                    radius = width * 0.65f
                ),
                center = Offset(circle2X, circle2Y),
                radius = width * 0.65f
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = "AI Smart Agent",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Tanyakan sesuatu kepada AI",
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Misal: 'Apakah suhu kamarku normal?', 'Berapa pemakaian listrik AC?'",
                                    color = textSecColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 20.dp,
                                        topEnd = 20.dp,
                                        bottomStart = if (isUser) 20.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 20.dp
                                    )
                                )
                                .background(if (isUser) NeonBlue else cardBg)
                                .border(1.dp, if (isUser) Color.Transparent else cardBorder, RoundedCornerShape(
                                        topStart = 20.dp,
                                        topEnd = 20.dp,
                                        bottomStart = if (isUser) 20.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 20.dp
                                ))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = msg.content,
                                color = if (isUser) Color.White else textColor,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                                    .padding(16.dp)
                            ) {
                                Text("Berpikir...", color = textSecColor, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ketik pesan...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = cardBorder,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    val userMsg = inputText.trim()
                                    messages = messages + ChatMessage("user", userMsg)
                                    inputText = ""
                                    isLoading = true
                                    
                                    ApiClient.sendChatMessage(userMsg,
                                        onSuccess = { reply ->
                                            coroutineScope.launch {
                                                messages = messages + ChatMessage("ai", reply)
                                                isLoading = false
                                            }
                                        },
                                        onError = {
                                            coroutineScope.launch {
                                                messages = messages + ChatMessage("ai", "Gagal terhubung ke server.")
                                                isLoading = false
                                            }
                                        }
                                    )
                                }
                            },
                            enabled = !isLoading && inputText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Kirim", tint = if (inputText.isNotBlank()) NeonBlue else textSecColor)
                        }
                    }
                )
            }
        }
    }
}
