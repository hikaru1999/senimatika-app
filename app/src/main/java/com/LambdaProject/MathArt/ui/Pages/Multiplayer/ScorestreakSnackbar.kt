package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.ScorestreakState
import com.LambdaProject.MathArt.data.model.ScoreType

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
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (state != null) {
            val accentColor = when (state.type) {
                ScoreType.GOOD -> Color(0xFF8BC34A)
                ScoreType.COOL -> Color(0xFF00BCD4)
                ScoreType.AWESOME -> Color(0xFFFFB300)
                ScoreType.UPS, ScoreType.NOT_FOCUSED, ScoreType.TIME_OUT -> Color(0xFFFF7043)
            }

            val containerColor = when (state.type) {
                ScoreType.GOOD -> Color(0xFFF9FBE7)
                ScoreType.COOL -> Color(0xFFE0F7FA)
                ScoreType.AWESOME -> Color(0xFFFFF8E1)
                ScoreType.UPS, ScoreType.NOT_FOCUSED, ScoreType.TIME_OUT -> Color(0xFFFBE9E7)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp, 
                        shape = CircleShape, 
                        spotColor = accentColor
                    ),
                color = containerColor,
                shape = CircleShape,
                border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, accentColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = state.imageRes),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF1A237E),
                            fontFamily = interFontFamily,
                            letterSpacing = 0.2.sp
                        )

                        Text(
                            text = state.subtitle,
                            color = Color.DarkGray.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = interFontFamily,
                            lineHeight = 14.sp
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.5f),
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(accentColor, CircleShape)
                                    .shadow(6.dp, CircleShape, spotColor = accentColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
