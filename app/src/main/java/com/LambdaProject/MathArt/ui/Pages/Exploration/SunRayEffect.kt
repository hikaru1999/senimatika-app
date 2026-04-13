package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.*

@Composable
fun SunRayEffect(
    modifier: Modifier = Modifier,
    rayColor: Color = Color(0xFFFFD600)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rays")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val rayCount = 8
        val rayAngle = 360f / rayCount
        val rayWidth = 25f
        val radius = size.width.coerceAtLeast(size.height) * 0.9f

        rotate(rotation) {
            for (i in 0 until rayCount) {
                val angle = i * rayAngle
                val brush = Brush.radialGradient(
                    colors = listOf(
                        rayColor.copy(alpha = 0.6f),
                        rayColor.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                )

                val path = Path().apply {
                    moveTo(center.x, center.y)
                    val x1 = center.x + radius * cos(Math.toRadians((angle - rayWidth / 2).toDouble())).toFloat()
                    val y1 = center.y + sin(Math.toRadians((angle - rayWidth / 2).toDouble())).toFloat() * radius
                    val x2 = center.x + radius * cos(Math.toRadians((angle + rayWidth / 2).toDouble())).toFloat()
                    val y2 = center.y + sin(Math.toRadians((angle + rayWidth / 2).toDouble())).toFloat() * radius
                    lineTo(x1, y1)
                    lineTo(x2, y2)
                    close()
                }
                drawPath(path, brush = brush)
            }
        }
    }
}