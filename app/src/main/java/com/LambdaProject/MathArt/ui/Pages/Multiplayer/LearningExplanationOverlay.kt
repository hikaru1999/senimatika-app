package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText

@Composable
fun LearningExplanationOverlay(
    visible: Boolean,
    isCorrect: Boolean,
    pembahasan: String?,
    isLastQuestion: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200)),
        modifier = modifier.zIndex(150f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* Consume taps outside to prevent accidental dismiss */ },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(250)
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    border = BorderStroke(
                        1.dp,
                        if (isCorrect) Color(0xFFBBF7D0) else Color(0xFFFECDD3)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Header Status Pill
                            Surface(
                                color = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFFE4E6),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isCorrect) Color(0xFF86EFAC) else Color(0xFFFDA4AF))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                if (isCorrect) Color(0xFF16A34A) else Color(0xFFE11D48),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isCorrect) "Luar Biasa, Jawabanmu Benar!" else "Ups, Jawabanmu Belum Tepat",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = interFontFamily,
                                        color = if (isCorrect) Color(0xFF15803D) else Color(0xFFBE123C)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Explanation Title Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pembahasan Solusi",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            // Explanation Card
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    val content = pembahasan?.takeIf { it.isNotBlank() }
                                        ?: "Tidak ada pembahasan yang tersedia untuk soal ini."
                                    MathText(
                                        text = content,
                                        fontSize = 14,
                                        color = Color(0xFF1E293B),
                                        textAlign = "left",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Continue Button
                        Button(
                            onClick = onContinue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCorrect) Color(0xFF16A34A) else Color(0xFF2563EB)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = if (isLastQuestion) "Selesai & Lihat Hasil" else "Paham, Lanjut",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = interFontFamily,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
