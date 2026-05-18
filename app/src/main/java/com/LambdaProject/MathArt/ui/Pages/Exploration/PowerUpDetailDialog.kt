package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.LambdaProject.MathArt.data.MAX_BAG_WEIGHT
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun PowerUpDetailDialog(
    pu: PowerUpType,
    isEquipped: Boolean,
    isDropping: Boolean = false,
    maxBagWeight: Float = MAX_BAG_WEIGHT,
    currentWeight: Float = 0f,
    onDismiss: () -> Unit,
    onUse: () -> Unit,
    onDrop: () -> Unit,
    isQuizActive: Boolean,
    isLobby: Boolean = false,
    showDropButton: Boolean = true
) {
    val title = when(pu) {
        PowerUpType.FREEZE_TIMER -> "Chrono Freeze"
        PowerUpType.REMOVE_TWO_OPTIONS -> "Truth Filter"
        PowerUpType.STREAK_PROTECTION -> "Battle Shield"
        PowerUpType.HEALING_VIAL -> "Vial of Focus"
        PowerUpType.LEATHER_STRAPS -> "Extra Leather Straps"
        PowerUpType.MAGIC_KEY -> "Magic Key"
        PowerUpType.BINOCULAR -> "Binokular"
        PowerUpType.TORCH -> "Torch"
        PowerUpType.LANTERN -> "Lantern"
    }
    val description = when(pu) {
        PowerUpType.FREEZE_TIMER -> "Hentikan waktu kuis selama 5 detik agar kamu bisa berpikir lebih tenang."
        PowerUpType.REMOVE_TWO_OPTIONS -> "Menghilangkan 2 pilihan jawaban yang salah secara otomatis."
        PowerUpType.STREAK_PROTECTION -> "Memberikan proteksi streak jika kamu menjawab soal yang salah."
        PowerUpType.HEALING_VIAL -> "Ramuan konsentrasi untuk memulihkan +20 HP saat eksplorasi."
        PowerUpType.LEATHER_STRAPS -> "Tali kulit tambahan yang meningkatkan kapasitas maksimal tasmu sebanyak +5 kg secara permanen."
        PowerUpType.MAGIC_KEY -> "Kunci kuno yang mampu membuka Station secara paksa tanpa harus menjawab tantangan."
        PowerUpType.BINOCULAR -> "Takut untuk melangkah? Gunakan binokular untuk memperluas area pandangan selama 5 detik"
        PowerUpType.TORCH -> "Takut kegelapan? Gunakan obor untuk menerangi area sekitar selama 5 detik"
        PowerUpType.LANTERN -> "Say no more to the dark! Gunakan lantern untuk menerangi area sekitar"
    }

    val canDrop = if (pu == PowerUpType.LEATHER_STRAPS && isEquipped) {
        currentWeight <= (maxBagWeight - 5.0f)
    } else {
        true
    }

    val isBattleItem = pu == PowerUpType.FREEZE_TIMER ||
            pu == PowerUpType.REMOVE_TWO_OPTIONS ||
            pu == PowerUpType.STREAK_PROTECTION ||
            pu == PowerUpType.HEALING_VIAL

    val isExplorationItem = pu == PowerUpType.LEATHER_STRAPS ||
            pu == PowerUpType.BINOCULAR ||
            pu == PowerUpType.HEALING_VIAL ||
            pu == PowerUpType.TORCH ||
            pu == PowerUpType.LANTERN

    val isPermanent = pu == PowerUpType.LANTERN || pu == PowerUpType.LEATHER_STRAPS

    val buttonText = when {
        !isPermanent -> "GUNAKAN SEKARANG"
        isEquipped -> "LEPAS"
        else -> "PASANG"
    }

    val canShowUseButton = when {
        isLobby -> false
        pu == PowerUpType.MAGIC_KEY -> false
        isQuizActive -> isBattleItem
        else -> isExplorationItem
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box {
                if (!isLobby) {
                    androidx.compose.material3.IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.Gray
                        )
                    }
                }

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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = description,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontFamily = interFontFamily,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (canShowUseButton) {
                        Button(
                            onClick = {
                                onUse()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isQuizActive -> Color(0xFFD32F2F)
                                    isEquipped -> Color.DarkGray
                                    else -> Color(0xFF1976D2)
                                }
                            )
                        ) {
                            Text(
                                text = buttonText,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isLobby) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "MENGERTI",
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Black,
                                fontFamily = interFontFamily
                            )
                        }
                    }

                    if (!isQuizActive && showDropButton) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onDrop,
                            enabled = !isDropping && canDrop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.Red),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            if (isDropping) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Red, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (canDrop) "BUANG" else "TAS TERLALU BERAT", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}