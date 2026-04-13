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
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(enabled = false) {},
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).background(Color.White, RoundedCornerShape(24.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (step == 5) "PERINGATAN BOSS" else "TUTORIAL (${step + 1}/5)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (step == 5) Color.Red else Color(0xFF1976D2),
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
                color = if (step == 5) Color.Black else Color.DarkGray,
                fontWeight = if (step == 5) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (step == 5) Color.Red else Color(0xFF1976D2))
            ) {
                Text(if (step < 4) "MENGERTI" else if (step == 5) "SIAP BERTARUNG!" else "MULAI EKSPLORASI", fontWeight = FontWeight.Bold)
            }
        }
    }
}