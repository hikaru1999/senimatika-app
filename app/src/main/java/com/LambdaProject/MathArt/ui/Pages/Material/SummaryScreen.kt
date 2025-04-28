package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.LambdaProject.MathArt.Data.SessionRepository.markSessionCompleted
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ViewModels.QuizViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SummaryScreen(viewModel: QuizViewModel, onBackToDashboard: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val userAnswers = viewModel.userAnswers
    val correctAnswers = viewModel.correctAnswers
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (userId != null) {
            viewModel.loadQuizResults(userId, "transformasi_geometri") {
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0xff5E9DFF),
                    shape = RoundedCornerShape(10.dp)
                ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xffFFFFFF)
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_task_completed),
                    contentDescription = "Task Completed",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Kamu telah menyelesaikan materi ini",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 2.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ringkasan Belajar",
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    fontSize = 24.sp
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                    .fillMaxWidth(),
            ) {
                val sections = listOf(
                    "Materi: Pengantar",
                    "Materi: Translasi",
                    "Materi: Refleksi",
                    "Materi: Rotasi",
                    "Materi: Dilatasi",
                    "Kuis Transformasi Geometri"
                )
                sections.forEachIndexed { index, section ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = section,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green
                        )
                    }

                    if (index != sections.lastIndex) {
                        Spacer(modifier = Modifier.height(5.dp))
                        HorizontalDivider(thickness = 1.dp)
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(thickness = 2.dp)
                Text(
                    "Detail Jawaban",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
                if (userAnswers.isEmpty()) {
                    Text(
                        text = "Belum ada kuis yang dikerjakan",
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp, )
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        userAnswers.forEachIndexed { index, answer ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Soal Batik ${index + 1}", fontWeight = FontWeight.Bold)
                            Text(
                                "Jawaban Kamu: ${answer.selectedAnswers.joinToString()}", color = Color.Gray, fontFamily = interFontFamily
                                )
                            Text(
                                "Jawaban Benar: ${answer.correctAnswers.joinToString()}", color = Color(0xFF388E3C), fontFamily = interFontFamily
                                )
                            /* Text(
                                text = if (answer.isCorrect) "Benar" else "Salah",
                                color = if (answer.isCorrect) Color(0xFF388E3C) else Color(0xFFF44336),
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily
                            ) */
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Kembali ke Dashboard", color = Color.White, fontFamily = interFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(text = "Reset Progress", fontFamily = interFontFamily, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(text = "Apakah kamu yakin ingin kembali ke dashboard? Progress materi akan di-reset.", fontFamily = interFontFamily, textAlign = TextAlign.Justify)
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            if (userId != null) {
                                viewModel.resetQuiz()
                                resetAccessiblePage(userId)
                                markSessionCompleted(userId = userId, materialId = "transformasi_geometri")
                            }
                            onBackToDashboard()
                        }) {
                            Text("Ya", fontFamily = interFontFamily)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Batal", fontFamily = interFontFamily, color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}