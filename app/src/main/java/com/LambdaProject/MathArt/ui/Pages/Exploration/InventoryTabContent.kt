package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.Pages.Profile.EmptyStateCard
import com.LambdaProject.MathArt.ui.Pages.Profile.SectionHeader
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun InventoryTabContent(inventory: Inventory) {
    var selectedPowerUp by remember { mutableStateOf<PowerUpType?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFFFF9C4),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🪙", fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Koin Dimiliki",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = interFontFamily
                    )
                    Text(
                        "${inventory.coins} Senimatika Coins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        color = Color(0xFFFBC02D)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            SectionHeader("Power Ups", inventory.powerUps.size)
            Spacer(modifier = Modifier.height(16.dp))
            if (inventory.powerUps.isEmpty()) {
                EmptyStateCard("Belum ada Power-Up")
            } else {
                val groupedPUs = inventory.powerUps.groupBy { it.name }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedPUs.forEach { (name, list) ->
                        val pu = list.first()
                        Box(modifier = Modifier.padding(bottom = 12.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.clickable { selectedPowerUp = pu }
                            ) {
                                PowerUpIcon(pu)
                                if (list.size > 1) {
                                    Surface(
                                        color = Color(0xFF1976D2),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp),
                                        border = BorderStroke(2.dp, Color.White),
                                        shadowElevation = 4.dp
                                    ) {
                                        Text(
                                            text = list.size.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
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
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            SectionHeader("Scroll Materi", inventory.scrolls.size)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (inventory.scrolls.isEmpty()) {
            item { EmptyStateCard("Belum ada Scroll Materi") }
        } else {
            items(inventory.scrolls) { scroll ->
                Box(modifier = Modifier.padding(bottom = 12.dp)) {
                    ScrollItem(scroll)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }

    selectedPowerUp?.let { pu ->
        PowerUpDetailDialog(pu = pu, onDismiss = { selectedPowerUp = null })
    }
}