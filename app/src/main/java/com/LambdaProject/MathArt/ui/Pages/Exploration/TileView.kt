package com.LambdaProject.MathArt.ui.Pages.Exploration

import com.LambdaProject.MathArt.data.model.TileData
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.LambdaProject.MathArt.utils.toDrawableResId

/**
 * Renders an individual tile with ground and object layers.
 * 
 * Note: Fog is now handled by a global FogLayer for a more natural look.
 *
 * @param tile The data for the tile to render.
 */
@Composable
fun TileView(tile: TileData) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Base Ground Layer
        val groundRes = tile.groundVariant.toDrawableResId(context)
        if (groundRes != 0) {
            Image(
                painter = painterResource(groundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Object Layer (Trees, Houses, Chests, etc.)
        if (tile.objectVariant.isNotEmpty()) {
            val objRes = tile.objectVariant.toDrawableResId(context)
            if (objRes != 0) {
                Image(
                    painter = painterResource(objRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
