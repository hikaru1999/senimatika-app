package com.LambdaProject.MathArt.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.data.model.SENIMATIKA_LEVELS
import com.LambdaProject.MathArt.interFontFamily

/**
 * Custom Tag Shape (Bentuk Tag dengan ujung kiri meruncing/chamfered dan lubang eyelet tag)
 */
val LevelTagShape: Shape = GenericShape { size, _ ->
    val notch = (size.height * 0.32f).coerceAtMost(10f * 3.5f)
    val cornerRadius = 6f * 3.5f

    // Start from top-notch point
    moveTo(notch, 0f)
    // Top line to top-right corner
    lineTo(size.width - cornerRadius, 0f)
    // Top-right rounded corner
    quadraticTo(size.width, 0f, size.width, cornerRadius)
    // Right vertical line
    lineTo(size.width, size.height - cornerRadius)
    // Bottom-right rounded corner
    quadraticTo(size.width, size.height, size.width - cornerRadius, size.height)
    // Bottom line to bottom-notch point
    lineTo(notch, size.height)
    // Bottom-left angled cut to center-left point
    lineTo(0f, size.height / 2f)
    // Center-left point back to top-notch
    lineTo(notch, 0f)
    close()
}

enum class TagBadgeSize {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * LevelTagBadge Composable
 * Menampilkan Tag pemain dengan shape berbentuk tag kustom.
 * Desain dan style tag selalu otentik sesuai jenis tag itu sendiri (Level 1..15).
 * Khusus Level 10 - Level 15 memiliki styling ultra mewah, legendary, dan animasi partikel/shimmer.
 * Mendukung batasan lebar (width constraint) dan running text (marquee) jika teks melebihi ruang tag.
 */
@Composable
fun LevelTagBadge(
    tag: String,
    level: Int? = null,
    modifier: Modifier = Modifier,
    size: TagBadgeSize = TagBadgeSize.MEDIUM,
    maxWidth: Dp? = null,
    isAnimated: Boolean = true
) {
    if (tag.isBlank()) return
    // Resolusi level berdasarkan nama Tag itu sendiri (agar style tag selalu otentik sesuai jenis tag-nya)
    val resolvedLevel = SENIMATIKA_LEVELS.find { 
        it.title.equals(tag, ignoreCase = true) 
    }?.level ?: (level ?: 1).coerceIn(1, 15)

    val safeLevel = resolvedLevel.coerceIn(1, 15)
    val isLuxury = safeLevel >= 10

    val infiniteTransition = rememberInfiniteTransition(label = "tag_badge_anim")

    val shimmerOffset by if (isAnimated && isLuxury) {
        infiniteTransition.animateFloat(
            initialValue = -200f,
            targetValue = 600f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "tag_shimmer"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val luxuryPulse by if (isAnimated && isLuxury) {
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "luxury_pulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val style = getTagStyle(safeLevel, tag)

    val dimens = when (size) {
        TagBadgeSize.SMALL -> TagDimens(9.5.sp, 10.dp, 7.dp, 6.dp, 2.5.dp, 85.dp)
        TagBadgeSize.MEDIUM -> TagDimens(11.sp, 12.dp, 9.dp, 8.dp, 3.5.dp, 120.dp)
        TagBadgeSize.LARGE -> TagDimens(13.sp, 15.dp, 12.dp, 10.dp, 5.dp, 150.dp)
    }

    val (fontSize, iconSize, hPaddingStart, hPaddingEnd, vPadding, defaultMaxWidth) = dimens

    val effectiveMaxWidth = maxWidth ?: defaultMaxWidth

    val borderBrush = if (isLuxury) {
        Brush.linearGradient(
            colors = style.borderColors,
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 180f, 80f)
        )
    } else {
        Brush.linearGradient(colors = style.borderColors)
    }

    Surface(
        modifier = modifier
            .widthIn(max = effectiveMaxWidth)
            .graphicsLayer {
                if (isLuxury) {
                    scaleX = luxuryPulse
                    scaleY = luxuryPulse
                }
            }
            .shadow(
                elevation = if (isLuxury) 8.dp else 2.dp,
                shape = LevelTagShape,
                spotColor = style.glowColor.copy(alpha = if (isLuxury) 0.65f else 0.25f)
            ),
        shape = LevelTagShape,
        color = Color.Transparent,
        border = BorderStroke(if (isLuxury) 1.5.dp else 1.dp, borderBrush)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(colors = style.backgroundColors))
                .padding(start = hPaddingStart, end = hPaddingEnd, top = vPadding, bottom = vPadding),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Eyelet Hole Dot (Ciri Khas Tag)
                Box(
                    modifier = Modifier
                        .size(if (size == TagBadgeSize.SMALL) 4.dp else 5.dp)
                        .background(style.eyeletColor, CircleShape)
                        .border(
                            0.75.dp,
                            if (isLuxury) Color(0xFFFFD700).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(3.5.dp))

                // Luxury / Standard Icon
                if (style.icon != null) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = style.iconTint,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }

                // Tag Text Container with Running Text (Marquee) and Right Alignment
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            animationMode = MarqueeAnimationMode.Immediately,
                            velocity = 30.dp,
                            initialDelayMillis = 1200
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    brush = Brush.horizontalGradient(colors = style.textGradient),
                                    fontWeight = if (isLuxury) FontWeight.ExtraBold else FontWeight.Bold,
                                    letterSpacing = if (isLuxury) 0.5.sp else 0.2.sp
                                )
                            ) {
                                append(tag)
                            }
                        },
                        fontFamily = interFontFamily,
                        fontSize = fontSize,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

private data class TagDimens(
    val fontSize: TextUnit,
    val iconSize: Dp,
    val hPaddingStart: Dp,
    val hPaddingEnd: Dp,
    val vPadding: Dp,
    val defaultMaxWidth: Dp
)

private data class TagStyleConfig(
    val backgroundColors: List<Color>,
    val borderColors: List<Color>,
    val textGradient: List<Color>,
    val glowColor: Color,
    val eyeletColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val iconTint: Color
)

private fun getTagStyle(level: Int, tag: String): TagStyleConfig {
    return when (level) {
        // --- LUXURY & LEGENDARY TIERS (Level 10 - 15) ---
        15 -> { // Level 15: Scholar (The Myth & The Legend Tier) - Supernova Gold & Cosmic Ruby
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFF1E0B2B), Color(0xFF3B0764), Color(0xFF1E0B2B)),
                borderColors = listOf(Color(0xFFFFD700), Color(0xFFFF007F), Color(0xFF00E5FF), Color(0xFFFFD700)),
                textGradient = listOf(Color(0xFFFFEE58), Color(0xFFFF80AB), Color(0xFF80D8FF)),
                glowColor = Color(0xFFFF007F),
                eyeletColor = Color(0xFFFFD700),
                icon = Icons.Default.AutoAwesome,
                iconTint = Color(0xFFFFD700)
            )
        }
        14 -> { // Level 14: Sage - Imperial Emerald & Dragon Jade
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF022C22)),
                borderColors = listOf(Color(0xFF34D399), Color(0xFF6EE7B7), Color(0xFFFDE047), Color(0xFF34D399)),
                textGradient = listOf(Color(0xFFA7F3D0), Color(0xFF34D399), Color(0xFFFDE047)),
                glowColor = Color(0xFF10B981),
                eyeletColor = Color(0xFF6EE7B7),
                icon = Icons.Default.WorkspacePremium,
                iconTint = Color(0xFF34D399)
            )
        }
        13 -> { // Level 13: Prodigy - Cyberpunk Neon Cyan & Electric Magenta
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFF0F172A), Color(0xFF311042), Color(0xFF0F172A)),
                borderColors = listOf(Color(0xFF38BDF8), Color(0xFFF43F5E), Color(0xFFA855F7), Color(0xFF38BDF8)),
                textGradient = listOf(Color(0xFF7DD3FC), Color(0xFFF472B6), Color(0xFFC084FC)),
                glowColor = Color(0xFFE11D48),
                eyeletColor = Color(0xFF38BDF8),
                icon = Icons.Default.ElectricBolt,
                iconTint = Color(0xFF38BDF8)
            )
        }
        12 -> { // Level 12: Grandmaster - Radiant Sunburst Royal Amber Gold
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFF291500), Color(0xFF451A03), Color(0xFF291500)),
                borderColors = listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFFFD700)),
                textGradient = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFFDE047)),
                glowColor = Color(0xFFD97706),
                eyeletColor = Color(0xFFFFD700),
                icon = Icons.Default.MilitaryTech,
                iconTint = Color(0xFFFFD700)
            )
        }
        11 -> { // Level 11: Virtuoso - Astral Royal Indigo & Mystic Violet
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFF1E1B4B), Color(0xFF2E1065), Color(0xFF1E1B4B)),
                borderColors = listOf(Color(0xFF818CF8), Color(0xFFC084FC), Color(0xFFE879F9), Color(0xFF818CF8)),
                textGradient = listOf(Color(0xFFC7D2FE), Color(0xFFE879F9), Color(0xFFA5B4FC)),
                glowColor = Color(0xFF7C3AED),
                eyeletColor = Color(0xFFC084FC),
                icon = Icons.Default.WorkspacePremium,
                iconTint = Color(0xFFC084FC)
            )
        }
        10 -> { // Level 10: Expert - Deep Platinum Sapphire
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A)),
                borderColors = listOf(Color(0xFF60A5FA), Color(0xFFE2E8F0), Color(0xFF93C5FD), Color(0xFF60A5FA)),
                textGradient = listOf(Color(0xFFFFFFFF), Color(0xFF93C5FD), Color(0xFFBFDBFE)),
                glowColor = Color(0xFF3B82F6),
                eyeletColor = Color(0xFFE2E8F0),
                icon = Icons.Default.Diamond,
                iconTint = Color(0xFF93C5FD)
            )
        }

        // --- STANDARD TIERS (Level 1 - 9) ---
        9 -> { // Mathematician
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFEFF6FF), Color(0xFFEEF2FF)),
                borderColors = listOf(Color(0xFF6366F1), Color(0xFF818CF8)),
                textGradient = listOf(Color(0xFF4338CA), Color(0xFF4F46E5)),
                glowColor = Color(0xFF6366F1),
                eyeletColor = Color(0xFF6366F1),
                icon = Icons.Default.Star,
                iconTint = Color(0xFF4F46E5)
            )
        }
        8 -> { // Trailblazer
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5)),
                borderColors = listOf(Color(0xFFF97316), Color(0xFFFB923C)),
                textGradient = listOf(Color(0xFFC2410C), Color(0xFFEA580C)),
                glowColor = Color(0xFFF97316),
                eyeletColor = Color(0xFFF97316),
                icon = Icons.Default.Explore,
                iconTint = Color(0xFFEA580C)
            )
        }
        7 -> { // Strategist
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE)),
                borderColors = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)),
                textGradient = listOf(Color(0xFF6D28D9), Color(0xFF7C3AED)),
                glowColor = Color(0xFF8B5CF6),
                eyeletColor = Color(0xFF8B5CF6),
                icon = Icons.Default.Psychology,
                iconTint = Color(0xFF7C3AED)
            )
        }
        6 -> { // Solver
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)),
                borderColors = listOf(Color(0xFF10B981), Color(0xFF34D399)),
                textGradient = listOf(Color(0xFF047857), Color(0xFF059669)),
                glowColor = Color(0xFF10B981),
                eyeletColor = Color(0xFF10B981),
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF059669)
            )
        }
        5 -> { // Thinker
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFFDF4FF), Color(0xFFFAE8FF)),
                borderColors = listOf(Color(0xFFD946EF), Color(0xFFE879F9)),
                textGradient = listOf(Color(0xFFA21CAF), Color(0xFFC026D3)),
                glowColor = Color(0xFFD946EF),
                eyeletColor = Color(0xFFD946EF),
                icon = Icons.Default.Lightbulb,
                iconTint = Color(0xFFC026D3)
            )
        }
        4 -> { // Explorer
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFECFEFF), Color(0xFFCFFAFE)),
                borderColors = listOf(Color(0xFF06B6D4), Color(0xFF22D3EE)),
                textGradient = listOf(Color(0xFF0E7490), Color(0xFF0891B2)),
                glowColor = Color(0xFF06B6D4),
                eyeletColor = Color(0xFF06B6D4),
                icon = Icons.Default.CompassCalibration,
                iconTint = Color(0xFF0891B2)
            )
        }
        3 -> { // Learner
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7)),
                borderColors = listOf(Color(0xFF22C55E), Color(0xFF4ADE80)),
                textGradient = listOf(Color(0xFF15803D), Color(0xFF16A34A)),
                glowColor = Color(0xFF22C55E),
                eyeletColor = Color(0xFF22C55E),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconTint = Color(0xFF16A34A)
            )
        }
        2 -> { // Apprentice
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
                borderColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                textGradient = listOf(Color(0xFFB45309), Color(0xFFD97706)),
                glowColor = Color(0xFFF59E0B),
                eyeletColor = Color(0xFFF59E0B),
                icon = Icons.Default.School,
                iconTint = Color(0xFFD97706)
            )
        }
        else -> { // Level 1: Novice
            TagStyleConfig(
                backgroundColors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)),
                borderColors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)),
                textGradient = listOf(Color(0xFF1D4ED8), Color(0xFF2563EB)),
                glowColor = Color(0xFF3B82F6),
                eyeletColor = Color(0xFF3B82F6),
                icon = Icons.Default.Star,
                iconTint = Color(0xFF2563EB)
            )
        }
    }
}
