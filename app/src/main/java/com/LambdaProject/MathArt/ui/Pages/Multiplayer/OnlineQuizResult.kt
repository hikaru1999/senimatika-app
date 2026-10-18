package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    onClickRematch: () -> Unit = {},
    materialId: String,
    userId: String,
    quizList: List<OnlineQuizDesc>,
    isDuel: Boolean = false,
    botId: String = "",
    isSurrendered: Boolean = false,
    viewModel: OnlineQuizViewModel = hiltViewModel()
) {
    if (isDuel) {
        DuelQuizResult(
            onClickLeaderboard = onClickLeaderboard,
            onClickHome = onClickHome,
            onClickRematch = onClickRematch,
            materialId = materialId,
            userId = userId,
            quizList = quizList,
            botId = botId,
            isSurrendered = isSurrendered,
            viewModel = viewModel
        )
        return
    }

    val quizResult by viewModel.quizResult.collectAsState()
    val rewardStatus by viewModel.rewardStatus.observeAsState("")
    val rewardCoins by viewModel.rewardCoins.observeAsState(0)

    var isLoading by remember { mutableStateOf(false) }
    var isLoadingLobby by remember { mutableStateOf(false) }

    // Staggered sequence-in animations
    val animStep1 = remember { Animatable(0f) }
    val animStep2 = remember { Animatable(0f) }
    val animStep3 = remember { Animatable(0f) }
    val animStep4 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.fetchQuizResult(userId, materialId)
        viewModel.rewardIfFirstTime(userId, materialId, quizList)

        launch {
            animStep1.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }
        delay(150)
        launch {
            animStep2.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }
        delay(150)
        launch {
            animStep3.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }
        delay(150)
        launch {
            animStep4.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }
    }

    BackHandler(enabled = true) {
        // Prevent accidental back press
    }

    quizResult?.let { result ->
        val totalQuestions = result.answers.size
        val answeredQuestions = result.answers.count { it.selectedAnswers.isNotEmpty() || !it.userTextAnswer.isNullOrBlank() }
        val correctAnswers = result.answers.count { it.isCorrect }
        val totalTimeSeconds = result.answers.sumOf { it.timeTaken }
        val basePoints = result.totalBasePoints
        val timeBonus = result.totalTimeBonus
        val streakBonus = result.totalStreakBonus
        val totalBonusPoints = timeBonus + streakBonus
        val totalPoints = result.totalPoints
        val accuracyPercent = if (totalQuestions > 0) ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100).toInt() else 0
        val avgTimePerQuestion = if (answeredQuestions > 0) totalTimeSeconds / answeredQuestions else 0

        Scaffold(
            containerColor = Color(0xFFF8FAFC)
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFEFF6FF), // soft pale blue
                                Color(0xFFF8FAFC),
                                Color(0xFFF1F5F9)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Header Trophy & Congratulatory Badge
                    val (resultTitle, resultSubtitle) = when {
                        totalQuestions > 0 && correctAnswers == totalQuestions -> {
                            "SEMPURNA!" to "Luar biasa! Semua $totalQuestions soal berhasil kamu jawab dengan benar."
                        }
                        accuracyPercent >= 75 -> {
                            "SANGAT BAGUS!" to "Hebat! Kamu berhasil menjawab $correctAnswers dari $totalQuestions soal dengan benar."
                        }
                        accuracyPercent >= 50 -> {
                            "KERJA BAGUS!" to "Bagus! Kamu berhasil menjawab $correctAnswers dari $totalQuestions soal. Tingkatkan terus kemampuanmu!"
                        }
                        else -> {
                            "TANTANGAN SELESAI" to "Kamu menjawab $correctAnswers dari $totalQuestions soal dengan benar. Jangan patah semangat, coba lagi!"
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            scaleX = animStep1.value
                            scaleY = animStep1.value
                            alpha = animStep1.value
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Soft Ambient Sun Rays & Radial Glow
                            SpinningSunAura(modifier = Modifier.size(150.dp))

                            Surface(
                                modifier = Modifier
                                    .size(96.dp)
                                    .shadow(16.dp, CircleShape, spotColor = Color(0xFFF59E0B).copy(alpha = 0.5f)),
                                color = Color.White,
                                shape = CircleShape,
                                border = BorderStroke(3.dp, Brush.sweepGradient(listOf(Color(0xFFF59E0B), Color(0xFFFCD34D), Color(0xFFF59E0B))))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_trophy),
                                        contentDescription = "Trophy",
                                        modifier = Modifier.size(54.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = resultTitle,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = Color(0xFF0F172A),
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = resultSubtitle,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Hero Total Score Card
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF2563EB).copy(alpha = 0.35f))
                            .graphicsLayer {
                                alpha = animStep2.value
                                translationY = (1f - animStep2.value) * 40f
                            },
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF1E3A8A), // Deep navy blue
                                            Color(0xFF2563EB), // Vibrant blue
                                            Color(0xFF4F46E5)  // Indigo
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(1000f, 1000f)
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(100.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFDE047),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "TOTAL SKOR AKHIR",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFontFamily,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                AnimatedScoreCounter(
                                    targetValue = totalPoints,
                                    fontSize = 42.sp,
                                    fontColor = Color.White
                                )

                                Spacer(Modifier.height(16.dp))

                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.15f),
                                    thickness = 1.dp
                                )

                                Spacer(Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ScoreMiniChip(
                                        label = "Poin Dasar",
                                        value = "$basePoints",
                                        color = Color(0xFF93C5FD)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(28.dp)
                                            .background(Color.White.copy(alpha = 0.2f))
                                    )
                                    ScoreMiniChip(
                                        label = "Bonus Waktu",
                                        value = "+$timeBonus",
                                        color = Color(0xFF6EE7B7)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(28.dp)
                                            .background(Color.White.copy(alpha = 0.2f))
                                    )
                                    ScoreMiniChip(
                                        label = "Bonus Streak",
                                        value = "+$streakBonus",
                                        color = Color(0xFFFDE047)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Performance Metrics Grid (2x2)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = animStep3.value
                                translationY = (1f - animStep3.value) * 40f
                            },
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                icon = Icons.Default.CheckCircle,
                                iconTint = Color(0xFF10B981),
                                iconBg = Color(0xFFD1FAE5),
                                label = "Jawaban Benar",
                                value = "$correctAnswers / $totalQuestions",
                                subtext = "$accuracyPercent% Akurasi",
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                icon = Icons.Default.Timer,
                                iconTint = Color(0xFFF59E0B),
                                iconBg = Color(0xFFFEF3C7),
                                label = "Durasi Pengerjaan",
                                value = formatDuration(totalTimeSeconds),
                                subtext = "rerata: $avgTimePerQuestion dtk/soal",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Detailed Points Breakdown Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = animStep3.value
                                translationY = (1f - animStep3.value) * 40f
                            }
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Rincian Perolehan Poin",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            BreakdownRow(
                                icon = Icons.Default.Star,
                                iconTint = Color(0xFF3B82F6),
                                label = "Base Point Soal",
                                value = "$basePoints",
                                valueColor = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            BreakdownRow(
                                icon = Icons.Default.Bolt,
                                iconTint = Color(0xFF10B981),
                                label = "Bonus Kecepatan Waktu",
                                value = "+$timeBonus",
                                valueColor = Color(0xFF059669)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            BreakdownRow(
                                icon = Icons.Default.LocalFireDepartment,
                                iconTint = Color(0xFFF59E0B),
                                label = "Bonus Jawaban Beruntun (Streak)",
                                value = "+$streakBonus",
                                valueColor = Color(0xFFD97706)
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = Color(0xFFF1F5F9)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Bonus",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "+$totalBonusPoints Poin",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4b. EXP Progression Card
                    val expBreakdown by viewModel.quizExpBreakdown.collectAsState()
                    if (expBreakdown != null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = animStep3.value
                                    translationY = (1f - animStep3.value) * 40f
                                }
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF7C3AED).copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = Color(0xFF7C3AED),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Experience (EXP) Diperoleh",
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Progres level Senimatika",
                                            fontFamily = interFontFamily,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = Color(0xFF7C3AED)
                                    ) {
                                        Text(
                                            text = "+${expBreakdown?.finalExp ?: 0} EXP",
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                Spacer(Modifier.height(12.dp))

                                BreakdownRow(
                                    icon = Icons.Default.Star,
                                    iconTint = Color(0xFF8B5CF6),
                                    label = "Base EXP Kuis",
                                    value = "${expBreakdown?.baseExp ?: 100} EXP",
                                    valueColor = Color(0xFF1E293B)
                                )
                                Spacer(Modifier.height(8.dp))
                                BreakdownRow(
                                    icon = Icons.Default.Bolt,
                                    iconTint = Color(0xFF10B981),
                                    label = "Bonus Performa (Akurasi & Streak)",
                                    value = "+${expBreakdown?.bonusExp ?: 0} EXP",
                                    valueColor = Color(0xFF059669)
                                )
                                Spacer(Modifier.height(8.dp))
                                BreakdownRow(
                                    icon = Icons.Default.Timer,
                                    iconTint = Color(0xFFF59E0B),
                                    label = "Multiplier Sesi (${expBreakdown?.sessionCount ?: 1}x main)",
                                    value = "${((expBreakdown?.repeatMultiplier ?: 1.0f) * 100).toInt()}%",
                                    valueColor = Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. First-Time Reward Card (if applicable)
                    if (rewardStatus.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.3f))
                                .graphicsLayer {
                                    alpha = animStep4.value
                                    translationY = (1f - animStep4.value) * 40f
                                },
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFDE68A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFFFFBEB),
                                                Color(0xFFFEF3C7)
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SpinningSunBehindCoin(
                                        coinResId = R.drawable.ic_poin,
                                        modifier = Modifier.size(68.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = Color(0xFFF59E0B),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "HADIAH PERTAMA KALI",
                                                fontFamily = interFontFamily,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.5.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        AnimatedCoin(value = rewardCoins.toString())
                                        Text(
                                            text = "Koin telah ditambahkan ke akunmu!",
                                            fontFamily = interFontFamily,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 6. Action Buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = animStep4.value
                                translationY = (1f - animStep4.value) * 40f
                            },
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val currentQuiz = quizList.find { it.id == materialId }
                        val isLearningMode = currentQuiz?.isLearningMode ?: false

                        if (!isLearningMode) {
                            Button(
                                onClick = {
                                    isLoading = true
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(500)
                                        onClickLeaderboard()
                                        isLoading = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF2563EB).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "LIHAT LEADERBOARD",
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFontFamily,
                                            fontSize = 13.5.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                isLoadingLobby = true
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500)
                                    onClickHome()
                                    isLoadingLobby = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White
                            )
                        ) {
                            if (isLoadingLobby) {
                                CircularProgressIndicator(color = Color(0xFF2563EB), modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Home, null, tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "KEMBALI KE LOBBY",
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        fontSize = 13.5.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    } ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF2563EB))
        }
    }
}

