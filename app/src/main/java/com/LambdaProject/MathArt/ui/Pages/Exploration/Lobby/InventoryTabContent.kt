package com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.Reward
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.Pages.Exploration.PowerUpDetailDialog
import com.LambdaProject.MathArt.ui.Pages.Exploration.PowerUpIcon
import com.LambdaProject.MathArt.ui.Pages.Exploration.ScrollRewardView
import com.LambdaProject.MathArt.utils.getLandmarkResourceId

@Composable
fun InventoryTabContent(
    inventory: Inventory,
    unlockedArtifacts: List<MapViewModel.ArtifactData>,
    onArtifactClick: (MapViewModel.ArtifactData) -> Unit,
    isMallDisabled: Boolean = false
) {
    var selectedPowerUp by remember { mutableStateOf<PowerUpType?>(null) }
    var isMallOpen by remember { mutableStateOf(false) }
    var selectedScroll by remember { mutableStateOf<Reward?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Balance & Exchange Center Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0xFF1E293B).copy(alpha = 0.12f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Coin Balance
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = CircleShape,
                                modifier = Modifier.size(42.dp),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_coin),
                                        contentDescription = "Coins",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "${inventory.coins} Koin",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFFD97706),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Saldo Koin",
                                    fontSize = 11.sp,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Exchange Center Button
                        Button(
                            onClick = { isMallOpen = true },
                            enabled = !isMallDisabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFF1F5F9),
                                disabledContentColor = Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isMallDisabled) 0.dp else 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMallDisabled) Icons.Default.Lock else Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Exchange Center",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 2. Dedicated Exchange Center Guide & Active Session Notice
            item {
                Surface(
                    color = if (isMallDisabled) Color(0xFFFFF1F2) else Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isMallDisabled) Color(0xFFFECDD3) else Color(0xFFBBF7D0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isMallDisabled) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isMallDisabled) Icons.Default.LockClock else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isMallDisabled) Color(0xFFE11D48) else Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMallDisabled) "Exchange Center Terkunci Sementara" else "Panduan Exchange Center",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                color = if (isMallDisabled) Color(0xFF9F1239) else Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isMallDisabled) {
                                    "Kamu memiliki sesi petualangan yang sedang aktif. Untuk mengakses Exchange Center, selesaikan eksplorasi yang sedang berlangsung terlebih dahulu."
                                } else {
                                    "Tukarkan koin yang kamu kumpulkan dengan berbagai power-up & item di sini. Catatan: Jika ada sesi petualangan aktif, selesaikan terlebih dahulu sebelum berbelanja."
                                },
                                fontSize = 11.sp,
                                fontFamily = interFontFamily,
                                color = if (isMallDisabled) Color(0xFFBE123C) else Color(0xFF15803D),
                                lineHeight = 15.5.sp
                            )
                        }
                    }
                }
            }

            // 3. Section: Landmark & Sejarah
            item {
                InventorySectionHeader(
                    title = "Landmark & Sejarah",
                    count = unlockedArtifacts.size,
                    icon = Icons.Default.AccountBalance
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (unlockedArtifacts.isEmpty()) {
                    InventoryEmptyState(
                        icon = Icons.Default.AccountBalance,
                        message = "Belum ada landmark ditemukan. Jelajahi pos wilayah untuk membuka landmark bersejarah!"
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        unlockedArtifacts.forEach { artifact ->
                            Card(
                                modifier = Modifier
                                    .width(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onArtifactClick(artifact) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Image(
                                                painter = painterResource(id = getLandmarkResourceId(artifact.variant)),
                                                contentDescription = artifact.title,
                                                modifier = Modifier.size(42.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = artifact.title.ifEmpty { "Landmark" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        color = Color(0xFF334155),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Section: Power Ups
            item {
                InventorySectionHeader(
                    title = "Power Ups",
                    count = inventory.powerUps.size,
                    icon = Icons.Default.Bolt
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (inventory.powerUps.isEmpty()) {
                    InventoryEmptyState(
                        icon = Icons.Default.Bolt,
                        message = "Belum ada Power-Up. Dapatkan dari Exchange Center atau peti harta di peta!"
                    )
                } else {
                    val groupedPUs = inventory.powerUps.groupBy { it.name }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        groupedPUs.forEach { (_, list) ->
                            val pu = list.first()
                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedPowerUp = pu },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PowerUpIcon(pu)

                                    if (list.size > 1) {
                                        Surface(
                                            color = Color(0xFFEF4444),
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 6.dp, y = (-6).dp),
                                            border = BorderStroke(1.5.dp, Color.White),
                                            shadowElevation = 3.dp
                                        ) {
                                            Text(
                                                text = "${list.size}x",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = interFontFamily,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Section: Scroll Materi
            item {
                InventorySectionHeader(
                    title = "Scroll Materi",
                    count = inventory.scrolls.size,
                    icon = Icons.AutoMirrored.Filled.MenuBook
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (inventory.scrolls.isEmpty()) {
                item {
                    InventoryEmptyState(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        message = "Belum ada Scroll Materi. Temukan gulungan naskah materi saat mengeksplorasi wilayah!"
                    )
                }
            } else {
                items(inventory.scrolls) { scroll ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedScroll = scroll },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFFEF3C7)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFFFFFBEB),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = painterResource(R.drawable.ic_scroll_open),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = scroll.title.ifEmpty { "Materi Eksplorasi" },
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily,
                                        color = Color(0xFF1E293B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Ketuk untuk membaca materi ringkasan",
                                        fontSize = 11.sp,
                                        fontFamily = interFontFamily,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEEF2FF),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Buka",
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Scroll Reading Modal Dialog
        if (selectedScroll != null) {
            Dialog(
                onDismissRequest = { selectedScroll = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.82f))
                        .clickable { selectedScroll = null },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.clickable(enabled = false) { },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ScrollRewardView(
                            title = selectedScroll!!.title,
                            content = selectedScroll!!.content
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { selectedScroll = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Tutup Bacaan",
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily
                            )
                        }
                    }
                }
            }
        }

        if (isMallOpen) {
            MallModal(
                onClose = { isMallOpen = false },
                isDisabled = isMallDisabled
            )
        }

        selectedPowerUp?.let { pu ->
            PowerUpDetailDialog(
                pu = pu,
                onDismiss = { selectedPowerUp = null },
                onUse = { },
                onDrop = { },
                isQuizActive = false,
                showDropButton = false,
                isLobby = true,
                isEquipped = false
            )
        }
    }
}

/**
 * Modern section header for Inventory tab
 */
@Composable
private fun InventorySectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4F46E5),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                color = Color(0xFF0F172A)
            )
        }

        Surface(
            color = Color(0xFFEEF2FF),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color(0xFFC7D2FE))
        ) {
            Text(
                text = count.toString(),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                color = Color(0xFF4F46E5),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Clean empty state placeholder for inventory categories
 */
@Composable
private fun InventoryEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = Color(0xFFF1F5F9),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = message,
                fontSize = 12.sp,
                fontFamily = interFontFamily,
                color = Color(0xFF64748B),
                lineHeight = 16.sp
            )
        }
    }
}
