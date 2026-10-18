package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.data.model.BotDifficulty
import com.LambdaProject.MathArt.data.model.BotOpponent
import com.LambdaProject.MathArt.data.model.OnlineQuizDesc
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.utils.AvatarHelper
import com.LambdaProject.MathArt.utils.UserAvatarWithFrame
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RematchState {
    IDLE,
    THINKING,
    ACCEPTED,
    DECLINED
}

@Composable
fun DuelQuizResult(
    onClickLeaderboard: () -> Unit,
    onClickHome: () -> Unit,
    onClickRematch: () -> Unit,
    materialId: String,
    userId: String,
    quizList: List<OnlineQuizDesc>,
    botId: String = "",
    isSurrendered: Boolean = false,
    viewModel: OnlineQuizViewModel = hiltViewModel()
) {
    val quizResult by viewModel.quizResult.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val activeFrame by viewModel.activeFrame.collectAsState()
    val currentFirebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val userProfileName by viewModel.userName.collectAsState()
    val playerName = userProfileName.takeIf { it.isNotBlank() } ?: (currentFirebaseUser?.displayName?.takeIf { it.isNotBlank() } ?: "Player")
    val botDuelSummary by viewModel.botDuelSummary.collectAsState()
    val selectedBotFromVm by viewModel.selectedBot.collectAsState()
    val bot = remember(botId, botDuelSummary, selectedBotFromVm) {
        if (botId.isNotBlank()) {
            BotOpponent.findById(botId)
        } else {
            botDuelSummary?.bot ?: selectedBotFromVm ?: BotOpponent.getRandomBot()
        }
    }
    val actualIsSurrendered = isSurrendered || botDuelSummary?.isSurrendered == true

    var rematchState by remember { mutableStateOf(RematchState.IDLE) }
    var rematchCountdown by remember { mutableIntStateOf(5) }
    val coroutineScope = rememberCoroutineScope()

    val animStep1 = remember { Animatable(0f) } // Victory/Defeat Badge & Title
    val animStep2 = remember { Animatable(0f) } // Head-to-Head Comparison Card
    val animStep3 = remember { Animatable(0f) } // Leaderboard Info Banner
    val animStep4 = remember { Animatable(0f) } // Action Buttons
    val animStep5 = remember { Animatable(0f) } // Math Arena Branding Capsule

    LaunchedEffect(Unit) {
        viewModel.loadUserAvatar(userId)
        if (!actualIsSurrendered) {
            viewModel.rewardIfFirstTime(userId, materialId, quizList)
        }

        // Sequence in animations
        launch { animStep1.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
        delay(120)
        launch { animStep2.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
        delay(120)
        launch { animStep3.animateTo(1f, tween(350, easing = FastOutSlowInEasing)) }
        delay(100)
        launch { animStep4.animateTo(1f, tween(350, easing = FastOutSlowInEasing)) }
        delay(80)
        launch { animStep5.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
    }

    BackHandler(enabled = true) {
        // Ignored to prevent going back into completed quiz
    }

    val liveAnswers = viewModel.userAnswers
    val totalQuestions = questions.size.takeIf { it > 0 } ?: (liveAnswers.size.takeIf { it > 0 } ?: (quizResult?.answers?.size ?: 3))

    val playerCorrect = if (actualIsSurrendered) 0 else liveAnswers.count { it.isCorrect }
    val playerBasePoints = if (actualIsSurrendered) 0 else liveAnswers.sumOf { it.basePointsEarned }
    val playerTimeBonus = if (actualIsSurrendered) 0 else liveAnswers.sumOf { it.timeBonusEarned }
    val playerStreakBonus = if (actualIsSurrendered) 0 else liveAnswers.sumOf { it.streakBonusEarned }
    val playerBonusPoints = playerTimeBonus + playerStreakBonus
    val totalPoints = if (actualIsSurrendered) 0 else (playerBasePoints + playerBonusPoints)

    // Exact bot stats from match summary
    val botCorrect = if (actualIsSurrendered) (botDuelSummary?.totalCorrect ?: 1) else (botDuelSummary?.totalCorrect ?: 0)
    val botBasePoints = botDuelSummary?.totalBasePoints ?: 0
    val botTimeBonus = botDuelSummary?.totalTimeBonus ?: 0
    val botStreakBonus = botDuelSummary?.totalStreakBonus ?: 0
    val botBonusPoints = botDuelSummary?.totalBonusPoints ?: (botTimeBonus + botStreakBonus)
    val botScore = botDuelSummary?.totalScore ?: (botBasePoints + botBonusPoints)

    val isDraw = !actualIsSurrendered && (totalPoints == botScore && playerCorrect == botCorrect)
    val isVictory = !actualIsSurrendered && (totalPoints > botScore || (totalPoints == botScore && playerCorrect > botCorrect))

    val bannerColor = when {
        actualIsSurrendered -> Color(0xFFEF4444)
        isVictory -> Color(0xFFF59E0B)
        isDraw -> Color(0xFF38BDF8)
        else -> Color(0xFFEF4444)
    }

    val bannerGradient = when {
        actualIsSurrendered -> listOf(Color(0xFFF43F5E), Color(0xFFDC2626))
        isVictory -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
        isDraw -> listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
        else -> listOf(Color(0xFFF43F5E), Color(0xFFDC2626))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Victory / Defeat Badge
            val resultImageRes = when {
                actualIsSurrendered -> R.drawable.duel_surrender
                isVictory -> R.drawable.duel_victory
                isDraw -> R.drawable.duel_draw
                else -> R.drawable.duel_defeated
            }

            Box(
                modifier = Modifier
                    .size(92.dp)
                    .graphicsLayer {
                        scaleX = animStep1.value
                        scaleY = animStep1.value
                        alpha = animStep1.value
                    }
                    .shadow(12.dp, CircleShape, spotColor = bannerColor)
                    .background(
                        Brush.radialGradient(bannerGradient),
                        CircleShape
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = resultImageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
//                        .size(56.dp)
//                        .padding(2.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = when {
                    actualIsSurrendered -> "KAMU MENYERAH"
                    isVictory -> "KAMU MENANG DUEL!"
                    isDraw -> "HASIL SERI!"
                    else -> "KAMU KALAH DUEL"
                },
                fontFamily = interFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = animStep1.value
                    translationY = (1f - animStep1.value) * 30f
                }
            )

            Text(
                text = when {
                    actualIsSurrendered -> "Kamu keluar sebelum duel selesai. ${bot.name} otomatis memenangkan duel ini."
                    isVictory -> "Pertarungan hebat! Kamu berhasil mengungguli lawan."
                    isDraw -> "Kalian berdua sama-sama tangguh!"
                    else -> "Lawanmu kali ini lebih cepat dan cermat. Jangan menyerah, coba lagi!"
                },
                fontFamily = interFontFamily,
                fontSize = 13.sp,
                color = Color(0xFFBAE6FD),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = animStep1.value
                        translationY = (1f - animStep1.value) * 30f
                    }
                    .padding(top = 4.dp, bottom = 20.dp)
            )

            // 2. Head-to-Head Comparison Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.7f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = animStep2.value
                        translationY = (1f - animStep2.value) * 40f
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 18.dp, top = 28.dp, bottom = 18.dp)
                ) {
                    // Header Avatars & Final Scores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player Side
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                UserAvatarWithFrame(
                                    avatarName = userAvatar,
                                    frameId = activeFrame,
                                    avatarSize = 64.dp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = playerName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = interFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$totalPoints Pts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8),
                                fontFamily = interFontFamily
                            )
                            if (actualIsSurrendered) {
                                Spacer(Modifier.height(3.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.25f),
                                    border = BorderStroke(0.8.dp, Color(0xFFEF4444))
                                ) {
                                    Text(
                                        text = "Menyerah",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFECDD3),
                                        fontFamily = interFontFamily,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // VS / Score Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "$playerCorrect - $botCorrect",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Bot Side
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                UserAvatarWithFrame(
                                    avatarRes = bot.avatarRes,
                                    frameId = bot.frameId,
                                    avatarSize = 64.dp
                                )

                                // Chat Bubble (Semua teks "...", "Let's go!", "Lain kali" terkunci di anchor kanan bawah yang sama persis dan mengembang ke arah kiri)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = (-4).dp, y = 6.dp)
                                        .wrapContentSize(Alignment.BottomEnd, unbounded = true)
                                        .zIndex(10f)
                                ) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = rematchState != RematchState.IDLE,
                                        enter = fadeIn(tween(180)) + scaleIn(
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                            transformOrigin = TransformOrigin(1f, 1f)
                                        ),
                                        exit = fadeOut(tween(150)) + scaleOut(
                                            animationSpec = tween(150),
                                            transformOrigin = TransformOrigin(1f, 1f)
                                        )
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 2.dp, bottomStart = 10.dp),
                                            color = Color.White,
                                            shadowElevation = 6.dp,
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Text(
                                                text = when (rematchState) {
                                                    RematchState.THINKING -> "..."
                                                    RematchState.ACCEPTED -> "Let's go!"
                                                    RematchState.DECLINED -> "Lain kali ya"
                                                    else -> ""
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interFontFamily,
                                                maxLines = 1,
                                                softWrap = false,
                                                color = Color(0xFF0F172A),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = bot.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = interFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$botScore Pts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFB7185),
                                fontFamily = interFontFamily
                            )
                            if (actualIsSurrendered) {
                                Spacer(Modifier.height(3.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                                    border = BorderStroke(0.8.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = "Pemenang",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA7F3D0),
                                        fontFamily = interFontFamily,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    // Stats Comparison Table
                    DuelStatComparisonRow(
                        label = "Jawaban Benar",
                        playerValue = if (actualIsSurrendered) "0 / $totalQuestions" else "$playerCorrect / $totalQuestions",
                        botValue = "$botCorrect / $totalQuestions"
                    )
                    Spacer(Modifier.height(10.dp))
                    DuelStatComparisonRow(
                        label = "Base Point",
                        playerValue = if (actualIsSurrendered) "0" else "$playerBasePoints",
                        botValue = "$botBasePoints"
                    )
                    Spacer(Modifier.height(10.dp))
                    DuelStatComparisonRow(
                        label = "Bonus Waktu",
                        playerValue = if (actualIsSurrendered) "+0" else "+$playerTimeBonus",
                        botValue = "+$botTimeBonus"
                    )
                    Spacer(Modifier.height(10.dp))
                    DuelStatComparisonRow(
                        label = "Bonus Streak",
                        playerValue = if (actualIsSurrendered) "+0" else "+$playerStreakBonus",
                        botValue = "+$botStreakBonus"
                    )
                    Spacer(Modifier.height(10.dp))
                    DuelStatComparisonRow(
                        label = "Total Bonus",
                        playerValue = if (actualIsSurrendered) "+0" else "+$playerBonusPoints",
                        botValue = "+$botBonusPoints"
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            // 3. Leaderboard Info Banner (Solid Style)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (actualIsSurrendered) Color(0xFF450A0A) else Color(0xFF0F172A),
                border = BorderStroke(1.dp, if (actualIsSurrendered) Color(0xFF991B1B) else Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = animStep3.value
                        translationY = (1f - animStep3.value) * 40f
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Solid Icon Box
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (actualIsSurrendered) Color(0xFF7F1D1D).copy(alpha = 0.4f)
                                else Color(0xFFFBBF24).copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (actualIsSurrendered) Icons.Default.Close else Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = if (actualIsSurrendered) Color(0xFFFCA5A5) else Color(0xFFFDE047),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (actualIsSurrendered) "Duel Dibatalkan (Menyerah)" else "Leaderboard Diperbarui!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontFamily = interFontFamily
                        )
                        Text(
                            text = if (actualIsSurrendered)
                                "Poin tidak dicatat di Leaderboard karena kamu menyerah dari duel."
                            else "Total $totalPoints poin telah otomatis dicatat di leaderboard.",
                            fontSize = 12.sp,
                            color = if (actualIsSurrendered) Color(0xFFFECDD3) else Color(0xFFCBD5E1),
                            fontFamily = interFontFamily,
                            lineHeight = 17.sp
                        )
                    }
                }
            }


//            Surface(
//                shape = RoundedCornerShape(16.dp),
//                color = if (actualIsSurrendered) Color(0xFFEF4444).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f),
//                border = BorderStroke(1.dp, if (actualIsSurrendered) Color(0xFFEF4444).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f)),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .graphicsLayer {
//                        alpha = animStep3.value
//                        translationY = (1f - animStep3.value) * 40f
//                    }
//            ) {
//                Row(
//                    modifier = Modifier.padding(14.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(38.dp)
//                            .background(if (actualIsSurrendered) Color(0xFFEF4444).copy(alpha = 0.25f) else Color(0xFF38BDF8).copy(alpha = 0.2f), CircleShape),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            imageVector = if (actualIsSurrendered) Icons.Default.Close else Icons.Default.Leaderboard,
//                            contentDescription = null,
//                            tint = if (actualIsSurrendered) Color(0xFFFCA5A5) else Color(0xFFFDE047),
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            text = if (actualIsSurrendered) "Duel Dibatalkan (Menyerah)" else "Leaderboard Diperbarui!",
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 13.sp,
//                            color = Color.White,
//                            fontFamily = interFontFamily
//                        )
//                        Text(
//                            text = if (actualIsSurrendered) "Poin tidak dicatat di Leaderboard karena kamu menyerah dari duel." else "Total $totalPoints poin telah otomatis dicatat di leaderboard.",
//                            fontSize = 11.5.sp,
//                            color = if (actualIsSurrendered) Color(0xFFFECDD3) else Color(0xFFCBD5E1),
//                            fontFamily = interFontFamily
//                        )
//                    }
//                }
//            }

            // 3b. EXP Card (Duel)
            val expBreakdown by viewModel.quizExpBreakdown.collectAsState()
            if (!actualIsSurrendered && expBreakdown != null) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = animStep3.value
                            translationY = (1f - animStep3.value) * 40f
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Perolehan Experience (EXP)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontFamily = interFontFamily
                                )
                                Text(
                                    text = "Progres level akun Senimatika",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBAE6FD),
                                    fontFamily = interFontFamily
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
                                    fontSize = 12.5.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        ExpBreakdownRow(
                            label = "Base EXP",
                            value = "${expBreakdown?.baseExp ?: 100} EXP",
                            valueColor = Color(0xFFDDD6FE)
                        )
                        Spacer(Modifier.height(8.dp))
                        ExpBreakdownRow(
                            label = "Bonus Performa",
                            subLabel = "Akurasi & Streak",
                            value = "+${expBreakdown?.bonusExp ?: 0} EXP",
                            valueColor = Color(0xFF34D399)
                        )
                        Spacer(Modifier.height(8.dp))
                        val multPercent = ((expBreakdown?.repeatMultiplier ?: 1.0f) * 100).toInt()
                        ExpBreakdownRow(
                            label = "Multiplier Sesi",
                            subLabel = "Sesi ke-${expBreakdown?.sessionCount ?: 1}",
                            value = "$multPercent%",
                            valueColor = if (multPercent == 100) Color(0xFF60A5FA) else Color(0xFFFBBF24)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Informasi Mengapa Multiplier Berkurang
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 1.dp)
                                )
                                Column {
                                    Text(
                                        text = "Mengapa multiplier berkurang?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFDDD6FE),
                                        fontFamily = interFontFamily
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Multiplier EXP akan berkurang secara gradual ketika kuis yang sama dimainkan berulang kali. Yuk coba kuis lainnya agar kamu mendapat EXP penuh!",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFC4B5FD),
                                        fontFamily = interFontFamily,
                                        lineHeight = 14.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 4. Action Buttons / Rematch Countdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = animStep4.value
                        translationY = (1f - animStep4.value) * 40f
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (rematchState == RematchState.ACCEPTED) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0284C7),
                            border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { (rematchCountdown / 5f) },
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.3f),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Duel ulang dimulai dalam $rematchCountdown detik...",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (rematchState == RematchState.IDLE) {
                                    rematchState = RematchState.THINKING
                                    coroutineScope.launch {
                                        delay(3000) // 3 seconds thinking
                                        val isAccepted = kotlin.random.Random.nextBoolean() // 50/50 probability
                                        if (isAccepted) {
                                            rematchState = RematchState.ACCEPTED
                                            rematchCountdown = 5
                                            for (i in 5 downTo 1) {
                                                rematchCountdown = i
                                                delay(1000)
                                            }
                                            onClickRematch()
                                        } else {
                                            rematchState = RematchState.DECLINED
                                        }
                                    }
                                }
                            },
                            enabled = !actualIsSurrendered && rematchState == RematchState.IDLE,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (rematchState) {
                                    RematchState.DECLINED -> Color(0xFF475569)
                                    RematchState.THINKING -> Color(0xFF0284C7).copy(alpha = 0.7f)
                                    else -> Color(0xFF0284C7)
                                },
                                disabledContainerColor = when {
                                    actualIsSurrendered -> Color(0xFF334155).copy(alpha = 0.6f)
                                    rematchState == RematchState.DECLINED -> Color(0xFF334155).copy(alpha = 0.8f)
                                    else -> Color(0xFF0284C7).copy(alpha = 0.6f)
                                },
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    actualIsSurrendered -> Color(0xFF475569)
                                    rematchState == RematchState.DECLINED -> Color(0xFF64748B)
                                    else -> Color(0xFF38BDF8)
                                }
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (actualIsSurrendered) 0.dp else 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (actualIsSurrendered) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "DUEL ULANG (MENYERAH)",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                } else if (rematchState == RematchState.THINKING) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "MENUNGGU RESPONS LAWAN...",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        fontSize = 13.sp
                                    )
                                } else if (rematchState == RematchState.DECLINED) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "LAWAN MENOLAK DUEL ULANG",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "DUEL ULANG",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        }
                    }

                    if (rematchState != RematchState.ACCEPTED && rematchState != RematchState.THINKING) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onClickLeaderboard,
                                // Tombol juga otomatis mati saat sedang "Thinking"
