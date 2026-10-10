package com.LambdaProject.MathArt.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.interFontFamily

/**
 * Animated Wordart Emblem Composable for Senimatika
 * Fitur:
 * - Batasan lebar kapsul (max width)
 * - Animasi running text (Marquee) otomatis jika teks melebihi lebar kapsul
 * - Efek ambient glow, shimmer sweep, dan breathing pulse
 */
@Composable
fun LevelEmblem(
    emblemName: String,
    modifier: Modifier = Modifier,
    size: EmblemSize = EmblemSize.MEDIUM,
    isAnimated: Boolean = true
) {
    if (emblemName.isBlank() || emblemName.equals("Novice", ignoreCase = true)) return

    val infiniteTransition = rememberInfiniteTransition(label = "emblem_anim")

    val pulseScale by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emblem_pulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val shimmerOffset by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = -150f,
            targetValue = 600f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "emblem_shimmer"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val glowAlpha by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.40f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emblem_glow"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.5f) }
    }

    val styleConfig = getEmblemStyle(emblemName)

    val (fontSize, iconSize, hPadding, vPadding, radius, maxCapsuleWidth) = when (size) {
        EmblemSize.SMALL -> EmblemDimens(9.sp, 11.dp, 8.dp, 3.dp, 999.dp, 115.dp)
        EmblemSize.MEDIUM -> EmblemDimens(10.5.sp, 13.dp, 10.dp, 4.dp, 999.dp, 140.dp)
        EmblemSize.LARGE -> EmblemDimens(12.5.sp, 16.dp, 14.dp, 5.5.dp, 999.dp, 175.dp)
    }

    val borderBrush = Brush.linearGradient(
        colors = styleConfig.borderColors,
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 180f, 60f)
    )

    Surface(
        modifier = modifier
            .widthIn(max = maxCapsuleWidth)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(radius),
                spotColor = styleConfig.glowColor.copy(alpha = glowAlpha)
            ),
        shape = RoundedCornerShape(radius),
        color = Color.Transparent,
        border = BorderStroke(1.25.dp, borderBrush)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = styleConfig.backgroundColors
                    )
                )
                .padding(horizontal = hPadding, vertical = vPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(fraction = 1f)
            ) {
                Icon(
                    imageVector = styleConfig.icon,
                    contentDescription = null,
                    tint = styleConfig.iconTint,
                    modifier = Modifier.size(iconSize)
                )

                // Running Text (Marquee) container for long emblem titles
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            animationMode = MarqueeAnimationMode.Immediately,
                            velocity = 32.dp,
                            initialDelayMillis = 1200
                        )
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    brush = Brush.horizontalGradient(
                                        colors = styleConfig.textGradient
                                    ),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.3.sp
                                )
                            ) {
                                append(emblemName.uppercase())
                            }
                        },
                        fontFamily = interFontFamily,
                        fontSize = fontSize,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

enum class EmblemSize {
    SMALL,
    MEDIUM,
    LARGE
}

private data class EmblemDimens(
    val fontSize: TextUnit,
    val iconSize: Dp,
    val hPadding: Dp,
    val vPadding: Dp,
    val radius: Dp,
    val maxCapsuleWidth: Dp
)

data class EmblemStyleConfig(
    val backgroundColors: List<Color>,
    val borderColors: List<Color>,
    val textGradient: List<Color>,
    val glowColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconTint: Color
)

/**
 * Styling WordArt typography untuk Username di DashboardHeader
 */
data class UsernameWordArtConfig(
    val textGradient: List<Color>,
    val glowColor: Color,
    val subtitleAccent: Color
)

