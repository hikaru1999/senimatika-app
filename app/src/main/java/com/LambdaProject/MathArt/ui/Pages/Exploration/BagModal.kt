package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.Reward
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BagModal(
    inventory: Inventory,
    onClose: () -> Unit,
    onUsePowerUp: ((PowerUpType) -> Unit)? = null,
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
            delay(500) // Update setiap setengah detik agar animasi smooth
            currentTime = System.currentTimeMillis()
        }
    }

    fun getTotalCooldown(type: PowerUpType): Long {
        return when (type) {
            PowerUpType.REMOVE_TWO_OPTIONS -> 45000L
            PowerUpType.FREEZE_TIMER -> 60000L
            PowerUpType.STREAK_PROTECTION -> 90000L
        }
    }

    var selectedScroll by remember { mutableStateOf<Reward?>(null) }
    var selectedPowerUp by remember { mutableStateOf<PowerUpType?>(null) }

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
                .fillMaxHeight(0.7f)
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
                    text = "Inventori",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (!isQuizActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.ic_coin), null, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Koin: ${inventory.coins}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Text("Power Ups", fontWeight = FontWeight.Bold, color = Color(0xFF8B4513))
                    Spacer(modifier = Modifier.height(12.dp))

                    val groupedPowerUps = inventory.powerUps.groupBy { it.name }

                    if (groupedPowerUps.isEmpty()) {
                        Text("Kosong", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            groupedPowerUps.forEach { (name, list) ->
                                val pu = list.first()
                                val readyTime = powerUpCooldowns[pu] ?: 0L
                                val isCooldown = currentTime < readyTime

                                val totalDuration = getTotalCooldown(pu)
                                val remaining = (readyTime - currentTime).coerceAtLeast(0L)
                                val progress = if (isCooldown)
                                    ((totalDuration - remaining).toFloat() / totalDuration.toFloat())
                                else 1f

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(64.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .alpha(if (isCooldown) 0.4f else 1f)
                                            .clickable(enabled = !isCooldown) {
                                                selectedPowerUp = pu
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(modifier = Modifier.alpha(if (isCooldown) 0.4f else 1f)) {
                                            PowerUpIcon(pu)
                                        }

                                        if (isCooldown) {
                                            // Background Lingkaran Redup
                                            CircularProgressIndicator(
                                                progress = { 1f },
                                                modifier = Modifier.size(40.dp),
                                                color = Color.Black.copy(alpha = 0.1f),
                                                strokeWidth = 3.dp,
                                                strokeCap = StrokeCap.Round
                                            )
                                            // Progress yang berjalan (Filling up)
                                            CircularProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.size(40.dp),
                                                color = Color(0xFF5D4037), // Cokelat Leather
                                                strokeWidth = 3.dp,
                                                strokeCap = StrokeCap.Round
                                            )
                                        }

                                        if (list.size > 1) {
                                            Surface(
                                                color = Color.Red,
                                                shape = CircleShape,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 6.dp, y = (-6).dp),
                                                border = BorderStroke(1.5.dp, Color.White)
                                            ) {
                                                Text(
                                                    text = list.size.toString(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // --- TEXT STATUS READY / RECHARGING ---
                                    Text(
                                        text = if (isCooldown) "RECHARGING" else "READY!",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp,
                                        color = if (isCooldown) Color(0xFF1B036E) else Color(
                                            0xFFEC7811
                                        ), // Abu vs Hijau
                                        fontFamily = interFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
        PowerUpDetailDialog(
            pu = pu,
            onDismiss = { selectedPowerUp = null },
            onUse = {
                onUsePowerUp?.invoke(pu)
                selectedPowerUp = null
            },
            isQuizActive = isQuizActive
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