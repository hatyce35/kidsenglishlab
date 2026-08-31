package com.example.kidsenglishlab.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class Particle(
    val xRatio: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 45
) {
    val colors = listOf(
        Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF10B981),
        Color(0xFF3B82F6), Color(0xFFA855F7), Color(0xFFEC4899),
        Color(0xFFFDE047), Color(0xFF38BDF8)
    )

    val particles = remember {
        List(particleCount) {
            Particle(
                xRatio = Random.nextFloat(),
                initialY = -Random.nextFloat() * 200f,
                speed = 0.6f + Random.nextFloat() * 0.8f,
                size = 12f + Random.nextFloat() * 16f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value

        particles.forEach { p ->
            val y = p.initialY + (h + 300f) * t * p.speed
            val x = (p.xRatio * w) + (kotlin.math.sin((t * 6f + p.xRatio * 10f).toDouble()).toFloat() * 30f)
            val rot = t * p.rotationSpeed

            if (y in -50f..h + 50f) {
                rotate(rot, pivot = Offset(x, y)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color,
                            radius = p.size / 2f,
                            center = Offset(x, y)
                        )
                    } else {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(x - p.size / 2f, y - p.size / 3f),
                            size = Size(p.size, p.size * 0.65f)
                        )
                    }
                }
            }
        }
    }
}
