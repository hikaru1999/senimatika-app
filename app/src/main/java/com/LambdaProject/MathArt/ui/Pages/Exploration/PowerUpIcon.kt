package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PowerUpIcon(type: PowerUpType) {
    val iconRes = when (type) {
        PowerUpType.FREEZE_TIMER -> R.drawable.ic_pu_freeze
        PowerUpType.DOUBLE_COIN -> R.drawable.ic_pu_shield
        PowerUpType.REMOVE_TWO_OPTIONS -> R.drawable.ic_pu_magic
    }

    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF8B4513), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
    }
}