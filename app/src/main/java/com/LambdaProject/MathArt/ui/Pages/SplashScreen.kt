package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.SplashState
import com.LambdaProject.MathArt.ViewModels.SplashViewModel
import com.LambdaProject.MathArt.ViewModels.SplashViewModelFactory
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SplashViewModel = viewModel(factory = SplashViewModelFactory(context))
    val splashState by viewModel.splashState.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    if (splashState is SplashState.NoInternet) {
        LaunchedEffect(Unit) {
            scaffoldState.snackbarHostState.showSnackbar("Periksa kembali internet anda")
        }

        LaunchedEffect(splashState) {
            delay(5000)
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }

        Scaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier.fillMaxSize(),
            backgroundColor = Color(0xFFFAFAFA)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 48.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_no_internet),
                            modifier = Modifier.padding(24.dp),
                            contentDescription = "Disconnected"
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Koneksi Terputus",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Pastikan perangkat Anda terhubung ke internet. Mengarahkan Anda kembali...",
                        fontSize = 14.sp,
                        fontFamily = interFontFamily,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        return
    }

    LaunchedEffect(splashState) {
        when (splashState) {
            is SplashState.Intro -> {
                navController.navigate("intro") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is SplashState.Login -> {
                navController.navigate("login?message=${(splashState as SplashState.Login).message}") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is SplashState.Dashboard -> {
                navController.navigate("dashboard/${(splashState as SplashState.Dashboard).username}") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFDCF0FF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_logo_blue),
            contentDescription = "Logo Aplikasi",
            modifier = Modifier
                .size(190.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Presented By",
                fontFamily = interFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF1A237E),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_unpar),
                        contentDescription = "Unpar",
                        modifier = Modifier.height(42.dp).wrapContentWidth(),
                        contentScale = ContentScale.Fit
                    )
                    
                    Image(
                        painter = painterResource(id = R.drawable.img_unpar_stem),
                        contentDescription = "Unpar STEM",
                        modifier = Modifier.height(38.dp).wrapContentWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
