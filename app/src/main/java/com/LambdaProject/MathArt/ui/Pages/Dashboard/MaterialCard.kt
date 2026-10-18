package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.data.kelasMap

@Composable
fun MaterialCard(
    modifier: Modifier = Modifier,
    material: MaterialItem,
    isActive: Boolean,
    aspectRatio: Float = 16f / 9f,
    onClickLearn: (MaterialItem) -> Unit
) {
    val fullClassLevel = remember(material.classLevel) {
        val jenjang = kelasMap.entries.find { it.value.contains(material.classLevel) }?.key ?: ""
        if (jenjang.isNotEmpty()) "${material.classLevel} $jenjang" else "${material.classLevel}"
    }

    val context = LocalContext.current
    val fallbackRes = remember(material.imageRes, material.id) {
        if (material.imageRes != 0) material.imageRes
        else if (material.id == "transformasi_geometri") com.LambdaProject.MathArt.R.drawable.img_geo
        else com.LambdaProject.MathArt.R.drawable.img_kain_besurek
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
            // --- 1. Image Thumbnail with Dynamic Aspect Ratio (16:9 or 4:3) & SubcomposeAsyncImage ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                val cleanCoverUrl = material.coverUrl?.trim()
                if (!cleanCoverUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(cleanCoverUrl)
                            .crossfade(true)
                            .listener(
                                onError = { _, result ->
                                    Log.e("MaterialCard", "Gagal memuat coverUrl '$cleanCoverUrl': ${result.throwable.message}")
                                }
                            )
                            .build(),
                        contentDescription = material.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color(0xFF2563EB),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        },
                        error = {
                            Image(
                                painter = painterResource(id = fallbackRes),
                                contentDescription = material.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )
                } else {
                    Image(
                        painter = painterResource(id = fallbackRes),
                        contentDescription = material.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

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

                // Floating Class & Highlight Badges (Top-Start)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Floating Class Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.94f),
                        border = BorderStroke(0.8.dp, Color(0xFFDBEAFE)),
                        shadowElevation = 2.dp
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

                    // Floating Highlight Pill (if material.isHighlight == true)
                    if (material.isHighlight) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF08A).copy(alpha = 0.96f),
                            border = BorderStroke(1.dp, Color(0xFFFACC15)),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Highlight",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFFB45309)
                                )
                                Text(
                                    text = "Highlight",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF854D0E)
                                )
                            }
                        }
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

                // EXP Badge (Bottom-Start Overlay)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.78f),
                    border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "EXP Reward",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "+${material.exp} EXP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily,
                            color = Color(0xFFFEF3C7)
                        )
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
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (material.category.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(0.6.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = material.category,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                )
                            }
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Text(
                            text = if (isActive) "Lanjutkan progres belajarmu" else "Dapatkan +${material.exp} EXP",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = interFontFamily,
                            color = if (isActive) Color(0xFF059669) else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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