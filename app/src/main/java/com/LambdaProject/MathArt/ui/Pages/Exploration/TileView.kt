package com.LambdaProject.MathArt.ui.Pages.Exploration

import com.LambdaProject.MathArt.data.model.TileData
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.LambdaProject.MathArt.utils.toDrawableResId
import com.LambdaProject.MathArt.R
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

        // Object Layer
        if (tile.objectVariant.isNotEmpty()) {
            val objRes = tile.objectVariant.toDrawableResId(context)
            if (objRes != 0) {
                val isSack = tile.objectVariant.contains("sack")

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(objRes),
                        contentDescription = null,
                        modifier = if (isSack) {
                            Modifier.fillMaxSize(0.65f)
                        } else {
                            Modifier.fillMaxSize()
                        },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
