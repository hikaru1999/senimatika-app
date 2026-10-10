package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.MissionProgress
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun MissionTrackerModal(
    activeMissions: List<MissionProgress>,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f)
                    .wrapContentHeight()
                    .clickable(enabled = false) { }
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.5.dp, Color(0xFF334155)),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // --- Header ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TrackChanges,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Misi Tersedia",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    fontFamily = interFontFamily,
                                    color = Color.White
                                )
                                Text(
                                    text = "Selesaikan target untuk bonus koin dan EXP",
                                    fontSize = 11.5.sp,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // --- Missions List (Scrollable if many) ---
                    if (activeMissions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada misi aktif di wilayah ini.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontFamily = interFontFamily
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            activeMissions.forEach { progress ->
                                MissionItemCard(progress = progress)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- Total Bonus Footer ---
                    val totalBonus = activeMissions.filter { it.isCompleted }.sumOf { it.mission.bonusCoins }
                    val maxBonus = activeMissions.sumOf { it.mission.bonusCoins }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bonus Terkumpul:",
                                fontSize = 12.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFCBD5E1)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_coin),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+$totalBonus / $maxBonus Koin",
                                    fontSize = 13.sp,
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (totalBonus > 0) Color(0xFFFFD700) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- CTA Button (Always visible) ---
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text(
                            text = "Lanjutkan Petualangan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = interFontFamily,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionItemCard(progress: MissionProgress) {
    val mission = progress.mission
    val isDone = progress.isCompleted
    val current = progress.currentCount.coerceAtMost(mission.targetCount)
    val target = mission.targetCount

    val progressRatio by animateFloatAsState(
        targetValue = if (target > 0) (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 1f,
        label = "progressRatio"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDone) Color(0xFF14532D).copy(alpha = 0.3f) else Color(0xFF0F172A),
        border = BorderStroke(
            1.dp,
            if (isDone) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFF334155)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isDone) Color(0xFF22C55E) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = mission.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            lineHeight = 17.sp,
                            fontFamily = interFontFamily,
                            color = if (isDone) Color(0xFF86EFAC) else Color.White
                        )
                        if (mission.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mission.description,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                fontFamily = interFontFamily,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                if (mission.bonusCoins > 0) {
                    Surface(
                        color = if (isDone) Color(0xFF15803D).copy(alpha = 0.4f) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isDone) Color(0xFF22C55E) else Color(0xFF475569))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_coin),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+${mission.bonusCoins}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = interFontFamily,
                                color = if (isDone) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                            )
                        }
                    }
                }
            }

            // Progress bar
            if (target > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isDone) Color(0xFF22C55E) else Color(0xFF3B82F6),
                        trackColor = Color(0xFF334155)
                    )
                    Text(
                        text = "$current / $target",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        color = if (isDone) Color(0xFF86EFAC) else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
