package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun ExtractionIntroAnimation() {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }

    val slideDown by animateDpAsState(
        targetValue = if (startAnim) 0.dp else (-200).dp,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "slideDown"
    )
    val slideUp by animateDpAsState(
        targetValue = if (startAnim) 0.dp else 200.dp,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "slideUp"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(1000),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "EXTRACTION",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                fontFamily = interFontFamily,
                letterSpacing = 8.sp,
                modifier = Modifier.offset(y = slideDown)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SUCCESS",
                color = Color(0xFFFFD600),
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
                fontFamily = interFontFamily,
                modifier = Modifier.offset(y = slideUp)
            )
        }
    }
}
