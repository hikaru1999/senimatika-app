package com.LambdaProject.MathArt

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.DataAchievements
import com.LambdaProject.MathArt.data.model.AchievementItem

@Composable
fun UniversalSnackbar(
    achievementItem: AchievementItem? = null,
    isNetworkLost: Boolean = false,
    isSuccess: Boolean = false,
    remainingSeconds: Int = 0,
    visible: Boolean
) {
    var lastAchievement by remember { mutableStateOf<AchievementItem?>(null) }
    val isAchievementMode = achievementItem != null || (lastAchievement != null && !isNetworkLost && !isSuccess)

    LaunchedEffect(achievementItem) {
        if (achievementItem != null) {
            lastAchievement = achievementItem
        }
    }

    val backgroundBrush = when {
        isNetworkLost -> Brush.horizontalGradient(
            listOf(Color(0xFF2D1215), Color(0xFF3F171D))
        )
        isSuccess -> Brush.horizontalGradient(
            listOf(Color(0xFF0D2818), Color(0xFF133F25))
        )
        isAchievementMode -> Brush.horizontalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
        else -> Brush.horizontalGradient(
            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        )
    }

    val borderColor = when {
        isNetworkLost -> Color(0xFFEF5350).copy(alpha = 0.55f)
        isSuccess -> Color(0xFF4ADE80).copy(alpha = 0.5f)
        isAchievementMode -> Color(0xFFF59E0B).copy(alpha = 0.55f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    val shadowColor = when {
        isNetworkLost -> Color(0xFFE53935).copy(alpha = 0.25f)
        isSuccess -> Color(0xFF2E7D32).copy(alpha = 0.25f)
        isAchievementMode -> Color(0xFFF59E0B).copy(alpha = 0.25f)
        else -> Color.Black.copy(alpha = 0.3f)
    }

    val dynamicResId = remember(lastAchievement) {
        lastAchievement?.let { ach ->
            ach.imageRes.takeIf { it != 0 && it != R.drawable.obj_sack }
                ?: DataAchievements.find { it.name.equals(ach.name, ignoreCase = true) }?.imageRes
                ?: R.drawable.ic_trophy
        } ?: R.drawable.ic_trophy
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        ) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .zIndex(1000f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = shadowColor,
                        ambientColor = shadowColor
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, borderColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
                        .padding(horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge Icon Container (Fixed 46dp x 46dp)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        isNetworkLost -> Color(0xFFEF5350).copy(alpha = 0.18f)
                                        isSuccess -> Color(0xFF4ADE80).copy(alpha = 0.18f)
                                        isAchievementMode -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                                        else -> Color.White.copy(alpha = 0.1f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isNetworkLost -> Color(0xFFEF5350).copy(alpha = 0.4f)
                                        isSuccess -> Color(0xFF4ADE80).copy(alpha = 0.4f)
                                        isAchievementMode -> Color(0xFFF59E0B).copy(alpha = 0.4f)
                                        else -> Color.Transparent
                                    },
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isNetworkLost -> {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                isSuccess -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                isAchievementMode -> {
                                    Image(
                                        painter = painterResource(id = dynamicResId),
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        // Center Text Column (Fixed 2-line layout for all modes)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            when {
                                isNetworkLost -> {
                                    Text(
                                        text = "KONEKSI TERPUTUS",
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = Color(0xFFFF8A80),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Menghubungkan kembali...",
                                        fontSize = 13.5.sp,
                                        lineHeight = 16.sp,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                isSuccess -> {
                                    Text(
                                        text = "STATUS JARINGAN",
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = Color(0xFF86EFAC),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Berhasil terhubung kembali!",
                                        fontSize = 13.5.sp,
                                        lineHeight = 16.sp,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                isAchievementMode -> {
                                    Text(
                                        text = "ACHIEVEMENT DIBUKA!",
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = Color(0xFFFBBF24),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = lastAchievement?.name ?: "",
                                        fontSize = 14.sp,
                                        lineHeight = 17.sp,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Trailing Element (Countdown badge only for Network Lost)
                        if (isNetworkLost) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFEF5350).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${remainingSeconds}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFFFF8A80),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}