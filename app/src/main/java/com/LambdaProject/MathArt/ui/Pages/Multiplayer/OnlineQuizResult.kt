package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.OnlineQuizDesc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnlineQuizResult(
    onClickLeaderboard: () -> Unit,
    onClickHome: () -> Unit,
    materialId: String,
    userId: String,
    quizList: List<OnlineQuizDesc>,
    viewModel: OnlineQuizViewModel = hiltViewModel()
) {
    val quizResult by viewModel.quizResult.collectAsState()
    val rewardStatus by viewModel.rewardStatus.observeAsState("")
    val rewardCoins by viewModel.rewardCoins.observeAsState(0)

    var isLoading by remember { mutableStateOf(false) }
    var isLoadingLobby by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchQuizResult(userId, materialId)
        viewModel.rewardIfFirstTime(userId, materialId, quizList)
    }

    BackHandler(enabled = true) {
        // ignored
    }

    quizResult?.let { result ->
        val totalQuestions = result.answers.size
        val answeredQuestions = result.answers.count { it.selectedAnswers.isNotEmpty() || !it.userTextAnswer.isNullOrBlank() }
        val correctAnswers = result.answers.count { it.isCorrect }
        val totalTimeSeconds = result.answers.sumOf { it.timeTaken }
        val basePoints = result.totalBasePoints
        val bonusPoints = result.totalTimeBonus + result.totalStreakBonus
        val totalPoints = result.totalPoints

        Scaffold(
            containerColor = Color(0xFFF8F9FE)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(24.dp, CircleShape, spotColor = Color(0xFFFFD700)),
                        color = Color.White,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Kuis Selesai!",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color(0xFF1A237E)
                )

                Text(
                    text = "Luar biasa! Kamu telah menyelesaikan tantangan ini.",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                StatsCard(
                    title = "Statistik Performa",
                    group1 = listOf(
                        "Soal Terjawab" to "$answeredQuestions / $totalQuestions",
                        "Jawaban Benar" to "$correctAnswers",
                        "Total Durasi" to formatDuration(totalTimeSeconds)
                    ),
                    group2 = listOf(
                        "Poin Dasar" to "$basePoints",
                        "Bonus (Waktu & Streak)" to "$bonusPoints",
                        "Skor Akhir" to "$totalPoints"
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (rewardStatus.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            SpinningSunBehindCoin(
                                coinResId = R.drawable.ic_poin,
                                modifier = Modifier.size(80.dp),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hadiah Pertama Kali!",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A237E)
                                )
                                AnimatedCoin(value = rewardCoins.toString())
                                Text(
                                    text = "Selamat! Kamu mendapatkan koin tambahan.",
                                    fontFamily = interFontFamily,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            isLoading = true
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(800)
                                onClickLeaderboard()
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("LIHAT LEADERBOARD", fontWeight = FontWeight.Black, fontFamily = interFontFamily, letterSpacing = 0.5.sp)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isLoadingLobby = true
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(800)
                                onClickHome()
                                isLoadingLobby = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFEEEEEE))
                    ) {
                        if (isLoadingLobby) {
                            CircularProgressIndicator(color = Color(0xFF1976D2), modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("KEMBALI KE LOBBY", color = Color.Gray, fontWeight = FontWeight.Black, fontFamily = interFontFamily, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1976D2))
        }
    }
}

@Composable
fun SpinningSunBehindCoin(
    modifier: Modifier = Modifier,
    coinResId: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coinSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation }) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.2f
            val petalCount = 12
            val angleStep = 360f / petalCount

            repeat(petalCount) { i ->
                val angle = i * angleStep
                withTransform({
                    rotate(angle, pivot = center)
                }) {
                    drawPath(
                        path = Path().apply {
                            moveTo(center.x, center.y)
                            lineTo(center.x + radius * 0.12f, center.y - radius)
                            lineTo(center.x - radius * 0.12f, center.y - radius)
                            close()
                        },
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFD54F),
                                Color(0xFFFFD54F).copy(alpha = 0.1f)
                            ),
                            startY = center.y - radius,
                            endY = center.y
                        )
                    )
                }
            }
        }
        Image(
            painter = painterResource(coinResId),
            contentDescription = "Koin",
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    group1: List<Pair<String, String>>,
    group2: List<Pair<String, String>>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = Color(0xFF1A237E)
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                group1.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (label.lowercase()) {
                                "soal terjawab" -> Icons.Default.Description
                                "jawaban benar" -> Icons.Default.CheckCircle
                                "total durasi" -> Icons.Default.Timer
                                else -> Icons.Default.Info
                            }
                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF1976D2)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                label,
                                fontSize = 13.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        Text(
                            value,
                            fontSize = 14.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A237E)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), thickness = 1.dp, color = Color(0xFFF5F5F5))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                group2.forEachIndexed { index, (label, value) ->
                    val isTotal = index == group2.lastIndex
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            label,
                            fontSize = if (isTotal) 15.sp else 13.sp,
                            fontFamily = interFontFamily,
                            fontWeight = if (isTotal) FontWeight.Black else FontWeight.Bold,
                            color = if (isTotal) Color(0xFF1A237E) else Color.Gray
                        )

                        if (label.contains("Bonus", ignoreCase = true)) {
                            AnimatedPointsWithCounter(value)
                        } else {
                            AnimatedPoints(value, isTotal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedPointsWithCounter(value: String) {
    val targetValue = value.toIntOrNull() ?: 0
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = "+${animatedValue.value.toInt()}",
        fontSize = 14.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF66BB6A),
        fontFamily = interFontFamily
    )
}

@Composable
fun AnimatedPoints(value: String, isTotal: Boolean = false) {
    val targetValue = value.toIntOrNull() ?: 0
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = animatedValue.value.toInt().toString(),
        fontSize = if (isTotal) 20.sp else 14.sp,
        fontWeight = FontWeight.Black,
        fontFamily = interFontFamily,
        color = if (isTotal) Color(0xFF1976D2) else Color(0xFF1A237E)
    )
}

@Composable
fun AnimatedCoin(value: String) {
    val targetValue = value.toIntOrNull() ?: 0
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = "+ ${animatedValue.value.toInt()} Koin",
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        color = Color(0xFF66BB6A),
        fontFamily = interFontFamily
    )
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return buildString {
        if (minutes > 0) {
            append("$minutes menit")
        }
        append("$remainingSeconds detik")
    }
}
