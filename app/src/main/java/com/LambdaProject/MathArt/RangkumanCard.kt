package com.LambdaProject.MathArt

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.Data.*
import com.LambdaProject.MathArt.model.MaterialItem
import com.LambdaProject.MathArt.model.unlockPemulaAchievement
import com.LambdaProject.MathArt.ui.Screen.*

@Composable
fun RangkumanCard(material: MaterialItem, userId: String?, onClose: () -> Unit, onSesiDisimpan: () -> Unit) {
    val scrollState = rememberScrollState()
    val isScrolled = scrollState.value> 10
    val steps = sampleMaterialStep[material.id] ?: emptyList()
    val goals = materialGoals[material.id] ?: emptyList()
    val deskripsi = materialDescriptions[material.id] ?: "Deskripsi tidak tersedia."

    AnimatedContent(
        targetState = isScrolled,
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
        label = "Card Expansion"
    ) { scrolled ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (scrolled) Modifier.fillMaxHeight()
                    else Modifier.fillMaxHeight(0.75f)
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface (
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .background(Color.White)
                        .zIndex(1f),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                        ){
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup")
                            }
                        }
                        Text(
                            text = material.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(top = 150.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    Text(
                        text = "Deskripsi",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = deskripsi,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tujuan Pembelajaran",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LearningGoals(goals)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Alur Materi",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        steps.forEachIndexed { index, step ->
                            Row(verticalAlignment = Alignment.Top) {
                                Column(
                                    modifier = Modifier
                                        .width(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Canvas(modifier = Modifier.size(18.dp)) {
                                        drawCircle(color = Color(0xFF474747))
                                    }

                                    if (index != steps.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(25.dp)
                                                .background(Color.Gray)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step,
                                    fontSize = 16.sp,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            userId?.let {
                                unlockPemulaAchievement(userId)
                                SessionRepository.saveLearningSession(it, material) {
                                    onSesiDisimpan()
                                }
                                updateAccessiblePage(userId, 0)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5294ff))
                    ) {
                        Text("Mulai Belajar", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun LearningGoals(goals: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        goals.forEach { goal ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("•", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                Text(text = goal, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}