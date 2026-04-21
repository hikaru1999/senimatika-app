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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    var showFailure by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Theme Colors (Inspired by BossQuizModal)
    val darkBg = Color(0xFF1A1A1A)
    val accentRed = Color(0xFFD32F2F)
    val accentCyan = Color(0xFF00E5FF)
    val surfaceWhite = Color.White.copy(alpha = 0.05f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = darkBg,
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .heightIn(max = 600.dp)
                ) {
                    // Header Area - Modern Cyber Style
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentRed.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = accentRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STATION SECURITY CHALLENGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = accentRed,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Buka Akses",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question Content (Scrollable)
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = surfaceWhite,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                if (question.text.contains("$")) {
                                    MathText(
                                        text = question.text,
                                        color = Color.White,
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
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Options
                        question.options.forEachIndexed { index, option ->
                            val isCorrect = index == question.correctAnswer
                            val isSelected = selectedOption == index

                            val backgroundColor = when {
                                isAnswered && isCorrect && showSuccess -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                                isAnswered && isSelected && !isCorrect -> accentRed.copy(alpha = 0.2f)
                                isSelected -> accentRed.copy(alpha = 0.8f)
                                else -> surfaceWhite
                            }

                            val borderColor = when {
                                isAnswered && isCorrect && showSuccess -> Color(0xFF4CAF50)
                                isAnswered && isSelected && !isCorrect -> accentRed
                                isSelected -> Color.White
                                else -> Color.White.copy(alpha = 0.1f)
                            }

                            Surface(
                                onClick = {
                                    if (!isAnswered) {
                                        selectedOption = index
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = backgroundColor,
                                border = BorderStroke(if(isSelected || (isAnswered && (isSelected || (isCorrect && showSuccess)))) 2.dp else 1.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    if (option.contains("$")) {
                                        MathText(
                                            text = option,
                                            color = Color.White,
                                            fontSize = 13,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        Text(
                                            text = option,
                                            textAlign = TextAlign.Center,
                                            fontSize = 13.sp,
                                            fontFamily = interFontFamily,
                                            color = if (isSelected || (isAnswered && isCorrect && showSuccess)) Color.White else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentRed,
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        enabled = !isAnswered && selectedOption != null
                    ) {
                        Text(
                            text = "AKSES AREA",
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Success Overlay - Modernized
                AnimatedVisibility(
                    visible = showSuccess,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Surface(
                        modifier = Modifier.matchParentSize(),
                        color = darkBg.copy(alpha = 0.98f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Subtle glow effect
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(accentCyan.copy(alpha = 0.1f), CircleShape)
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = accentCyan,
                                    modifier = Modifier.size(80.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "STATION UNLOCKED",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = interFontFamily,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Keamanan berhasil ditembus.\nBonus +500 koin telah ditambahkan!",
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            Button(
                                onClick = onClose,
                                colors = ButtonDefaults.buttonColors(containerColor = accentCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(
                                    "MASUK KE STATION",
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }
                }

                // Failure Overlay (Auto-close after delay)
                AnimatedVisibility(
                    visible = showFailure,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    LaunchedEffect(Unit) {
                        delay(2000)
                        onClose()
                    }
                    Surface(
                        modifier = Modifier.matchParentSize(),
                        color = darkBg.copy(alpha = 0.98f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(100.dp).background(accentRed.copy(alpha = 0.1f), CircleShape))
                                Icon(Icons.Default.Warning, null, tint = accentRed, modifier = Modifier.size(80.dp))
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("AKSES DITOLAK", fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = interFontFamily, color = accentRed, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Jawaban salah. Protokol keamanan diaktifkan.\nSistem terkunci selama 10 detik.", textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
