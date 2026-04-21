package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun PrepareExplorationModal(
    permanentPowerUps: List<PowerUpType>,
    onCancel: () -> Unit,
    onConfirm: (List<PowerUpType>) -> Unit
) {
    val selectedItems = remember { mutableStateListOf<PowerUpType>() }
    val groupedPUs = permanentPowerUps.groupBy { it.name }

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
                .fillMaxHeight(0.85f)
                .padding(top = 48.dp)
        ) {
            Column {
                // Header with Gradient Background and Close Button
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

                    // Tombol Silang (Batal) di Pojok Kanan Atas
                    IconButton(
                        onClick = onCancel,
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

                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp).weight(1f)) {
                    if (groupedPUs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada Power-Up di Inventory",
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
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
                                    val displayName = when (puType) {
                                        PowerUpType.FREEZE_TIMER -> "Chrono Freeze"
                                        PowerUpType.STREAK_PROTECTION -> "Battle Shield"
                                        PowerUpType.REMOVE_TWO_OPTIONS -> "Truth Filter"
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
                                                    displayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF1A237E)
                                                )
                                                Text(
                                                    "Tersedia: $available",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray,
                                                    fontFamily = interFontFamily
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .background(Color.White, CircleShape)
                                                    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
                                                    .padding(2.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { selectedItems.remove(puType) },
                                                    enabled = countInBag > 0,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Remove,
                                                        null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = if (countInBag > 0) Color.Red else Color.Gray
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
                                                    enabled = available > 0,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = if (available > 0) Color(0xFF1A237E) else Color.Gray
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

                // Hanya 1 Tombol Berangkat di bawah
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = { onConfirm(selectedItems.toList()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text(
                            "MULAI EKSPLORASI",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }



    /* Dialog(onDismissRequest = onCancel) {

    } */
}