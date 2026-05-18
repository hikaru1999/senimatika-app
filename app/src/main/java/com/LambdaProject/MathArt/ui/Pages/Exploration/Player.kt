package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.LambdaProject.MathArt.R

@Composable
fun Player(
    tileSize: Dp,
    modifier: Modifier = Modifier,
    dx: Int = 0,
    dy: Int = 0
) {

    val playerSize = tileSize * 1.5f

    val playerSprite = when {
        dy < 0 -> R.drawable.player_forward
        dy > 0 -> R.drawable.player_backward
        dx > 0 -> R.drawable.player_right
        dx < 0 -> R.drawable.player_left
        else -> R.drawable.player_backward
    }

    Image(
        painter = painterResource(playerSprite),
        contentDescription = null,
        modifier = modifier
            .size(playerSize)
    )
}