package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "heartbeat-scale"
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
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.ic_no_internet),
                        modifier = Modifier.size(100.dp),
                        contentDescription = "Disconnected"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak ada koneksi internet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Akan diarahkan ke halaman login ...",
                        fontSize = 14.sp,
                        fontFamily = interFontFamily,
                        color = Color.Gray
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
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.img_logo_blue),
                contentDescription = "Logo Aplikasi",
                modifier = Modifier
                    .size(170.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Presented By
            Column(
                horizontalAlignment = Alignment
                    .CenterHorizontally
            ) {
                Text(
                    text = "Brought to you by:",
                    fontFamily = interFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row (
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_unpar),
                        contentDescription = "Presenter Logo",
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_unpar_stem),
                        contentDescription = "Presenter Logo",
                        modifier = Modifier.height(45.dp).wrapContentWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}