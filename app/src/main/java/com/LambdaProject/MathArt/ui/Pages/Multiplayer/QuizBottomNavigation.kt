package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import android.content.Context
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.ScorestreakState
import kotlinx.coroutines.*

@Composable
fun BottomQuizNav(
    selectedAnswers: List<Int>,
    userTextAnswer: String? = null,
    timeLeft: Int,
    onTriggerScorestreak: (ScorestreakState) -> Unit,
    powerUpIcons: List<Int>,
    powerUpColors: List<Color>,
    currentIndex: Int,
    totalQuestions: Int,
    onNextClick: () -> Unit,
    onFinishQuiz: () -> Unit,
    onPowerUpClicked: () -> Unit,
    viewModel: OnlineQuizViewModel,
    materialId: String
) {
    var isLoading by remember { mutableStateOf(false) }
    var isAnswered by remember { mutableStateOf(false) }

    val context = LocalContext.current

    BottomNavigation(
        backgroundColor = Color.White,
        elevation = 8.dp,
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            /* Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .shadow(4.dp, CircleShape)
                            .background(powerUpColors[index], CircleShape)
                            .clickable { onPowerUpClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = powerUpIcons[index]),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } */
            // Power-up
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(40.dp) // beri ruang untuk badge di luar lingkaran
                            .clickable { onPowerUpClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        // Lingkaran utama power-up
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .shadow(4.dp, CircleShape)
                                .background(powerUpColors[index], CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = powerUpIcons[index]),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp) // posisi di luar lingkaran
                                .background(Color.DarkGray, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Terkunci",
                                tint = Color.White,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    isAnswered = true

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2000)
                        viewModel.checkAnswer(
                            selectedAnswers = selectedAnswers,
                            userTextAnswer = userTextAnswer,
                            timeLeft = timeLeft,
                            onScoreUpdated = { points, coins ->

                            },
                            onStreakUpdate = { streakState ->
                                onTriggerScorestreak(streakState)
                            }
                        )

                        Log.d("QuizDebug", "currentIndex: $currentIndex | totalQuestions: $totalQuestions")

                        if (currentIndex == totalQuestions - 1) {
                            delay(1250)

                            val userId = viewModel.getCurrentUserId() ?: return@launch
                            Log.d("QuizDebug", "Trying to save: userId=$userId, materialId=$materialId")
                            viewModel.saveQuizResult(userId, materialId)
                            onFinishQuiz()
                        }
                        onNextClick()
                        isLoading = false
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E60DD)),
                enabled = selectedAnswers.isNotEmpty() || !userTextAnswer.isNullOrBlank()
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .widthIn(min = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Text(
                            text = if (currentIndex < totalQuestions - 1) "Cek Jawabanmu" else "Selesai & Lihat Hasil",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}