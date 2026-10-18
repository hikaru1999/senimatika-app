package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun ActiveMaterialCard(
    modifier: Modifier = Modifier,
    userId: String,
    material: MaterialItem,
    maxPage: Int,
    isActive: Boolean,
    onClickLearn: (MaterialItem) -> Unit
) {
    val tabs = listOf("Pengantar", "Translasi", "Refleksi", "Rotasi", "Dilatasi", "Kuis", "Hasil Belajar")
    val coercedMaxPage = remember(maxPage) { maxPage.coerceIn(tabs.indices) }

    val lastVisitedTabTitle = tabs.getOrNull(coercedMaxPage) ?: "Pengantar"
    val progress = (coercedMaxPage.toFloat() / (tabs.size - 1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1200),
        label = "progressAnimation"
    )

    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0xFF6D28D9).copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF4C1D95).copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1035), // Deep Dark Violet / Indigo
                            Color(0xFF28134E),
                            Color(0xFF180D2E)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: Material Title & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = material.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFontFamily,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                        border = BorderStroke(0.8.dp, Color(0xFFA78BFA).copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF34D399), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Sedang Berjalan",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = interFontFamily,
                                color = Color(0xFFE9D5FF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Semicircular Dome Gauge with center info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Gauge Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        val strokePx = 10.dp.toPx()
                        val arcWidth = minOf(size.width - 24.dp.toPx(), 250.dp.toPx())
                        val arcHeight = arcWidth
                        val left = (size.width - arcWidth) / 2
                        val top = size.height - (arcWidth / 2)
                        val arcSize = Size(arcWidth, arcHeight)
                        val topLeft = Offset(left, top)

                        // 1. Background full 180° track with soft gradient & glow
                        drawArc(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4C1D95).copy(alpha = 0.4f),
                                    Color(0xFF6D28D9).copy(alpha = 0.4f),
                                    Color(0xFFFEF08A).copy(alpha = 0.45f)
                                ),
                                startX = left,
                                endX = left + arcWidth
                            ),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )

                        // 2. Outer subtle thin halo ring (as in reference image)
                        val outerOffset = 6.dp.toPx()
                        val outerArcSize = Size(arcWidth + (outerOffset * 2), arcHeight + (outerOffset * 2))
                        val outerTopLeft = Offset(left - outerOffset, top - outerOffset)
                        drawArc(
                            color = Color(0xFFA78BFA).copy(alpha = 0.2f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = outerTopLeft,
                            size = outerArcSize,
                            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 3. Active Progress Arc
                        val currentProgress = animatedProgress
                        if (currentProgress > 0.005f) {
                            val sweep = (180f * currentProgress).coerceIn(2f, 180f)
                            drawArc(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFE9D5FF),
                                        Color(0xFFA78BFA),
                                        Color(0xFF818CF8),
                                        Color(0xFFFDE047)
                                    ),
                                    startX = left,
                                    endX = left + arcWidth
                                ),
                                startAngle = 180f,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Center Content inside the Dome
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Topik: $lastVisitedTabTitle ($coercedMaxPage/${tabs.size - 1})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = interFontFamily,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom CTA Button: Lanjut Belajar
                Button(
                    onClick = { onClickLearn(material) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6D28D9)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isActive) "Lanjut Belajar" else "Mulai Belajar",
                            fontSize = 13.5.sp,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
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
            onResult(0)
        }
}