fun getEmblemStyle(name: String): EmblemStyleConfig {
    return when {
        // Level 15: The Myth & The Legend (Supernova Ungu Gelap & Imperial Golden)
        name.contains("Legend", ignoreCase = true) || name.contains("Myth", ignoreCase = true) -> {
            EmblemStyleConfig(
                backgroundColors = listOf(Color(0xFF1B072B), Color(0xFF2E1065), Color(0xFF1B072B)),
                borderColors = listOf(Color(0xFFFBBF24), Color(0xFF7E22CE), Color(0xFFF59E0B), Color(0xFFFBBF24)),
                textGradient = listOf(Color(0xFFFDE047), Color(0xFFE9D5FF), Color(0xFFFBBF24)),
                glowColor = Color(0xFFF59E0B),
                icon = Icons.Default.AutoAwesome,
                iconTint = Color(0xFFFBBF24)
            )
        }
        // Level 11: Seal of Mastery (Regal Astral Royal Purple & Silver Starlight)
        name.contains("Mastery", ignoreCase = true) || name.contains("Virtuoso", ignoreCase = true) -> {
            EmblemStyleConfig(
                backgroundColors = listOf(Color(0xFF150B2E), Color(0xFF261057), Color(0xFF150B2E)),
                borderColors = listOf(Color(0xFF818CF8), Color(0xFFC084FC), Color(0xFFE879F9), Color(0xFF818CF8)),
                textGradient = listOf(Color(0xFFC7D2FE), Color(0xFFE879F9), Color(0xFFA5B4FC)),
                glowColor = Color(0xFF8B5CF6),
                icon = Icons.Default.WorkspacePremium,
                iconTint = Color(0xFFC084FC)
            )
        }
        // Level 9: Order of Numbers (Cyber Matrix Cyan & Golden Ratio Neon)
        name.contains("Order", ignoreCase = true) || name.contains("Number", ignoreCase = true) -> {
            EmblemStyleConfig(
                backgroundColors = listOf(Color(0xFF061826), Color(0xFF0B2942), Color(0xFF061826)),
                borderColors = listOf(Color(0xFF38BDF8), Color(0xFF22D3EE), Color(0xFFFACC15), Color(0xFF38BDF8)),
                textGradient = listOf(Color(0xFF7DD3FC), Color(0xFF38BDF8), Color(0xFFFEF08A)),
                glowColor = Color(0xFF0284C7),
                icon = Icons.Default.Functions,
                iconTint = Color(0xFF38BDF8)
            )
        }
        // Level 6: Breaker of Barriers (Molten Crimson Fire & Amber Warrior)
        name.contains("Barrier", ignoreCase = true) || name.contains("Breaker", ignoreCase = true) -> {
            EmblemStyleConfig(
                backgroundColors = listOf(Color(0xFF2E0909), Color(0xFF4A1010), Color(0xFF2E0909)),
                borderColors = listOf(Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFFBBF24), Color(0xFFEF4444)),
                textGradient = listOf(Color(0xFFFCA5A5), Color(0xFFF87171), Color(0xFFFBBF24)),
                glowColor = Color(0xFFDC2626),
                icon = Icons.Default.Shield,
                iconTint = Color(0xFFF87171)
            )
        }
        // Level 5: Crystal of Insight (Radiant Amethyst & Prismatic Insight)
        name.contains("Crystal", ignoreCase = true) || name.contains("Insight", ignoreCase = true) -> {
            EmblemStyleConfig(
                backgroundColors = listOf(Color(0xFF280B38), Color(0xFF3F1357), Color(0xFF280B38)),
                borderColors = listOf(Color(0xFFD946EF), Color(0xFFA855F7), Color(0xFFF472B6), Color(0xFFD946EF)),
                textGradient = listOf(Color(0xFFF0ABFC), Color(0xFFE879F9), Color(0xFFF472B6)),
                glowColor = Color(0xFFC026D3),
                icon = Icons.Default.Lightbulb,
                iconTint = Color(0xFFE879F9)
            )
        }
        // Level 3: Curious Eye (Emerald Jade & All-Seeing Eye)
        name.contains("Curious", ignoreCase = true) || name.contains("Eye", ignoreCase = true) -> {
            EmblemStyleConfig(
                backgroundColors = listOf(Color(0xFF05291E), Color(0xFF083D2D), Color(0xFF05291E)),
                borderColors = listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF6EE7B7), Color(0xFF34D399)),
                textGradient = listOf(Color(0xFFA7F3D0), Color(0xFF34D399), Color(0xFF6EE7B7)),
                glowColor = Color(0xFF059669),
                icon = Icons.Default.Visibility,
                iconTint = Color(0xFF34D399)
            )
        }
        // Level 2: Book of Beginnings (Ancient Sapphire & Golden Parchment)
        name.contains("Book", ignoreCase = true) || name.contains("Beginning", ignoreCase = true) -> {
            EmblemStyleConfig(
                backgroundColors = listOf(Color(0xFF0C192E), Color(0xFF162A4D), Color(0xFF0C192E)),
                borderColors = listOf(Color(0xFF60A5FA), Color(0xFFF59E0B), Color(0xFF93C5FD), Color(0xFF60A5FA)),
                textGradient = listOf(Color(0xFFBFDBFE), Color(0xFF93C5FD), Color(0xFFFDE68A)),
                glowColor = Color(0xFF2563EB),
                icon = Icons.Default.AutoStories,
                iconTint = Color(0xFF93C5FD)
            )
        }
        // Default / Apprentice / Solver / Virtuoso fallback
        else -> {
            EmblemStyleConfig(
                backgroundColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A)),
                borderColors = listOf(Color(0xFF60A5FA), Color(0xFF93C5FD), Color(0xFF3B82F6)),
                textGradient = listOf(Color(0xFFDBEAFE), Color(0xFF93C5FD), Color(0xFF60A5FA)),
                glowColor = Color(0xFF3B82F6),
                icon = Icons.Default.Star,
                iconTint = Color(0xFF60A5FA)
            )
        }
    }
}

