package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.LambdaProject.MathArt.data.repository.SessionRepository.markSessionCompleted
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ViewModels.QuizViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SummaryScreen(viewModel: QuizViewModel, onBackToDashboard: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val db = remember { FirebaseFirestore.getInstance() }
    val userAnswers = viewModel.userAnswers
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (userId != null) {
            viewModel.loadQuizResults(userId, "transformasi_geometri") {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FE))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFFFF9C4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_task_completed),
                        contentDescription = "Completed",
                        modifier = Modifier.size(70.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Luar Biasa!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
                Text(
                    text = "Kamu telah menyelesaikan materi ini",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = interFontFamily,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Ringkasan Belajar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val sections = listOf(
                    "Materi: Pengantar", "Materi: Translasi", "Materi: Refleksi",
                    "Materi: Rotasi", "Materi: Dilatasi", "Kuis Transformasi"
                )
                
                sections.forEach { section ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = section,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF455A64)
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Detail Jawaban Kuis",
                    fontWeight = FontWeight.Black,
                    fontFamily = interFontFamily,
                    fontSize = 18.sp,
                    color = Color(0xFF1A237E)
                )
                
                if (userAnswers.isEmpty()) {
                    Text(
                        text = "Belum ada kuis yang dikerjakan",
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    userAnswers.forEachIndexed { index, answer ->
                        Surface(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFF8F9FE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()) {
                                Text(
                                    "Soal Batik ${index + 1}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A237E)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Kamu: ${answer.selectedAnswers.joinToString()}",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontFamily = interFontFamily
                                )
                                Text(
                                    "Kunci: ${answer.correctAnswers.joinToString()}",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text(
                "Selesai & Keluar",
                color = Color.White,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Konfirmasi", fontWeight = FontWeight.Black, fontFamily = interFontFamily) },
            text = { Text("Progress materi akan di-reset agar kamu bisa belajar kembali nanti. Yakin ingin keluar?", fontFamily = interFontFamily) },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (userId != null) {
                        db.collection("quizResults")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("materialId", "transformasi_geometri")
                            .get()
                            .addOnSuccessListener { documents ->
                                val batch = db.batch()
                                for (document in documents) {
                                    batch.delete(document.reference)
                                }
                                batch.commit().addOnSuccessListener {
                                    viewModel.resetQuiz()
                                    resetAccessiblePage(userId)
                                    markSessionCompleted(
                                        userId = userId,
                                        materialId = "transformasi_geometri"
                                    )
                                    onBackToDashboard()
                                    viewModel.resetQuiz()
                                    viewModel.resetQuizReadyState()
                                }
                            }
                            .addOnFailureListener {
                                onBackToDashboard()
                            }

                    } else {
                        onBackToDashboard()
                    }
                }) {
                    Text("Ya, Keluar", fontWeight = FontWeight.Black, color = Color(0xFFC62828))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        )
    }
}
