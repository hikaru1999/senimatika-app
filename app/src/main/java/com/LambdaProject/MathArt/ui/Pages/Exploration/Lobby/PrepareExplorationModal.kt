package com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.data.ADD_STRAP
import com.LambdaProject.MathArt.data.MAX_BAG_WEIGHT
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.getPowerUpWeight
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.Pages.Exploration.PowerUpIcon

@Composable
fun PrepareExplorationModal(
    permanentPowerUps: List<PowerUpType>,
    onCancel: () -> Unit,
    onConfirm: (List<PowerUpType>) -> Unit,
    maxWeight: Float = MAX_BAG_WEIGHT
) {
    var isLoading by remember { mutableStateOf(false)}
    val selectedItems = remember { mutableStateListOf<PowerUpType>() }
    val groupedPUs = permanentPowerUps.groupBy { it.name }

    val currentMaxWeight by remember {
        derivedStateOf {
            val strapsCount = selectedItems.count { it == PowerUpType.LEATHER_STRAPS }
            maxWeight + (strapsCount * ADD_STRAP)
        }
    }

    val totalWeight by remember {
        derivedStateOf {
            selectedItems.filter { it != PowerUpType.LEATHER_STRAPS }
                .sumOf { getPowerUpWeight(it).toDouble() }.toFloat()
        }
    }

    val isOverweight = totalWeight > currentMaxWeight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.90f)
                .padding(top = 48.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Mau bawa apa ya?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                fontFamily = interFontFamily,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Pilih item yang ingin dibawa. Hati-hati, item akan hilang jika kamu gagal!",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontFamily = interFontFamily,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(end = 48.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isLoading = false
                            onCancel()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Batal",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Column(modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .weight(1f)) {
                    if (groupedPUs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada Power-Up di Inventory",
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic,
                                fontFamily = interFontFamily
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            groupedPUs.forEach { (name, list) ->
                                item {
                                    val puType = list.first()
                                    val countInBag = selectedItems.count { it == puType }
                                    val available = list.size - countInBag
                                    val itemWeight = getPowerUpWeight(puType)

                                    val isLanternLimitReached = puType == PowerUpType.LANTERN && countInBag >= 1

                                    val canRemove = if (puType == PowerUpType.LEATHER_STRAPS && countInBag > 0) {
                                        totalWeight <= maxWeight
                                    } else {
                                        countInBag > 0
                                    }

                                    val displayName = when (puType) {
                                        PowerUpType.FREEZE_TIMER -> "Chrono Freeze"
                                        PowerUpType.STREAK_PROTECTION -> "Battle Shield"
                                        PowerUpType.REMOVE_TWO_OPTIONS -> "Truth Filter"
                                        PowerUpType.MAGIC_KEY -> "Magic Key"
                                        PowerUpType.HEALING_VIAL -> "Healing Vial"
                                        PowerUpType.BINOCULAR -> "Binocular"
                                        PowerUpType.LEATHER_STRAPS -> "Leather Straps"
                                        PowerUpType.LANTERN -> "Lantern"
                                        PowerUpType.TORCH -> "Torch"
                                        else -> name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                                    }

                                    Surface(
                                        color = Color(0xFFF8F9FF),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PowerUpIcon(puType)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = displayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                if (puType == PowerUpType.LEATHER_STRAPS || puType == PowerUpType.LANTERN) {
                                                    Text(
                                                        text = if (countInBag > 0) "EFEK AKTIF" else "TIDAK DIPAKAI",
                                                        color = if (countInBag > 0) Color(0xFF2E7D32) else Color.Gray,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Scale, null, Modifier.size(12.dp), tint = Color.Gray)
                                                    Spacer(Modifier.width(4.dp))

                                                    val weightText = if (puType == PowerUpType.LEATHER_STRAPS) "0.0kg" else "${itemWeight}kg"
                                                    Text(weightText, fontSize = 11.sp, color = Color.Gray)
                                                }

                                                Text("Tersedia: $available", fontSize = 11.sp, color = Color.Gray)
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .background(Color.White, CircleShape)
                                                    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                                                    .padding(2.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (puType == PowerUpType.LEATHER_STRAPS) {
                                                            if (totalWeight <= maxWeight) {
                                                                selectedItems.remove(puType)
                                                            }
                                                        } else {
                                                            selectedItems.remove(puType)
                                                        }
                                                    },
                                                    enabled = canRemove,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Remove,
                                                        null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = if (canRemove) Color.Red else Color.Gray
                                                    )
                                                }

                                                Text(
                                                    "$countInBag",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    fontFamily = interFontFamily,
                                                    modifier = Modifier.padding(horizontal = 8.dp),
                                                    color = if (countInBag > 0) Color(0xFF1A237E) else Color.Gray
                                                )

                                                IconButton(
                                                    onClick = { selectedItems.add(puType) },
                                                    enabled = available > 0 && !isOverweight && !isLanternLimitReached,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = if (available > 0 && !isLanternLimitReached) Color(0xFF1A237E) else Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kapasitas Ransel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )
                        Text(
                            text = String.format("%.1f / %.1f kg", totalWeight, currentMaxWeight),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isOverweight) Color.Red else Color(0xFF1A237E)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((totalWeight / currentMaxWeight).coerceAtMost(1f))
                                .background(if (isOverweight) Color.Red else Color(0xFF1976D2))
                        )
                    }
                    if (isOverweight) {
                        Text(
                            "Ransel terlalu berat! Kurangi beberapa item.",
                            color = Color.Red,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tombol Konfirmasi
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Button(
                        onClick = {
                            if (!isLoading && !isOverweight) {
                                isLoading = true
                                onConfirm(selectedItems.toList())
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && !isOverweight,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            disabledContainerColor = Color.Gray
                        ),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Text("MULAI EKSPLORASI", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}