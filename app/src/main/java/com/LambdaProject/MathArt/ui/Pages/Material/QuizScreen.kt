package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ViewModels.QuizViewModel
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ui.components.MathText

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onQuizFinished: () -> Unit,
    userId: String,
    materialId: String,
    currentPage: Int,
    myPage: Int
) {
    val currentQuestion = viewModel.currentQuestion
    val totalQuestions = viewModel.totalQuestions
    val currentIndex = viewModel.currentQuestionIndex
    val selectedAnswer = viewModel.selectedAnswers
    val isAnswerChecked = viewModel.isAnswerChecked
    val lastAnswerIsCorrect = viewModel.lastAnswerIsCorrect
    val scrollState = rememberScrollState()

    var showZoomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentPage) {
        if (currentPage != myPage) {
            YouTubePlayerManager.pauseAll()
        }
    }

    LaunchedEffect(totalQuestions) {
        if (totalQuestions > 0) {
            viewModel.loadCorrectAnswers()
        }
    }

    LaunchedEffect(currentIndex) {
        scrollState.scrollTo(0)
    }

    val isQuizReady by viewModel.isQuizReady

    if (!isQuizReady) {
        QuizScreenSkeleton()
        return
    }

    val isComplex = currentQuestion.questionType.equals("complex", ignoreCase = true) ||
            currentQuestion.type.equals("complex", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. Top Card: Progress & Instruction ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Question Number Pill
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                    ) {
                        Text(
                            text = "SOAL ${currentIndex + 1} DARI $totalQuestions",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = Color(0xFF1E40AF),
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            fontSize = 11.sp
                        )
                    }

                    // Question Type Pill
                    Surface(
                        color = if (isComplex) Color(0xFFF3E8FF) else Color(0xFFECFDF5),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isComplex) Color(0xFFE9D5FF) else Color(0xFFA7F3D0))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isComplex) Icons.Default.Check else Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isComplex) Color(0xFF7E22CE) else Color(0xFF047857)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isComplex) "Pilihan Ganda Kompleks" else "Pilihan Tunggal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = if (isComplex) Color(0xFF6B21A8) else Color(0xFF065F46)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                val rawInstruction = currentQuestion.instruction.ifBlank {
                    "Pilih jenis transformasi yang terjadi pada motif batik tersebut!"
                }
                val instHasMath = remember(rawInstruction) {
                    rawInstruction.contains("$") || rawInstruction.contains("\\") || rawInstruction.contains("![")
                }

                if (instHasMath) {
                    MathText(
                        text = rawInstruction,
                        fontSize = 14,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        onImageClick = { showZoomDialog = true }
                    )
                } else {
                    Text(
                        text = remember(rawInstruction) { parseFormattedMarkdown(rawInstruction) },
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // --- 2. Batik Image Card with Zoom Option ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clickable { showZoomDialog = true },
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (currentQuestion.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = currentQuestion.imageUrl,
                        contentDescription = "Gambar Batik",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (currentQuestion.imageResId != 0) {
                    Image(
                        painter = painterResource(id = currentQuestion.imageResId),
                        contentDescription = "Gambar Batik",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Gambar tidak tersedia", fontFamily = interFontFamily, color = Color.Gray)
                    }
                }

                // Zoom hint overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Ketuk untuk Perbesar",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- 3. Options Header ---
        Text(
            text = if (isComplex) "Pilih Jawabanmu (Bisa lebih dari 1):" else "Pilih Jawabanmu:",
            fontFamily = interFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(start = 2.dp)
        )

        // --- 4. Options List with Immediate Feedback Highlights ---
        val options = if (currentQuestion.options.isNotEmpty()) {
            currentQuestion.options
        } else {
            listOf("Translasi", "Refleksi", "Rotasi", "Dilatasi")
        }

        val correctAnswersLower = remember(currentQuestion) {
            currentQuestion.correctAnswers.map { it.trim().lowercase() }.toSet()
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { optionText ->
                val optionLower = optionText.trim().lowercase()
                val isSelected = selectedAnswer.any { it.trim().equals(optionText.trim(), ignoreCase = true) }
                val isCorrectAnswer = correctAnswersLower.contains(optionLower)

                // Status styling after check
                val (surfaceColor, borderColor, contentColor, badgeText, badgeColor) = when {
                    !isAnswerChecked -> {
                        if (isSelected) {
                            Tuple5(Color(0xFFEFF6FF), Color(0xFF2563EB), Color(0xFF1E40AF), null, Color.Transparent)
                        } else {
                            Tuple5(Color.White, Color(0xFFE2E8F0), Color(0xFF334155), null, Color.Transparent)
                        }
                    }
                    isCorrectAnswer -> {
                        // Kunci Jawaban (hijau)
                        Tuple5(Color(0xFFDCFCE7), Color(0xFF16A34A), Color(0xFF15803D), "Kunci Jawaban", Color(0xFF15803D))
                    }
                    isSelected && !isCorrectAnswer -> {
                        // Pilihan user tapi salah (merah)
                        Tuple5(Color(0xFFFFE4E6), Color(0xFFE11D48), Color(0xFFBE123C), "Pilihanmu (Salah)", Color(0xFFBE123C))
                    }
                    else -> {
                        // Tidak dipilih & bukan jawaban (dimmed)
                        Tuple5(Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFF94A3B8), null, Color.Transparent)
                    }
                }

                Surface(
                    onClick = {
                        if (!isAnswerChecked) {
                            viewModel.toggleAnswer(optionText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = surfaceColor,
                    border = BorderStroke(if (isSelected || (isAnswerChecked && isCorrectAnswer)) 1.8.dp else 1.dp, borderColor),
                    shadowElevation = if (isSelected && !isAnswerChecked) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Selection / Status Icon Box
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(if (isComplex) RoundedCornerShape(6.dp) else CircleShape)
                                .background(
                                    when {
                                        isAnswerChecked && isCorrectAnswer -> Color(0xFF16A34A)
                                        isAnswerChecked && isSelected && !isCorrectAnswer -> Color(0xFFE11D48)
                                        isSelected -> Color(0xFF2563EB)
                                        else -> Color.White
                                    }
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = when {
                                        isAnswerChecked && isCorrectAnswer -> Color(0xFF16A34A)
                                        isAnswerChecked && isSelected && !isCorrectAnswer -> Color(0xFFE11D48)
                                        isSelected -> Color(0xFF2563EB)
                                        else -> Color(0xFFCBD5E1)
                                    },
                                    shape = if (isComplex) RoundedCornerShape(6.dp) else CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAnswerChecked) {
                                if (isCorrectAnswer) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            } else if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Option Label with LaTeX & Formatting Detection
                        val optHasMath = remember(optionText) {
                            optionText.contains("$") || optionText.contains("\\") || optionText.contains("![")
                        }

                        if (optHasMath) {
                            MathText(
                                text = optionText,
                                fontSize = 15,
                                color = contentColor,
                                fontWeight = if (isSelected || (isAnswerChecked && isCorrectAnswer)) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                text = remember(optionText) { parseFormattedMarkdown(optionText) },
                                style = TextStyle(
                                    fontFamily = interFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected || (isAnswerChecked && isCorrectAnswer)) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Status Badge
                        if (badgeText != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeColor.copy(alpha = 0.12f),
                                border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Immediate Answer Feedback Banner (Like OnlineQuizMaster) ---
        AnimatedVisibility(
            visible = isAnswerChecked,
            enter = fadeIn() + expandVertically()
        ) {
            val bannerBg = if (lastAnswerIsCorrect) Color(0xFFDCFCE7) else Color(0xFFFFE4E6)
            val bannerBorder = if (lastAnswerIsCorrect) Color(0xFF86EFAC) else Color(0xFFFDA4AF)
            val bannerTitle = if (lastAnswerIsCorrect) "Luar Biasa, Jawabanmu Benar!" else "Ups, Jawabanmu Belum Tepat"
            val bannerTitleColor = if (lastAnswerIsCorrect) Color(0xFF15803D) else Color(0xFFBE123C)
            val correctListString = currentQuestion.correctAnswers.joinToString(", ")

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = bannerBg,
                border = BorderStroke(1.dp, bannerBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(if (lastAnswerIsCorrect) Color(0xFF16A34A) else Color(0xFFE11D48), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (lastAnswerIsCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = bannerTitle,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = bannerTitleColor
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    val rawFeedback = when {
                        currentQuestion.explanation.isNotBlank() -> currentQuestion.explanation
                        lastAnswerIsCorrect -> "Motif batik di atas memang menunjukkan penerapan transformasi: $correctListString."
                        else -> "Transformasi yang tepat untuk motif batik di atas adalah: $correctListString."
                    }
                    val fbHasMath = remember(rawFeedback) {
                        rawFeedback.contains("$") || rawFeedback.contains("\\") || rawFeedback.contains("![")
                    }

                    if (fbHasMath) {
                        MathText(
                            text = rawFeedback,
                            fontSize = 13,
                            color = if (lastAnswerIsCorrect) Color(0xFF14532D) else Color(0xFF881337)
                        )
                    } else {
                        Text(
                            text = remember(rawFeedback) { parseFormattedMarkdown(rawFeedback) },
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (lastAnswerIsCorrect) Color(0xFF14532D) else Color(0xFF881337),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // --- 6. Action Button (Periksa Jawaban -> Soal Berikutnya / Selesai) ---
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Button(
                onClick = {
                    if (!isAnswerChecked) {
                        viewModel.checkCurrentAnswer()
                    } else {
                        if (currentIndex < totalQuestions - 1) {
                            viewModel.nextQuestion()
                        } else {
                            viewModel.finishQuiz(userId, materialId) {
                                updateAccessiblePage(userId, myPage + 1)
                                onQuizFinished()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(if (selectedAnswer.isNotEmpty() || isAnswerChecked) 6.dp else 0.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        !isAnswerChecked -> Color(0xFF2563EB)
                        currentIndex >= totalQuestions - 1 -> Color(0xFF16A34A)
                        else -> Color(0xFF2563EB)
                    },
                    disabledContainerColor = Color(0xFFE2E8F0)
                ),
                enabled = selectedAnswer.isNotEmpty() || isAnswerChecked
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when {
                            !isAnswerChecked -> "Periksa Jawaban"
                            currentIndex >= totalQuestions - 1 -> "Selesai & Lihat Hasil"
                            else -> "Soal Berikutnya"
                        },
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (!isAnswerChecked) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Progres Evaluasi",
                        fontFamily = interFontFamily,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        "${currentIndex + 1} / $totalQuestions",
                        fontFamily = interFontFamily,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFE2E8F0)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    // Zoom Dialog for Image
    if (showZoomDialog) {
        Dialog(onDismissRequest = { showZoomDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentQuestion.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = currentQuestion.imageUrl,
                            contentDescription = "Zoom Gambar Batik",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else if (currentQuestion.imageResId != 0) {
                        Image(
                            painter = painterResource(id = currentQuestion.imageResId),
                            contentDescription = "Zoom Gambar Batik",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { showZoomDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Tutup", fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
