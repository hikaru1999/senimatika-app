package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.model.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.EmblemSize
import com.LambdaProject.MathArt.ui.components.LevelEmblem
import com.LambdaProject.MathArt.ui.components.LevelTagBadge
import com.LambdaProject.MathArt.ui.components.TagBadgeSize
import com.LambdaProject.MathArt.ui.components.ProgressionDebugModal
import com.LambdaProject.MathArt.utils.AvatarFrameHelper
import com.LambdaProject.MathArt.utils.ProgressionDebugManager
import kotlinx.coroutines.launch

@Composable
fun LevelRoadModal(
    currentExp: Long,
    currentLevel: Int,
    claimedRewards: List<Int>,
    activeTitle: String,
    activeEmblem: String,
    activeFrame: String = "",
    unlockedTitles: List<String>,
    unlockedEmblems: List<String>,
    unlockedFrames: List<String> = emptyList(),
    onClaimReward: (Int) -> Unit,
    onSelectTitle: (String) -> Unit,
    onSelectEmblem: (String) -> Unit,
    onSelectFrame: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Level Road, 1: Gelar & Emblem, 2: Bingkai Avatar
    var showDebugModal by remember { mutableStateOf(false) }
    val currentLevelInfo = getLevelInfoForExp(currentExp)
    val (expInCurrentLvl, expNeeded) = getExpProgressInCurrentLevel(currentExp)
    val progressRatio = if (expNeeded > 0) (expInCurrentLvl.toFloat() / expNeeded.toFloat()).coerceIn(0f, 1f) else 1f

    if (showDebugModal) {
        ProgressionDebugModal(
            currentExp = currentExp,
            currentLevel = currentLevel,
            activeTitle = activeTitle,
            activeEmblem = activeEmblem,
            claimedRewardsCount = claimedRewards.size,
            onDismiss = { showDebugModal = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header: Title & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Informasi Level & Hadiah",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Level $currentLevel • $currentExp EXP Kumulatif",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (ProgressionDebugManager.isDebugEnabled) {
                            IconButton(
                                onClick = { showDebugModal = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (ProgressionDebugManager.isDebugEnabled) Color(0xFFEEF2FF) else Color(0xFFF1F5F9),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (ProgressionDebugManager.isDebugEnabled) Color(0xFF818CF8) else Color(0xFFCBD5E1),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = "Debug Progression",
                                    tint = if (ProgressionDebugManager.isDebugEnabled) Color(0xFF4F46E5) else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE2E8F0), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- Calling Card Gamer Banner ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF090D16),
                    border = BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8),
                                Color(0xFF6366F1),
                                Color(0xFFA855F7),
                                Color(0xFFEC4899)
                            )
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF0F172A),
                                        Color(0xFF070A12)
                                    ),
                                    radius = 900f
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            // Top Row: Level Crest + Player Title & Rank on Left, Calling Card Showcase on Right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Level Crest + Title & Subtitle
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    // Futuristic Gamer Level Crest
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(8.dp, RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                                ),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .border(
                                                1.5.dp,
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF93C5FD), Color(0xFFC084FC))
                                                ),
                                                RoundedCornerShape(14.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$currentLevel",
                                                fontFamily = interFontFamily,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                fontSize = 17.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = activeTitle.ifBlank { currentLevelInfo.title },
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            letterSpacing = 0.2.sp
                                        )

                                        Surface(
                                            color = Color(0x3338BDF8),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.5.dp, Color(0x6638BDF8))
                                        ) {
                                            Text(
                                                text = if (currentLevel < 15) "🎯 Menuju Level ${currentLevel + 1}" else "Max. Level",
                                                fontFamily = interFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF7DD3FC),
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Right: Calling Card Emblem / Tag Display
                                Box(
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (activeEmblem.isNotBlank() && !activeEmblem.equals("Novice", ignoreCase = true)) {
                                        LevelEmblem(emblemName = activeEmblem, size = EmblemSize.SMALL)
                                    } else {
                                        LevelTagBadge(
                                            tag = activeTitle.ifBlank { currentLevelInfo.title },
                                            level = currentLevel,
                                            size = TagBadgeSize.SMALL
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // EXP Progress Bar (HUD Style)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = Color(0x2260A5FA),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "EXP",
                                                fontFamily = interFontFamily,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF38BDF8),
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        Text(
                                            text = if (currentLevel < 15) {
                                                "$expInCurrentLvl / $expNeeded"
                                            } else {
                                                "$currentExp EXP"
                                            },
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 12.sp
                                        )
                                    }

                                    Surface(
                                        color = Color(0x33818CF8),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, Color(0x55818CF8))
                                    ) {
                                        Text(
                                            text = "${(progressRatio * 100).toInt()}%",
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFA5B4FC),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // HUD Segmented Progress Track
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(9.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(5.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progressRatio)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFF06B6D4),
                                                        Color(0xFF3B82F6),
                                                        Color(0xFF8B5CF6),
                                                        Color(0xFFD946EF)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs (Level Road vs Koleksi Tag & Emblem)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabButton(
                        title = "Hadiah Level",
                        isSelected = selectedTab == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 0 }
                    )
                    TabButton(
                        title = "Tag & Emblem",
                        isSelected = selectedTab == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Area
                when (selectedTab) {
                    0 -> {
                        // Level Road Timeline
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(SENIMATIKA_LEVELS, key = { it.level }) { levelItem ->
                                val isReached = currentLevel >= levelItem.level
                                // Level 1 selalu otomatis diklaim (tidak perlu klaim manual)
                                val isClaimed = claimedRewards.contains(levelItem.level) || levelItem.level == 1
                                val isCurrent = currentLevel == levelItem.level

                                LevelRoadItem(
                                    levelInfo = levelItem,
                                    isReached = isReached,
                                    isClaimed = isClaimed,
                                    isCurrent = isCurrent,
                                    onClaim = { onClaimReward(levelItem.level) }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Tag & Emblem Customizer
                        TitleAndEmblemTab(
                            currentLevel = currentLevel,
                            activeTitle = activeTitle,
                            activeEmblem = activeEmblem,
                            unlockedTitles = unlockedTitles,
                            unlockedEmblems = unlockedEmblems,
                            onSelectTitle = onSelectTitle,
                            onSelectEmblem = onSelectEmblem
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelRoadItem(
    levelInfo: LevelInfo,
    isReached: Boolean,
    isClaimed: Boolean,
    isCurrent: Boolean,
    onClaim: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = when {
            isCurrent -> Color(0xFFF0F7FF)
            isReached -> Color.White
            else -> Color(0xFFF8FAFC).copy(alpha = 0.8f)
        },
        border = BorderStroke(
            width = if (isCurrent) 2.dp else 1.dp,
            color = when {
                isCurrent -> Color(0xFF3B82F6)
                isReached -> Color(0xFFE2E8F0)
                else -> Color(0xFFF1F5F9)
            }
        ),
        shadowElevation = if (isCurrent) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. Level Node Circle ---
            Box(contentAlignment = Alignment.Center) {
                if (isCurrent) {
                    // Animasi glow untuk level saat ini
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                    ) {}
                }
                Surface(
                    color = when {
                        isCurrent -> Color(0xFF3B82F6)
                        isReached -> Color(0xFF10B981)
                        else -> Color(0xFFCBD5E1)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp),
                    shadowElevation = if (isReached) 4.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${levelInfo.level}",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // --- 2. Info Content ---
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = levelInfo.title,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isReached) Color(0xFF0F172A) else Color(0xFF64748B)
                    )

                    if (isCurrent) {
                        Surface(
                            color = Color(0xFF3B82F6),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "AKTIF",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Required EXP dengan icon kecil
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isReached) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${levelInfo.requiredExp} EXP",
                        fontFamily = interFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isReached) Color(0xFF64748B) else Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 3. Reward Badges (FlowRow) ---
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Emblem Reward
                    if (levelInfo.emblemName != null && !levelInfo.emblemName.equals("Novice", ignoreCase = true)) {
                        RewardBadge(
                            icon = R.drawable.ic_pu_magic, // Gunakan icon master emblem
                            label = levelInfo.emblemName!!,
                            bgColor = Color(0xFFEEF2FF),
                            textColor = Color(0xFF4F46E5),
                            borderColor = Color(0xFFC7D2FE),
                            isEmblem = true
                        )
                    }

                    // Exploration Unlock
                    if (levelInfo.unlocksExploration) {
                        RewardBadge(
                            icon = R.drawable.img_explore,
                            label = "Mode Eksplorasi",
                            bgColor = Color(0xFFF0FDF4),
                            textColor = Color(0xFF166534),
                            borderColor = Color(0xFFBBF7D0)
                        )
                    }

                    // Frame Reward
                    if (levelInfo.frameId != null) {
                        RewardBadge(
                            icon = AvatarFrameHelper.getFrameConfig(levelInfo.frameId)?.drawableRes ?: R.drawable.frame_1,
                            label = levelInfo.frameName ?: "Bingkai",
                            bgColor = Color(0xFFFDF4FF),
                            textColor = Color(0xFF86198F),
                            borderColor = Color(0xFFF0ABFC)
                        )
                    }

                    // Power Ups
                    levelInfo.rewardPowerUps.forEach { (puType, count) ->
                        val puIcon = when (puType) {
                            PowerUpType.FREEZE_TIMER -> R.drawable.ic_pu_freeze
                            PowerUpType.STREAK_PROTECTION -> R.drawable.ic_pu_shield
                            PowerUpType.MAGIC_KEY -> R.drawable.ic_pu_key
                            else -> R.drawable.ic_pu_vial
                        }
                        RewardBadge(
                            icon = puIcon,
                            label = "${count}x ${puType.name.replace("_", " ").lowercase().capitalize()}",
                            bgColor = Color(0xFFF1F5F9),
                            textColor = Color(0xFF334155),
                            borderColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- 4. Action State ---
            Box(contentAlignment = Alignment.Center) {
                when {
                    !isReached -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    isClaimed -> {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Text("Diklaim", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            }
                        }
                    }
                    else -> {
                        Button(
                            onClick = onClaim,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("KLAIM", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardBadge(
    icon: Int,
    label: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color,
    isEmblem: Boolean = false
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = label,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                color = textColor,
                maxLines = 1
            )

            if (isEmblem) {
                // Penanda kecil bahwa ini adalah Gelar/Badge WordArt
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                fontFamily = interFontFamily,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (isSelected) Color(0xFF0F172A) else Color(0xFF64748B)
            )
        }
    }
}

//@Composable
//private fun LevelRoadItem(
//    levelInfo: LevelInfo,
//    isReached: Boolean,
//    isClaimed: Boolean,
//    isCurrent: Boolean,
//    onClaim: () -> Unit
//) {
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        color = when {
//            isCurrent -> Color(0xFFEFF6FF)
//            isReached -> Color.White
//            else -> Color(0xFFF1F5F9).copy(alpha = 0.6f)
//        },
//        border = BorderStroke(
//            1.5.dp,
//            when {
//                isCurrent -> Color(0xFF3B82F6)
//                isReached -> Color(0xFFCBD5E1)
//                else -> Color(0xFFE2E8F0)
//            }
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(14.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Level Node Circle
//            Row(
//                modifier = Modifier.weight(1f),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Surface(
//                    color = when {
//                        isCurrent -> Color(0xFF3B82F6)
//                        isReached -> Color(0xFF10B981)
//                        else -> Color(0xFF94A3B8)
//                    },
//                    shape = CircleShape,
//                    modifier = Modifier.size(38.dp)
//                ) {
//                    Box(contentAlignment = Alignment.Center) {
//                        Text(
//                            text = "${levelInfo.level}",
//                            fontFamily = interFontFamily,
//                            fontWeight = FontWeight.Black,
//                            color = Color.White,
//                            fontSize = 15.sp
//                        )
//                    }
//                }
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(6.dp)
//                    ) {
//                        Text(
//                            text = levelInfo.title,
//                            fontFamily = interFontFamily,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 15.sp,
//                            color = if (isReached) Color(0xFF0F172A) else Color(0xFF64748B)
//                        )
//
//                        if (isCurrent) {
//                            Surface(
//                                color = Color(0xFFDBEAFE),
//                                shape = RoundedCornerShape(6.dp)
//                            ) {
//                                Text(
//                                    text = "LEVEL SAAT INI",
//                                    fontFamily = interFontFamily,
//                                    fontSize = 9.sp,
//                                    fontWeight = FontWeight.Black,
//                                    color = Color(0xFF1D4ED8),
//                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    @OptIn(ExperimentalLayoutApi::class)
//                    FlowRow(
//                        verticalArrangement = Arrangement.spacedBy(4.dp),
//                        horizontalArrangement = Arrangement.spacedBy(6.dp),
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        // Nominal EXP
//                        Text(
//                            text = "${levelInfo.requiredExp} EXP",
//                            fontFamily = interFontFamily,
//                            fontSize = 11.5.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = if (isReached) Color(0xFF334155) else Color(0xFF94A3B8),
//                            modifier = Modifier.align(Alignment.CenterVertically)
//                        )
//
//                        // Visual Emblem
//                        val hasEmblem = levelInfo.emblemName != null && !levelInfo.emblemName.equals("Novice", ignoreCase = true)
//                        if (hasEmblem) {
//                            LevelEmblem(
//                                emblemName = levelInfo.emblemName!!,
//                                size = EmblemSize.SMALL
//                            )
//                        }
//
//                        // Mode Eksplorasi Terbuka
//                        if (levelInfo.unlocksExploration) {
//                            ExplorationUnlockBadge()
//                        }
//
//                        // Bingkai Avatar Khusus
//                        if (levelInfo.frameId != null) {
//                            FrameRewardBadge(
//                                frameId = levelInfo.frameId,
//                                frameName = levelInfo.frameName ?: "Bingkai Avatar"
//                            )
//                        }
//
//                        // Power Up Badges with Images & Count
//                        if (levelInfo.rewardPowerUps.isNotEmpty()) {
//                            levelInfo.rewardPowerUps.forEach { (puType, count) ->
//                                PowerUpRewardBadge(
//                                    powerUpType = puType,
//                                    count = count
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // Action Button
//            when {
//                !isReached -> {
//                    Surface(
//                        color = Color(0xFFE2E8F0),
//                        shape = RoundedCornerShape(8.dp)
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(4.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Lock,
//                                contentDescription = null,
//                                tint = Color(0xFF94A3B8),
//                                modifier = Modifier.size(13.dp)
//                            )
//                            Text(
//                                text = "Terkunci",
//                                fontFamily = interFontFamily,
//                                fontSize = 11.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF94A3B8)
//                            )
//                        }
//                    }
//                }
//                isClaimed -> {
//                    Surface(
//                        color = Color(0xFFECFDF5),
//                        shape = RoundedCornerShape(8.dp),
//                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(4.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Check,
//                                contentDescription = null,
//                                tint = Color(0xFF059669),
//                                modifier = Modifier.size(13.dp)
//                            )
//                            Text(
//                                text = "Diklaim",
//                                fontFamily = interFontFamily,
//                                fontSize = 11.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF059669)
//                            )
//                        }
//                    }
//                }
//                else -> {
//                    Button(
//                        onClick = onClaim,
//                        shape = RoundedCornerShape(10.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFF59E0B),
//                            contentColor = Color.White
//                        ),
//                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
//                        modifier = Modifier.height(36.dp)
//                    ) {
//                        Text(
//                            text = "Klaim",
//                            fontFamily = interFontFamily,
//                            fontWeight = FontWeight.Black,
//                            fontSize = 12.sp
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
private fun TitleAndEmblemTab(
    currentLevel: Int,
    activeTitle: String,
    activeEmblem: String,
    unlockedTitles: List<String>,
    unlockedEmblems: List<String>,
    onSelectTitle: (String) -> Unit,
    onSelectEmblem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Tag (Titles)
        item {
            Text(
                text = "Koleksi Tag",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SENIMATIKA_LEVELS.forEach { levelInfo ->
                    val tag = levelInfo.title
                    val isUnlocked = tag == "Novice" || unlockedTitles.contains(tag)
                    val isActive = activeTitle == tag

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isActive) Color(0xFFEFF6FF) else Color.White,
                        border = BorderStroke(1.dp, if (isActive) Color(0xFF3B82F6) else Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                LevelTagBadge(
                                    tag = tag,
                                    level = levelInfo.level,
                                    size = TagBadgeSize.SMALL
                                )
                                Column {
                                    Text(
                                        text = "Level ${levelInfo.level}",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isUnlocked) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = if (isUnlocked) "Tag Terbuka" else "Buka di Level ${levelInfo.level}",
                                        fontFamily = interFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            if (isActive) {
                                Surface(
                                    color = Color(0xFF3B82F6),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Aktif",
                                        color = Color.White,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            } else if (isUnlocked) {
                                OutlinedButton(
                                    onClick = { onSelectTitle(tag) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Pakai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Icon(Icons.Default.Lock, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section: Emblem
        item {
            Text(
                text = "Koleksi Emblem",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val allEmblems = listOf(
                "Book of Beginnings",
                "Curious Eye",
                "Crystal of Insight",
                "Breaker of Barriers",
                "Order of Numbers",
                "Seal of Mastery",
                "The Myth & The Legend"
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allEmblems.forEach { emblem ->
                    val isUnlocked = unlockedEmblems.contains(emblem)
                    val isActive = activeEmblem == emblem

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isActive) Color(0xFFEFF6FF) else Color.White,
                        border = BorderStroke(1.dp, if (isActive) Color(0xFF3B82F6) else Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                LevelEmblem(emblemName = emblem, size = EmblemSize.SMALL)
                            }

                            if (isActive) {
                                Surface(
                                    color = Color(0xFF3B82F6),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Aktif",
                                        color = Color.White,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            } else if (isUnlocked) {
                                OutlinedButton(
                                    onClick = { onSelectEmblem(emblem) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Pakai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Icon(Icons.Default.Lock, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerUpRewardBadge(
    powerUpType: PowerUpType,
    count: Int
) {
    val iconRes = when (powerUpType) {
        PowerUpType.FREEZE_TIMER -> R.drawable.ic_pu_freeze
        PowerUpType.STREAK_PROTECTION -> R.drawable.ic_pu_shield
        PowerUpType.REMOVE_TWO_OPTIONS -> R.drawable.ic_pu_magic
        PowerUpType.HEALING_VIAL -> R.drawable.ic_pu_vial
        PowerUpType.LEATHER_STRAPS -> R.drawable.ic_pu_belt
        PowerUpType.MAGIC_KEY -> R.drawable.ic_pu_key
        PowerUpType.BINOCULAR -> R.drawable.ic_pu_binocular
        PowerUpType.LANTERN -> R.drawable.ic_pu_lantern
        PowerUpType.TORCH -> R.drawable.ic_pu_torch
    }

    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${count}x",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF334155)
            )
        }
    }
}

@Composable
private fun ExplorationUnlockBadge() {
    Surface(
        color = Color(0xFFF0FDF4),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_explore),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Eksplorasi",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF166534)
            )
        }
    }
}

@Composable
private fun FrameRewardBadge(
    frameId: String,
    frameName: String
) {
    val frameConfig = com.LambdaProject.MathArt.utils.AvatarFrameHelper.getFrameConfig(frameId) ?: return
    Surface(
        color = Color(0xFFFDF4FF),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFF0ABFC)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(id = frameConfig.drawableRes),
                contentDescription = frameName,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Bingkai $frameName",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF86198F)
            )
        }
    }
}





