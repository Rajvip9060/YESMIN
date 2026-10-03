package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    IDLE, LISTENING, THINKING, SPEAKING
}

@Composable
fun AiOrb(
    state: OrbState = OrbState.IDLE,
    size: Dp = 180.dp,
    amplitude: Float = 0f,
    accentColor: Color = Color(0xFFEC4899),
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransition")

    // Rotation angle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == OrbState.THINKING) 3000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Breathing pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.SPEAKING -> 600
                    OrbState.LISTENING -> 800
                    OrbState.THINKING -> 500
                    OrbState.IDLE -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    // Ripple radius animation
    val rippleScale = remember { Animatable(1f) }
    LaunchedEffect(state) {
        if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
            rippleScale.animateTo(
                targetValue = 1.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rippleScale.snapTo(1f)
        }
    }

    val primaryGlow = when (state) {
        OrbState.IDLE -> Color(0xFF9333EA)
        OrbState.LISTENING -> Color(0xFF06B6D4)
        OrbState.THINKING -> Color(0xFFF59E0B)
        OrbState.SPEAKING -> accentColor
    }

    val secondaryGlow = when (state) {
        OrbState.IDLE -> Color(0xFF3B82F6)
        OrbState.LISTENING -> Color(0xFF10B981)
        OrbState.THINKING -> Color(0xFFEC4899)
        OrbState.SPEAKING -> Color(0xFF8B5CF6)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 3.2f
            val dynamicRadius = baseRadius * pulse * (1f + (amplitude * 0.08f).coerceAtMost(0.3f))

            // Outer ripple ring when active
            if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryGlow.copy(alpha = 0.35f), Color.Transparent),
                        center = center,
                        radius = dynamicRadius * 1.6f
                    ),
                    radius = dynamicRadius * 1.55f,
                    center = center
                )
            }

            // Outer soft glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryGlow.copy(alpha = 0.5f), primaryGlow.copy(alpha = 0.15f), Color.Transparent),
                    center = center,
                    radius = dynamicRadius * 1.5f
                ),
                radius = dynamicRadius * 1.4f,
                center = center
            )

            // Inner Orb sphere with gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryGlow,
                        secondaryGlow,
                        Color(0xFF0F172A)
                    ),
                    center = Offset(center.x - dynamicRadius * 0.25f, center.y - dynamicRadius * 0.25f),
                    radius = dynamicRadius
                ),
                radius = dynamicRadius,
                center = center
            )

            // Futuristic Orbiting Rings
            rotate(rotation, center) {
                drawCircle(
                    color = primaryGlow.copy(alpha = 0.7f),
                    radius = dynamicRadius * 1.18f,
                    center = center,
                    style = Stroke(width = 2.5f)
                )

                // Orbiting satellite dots
                for (i in 0 until 4) {
                    val angle = (i * 90) * (Math.PI / 180.0)
                    val orbitX = center.x + (dynamicRadius * 1.18f) * cos(angle).toFloat()
                    val orbitY = center.y + (dynamicRadius * 1.18f) * sin(angle).toFloat()
                    drawCircle(
                        color = Color.White,
                        radius = 4.5f,
                        center = Offset(orbitX, orbitY)
                    )
                }
            }

            // Reverse Orbiting Ring
            rotate(-rotation * 1.3f, center) {
                drawCircle(
                    color = secondaryGlow.copy(alpha = 0.6f),
                    radius = dynamicRadius * 1.32f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                val angle = 45 * (Math.PI / 180.0)
                val dotX = center.x + (dynamicRadius * 1.32f) * cos(angle).toFloat()
                val dotY = center.y + (dynamicRadius * 1.32f) * sin(angle).toFloat()
                drawCircle(
                    color = accentColor,
                    radius = 5f,
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}
