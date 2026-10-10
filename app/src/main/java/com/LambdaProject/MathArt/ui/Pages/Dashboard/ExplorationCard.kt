package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.copy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun ExplorationCard(
    isLocked: Boolean = false,
    requiredLevel: Int = 2,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() }
            .shadow(
                12.dp,
                RoundedCornerShape(24.dp),
                spotColor = if (isLocked) Color(0xFF64748B) else Color(0xFF1976D2)
            ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = com.LambdaProject.MathArt.R.drawable.img_explore),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                if (isLocked) Color.Black.copy(alpha = 0.5f) else Color.Transparent,
                                Color.Black.copy(alpha = if (isLocked) 0.88f else 0.75f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF0F172A).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = (-0.2).sp
                                        )
                                    ) {
                                        append("Math")
                                    }
                                    withStyle(
                                        SpanStyle(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF34D399), // Emerald Green
                                                    Color(0xFF2DD4BF), // Teal
                                                    Color(0xFFFBBF24)  // Golden Amber
                                                )
                                            ),
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = (-0.2).sp
                                        )
                                    ) {
                                        append("venture")
                                    }
                                },
                                fontSize = 11.5.sp,
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.5.dp)
                            )
                        }

                        if (isLocked) {
                            Surface(
                                color = Color(0xFFDC2626).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
//                                        text = "Buka di Level $requiredLevel",
                                        text = "Terkunci",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "JELAJAHI NUMERIA",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        fontFamily = interFontFamily,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isLocked) "Capai Level $requiredLevel (Apprentice) untuk membuka mode ini!" else "Kalahkan boss dan temukan harta karun matematika!",
                        color = if (isLocked) Color(0xFFFCA5A5) else Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontFamily = interFontFamily
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isLocked) Color(0xFF334155) else Color.White,
                    modifier = Modifier.size(40.dp),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = if (isLocked) androidx.compose.material.icons.Icons.Default.Lock else androidx.compose.material.icons.Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp),
                        tint = if (isLocked) Color(0xFF94A3B8) else Color(0xFF1976D2)
                    )
                }
            }
        }
    }
}