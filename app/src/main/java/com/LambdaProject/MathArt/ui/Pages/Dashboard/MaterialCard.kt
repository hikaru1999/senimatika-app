package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.data.kelasMap

@Composable
fun MaterialCard(
    modifier: Modifier = Modifier,
    material: MaterialItem,
    isActive: Boolean,
    onClickLearn: (MaterialItem) -> Unit
) {
    val fullClassLevel = remember(material.classLevel) {
        val jenjang = kelasMap.entries.find { it.value.contains(material.classLevel) }?.key ?: ""
        if (jenjang.isNotEmpty()) "${material.classLevel} $jenjang" else "${material.classLevel}"
    }

    Card(
        onClick = { onClickLearn(material) },
        modifier = modifier
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF64748B).copy(alpha = 0.15f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // --- 1. Image Thumbnail with Gradient Overlay & Glassmorphism Badges ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(id = material.imageRes),
                    contentDescription = material.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle vignette gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.35f)
                                )
                            )
                        )
                )

                // Floating Class Badge (Top-Start)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    border = BorderStroke(0.8.dp, Color(0xFFDBEAFE)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = "Kelas",
                            modifier = Modifier.size(13.dp),
                            tint = Color(0xFF2563EB)
                        )
                        Text(
                            text = fullClassLevel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }

                // Active Status Badge (Top-End)
                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5).copy(alpha = 0.96f),
                        border = BorderStroke(0.8.dp, Color(0xFFA7F3D0)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Text(
                                text = "Aktif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- 2. Title & Action Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Text(
                        text = material.title,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isActive) "Lanjutkan progres belajarmu" else "Klik \"Mulai\" untuk belajar",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = interFontFamily,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // --- 3. Gradient Action Button ---
                Surface(
                    onClick = { onClickLearn(material) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = if (isActive) Color(0xFF059669).copy(alpha = 0.4f) else Color(0xFF2563EB).copy(alpha = 0.4f)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = if (isActive) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF059669), Color(0xFF10B981))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
                                    )
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = if (isActive) "Lanjut" else "Mulai",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color.White
                            )
                            Icon(
                                imageVector = if (isActive) Icons.Filled.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}