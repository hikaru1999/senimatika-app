package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
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
            title = { Text("Keluar dari Kuis?", fontFamily = interFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("Jika kamu keluar kuis maka progresmu akan hilang. Yakin ingin keluar?", fontFamily = interFontFamily, textAlign = TextAlign.Justify) },
            confirmButton = {
                TextButton(onClick = onBackPressed) {
                    Text("Ya", fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", fontFamily = interFontFamily, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Navigation Arrow
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
            ) {
                IconButton(
                    onClick = { showDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(25.dp),
                        tint = Color.Gray
                    )
                }
            }
        }
        // Progress Bar and Number of Question Indicator
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth(0.5f)
                            .background(Color.LightGray, shape = RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((currentIndex + 1).toFloat() / totalQuestions)
                                .background(Color(0xFF0288D1), shape = RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        "${currentIndex + 1}/$totalQuestions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(end = 15.dp)
                .width(75.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF9EDCFF)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AnimatedBasePts(
                    value = currentBasePoints
                )

                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_poin),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
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
            animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = "${animatedValue.value.toInt()}",
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        fontFamily = interFontFamily,
        fontSize = 14.sp,
        color = Color.White
    )
}