package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.LambdaProject.MathArt.R

/* @Preview(showBackground = true)
@Composable
fun SpinningSun(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = rotation
            }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.2f
            val petalCount = 12
            val angleStep = 360f / petalCount

            repeat(petalCount) { i ->
                val angle = i * angleStep  // angle dalam DERAJAT, bukan radian!
                withTransform({
                    rotate(angle, pivot = center)
                }) {
                    drawPath(
                        path = Path().apply {
                            moveTo(center.x, center.y)
                            lineTo(center.x + radius * 0.1f, center.y - radius)
                            lineTo(center.x - radius * 0.1f, center.y - radius)
                            close()
                        },
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Yellow,
                                Color.Yellow.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            startY = center.y - radius,
                            endY = center.y
                        )
                    )
                }
            }
        }

        // Gambar koin/logo di tengah
        Image(
            painter = painterResource(id = R.drawable.ic_poin),
            contentDescription = "Koin",
            modifier = Modifier.size(48.dp)
        )
    }
} */