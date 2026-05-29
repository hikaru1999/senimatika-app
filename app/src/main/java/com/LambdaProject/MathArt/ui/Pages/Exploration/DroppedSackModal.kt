package com.LambdaProject.MathArt.ui.Pages.Exploration

import android.annotation.SuppressLint
import androidx.compose.animation.core.copy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.*

@SuppressLint("DefaultLocale")
@Composable
fun DroppedSackModal(
    sackInventory: Inventory,
    maxBagWeight: Float = MAX_BAG_WEIGHT,
    playerInventory: Inventory,
    isAfterDeadSack: Boolean,
    onClose: () -> Unit,
    onPickItem: (PowerUpType) -> Unit,
    onPickCoins: () -> Unit = {}
) {
    val totalWeight = playerInventory.calculateTotalWeight()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF0E7D8),
            border = BorderStroke(3.dp, if (isAfterDeadSack) Color(0xFFB71C1C) else Color(0xFF5D4037))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (isAfterDeadSack) "SISA BARANG PERJALANAN LALU" else "BARANG TERCECER",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = if (isAfterDeadSack) Color(0xFFB71C1C) else Color(0xFF3E2723),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Koin
                    if (sackInventory.coins > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE5DCC3), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(painterResource(R.drawable.ic_coin), null, Modifier.size(32.dp))
                            Text(
                                "Koin: ${sackInventory.coins}",
                                Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                            Button(
                                onClick = onPickCoins,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("AMBIL", fontSize = 10.sp)
                            }
                        }
                    }

                    val grouped = sackInventory.powerUps.groupBy { it.name }
                    grouped.forEach { (name, list) ->
                        val pu = list.first()
                        val countOnGround = list.size

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE5DCC3), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp)) { PowerUpIcon(pu) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name.replace("_", " "), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Row {
                                    Text("${String.format("%.1f", getPowerUpWeight(pu))} kg", fontSize = 10.sp, color = Color.Gray)
                                    if (countOnGround > 1) {
                                        Text(" • $countOnGround Tersedia", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Button(
                                onClick = { onPickItem(pu) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("AMBIL", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kapasitas Tas Anda", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF5D4037))
                        Text("${String.format("%.1f", totalWeight)} / ${
                            String.format(
                                "%.1f",
                                maxBagWeight
                            )
                        } kg", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD7CCC8))
                    ) {
                        Box(modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((totalWeight / maxBagWeight).coerceAtMost(1f))
                            .background(if (totalWeight > maxBagWeight) Color.Red else Color(0xFF8B4513)))
                    }
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(2.dp, Color(0xFF5D4037)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("TINGGALKAN", color = Color(0xFF5D4037), fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}