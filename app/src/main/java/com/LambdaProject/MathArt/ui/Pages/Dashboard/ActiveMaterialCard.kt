package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.*

@Composable
fun ActiveMaterialCard(
    modifier: Modifier = Modifier,
    userId: String,
    material: MaterialItem,
    isActive: Boolean,
    onClickLearn: (MaterialItem) -> Unit
) {
    val tabs = listOf("Pengantar", "Translasi", "Refleksi", "Rotasi", "Dilatasi", "Kuis", "Hasil Belajar")
    var maxPage by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId, material.id) {
        getUserMaterialProgress(userId, material.id) {
            maxPage = it.coerceIn(tabs.indices)
        }
    }

    val lastVisitedTabTitle = tabs.getOrNull(maxPage) ?: "Belum ada"
    val progress = (maxPage.toFloat() / (tabs.size - 1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progressAnimation"
    )

    Card(
        modifier = Modifier
            /* .width(320.dp) */
            .padding(vertical = 8.dp, horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title and Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = material.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    fontSize = 20.sp,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.weight(1f)
                )

                /* if (isActive) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Sedang Dipelajari",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } */
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Progress Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progres Terakhir:",
                        fontFamily = interFontFamily,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = lastVisitedTabTitle,
                        fontFamily = interFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (!isLoading) {
                                isLoading = true
                                scope.launch {
                                    delay(800)
                                    onClickLearn(material)
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth(0.9f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFF2979FF)
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isActive) "Lanjut Belajar" else "Mulai Belajar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Right side: Circular Progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    val progressColor = when {
                        progress < 0.4f -> Color(0xFFEF5350)
                        progress < 0.9f -> Color(0xFFFFB74D)
                        else -> Color(0xFF66BB6A)
                    }

                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = progressColor,
                        strokeWidth = 8.dp,
                        trackColor = Color(0xFFF5F5F5),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

fun getUserMaterialProgress(
    userId: String,
    materialId: String,
    onResult: (Int) -> Unit
) {
    Firebase.firestore.collection("userProgress").document(userId)
        .get()
        .addOnSuccessListener { document ->
            val maxPage = document.getLong("maxPage")?.toInt() ?: 0
            onResult(maxPage)
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Failed to get maxPage", exception)
            onResult(0)
        }
}