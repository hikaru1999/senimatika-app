package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun PowerUpDetailDialog(pu: PowerUpType, onDismiss: () -> Unit) {
    val title = when(pu) {
        PowerUpType.FREEZE_TIMER -> "Freeze Timer"
        PowerUpType.REMOVE_TWO_OPTIONS -> "Truth Filter"
        PowerUpType.STREAK_PROTECTION -> "Battle Shield"
    }
    val description = when(pu) {
        PowerUpType.FREEZE_TIMER -> "Hentikan waktu kuis selama 5 detik agar kamu bisa berpikir lebih tenang."
        PowerUpType.REMOVE_TWO_OPTIONS -> "Menghilangkan 2 pilihan jawaban yang salah secara otomatis."
        PowerUpType.STREAK_PROTECTION -> "Memberikan proteksi streak jika kamu menjawab soal yang salah."
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE3F2FD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PowerUpIcon(pu)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = description,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontFamily = interFontFamily,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("MENGERTI", fontWeight = FontWeight.Bold, fontFamily = interFontFamily)
                }
            }
        }
    }
}