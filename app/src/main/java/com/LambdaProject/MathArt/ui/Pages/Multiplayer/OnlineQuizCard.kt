package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.OnlineQuizDesc
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnlineQuizCard(
    material: OnlineQuizDesc,
    modifier: Modifier = Modifier,
    userPlayed: Int = remember { (15..250).random() },
    onQuizClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val isLearning = material.isLearningMode

    // Palette & Tokens
    val cardBorderBrush = if (isLearning) {
        Brush.linearGradient(listOf(Color(0xFF6EE7B7), Color(0xFFA7F3D0)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFFC7D2FE)))
    }

    val shadowSpotColor = if (isLearning) {
        Color(0xFF059669).copy(alpha = 0.25f)
    } else {
        Color(0xFF4F46E5).copy(alpha = 0.22f)
    }

    val headerGradient = if (isLearning) {
        Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF0D9488)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF4338CA), Color(0xFF6366F1)))
    }

    val buttonGradient = if (isLearning) {
        Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF2563EB)))
    }

    val questionCount = material.questions.size.takeIf { it > 0 } ?: material.numberQuestion

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isLearning) 6.dp else 5.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = shadowSpotColor
            )
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.5.dp,
                brush = cardBorderBrush,
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            // 1. Thumbnail Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (material.imageRes != 0) {
                    Image(
                        painter = painterResource(id = material.imageRes),
                        contentDescription = material.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(headerGradient)
                    )
                }

                // Decorative Watermark Icon (Abstract Subtle Depth)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 18.dp, y = 20.dp)
                ) {
                    Icon(
                        imageVector = if (isLearning) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier
                            .size(120.dp)
                            .rotate(if (isLearning) -10f else 12f)
                    )
                }

                // Smooth Multi-stop Vertical Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.45f),
                                    0.45f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top Floating Bar (Badges)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Question Count Pill
                    Surface(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = Color(0xFFF1F5F9),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "$questionCount Soal",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily
                            )
                        }
                    }

                    // Mode Badge & Class Level
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLearning) {
                            Surface(
                                color = Color(0xFF059669).copy(alpha = 0.95f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF6EE7B7))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Mode Belajar",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFF4338CA).copy(alpha = 0.95f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF818CF8))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFFFDE047),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Arena Kuis",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }

                        // Class Level Badge
                        if (material.classLevel.isNotBlank()) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.50f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = material.classLevel,
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.5.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Floating Badges on Thumbnail
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reward Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (material.rewardCoin > 0) {
                            Surface(
                                color = Color(0xFFD97706).copy(alpha = 0.92f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFCD34D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_coin),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "+${material.rewardCoin}",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }

                        if (material.exp > 0) {
                            Surface(
                                color = Color(0xFF475569).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "+${material.exp} EXP",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    }

                    // Duration Badge
                    val durationMin = material.durationMinutes.takeIf { it > 0 } ?: ((questionCount * 30 + 59) / 60).coerceAtLeast(1)
                    Surface(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "$durationMin Menit",
                                color = Color(0xFFF1F5F9),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = interFontFamily
                            )
                        }
                    }
                }
            }

            // 2. Card Content & Body Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Feature Highlight Banner
                Surface(
                    color = if (isLearning) Color(0xFFECFDF5) else Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isLearning) Color(0xFFA7F3D0) else Color(0xFFC7D2FE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isLearning) Icons.Default.Lightbulb else Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (isLearning) Color(0xFF059669) else Color(0xFF4F46E5),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isLearning) {
                                "Dilengkapi Pembahasan • Belajar Santai & Edukatif"
                            } else {
                                "Skor Masuk Leaderboard • Poin Kecepatan & Duel 1v1"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interFontFamily,
                            color = if (isLearning) Color(0xFF047857) else Color(0xFF3730A3),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Title & Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = material.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = interFontFamily,
                        color = Color(0xFF0F172A),
                        lineHeight = 23.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (material.description.isNotBlank() && !material.description.equals("None.", ignoreCase = true)) {
                        Text(
                            text = material.description,
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B),
                            fontFamily = interFontFamily,
                            lineHeight = 17.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Feature Tags Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLearning) {
                        QuickTagItem(
                            icon = Icons.Default.CheckCircle,
                            text = "Solusi Step-by-Step",
                            tint = Color(0xFF059669),
                            bg = Color(0xFFF0FDF4)
                        )
                        QuickTagItem(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            text = "Fokus Konsep",
                            tint = Color(0xFF0D9488),
                            bg = Color(0xFFF0FDFA)
                        )
                    } else {
                        QuickTagItem(
                            icon = Icons.Default.People,
                            text = "$userPlayed Peserta",
                            tint = Color(0xFF4F46E5),
                            bg = Color(0xFFEEF2FF)
                        )
                        QuickTagItem(
                            icon = Icons.Default.Bolt,
                            text = "Adu Kecepatan",
                            tint = Color(0xFFD97706),
                            bg = Color(0xFFFFFBEB)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Action Button (Gradient Elevated Button)
                Surface(
                    onClick = {
                        if (!isLoading) {
                            isLoading = true
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(400)
                                onQuizClick()
                                isLoading = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = if (isLearning) 4.dp else 4.dp,
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(buttonGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLearning) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isLearning) "Buka Ruang Belajar" else "Masuk Arena Kuis",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    letterSpacing = 0.3.sp
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTagItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color,
    bg: Color
) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = interFontFamily,
                color = tint
            )
        }
    }
}

// Overload untuk kemudahan integrasi langsung dengan OnlineQuizItem
@Composable
fun OnlineQuizCard(
    quiz: OnlineQuizItem,
    modifier: Modifier = Modifier,
    userPlayed: Int = remember { (15..250).random() },
    onQuizClick: () -> Unit
) {
    val desc = remember(quiz) {
        OnlineQuizDesc(
            id = quiz.id,
            title = quiz.title,
            active = quiz.active,
            description = quiz.description,
            rewardCoin = quiz.rewardCoin.toInt(),
            exp = quiz.exp.toInt(),
            classLevel = quiz.jenjang.ifBlank { "Umum" },
            isRandomized = quiz.isRandomized,
            isLearningMode = quiz.isLearningMode,
            numberQuestion = quiz.questions.size.takeIf { it > 0 } ?: quiz.totalQuestions
        )
    }
    OnlineQuizCard(
        material = desc,
        modifier = modifier,
        userPlayed = userPlayed,
        onQuizClick = onQuizClick
    )
}
