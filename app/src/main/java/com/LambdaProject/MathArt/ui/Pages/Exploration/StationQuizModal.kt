package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.LambdaProject.MathArt.data.Question
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText
import kotlinx.coroutines.delay

@Composable
fun StationQuizModal(
    question: Question?,
    cooldownEndMillis: Long = 0L,
    onAnswer: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showFailure by remember { mutableStateOf(false) }

    var typedAnswer by remember(question) { mutableStateOf("") }


    val penaltyDisplay = remember { (3..5).random() }
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
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = paperBg,
            border = BorderStroke(3.dp, woodBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
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
                            Column(modifier = Modifier.padding(20.dp).heightIn(max = 600.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = woodBorder) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                            Icon(Icons.Default.Lock, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("FINAL CHALLENGE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                    }
                                    /* Text("Buka Akses Area", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = darkText) */
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(scrollState)) {
                                    Surface(modifier = Modifier.fillMaxWidth(), color = innerPaper, shape = RoundedCornerShape(16.dp)) {
                                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                            val hasFormatting = question.text.contains("$") ||
                                                    question.text.contains("**") ||
                                                    question.text.contains("_") ||
                                                    question.text.contains("![") ||
                                                    question.text.contains("[") ||
                                                    question.text.contains("* ")

                                            if (hasFormatting) {
                                                if(!isEquationReady) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = Color(0xFF5D4037).copy(alpha = 0.4f),
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                                MathText(
                                                    text = question.text,
                                                    color = darkText,
                                                    textAlign = "left",
                                                    fontSize = 14,
                                                    onRenderComplete = { isEquationReady = true }
                                                )
                                            } else {
                                                Text(
                                                    text = question.text,
                                                    textAlign = TextAlign.Left,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    lineHeight = 20.sp,
                                                    color = darkText
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    if (question.questionType == "short_answer") {
                                        // TAMPILAN ISIAN SINGKAT
                                        OutlinedTextField(
                                            value = typedAnswer,
                                            onValueChange = {
                                                typedAnswer = it
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            placeholder = { Text("Ketik jawabanmu di sini...", color = Color(0xFF5D4037).copy(alpha = 0.5f)) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF5D4037),
                                                unfocusedBorderColor = Color(0xFF5D4037).copy(alpha = 0.3f),
                                                focusedContainerColor = Color(0xFFFDF8E1),
                                                unfocusedContainerColor = Color(0xFFFDF8E1)
                                            ),
                                            singleLine = true
                                        )
                                    } else {
                                        question.options.forEachIndexed { index, option ->
                                            var isEquationReady by remember(option) { mutableStateOf(false) }

                                            val isSelected = selectedOption == index

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 10.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (isSelected) Color(0xFFD3C5B9) else Color(0xFFFDF8E1),
                                                    border = BorderStroke(
                                                        if (isSelected) 2.dp else 1.dp,
                                                        if (isSelected) woodBorder else woodBorder.copy(alpha = 0.2f)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        val hasFormatting = option.contains("$") ||
                                                                option.contains("**") ||
                                                                option.contains("_") ||
                                                                option.contains("![") ||
                                                                option.contains("[") ||
                                                                option.contains("* ")
                                                        if (hasFormatting) {
                                                            if (!isEquationReady) {
                                                                CircularProgressIndicator(
                                                                    modifier = Modifier.size(24.dp),
                                                                    color = Color(0xFF5D4037).copy(alpha = 0.4f),
                                                                    strokeWidth = 2.dp
                                                                )
                                                            }
                                                            MathText(
                                                                text = option,
                                                                color = darkText,
                                                                textAlign = "left",
                                                                fontSize = 13,
                                                                onRenderComplete = { isEquationReady = true }
                                                            )
                                                        } else {
                                                            Text(
                                                                text = option,
                                                                textAlign = TextAlign.Left,
                                                                fontSize = 13.sp,
                                                                lineHeight = 15.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = darkText,
                                                                modifier = Modifier.fillMaxWidth()
                                                            )
                                                        }
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .matchParentSize()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable(enabled = !isAnswered) {
                                                            selectedOption = index
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

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
                                            showSuccess = true
                                            onAnswer(true)
                                        } else {
                                            showFailure = true
                                            onAnswer(false)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = woodBorder),
                                    enabled = !isAnswered && (selectedOption != null || (question.questionType == "short_answer" && typedAnswer.isNotBlank()))
                                ) {
                                    Text("KONFIRMASI JAWABAN", fontWeight = FontWeight.Black, color = Color.White)
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
                                color = woodBorder
                            )
                            Text(
                                text = "Sistem sedang memulihkan diri setelah kegagalan akses sebelumnya. Mohon tunggu:",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 13.sp,
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
                                Text("KEMBALI", fontWeight = FontWeight.Bold)
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
                            Text("AREA TERBUKA!", fontSize = 22.sp, fontWeight = FontWeight.Black, color = darkText)
                            Text("Akses wilayah kini telah terbuka. Selamat menjelajah!", textAlign = TextAlign.Center, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = onClose,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("MENGERTI", fontWeight = FontWeight.Bold)
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
                                color = Color(0xFFB71C1C)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Jawaban Anda salah.\nArea ini akan terkunci selama",
                                textAlign = TextAlign.Center,
                                color = darkText,
                                fontSize = 14.sp,
                                fontFamily = interFontFamily
                            )
                            Text(
                                text = "$penaltyDisplay MENIT",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB71C1C),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            Spacer(Modifier.height(24.dp))

                            // Visual Timer Penalti
                            /* Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { timerCount / 10f },
                                    modifier = Modifier.size(60.dp),
                                    color = Color(0xFFB71C1C),
                                    strokeWidth = 6.dp,
                                    strokeCap = StrokeCap.Round
                                )
                                Text(timerCount.toString(), fontWeight = FontWeight.Black, color = Color(0xFFB71C1C))
                            }
                            Text("Mohon tunggu...", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp)) */
                        }
                    }
                }

            }

            /* Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .heightIn(max = 600.dp)
                ) {
                    // Header Area - RPG Label Style
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = woodBorder,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STATION CHALLENGE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Text(
                            text = "Buka Akses Area",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = darkText,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = innerPaper,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, woodBorder.copy(alpha = 0.3f))
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                if (question.text.contains("$")) {
                                    MathText(
                                        text = question.text,
                                        color = darkText,
                                        fontSize = 13,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = question.text,
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        color = darkText,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        question.options.forEachIndexed { index, option ->
                            val isSelected = selectedOption == index

                            val backgroundColor = when {
                                isAnswered && index == question.correctAnswer && showSuccess -> Color(0xFFC8E6C9) // Sukses Hijau Muda
                                isSelected -> Color(0xFFD3C5B9)
                                else -> Color(0xFFFDF8E1)
                            }

                            val borderColor = when {
                                isAnswered && index == question.correctAnswer && showSuccess -> Color(0xFF2E7D32)
                                isSelected -> woodBorder
                                else -> woodBorder.copy(alpha = 0.2f)
                            }

                            Surface(
                                onClick = { if (!isAnswered) selectedOption = index },
                                shape = RoundedCornerShape(12.dp),
                                color = backgroundColor,
                                border = BorderStroke(if(isSelected) 2.dp else 1.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    if (option.contains("$")) {
                                        MathText(
                                            text = option,
                                            color = darkText,
                                            fontSize = 13,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        Text(
                                            text = option,
                                            textAlign = TextAlign.Center,
                                            fontSize = 13.sp,
                                            fontFamily = interFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = darkText,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                isAnswered = true
                                if (selectedOption == question.correctAnswer) {
                                    showSuccess = true
                                    onAnswer(true)
                                } else {
                                    showFailure = true
                                    onAnswer(false)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = woodBorder,
                            disabledContainerColor = woodBorder.copy(alpha = 0.5f)
                        ),
                        enabled = !isAnswered && selectedOption != null
                    ) {
                        Text(
                            text = "KONFIRMASI JAWABAN",
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }

                AnimatedVisibility(visible = showSuccess, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        modifier = Modifier.matchParentSize(),
                        color = paperBg.copy(alpha = 0.98f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "AKSES DIBERIKAN",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = darkText
                            )
                            Text(
                                "Area berhasil dibuka.\nBonus +500 koin didapatkan!",
                                textAlign = TextAlign.Center,
                                color = darkText.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = onClose,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("LANJUTKAN", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showFailure, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        modifier = Modifier.matchParentSize(),
                        color = paperBg.copy(alpha = 0.98f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning, // Ikon Peringatan
                                contentDescription = null,
                                tint = Color(0xFFB71C1C), // Merah Marun
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "JAWABAN SALAH",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB71C1C),
                                fontFamily = interFontFamily
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Akses ditolak. Gerbang terkunci sementara.",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = darkText,
                                fontFamily = interFontFamily
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Indikator Visual Hitung Mundur
                            /* Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { timerCount / 10f },
                                    modifier = Modifier.size(60.dp),
                                    color = Color(0xFFB71C1C),
                                    strokeWidth = 6.dp,
                                    strokeCap = StrokeCap.Round,
                                    trackColor = woodBorder.copy(alpha = 0.1f)
                                )
                                Text(
                                    text = timerCount.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB71C1C)
                                )
                            }

                            Text(
                                text = "Mohon tunggu...",
                                modifier = Modifier.padding(top = 12.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            ) */
                        }
                    }
                }
            } */
        }
    }
}
