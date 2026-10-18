package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.CategoryItem

@Composable
fun CategoryCard(
    category: CategoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val fallbackRes = remember(category.name, category.imageRes) {
        if (category.imageRes != 0) {
            category.imageRes
        } else {
            when (category.name.lowercase().trim()) {
                "geometri" -> R.drawable.ico_geometry
                "kalkulus" -> R.drawable.ico_calculus
                "trigonometri" -> R.drawable.ico_trigo
                "statistika" -> R.drawable.ico_statistic
                else -> R.drawable.ic_material
            }
        }
    }

    Box(
        modifier = modifier
            .height(90.dp)
            .padding(1.dp)
    ) {
        Card(
            onClick = {
                if (category.isLocked) {
                    Toast.makeText(
                        context,
                        "Materi untuk kategori ${category.name} belum tersedia.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    onClick()
                }
            },
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(
                1.dp,
                if (category.isLocked) Color(0xFFE2E8F0) else Color(0xFFBFDBFE)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (category.isLocked) 1.dp else 2.5.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (category.isLocked) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFEFF6FF), Color.White)
                            )
                        }
                    )
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(
                                0.8.dp,
                                if (category.isLocked) Color(0xFFE2E8F0) else Color(0xFFDBEAFE),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val cleanIconUrl = category.iconUrl?.trim()
                        if (!cleanIconUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(cleanIconUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = category.name,
                                modifier = Modifier.size(24.dp),
                                contentScale = ContentScale.Fit,
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF2563EB)
                                    )
                                },
                                error = {
                                    Image(
                                        painter = painterResource(id = fallbackRes),
                                        contentDescription = category.name,
                                        modifier = Modifier.size(24.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            )
                        } else {
                            Image(
                                painter = painterResource(id = fallbackRes),
                                contentDescription = category.name,
                                modifier = Modifier.size(24.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = category.name,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        color = if (category.isLocked) Color(0xFF64748B) else Color(0xFF1E293B),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Lock Overlay
        if (category.isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xB21E293B)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci (Belum ada materi)",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
