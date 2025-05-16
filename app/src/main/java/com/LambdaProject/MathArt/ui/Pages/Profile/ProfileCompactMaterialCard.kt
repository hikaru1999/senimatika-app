package com.LambdaProject.MathArt.ui.Pages.Profile

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
fun CompactMaterialCard(userId: String, material: MaterialItem) {
    val db = Firebase.firestore
    var progress by remember { mutableStateOf(0f) }
    val totalPage = 6

    LaunchedEffect(userId) {
        db.collection("userProgress").document(userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val maxPage = it.getLong("maxPage")?.toInt() ?: 0
                    progress = ((maxPage + 1).toFloat() / totalPage).coerceIn(0f, 1f)
                }
            }
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
                LinearProgressIndicator(
                    progress = {
                        progress
                    },
                    modifier = Modifier
                        .height(6.dp)
                        .width(260.dp),
                    color = Color(0xFF6781FF),
                    trackColor = Color.LightGray,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6781FF)
                )
            }
        }
    }
}