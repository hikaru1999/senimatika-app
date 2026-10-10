package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.GameMapInfo

@Composable
fun MapThumbnailCard(
    map: GameMapInfo,
    isLocked: Boolean,
    lockReason: String,
    isActiveSession: Boolean,
    onClick: () -> Unit
) {
    val isTutorial = map.id == "tutorial_eksplorasi" || map.name.contains("Tutorial", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp)
            .shadow(
                elevation = if (isLocked) 2.dp else if (isActiveSession) 12.dp else 8.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = if (isActiveSession) Color(0xFF10B981) else Color(0xFF1E3A8A).copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = if (isActiveSession) 2.dp else if (isLocked) 1.dp else 1.dp,
                color = when {
                    isActiveSession -> Color(0xFF10B981)
                    isLocked -> Color(0xFF334155).copy(alpha = 0.3f)
                    else -> Color(0xFFE2E8F0)
                },
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(enabled = !isLocked) { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Thumbnail Image
            Image(
                painter = painterResource(id = R.drawable.img_explore),
                contentDescription = map.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = if (isLocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.1f) }) else null
            )

            // 2. Multi-layer Gradient Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = if (isLocked) 0.75f else 0.45f),
                                0.4f to Color.Black.copy(alpha = if (isLocked) 0.65f else 0.2f),
                                1.0f to Color.Black.copy(alpha = if (isLocked) 0.90f else 0.85f)
                            )
                        )
                    )
            )

            // 3. Top Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category / Tag Badge (Hanya untuk Tutorial atau Terkunci)
                if (isTutorial || isLocked) {
                    Surface(
                        color = when {
                            isLocked -> Color(0xFF1E293B).copy(alpha = 0.85f)
                            else -> Color(0xFF3B82F6).copy(alpha = 0.85f)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isLocked) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = if (isTutorial) "⭐ TUTORIAL" else "🔒 TERKUNCI",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = if (isLocked) Color(0xFF94A3B8) else Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Active Session or Lock Badge on the right
                if (isActiveSession) {
                    Surface(
                        color = Color(0xFF059669).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF34D399))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF6EE7B7), CircleShape)
                            )
                            Text(
                                text = "SESI AKTIF",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = interFontFamily,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // 4. Bottom Content Info
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = map.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        fontFamily = interFontFamily,
                        color = if (isLocked) Color(0xFFCBD5E1) else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else if (isActiveSession) Icons.Default.PlayCircle else Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = when {
                                isLocked -> Color(0xFFEF4444)
                                isActiveSession -> Color(0xFF34D399)
                                else -> Color(0xFFFBBF24)
                            }
                        )
                        Text(
                            text = when {
                                isLocked -> lockReason
                                isActiveSession -> "Lanjutkan petualangan aktif"
                                else -> "Ketuk untuk eksplorasi wilayah"
                            },
                            fontSize = 11.5.sp,
                            color = when {
                                isLocked -> Color(0xFFFCA5A5)
                                isActiveSession -> Color(0xFF6EE7B7)
                                else -> Color(0xFFE2E8F0)
                            },
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Action Pill / Icon Button
                if (!isLocked) {
                    Surface(
                        shape = CircleShape,
                        color = if (isActiveSession) Color(0xFF10B981) else Color.White.copy(alpha = 0.25f),
                        border = BorderStroke(
                            1.dp,
                            if (isActiveSession) Color(0xFF34D399) else Color.White.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isActiveSession) Icons.Default.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Buka Wilayah",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E293B).copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Terkunci",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}