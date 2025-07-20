package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.MaterialItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun MaterialTracker(userId: String, material: MaterialItem) {
    val db = Firebase.firestore
    val totalPage = 6
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(userId) {
        db.collection("userProgress").document(userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val maxPage = it.getLong("maxPage")?.toInt() ?: 0
                    progress = (maxPage.toFloat() / totalPage).coerceIn(0f, 1f)
                }
            }
    }

    val progressColor = when {
        progress < 0.4f -> Color(0xFFF44336)
        progress < 0.7f -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F7FF)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(
                text = material.title,
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .weight(1f)
                        .background(Color.LightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(progressColor)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6781FF),
                    modifier = Modifier.wrapContentWidth(Alignment.End)
                )
            }
        }
    }
}