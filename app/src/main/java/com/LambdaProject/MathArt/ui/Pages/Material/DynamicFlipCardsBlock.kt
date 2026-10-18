package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.data.model.ContentBlock
import com.LambdaProject.MathArt.data.model.FlipCardItem
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText

@Composable
fun DynamicFlipCardsBlock(block: ContentBlock) {
    val cards = block.cards ?: emptyList()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF5F3FF),
        border = BorderStroke(1.2.dp, Color(0xFFDDD6FE)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = block.title?.ifBlank { "Glosarium Rumus & Konsep" } ?: "Glosarium Rumus & Konsep",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color(0xFF1E1B4B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = block.instruction?.ifBlank { "Ketuk kartu untuk membalik dan melihat rumus serta penjelasan lengkapnya." }
                            ?: "Ketuk kartu untuk membalik dan melihat rumus serta penjelasan lengkapnya.",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF6366F1),
                        lineHeight = 16.sp
                    )
                }
            }

            if (cards.isEmpty()) {
                Text(
                    text = "Belum ada kartu rumus yang ditambahkan.",
                    fontFamily = interFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                cards.forEachIndexed { index, card ->
                    FlipCardItemView(card = card, index = index, totalCards = cards.size)
                }
            }
        }
    }
}

private data class FlipCardTheme(
    val frontGradient: List<Color>,
    val frontBorder: Color,
    val frontBadgeBg: Color,
    val frontBadgeText: Color = Color.White,
    val frontTitleColor: Color,
    val frontDividerColor: Color,
    val frontActionColor: Color,
    val frontActionBg: Color,
    val frontTextColor: Color = Color(0xFF1E293B),

    val backGradient: List<Color>,
    val backBorder: Color,
    val backBadgeBg: Color,
    val backBadgeText: Color = Color.White,
    val backTitleColor: Color,
    val backFormulaBg: Color = Color.White,
    val backFormulaBorder: Color,
    val backFormulaText: Color,
    val backHintBg: Color,
    val backHintBorder: Color,
    val backHintText: Color,
    val backActionColor: Color,
    val backActionBg: Color
)

private val FLIP_CARD_THEMES = listOf(
    // 0: Royal Blue / Sapphire (Front) -> Warm Amber (Back)
    FlipCardTheme(
        frontGradient = listOf(Color(0xFFF0F7FF), Color(0xFFDBEAFE)),
        frontBorder = Color(0xFF93C5FD),
        frontBadgeBg = Color(0xFF2563EB),
        frontTitleColor = Color(0xFF1E3A8A),
        frontDividerColor = Color(0xFFBFDBFE),
        frontActionColor = Color(0xFF1D4ED8),
        frontActionBg = Color(0xFFBFDBFE).copy(alpha = 0.55f),

        backGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
        backBorder = Color(0xFFF59E0B),
        backBadgeBg = Color(0xFFD97706),
        backTitleColor = Color(0xFF78350F),
        backFormulaBorder = Color(0xFFFBBF24),
        backFormulaText = Color(0xFF92400E),
        backHintBg = Color(0xFFFFFBEB),
        backHintBorder = Color(0xFFFDE68A),
        backHintText = Color(0xFF92400E),
        backActionColor = Color(0xFFB45309),
        backActionBg = Color(0xFFFDE68A).copy(alpha = 0.55f)
    ),
    // 1: Fresh Emerald / Mint (Front) -> Sunset Tangerine (Back)
    FlipCardTheme(
        frontGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7)),
        frontBorder = Color(0xFF86EFAC),
        frontBadgeBg = Color(0xFF16A34A),
        frontTitleColor = Color(0xFF14532D),
        frontDividerColor = Color(0xFFBBF7D0),
        frontActionColor = Color(0xFF15803D),
        frontActionBg = Color(0xFFBBF7D0).copy(alpha = 0.55f),

        backGradient = listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5)),
        backBorder = Color(0xFFFB923C),
        backBadgeBg = Color(0xFFEA580C),
        backTitleColor = Color(0xFF7C2D12),
        backFormulaBorder = Color(0xFFFDBA74),
        backFormulaText = Color(0xFF9A3412),
        backHintBg = Color(0xFFFFF7ED),
        backHintBorder = Color(0xFFFED7AA),
        backHintText = Color(0xFF9A3412),
        backActionColor = Color(0xFFC2410C),
        backActionBg = Color(0xFFFED7AA).copy(alpha = 0.55f)
    ),
    // 2: Radiant Violet / Lavender (Front) -> Warm Amber (Back)
    FlipCardTheme(
        frontGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF)),
        frontBorder = Color(0xFFD8B4FE),
        frontBadgeBg = Color(0xFF7C3AED),
        frontTitleColor = Color(0xFF4C1D95),
        frontDividerColor = Color(0xFFE9D5FF),
        frontActionColor = Color(0xFF6D28D9),
        frontActionBg = Color(0xFFE9D5FF).copy(alpha = 0.55f),

        backGradient = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
        backBorder = Color(0xFFF59E0B),
        backBadgeBg = Color(0xFFD97706),
        backTitleColor = Color(0xFF78350F),
        backFormulaBorder = Color(0xFFFBBF24),
        backFormulaText = Color(0xFF92400E),
        backHintBg = Color(0xFFFFFBEB),
        backHintBorder = Color(0xFFFDE68A),
        backHintText = Color(0xFF92400E),
        backActionColor = Color(0xFFB45309),
        backActionBg = Color(0xFFFDE68A).copy(alpha = 0.55f)
    ),
    // 3: Coral Rose (Front) -> Teal Cyan (Back)
    FlipCardTheme(
        frontGradient = listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6)),
        frontBorder = Color(0xFFFDA4AF),
        frontBadgeBg = Color(0xFFE11D48),
        frontTitleColor = Color(0xFF881337),
        frontDividerColor = Color(0xFFFECDD3),
        frontActionColor = Color(0xFFBE123C),
        frontActionBg = Color(0xFFFECDD3).copy(alpha = 0.55f),

        backGradient = listOf(Color(0xFFF0FDFA), Color(0xFFCCFBF1)),
        backBorder = Color(0xFF2DD4BF),
        backBadgeBg = Color(0xFF0D9488),
        backTitleColor = Color(0xFF134E4A),
        backFormulaBorder = Color(0xFF5EEAD4),
        backFormulaText = Color(0xFF115E59),
        backHintBg = Color(0xFFF0FDFA),
        backHintBorder = Color(0xFF99F6E4),
        backHintText = Color(0xFF115E59),
        backActionColor = Color(0xFF0F766E),
        backActionBg = Color(0xFF99F6E4).copy(alpha = 0.55f)
    )
)

