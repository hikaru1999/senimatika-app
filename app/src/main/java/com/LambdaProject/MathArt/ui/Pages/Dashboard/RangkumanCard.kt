package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.*
import com.LambdaProject.MathArt.data.repository.SessionRepository
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.data.model.unlockPemulaAchievement
import com.LambdaProject.MathArt.ui.Pages.Material.updateAccessiblePage

@SuppressLint("FrequentlyChangingValue")
@Composable
fun RangkumanCard(material: MaterialItem, userId: String?, onClose: () -> Unit, onSesiDisimpan: () -> Unit) {
    val scrollState = rememberScrollState()
    val isScrolled = scrollState.value > 10
    val steps = DataMaterialStep[material.id] ?: emptyList()
    val goals = materialGoals[material.id] ?: emptyList()
    val deskripsi = materialDescriptions[material.id] ?: "Deskripsi tidak tersedia."

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isScrolled) Modifier.fillMaxHeight()
                else Modifier.fillMaxHeight(0.85f)
            ),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = Color.White,
        tonalElevation = 4.dp,
        shadowElevation = 16.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Sticky Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .zIndex(2f),
                color = Color.White,
                shadowElevation = if (isScrolled) 4.dp else 0.dp
            ) {
                Column {
                    // Pull Handle
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f), CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = material.title,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color(0xFF1A237E),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                // Section: Deskripsi
                RangkumanSectionHeader(title = "Deskripsi Materi")
                Text(
                    text = deskripsi,
                    fontFamily = interFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Section: Tujuan
                RangkumanSectionHeader(title = "Tujuan Pembelajaran")
                LearningGoals(goals)
                Spacer(modifier = Modifier.height(24.dp))

                // Section: Alur
                RangkumanSectionHeader(title = "Alur Belajar")
                TimelineLayout(steps)

                Spacer(modifier = Modifier.height(40.dp))

                // Action Button
                Button(
                    onClick = {
                        userId?.let {
                            unlockPemulaAchievement(it)
                            SessionRepository.saveLearningSession(it, material) {
                                onSesiDisimpan()
                            }
                            updateAccessiblePage(userId, 0)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(
                        "MULAI BELAJAR SEKARANG",
                        color = Color.White,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                // Tambahan Spacer yang cukup besar agar tombol tidak terhalang BottomNav saat discroll ke paling bawah
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            // Elemen penutup celah (Box putih solid di layer terbawah)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.White)
                    .align(Alignment.BottomCenter)
                    .zIndex(-1f)
            )
        }
    }
}

@Composable
fun RangkumanSectionHeader(title: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            color = Color(0xFF1A237E)
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(width = 32.dp, height = 3.dp)
                .background(Color(0xFF1976D2), CircleShape)
        )
    }
}

@Composable
fun TimelineLayout(steps: List<String>) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        steps.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.width(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(2.dp, Color(0xFF1976D2), CircleShape)
                            .padding(4.dp)
                            .background(Color(0xFF1976D2), CircleShape)
                    )

                    if (index != steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1976D2), Color(0xFFEEEEEE))
                                    )
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = step,
                    fontFamily = interFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun LearningGoals(goals: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        goals.forEach { goal ->
            Surface(
                color = Color(0xFFF8F9FE),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF66BB6A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = goal,
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}
