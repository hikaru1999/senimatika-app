package com.LambdaProject.MathArt.ui.Pages.Register

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

    LaunchedEffect(Unit) {
        delay(5000)
        navController.navigate("dashboard/$username") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3490DE), Color(0xFF1A237E))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-120).dp, y = (-80).dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 120.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(160.dp),
                color = Color.White.copy(alpha = 0.12f),
                shape = CircleShape,
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.35f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_reg_success),
                        contentDescription = "Ikon Sukses",
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Pendaftaran Berhasil!",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Halo, $username! Akunmu telah siap digunakan.",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Notice Card: Verifikasi Email Terkirim
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✉",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kode Verifikasi Terkirim",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Cek kotak masuk emailmu untuk melihat kode OTP 6-digit. Kamu dapat memasukkan kodenya kapan saja di halaman Profil.",
                            fontFamily = interFontFamily,
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    navController.navigate("dashboard/$username") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Lanjut ke Dashboard",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color(0xFF1A237E)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Mengalihkan otomatis dalam beberapa detik...",
                    fontFamily = interFontFamily,
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
