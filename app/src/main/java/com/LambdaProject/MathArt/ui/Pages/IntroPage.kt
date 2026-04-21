package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    
    val backgroundColor = Color(0xFFFAFAFA)
    val primaryColor = Color(0xFF1E88E5)
    val textColor = Color(0xFF1A1A1A)
    val secondaryTextColor = Color(0xFF757575)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Decorative background element
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) { page ->
                when (page) {
                    0 -> IntroPageContent(
                        imageRes = R.drawable.img_logo_wo_name,
                        title = "Selamat Datang di Senimatika!",
                        description = "Platform inovatif belajar matematika melalui keindahan seni dan kekayaan budaya nusantara.",
                        primaryColor = primaryColor,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor
                    )
                    1 -> IntroPageContent(
                        imageRes = R.drawable.ic_start_explorer,
                        title = "Mari Mulai!",
                        description = "Jelajahi materi interaktif dan tantangan menarik yang dirancang khusus untuk Anda.",
                        primaryColor = primaryColor,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
            }

            // Bottom Navigation & Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DotsIndicator(
                    currentPage = pagerState.currentPage,
                    totalPages = 2,
                    activeColor = primaryColor
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage < 1) {
                                pagerState.animateScrollToPage(1)
                            } else {
                                userPreferences.setIntroShown(true)
                                navController.navigate("login") {
                                    popUpTo("intro") { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == 0) "Lanjutkan" else "Mulai Sekarang",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interFontFamily,
                        color = Color.White
                    )
                }
                
                if (pagerState.currentPage == 0) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                userPreferences.setIntroShown(true)
                                navController.navigate("login") {
                                    popUpTo("intro") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Lewati",
                            color = secondaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = interFontFamily
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(56.dp)) // Maintain layout height
                }
            }
        }
    }
}

@Composable
fun IntroPageContent(
    imageRes: Int,
    title: String,
    description: String,
    primaryColor: Color,
    textColor: Color,
    secondaryTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Image Container with subtle elevation effect
        Surface(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .padding(40.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(56.dp))
        
        Text(
            text = title,
            fontSize = 26.sp,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            fontSize = 16.sp,
            fontFamily = interFontFamily,
            textAlign = TextAlign.Center,
            color = secondaryTextColor,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun DotsIndicator(currentPage: Int, totalPages: Int, activeColor: Color) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "width"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = alpha))
            )
        }
    }
}
