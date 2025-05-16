package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import android.util.Log
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.OnlineQuizDesc
import com.LambdaProject.MathArt.ui.Pages.Profile.formatDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

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

    }

    quizResult?.let { result ->
        val totalQuestions = result.answers.size
        val answeredQuestions = result.answers.count { it.selectedAnswers.isNotEmpty() ||!it.userTextAnswer.isNullOrBlank() }
        val correctAnswers = result.answers.count { it.isCorrect }
        val totalTimeSeconds = result.answers.sumOf { it.timeTaken }
        val basePoints = result.totalBasePoints
        val bonusPoints = result.totalTimeBonus + result.totalStreakBonus
        val totalPoints = result.totalPoints

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Kuis Selesai",
                tint = Color(0xFFFFD700),
                modifier = Modifier
                    .size(80.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Color(0xFFF9F5FF), CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kuis Selesai!",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                fontFamily = interFontFamily
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatsCard(
                title = "Statistik Kuis",
                group1 = listOf(
                    "Soal Terjawab" to "$answeredQuestions / $totalQuestions",
                    "Jawaban Benar" to "$correctAnswers",
                    "Durasi" to formatDuration(totalTimeSeconds)
                ),
                group2 = listOf(
                    "Base Points" to "$basePoints",
                    "Bonus Points" to "$bonusPoints",
                    "Total Points" to "$totalPoints"
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (rewardStatus.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                .padding(8.dp)
                        ) {
                            SpinningSunBehindCoin(
                                coinResId = R.drawable.ic_poin,
                                modifier = Modifier,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Hadiah Koin",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                AnimatedCoin(value = rewardCoins.toString())
                                Text(
                                    text = "Karena kamu telah meneyelesaikan kuis untuk pertama kalinya",
                                    fontFamily = interFontFamily,
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.DarkGray,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        isLoading = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000)
                            onClickLeaderboard()
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
                ) {
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .widthIn(min = 120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 4.dp
                            )
                        } else {
                            Text(
                                "Leaderboard",
                                color = Color.White,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        isLoadingLobby = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000)
                            onClickHome()
                            isLoadingLobby = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .widthIn(min = 120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingLobby) {
                            CircularProgressIndicator(
                                color = Color.DarkGray,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 4.dp
                            )
                        } else {
                            Text(
                                "Quiz Lobby",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SpinningSunBehindCoin(
    modifier: Modifier,
    coinResId: Int
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = rotation
            }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2.2f
            val petalCount = 12
            val angleStep = 360f / petalCount

            repeat(petalCount) { i ->
                val angle = i * angleStep  // angle dalam DERAJAT, bukan radian!
                withTransform({
                    rotate(angle, pivot = center)
                }) {
                    drawPath(
                        path = Path().apply {
                            moveTo(center.x, center.y)
                            lineTo(center.x + radius * 0.1f, center.y - radius)
                            lineTo(center.x - radius * 0.1f, center.y - radius)
                            close()
                        },
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Yellow,
                                Color.Yellow.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            startY = center.y - radius,
                            endY = center.y
                        )
                    )
                }
            }
        }
        // Gambar koin/logo di tengah
        Image(
            painter = painterResource(coinResId),
            contentDescription = "Koin",
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    group1: List<Pair<String, String>>,
    group2: List<Pair<String, String>>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                "durasi" -> Icons.Default.Timer
                                else -> Icons.Default.Info
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF92D0FF)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = interFontFamily
                            )
                        }

                        Text(
                            value,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            fontFamily = interFontFamily
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                group2.forEachIndexed { index, (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, fontFamily = interFontFamily)

                        if (index == 1) {
                            AnimatedPointsWithCounter(value) // Bonus Points menggunakan Animated Counter
                        } else {
                            AnimatedPoints(value) // Base Points dan Total Points
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
            animationSpec = tween(durationMillis = 2500, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = "+${animatedValue.value.toInt()}",
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4CAF50),
            fontFamily = interFontFamily
        )
    )
}

@Composable
fun AnimatedPoints(value: String) {
    val targetValue = value.toIntOrNull() ?: 0
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 2500, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = animatedValue.value.toInt().toString(),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        fontFamily = interFontFamily
    )
}

@Composable
fun AnimatedCoin(value: String) {
    val targetValue = value.toIntOrNull() ?: 0
    val animatedValue = remember { Animatable(0f) } // Menggunakan Animatable untuk nilai float

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 2500, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = "+ ${animatedValue.value.toInt()} Koin",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
        fontSize = 21.sp,
        color = Color(0xFF66BB6A),
        fontFamily = interFontFamily
    )
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return buildString {
        if (minutes > 0) {
            append("$minutes menit ")
        }
        append("$remainingSeconds detik")
    }
}