package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.data.DataMaterials
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun PlayerWalkingAnimation(
    modifier: Modifier = Modifier,
    frameDurationMs: Long = 55L
) {
    val walkingFrames = remember {
        listOf(
            R.drawable.player_walking_0,
            R.drawable.player_walking_1,
            R.drawable.player_walking_2,
            R.drawable.player_walking_3,
            R.drawable.player_walking_4,
            R.drawable.player_walking_5,
            R.drawable.player_walking_6,
            R.drawable.player_walking_7,
            R.drawable.player_walking_8,
            R.drawable.player_walking_9,
            R.drawable.player_walking_10,
            R.drawable.player_walking_11,
            R.drawable.player_walking_12,
            R.drawable.player_walking_13,
            R.drawable.player_walking_14,
            R.drawable.player_walking_15,
            R.drawable.player_walking_16,
            R.drawable.player_walking_17,
            R.drawable.player_walking_18,
            R.drawable.player_walking_19
        )
    }

    var currentFrameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(frameDurationMs)
            currentFrameIndex = (currentFrameIndex + 1) % walkingFrames.size
        }
    }

    Image(
        painter = painterResource(id = walkingFrames[currentFrameIndex]),
        contentDescription = "Karakter Berjalan",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

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
    var countdown by remember { mutableIntStateOf(5) }
    var isCountdownActive by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(durationMillis = 800),
        label = "fadeOut"
    )

    val explorationName = remember(mapId) {
        when (mapId) {
            "transformasi_geometri" -> "Transformasi Geometri"
            "lingkaran" -> "Lingkaran"
            else -> DataMaterials.find { it.id == mapId }?.title
                ?: mapId.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
    }

    val tips = remember {
        listOf(
            "Gunakan Power-Up saat menghadapi Boss yang sulit!",
            "Kumpulkan Koin untuk ditukar di Exchange Center.",
            "Scroll materi membantumu menjawab kuis dengan lebih mudah.",
            "Hati-hati dengan Station! Kamu butuh kode untuk membukanya.",
            "Punya Lantern? Gunakan pada Night Mode untuk meningkatkan jangkauan cahaya.",
            "Setiap Boss memiliki kesempatan memberikan digit kode Station."
        )
    }
    val randomTip = remember { tips.random() }

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
                // Start 5-second countdown once map data loading is completed
                isCountdownActive = true
                countdown = 5
                while (countdown > 0) {
                    delay(1000L)
                    countdown -= 1
                }

                isExiting = true
                delay(600)

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
                    colors = listOf(
                        Color(0xFF0F172A), // Dark Slate
                        Color(0xFF0C4A6E), // Deep Ocean
                        Color(0xFF1E3A8A)  // Deep Blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            // Character Walking Showcase Pedestal
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .shadow(16.dp, CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF38BDF8).copy(alpha = 0.25f),
                                Color(0xFF1E293B).copy(alpha = 0.85f)
                            )
                        ),
                        CircleShape
                    )
                    .border(2.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PlayerWalkingAnimation(
                    modifier = Modifier
                        .size(90.dp)
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Mempersiapkan Wilayah...",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFontFamily,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Menyusun peta & artefak matematika...",
                color = Color(0xFFBAE6FD),
                fontSize = 12.5.sp,
                fontFamily = interFontFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF38BDF8),
                trackColor = Color.White.copy(alpha = 0.15f)
            )

            if (errorMessage != null) {
                AlertDialog(
                    onDismissRequest = { },
                    title = {
                        Text(
                            text = "Peta Gagal Dimuat",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = errorMessage!!,
                            fontFamily = interFontFamily
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                        ) {
                            Text("Kembali ke Lobby", fontFamily = interFontFamily)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Tips Card
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TIPS EKSPLORASI",
                            color = Color(0xFFFDE047),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFontFamily,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = randomTip,
                        color = Color(0xFFF1F5F9),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom Section: WordArt "Mathventure - [nama eksplorasi]" & Countdown / Status Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            // Mathventure - [nama eksplorasi] WordArt
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        ) {
                            append("Math")
                        }
                        withStyle(
                            SpanStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF34D399), // Emerald Green
                                        Color(0xFF2DD4BF), // Teal
                                        Color(0xFFFBBF24)  // Golden Amber
                                    )
                                ),
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        ) {
                            append("venture")
                        }
                    },
                    fontFamily = interFontFamily,
                    fontSize = 22.sp
                )

                Text(
                    text = " - $explorationName",
                    color = Color(0xFFBAE6FD),
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Bottom Status or Countdown Indicator
            if (isCountdownActive) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, Color(0xFFFDE047).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Memasuki area dalam $countdown detik...",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = interFontFamily
                        )
                    }
                }
            } else {
                Text(
                    text = when {
                        errorMessage != null -> "Gagal memuat peta."
                        mapViewModel.isLoading -> "Menghubungkan ke server..."
                        else -> "Data siap. Mempersiapkan petualangan..."
                    },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = interFontFamily
                )
            }
        }
    }
}
