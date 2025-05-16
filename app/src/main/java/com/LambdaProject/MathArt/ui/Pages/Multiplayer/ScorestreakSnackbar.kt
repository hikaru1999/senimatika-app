package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.ScorestreakState
import com.LambdaProject.MathArt.model.ScoreType

@Composable
fun ScorestreakSnackbar(
    modifier: Modifier = Modifier,
    state: ScorestreakState?,
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (state != null) {
            val backgroundColor = when (state.type) {
                ScoreType.GOOD -> Brush.verticalGradient(
                    colors = listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)) // light to dark green
                )
                ScoreType.COOL -> Brush.verticalGradient(
                    colors = listOf(Color(0xFF42A5F5), Color(0xFF1565C0)) // light to deep blue
                )
                ScoreType.AWESOME -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B)) // gold to dark gold
                )
                ScoreType.UPS, ScoreType.NOT_FOCUSED -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFEF5350), Color(0xFFC62828)) // light to dark red
                )
                ScoreType.TIME_OUT -> Brush.verticalGradient(
                    colors = listOf(Color(0xFFEF5350), Color(0xFFC62828))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = backgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = state.imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = state.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            color = Color.White,
                            fontFamily = interFontFamily,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = state.subtitle,
                            color = Color.White,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 14.sp
                            ),
                        )
                    }
                }
            }
        }
    }
}
