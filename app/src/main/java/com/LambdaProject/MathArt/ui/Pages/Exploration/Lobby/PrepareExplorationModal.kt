package com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.ADD_STRAP
import com.LambdaProject.MathArt.data.MAX_BAG_WEIGHT
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.getPowerUpWeight
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.Pages.Exploration.PowerUpIcon
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

@Composable
fun PlayerIdleAnimation(
    modifier: Modifier = Modifier,
    frameDurationMs: Long = 75L
) {
    val idleFrames = remember {
        listOf(
            R.drawable.player_idle_0,
            R.drawable.player_idle_1,
            R.drawable.player_idle_2,
            R.drawable.player_idle_3,
            R.drawable.player_idle_4,
            R.drawable.player_idle_5,
            R.drawable.player_idle_6,
            R.drawable.player_idle_7,
            R.drawable.player_idle_8,
            R.drawable.player_idle_9,
            R.drawable.player_idle_10,
            R.drawable.player_idle_11,
            R.drawable.player_idle_12,
            R.drawable.player_idle_13,
            R.drawable.player_idle_14,
            R.drawable.player_idle_15
        )
    }

    var currentFrameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(frameDurationMs)
            currentFrameIndex = (currentFrameIndex + 1) % idleFrames.size
        }
    }

    Image(
        painter = painterResource(id = idleFrames[currentFrameIndex]),
        contentDescription = "Player Idle Animation",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun PrepareExplorationModal(
    permanentPowerUps: List<PowerUpType>,
    onCancel: () -> Unit,
    onConfirm: (List<PowerUpType>) -> Unit,
    maxWeight: Float = MAX_BAG_WEIGHT
) {
    var isLoading by remember { mutableStateOf(false) }
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
    val weightRatio = if (currentMaxWeight > 0f) (totalWeight / currentMaxWeight).coerceIn(0f, 1f) else 0f
    val animatedWeightProgress by animateFloatAsState(
        targetValue = weightRatio,
        animationSpec = tween(300),
        label = "weightProgress"
    )

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .shadow(24.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header with Character Idle Animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A), // Dark Slate
                                    Color(0xFF1E3A8A)  // Deep Blue
                                )
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Character Idle Showcase Pedestal
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(6.dp, CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.35f), Color(0xFF1E293B).copy(alpha = 0.8f))
                                    ),
                                    CircleShape
                                )
                                .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            PlayerIdleAnimation(
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 28.dp)
                        ) {
                            Text(
                                text = "Mau bawa apa ya?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = interFontFamily,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pilih perlengkapan yang akan kamu bawa. Hati-hati! Item akan tertinggal jika kamu gagal mencapai finish!",
                                fontSize = 11.5.sp,
                                color = Color(0xFFBAE6FD),
                                fontFamily = interFontFamily,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Close Button
                    IconButton(
                        onClick = {
                            isLoading = false
                            onCancel()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Batal",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 2. Power-Up Inventory List
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .weight(1f)
                ) {
                    if (groupedPUs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Belum ada Power-Up di Inventory",
                                    color = Color(0xFF94A3B8),
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = interFontFamily,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
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

                                    val isSelected = countInBag > 0

                                    Surface(
                                        color = if (isSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF86EFAC) else Color(0xFFE2E8F0)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PowerUpIcon(puType)

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = displayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    fontFamily = interFontFamily,
                                                    color = Color(0xFF0F172A)
                                                )

                                                Spacer(modifier = Modifier.height(3.dp))

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Scale,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(12.dp),
                                                            tint = Color(0xFF64748B)
                                                        )
                                                        Spacer(Modifier.width(3.dp))
                                                        val weightText = if (puType == PowerUpType.LEATHER_STRAPS) "+0.0kg" else "${itemWeight}kg"
                                                        Text(
                                                            text = weightText,
                                                            fontSize = 11.sp,
                                                            fontFamily = interFontFamily,
                                                            color = Color(0xFF64748B)
                                                        )
                                                    }

                                                    Text(
                                                        text = "• Tersedia: $available",
                                                        fontSize = 11.sp,
                                                        fontFamily = interFontFamily,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }

                                                if (puType == PowerUpType.LEATHER_STRAPS || puType == PowerUpType.LANTERN) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = if (countInBag > 0) "EFEK AKTIF (+${ADD_STRAP}kg)" else "PASIF / TIDAK DIPAKAI",
                                                        color = if (countInBag > 0) Color(0xFF16A34A) else Color(0xFF94A3B8),
                                                        fontSize = 9.5.sp,
                                                        fontFamily = interFontFamily,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // Plus-Minus Counter
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
                                                            imageVector = Icons.Default.Remove,
                                                            contentDescription = "Kurang",
                                                            modifier = Modifier.size(15.dp),
                                                            tint = if (canRemove) Color(0xFFE11D48) else Color(0xFFCBD5E1)
                                                        )
                                                    }

                                                    Text(
                                                        text = "$countInBag",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 14.sp,
                                                        fontFamily = interFontFamily,
                                                        modifier = Modifier.padding(horizontal = 6.dp),
                                                        color = if (countInBag > 0) Color(0xFF1E3A8A) else Color(0xFF94A3B8)
                                                    )

                                                    IconButton(
                                                        onClick = { selectedItems.add(puType) },
                                                        enabled = available > 0 && !isOverweight && !isLanternLimitReached,
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = "Tambah",
                                                            modifier = Modifier.size(15.dp),
                                                            tint = if (available > 0 && !isLanternLimitReached) Color(0xFF2563EB) else Color(0xFFCBD5E1)
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
                }

                // 3. Backpack Capacity Gauge
                Surface(
                    color = if (isOverweight) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (isOverweight) Color(0xFFFECDD3) else Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Berat Ransel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = if (isOverweight) Color(0xFFBE123C) else Color(0xFF1E293B)
                            )
                            Text(
                                text = String.format("%.1f / %.1f kg", totalWeight, currentMaxWeight),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = interFontFamily,
                                color = if (isOverweight) Color(0xFFE11D48) else Color(0xFF2563EB)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedWeightProgress)
                                    .background(
                                        brush = if (isOverweight) {
                                            Brush.horizontalGradient(listOf(Color(0xFFF43F5E), Color(0xFFE11D48)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF2563EB)))
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }

                        if (isOverweight) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ Ransel kelebihan muatan! Kurangi beberapa item.",
                                color = Color(0xFFE11D48),
                                fontSize = 10.5.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 4. Confirmation CTA Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isLoading && !isOverweight) {
                                isLoading = true
                                onConfirm(selectedItems.toList())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(if (!isLoading && !isOverweight) 6.dp else 0.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading && !isOverweight,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFF94A3B8)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "MULAI EKSPLORASI",
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = interFontFamily,
                                fontSize = 14.5.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}