package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.MaterialItem
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
        progress < 0.4f -> Color(0xFFEF5350) // Soft Red
        progress < 0.7f -> Color(0xFFFFB74D) // Soft Orange
        else -> Color(0xFF66BB6A) // Soft Green
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), clip = false),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = material.title,
                    fontFamily = interFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A237E)
                )
                
                Surface(
                    color = progressColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        color = progressColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Modern Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(Color(0xFFF5F5F5), CircleShape)
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(progressColor.copy(alpha = 0.7f), progressColor)
                            ),
                            shape = CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(
                        id = if (progress >= 1f) com.LambdaProject.MathArt.R.drawable.ic_assessment 
                             else com.LambdaProject.MathArt.R.drawable.ic_info
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (progress >= 1f) Color(0xFF66BB6A) else Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (progress >= 1f) "Materi telah diselesaikan!" else "Lanjutkan perjalanan belajarmu",
                    fontFamily = interFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (progress >= 1f) Color(0xFF66BB6A) else Color.Gray
                )
            }
        }
    }
}
