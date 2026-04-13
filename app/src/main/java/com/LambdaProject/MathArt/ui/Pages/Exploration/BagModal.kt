package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.PowerUpType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BagModal(
    inventory: Inventory,
    onClose: () -> Unit,
    onUsePowerUp: ((PowerUpType) -> Unit)? = null
) {
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

                // Coins
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

                Spacer(modifier = Modifier.height(24.dp))

                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Text("Power Ups", fontWeight = FontWeight.Bold, color = Color(0xFF8B4513))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (inventory.powerUps.isEmpty()) {
                        Text("Kosong", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        // FIX: Group by name/string representation of PowerUpType to ensure matching
                        val groupedPowerUps = inventory.powerUps.groupBy { it.name }
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            groupedPowerUps.forEach { (name, list) ->
                                val puType = list.first()
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = onUsePowerUp != null) {
                                            onUsePowerUp?.invoke(puType)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    PowerUpIcon(puType)
                                    
                                    // Badge Counter
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
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Scrolls", fontWeight = FontWeight.Bold, color = Color(0xFF8B4513))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (inventory.scrolls.isEmpty()) {
                        Text("Kosong", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        inventory.scrolls.forEach { ScrollItem(it) }
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
    }
}

@Composable
fun ScrollItem(content: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        border = BorderStroke(1.dp, Color(0xFF8B4513))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_scroll_open), null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(content, fontSize = 13.sp, color = Color(0xFF3E2723), maxLines = 1)
        }
    }
}