//                                enabled = rematchState != RematchState.THINKING,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.List, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Leaderboard", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = onClickHome,
                                enabled = rematchState != RematchState.THINKING,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Kembali", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(10.dp)
//                        ) {
//                            OutlinedButton(
//                                onClick = onClickLeaderboard,
//                                enabled = rematchState != RematchState.THINKING && rematchState != RematchState.ACCEPTED,
//                                shape = RoundedCornerShape(14.dp),
//                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.1f)),
//                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(46.dp)
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
//                                ) {
//                                    Icon(Icons.AutoMirrored.Filled.List, null, tint = Color.White, modifier = Modifier.size(16.dp))
//                                    Text("Leaderboard", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
//                                }
//                            }
//
//                            OutlinedButton(
//                                onClick = onClickHome,
//                                enabled = rematchState != RematchState.THINKING && rematchState != RematchState.ACCEPTED,
//                                shape = RoundedCornerShape(14.dp),
//                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.1f)),
//                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(46.dp)
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
//                                ) {
//                                    Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(16.dp))
//                                    Text("Kembali", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
//                                }
//                            }
//                        }
                    }
                }

            Spacer(Modifier.height(24.dp))

            // Math Arena Branding
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f)),
                modifier = Modifier.graphicsLayer {
                    scaleX = 0.85f + 0.15f * animStep5.value
                    scaleY = 0.85f + 0.15f * animStep5.value
                    alpha = animStep5.value
                    translationY = (1f - animStep5.value) * 30f
                }
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        ) {
                            append("Math ")
                        }
                        withStyle(
                            SpanStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2563EB), // Royal Blue
                                        Color(0xFF0284C7), // Sky Blue
                                        Color(0xFF06B6D4)  // Cyan
                                    )
                                ),
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        ) {
                            append("Arena")
                        }
                    },
                    fontFamily = interFontFamily,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DuelStatComparisonRow(
    label: String,
    playerValue: String,
    botValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = playerValue,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF38BDF8),
            fontFamily = interFontFamily,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            fontFamily = interFontFamily,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.Center
        )
        Text(
            text = botValue,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFFFB7185),
            fontFamily = interFontFamily,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ExpBreakdownRow(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    subLabel: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFCBD5E1),
                fontFamily = interFontFamily
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    fontSize = 10.5.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = interFontFamily
                )
            }
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontFamily = interFontFamily
        )
    }
}
