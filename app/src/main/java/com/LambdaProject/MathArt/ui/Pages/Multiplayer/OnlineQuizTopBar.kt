package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun QuizTopBar(
    currentIndex: Int,
    totalQuestions: Int,
    currentBasePoints: Int,
    onBackPressed: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { 
                Text(
                    "Keluar dari Kuis?", 
                    fontFamily = interFontFamily, 
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A237E)
                ) 
            },
            text = { 
                Text(
                    "Jika kamu keluar kuis maka progresmu akan hilang. Yakin ingin keluar?", 
                    fontFamily = interFontFamily, 
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                ) 
            },
            confirmButton = {
                Button(
                    onClick = onBackPressed,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya, Keluar", fontFamily = interFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", fontFamily = interFontFamily, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.size(40.dp).background(Color(0xFFF5F5F5), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF1A237E)
                )
            }
            /* Surface(
                onClick = { showDialog = true },
                color = Color(0xFFF5F5F5),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF1A237E)
                    )
                }
            } */

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Pertanyaan ",
                        fontSize = 11.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${currentIndex + 1}",
                        fontSize = 13.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A237E)
                    )
                    Text(
                        text = " dari $totalQuestions",
                        fontSize = 11.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }

                val progress by animateFloatAsState(
                    targetValue = (currentIndex + 1).toFloat() / totalQuestions.coerceAtLeast(1),
                    animationSpec = tween(durationMillis = 400 /* ,easing = LinearOutSlowInEasing */),
                    label = "quizProgress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp)
                        .background(Color(0xFFF0F0F0), CircleShape)
                        .clip(CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
                                ),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Surface(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFBBDEFB).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_poin),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AnimatedBasePts(value = currentBasePoints)
                }
            }
        }
    }
}

@Composable
private fun AnimatedBasePts(value: Int) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(value) {
        animatedValue.animateTo(
            targetValue = value.toFloat(),
            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = "${animatedValue.value.toInt()}",
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Black
        ),
        fontFamily = interFontFamily,
        fontSize = 14.sp,
        color = Color(0xFF1976D2)
    )
}
