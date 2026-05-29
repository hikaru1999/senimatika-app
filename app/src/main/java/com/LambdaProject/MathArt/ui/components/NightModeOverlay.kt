package com.LambdaProject.MathArt.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.copy
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun NightModeOverlay(
    mapId: String,
    playerX: Int,
    playerY: Int,
    isTorchActive: Boolean,
    isLanternActive: Boolean,
    isCombined: Boolean,
    tileSize: Dp,
    offsetX: Dp,
    offsetY: Dp
) {
    val density = LocalDensity.current
    val tileSizePx = with(density) { tileSize.toPx() }

    /* val targetMultiplier = when {
        isLanternActive -> 3.6f
        isTorchActive -> 2.5f
        else -> 1.5f
    } */

    val targetMultiplier = when {
        isLanternActive -> if (isCombined) 2.75f else 3.6f
        isTorchActive -> if (isCombined) 2.35f else 2.5f
        else -> if (isCombined) 1.5f else 1.5f
    }

    val animatedRadius by animateFloatAsState(
        targetValue = tileSizePx * targetMultiplier,
        animationSpec = tween(1000),
        label = "nightLightRadius"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
    ) {

        drawRect(color = Color(0xFF000000).copy(alpha = 0.9f))

        drawRect(color = Color.Black.copy(alpha = if (isCombined) 0.85f else 1f))

        val centerX = size.width / 2
        val centerY = size.height / 2

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black, Color.Transparent),
                center = Offset(centerX, centerY),
                radius = animatedRadius
            ),
            radius = animatedRadius,
            center = Offset(centerX, centerY),
            blendMode = BlendMode.DstOut
        )
    }
}