package com.LambdaProject.MathArt.ui.Pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay

@Composable
fun SuccessPage(navController: NavController, username: String) {

    LaunchedEffect(true) {
        delay(5000)
        navController.navigate("dashboard/$username") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3490De))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_reg_success),
                contentDescription = "Ikon Sukses",
                modifier = Modifier.size(125.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Semua Sudah Siap!",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Selamat datang, $username. Kamu akan diarahkan ke Dashboard dalam beberapa detik...",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Selamat Belajar!",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}