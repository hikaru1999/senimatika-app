package com.LambdaProject.MathArt.ui.Screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.UserPreferences
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.launch

@Composable
fun IntroScreen(navController: NavController, userPreferences: UserPreferences) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) {
        2
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> IntroPage(
                        imageRes = R.drawable.img_logo_wo_name,
                        title = "Selamat Datang di Senimatika!",
                        description = "Platform ini akan membantu Anda belajar matematika dengan pendekatan seni dan budaya (etnomatematika).\n\n\n\n",
                        showButton = false,
                        onStartClick = {}
                    )
                    1 -> IntroPage(
                        imageRes = R.drawable.ic_start_explorer,
                        title = "Mari Mulai!",
                        description = "Jelajahi materi dan latihan menarik di dalam aplikasi.",
                        showButton = true,
                        onStartClick = {
                            coroutineScope.launch {
                                userPreferences.setIntroShown(true)
                                navController.navigate("login") {
                                    popUpTo("intro") { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
        DotsIndicator(currentPage = pagerState.currentPage, totalPages = 2, modifier = Modifier.align(
            Alignment.BottomCenter))
    }
}

@Composable
fun IntroPage(imageRes: Int, title: String, description: String, showButton: Boolean, onStartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 75.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Home Image",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 22.sp,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = description,
            fontSize = 16.sp,
            fontFamily = interFontFamily,
            textAlign = TextAlign.Justify,
            color = Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (showButton) {
            Spacer(modifier = Modifier.height(46.dp))
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Mulai", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun DotsIndicator(currentPage: Int, totalPages: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalPages) { index ->
            val alpha by animateFloatAsState(
                targetValue = if (index == currentPage) 1f else 0.5f,
                animationSpec = tween(durationMillis = 300),
                label = "dotAlpha"
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF3490DE).copy(alpha = alpha), shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}