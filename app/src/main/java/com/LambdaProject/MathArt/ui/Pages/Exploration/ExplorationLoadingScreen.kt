package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun ExplorationLoadingScreen(
    mapId: String,
    bagItems: String,
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
    var isExiting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeOut"
    )

    val tips = listOf(
        "Gunakan Power-Up saat menghadapi Boss yang sulit!",
        "Kumpulkan Koin untuk ditukar di Exchange Center.",
        "Scroll materi membantumu menjawab kuis dengan lebih mudah.",
        "Hati-hati dengan Station! Kamu butuh kode untuk membukanya.",
        "Punya Lantern? Gunakan pada Night Mode untuk meningkatkan jangkauan cahaya.",
        "Setiap Boss memiliki kesempatan memberikan digit kode Station."
    )
    val randomTip = remember { tips.random() }

    /* LaunchedEffect(Unit) {
        mapViewModel.loadMap(mapId, context)

        delay(5000)

        while (mapViewModel.isLoading) {
            delay(500)
        }

        isExiting = true
        delay(1000)

        navController.navigate("map/$mapId?bagItems=$bagItems") {
            popUpTo("ExplorationLoading/$mapId?bagItems=$bagItems") { inclusive = true }
        }
    } */

    LaunchedEffect(Unit) {
        try {
            mapViewModel.loadMap(mapId, context)

            withTimeoutOrNull(60000) {
                while (mapViewModel.isLoading) {
                    delay(500)
                }
            }
            if (mapViewModel.isLoading) {
                errorMessage = "Gagal memuat peta. Periksa koneksi internet Anda."
            } else {
                delay(1000)
                isExiting = true
                delay(1000)

                navController.navigate("map/$mapId?bagItems=$bagItems") {
                    popUpTo("ExplorationLoading/$mapId?bagItems=$bagItems") { inclusive = true }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Terjadi kesalahan sistem."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = alphaAnim)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                )
                Image(
                    painter = painterResource(id = R.drawable.img_logo_white),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Mempersiapkan Wilayah...",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = interFontFamily,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color(0xFFFBC02D),
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Peta Gagal Dimuat") },
                    text = { Text(errorMessage!!) },
                    confirmButton = {
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Kembali ke Lobby")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = null,
                            tint = Color(0xFFFBC02D),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TIPS EKSPLORASI",
                            color = Color(0xFFFBC02D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = randomTip,
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            text = when {
                errorMessage != null -> "Gagal memuat peta."
                mapViewModel.isLoading -> "Menghubungkan ke server..."
                else -> "Data siap. Memulai petualangan..."
            },
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            fontFamily = interFontFamily
        )
    }
}
