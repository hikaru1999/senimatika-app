package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.tutorialTexts
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun TutorialOverlay(
    step: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onOpenBag: () -> Unit
) {
    // Categories:
    // 0-4: Intro Tutorial
    // 5-12: Boss Proximity Sequence
    // 13: First Reward/Chest Info
    // 14: Station Proximity
    // 15: Extraction/Flag Proximity

    val isIntro = step in 0..4
    val isBossSequence = step in 5..12
    val isRewardInfo = step == 13
    val isStationInfo = step == 14
    val isFlagInfo = step == 15

    val title = when {
        isIntro -> "TUTORIAL (${step + 1}/5)"
        isBossSequence -> "PERINGATAN! (${step - 4}/8)"
        isRewardInfo -> "HARTA KARUN!"
        isStationInfo -> "STATION DITEMUKAN"
        isFlagInfo -> "MISI SELESAI?"
        else -> "INFORMASI"
    }

    val headerColor = when {
        isBossSequence -> Color.Red
        isIntro -> Color(0xFF1976D2)
        else -> Color(0xFFFFA000)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = headerColor,
                    fontFamily = interFontFamily
                )
                IconButton(onClick = onSkip, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = tutorialTexts.getOrElse(step) { "" },
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontFamily = interFontFamily,
                lineHeight = 24.sp,
                color = if (isBossSequence) Color.Black else Color.DarkGray,
                fontWeight = if (isBossSequence) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (step == 13) {
                        onOpenBag()
                    }
                    onNext()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = headerColor)
            ) {
                Text(
                    text = when {
                        step < 4 -> "MENGERTI"
                        step == 4 -> "MULAI EKSPLORASI"
                        step < 12 -> "MENGERTI"
                        step == 12 -> "SIAP BERTARUNG!"
                        step == 13 -> "LIHAT TAS"
                        step == 14 -> "MAU BUKA"
                        step == 15 -> "SELESAIKAN MISI"
                        else -> "MENGERTI"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
