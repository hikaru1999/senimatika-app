package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun HUDGameButton(
    icon: Int? = null,
    vectorIcon: ImageVector? = null,
    onClick: () -> Unit,
    borderColor: Color = Color.White
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF535353), Color(0xFF1A1A1A))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(2.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        } else if (vectorIcon != null) {
            Icon(
                imageVector = vectorIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}