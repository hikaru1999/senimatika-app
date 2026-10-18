package com.LambdaProject.MathArt.ui.Pages.Profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.BottomNavigationMenu
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.ProfileViewModel
import com.LambdaProject.MathArt.data.DataMaterials
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningReportScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val username by profileViewModel.username
    val coins by profileViewModel.coins
    val studyDurationMs by profileViewModel.studyDuration // in milliseconds
    val unlockedAchievements = profileViewModel.unlockedAchievements
    val userId = profileViewModel.userId

    val onlineQuizCount by profileViewModel.onlineQuizCount
    val highestOnlineScore by profileViewModel.highestOnlineScore
    val totalOnlinePoints by profileViewModel.totalOnlinePoints
    val recentOnlineQuizzes = profileViewModel.recentOnlineQuizzes
    val standardQuizCount by profileViewModel.standardQuizCount
    val isReportLoaded by profileViewModel.isReportLoaded
    val isReportLoading by profileViewModel.isReportLoading

    val isDataLoading = !isReportLoaded && isReportLoading

    LaunchedEffect(userId) {
        if (!isReportLoaded && userId.isNotEmpty()) {
            profileViewModel.loadLearningReport()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Laporan Belajarku",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A237E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1A237E)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavigationMenu(navController) }
    ) { paddingValues ->
        if (isDataLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LearningReportSkeleton()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8F9FE))
                    .verticalScroll(rememberScrollState())
            ) {
            // Study Duration Hero Card
            StudyDurationHeroCard(studyDurationMs = studyDurationMs)

            // Statistics Grid Card
            StatsOverviewCard(
                onlineQuizCount = onlineQuizCount,
                highestOnlineScore = highestOnlineScore,
                totalOnlinePoints = totalOnlinePoints,
                standardQuizCount = standardQuizCount,
                unlockedAchievementsCount = unlockedAchievements.size,
                coins = coins
            )

            // Material Tracker Section
            Text(
                text = "Progres Materi Pembelajaran",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF1A237E),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                DataMaterials.forEach { material ->
                    MaterialTracker(userId = userId, material = material)
                }
            }

            // Recent Quiz Activities Section
            if (recentOnlineQuizzes.isNotEmpty()) {
                Text(
                    text = "Aktivitas Kuis Terakhir",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    recentOnlineQuizzes.forEachIndexed { index, quiz ->
                        val materialName = quiz.title.ifBlank {
                            DataMaterials.find { it.id.equals(quiz.id, ignoreCase = true) }?.title
                                ?: quiz.id.replace("_", " ")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_quiz_icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color(0xFF2979FF)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = materialName,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1A237E)
                                    )
                                    if (quiz.category == "Math Arena") {
                                        MathArenaBrandingText(fontSize = 11.sp)
                                    } else {
                                        Text(
                                            text = quiz.category,
                                            fontFamily = interFontFamily,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "+${quiz.points} Pts",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        if (index < recentOnlineQuizzes.size - 1) {
                            HorizontalDivider(color = Color(0xFFF5F5F5))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StudyDurationHeroCard(studyDurationMs: Long) {
    val totalSeconds = studyDurationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val durationText = when {
        hours > 0 -> "${hours}j ${minutes}m ${seconds}d"
        minutes > 0 -> "${minutes}m ${seconds}d"
        else -> "${seconds}d"
    }

    // Goal calculation (e.g. 60 minutes weekly goal)
    val weeklyGoalMs = 60 * 60 * 1000L // 60 minutes
    val progress = (studyDurationMs.toFloat() / weeklyGoalMs).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D47A1), Color(0xFF000749))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DURASI BELAJAR MINGGU INI",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = durationText,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            color = Color.White
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_explorer),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Bar towards Goal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Mingguan: 120 Menit",
                        fontFamily = interFontFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(Color(0xFFFFD600), RoundedCornerShape(4.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (progress >= 1f) "Hebat! Kamu telah melampaui target belajar mingguanmu." else "Yuk semangat lagi belajarnya untuk mencapai target target belajarmu!",
                    fontFamily = interFontFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatsOverviewCard(
    onlineQuizCount: Int,
    highestOnlineScore: Int,
    totalOnlinePoints: Int,
    standardQuizCount: Int,
    unlockedAchievementsCount: Int,
    coins: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Ringkasan Performa",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color(0xFF1A237E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // First Row of Stats
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItemBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_quiz_icon,
                    value = "$onlineQuizCount",
                    label = "Math Arena",
                    tint = Color(0xFF2979FF)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatItemBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_ss_awesome,
                    value = "$highestOnlineScore",
                    label = "Skor Terbaik",
                    tint = Color(0xFFFF9100)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Second Row of Stats
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItemBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_assessment,
                    value = "$standardQuizCount",
                    label = "Kuis Mandiri",
                    tint = Color(0xFF00E676)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatItemBox(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_explorer,
                    value = "$unlockedAchievementsCount",
                    label = "Pencapaian",
                    tint = Color(0xFFD500F9)
                )
            }
        }
    }
}

@Composable
fun StatItemBox(
    modifier: Modifier = Modifier,
    iconRes: Int,
    value: String,
    label: String,
    tint: Color
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF8F9FE),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = tint.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = tint
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF1A237E)
                )
                if (label == "Math Arena") {
                    MathArenaBrandingText(fontSize = 10.sp)
                } else {
                    Text(
                        text = label,
                        fontFamily = interFontFamily,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MathArenaBrandingText(
    fontSize: TextUnit = 11.sp,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("Math ")
            }
            withStyle(
                SpanStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2563EB),
                            Color(0xFF0284C7)
                        )
                    ),
                    fontWeight = FontWeight.ExtraBold
                )
            ) {
                append("Arena")
            }
        },
        fontFamily = interFontFamily,
        fontSize = fontSize,
        modifier = modifier
    )
}