@Composable
private fun ScoreMiniChip(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            fontFamily = interFontFamily
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium,
            fontSize = 10.5.sp,
            fontFamily = interFontFamily
        )
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                color = iconBg,
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 10.5.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = iconTint
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = iconTint.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF334155)
            )
        }

        Text(
            text = value,
            fontSize = 13.5.sp,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun AnimatedScoreCounter(
    targetValue: Int,
    fontSize: TextUnit = 40.sp,
    fontColor: Color = Color.White
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = animatedValue.value.toInt().toString(),
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        fontFamily = interFontFamily,
        color = fontColor
    )
}

@Composable
private fun SpinningSunAura(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "auraSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auraRotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Core ambient warm sunlight glow (soft radial gradient)
        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFDE047).copy(alpha = 0.65f),
                            Color(0xFFF59E0B).copy(alpha = 0.35f),
                            Color(0xFFF59E0B).copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // 2. Soft blurred radiant sun rays
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = rotation }
                .blur(8.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2f
            val rayCount = 14
            val angleStep = 360f / rayCount

            repeat(rayCount) { i ->
                val angle = i * angleStep
                withTransform({
                    rotate(angle, pivot = center)
                }) {
                    drawPath(
                        path = Path().apply {
                            moveTo(center.x, center.y)
                            lineTo(center.x + radius * 0.12f, center.y - radius * 0.95f)
                            quadraticTo(center.x, center.y - radius, center.x - radius * 0.12f, center.y - radius * 0.95f)
                            close()
                        },
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFDE047).copy(alpha = 0.55f),
                                Color(0xFFF59E0B).copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        )
                    )
                }
            }
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
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
fun AnimatedCoin(value: String) {
    val targetValue = value.toIntOrNull() ?: 0
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = "+${animatedValue.value.toInt()} Koin",
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        color = Color(0xFFD97706),
        fontFamily = interFontFamily
    )
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return buildString {
        if (minutes > 0) {
            append("${minutes}m ")
        }
        append("${remainingSeconds}s")
    }
}

