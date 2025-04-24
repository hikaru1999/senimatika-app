package com.LambdaProject.MathArt.ui.Screen

import android.util.Log
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.UserPreferences
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val userPreferences = UserPreferences(context)

    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "heartbeat-scale"
    )

    LaunchedEffect(Unit) {
        val isIntroShown = userPreferences.isIntroShown.first()
        val user = FirebaseAuth.getInstance().currentUser
        val isUserLoggedIn = user != null

        delay(5000)

        when {
            !isIntroShown -> {
                navController.navigate("intro") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            isUserLoggedIn -> {
                val uid = user!!.uid
                try {
                    val db = FirebaseFirestore.getInstance()
                    val document = db.collection("users").document(uid).get().await()
                    val username = document.getString("username") ?: "User"
                    navController.navigate("dashboard/$username") {
                        popUpTo("splash") { inclusive = true }
                    }
                } catch (e: Exception) {
                    navController.navigate("login?message=Gagal mengambil data") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            else -> {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
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