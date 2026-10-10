package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.BotOpponent
import com.LambdaProject.MathArt.data.model.AppAudioManager
import com.LambdaProject.MathArt.data.model.OnlineQuizQuestion
import com.LambdaProject.MathArt.data.model.ScorestreakState
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.utils.UserAvatarWithFrame
import kotlinx.coroutines.delay

// Helper for calculating duel rounds and question divisions
object DuelRoundHelper {
    fun getQuestionsPerRound(totalQuestions: Int): Int = when {
        totalQuestions <= 5 -> 1
        totalQuestions <= 10 -> 2
        totalQuestions <= 15 -> 3
        else -> 4
    }

    fun getTotalRounds(totalQuestions: Int): Int {
        if (totalQuestions <= 0) return 0
        val perRound = getQuestionsPerRound(totalQuestions)
        return (totalQuestions + perRound - 1) / perRound
    }

    fun getRoundIndex(questionIndex: Int, totalQuestions: Int): Int {
        val perRound = getQuestionsPerRound(totalQuestions)
        return questionIndex / perRound
    }

    fun isRoundEndQuestion(questionIndex: Int, totalQuestions: Int): Boolean {
        if (questionIndex >= totalQuestions - 1) return true
        val perRound = getQuestionsPerRound(totalQuestions)
        return (questionIndex + 1) % perRound == 0
    }
}

@Composable
fun DuelScoreboardOverlay(
    visible: Boolean = true,
    playerName: String,
    playerAvatarRes: Int,
    playerFrameId: String? = null,
    playerScore: Int,
    bot: BotOpponent,
    questions: List<OnlineQuizQuestion>,
    playerHistory: List<Boolean?>,
    botHistory: List<Boolean?>,
    currentQuestionIndex: Int,
    onNextQuestion: () -> Unit,
    isLastQuestion: Boolean,
    isSurrendered: Boolean = false,
    scorestreakState: ScorestreakState? = null,
    modifier: Modifier = Modifier
) {
    // Count of correct answers (accumulated across all rounds)
    val playerCountCorrect = playerHistory.count { it == true }
    val botCountCorrect = botHistory.count { it == true }

    // Countdown Timer (7 seconds per round)
    var countdown by remember(currentQuestionIndex, visible) { mutableIntStateOf(7) }
    var showSnackbar by remember(currentQuestionIndex, visible) { mutableStateOf(false) }

    // Sequential (linear) animation steps
    val animStep1 = remember { Animatable(0f) } // Header
    val animStep2 = remember { Animatable(0f) } // Head-to-Head Card
    val animStep3 = remember { Animatable(0f) } // Rounds list
    val animStep4 = remember { Animatable(0f) } // Bottom CTA

    val questionsPerRound = DuelRoundHelper.getQuestionsPerRound(questions.size)
    val roundChunks = questions.indices.chunked(questionsPerRound)
    val currentRoundIndex = DuelRoundHelper.getRoundIndex(currentQuestionIndex, questions.size)

    LaunchedEffect(currentQuestionIndex, visible) {
        if (visible) {
            animStep1.snapTo(0f)
            animStep2.snapTo(0f)
            animStep3.snapTo(0f)
            animStep4.snapTo(0f)

            if (scorestreakState != null) {
                showSnackbar = true
                scorestreakState.type?.let { AppAudioManager.playScoreSound(it) }
            }

            // Sequential linear animations
            animStep1.animateTo(1f, tween(180, easing = LinearEasing))
            animStep2.animateTo(1f, tween(200, easing = LinearEasing))
            animStep3.animateTo(1f, tween(200, easing = LinearEasing))
            animStep4.animateTo(1f, tween(180, easing = LinearEasing))

            countdown = 7
            while (countdown > 0) {
                delay(1000L)
                countdown -= 1
                if (countdown == 3) {
                    showSnackbar = false
                }
            }
            onNextQuestion()
        }
    }

    if (visible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(90f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A), // Dark slate
                            Color(0xFF0C4A6E), // Deep ocean
                            Color(0xFF0284C7)  // Vibrant blue
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Top Bar Header (Step 1 Animation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp)
                        .alpha(animStep1.value)
                        .offset(y = ((1f - animStep1.value) * -16).dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    Surface(
//                        shape = CircleShape,
//                        color = Color(0xFF38BDF8).copy(alpha = 0.2f),
//                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Shield,
//                            contentDescription = null,
//                            tint = Color(0xFF38BDF8),
//                            modifier = Modifier
//                                .padding(6.dp)
//                                .size(16.dp)
//                        )
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Klasemen Sementara",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = interFontFamily
                    )
                }

                // Surrender Notice Banner (if player surrendered)
                if (isSurrendered) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE11D48).copy(alpha = 0.25f),
                        border = BorderStroke(1.2.dp, Color(0xFFFB7185).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .alpha(animStep2.value)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE11D48),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Kamu Menyerah",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = interFontFamily
                                )
                                Text(
                                    text = "Lawan (${bot.name}) otomatis memenangkan pertandingan.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFECDD3),
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }
                }

                // 2. Head-to-Head Score Card (Step 2 Animation)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(animStep2.value)
                        .scale(0.92f + 0.08f * animStep2.value)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player Side
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(68.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                UserAvatarWithFrame(
                                    avatarRes = playerAvatarRes,
                                    frameId = playerFrameId,
                                    avatarSize = 64.dp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = playerName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = interFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$playerScore Poin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF38BDF8),
                                fontFamily = interFontFamily
                            )
                        }

                        // Middle Score (accumulated correct answers across rounds)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "$playerCountCorrect - $botCountCorrect",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = interFontFamily,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "SKOR BENAR",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE0F2FE),
                                    fontFamily = interFontFamily,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Bot Side
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(68.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                UserAvatarWithFrame(
                                    avatarRes = bot.avatarRes,
                                    frameId = bot.frameId,
                                    avatarSize = 64.dp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = bot.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = interFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${bot.currentScore} Poin",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFB7185),
                                fontFamily = interFontFamily
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // 3. Rounds Breakdown List Card (Step 3 Animation)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .alpha(animStep3.value)
                        .offset(y = ((1f - animStep3.value) * 16).dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roundChunks.forEachIndexed { rIndex, qIndices ->
                            val pResults = qIndices.map { playerHistory.getOrNull(it) }
                            val bResults = qIndices.map { botHistory.getOrNull(it) }
                            val isCurrent = rIndex == currentRoundIndex

                            DuelRoundRow(
                                roundNumber = rIndex + 1,
                                playerResults = pResults,
                                botResults = bResults,
                                isCurrentRound = isCurrent,
                                questionsPerRound = questionsPerRound
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                val isFinal = isLastQuestion || isSurrendered

                // 4. Bottom Countdown Indicator (Non-clickable, handled by timer)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isFinal) Color(0xFF0284C7) else Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(
                        1.5.dp,
                        if (isFinal) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.3f)
                    ),
                    shadowElevation = if (isFinal) 6.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .alpha(animStep4.value)
                        .offset(y = ((1f - animStep4.value) * 16).dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { (7 - countdown) / 7f },
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFFFDE047),
                                trackColor = Color.White.copy(alpha = 0.25f),
                                strokeWidth = 2.5.dp
                            )
                            if (!isFinal) {
                                val buttonText = if (questionsPerRound > 1) {
                                    "RONDE BERIKUTNYA (${countdown}s)"
                                } else {
                                    "SOAL BERIKUTNYA (${countdown}s)"
                                }
                                Text(
                                    text = buttonText,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            } else {
                                Text(
                                    text = "LIHAT HASIL DUEL (${countdown}s)",
                                    fontWeight = FontWeight.Black,
                                    fontFamily = interFontFamily,
                                    fontSize = 14.5.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            ScorestreakSnackbar(
                state = scorestreakState,
                visible = showSnackbar,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 10.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(100f)
            )
        }
    }
}

@Composable
fun DuelRoundRow(
    roundNumber: Int,
    playerResults: List<Boolean?>,
    botResults: List<Boolean?>,
    isCurrentRound: Boolean,
    questionsPerRound: Int
) {
    val rowBackground = if (isCurrentRound) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Color.White.copy(alpha = 0.06f)
    }

    val playerScrollState = rememberScrollState()
    val botScrollState = rememberScrollState()

    // Sync horizontal scrolling between player and bot sides if swiped
    if (questionsPerRound >= 4) {
        LaunchedEffect(playerScrollState.value) {
            if (botScrollState.value != playerScrollState.value) {
                botScrollState.scrollTo(playerScrollState.value)
            }
        }
        LaunchedEffect(botScrollState.value) {
            if (playerScrollState.value != botScrollState.value) {
                playerScrollState.scrollTo(botScrollState.value)
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = rowBackground,
        border = if (isCurrentRound) BorderStroke(1.2.dp, Color(0xFFFDE047)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Indicator Section (Left Column, constrained width)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                contentAlignment = if (questionsPerRound >= 4) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                DuelIndicatorList(
                    results = playerResults,
                    scrollState = playerScrollState,
                    isBot = false,
                    questionsPerRound = questionsPerRound
                )
            }

            // Center Round Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isCurrentRound) Color(0xFFFDE047).copy(alpha = 0.2f) else Color.Transparent,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "RONDE $roundNumber",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        fontFamily = interFontFamily,
                        color = if (isCurrentRound) Color(0xFFFEF08A) else Color.White
                    )
                }
            }

            // Bot Indicator Section (Right Column, constrained width)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                DuelIndicatorList(
                    results = botResults,
                    scrollState = botScrollState,
                    isBot = true,
                    questionsPerRound = questionsPerRound
                )
            }
        }
    }
}

