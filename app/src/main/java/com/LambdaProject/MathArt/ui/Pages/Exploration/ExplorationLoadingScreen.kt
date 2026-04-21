package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun ExplorationLoadingScreen(
    mapId: String,
    bagItems: String,
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current
    var isExiting by remember { mutableStateOf(false) }
    
    // Animasi Alpha untuk transisi keluar (Fade Out)
    val alphaAnim by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "fadeOut"
    )

    val tips = listOf(
        "Gunakan Power-Up saat menghadapi Boss yang sulit!",
        "Kumpulkan Koin untuk berbelanja di Mall.",
        "Scroll materi rahasia membantumu menjawab kuis dengan lebih mudah.",
        "Hati-hati dengan Station! Kamu butuh kode untuk membukanya.",
        "Eksplorasi wilayah gelap untuk menemukan peti harta karun.",
        "Setiap Boss memiliki kesempatan memberikan digit kode Station."
    )
    val randomTip = remember { tips.random() }
    
    // Logic Sinkronisasi & Timer Minimal 5 Detik
    LaunchedEffect(Unit) {
        // 1. Mulai Sinkronisasi Latar Belakang (isLoading = true)
        mapViewModel.loadMap(mapId, context)
        
        // 2. Timer Minimal 5 Detik
        delay(5000)
        
        // 3. Menunggu sinkronisasi Firestore selesai jika belum
        while (mapViewModel.isLoading) {
            delay(500)
        }
        
        // 4. Mulai Animasi Keluar
        isExiting = true
        delay(1000) // Tunggu durasi alphaAnim
        
        // 5. Navigasi
        navController.navigate("map/$mapId?bagItems=$bagItems") {
            popUpTo("ExplorationLoading/$mapId?bagItems=$bagItems") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = alphaAnim) // Hubungkan animasi alpha
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
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
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
            text = if (mapViewModel.isLoading) "Sedang menyinkronkan data peta terbaru..." else "Sinkronisasi selesai. Memasuki wilayah...",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            fontFamily = interFontFamily
        )
    }
}
