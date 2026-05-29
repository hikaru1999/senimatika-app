package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF3949AB), Color(0xFF1A237E))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .clip(RoundedCornerShape(28.dp))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.ic_clock_yellow),
                            contentDescription = "Durasi Belajar",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = "Durasi Belajarmu Minggu Ini",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = formatDuration(studyDuration),
                        fontFamily = interFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF81C784), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terus tingkatkan prestasimu!",
                            fontFamily = interFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(title = "Materi Aktif", count = activeSessions.size)
        Spacer(modifier = Modifier.height(16.dp))

        if (activeSessions.isEmpty()) {
            EmptyStateCard("Belum ada materi aktif")
        } else {
            activeSessions.forEach { material ->
                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                    MaterialTracker(userId, material = material)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AchievementSection(unlockedAchievements, navController)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lainnya",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = interFontFamily,
            color = Color(0xFF1A237E)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        ProfileActionButton(
            label = "Beri Penilaian Aplikasi",
            iconRes = R.drawable.ic_assessment,
            onClick = { navController.navigate("validator_screen") }
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        ProfileActionButton(
            label = "Tentang Senimatika",
            iconRes = R.drawable.ic_info,
            onClick = { navController.navigate("Senimatika_screen") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF5252).copy(alpha = 0.1f),
                contentColor = Color(0xFFFF5252)
            ),
            onClick = { onLogoutClicked() },
            enabled = !isLoggingOut,
            shape = RoundedCornerShape(16.dp),
            elevation = null
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    color = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp),
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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Keluar dari Akun",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileActionButton(label: String, iconRes: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFF5F5F5),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1A237E)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                color = Color.DarkGray,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.ic_play),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = 0f },
                tint = Color.LightGray
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
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            fontFamily = interFontFamily,
            color = Color(0xFF1A237E)
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0xFF1976D2),
                shape = CircleShape
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(text: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.LightGray,
                fontFamily = interFontFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp
            )
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
        Text(
            text = "Lencanaku",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            fontFamily = interFontFamily,
            color = Color(0xFF1A237E)
        )
        Text(
            text = "Lihat Semua",
            fontSize = 13.sp,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            modifier = Modifier
                .clickable { navController.navigate("achievement") }
                .padding(4.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(64.dp)
                .shadow(if (isUnlocked) 4.dp else 0.dp, CircleShape),
            color = if (isUnlocked) Color.White else Color(0xFFF5F5F5),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = achievement.imageRes),
                    contentDescription = achievement.name,
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer { alpha = if (isUnlocked) 1f else 0.3f },
                    colorFilter = if (isUnlocked) null else ColorFilter.tint(Color.Gray, BlendMode.SrcIn),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = achievement.name,
            fontFamily = interFontFamily,
            textAlign = TextAlign.Center,
            fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = if (isUnlocked) Color(0xFF1A237E) else Color.Gray,
            maxLines = 1
        )
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
