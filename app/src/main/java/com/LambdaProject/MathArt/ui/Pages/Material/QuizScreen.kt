package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ViewModels.QuizViewModel
import com.LambdaProject.MathArt.data.model.unlockProAchievement

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
    val scrollState = rememberScrollState()
    var isReady by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        viewModel.initializeQuiz(userId, materialId) {
            isReady = true
        }
    }

    if (!isReady || totalQuestions == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF5294FF), strokeWidth = 3.dp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Instruction Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF0F4FF),
            border = BorderStroke(1.dp, Color(0xFF5294FF).copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF5294FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "SOAL ${currentIndex + 1}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Pilih jenis transformasi yang terjadi pada motif batik tersebut!",
                    fontFamily = interFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E),
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Question Image Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Image(
                painter = painterResource(id = currentQuestion.imageResId),
                contentDescription = "Gambar Batik",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Options Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Pilih Jawabanmu:",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color(0xFF1A237E),
                modifier = Modifier.padding(start = 4.dp)
            )

            listOf("Translasi", "Rotasi", "Refleksi", "Dilatasi").forEach { label ->
                val isSelected = selectedAnswer.contains(label)
                ModernQuizOption(
                    label = label,
                    selected = isSelected,
                    onClick = { viewModel.toggleAnswer(label) }
                )
            }
        }

        // Action Button & Progress
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(
                onClick = {
                    if (!viewModel.isFinished) {
                        viewModel.submitAnswer(userId, "transformasi_geometri")
                    } else {
                        viewModel.submitAnswer(userId, "transformasi_geometri")
                        updateAccessiblePage(userId, 6)
                        onQuizFinished()
                        unlockProAchievement(userId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                enabled = selectedAnswer.isNotEmpty()
            ) {
                Text(
                    text = if (currentIndex == totalQuestions - 1) "Lihat Hasil" else "Soal Berikutnya",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            // Progress Indicator
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Progress",
                        fontFamily = interFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        "${currentIndex + 1} / $totalQuestions",
                        fontFamily = interFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ModernQuizOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) Color(0xFF5294FF) else Color(0xFFF8F9FE)
    val contentColor = if (selected) Color.White else Color(0xFF1A237E)
    val borderColor = if (selected) Color.Transparent else Color(0xFFCFD8DC).copy(alpha = 0.5f)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (selected) Color.White.copy(alpha = 0.2f) else Color.White)
                    .border(2.dp, if (selected) Color.White else Color(0xFFCFD8DC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = interFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
            )
        }
    }
}
