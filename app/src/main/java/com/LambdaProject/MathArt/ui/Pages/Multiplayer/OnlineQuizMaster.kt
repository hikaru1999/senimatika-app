package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import android.annotation.SuppressLint
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.Data.ScoreSoundManager
import com.LambdaProject.MathArt.Data.powerUpColors
import com.LambdaProject.MathArt.Data.powerUpIcons
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.*
import kotlinx.coroutines.*

@SuppressLint("MutableCollectionMutableState")
@Composable
fun OnlineQuizMaster(
    onFinishQuiz: () -> Unit,
    navController: NavController,
    onNextClick: () -> Unit,
    onBackPressed: () -> Unit,
    onTriggerScorestreak: (ScorestreakState) -> Unit,
    materialId: String,
    userId: String,
    viewModel: OnlineQuizViewModel = hiltViewModel()
) {
    LaunchedEffect(materialId) {
        viewModel.loadQuizForSelectedMaterial(userId, materialId)
    }

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val currentBasePoints by viewModel.currentBasePoints.collectAsState()
    val context = LocalContext.current
    val scorestreakState by viewModel.scorestreakState.collectAsState()

    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val question = questions[currentIndex]
    val selectedAnswers = remember(question.questionNumber) { mutableStateOf(mutableSetOf<Int>()) }
    val animatedProgress = remember { Animatable(1f) }
    val typedAnswer = remember(question) { mutableStateOf("") }
    val totalQuestions = questions.size
    val imageRes = question.imageRes

    var timer by remember(question.questionNumber) { mutableIntStateOf(question.durationSeconds) }
    var showScoreSnackbar by remember { mutableStateOf(false) }
    var showZoomDialog by remember { mutableStateOf(false) }
    var scoreState by remember { mutableStateOf<ScorestreakState?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showPowerUpInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = currentIndex) {
        isAnswered = false
        val startTime = SystemClock.elapsedRealtime()
        val durationMs = question.durationSeconds * 1000
        val endTime = startTime + durationMs
        animatedProgress.snapTo(1f)

        launch {
            while (true) {
                val now = SystemClock.elapsedRealtime()
                val timeLeftMs = (endTime - now).coerceAtLeast(0L)
                val progress = timeLeftMs / durationMs.toFloat()
                animatedProgress.snapTo(progress)
                timer = (timeLeftMs / 1000).toInt()

                if (isAnswered || timeLeftMs <= 0L) {
                    if (timeLeftMs <= 0L && !isAnswered) {
                        isAnswered = true
                        val finalAnswer = selectedAnswers.value.toList()
                        viewModel.checkAnswer(
                            selectedAnswers = finalAnswer,
                            userTextAnswer = typedAnswer.toString(),
                            timeLeft = 0,
                            onScoreUpdated = { _, _ -> },
                            onStreakUpdate = { streakState ->
                                scoreState = streakState
                                showScoreSnackbar = true
                            }
                        )
                    }
                    if (currentIndex < questions.lastIndex) {
                        viewModel.nextQuestion()
                    } else if (currentIndex == totalQuestions - 1) {
                        delay(1000)
                        navController.navigate("OnlineQuizResult/${materialId}/${userId}") {
                            popUpTo("OnlineQuizMaster") {
                                inclusive = true
                            }
                        }
                    }
                    break
                }
                delay(16L)
            }
        }
    }

    LaunchedEffect(showScoreSnackbar) {
        if (showScoreSnackbar) {
            ScoreSoundManager.playSound(context, scorestreakState!!.type)
            delay(3000)
            showScoreSnackbar = false
        }
    }

    BackHandler(enabled = true) {
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Keluar dari Kuis?", fontFamily = interFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("Jika kamu keluar kuis maka progresmu akan hilang. Yakin ingin keluar?", fontFamily = interFontFamily, textAlign = TextAlign.Justify) },
            confirmButton = {
                TextButton(onClick = onBackPressed) {
                    Text("Ya", fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", fontFamily = interFontFamily, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPowerUpInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPowerUpInfoDialog = false },
            title = {
                Text(
                    text = "Power Up",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily
                )
            },
            text = {
                Text(
                    text = "Power Up masih dalam tahap pengembangan. Harap bersabar ya dan stay tuned!",
                    fontFamily = interFontFamily
                )
            },
            confirmButton = {
                TextButton(onClick = { showPowerUpInfoDialog = false }) {
                    Text(
                        text = "OK",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFDBF1FF),
                        Color(0xFFB3E5FC),
                        Color(0xFFE1F5FE)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .consumeWindowInsets(WindowInsets.ime)
    ) {
        ScorestreakSnackbar(
            state = scoreState,
            visible = showScoreSnackbar,
            modifier = Modifier
                .zIndex(1f)
                .padding(top = 20.dp)
                .align(Alignment.TopCenter)
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                QuizTopBar(
                    currentIndex = currentIndex,
                    totalQuestions = questions.size,
                    currentBasePoints = currentBasePoints,
                    onBackPressed = { navController.popBackStack() }
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val progressColor = when {
                            animatedProgress.value > 0.6f -> Color(0xFF4CAF50)
                            animatedProgress.value > 0.3f -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color(0xFFBDBDBD), RoundedCornerShape(0.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress.value)
                                    .align(Alignment.CenterEnd)
                                    .background(progressColor, RoundedCornerShape(0.dp))
                            )
                        }
                        BottomQuizNav(
                            selectedAnswers = selectedAnswers.value.toList(),
                            userTextAnswer = typedAnswer.value,
                            timeLeft = timer,
                            onTriggerScorestreak = { streakState ->
                                scoreState = streakState
                                showScoreSnackbar = true
                            },
                            powerUpIcons = powerUpIcons,
                            powerUpColors = powerUpColors,
                            currentIndex = currentIndex,
                            totalQuestions = questions.size,
                            onNextClick = {
                                Log.d("onNextClick", "Material aktif saat ini: $materialId")
                                if (currentIndex < questions.lastIndex) {
                                    viewModel.nextQuestion()
                                } else {
                                    onFinishQuiz()
                                }
                            },
                            viewModel = viewModel,
                            onFinishQuiz = onFinishQuiz,
                            materialId = materialId,
                            onPowerUpClicked = { showPowerUpInfoDialog = true }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                    ) {
                        question.imageRes?.let {
                            Column(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0XFFE8F9FE), shape = RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Informasi",
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ketuk gambar untuk melakukan pembesaran",
                                            fontFamily = interFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFF2BA2FF)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Image(
                                painter = painterResource(id = it),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clickable { showZoomDialog = true }
                            )
                        }
                        Text(
                            text = question.questionText,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (showZoomDialog && imageRes != null) {
                        ZoomableImageDialog(imageRes = imageRes, onDismiss = { showZoomDialog = false })
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (question.type == QuestionType.SHORT_ANSWER) "Ayo isi jawabanmu!" else "Ayo pilih jawabanmu!",
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        if (question.type == QuestionType.SHORT_ANSWER) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                            ) {
                                OutlinedTextField(
                                    value = typedAnswer.value,
                                    onValueChange = { typedAnswer.value = it },
                                    label = { Text("Jawabanmu", fontFamily = interFontFamily) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, bottom = 18.dp, start = 16.dp, end = 16.dp)
                                        .background(Color.White, shape = MaterialTheme.shapes.medium),
                                )
                            }
                        } else {
                            question.choices.forEachIndexed { index, choice ->
                                val isSelected = selectedAnswers.value.contains(index)
                                val isUnanswered = !isSelected

                                val backgroundBrush = if (isSelected) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFF1FAF0),
                                            Color(0xFFB9F6CA),
                                            Color(0xFFADEEBE)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(400f, 400f)
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF),
                                            Color(0xFFF5F5F5)
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .background(brush = backgroundBrush, shape = MaterialTheme.shapes.medium)
                                        .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
                                        .clickable {
                                            if (question.type == QuestionType.CHECKBOX) {
                                                val newSet = selectedAnswers.value.toMutableSet()
                                                if (isSelected) newSet.remove(index)
                                                else newSet.add(index)
                                                selectedAnswers.value = newSet
                                            } else {
                                                selectedAnswers.value = mutableSetOf(index)
                                            }
                                        }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (question.type == QuestionType.CHECKBOX) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = {
                                                    val newSet = selectedAnswers.value.toMutableSet()
                                                    if (isSelected) newSet.remove(index)
                                                    else newSet.add(index)
                                                    selectedAnswers.value = newSet
                                                },
                                            )
                                        } else {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedAnswers.value = mutableSetOf(index)
                                                },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = Color.Black,
                                                    unselectedColor = Color.Gray,
                                                    disabledSelectedColor = Color.LightGray
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = choice,
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                lineHeight = 15.sp
                                            ),
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}