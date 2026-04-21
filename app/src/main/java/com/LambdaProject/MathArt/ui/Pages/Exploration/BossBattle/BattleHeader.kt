package com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    bossType: String,
    isChronoFreezeActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val inkColor = Color(0xFF3E2723) // Cokelat sangat tua (seperti tinta)
    val leatherColor = Color(0xFF5D4037) // Aksen cokelat kulit

    val bossName = when (bossType) {
        "boss_1", "obj_boss_1" -> "Vardos"
        "boss_2", "obj_boss_2" -> "Grooten"
        "boss_3", "obj_boss_3" -> "Mortis"
        "boss_4", "obj_boss_4" -> "Aether"
        else -> "Boss"
    }
    // Mapping Boss Frame berdasarkan type yang dikirim
    val bossFrameRes = when (bossType) {
        "boss_1", "obj_boss_1" -> R.drawable.fr_boss_1
        "boss_2", "obj_boss_2" -> R.drawable.fr_boss_2
        "boss_3", "obj_boss_3" -> R.drawable.fr_boss_3
        "boss_4", "obj_boss_4" -> R.drawable.fr_boss_4
        else -> R.drawable.fr_boss_1
    }

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- PLAYER SECTION ---
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Frame Player
                Image(
                    painter = painterResource(id = R.drawable.fr_player),
                    contentDescription = "Player Frame",
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "PLAYER",
                        color = leatherColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // HP Bar Player
                    HPBar(hp = playerHp, color = Color(0xFF340000), isReversed = false)
                }
            }

            // VS Middle Spacer
            Text(
                "VS",
                color = inkColor,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )

            // --- BOSS SECTION ---
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = bossName.uppercase(),
                        color = leatherColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // HP Bar Boss
                    HPBar(hp = bossHp, color = Color(0xFF340000), isReversed = true)
                }

                Spacer(modifier = Modifier.width(8.dp))
                // Frame Boss Dinamis
                Image(
                    painter = painterResource(id = bossFrameRes),
                    contentDescription = "Boss Frame",
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boss Thinking Progress (Tetap sama dengan logika Anda)
        BossThinkingSection(
            bossProgress,
            bossTimeLeftMillis,
            bossName = bossName,
            isChronoFreezeActive
        )
    }
}

@Composable
fun HPBar(hp: Float, color: Color, isReversed: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(Color(0xFFD7CCC8))
            .border(1.dp, Color(0xFF3E2723).copy(alpha = 0.15f), CircleShape)
            .let { if (isReversed) it.graphicsLayer(scaleX = -1f) else it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((hp / 100f).coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color)
        )
    }
}

@Composable
fun BossThinkingSection(progress: Float, timeLeft: Long, bossName: String, isChrono: Boolean) {
    val statusText = when {
        isChrono -> "CHRONO FREEZE AKTIF!"
        timeLeft <= 3000L -> "$bossName MENYERANG!"
        progress >= 0.5f -> "$bossName bersiap menyerang..."
        else -> "Boss menganalisis strategi..."
    }
    val statusColor = when {
        isChrono -> Color(0xFF06273B)
        timeLeft <= 3000L -> Color(0xFFB70707)
        progress >= 0.5f -> Color(0xFFE65100)
        else -> Color(0xFF3E2723)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isChrono) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pu_freeze),
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .padding(end = 4.dp)
                    )
                }
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .width(220.dp) // Sedikit lebih lebar agar jelas
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFD7CCC8)) // Warna background bar (kertas gelap)
                    .border(
                        1.5.dp, Color(0xFF3E2723).copy(alpha = 0.3f),
                        androidx.compose.foundation.shape.RoundedCornerShape(5.dp)
                    )
            ) {
                // Box Progress (Warna yang berjalan)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        // Menggunakan progress untuk lebar dinamis (0.0f sampai 1.0f)
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .background(statusColor)
                )
            }

            /* if (timeLeft > 0 && !isChrono) {
                Text(
                    text = "${(timeLeft / 1000) + 1} detik tersisa",
                    color = statusColor.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } */
            /* LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.width(180.dp).height(4.dp).clip(CircleShape),
                color = statusColor,
                trackColor = Color(0xFF3E2723)
            ) */
        }
    }
}
