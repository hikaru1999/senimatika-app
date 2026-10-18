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
    isDuel: Boolean = false,
    onBackPressed: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.White,
        shadowElevation = 0.5.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Surface(
                onClick = onBackPressed,
                shape = CircleShape,
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF1E293B)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Progress Section
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Soal ",
                        fontSize = 11.5.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${currentIndex + 1}",
                        fontSize = 13.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = " dari $totalQuestions",
                        fontSize = 11.5.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                val progress by animateFloatAsState(
                    targetValue = (currentIndex + 1).toFloat() / totalQuestions.coerceAtLeast(1),
                    animationSpec = tween(durationMillis = 400),
                    label = "quizProgress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(5.dp)
                        .background(Color(0xFFF1F5F9), CircleShape)
                        .clip(CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                                ),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Base Points Badge
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFDBEAFE))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_poin),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    AnimatedBasePts(value = currentBasePoints)
                }
            }
        }
    }
}

@Composable
fun AnimatedBasePts(value: Int) {
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
            fontWeight = FontWeight.Bold
        ),
        fontFamily = interFontFamily,
        fontSize = 13.sp,
        color = Color(0xFF1E40AF)
    )
}
