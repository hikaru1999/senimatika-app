package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.LambdaProject.MathArt.data.Question
import com.LambdaProject.MathArt.data.model.AppAudioManager
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText
import com.LambdaProject.MathArt.utils.ZoomableImageOverlay
import kotlinx.coroutines.delay

@Composable
fun StationQuizModal(
    question: Question?,
    cooldownEndMillis: Long = 0L,
    expReward: Int = 150,
    onAnswer: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showFailure by remember { mutableStateOf(false) }
    var typedAnswer by remember(question) { mutableStateOf("") }

    val penaltyDisplay = 1
    val scrollState = rememberScrollState()
    val paperBg = Color(0xFFF0E7D8)
    val woodBorder = Color(0xFF5D4037)
    val darkText = Color(0xFF3E2723)
    val innerPaper = Color(0xFFE5DCC3)

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val remaining = (cooldownEndMillis - currentTime).coerceAtLeast(0L)
    val secondsLeft = (remaining / 1000) % 60
    val minutesLeft = (remaining / 1000) / 60
    val timeFormatted = String.format("%02d:%02d", minutesLeft, secondsLeft)

    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = zoomImageUrl != null) {
        zoomImageUrl = null
    }

    LaunchedEffect(showFailure) {
        if (showFailure) {
            delay(2000)
            onClose()
        }
    }

    LaunchedEffect(cooldownEndMillis) {
        while (remaining > 0) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .systemBarsPadding()
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = paperBg,
            border = BorderStroke(3.dp, woodBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.86f)
        ) {
            AnimatedContent(
                targetState = when {
                    showSuccess -> "SUCCESS"
                    showFailure -> "FAILURE"
                    remaining > 0 -> "COOLDOWN"
                    else -> "QUIZ"
                },
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "ModalContent"
            ) { targetState ->
                when (targetState) {
                    "QUIZ" -> {
                        var isEquationReady by remember(question) { mutableStateOf(false) }
                        if (question != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 18.dp, start = 18.dp, end = 18.dp, bottom = 16.dp)
                            ) {
                                // Header
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = woodBorder) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Lock, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "TANTANGAN AKHIR",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                fontFamily = interFontFamily
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Scrollable Content (Question Box + Options)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(scrollState)
                                    ) {
                                        // Question Card
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = innerPaper,
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.5.dp, woodBorder.copy(alpha = 0.3f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(14.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = woodBorder.copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            text = "PERTANYAAN",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black,
                                                            fontFamily = interFontFamily,
                                                            color = woodBorder,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            letterSpacing = 0.5.sp
                                                        )
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    contentAlignment = Alignment.TopStart
                                                ) {
                                                    val hasFormatting = question.text.contains("$") ||
                                                            question.text.contains("**") ||
                                                            question.text.contains("_") ||
                                                            question.text.contains("![") ||
                                                            question.text.contains("[") ||
                                                            question.text.contains("* ") ||
                                                            question.text.contains("- ") ||
                                                            question.text.contains("+ ") ||
                                                            question.text.contains("•") ||
                                                            question.text.contains(Regex("""\d+[\.\)]\s"""))

                                                    if (hasFormatting) {
                                                        if (!isEquationReady) {
                                                            StationQuestionSkeleton()
                                                        }
                                                        MathText(
                                                            text = question.text,
                                                            color = darkText,
                                                            textAlign = "left",
                                                            fontSize = 14,
                                                            onRenderComplete = { isEquationReady = true },
                                                            onImageClick = { url -> zoomImageUrl = url }
                                                        )
                                                    } else {
                                                        Text(
                                                            text = question.text,
                                                            textAlign = TextAlign.Left,
                                                            fontSize = 13.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            lineHeight = 19.sp,
                                                            color = darkText,
                                                            fontFamily = interFontFamily
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Options or Short Answer Input
                                        if (question.questionType == "short_answer") {
                                            OutlinedTextField(
                                                value = typedAnswer,
                                                onValueChange = { typedAnswer = it },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                placeholder = { Text("Ketik jawabanmu di sini...", color = Color(0xFF5D4037).copy(alpha = 0.5f), fontFamily = interFontFamily) },
                                                shape = RoundedCornerShape(14.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = woodBorder,
                                                    unfocusedBorderColor = woodBorder.copy(alpha = 0.35f),
                                                    focusedContainerColor = Color(0xFFFDF8E1),
                                                    unfocusedContainerColor = Color(0xFFFDF8E1)
                                                ),
                                                singleLine = true
                                            )
                                        } else {
                                            val optionLetters = listOf("A", "B", "C", "D", "E", "F")
                                            val validOptions = remember(question.options) {
                                                question.options
                                                    .mapIndexed { originalIndex, option -> originalIndex to option }
                                                    .filter { it.second.isNotBlank() }
                                            }
                                            validOptions.forEachIndexed { displayIndex, (originalIndex, option) ->
                                                var isOptionEquationReady by remember(option) { mutableStateOf(false) }
                                                val isSelected = selectedOption == originalIndex
                                                val letter = optionLetters.getOrElse(displayIndex) { "${displayIndex + 1}" }

                                                val cardBg by animateColorAsState(
                                                    targetValue = if (isSelected) Color(0xFFEADBCE) else Color(0xFFFDF8E1),
                                                    animationSpec = tween(durationMillis = 200),
                                                    label = "stationOptionBg"
                                                )
                                                val cardBorder by animateColorAsState(
                                                    targetValue = if (isSelected) woodBorder else woodBorder.copy(alpha = 0.22f),
                                                    animationSpec = tween(durationMillis = 200),
                                                    label = "stationOptionBorder"
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 10.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(14.dp),
                                                        color = cardBg,
                                                        border = BorderStroke(
                                                            if (isSelected) 2.dp else 1.dp,
                                                            cardBorder
                                                        ),
                                                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            // Option Letter Badge (A, B, C, D)
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = if (isSelected) woodBorder else woodBorder.copy(alpha = 0.12f),
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Text(
                                                                        text = letter,
                                                                        fontSize = 12.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontFamily = interFontFamily,
                                                                        color = if (isSelected) Color(0xFFFFD700) else woodBorder
                                                                    )
                                                                }
                                                            }

                                                            // Option Content
                                                            Box(
                                                                modifier = Modifier.weight(1f),
                                                                contentAlignment = Alignment.CenterStart
                                                            ) {
                                                                val hasFormatting = option.contains("$") ||
                                                                        option.contains("**") ||
                                                                        option.contains("_") ||
                                                                        option.contains("![") ||
                                                                        option.contains("[") ||
                                                                        option.contains("* ") ||
                                                                        option.contains("- ") ||
                                                                        option.contains("+ ") ||
                                                                        option.contains("•") ||
                                                                        option.contains(Regex("""\d+[\.\)]\s"""))
                                                                if (hasFormatting) {
                                                                    if (!isOptionEquationReady) {
                                                                        StationOptionSkeleton()
                                                                    }
                                                                    MathText(
                                                                        text = option,
                                                                        color = darkText,
                                                                        textAlign = "left",
                                                                        fontSize = 13,
                                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        onRenderComplete = { isOptionEquationReady = true }
                                                                    )
                                                                } else {
                                                                    Text(
                                                                        text = option,
                                                                        textAlign = TextAlign.Left,
                                                                        fontSize = 13.sp,
                                                                        lineHeight = 18.sp,
                                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                        color = darkText,
                                                                        fontFamily = interFontFamily,
                                                                        modifier = Modifier.fillMaxWidth()
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    // Transparent touch interceptor overlay
                                                    Box(
                                                        modifier = Modifier
                                                            .matchParentSize()
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .clickable(enabled = !isAnswered) {
                                                                selectedOption = originalIndex
                                                            }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    // Floating Scroll Hint
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = scrollState.canScrollForward,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 6.dp)
                                    ) {
                                        Surface(
                                            color = woodBorder.copy(alpha = 0.92f),
                                            shape = RoundedCornerShape(20.dp),
                                            shadowElevation = 4.dp
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "Gulir ke bawah untuk opsi lainnya ↓",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interFontFamily,
                                                    color = Color(0xFFFFD700)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Divider
                                HorizontalDivider(
                                    color = woodBorder.copy(alpha = 0.15f),
                                    thickness = 1.dp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Sticky CTA Button
                                Button(
                                    onClick = {
                                        val isCorrect = if (question.questionType == "short_answer") {
                                            question.answerKey.any { key ->
                                                key.trim().equals(typedAnswer.trim(), ignoreCase = true)
                                            }
                                        } else {
                                            selectedOption == question.correctAnswer
                                        }
                                        isAnswered = true

                                        if (isCorrect) {
                                            AppAudioManager.playSfx("unlocked")
                                            showSuccess = true
                                            onAnswer(true)
                                        } else {
                                            showFailure = true
                                            onAnswer(false)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = woodBorder,
                                        disabledContainerColor = woodBorder.copy(alpha = 0.35f),
                                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 2.dp,
                                        pressedElevation = 0.dp
                                    ),
                                    enabled = !isAnswered && (selectedOption != null || (question.questionType == "short_answer" && typedAnswer.isNotBlank()))
                                ) {
                                    Text(
                                        text = "KONFIRMASI JAWABAN",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.5.sp,
                                        fontFamily = interFontFamily,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    "COOLDOWN" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp).fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = woodBorder,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "KEAMANAN AKTIF",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = woodBorder,
                                fontFamily = interFontFamily
                            )
                            val totalSeconds = (remaining / 1000).coerceAtLeast(1)
                            Text(
                                text = "Sistem keamanan aktif. Coba lagi dalam $totalSeconds detik",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Surface(
                                color = woodBorder.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, woodBorder)
                            ) {
                                Text(
                                    text = timeFormatted,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = woodBorder,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = onClose,
                                colors = ButtonDefaults.buttonColors(containerColor = woodBorder),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("KEMBALI", fontWeight = FontWeight.Bold, fontFamily = interFontFamily)
                            }
                        }
                    }

                    "SUCCESS" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp).fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("AREA TERBUKA!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = darkText, fontFamily = interFontFamily)
                            Text("Area kini telah terbuka. Selamat menjelajah dan dapatkah hadiahnya!", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 13.sp, fontFamily = interFontFamily, modifier = Modifier.padding(top = 8.dp))
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "+$expReward EXP",
                                    color = Color(0xFF5B21B6),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "Pending ekstraksi",
                                color = Color.Gray.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            Button(
                                onClick = onClose,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("MENGERTI", fontWeight = FontWeight.Bold, fontFamily = interFontFamily)
                            }
                        }
                    }

                    "FAILURE" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp).fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFB71C1C), modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Area Terkunci",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB71C1C),
                                fontFamily = interFontFamily
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Jawaban Anda salah.\nArea ini terkunci selama 1 menit.",
                                textAlign = TextAlign.Center,
                                color = darkText,
                                fontSize = 14.sp,
                                fontFamily = interFontFamily
                            )
                            Text(
                                text = "1 MENIT",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB71C1C),
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
        ZoomableImageOverlay(
            imageUrl = zoomImageUrl,
            onDismiss = { zoomImageUrl = null }
        )
    }
}

@Composable
fun Modifier.stationShimmer(shape: Shape = RoundedCornerShape(4.dp)): Modifier {
    val transition = rememberInfiniteTransition(label = "stationShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stationShimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF5D4037).copy(alpha = 0.08f),
                    Color(0xFF5D4037).copy(alpha = 0.20f),
                    Color(0xFF5D4037).copy(alpha = 0.08f)
                ),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 300f, translateAnim + 300f)
            )
        )
}

@Composable
fun StationQuestionSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(14.dp)
                .stationShimmer(RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .height(14.dp)
                .stationShimmer(RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.60f)
                .height(14.dp)
                .stationShimmer(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun StationOptionSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .height(14.dp)
            .stationShimmer(RoundedCornerShape(4.dp))
    )
}

