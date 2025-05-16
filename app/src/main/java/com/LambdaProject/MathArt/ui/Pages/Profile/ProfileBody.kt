package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.LambdaProject.MathArt.Data.sampleAchievements
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.AchievementItem
import com.LambdaProject.MathArt.model.MaterialItem
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileBody(
    navController: NavController,
    studyDuration: Long,
    activeSessions: List<MaterialItem>,
    unlockedAchievements: List<String>,
    isLoggingOut: Boolean,
    onLogoutClicked: () -> Unit
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(125.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_clock_yellow),
                        contentDescription = "Durasi Belajar",
                        modifier = Modifier
                            .size(80.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Durasi Belajar Minggu Ini",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatDuration(studyDuration),
                            fontFamily = interFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Keep it up!",
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Materi Aktif",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFontFamily
        )

        Spacer(modifier = Modifier.height(16.dp))

        activeSessions.forEach { material ->
            CompactMaterialCard(userId, material = material)
        }

        if (activeSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .border(
                        width = 0.dp,
                        brush = Brush.linearGradient(colors = listOf(Color.Gray, Color.Gray)),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada materi yang sedang dipelajari",
                    fontFamily = interFontFamily,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AchievementSection(unlockedAchievements, navController)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Lainnya",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFontFamily
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            border = BorderStroke(1.dp, Color.Black),
            onClick = { navController.navigate("Senimatika_screen") },
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Button Info",
                    modifier = Modifier.height(20.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tentang Senimatika",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(0.dp),
            border = BorderStroke(1.dp, Color.Black),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC40300),
                disabledContainerColor = Color(0xFFC40300),
            ),
            onClick = { onLogoutClicked() },
            enabled = !isLoggingOut,
            shape = RoundedCornerShape(4.dp)
        ) {
            if (isLoggingOut) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 4.dp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(0.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logout_exit),
                        contentDescription = "Button Info",
                        modifier = Modifier.height(20.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Akhiri Sesi (Log Out)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: AchievementItem, isUnlocked: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(90.dp)
    ) {
        Image(
            painter = painterResource(id = achievement.imageRes),
            contentDescription = achievement.name,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .graphicsLayer { alpha = if(isUnlocked) 1f else 0.5f },
            colorFilter = if (isUnlocked) null else ColorFilter.tint(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = achievement.name,
            fontFamily = interFontFamily,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            style = TextStyle(letterSpacing = 0.5.sp),
            color = if (isUnlocked) Color.Black else Color.Gray)
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun AchievementSection(unlockedAchievements: List<String>, navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Lencanaku",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = interFontFamily
        )
        Text(
            text = "Lihat Semua",
            fontSize = 14.sp,
            fontFamily = interFontFamily,
            color = Color(0xFF4397E2),
            modifier = Modifier
                .clickable { navController.navigate("achievement") }
                .padding(4.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(sampleAchievements) { achievement ->
                AchievementCard(
                    achievement = achievement,
                    isUnlocked = achievement.name in unlockedAchievements)
            }
        }
    }
}