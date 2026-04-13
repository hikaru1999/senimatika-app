package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.LambdaProject.MathArt.R

@Composable
fun Player(tileSize: Dp, modifier: Modifier = Modifier) {

    val playerSize = tileSize * 0.80f

    Image(
        painter = painterResource(R.drawable.ic_player),
        contentDescription = null,
        modifier = modifier
            .size(playerSize)
    )
}