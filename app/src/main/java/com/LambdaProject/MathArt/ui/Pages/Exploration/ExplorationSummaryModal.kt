package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.ViewModels.ExplorationStats
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.ExplorationAudioManager
import kotlinx.coroutines.delay

@Composable
fun ExplorationSummaryModal(
    stats: ExplorationStats,
    onClose: () -> Unit,
    audio: ExplorationAudioManager
) {
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (stats.isSuccess) {
            // Suara success sudah dipicu di InteractionType.FINISH
        } else {
            // Suara Game Over dimainkan tepat saat modal muncul
            audio.playSfx("gameover")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = !isProcessing) { if (!stats.isSuccess) onClose() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1A1A1A),
            border = BorderStroke(4.dp, if (stats.isSuccess) Color(0xFFFFD600) else Color.Red)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (stats.isSuccess) Icons.Default.EmojiEvents else Icons.Default.SentimentVeryDissatisfied,
                    contentDescription = null,
                    tint = if (stats.isSuccess) Color(0xFFFFD600) else Color.Red,
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = if (stats.isSuccess) "EXTRACTION SUCCESS!" else "MISSION FAILED",
                    color = if (stats.isSuccess) Color(0xFFFFD600) else Color.Red,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (stats.isSuccess) "Item telah disimpan ke koleksi" else "Semua item yang ditemukan telah hilang",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = Color.White.copy(alpha = 0.1f))

                // Stats Rows
                SummaryStatRow("Boss Dikalahkan", "${stats.bossesDefeated}", Color.Red)
                SummaryStatRow("Koin Didapat", "+${stats.coinsCollected}", Color.Yellow)
                SummaryStatRow("Scroll Ditemukan", "${stats.scrollsCollected.size}", Color.Cyan)
                SummaryStatRow("PowerUp Dibawa", "${stats.powerUpsCollected.size}", Color.Magenta)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                onClose()
                            }, 2500)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (stats.isSuccess) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text("KEMBALI KE LOBBY", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
