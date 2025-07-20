package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
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
import com.LambdaProject.MathArt.model.unlockProAchievement

@Composable
fun QuizScreen(viewModel: QuizViewModel, onQuizFinished: () -> Unit, userId: String, materialId: String, currentPage: Int, myPage: Int,) {
    /* val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid */
    val question = viewModel.currentQuestion
    val currentQuestion = viewModel.currentQuestion
    val totalQuestions = viewModel.totalQuestions
    val currentIndex = viewModel.currentQuestionIndex
    val selectedAnswer = viewModel.selectedAnswers
    val isFinished = viewModel.isFinished
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
        scrollState.scrollTo(0)  // Scroll ke atas
    }

    LaunchedEffect(Unit) {
        viewModel.initializeQuiz(userId, materialId) {
            isReady = true
        }
    }

    if (!isReady || totalQuestions == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F9FE))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = ParagraphStyle(
                                textAlign = TextAlign.Justify,
                                lineHeight = 20.sp
                            )
                        ) {
                            append("Perhatikan motif batik di bawah ini. Pilih jenis transformasi yang terjadi pada motif batik tersebut!")
                        }
                    },
                    fontFamily = interFontFamily,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            Image(
                painter = painterResource(id = currentQuestion.imageResId),
                contentDescription = "Gambar Batik",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pilih Jawabanmu!",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        listOf("Translasi", "Rotasi", "Refleksi", "Dilatasi").forEach { label ->
            QuizCheckbox(
                label = label,
                checked = selectedAnswer.contains(label),
                onCheckedChange = { viewModel.toggleAnswer(label) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
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
                .height(50.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            enabled = selectedAnswer.isNotEmpty()
        ) {
            Text(
                text = if (currentIndex == totalQuestions - 1) "Lihat Hasil" else "Soal Berikutnya",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF2E7D32),
        )
    }
}

@Composable
fun QuizCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = Color(0xFFF0F0F0),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF1976D2)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = interFontFamily
            )
        }
    }
}