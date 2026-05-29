package com.LambdaProject.MathArt.ui.Pages.Exploration

//import androidx.compose.foundation.*
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.*
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.*
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.Dp
//import com.LambdaProject.MathArt.ViewModels.MapViewModel
//
//@Composable
//fun FogLayer (
//    viewModel: MapViewModel,
//    tileSize: Dp
//) {
//    val density = LocalDensity.current
//
//    Canvas(
//        modifier = Modifier
//            .fillMaxSize()
//    ) {
//        val tilePx = with(density) { tileSize.toPx() }
//
//        drawRect(
//            color = Color.Black.copy(alpha = 0.75f)
//        )
//
//        viewModel.visibleTiles.forEach { (x, y) ->
//
//            val cx = (x * tilePx) + tilePx / 2
//            val cy = (y * tilePx) + tilePx / 2
//
//            drawCircle(
//                brush = Brush.radialGradient(
//                    colors = listOf(
//                        Color.Transparent,
//                        Color.Transparent,
//                        Color.Black.copy(alpha = 0.4f)
//                    ),
//                    radius = tilePx * 1.2f,
//                    center = Offset(cx, cy)
//                ),
//                radius = tilePx * 1.2f,
//                center = Offset(cx, cy),
//                blendMode = BlendMode.Clear
//            )
//        }
//
//        viewModel.visibleTiles.forEach { (x, y) ->
//
//            val cx = (x * tilePx) + tilePx / 2
//            val cy = (y * tilePx) + tilePx / 2
//
//            drawCircle(
//                brush = Brush.radialGradient(
//                    colors = listOf(
//                        Color.Transparent,
//                        Color.Transparent,
//                        Color.Black.copy(alpha = 0.4f)
//                    ),
//                    radius = tilePx * 1.2f,
//                    center = Offset(cx, cy)
//                ),
//                radius = tilePx * 1.2f,
//                center = Offset(cx, cy),
//                blendMode = BlendMode.Clear
//            )
//        }
//    }
//}
//
//@Composable
//fun SoftFogLayer(
//    tileSize: Dp
//) {
//
//    val density = LocalDensity.current
//
//    Canvas(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        val tilePx = with(density) { tileSize.toPx() }
//
//        val centerX = size.width / 2
//        val centerY = size.height / 2
//
//        val radius = tilePx * 2.5f
//
//        drawRect(
//            brush = Brush.radialGradient(
//                colors = listOf(
//                    Color.Transparent,
//                    Color.Black.copy(alpha = 0.25f),
//                    Color.Black.copy(alpha = 0.6f),
//                    Color.Black.copy(alpha = 0.8f)
//                ),
//                center = Offset(centerX, centerY),
//                radius = radius
//            ),
//            size = size
//        )
//    }
//}