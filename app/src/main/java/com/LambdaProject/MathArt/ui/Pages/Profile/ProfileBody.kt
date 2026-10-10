package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.LambdaProject.MathArt.data.DataAchievements
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.AchievementItem
import com.LambdaProject.MathArt.data.model.MaterialItem

@Composable
fun ProfileBody(
    navController: NavController,
    studyDuration: Long,
    activeSessions: List<MaterialItem>,
    unlockedAchievements: List<String>,
    isLoggingOut: Boolean,
    onLogoutClicked: () -> Unit
) {
    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Modern Study Duration Hero Card
        StudyDurationProfileCard(
            studyDuration = studyDuration,
            onViewReportClicked = { navController.navigate("learning_report") }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Active Materials Section
        SectionHeader(title = "Materi Aktif", count = activeSessions.size)
        Spacer(modifier = Modifier.height(14.dp))

        if (activeSessions.isEmpty()) {
            EmptyStateCard(
                text = "Belum ada materi aktif yang dipelajari",
                subtext = "Pilih materi di Beranda untuk mulai belajar!"
            )
        } else {
            activeSessions.forEach { material ->
                Box(modifier = Modifier.padding(bottom = 10.dp)) {
                    MaterialTracker(userId, material = material)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Achievements Section
        AchievementSection(unlockedAchievements, navController)

        Spacer(modifier = Modifier.height(28.dp))

        // Menu & Other Settings Section
        Text(
            text = "Menu & Pengaturan",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFontFamily,
            color = Color(0xFF0F172A)
        )
        
        Spacer(modifier = Modifier.height(14.dp))

        // Grouped Actions Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF64748B).copy(alpha = 0.1f)),
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ProfileMenuItem(
                    label = "Laporan Belajarku",
                    subtitle = "Grafik & rangkuman aktivitas kuis",
                    iconRes = R.drawable.ic_assessment,
                    iconBgColor = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    onClick = { navController.navigate("learning_report") }
                )
                
                HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp)

                ProfileMenuItem(
                    label = "Beri Penilaian Aplikasi",
                    subtitle = "Evaluasi & masukan instrumen pembelajaran",
                    iconRes = R.drawable.ic_explorer,
                    iconBgColor = Color(0xFFFAF5FF),
                    iconTint = Color(0xFF9333EA),
                    onClick = { navController.navigate("validator_screen") }
                )

                HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp)

                ProfileMenuItem(
                    label = "Tentang Senimatika",
                    subtitle = "Informasi versi aplikasi & tim pengembang",
                    iconRes = R.drawable.ic_info,
                    iconBgColor = Color(0xFFF0FDF4),
                    iconTint = Color(0xFF16A34A),
                    onClick = { navController.navigate("Senimatika_screen") }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Logout Button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEF2F2),
                contentColor = Color(0xFFDC2626)
            ),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            onClick = { onLogoutClicked() },
            enabled = !isLoggingOut,
            shape = RoundedCornerShape(16.dp),
            elevation = null
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    color = Color(0xFFDC2626),
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout_exit),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Keluar dari Akun",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun StudyDurationProfileCard(
    studyDuration: Long,
    onViewReportClicked: () -> Unit
) {
    val weeklyGoalMs = 60 * 60 * 1000L // 60 menit
    val progress = (studyDuration.toFloat() / weeklyGoalMs).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFF1E3A8A).copy(alpha = 0.35f))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Decorative Ambient Circles
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-20).dp)
                .size(140.dp)
                .background(Color(0xFF3B82F6).copy(alpha = 0.15f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 30.dp)
                .size(100.dp)
                .background(Color(0xFF6366F1).copy(alpha = 0.1f), CircleShape)
        )

        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.ic_clock_yellow),
                            contentDescription = "Durasi Belajar",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DURASI BELAJAR MINGGU INI",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatDuration(studyDuration),
                        fontFamily = interFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progress Bar & Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target Mingguan: 120 Menit",
                    fontFamily = interFontFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color(0xFFFFD600)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFD600), Color(0xFFFFAB00))
                            ),
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View Report Action Link
            Surface(
                onClick = onViewReportClicked,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF4ADE80), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lihat Laporan Belajar",
                            fontFamily = interFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    label: String,
    subtitle: String,
    iconRes: Int,
    iconBgColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconBgColor,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = interFontFamily,
                    color = Color(0xFF64748B)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            fontFamily = interFontFamily,
            color = Color(0xFF0F172A)
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0xFFEFF6FF),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1D4ED8)
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(text: String, subtext: String? = null) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFFF8FAFC),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                color = Color(0xFF475569),
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    color = Color(0xFF94A3B8),
                    fontFamily = interFontFamily,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AchievementSection(unlockedAchievements: List<String>, navController: NavController) {
    val sortedAchievements by remember(unlockedAchievements) {
        derivedStateOf {
            DataAchievements.sortedByDescending { it.name in unlockedAchievements }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Lencanaku",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                fontFamily = interFontFamily,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0xFFFEF3C7),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Text(
                    text = "${unlockedAchievements.size}/${DataAchievements.size}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFontFamily,
                    color = Color(0xFFB45309)
                )
            }
        }

        Surface(
            onClick = { navController.navigate("achievement") },
            color = Color.Transparent
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "Lihat Semua",
                    fontSize = 12.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color(0xFF2563EB)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    LazyRow(
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(sortedAchievements) { achievement ->
            AchievementCard(
                achievement = achievement,
                isUnlocked = achievement.name in unlockedAchievements
            )
        }
    }
}

@Composable
fun AchievementCard(achievement: AchievementItem, isUnlocked: Boolean) {
    Surface(
        modifier = Modifier.width(88.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isUnlocked) Color.White else Color(0xFFF8FAFC),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) Color(0xFFE2E8F0) else Color(0xFFF1F5F9)
        ),
        shadowElevation = if (isUnlocked) 3.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                color = if (isUnlocked) Color(0xFFFFFBEB) else Color(0xFFF1F5F9),
                shape = CircleShape,
                border = BorderStroke(
                    1.5.dp,
                    if (isUnlocked) Color(0xFFFDE68A) else Color(0xFFE2E8F0)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = achievement.imageRes),
                        contentDescription = achievement.name,
                        modifier = Modifier
                            .size(34.dp)
                            .graphicsLayer { alpha = if (isUnlocked) 1f else 0.35f },
                        colorFilter = if (isUnlocked) null else ColorFilter.tint(Color(0xFF94A3B8), BlendMode.SrcIn),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.name,
                fontFamily = interFontFamily,
                textAlign = TextAlign.Center,
                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isUnlocked) Color(0xFF0F172A) else Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}

fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 -> "${hours} jam ${minutes} menit"
        else -> "${minutes} menit"
    }
}
