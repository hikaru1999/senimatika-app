package com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay

@Composable
fun BattleActionOverlay(text: String, subText: String) {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(text) {
        startAnim = false
        delay(50)
        startAnim = true
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1.2f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "textScale"
    )

    val mainColor = when {
        text.contains("ATTACK") || text.contains("BLOCKED") || text.contains("SUKSES") -> Color.Cyan
        text.contains("BOSS") || text.contains("COUNTER") || text.contains("MISSED") -> Color.Red
        else -> Color.White
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            AnimatedContent(
                targetState = text,
                transitionSpec = {
                    (slideInHorizontally { -it } + fadeIn(tween(500)))
                        .togetherWith(slideOutHorizontally { it } + fadeOut(tween(250)))
                },
                label = "MainTextAnim"
            ) { targetText ->
                Text(
                    text = targetText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = mainColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- ANIMASI FLOAT IN UNTUK SUB-TEKS ---
            AnimatedContent(
                targetState = subText,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn(tween(500, delayMillis = 150)))
                        .togetherWith(slideOutHorizontally { -it } + fadeOut(tween(250)))
                },
                label = "SubTextAnim"
            ) { targetSub ->
                if (targetSub.isNotEmpty()) {
                    Text(
                        text = targetSub,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        fontFamily = interFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}