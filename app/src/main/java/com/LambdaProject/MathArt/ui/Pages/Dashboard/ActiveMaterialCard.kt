package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
    userId: String,
    material: MaterialItem,
    isActive: Boolean,
    onClickLearn: (MaterialItem) -> Unit
) {
    val tabs = listOf("Pengantar", "Translasi", "Refleksi", "Rotasi", "Dilatasi", "Kuis", "Hasil Belajar")
    var maxPage by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(userId, material.id) {
        getUserMaterialProgress(userId, material.id) {
            maxPage = it.coerceIn(tabs.indices)
        }
    }

    val lastVisitedTabTitle = tabs.getOrNull(maxPage) ?: "Belum ada"

    Card(
        modifier = Modifier
            .width(300.dp)
            .padding(top = 8.dp)
            .border(
                width = 1.dp,
                color = Color(0xFF5E9DFF),
                shape = RoundedCornerShape(10.dp)
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 24.dp)
        ) {
            Text(text = material.title, fontWeight = FontWeight.Bold, fontFamily = interFontFamily, fontSize = 18.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Materi Terakhir:",
                        fontFamily = interFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = lastVisitedTabTitle,
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF42A5F5)
                    )
                    Button(
                        onClick = {
                            isLoading = true
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1000)
                                onClickLearn(material)
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFF0E60DD)
                        ),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .widthIn(min = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 4.dp
                                )
                            } else {
                                Text(
                                    text = if (isActive) "Lanjut Belajar" else "Mulai Belajar",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                val progress = (maxPage.toFloat() / 6).coerceIn(0f, 1f)
                val progressPercent = (progress * 100).toInt()
                val progressColour = when {
                    progress < 0.4f -> Color(0xFFF44336)
                    progress < 0.7f -> Color(0xFFFF9800)
                    else -> Color(0xFF42A5F5)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = progressColour,
                        strokeWidth = 6.dp,
                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                    )
                    Text(
                        text = "$progressPercent%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
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
    val docRef = Firebase.firestore.collection("userProgress").document(userId)

    docRef.get()
        .addOnSuccessListener { document ->
            val maxPage = document.getLong("maxPage")?.toInt() ?: 0
            onResult(maxPage)
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Failed to get maxPage", exception)
            onResult(0)
        }
}