@Composable
private fun FlipCardItemView(card: FlipCardItem, index: Int, totalCards: Int) {
    var isFlipped by remember(card.id) { mutableStateOf(false) }
    val density = LocalDensity.current.density
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "FlipAnimation_${card.id}"
    )

    val theme = FLIP_CARD_THEMES[index % FLIP_CARD_THEMES.size]

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                if (rotation != 0f) {
                    rotationY = rotation
                    cameraDistance = 14f * density
                }
            }
    ) {
        if (rotation <= 90f) {
            // FRONT FACE
            Card(
                onClick = { isFlipped = !isFlipped },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, theme.frontBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(theme.frontGradient))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.frontBadgeBg
                            ) {
                                Text(
                                    text = "KARTU ${index + 1}",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = theme.frontBadgeText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                                )
                            }

//                            Surface(
//                                shape = RoundedCornerShape(100.dp),
//                                color = theme.frontActionBg
//                            ) {
//                                Row(
//                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Default.Refresh,
//                                        contentDescription = "Balik",
//                                        tint = theme.frontActionColor,
//                                        modifier = Modifier.size(13.dp)
//                                    )
//                                    Text(
//                                        text = "Ketuk untuk Membalik",
//                                        fontFamily = interFontFamily,
//                                        fontSize = 11.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = theme.frontActionColor
//                                    )
//                                }
//                            }
                        }

                        if (card.frontTitle.isNotBlank()) {
                            Text(
                                text = card.frontTitle,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = theme.frontTitleColor
                            )
                        }

                        HorizontalDivider(
                            color = theme.frontDividerColor,
                            thickness = 1.dp
                        )

                        // Front Text with KaTeX support
                        val fHasMath = remember(card.frontText) {
                            card.frontText.contains("$") || card.frontText.contains("\\")
                        }
                        if (fHasMath) {
                            MathText(
                                text = card.frontText,
                                fontSize = 14,
                                color = theme.frontTextColor,
                                textAlign = "justify"
                            )
                        } else {
                            Text(
                                text = card.frontText,
                                fontFamily = interFontFamily,
                                fontSize = 14.sp,
                                color = theme.frontTextColor,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Justify
                            )
                        }
                    }

                    // Click overlay to ensure tapping the card always flips even over WebView
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { isFlipped = !isFlipped }
                            )
                    )
                }
            }
        } else {
            // BACK FACE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = 180f
                    }
            ) {
                Card(
                    onClick = { isFlipped = !isFlipped },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.8.dp, theme.backBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.verticalGradient(theme.backGradient))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = theme.backBadgeBg
                                ) {
                                    Text(
                                        text = "RUMUS & JAWABAN",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = theme.backBadgeText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = theme.backActionBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Balik",
                                            tint = theme.backActionColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Kembali ke Pertanyaan",
                                            fontFamily = interFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.backActionColor
                                        )
                                    }
                                }
                            }

                            if (card.backTitle.isNotBlank()) {
                                Text(
                                    text = card.backTitle,
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = theme.backTitleColor
                                )
                            }

                            // Formula Container (White card with colored border for maximum math formula legibility)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = theme.backFormulaBg,
                                border = BorderStroke(1.2.dp, theme.backFormulaBorder),
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MathText(
                                        text = card.backText,
                                        fontSize = 16,
                                        color = theme.backFormulaText,
                                        textAlign = "center",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Back Hint / Explanation
                            if (!card.backHint.isNullOrBlank()) {
                                val hintText = card.backHint
                                val hintHasMath = remember(hintText) {
                                    hintText.contains("$") || hintText.contains("\\")
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = theme.backHintBg,
                                    border = BorderStroke(1.dp, theme.backHintBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "Petunjuk",
                                            tint = theme.backActionColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        if (hintHasMath) {
                                            MathText(
                                                text = hintText,
                                                fontSize = 12,
                                                color = theme.backHintText
                                            )
                                        } else {
                                            Text(
                                                text = hintText,
                                                fontFamily = interFontFamily,
                                                fontSize = 12.sp,
                                                color = theme.backHintText,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Click overlay to flip back
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { isFlipped = !isFlipped }
                                )
                        )
                    }
                }
            }
        }
    }
}
