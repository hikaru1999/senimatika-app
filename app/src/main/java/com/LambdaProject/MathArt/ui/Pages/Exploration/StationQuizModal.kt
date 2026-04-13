package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    question: Question,
    onAnswer: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = { if (showSuccess) onClose() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .heightIn(max = 500.dp)
                ) {
                    // Header Area
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = Color(0xFFE8EAF6),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "UNIQUE CHALLENGE",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A237E),
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Final Security Check",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = Color(0xFF1A237E)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Question Content (Scrollable)
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState)
                    ) {
                        if (question.text.contains("$")) {
                            MathText(
                                text = question.text,
                                color = Color.Black,
                                fontSize = 18,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = question.text,
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                lineHeight = 26.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Options
                        question.options.forEachIndexed { index, option ->
                            val isCorrect = index == question.correctAnswer
                            val isSelected = selectedOption == index
                            
                            val backgroundColor = when {
                                isAnswered && isCorrect -> Color(0xFFE8F5E9)
                                isAnswered && isSelected && !isCorrect -> Color(0xFFFFEBEE)
                                isSelected -> Color(0xFFE3F2FD)
                                else -> Color.White
                            }
                            
                            val borderColor = when {
                                isAnswered && isCorrect -> Color(0xFF4CAF50)
                                isAnswered && isSelected && !isCorrect -> Color.Red
                                isSelected -> Color(0xFF1976D2)
                                else -> Color(0xFFEEEEEE)
                            }

                            Surface(
                                onClick = {
                                    if (!isAnswered) {
                                        selectedOption = index
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = backgroundColor,
                                border = BorderStroke(2.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    if (option.contains("$")) {
                                        MathText(
                                            text = option,
                                            color = Color.Black,
                                            fontSize = 16,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        Text(
                                            text = option,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected || (isAnswered && isCorrect)) Color(0xFF1A237E) else Color.Gray,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                isAnswered = true
                                if (selectedOption == question.correctAnswer) {
                                    showSuccess = true
                                    onAnswer(true)
                                } else {
                                    onAnswer(false)
                                    // Give feedback before closing or allow retry?
                                    // For now, consistent with existing logic
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        enabled = !isAnswered && selectedOption != null
                    ) {
                        Text("SUBMIT JAWABAN", fontWeight = FontWeight.Bold)
                    }
                }

                // Success Overlay
                AnimatedVisibility(
                    visible = showSuccess,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Surface(
                        modifier = Modifier.matchParentSize(),
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Selamat!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A237E)
                            )
                            Text(
                                text = "Station berhasil dibuka. Kamu mendapatkan 500 koin!",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = onClose,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("LANJUTKAN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
