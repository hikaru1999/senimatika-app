package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import android.annotation.SuppressLint
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
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
import com.LambdaProject.MathArt.data.model.ScoreSoundManager
import com.LambdaProject.MathArt.data.powerUpColors
import com.LambdaProject.MathArt.data.powerUpIcons
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.data.model.QuestionType
import com.LambdaProject.MathArt.data.model.ScorestreakState
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ui.components.MathText
import com.LambdaProject.MathArt.utils.ZoomableImageOverlay
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
    LaunchedEffect(materialId, userId) {
        viewModel.loadQuizForSelectedMaterial(userId, materialId)
    }

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val currentBasePoints by viewModel.currentBasePoints.collectAsState()
    val scorestreakState by viewModel.scorestreakState.collectAsState()
    val context = LocalContext.current

    if (questions.isEmpty() || currentIndex >= questions.size) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1976D2), strokeWidth = 3.dp)
        }
        return
    }

    val question = questions[currentIndex]
    val totalQuestions = questions.size

    val selectedAnswers = remember(question.questionNumber) { mutableStateOf(mutableSetOf<Int>()) }
    val typedAnswer = remember(question) { mutableStateOf("") }
    val animatedProgress = remember { Animatable(1f) }

    val imageRes = question.imageRes

    var timer by remember(question.questionNumber) { mutableIntStateOf(question.durationSeconds) }
    var isAnswered by remember { mutableStateOf(false) }
    var isReadyToRender by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    var showScoreSnackbar by remember { mutableStateOf(false) }
    var scoreState by remember { mutableStateOf<ScorestreakState?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showPowerUpInfoDialog by remember { mutableStateOf(false) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    var showZoomDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val currentQuestionType = question.type

    BackHandler(enabled = zoomImageUrl != null) {
        zoomImageUrl = null
    }

    LaunchedEffect(key1 = currentIndex) {
        isAnswered = false
        isReadyToRender = false

        delay(500)
        isReadyToRender = true

        val durationMs = (question.durationSeconds * 1000).toLong()
        val startTime = SystemClock.elapsedRealtime()
        val endTime = startTime + durationMs

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
                                showScoreSnackbar = true
                            }
                        )
                    }
                    if (currentIndex < questions.lastIndex) {
                        delay(600)
                        viewModel.nextQuestion()
                    } else if (!isNavigating /* currentIndex == totalQuestions - 1 */) {
                        isNavigating = true
                        delay(1000)
                        navController.navigate("OnlineQuizResult/${materialId}/${userId}") {
                            popUpTo("OnlineQuizMaster") { inclusive = true }
                        }
                    }
                    break
                }
                delay(100L)
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
            title = { Text("Keluar dari Kuis?", fontFamily = interFontFamily, fontWeight = FontWeight.Black, color = Color(0xFF1A237E)) },
            text = { Text("Jika kamu keluar kuis maka progresmu akan hilang. Yakin ingin keluar?", fontFamily = interFontFamily, textAlign = TextAlign.Justify) },
            confirmButton = {
                Button(
                    onClick = {
                        isLoading = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(2000)
                            onBackPressed()
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text("Ya, Keluar", fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", fontFamily = interFontFamily, color = Color.Gray, fontWeight = FontWeight.Bold)
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
            /*.consumeWindowInsets(WindowInsets.ime) */
    ) {
        ScorestreakSnackbar(
            state = scoreState,
            visible = showScoreSnackbar,
            modifier = Modifier
                .zIndex(10f)
                .padding(top = 80.dp)
                .align(Alignment.TopCenter)
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                QuizTopBar(
                    currentIndex = currentIndex,
                    totalQuestions = questions.size,
                    currentBasePoints = currentBasePoints,
                    onBackPressed = { showDialog = true }
                )
            },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Time Indicator Bar (Modernized)
                    val progressColor = when {
                        animatedProgress.value > 0.6f -> Color(0xFF66BB6A)
                        animatedProgress.value > 0.3f -> Color(0xFFFFB300)
                        else -> Color(0xFFEF5350)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFFEEEEEE))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress.value)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(progressColor.copy(alpha = 0.7f), progressColor)
                                    )
                                )
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
                            if (currentIndex < questions.lastIndex) {
                                viewModel.nextQuestion()
                            } else {
                                onFinishQuiz()
                            }
                        },
                        viewModel = viewModel,
                        onFinishQuiz = onFinishQuiz,
                        materialId = materialId,
                        onPowerUpClicked = { showPowerUpInfoDialog = true },
                        onAnsweredChange = { isAnswered = it },
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lightbulb, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (question.type == QuestionType.SHORT_ANSWER) "Isi jawabanmu dengan teliti!" else "Pilih satu atau lebih jawaban yang benar",
                            fontSize = 11.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }

                // Question Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        question.imageRes?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .clickable { showZoomDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = it),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                                // Zoom hint overlay
                                Surface(
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_info),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp).size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        val hasFormatting = question.questionText.contains("$") ||
                                question.questionText.contains("**") ||
                                question.questionText.contains("_") ||
                                question.questionText.contains("![") ||
                                question.questionText.contains("[") ||
                                question.questionText.contains("* ")

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
                                Box(Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                }
                            }
                        } else {
                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    /* fontWeight = FontWeight.Bold, */
                                    fontFamily = interFontFamily,
                                    lineHeight = 18.sp
                                ),
                                color = Color(0xFF1A237E),
                                textAlign = TextAlign.Left,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Answer Options Section
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentQuestionType == QuestionType.SHORT_ANSWER) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            OutlinedTextField(
                                value = typedAnswer.value,
                                onValueChange = { typedAnswer.value = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Jawaban Singkat") },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1976D2),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Gunakan huruf atau angka yang tepat",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontFamily = interFontFamily
                            )
                        }
                    } else {
                        question.choices.forEachIndexed { index, choice ->
                            val isSelected = selectedAnswers.value.contains(index)
                            
                            val surfaceColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                            val borderColor = if (isSelected) Color(0xFF1976D2) else Color(0xFFEEEEEE)
                            val textColor = if (isSelected) Color(0xFF1976D2) else Color(0xFF1A237E)

                            Surface(
                                onClick = {
                                    if (question.type == QuestionType.CHECKBOX) {
                                        val newSet = selectedAnswers.value.toMutableSet()
                                        if (isSelected) newSet.remove(index)
                                        else newSet.add(index)
                                        selectedAnswers.value = newSet
                                    } else {
                                        selectedAnswers.value = mutableSetOf(index)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = surfaceColor,
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                                shadowElevation = if (isSelected) 4.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (question.type == QuestionType.CHECKBOX) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1976D2))
                                        )
                                    } else {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = null,
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1976D2))
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    val hasFormatting = choice.contains("$") ||
                                            choice.contains("**") ||
                                            choice.contains("_") ||
                                            choice.contains("![") ||
                                            choice.contains("[") ||
                                            choice.contains("* ")

                                    if (hasFormatting) {
                                        if (isReadyToRender) {
                                            MathText(
                                                text = choice,
                                                fontSize = 14,
                                                textAlign = "left",
                                                color = textColor,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .pointerInteropFilter {
                                                        false
                                                    }
                                            )
                                        } else {
                                            Box(Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = choice,
                                            fontSize = 14.sp,
                                            fontFamily = interFontFamily,
                                            /* fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, */
                                            color = textColor,
                                            textAlign = TextAlign.Left,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showZoomDialog && imageRes != null) {
                ZoomableImageDialog(imageRes = imageRes, onDismiss = { showZoomDialog = false })
            }
        }
        ZoomableImageOverlay(
            imageUrl = zoomImageUrl,
            onDismiss = { zoomImageUrl = null }
        )
    }
}