@Composable
fun DuelIndicatorList(
    results: List<Boolean?>,
    scrollState: ScrollState,
    isBot: Boolean,
    questionsPerRound: Int
) {
    val boxWidth = when (questionsPerRound) {
        1 -> 44.dp
        2 -> 36.dp
        3 -> 30.dp
        else -> 28.dp
    }

    if (questionsPerRound >= 4) {
        // Horizontally swipeable row when there are 4 or 5 questions
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            results.forEach { status ->
                DuelIndicatorBox(
                    status = status,
                    width = boxWidth,
                    height = 26.dp
                )
            }
        }
    } else {
        // Fixed row aligned towards center
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            results.forEach { status ->
                DuelIndicatorBox(
                    status = status,
                    width = boxWidth,
                    height = 26.dp
                )
            }
        }
    }
}

@Composable
fun DuelIndicatorBox(
    status: Boolean?,
    width: Dp = 44.dp,
    height: Dp = 26.dp
) {
    val (boxColor, iconVector, iconTint) = when (status) {
        true -> Triple(Color(0xFF10B981), Icons.Default.Check, Color.White)
        false -> Triple(Color(0xFFEF4444), Icons.Default.Close, Color.White)
        null -> Triple(Color(0xFF475569).copy(alpha = 0.6f), null, Color.Transparent)
    }

    val iconSize = if (width.value < 34f) 14.dp else 16.dp

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .background(boxColor, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

