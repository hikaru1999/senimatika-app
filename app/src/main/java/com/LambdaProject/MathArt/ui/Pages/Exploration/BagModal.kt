package com.LambdaProject.MathArt.ui.Pages.Exploration

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.data.MAX_BAG_WEIGHT
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.Reward
import com.LambdaProject.MathArt.data.calculateTotalWeight
import com.LambdaProject.MathArt.data.getCooldownDuration
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BagModal(
    viewModel: MapViewModel,
    inventory: Inventory,
    playerHp: Float = 100f,
    maxBagWeight: Float = MAX_BAG_WEIGHT,
    isDropping: Boolean = false,
    isLanternActive: Boolean = false,
    isLeatherStrapsActive: Boolean = false,
    onClose: () -> Unit,
    onUsePowerUp: ((PowerUpType) -> Unit)? = null,
    onDropPowerUp: ((PowerUpType) -> Unit)? = null,
    isQuizActive: Boolean = false,
    powerUpCooldowns: Map<PowerUpType, Long> = emptyMap()
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(key1 = isQuizActive) {
        while (isQuizActive) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(500)
            currentTime = System.currentTimeMillis()
        }
    }

    val totalWeight = inventory.calculateTotalWeight()
    var selectedScroll by remember { mutableStateOf<Reward?>(null) }
    var selectedPowerUp by remember { mutableStateOf<PowerUpType?>(null) }

    val battleReadyList = listOf(
        PowerUpType.REMOVE_TWO_OPTIONS,
        PowerUpType.FREEZE_TIMER,
        PowerUpType.STREAK_PROTECTION,
        PowerUpType.HEALING_VIAL
    )

    val equipmentToolsList = listOf(
        PowerUpType.LEATHER_STRAPS,
        PowerUpType.MAGIC_KEY,
        PowerUpType.BINOCULAR,
        PowerUpType.TORCH,
        PowerUpType.LANTERN
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .fillMaxWidth(0.85f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF5F5DC),
            border = BorderStroke(4.dp, Color(0xFF8B4513))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Ranselku",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Health",
                        tint = Color(0xFFB71C1C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                Color(0xFF5D4037).copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(playerHp / 100f)
                                .background(
                                    if (playerHp > 30f) Color(0xFF4CAF50) else Color(0xFFB71C1C)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${playerHp.toInt()}/100",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (playerHp > 30f) Color(0xFF5D4037) else Color(0xFFB71C1C),
                        fontFamily = interFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isQuizActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.ic_coin), null, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Koin: ${inventory.coins}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    val groupedPowerUps = inventory.powerUps.groupBy { it }
                    val battleItems = groupedPowerUps.filter { it.key in battleReadyList }

                    if (battleItems.isNotEmpty()) {
                        Text(
                            text = "Battle Ready",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFB71C1C)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            battleItems.forEach { (type, list) ->
                                PowerUpItemView(
                                    pu = type,
                                    count = list.size,
                                    currentTime = currentTime,
                                    powerUpCooldowns = powerUpCooldowns,
                                    isEquipped = when(type) {
                                        PowerUpType.LANTERN -> isLanternActive
                                        PowerUpType.LEATHER_STRAPS -> isLeatherStrapsActive
                                        else -> false
                                    },
                                    onClick = { selectedPowerUp = type }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (!isQuizActive) {
                        val equipmentItems = groupedPowerUps.filter { it.key in equipmentToolsList }
                        if (equipmentItems.isNotEmpty()) {
                            Text(
                                text = "Equipment Tools",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFF1A237E)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                equipmentItems.forEach { (type, list) ->
                                    PowerUpItemView(
                                        pu = type,
                                        count = list.size,
                                        currentTime = currentTime,
                                        powerUpCooldowns = powerUpCooldowns,
                                        isEquipped = when(type) {
                                            PowerUpType.LANTERN -> isLanternActive
                                            PowerUpType.LEATHER_STRAPS -> isLeatherStrapsActive
                                            else -> false
                                        },
                                        onClick = { selectedPowerUp = type }
                                    )
                                }
                            }
                        }
                    }

                    if (groupedPowerUps.isEmpty()) {
                        Text("Kosong", color = Color.Gray, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

//                    if (viewModel.unlockedArtifactDetails.isNotEmpty()) {
//                        Text(
//                            text = "Artefak Terkoleksi",
//                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold),
//                            color = Color(0xFF5D4037)
//                        )
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        FlowRow(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(16.dp),
//                            verticalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//                            viewModel.unlockedArtifactDetails.forEach { artifact ->
//                                Column(
//                                    horizontalAlignment = Alignment.CenterHorizontally,
//                                    modifier = Modifier
//                                        .width(72.dp)
//                                        .clickable {
//                                            // Memanfaatkan triggerArtifact yang sudah ada
//                                            // Atau langsung set state jika data sudah lengkap
//                                            viewModel.openArtifactFromInventory(artifact)
//                                        }
//                                ) {
//                                    Surface(
//                                        modifier = Modifier.size(54.dp),
//                                        shape = RoundedCornerShape(12.dp),
//                                        color = Color(0xFFFDF8E1),
//                                        border = BorderStroke(2.dp, Color(0xFF8D6E63).copy(alpha = 0.5f))
//                                    ) {
//                                        Box(contentAlignment = Alignment.Center) {
//                                            // Ganti dengan ikon landmark yang sesuai
//                                            Image(
//                                                painter = painterResource(id = R.drawable.obj_landmark_tugu),
//                                                contentDescription = artifact.title,
//                                                modifier = Modifier.size(32.dp)
//                                            )
//                                        }
//                                    }
//                                    /* Spacer(modifier = Modifier.height(4.dp))
//                                    Text(
//                                        text = artifact.title,
//                                        fontSize = 10.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color(0xFF3E2723),
//                                        textAlign = TextAlign.Center,
//                                        maxLines = 2,
//                                        lineHeight = 11.sp
//                                    ) */
//                                }
//                            }
//                        }
//                        Spacer(modifier = Modifier.height(24.dp))
//                    }

                    Text("Scrolls", fontWeight = FontWeight.Bold, color = Color(0xFF8B4513))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (inventory.scrolls.isEmpty()) {
                        Text("Kosong", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        inventory.scrolls.forEach { reward ->
                            ScrollItem(
                                title = reward.title,
                                onClick = { selectedScroll = reward }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Berat Ransel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            "${String.format("%.1f", totalWeight)} / ${String.format("%.1f", maxBagWeight)} kg",
                            fontSize = 11.sp,
                            fontFamily = interFontFamily,
                            color = if (totalWeight > (maxBagWeight * 0.9f)) Color.Red else Color(0xFF5D4037)
                        )
                    }
                    Spacer(Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFD7CCC8))
                            .border(
                                width = 1.dp,
                                color = Color(0xFF5D4037).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(5.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((totalWeight / maxBagWeight).coerceIn(0f, 1f))
                                .background(
                                    if (totalWeight > 9f) Color.Red else Color(0xFF8B4513)
                                )
                        )
                    }
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
                ) {
                    Text("Tutup", color = Color.White)
                }
            }
        }

        if (selectedScroll != null) {
            Dialog(
                onDismissRequest = { selectedScroll = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable { selectedScroll = null },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ScrollRewardView(
                            title = selectedScroll!!.title,
                            content = selectedScroll!!.content
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { selectedScroll = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Tutup Bacaan", color = Color(0xFF8B4513), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    selectedPowerUp?.let { pu ->
        val currentIsEquipped = when(pu) {
            PowerUpType.LANTERN -> isLanternActive
            PowerUpType.LEATHER_STRAPS -> isLeatherStrapsActive
            else -> false
        }

        PowerUpDetailDialog(
            pu = pu,
            isDropping = isDropping,
            currentWeight = totalWeight,
            maxBagWeight = maxBagWeight,
            onDismiss = { selectedPowerUp = null },
            onUse = {
                onUsePowerUp?.invoke(pu)
                if (pu == PowerUpType.BINOCULAR || pu == PowerUpType.TORCH || pu == PowerUpType.LANTERN) {
                    onClose()
                }
                selectedPowerUp = null
            },
            onDrop = {
              onDropPowerUp?.invoke(pu)
              selectedPowerUp = null
            },
            isQuizActive = isQuizActive,
            isEquipped = currentIsEquipped,
            isLobby = false
        )
    }
}

@Composable
fun ScrollItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        border = BorderStroke(1.dp, Color(0xFF8B4513))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_scroll_open),
                null,
                modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title.ifEmpty { "Materi Tanpa Judul" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723),
                maxLines = 1
            )
        }
    }
}

@Composable
fun PowerUpItemView(
    pu: PowerUpType,
    count: Int,
    currentTime: Long,
    powerUpCooldowns: Map<PowerUpType, Long>,
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    val readyTime = powerUpCooldowns[pu] ?: 0L
    val isCooldown = currentTime < readyTime
    val totalDuration = getCooldownDuration(pu)
    val remaining = (readyTime - currentTime).coerceAtLeast(0L)
    val progress = if (isCooldown) ((totalDuration - remaining).toFloat() / totalDuration.toFloat()) else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .alpha(if (isCooldown) 0.4f else 1f)
                .clickable(enabled = !isCooldown) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Background Ikon
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color(0xFF8B4513).copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PowerUpIcon(pu)
                }
            }

            // Badge Equipped
            if (isEquipped) {
                Surface(
                    color = Color(0xFF2E7D32),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(20.dp)
                        .offset(x = (-2).dp, y = 2.dp),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.padding(2.dp))
                }
            }

            // Cooldown Progress
            if (isCooldown) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(44.dp),
                    color = Color(0xFF5D4037),
                    strokeWidth = 3.dp,
                    strokeCap = StrokeCap.Round
                )
            }

            if (count > 1) {
                Surface(
                    color = Color.Red,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                        .offset(x = 4.dp, y = (-4).dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = count.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
        Text(
            text = if (isCooldown) "RECHARGING" else "READY!",
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = if (isCooldown) Color(0xFF009406) else Color(0xFFEC7811)
        )
    }
}