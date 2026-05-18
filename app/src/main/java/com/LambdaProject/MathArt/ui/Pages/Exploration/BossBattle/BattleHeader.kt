package com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.R

@Composable
fun BattleHeader(
    playerHp: Float,
    bossHp: Float,
    bossProgress: Float,
    bossTimeLeftMillis: Long,
    bossType: String,
    isChronoFreezeActive: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val inkColor = Color(0xFF3E2723)
    val leatherColor = Color(0xFF5D4037)

    val bossName = when (bossType) {
        "boss_1", "obj_boss_1" -> "Vardos"
        "boss_2", "obj_boss_2" -> "Grooten"
        "boss_3", "obj_boss_3" -> "Mortis"
        "boss_4", "obj_boss_4" -> "Aether"
        else -> "Boss"
    }

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
                    HPBar(hp = playerHp, color = Color(0xFF340000), isReversed = false)
                }
            }

            Text(
                "VS",
                color = inkColor,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )

            // --- OPPONENT SECTION ---
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

                    HPBar(hp = bossHp, color = Color(0xFF340000), isReversed = true)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(id = bossFrameRes),
                    contentDescription = "Boss Frame",
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
        isChrono -> "$bossName mendapatkan sabotase!"
        timeLeft <= 3000L -> "$bossName MENYERANG!"
        progress >= 0.5f -> "$bossName bersiap menyerang..."
        else -> "$bossName menganalisis strategi..."
    }
    val statusColor = when {
        isChrono -> Color(0xFF04A3FF)
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
                    .width(220.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFD7CCC8)) // Warna background bar (kertas gelap)
                    .border(
                        1.5.dp, Color(0xFF3E2723).copy(alpha = 0.3f),
                        androidx.compose.foundation.shape.RoundedCornerShape(5.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
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
