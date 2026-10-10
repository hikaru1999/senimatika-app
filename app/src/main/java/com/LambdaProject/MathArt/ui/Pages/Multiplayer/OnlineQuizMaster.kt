package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import android.annotation.SuppressLint
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.data.powerUpColors
import com.LambdaProject.MathArt.data.powerUpIcons
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.data.model.QuestionType
import com.LambdaProject.MathArt.data.model.ScorestreakState
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.NetworkObserver
import com.LambdaProject.MathArt.data.model.AppAudioManager
import com.LambdaProject.MathArt.ui.Pages.NotFoundScreen
import com.LambdaProject.MathArt.ui.components.MathText
import com.LambdaProject.MathArt.utils.ZoomableImageOverlay
import com.LambdaProject.MathArt.utils.AvatarHelper
import com.LambdaProject.MathArt.utils.UserAvatarWithFrame
import com.LambdaProject.MathArt.data.model.BotDuelEngine
import com.LambdaProject.MathArt.data.model.BotOpponent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

@SuppressLint("MutableCollectionMutableState")
@Composable
fun OnlineQuizMaster(
    onFinishQuiz: (botId: String, isSurrendered: Boolean) -> Unit,
    navController: NavController,
    onNextClick: () -> Unit,
    onBackPressed: () -> Unit,
    onTriggerScorestreak: (ScorestreakState) -> Unit,
    materialId: String,
    userId: String,
    isDuel: Boolean = false,
    initialBotId: String = "",
    viewModel: OnlineQuizViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val networkObserver = remember { NetworkObserver(context) }
    val isOnline by networkObserver.isConnected.collectAsState(initial = true)

    var isDataInvalid by remember { mutableStateOf(false) }
    var wasOffline by remember { mutableStateOf(false) }
    var isAnswered by remember { mutableStateOf(false) }
    val userAvatar by viewModel.userAvatar.collectAsState()
    val activeFrame by viewModel.activeFrame.collectAsState()
    val selectedBotFromVm by viewModel.selectedBot.collectAsState()
    val botOpponent = remember(initialBotId, selectedBotFromVm) {
        if (initialBotId.isNotBlank()) {
            val bot = BotOpponent.findById(initialBotId)
            viewModel.setSelectedBot(bot)
            bot
        } else {
            val bot = selectedBotFromVm ?: BotOpponent.getRandomBot()
            if (selectedBotFromVm == null) {
                viewModel.setSelectedBot(bot)
            }
            bot
        }
    }
    val botEngine = remember(botOpponent) { BotDuelEngine(botOpponent) }
    val playerHistory = remember { mutableStateListOf<Boolean?>() }
    val botHistory = remember { mutableStateListOf<Boolean?>() }
    var showDuelOverlay by remember { mutableStateOf(false) }
    var showBotAnsweredAlert by remember { mutableStateOf(false) }
    var isPlayerSurrendered by remember { mutableStateOf(false) }

    val currentFirebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val userProfileName by viewModel.userName.collectAsState()
    val playerName = userProfileName.takeIf { it.isNotBlank() } ?: (currentFirebaseUser?.displayName?.takeIf { it.isNotBlank() } ?: "Player")

    LaunchedEffect(materialId, userId) {
        viewModel.loadUserAvatar(userId)
        viewModel.loadQuizForSelectedMaterial(userId, materialId)

        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("online_quizzes").document(materialId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null || !snapshot.exists() || snapshot.getBoolean("active") == false) {
                    isDataInvalid = true
                }
            }
    }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            wasOffline = true
        } else if (isOnline && wasOffline) {
            if (isAnswered) {
                delay(500)
            }
            wasOffline = false
        }
    }

    val questions by viewModel.questions.collectAsState()
    val isQuizNotFound by viewModel.isQuizNotFound.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val currentBasePoints by viewModel.currentBasePoints.collectAsState()
    val currentPoints by viewModel.currentPoints.collectAsState()
    val scorestreakState by viewModel.scorestreakState.collectAsState()

    var duelDisplayedPoints by remember { mutableIntStateOf(0) }