/**
 * Mendapatkan konfigurasi WordArt Typography untuk Username berdasarkan Emblem aktif pemain.
 */
fun getUsernameWordArtStyle(emblemName: String): UsernameWordArtConfig? {
    if (emblemName.isBlank()) return null
    return when {
        // Level 15: The Myth & The Legend (Ungu Gelap & Golden)
        emblemName.contains("Legend", ignoreCase = true) || emblemName.contains("Myth", ignoreCase = true) -> {
            UsernameWordArtConfig(
                textGradient = listOf(Color(0xFF2E1065), Color(0xFF581C87), Color(0xFFB45309), Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFF3B0764)),
                glowColor = Color(0xFFF59E0B),
                subtitleAccent = Color(0xFFFBBF24)
            )
        }
        // Level 11: Seal of Mastery
        emblemName.contains("Mastery", ignoreCase = true) || emblemName.contains("Virtuoso", ignoreCase = true) -> {
            UsernameWordArtConfig(
                textGradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFD946EF)),
                glowColor = Color(0xFF8B5CF6),
                subtitleAccent = Color(0xFF818CF8)
            )
        }
        // Level 9: Order of Numbers
        emblemName.contains("Order", ignoreCase = true) || emblemName.contains("Number", ignoreCase = true) -> {
            UsernameWordArtConfig(
                textGradient = listOf(Color(0xFF0284C7), Color(0xFF06B6D4), Color(0xFFEAB308)),
                glowColor = Color(0xFF0284C7),
                subtitleAccent = Color(0xFF06B6D4)
            )
        }
        // Level 6: Breaker of Barriers
        emblemName.contains("Barrier", ignoreCase = true) || emblemName.contains("Breaker", ignoreCase = true) -> {
            UsernameWordArtConfig(
                textGradient = listOf(Color(0xFFDC2626), Color(0xFFEA580C), Color(0xFFD97706)),
                glowColor = Color(0xFFDC2626),
                subtitleAccent = Color(0xFFEA580C)
            )
        }
        // Level 5: Crystal of Insight
        emblemName.contains("Crystal", ignoreCase = true) || emblemName.contains("Insight", ignoreCase = true) -> {
            UsernameWordArtConfig(
                textGradient = listOf(Color(0xFF9333EA), Color(0xFFC026D3), Color(0xFFDB2777)),
                glowColor = Color(0xFFC026D3),
                subtitleAccent = Color(0xFFC026D3)
            )
        }
        // Level 3: Curious Eye
        emblemName.contains("Curious", ignoreCase = true) || emblemName.contains("Eye", ignoreCase = true) -> {
            UsernameWordArtConfig(
                textGradient = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF0D9488)),
                glowColor = Color(0xFF10B981),
                subtitleAccent = Color(0xFF059669)
            )
        }
        // Level 2: Book of Beginnings
        emblemName.contains("Book", ignoreCase = true) || emblemName.contains("Beginning", ignoreCase = true) -> {
            UsernameWordArtConfig(
                textGradient = listOf(Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFFD97706)),
                glowColor = Color(0xFF2563EB),
                subtitleAccent = Color(0xFF2563EB)
            )
        }
        else -> null
    }
}
