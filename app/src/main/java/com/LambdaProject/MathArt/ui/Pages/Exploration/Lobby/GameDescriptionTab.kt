package com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

import com.LambdaProject.MathArt.data.model.Mission
import com.LambdaProject.MathArt.data.model.MissionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDescriptionTab(
    name: String,
    description: String,
    levelType: String,
    hasFog: Boolean,
    hasNight: Boolean,
    hasCombined: Boolean,
    playerHp: Float,
    missions: List<Mission> = emptyList(),
    onBack: () -> Unit,
    onStartExploration: () -> Unit,
    isResume: Boolean = false
) {
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. Hero Map Banner Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0xFF1A237E).copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_explore),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark Gradient Scrim for readable text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (levelType == "TUTORIAL") Color(0xFFFF9800) else Color(0xFF3F51B5),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 2.dp
                        ) {
                            Text(
                                text = if (levelType == "TUTORIAL") "TUTORIAL" else "EKSPLORASI",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.8.sp,
                                fontFamily = interFontFamily
                            )
                        }

                        if (isResume) {
                            Surface(
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SESI AKTIF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Title & Subtitle
                    Column {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            fontFamily = interFontFamily,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFD600)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wilayah Eksplorasi Senimatika",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Key Attributes / Stats Grid ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // HP Stat
            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = Icons.Default.Favorite,
                iconColor = Color(0xFFEF4444),
                bgColor = Color(0xFFFEF2F2),
                borderColor = Color(0xFFFEE2E2),
                title = "${playerHp.toInt()} HP",
                subtitle = "Hitpoint"
            )

            // Environment / Visibility Stat
            val envInfo = when {
                hasCombined -> Triple(Icons.Default.Cloud, "Kabut + Malam", Color(0xFFFF9800))
                hasNight -> Triple(Icons.Default.Nightlight, "Malam", Color(0xFF7E57C2))
                hasFog -> Triple(Icons.Default.Cloud, "Berkabut", Color(0xFF0288D1))
                else -> Triple(Icons.Default.WbSunny, "Normal", Color(0xFF10B981))
            }
            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = envInfo.first,
                iconColor = envInfo.third,
                bgColor = envInfo.third.copy(alpha = 0.1f),
                borderColor = envInfo.third.copy(alpha = 0.25f),
                title = envInfo.second,
                subtitle = "Kondisi Wilayah"
            )
        }

        // --- 3. Description Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0xFF1A237E).copy(alpha = 0.08f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Deskripsi Wilayah",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = interFontFamily,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = description.ifBlank { "Tidak ada deskripsi tersedia untuk wilayah ini." },
                    fontFamily = interFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        // --- 4. Dynamic Missions Section (Jika diset dari Server / Map Editor) ---
        if (missions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFF1A237E).copy(alpha = 0.08f)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TrackChanges,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Target Misi & Bonus",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = interFontFamily,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Selesaikan misi untuk mendapatkan koin dan EXP tambahan",
                                fontSize = 11.sp,
                                fontFamily = interFontFamily,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    missions.forEach { mission ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mission.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        lineHeight = 17.sp,
                                        fontFamily = interFontFamily,
                                        color = Color(0xFF1E293B)
                                    )
                                    if (mission.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = mission.description,
                                            fontSize = 11.5.sp,
                                            lineHeight = 15.sp,
                                            fontFamily = interFontFamily,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                if (mission.bonusCoins > 0) {
                                    Surface(
                                        color = Color(0xFFFFFBEB),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_coin),
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "+${mission.bonusCoins}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp,
                                                fontFamily = interFontFamily,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 5. Main Action Button ---
        Button(
            onClick = {
                if (!isLoading) {
                    if (isResume) {
                        isLoading = true
                    }
                    onStartExploration()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = Color(0xFF1976D2).copy(alpha = 0.4f)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2),
                disabledContainerColor = Color(0xFF94A3B8)
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isResume) "LANJUTKAN PETUALANGAN" else "MULAI PETUALANGAN",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    fontFamily = interFontFamily,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    borderColor: Color,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = bgColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subtitle,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    fontFamily = interFontFamily,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    fontFamily = interFontFamily,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