//    if (questions.isEmpty() || currentIndex >= questions.size) {
//        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator(color = Color(0xFF1976D2), strokeWidth = 3.dp)
//        }
//        return
//    }

    LaunchedEffect(isQuizNotFound, isDataInvalid) {
        if (isQuizNotFound || isDataInvalid) {
            navController.currentBackStackEntry?.savedStateHandle?.set("is_not_found", true)
        } else {
            navController.currentBackStackEntry?.savedStateHandle?.set("is_not_found", false)
        }
    }

    if (isQuizNotFound || isDataInvalid) {
        NotFoundScreen(
            title = "Yah, Kuis Tidak Ditemukan",
            message = "Maaf ya, konten ini sedang tidak tersedia.",
            onBack = {
                navController.currentBackStackEntry?.savedStateHandle?.set("is_not_found", false)
                viewModel.refreshMaterials()
                navController.popBackStack("OnlineQuizPage", inclusive = false)
            }
        )
        return
    }

    if (questions.isEmpty() || currentIndex >= questions.size) {
        OnlineQuizSkeletonScreen()
        return
    }

    val question = questions[currentIndex]
    val totalQuestions = questions.size

    val selectedAnswers = remember(question.questionNumber) { mutableStateOf(mutableSetOf<Int>()) }
    val typedAnswer = remember(question) { mutableStateOf("") }
    val animatedProgress = remember { Animatable(1f) }

    val imageRes = question.imageRes

    var timer by remember(question.questionNumber) { mutableIntStateOf(question.durationSeconds) }
    var isReadyToRender by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    var showScoreSnackbar by remember { mutableStateOf(false) }
    var scoreState by remember { mutableStateOf<ScorestreakState?>(null) }
    var snackbarTrigger by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var showPowerUpInfoDialog by remember { mutableStateOf(false) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }
    var showZoomDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val currentQuestionType = question.type

    // Duel State Tracking
    var isPlayerDone by remember(currentIndex) { mutableStateOf(false) }
    var playerTimeLeft by remember(currentIndex) { mutableIntStateOf(question.durationSeconds) }
    var playerProgress by remember(currentIndex) { mutableFloatStateOf(1f) }

    var isBotDone by remember(currentIndex) { mutableStateOf(false) }
    var botTimeLeft by remember(currentIndex) { mutableIntStateOf(question.durationSeconds) }
    var botProgress by remember(currentIndex) { mutableFloatStateOf(1f) }

    var bothDoneHandled by remember(currentIndex) { mutableStateOf(false) }
    var transitionCountdown by remember(currentIndex) { mutableIntStateOf(0) }

    fun lockPlayerAnswer(answers: List<Int>, textAns: String?) {
        if (isPlayerDone) return
        isPlayerDone = true
        val lockedSec = timer.coerceAtLeast(0)
        playerTimeLeft = lockedSec
        playerProgress = (lockedSec.toFloat() / question.durationSeconds.toFloat()).coerceIn(0f, 1f)

        viewModel.checkAnswer(
            selectedAnswers = answers,
            userTextAnswer = textAns,
            timeLeft = lockedSec,
            onScoreUpdated = { _, _ -> },
            onStreakUpdate = { streakState ->
                scoreState = streakState
                if (!isDuel) {
                    snackbarTrigger++
                    onTriggerScorestreak(streakState)
                }
            }
        )
    }

    BackHandler(enabled = zoomImageUrl != null) {
        zoomImageUrl = null
    }

    LaunchedEffect(questions) {
        if (questions.isEmpty()) {
            delay(3000)
            if (questions.isEmpty()) isDataInvalid = true
        }
    }

    LaunchedEffect(key1 = currentIndex) {
        isAnswered = false
        isReadyToRender = false
        showBotAnsweredAlert = false
        showScoreSnackbar = false
        isPlayerDone = false
        isBotDone = false
        bothDoneHandled = false
        transitionCountdown = 0
        playerTimeLeft = question.durationSeconds
        botTimeLeft = question.durationSeconds
        playerProgress = 1f
        botProgress = 1f

        delay(400)
        isReadyToRender = true

        val durationMs = (question.durationSeconds * 1000).toLong()
        val startTime = SystemClock.elapsedRealtime()
        val endTime = startTime + durationMs

        if (isDuel) {
            AppAudioManager.playDuelRoundStart()
            val minTime = botOpponent.difficulty.minThinkTimeSec.coerceAtMost(question.durationSeconds)
            val maxTime = botOpponent.difficulty.maxThinkTimeSec.coerceAtMost(question.durationSeconds)
            val botThinkSec = if (maxTime > minTime) (minTime..maxTime).random() else minTime
            val botFinishTime = startTime + (botThinkSec * 1000L)

            launch {
                while (isActive && !bothDoneHandled) {
                    val now = SystemClock.elapsedRealtime()
                    val timeLeftMs = (endTime - now).coerceAtLeast(0L)
                    val rawProgress = (timeLeftMs / durationMs.toFloat()).coerceIn(0f, 1f)
                    val currentSecLeft = (timeLeftMs / 1000).toInt()
                    timer = currentSecLeft

                    // 1. Update player timer if still thinking
                    if (!isPlayerDone) {
                        playerTimeLeft = currentSecLeft
                        playerProgress = rawProgress
                        if (timeLeftMs <= 0L) {
                            // Player timed out
                            isPlayerDone = true
                            playerTimeLeft = 0
                            playerProgress = 0f
                            viewModel.checkAnswer(
                                selectedAnswers = selectedAnswers.value.toList(),
                                userTextAnswer = typedAnswer.value,
                                timeLeft = 0,
                                onScoreUpdated = { _, _ -> },
                                onStreakUpdate = { streakState ->
                                    scoreState = streakState
                                    if (!isDuel) {
                                        snackbarTrigger++
                                        onTriggerScorestreak(streakState)
                                    }
                                }
                            )
                        }
                    }

                    // 2. Update bot timer if still thinking
                    if (!isBotDone) {
                        botTimeLeft = currentSecLeft
                        botProgress = rawProgress
                        if (now >= botFinishTime || timeLeftMs <= 0L) {
                            isBotDone = true
                            val botFinalSec = (question.durationSeconds - botThinkSec).coerceAtLeast(0)
                            botTimeLeft = botFinalSec
                            botProgress = (botFinalSec.toFloat() / question.durationSeconds.toFloat()).coerceIn(0f, 1f)
                            val isBotCorrect = botEngine.recordBotAnswer(currentIndex, question, botFinalSec).isCorrect
                            if (botHistory.size <= currentIndex) {
                                botHistory.add(isBotCorrect)
                            }
                        }
                    }

                    // 3. Trigger transition / scoreboard automatically when BOTH have answered or time expires
                    if ((isPlayerDone && isBotDone) || timeLeftMs <= 0L) {
                        bothDoneHandled = true
                        if (botHistory.size <= currentIndex) {
                            val isBotCorrect = botEngine.getOrSimulateCurrentResult(currentIndex, question)
                            botHistory.add(isBotCorrect)
                        }
                        val lastUserAnswer = viewModel.userAnswers.lastOrNull { it.questionNumber == question.questionNumber }
                        val isPlayerCorrect = lastUserAnswer?.isCorrect ?: false
                        if (playerHistory.size <= currentIndex) {
                            playerHistory.add(isPlayerCorrect)
                        }
                        isAnswered = true

                        val isRoundEnd = DuelRoundHelper.isRoundEndQuestion(currentIndex, questions.size) || isPlayerSurrendered

                        if (!isRoundEnd) {
                            // Intra-round: Munculkan ScorestreakSnackbar & timer transisi ke soal berikutnya
                            if (scoreState != null) {
                                snackbarTrigger++
                                scoreState?.let { onTriggerScorestreak(it) }
                            }
                            transitionCountdown = 3
                            while (transitionCountdown > 0) {
                                delay(1000L)
                                transitionCountdown -= 1
                            }
                            duelDisplayedPoints = currentBasePoints
                            viewModel.nextQuestion()
                            break
                        } else {
                            // Akhir ronde: Countdown transisi lalu munculkan DuelScoreboardOverlay
                            transitionCountdown = 2
                            while (transitionCountdown > 0) {
                                delay(1000L)
                                transitionCountdown -= 1
                            }
                            if (scoreState != null) {
                                scoreState?.let { onTriggerScorestreak(it) }
                            }
                            showDuelOverlay = true
                            break
                        }
                    }

                    delay(60L)
                }
            }
        } else {
            // Single Player Timer
            launch {
                while (isActive) {
                    if (isAnswered) break

                    val now = SystemClock.elapsedRealtime()
                    val timeLeftMs = (endTime - now).coerceAtLeast(0L)

                    val progress = timeLeftMs / durationMs.toFloat()
                    animatedProgress.snapTo(progress)
                    timer = (timeLeftMs / 1000).toInt()

                    if (isAnswered || timeLeftMs <= 0L) {
                        if (timeLeftMs <= 0L && !isAnswered) {
                            val finalAnswer = selectedAnswers.value.toList()
                            isAnswered = true
                            viewModel.checkAnswer(
                                selectedAnswers = finalAnswer,
                                userTextAnswer = typedAnswer.value,
                                timeLeft = 0,
                                onScoreUpdated = { _, _ -> },
                                onStreakUpdate = { streakState ->
                                    scoreState = streakState
                                    snackbarTrigger++
                                    onTriggerScorestreak(streakState)
                                }
                            )
                        }

                        if (currentIndex < questions.lastIndex) {
                            delay(600)
                            viewModel.nextQuestion()
                        } else if (!isNavigating) {
                            isNavigating = true
                            delay(1000)
                            val uid = viewModel.getCurrentUserId() ?: userId
                            viewModel.saveQuizResult(uid, materialId)
                            navController.navigate("OnlineQuizResult/${materialId}/${userId}") {
                                popUpTo("StartQuiz") { inclusive = true }
                            }
                        }
                        break
                    }
                    delay(100L)
                }
            }
        }
    }

    LaunchedEffect(snackbarTrigger) {
        if (snackbarTrigger > 0 && scoreState != null) {
            showScoreSnackbar = true
            scoreState?.type?.let { AppAudioManager.playScoreSound(it) }
            delay(3500)
            showScoreSnackbar = false
        }
    }

    BackHandler(enabled = true) {
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { 
                Text(
                    text = if (isDuel) "Menyerah dari Duel?" else "Keluar dari Kuis?", 
                    fontFamily = interFontFamily, 
                    fontWeight = FontWeight.Black, 
                    color = if (isDuel) Color(0xFFE11D48) else Color(0xFF1A237E)
                ) 
            },
            text = { 
                Text(
                    text = if (isDuel) 
                        "Jika kamu keluar saat duel sedang aktif, kamu akan langsung dinyatakan menyerah dan lawan (${botOpponent.name}) otomatis memenangkan duel.\n\nYakin ingin menyerah?"
                    else 
                        "Jika kamu keluar kuis maka progresmu akan hilang. Yakin ingin keluar?", 
                    fontFamily = interFontFamily, 
                    textAlign = TextAlign.Start,
                    color = Color.DarkGray
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        if (isDuel) {
                            isLoading = true
                            CoroutineScope(Dispatchers.Main).launch {
                                isPlayerSurrendered = true
                                while (playerHistory.size < questions.size) {
                                    playerHistory.add(false)
                                }
                                while (botHistory.size < questions.size) {
                                    val qIdx = botHistory.size
                                    if (qIdx < questions.size) {
                                        botEngine.getOrSimulateCurrentResult(qIdx, questions[qIdx])
                                    }
                                    botHistory.add(true)
                                }
                                val summary = botEngine.getSummary(questions.size, isSurrendered = true)
                                viewModel.setBotDuelSummary(summary)
                                delay(300)
                                isLoading = false
                                onFinishQuiz(botOpponent.id, true)
                            }
                        } else {
                            isLoading = true
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1500)
                                onBackPressed()
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDuel) Color(0xFFE11D48) else Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text(
                            text = if (isDuel) "Ya, Menyerah" else "Ya, Keluar", 
                            fontFamily = interFontFamily, 
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(
                        text = if (isDuel) "Lanjut Duel" else "Batal", 
                        fontFamily = interFontFamily, 
                        color = Color.Gray, 
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showPowerUpInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPowerUpInfoDialog = false },
            title = {
                Text(
                    text = "Fitur Power Up",
                    fontWeight = FontWeight.Black,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
            },
            text = {
                Text(
                    text = "Power Up masih dalam tahap pengembangan. Harap bersabar dan tunggu update selanjutnya!",
                    fontFamily = interFontFamily,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPowerUpInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mengerti", fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FE))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (isDuel) {
                    DuelDualTopBar(
                        currentIndex = currentIndex,
                        totalQuestions = questions.size,
                        playerName = playerName,
                        playerAvatarRes = AvatarHelper.getAvatarDrawableRes(userAvatar),
                        playerFrameId = activeFrame,
                        isPlayerDone = isPlayerDone,
                        playerProgress = playerProgress,
                        playerTimeLeft = playerTimeLeft,
                        bot = botOpponent,
                        isBotDone = isBotDone,
                        botProgress = botProgress,
                        botTimeLeft = botTimeLeft,
                        currentBasePoints = duelDisplayedPoints,
                        onBackPressed = { showDialog = true }
                    )
                } else {
                    QuizTopBar(
                        currentIndex = currentIndex,
                        totalQuestions = questions.size,
                        currentBasePoints = currentBasePoints,
                        isDuel = false,
                        onBackPressed = { showDialog = true }
                    )
                }
            },
            bottomBar = {
                if (isDuel) {
                    // Duel Mode Bottom Action / Status Bar
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .navigationBarsPadding(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isPlayerDone) {
                                // Locked status
                                Surface(
                                    color = if (isBotDone) Color(0xFFEFF6FF) else Color(0xFFF0FDF4),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, if (isBotDone) Color(0xFFBFDBFE) else Color(0xFFBBF7D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (!isBotDone) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = Color(0xFF10B981),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Jawaban disimpan! Menunggu ${botOpponent.name}...",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D),
                                                fontFamily = interFontFamily
                                            )
                                        } else {
                                            val isRoundEnd = DuelRoundHelper.isRoundEndQuestion(currentIndex, questions.size) || isPlayerSurrendered
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = Color(0xFF2563EB),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isRoundEnd) {
                                                    if (transitionCountdown > 0) "Membuka papan skor (${transitionCountdown}s)..." else "Membuka papan skor..."
                                                } else {
                                                    if (transitionCountdown > 0) "Soal berikutnya (${transitionCountdown}s)..." else "Soal berikutnya..."
                                                },
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E40AF),
                                                fontFamily = interFontFamily
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Player still thinking
                                val canLock = when (currentQuestionType) {
                                    QuestionType.SHORT_ANSWER -> typedAnswer.value.isNotBlank()
                                    else -> selectedAnswers.value.isNotEmpty()
                                }

                                Button(
                                    onClick = {
                                        if (canLock) {
                                            if (currentQuestionType == QuestionType.SHORT_ANSWER) {
                                                lockPlayerAnswer(emptyList(), typedAnswer.value)
                                            } else {
                                                lockPlayerAnswer(selectedAnswers.value.toList(), null)
                                            }
                                        }
                                    },
                                    enabled = canLock,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2563EB),
                                        disabledContainerColor = Color(0xFFE2E8F0),
                                        contentColor = Color.White,
                                        disabledContentColor = Color(0xFF94A3B8)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = if (canLock) Color.White else Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "Jawab",
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFontFamily,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val progressColor = when {
                            animatedProgress.value > 0.6f -> Color(0xFF10B981)
                            animatedProgress.value > 0.3f -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress.value)
                                .clip(RoundedCornerShape(3.dp))
                                .background(progressColor)
                            )
                        }

                        BottomQuizNav(
                            selectedAnswers = selectedAnswers.value.toList(),
                            userTextAnswer = typedAnswer.value,
                            timeLeft = timer,
                            onTriggerScorestreak = { streakState ->
                                scoreState = streakState
                                snackbarTrigger++
                                onTriggerScorestreak(streakState)
                            },
                            powerUpIcons = powerUpIcons,
                            powerUpColors = powerUpColors,
                            currentIndex = currentIndex,
                            totalQuestions = questions.size,
                            isDuel = false,
                            onNextClick = {
                                if (currentIndex < questions.lastIndex) {
                                    viewModel.nextQuestion()
                                } else {
                                    onFinishQuiz(botOpponent.id, false)
                                }
                            },
                            viewModel = viewModel,
                            onFinishQuiz = { onFinishQuiz(botOpponent.id, false) },
                            materialId = materialId,
                            onPowerUpClicked = { showPowerUpInfoDialog = true },
                            onAnsweredChange = { isAnswered = it },
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Question Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        // Question Type Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = when (question.type) {
                                            QuestionType.SHORT_ANSWER -> "Isian Singkat"
                                            QuestionType.CHECKBOX -> "Pilihan Ganda (Bisa Lebih Dari 1)"
                                            else -> "Pilihan Ganda"
                                        },
                                        fontSize = 11.sp,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E40AF)
                                    )
                                }
                            }
                        }

                        question.imageRes?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .clickable { showZoomDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = it),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_info),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Perbesar",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val hasFormatting = question.questionText.contains("$") ||
                                question.questionText.contains("**") ||
                                question.questionText.contains("_") ||
                                question.questionText.contains("![") ||
                                question.questionText.contains("[") ||
                                question.questionText.contains("* ") ||
                                question.questionText.contains("- ") ||
                                question.questionText.contains("+ ") ||
                                question.questionText.contains("•") ||
                                question.questionText.contains(Regex("""\d+[\.\)]\s"""))

                        if (hasFormatting) {
                            if (isReadyToRender) {
                                MathText(
                                    text = question.questionText,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = "left",
                                    fontSize = 14,
                                    onImageClick = { url ->
                                        zoomImageUrl = url
                                    }
                                )
                            } else {
                                QuestionContentSkeleton()
                            }
                        } else {
                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = interFontFamily,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Left,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Answer Options Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentQuestionType == QuestionType.SHORT_ANSWER) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                value = typedAnswer.value,
                                onValueChange = { 
                                    if (!isPlayerDone) {
                                        typedAnswer.value = it 
                                    }
                                },
                                enabled = !isPlayerDone,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Ketik jawabanmu di sini...", fontFamily = interFontFamily, color = Color(0xFF94A3B8)) },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                "Gunakan huruf atau angka yang tepat",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B),
                                fontFamily = interFontFamily
                            )
                        }
                    } else {
                        val optionLetters = listOf("A", "B", "C", "D", "E", "F")
                        question.choices.forEachIndexed { index, choice ->
                            val isSelected = selectedAnswers.value.contains(index)
                            val letter = optionLetters.getOrElse(index) { "${index + 1}" }

                            val surfaceColor = if (isSelected) Color(0xFFF0F7FF) else Color.White
                            val borderColor = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                            val textColor = if (isSelected) Color(0xFF1E40AF) else Color(0xFF1E293B)

                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = surfaceColor,
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                                    shadowElevation = if (isSelected) 1.dp else 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Option Letter Badge (A, B, C, D)
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = letter,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interFontFamily,
                                                    color = if (isSelected) Color.White else Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        val hasFormatting = choice.contains("$") ||
                                                choice.contains("**") ||
                                                choice.contains("_") ||
                                                choice.contains("![") ||
                                                choice.contains("[") ||
                                                choice.contains("* ") ||
                                                choice.contains("- ") ||
                                                choice.contains("+ ") ||
                                                choice.contains("•") ||
                                                choice.contains(Regex("""\d+[\.\)]\s"""))

                                        if (hasFormatting) {
                                            if (isReadyToRender) {
                                                MathText(
                                                    text = choice,
                                                    fontSize = 14,
                                                    textAlign = "left",
                                                    color = textColor,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            } else {
                                                ChoiceOptionSkeleton(
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = choice,
                                                fontSize = 14.sp,
                                                fontFamily = interFontFamily,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = textColor,
                                                textAlign = TextAlign.Left,
                                                lineHeight = 20.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                // Clickable Overlay across the entire card
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            if (isPlayerDone) return@clickable

                                            if (question.type == QuestionType.CHECKBOX) {
                                                val newSet = selectedAnswers.value.toMutableSet()
                                                if (isSelected) newSet.remove(index)
                                                else newSet.add(index)
                                                selectedAnswers.value = newSet
                                            } else {
                                                selectedAnswers.value = mutableSetOf(index)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showZoomDialog && imageRes != null) {
                ZoomableImageDialog(imageRes = imageRes, onDismiss = { showZoomDialog = false })
            }
        }

        ZoomableImageOverlay(
            imageUrl = zoomImageUrl,
            onDismiss = { zoomImageUrl = null }
        )

        if (isDuel) {
            DuelScoreboardOverlay(
                visible = showDuelOverlay,
                playerName = playerName,
                playerAvatarRes = AvatarHelper.getAvatarDrawableRes(userAvatar),
                playerFrameId = activeFrame,
                playerScore = currentPoints,
                bot = botOpponent,
                questions = questions,
                playerHistory = playerHistory,
                botHistory = botHistory,
                currentQuestionIndex = currentIndex,
                onNextQuestion = {
                    showDuelOverlay = false
                    showScoreSnackbar = false
                    duelDisplayedPoints = currentBasePoints
                    if (isPlayerSurrendered || currentIndex >= questions.lastIndex) {
                        val summary = botEngine.getSummary(questions, isSurrendered = isPlayerSurrendered)
                        viewModel.setBotDuelSummary(summary)
                        val uid = viewModel.getCurrentUserId() ?: userId
                        if (!isPlayerSurrendered) {
                            viewModel.saveQuizResult(uid, materialId)
                        }
                        onFinishQuiz(botOpponent.id, isPlayerSurrendered)
                    } else {
                        viewModel.nextQuestion()
                    }
                },
                isLastQuestion = isPlayerSurrendered || (currentIndex >= questions.lastIndex),
                isSurrendered = isPlayerSurrendered,
                scorestreakState = scoreState ?: scorestreakState,
                modifier = Modifier.zIndex(50f)
            )
        }

        if (!isDuel || !showDuelOverlay) {
            ScorestreakSnackbar(
                state = scoreState,
                visible = showScoreSnackbar,
                modifier = Modifier
                    .zIndex(120f)
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun DuelDualTopBar(
    currentIndex: Int,
    totalQuestions: Int,
    playerName: String,
    playerAvatarRes: Int,
    playerFrameId: String? = null,
    isPlayerDone: Boolean,
    playerProgress: Float,
    playerTimeLeft: Int,
    bot: BotOpponent,
    isBotDone: Boolean,
    botProgress: Float,
    botTimeLeft: Int,
    currentBasePoints: Int,
    onBackPressed: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Row: Surrender Button, Round Indicator, Base Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Surrender button
                Surface(
                    onClick = onBackPressed,
                    shape = CircleShape,
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFFE4E6)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Surrender",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFE11D48)
                        )
                    }
                }

                // Middle: Duel Round Pill
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "DUEL",
                            fontSize = 11.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        val questionsPerRound = DuelRoundHelper.getQuestionsPerRound(totalQuestions)
                        val totalRounds = DuelRoundHelper.getTotalRounds(totalQuestions)
                        val currentRound = DuelRoundHelper.getRoundIndex(currentIndex, totalQuestions) + 1

//                        Text(
//                            text = if (questionsPerRound > 1) {
//                                "Ronde $currentRound/$totalRounds • Soal ${currentIndex + 1}/$totalQuestions"
//                            } else {
//                                "Ronde $currentRound/$totalRounds"
//                            },
//                            fontSize = 11.sp,
//                            fontFamily = interFontFamily,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )

                        Text(
                            text = "Ronde $currentRound / $totalRounds",
                            fontSize = 12.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Right: Current Points Pill
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_poin),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AnimatedBasePts(value = currentBasePoints)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Progress & Avatar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- PLAYER SIDE (LEFT) ---
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatarWithFrame(
                                avatarRes = playerAvatarRes,
                                frameId = playerFrameId,
                                avatarSize = 34.dp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playerName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontFamily = interFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isPlayerDone) "✓ Selesai (${playerTimeLeft}s)" else "Menjawab (${playerTimeLeft}s)",
                                fontSize = 10.sp,
                                fontWeight = if (isPlayerDone) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPlayerDone) Color(0xFF10B981) else Color(0xFF2563EB),
                                fontFamily = interFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val pBarColor = when {
                        isPlayerDone -> Color(0xFF10B981)
                        playerProgress > 0.5f -> Color(0xFF3B82F6)
                        playerProgress > 0.25f -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(playerProgress)
                                .clip(CircleShape)
                                .background(pBarColor)
                        )
                    }
                }

                // --- VS CENTER PILL ---
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // --- BOT SIDE (RIGHT) ---
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = bot.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontFamily = interFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isBotDone) "✓ Selesai (${botTimeLeft}s)" else "Menjawab (${botTimeLeft}s)",
                                fontSize = 10.sp,
                                fontWeight = if (isBotDone) FontWeight.Bold else FontWeight.Medium,
                                color = if (isBotDone) Color(0xFFF43F5E) else Color(0xFF94A3B8),
                                fontFamily = interFontFamily
                            )
                        }

                        Box(
                            modifier = Modifier.size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatarWithFrame(
                                avatarRes = bot.avatarRes,
                                frameId = bot.frameId,
                                avatarSize = 34.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val bBarColor = when {
                        isBotDone -> Color(0xFF10B981)
                        botProgress > 0.5f -> Color(0xFFF43F5E)
                        botProgress > 0.25f -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(botProgress)
                                .align(Alignment.CenterEnd)
                                .clip(CircleShape)
                                .background(bBarColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.quizShimmer(
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE2E8F0),
                    Color(0xFFF1F5F9),
                    Color(0xFFE2E8F0)
                ),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 300f, translateAnim + 300f)
            )
        )
}

@Composable
fun OnlineQuizSkeletonScreen() {
    Scaffold(
        containerColor = Color(0xFFF8F9FE),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .quizShimmer(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(24.dp)
                        .quizShimmer(RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(32.dp)
                        .quizShimmer(RoundedCornerShape(16.dp))
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .quizShimmer(RoundedCornerShape(16.dp))
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Hint Banner Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .quizShimmer(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question Card Skeleton
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(18.dp)
                            .quizShimmer(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(18.dp)
                            .quizShimmer(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(18.dp)
                            .quizShimmer(RoundedCornerShape(4.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options Skeleton
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { idx ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .quizShimmer(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (idx % 2 == 0) 0.75f else 0.55f)
                                    .height(16.dp)
                                    .quizShimmer(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionContentSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(16.dp)
                .quizShimmer(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .height(16.dp)
                .quizShimmer(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(16.dp)
                .quizShimmer(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun ChoiceOptionSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .height(16.dp)
            .quizShimmer(RoundedCornerShape(4.dp))
    )
}
