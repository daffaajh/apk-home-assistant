package com.dapa.homeassist.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.dapa.homeassist.theme.NeonBlue
import com.dapa.homeassist.theme.NeonGreen
import kotlinx.coroutines.delay

@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glowColor: Color = NeonBlue,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Gloss reflection glow overlay for Liquid 3D Look
        Canvas(modifier = Modifier.matchParentSize()) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.4f)
                cubicTo(
                    0f, size.height * 0.1f,
                    size.width * 0.1f, 0f,
                    size.width * 0.4f, 0f
                )
                lineTo(size.width * 0.6f, 0f)
                cubicTo(
                    size.width * 0.9f, 0f,
                    size.width, size.height * 0.1f,
                    size.width, size.height * 0.4f
                )
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.Transparent
                    )
                )
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun LiquidGlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonGreen,
    inactiveColor: Color = Color.White.copy(alpha = 0.15f)
) {
    val trackWidth = 56.dp
    val trackHeight = 30.dp
    val thumbSize = 24.dp
    
    val targetOffset = if (checked) 28.dp else 2.dp
    
    val animatedOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
    )
    
    // Stretch animation depending on movement direction
    var isMoving by remember(checked) { mutableStateOf(false) }
    LaunchedEffect(checked) {
        isMoving = true
        delay(180)
        isMoving = false
    }
    
    val scaleX by animateFloatAsState(
        targetValue = if (isMoving) 1.3f else 1.0f,
        animationSpec = tween(150)
    )

    val bgColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.25f) else inactiveColor,
        animationSpec = tween(250)
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
        animationSpec = tween(250)
    )

    Box(
        modifier = modifier
            .size(trackWidth, trackHeight)
            .clip(RoundedCornerShape(15.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(15.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Glowing liquid bubble thumb
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .graphicsLayer {
                    this.scaleX = scaleX
                }
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            if (checked) activeColor else Color.White.copy(alpha = 0.9f)
                        )
                    )
                )
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    clip = false
                )
        )
    }
}

@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonBlue,
    inactiveColor: Color = Color.White.copy(alpha = 0.1f)
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        
        val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val thumbOffset = (normalizedValue * widthPx).coerceIn(0f, widthPx)

        // Custom Slider Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(inactiveColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(normalizedValue)
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(activeColor.copy(alpha = 0.4f), activeColor)
                        )
                    )
            )
        }

        // Thumb Button
        var isDragging by remember { mutableStateOf(false) }
        val thumbScale by animateFloatAsState(
            targetValue = if (isDragging) 1.4f else 1.0f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
        )

        val dragModifier = Modifier
            .offset(x = with(LocalDensity.current) { (thumbOffset - 12.dp.toPx()).toDp() })
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                ) { change, dragAmount ->
                    change.consume()
                    val newOffsetPx = (thumbOffset + dragAmount.x).coerceIn(0f, widthPx)
                    val newValue = valueRange.start + (newOffsetPx / widthPx) * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue.coerceIn(valueRange))
                }
            }

        Box(
            modifier = dragModifier
                .graphicsLayer {
                    scaleX = thumbScale
                    scaleY = thumbScale
                }
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, activeColor)
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                .shadow(3.dp, CircleShape)
        )
    }
}

@Composable
fun LiquidBottomTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonBlue,
    content: @Composable RowScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                ),
                RoundedCornerShape(32.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val tabWidthPx = widthPx / tabsCount
        val tabWidthDp = with(LocalDensity.current) { tabWidthPx.toDp() }
        
        // Elastic Liquid Selection Indicator
        val targetOffset = tabWidthDp * selectedTabIndex
        val animatedOffset by animateDpAsState(
            targetValue = targetOffset,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)
        )
        
        // Stretch width during transition
        var isTransitioning by remember { mutableStateOf(false) }
        LaunchedEffect(selectedTabIndex) {
            isTransitioning = true
            delay(180)
            isTransitioning = false
        }
        val scaleX by animateFloatAsState(
            targetValue = if (isTransitioning) 1.2f else 1.0f,
            animationSpec = tween(150)
        )

        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .graphicsLayer {
                    this.scaleX = scaleX
                }
                .width(tabWidthDp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.2f),
                            activeColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(activeColor.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        )
        
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun RowScope.LiquidBottomTab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}
