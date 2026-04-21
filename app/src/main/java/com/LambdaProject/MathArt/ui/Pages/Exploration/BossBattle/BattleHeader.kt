package com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.R

@Composable
fun BattleHeader(
    playerHp: Float,
    bossHp: Float,
    bossProgress: Float,
    bossTimeLeftMillis: Long,
    isChronoFreezeActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- PLAYER HP ---
            Column(modifier = Modifier.weight(1f)) {
                Text("PLAYER", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((playerHp / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Color.Green)
                    )
                }
            }

            // VS Spacer
            Text("VS", color = Color.White, modifier = Modifier.padding(
                horizontal = 12.dp
            ), fontWeight = FontWeight.Black)

            // --- BOSS HP ---
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("BOSS", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .graphicsLayer(scaleX = -1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((bossHp / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Color.Red)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Boss Thinking Progress
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val statusText = when {
                isChronoFreezeActive -> "Chrono - Freeze! Boss Mendapatkan Sabotase!"
                bossTimeLeftMillis <= 3000L -> "Boss Menyerang!"
                bossProgress >= 0.5f -> "Boss bersiap menyerang"
                else -> "Boss menyiapkan strategi"
            }
            val statusColor = when {
                isChronoFreezeActive -> Color.Cyan
                bossTimeLeftMillis <= 3000L -> Color.Red
                bossProgress >= 0.5f -> Color(0xFFFFA500) // Orange
                else -> Color.Yellow
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isChronoFreezeActive) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_pu_freeze),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp).padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = statusText,
                        color = statusColor.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                LinearProgressIndicator(
                    progress = { bossProgress },
                    modifier = Modifier
                        .width(150.dp)
                        .height(4.dp)
                        .clip(CircleShape),
                    color = statusColor,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}
