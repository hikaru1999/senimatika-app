package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.ExplorationAudioManager
import com.LambdaProject.MathArt.data.model.ExplorationStats
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExplorationSummaryModal(
    stats: ExplorationStats,
    isTutorialReplay: Boolean = false,
    onClose: () -> Unit,
    audio: ExplorationAudioManager
) {
    var isProcessing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Audio handling
        if (!stats.isSuccess) {
            audio.playSfx("gameover")
        } else {
            audio.playSfx("victory")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFF1E1E1E),
            border = BorderStroke(
                width = 3.dp,
                brush = Brush.verticalGradient(
                    if (stats.isSuccess) listOf(Color(0xFFFFD600), Color(0xFFFFA000))
                    else listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                if (stats.isSuccess) Color(0xFFFFD600).copy(0.1f)
                                else Color.Red.copy(0.1f),
                                CircleShape
                            )
                    )
                    Icon(
                        imageVector = if (stats.isSuccess) Icons.Default.EmojiEvents
                        else Icons.Default.SentimentVeryDissatisfied,
                        contentDescription = null,
                        tint = if (stats.isSuccess) Color(0xFFFFD600) else Color(0xFFF44336),
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (stats.isSuccess) "EXTRACTION SUCCESS!" else "MISSION FAILED",
                    color = if (stats.isSuccess) Color(0xFFFFD600) else Color(0xFFF44336),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFontFamily,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                if (isTutorialReplay && stats.isSuccess) {
                    Surface(
                        color = Color(0xFFFFC107).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFC107).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Mode Replay: Loot tidak disimpan",
                            color = Color(0xFFFFC107),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Text(
                        text = if (stats.isSuccess) "Hasil temuan berhasil diamankan"
                        else "Kamu kehilangan barang yang ditemukan",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryStatCard("Boss Dikalahkan", "${stats.bossesDefeated}", Icons.Default.Gavel, Color(0xFFFF5252))
                    SummaryStatCard("Koin Terkumpul", "+${stats.coinsCollected}", Icons.Default.MonetizationOn, Color(0xFFFFD740))
                    SummaryStatCard("Scroll Ditemukan", "${stats.scrollsCollected.size}", Icons.Default.HistoryEdu, Color(0xFF40C4FF))
                    SummaryStatCard("Power-Up Dibawa", "${stats.powerUpsCollected.size}", Icons.Default.Inventory2, Color(0xFFE040FB))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            isLoading = true
                            audio.stopBGM()
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000)
                                onClose()
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (stats.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    if (isLoading || isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text("KEMBALI KE LOBBY", fontWeight = FontWeight.Black, fontFamily = interFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatCard(label: String, value: String, icon: ImageVector, accentColor: Color) {
    Surface(
        color = Color(0xFF2C2C2E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}