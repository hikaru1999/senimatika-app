package com.LambdaProject.MathArt.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
fun FogOfWarOverlay(
    mapId: String,
    playerX: Int,
    playerY: Int,
    discoveredTiles: Set<Pair<Int, Int>>,
    isBinocularActive: Boolean = false,
    isLanternActive: Boolean,
    isTorchActive: Boolean,
    isCombined: Boolean,
    tileSize: Dp,
    offsetX: Dp,
    offsetY: Dp,
    maxWidth: Dp,
    maxHeight: Dp
) {
    val density = LocalDensity.current
    val tileSizePx = with(density) { tileSize.toPx() }

    val radiusMultiplier = when {
        isLanternActive -> if (isCombined) 1f else 3.2f
        isTorchActive -> if (isCombined) 1f else 2.5f
        else -> 1f /* if (isCombined) 1f else 1.5f */
    }

    val animatedRadiusMultiplier by animateFloatAsState(
        targetValue = if (isBinocularActive) radiusMultiplier * 3f else radiusMultiplier,
        /* targetValue = if (isBinocularActive) 3f else 1.5f, */
        animationSpec = tween(durationMillis = 500),
        label = "binocularRadius"
    )

    /* val cloudBitmap = ImageBitmap.imageResource(id = R.drawable.img_fog) */

    Canvas(modifier = Modifier
        .fillMaxSize()
        .graphicsLayer(alpha = 0.99f)
    ) {
        /* val shader = ImageShader(
            image = cloudBitmap,
            tileModeX = TileMode.Repeated,
            tileModeY = TileMode.Repeated
        )
        val brush = ShaderBrush(shader)
        drawRect(brush = brush) */

        drawRect(color = Color(0xFFDEE4EB))


        discoveredTiles.forEach { (tx, ty) ->
            val screenX = tx * tileSizePx + offsetX.toPx() + (tileSizePx / 2)
            val screenY = ty * tileSizePx + offsetY.toPx() + (tileSizePx / 2)
            val holeRadius = tileSizePx * animatedRadiusMultiplier

            if (screenX > -tileSizePx && screenX < size.width + tileSizePx &&
                screenY > -tileSizePx && screenY < size.height + tileSizePx) {

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.White),
                        center = Offset(screenX, screenY),
                        radius = holeRadius
                    ),
                    radius = holeRadius,
                    center = Offset(screenX, screenY),
                    blendMode = BlendMode.DstIn
                )
            }
        }
    }
}
