package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.EmblemSize
import com.LambdaProject.MathArt.ui.components.LevelEmblem
import com.LambdaProject.MathArt.ui.components.LevelTagBadge
import com.LambdaProject.MathArt.ui.components.TagBadgeSize
import com.LambdaProject.MathArt.ui.components.ProgressionDebugModal
import com.LambdaProject.MathArt.utils.AvatarHelper
import com.LambdaProject.MathArt.utils.ProgressionDebugManager
import com.LambdaProject.MathArt.utils.UserAvatarWithFrame

@Composable
fun ProfileHeader(
    fullName: String,
    email: String,
    coins: Int,
    avatar: String = AvatarHelper.DEFAULT_AVATAR,
    exp: Long = 0L,
    level: Int = 1,
    activeTitle: String = "Novice",
    activeEmblem: String = "",
    activeFrame: String = "",
    claimedLevelRewards: List<Int> = emptyList(),
    unlockedTitles: List<String> = listOf("Novice"),
    unlockedEmblems: List<String> = emptyList(),
    unlockedFrames: List<String> = emptyList(),
    onClaimReward: (Int) -> Unit = {},
    onSelectTitle: (String) -> Unit = {},
    onSelectEmblem: (String) -> Unit = {},
    onSelectFrame: (String) -> Unit = {},
    onAvatarChanged: (String) -> Unit = {}
) {
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showLevelRoadModal by remember { mutableStateOf(false) }
    var showDebugModal by remember { mutableStateOf(false) }

    if (showAvatarPicker) {
        AvatarPickerModal(
            currentAvatar = avatar,
            currentFrame = activeFrame,
            unlockedFrames = unlockedFrames,
            onAvatarSelected = { newAvatar ->
                onAvatarChanged(newAvatar)
                showAvatarPicker = false
            },
            onFrameSelected = { newFrame ->
                onSelectFrame(newFrame)
            },
            onDismiss = { showAvatarPicker = false }
        )
    }

    if (showLevelRoadModal) {
        LevelRoadModal(
            currentExp = exp,
            currentLevel = level,
            claimedRewards = claimedLevelRewards,
            activeTitle = activeTitle,
            activeEmblem = activeEmblem,
            activeFrame = activeFrame,
            unlockedTitles = unlockedTitles,
            unlockedEmblems = unlockedEmblems,
            unlockedFrames = unlockedFrames,
            onClaimReward = onClaimReward,
            onSelectTitle = onSelectTitle,
            onSelectEmblem = onSelectEmblem,
            onSelectFrame = onSelectFrame,
            onDismiss = { showLevelRoadModal = false }
        )
    }

    if (showDebugModal) {
        ProgressionDebugModal(
            currentExp = exp,
            currentLevel = level,
            activeTitle = activeTitle,
            activeEmblem = activeEmblem,
            claimedRewardsCount = claimedLevelRewards.size,
            onDismiss = { showDebugModal = false }
        )
    }

    val currentLevelInfo = getLevelInfoForExp(exp)
    val (expInCurrentLvl, expNeeded) = getExpProgressInCurrentLevel(exp)

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Ambient Header Background Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2563EB).copy(alpha = 0.12f),
                            Color(0xFF6366F1).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Debug Launcher Button in Top-Right (Hanya muncul jika debug mode diaktifkan)
        if (ProgressionDebugManager.isDebugEnabled) {
            Surface(
                onClick = { showDebugModal = true },
                shape = CircleShape,
                color = if (ProgressionDebugManager.isDebugEnabled) Color(0xFF4F46E5) else Color.White,
                border = BorderStroke(
                    1.dp,
                    if (ProgressionDebugManager.isDebugEnabled) Color(0xFF818CF8) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Progression Debug Suite",
                        tint = if (ProgressionDebugManager.isDebugEnabled) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Extra top space for frame halos & crowns
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar Box with Frame & Edit Badge
            Box(
                modifier = Modifier
                    .clickable { showAvatarPicker = true }
                    .padding(4.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatarWithFrame(
                        avatarName = avatar,
                        frameId = activeFrame,
                        avatarSize = 92.dp
                    )
                }

                // Edit Avatar Button Badge
                Surface(
                    onClick = { showAvatarPicker = true },
                    shape = CircleShape,
                    color = Color(0xFF2563EB),
                    border = BorderStroke(2.5.dp, Color.White),
                    modifier = Modifier
                        .size(30.dp)
                        .offset(x = 2.dp, y = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Name
            Text(
                text = fullName.ifEmpty { "Pengguna Senimatika" },
                fontFamily = interFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Email
            Text(
                text = email.ifEmpty { "email@senimatika.id" },
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Row 1: Tag & Animated Wordart Emblem
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LevelTagBadge(
                    tag = activeTitle.ifEmpty { currentLevelInfo.title },
                    size = TagBadgeSize.MEDIUM
                )

                val currentEmblem = activeEmblem.ifEmpty { currentLevelInfo.emblemName ?: "" }
                if (currentEmblem.isNotBlank() && !currentEmblem.equals("Novice", ignoreCase = true)) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LevelEmblem(
                        emblemName = currentEmblem,
                        size = EmblemSize.SMALL
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Coins Badge Capsule
            Surface(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_coin),
                        contentDescription = "Coins",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$coins",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Horizontal Level Track Section ---
            Surface(
                onClick = { showLevelRoadModal = true },
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    // Level Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = Color(0xFF3B82F6),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$level",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Level $level: ${currentLevelInfo.title}",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                if (level < 15) {
                                    Text(
                                        text = "$expInCurrentLvl / $expNeeded EXP Ke Level Selanjutnya",
                                        fontFamily = interFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Level Progression",
                                fontFamily = interFontFamily,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Refreshed Horizontal Level Bar with Segmented Track
                    HorizontalLevelTrack(
                        currentLevel = level,
                        claimedRewards = claimedLevelRewards
                    )
                }
            }
        }
    }
}

/**
 * Horizontal Level Line with Fade-out left & right edges and 15 interactive level nodes.
 * UI Refreshed with better visual hierarchy, status markers, and breathing animations.
 */
@Composable
private fun HorizontalLevelTrack(
    currentLevel: Int,
    claimedRewards: List<Int>
) {
    // Center the current level in the scrollable view
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (currentLevel - 3).coerceAtLeast(0))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
                )
            )
            .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            itemsIndexed(SENIMATIKA_LEVELS, key = { _, it -> it.level }) { index, levelItem ->
                val nodeLevel = levelItem.level
                val isReached = currentLevel >= nodeLevel
                val isCurrent = currentLevel == nodeLevel
                val isClaimed = claimedRewards.contains(nodeLevel)
                val isCompleted = currentLevel > nodeLevel

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelNodeItem(
                        level = nodeLevel,
                        isReached = isReached,
                        isCurrent = isCurrent,
                        isClaimed = isClaimed,
                        isCompleted = isCompleted
                    )

                    if (index < SENIMATIKA_LEVELS.size - 1) {
                        // Segmented track connector centered with the node circles
                        val isPathActive = currentLevel > nodeLevel
                        Box(
                            modifier = Modifier
                                .padding(top = 10.dp) // Offset adjustment to align with node midpoint
                                .width(32.dp)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPathActive) 
                                        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
                                    else 
                                        SolidColor(Color(0xFFCBD5E1).copy(alpha = 0.5f))
                                )
                        )
                    }
                }
            }
        }

        // Left Edge Fade-Out Overlay
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFF1F5F9).copy(alpha = 0.9f), Color.Transparent)
                    )
                )
        )

        // Right Edge Fade-Out Overlay
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFF1F5F9).copy(alpha = 0.9f))
                    )
                )
        )
    }
}

@Composable
private fun LevelNodeItem(
    level: Int,
    isReached: Boolean,
    isCurrent: Boolean,
    isClaimed: Boolean,
    isCompleted: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "node_fx")
    
    val pulseScale by if (isCurrent) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    val glowAlpha by if (isCurrent) {
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(56.dp)
    ) {
        // Status badge (Gift / Check / Lock)
        Box(modifier = Modifier.height(28.dp), contentAlignment = Alignment.BottomCenter) {
            when {
                isReached && !isClaimed && level > 1 -> {
                    Surface(
                        color = Color(0xFFF59E0B),
                        shape = CircleShape,
                        modifier = Modifier.size(20.dp).shadow(3.dp, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redeem,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                isClaimed -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(17.dp)
                    )
                }
                !isReached -> {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(contentAlignment = Alignment.Center) {
            if (isCurrent) {
                // Outer Glow Aura for Active Level
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .scale(pulseScale)
                        .background(Color(0xFF3B82F6).copy(alpha = glowAlpha), CircleShape)
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape)
                )
            }

            Surface(
                modifier = Modifier
                    .size(if (isCurrent) 42.dp else 36.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .shadow(
                        elevation = if (isCurrent) 10.dp else 1.dp,
                        shape = CircleShape,
                        spotColor = if (isCurrent) Color(0xFF2563EB) else Color.Transparent
                    ),
                shape = CircleShape,
                color = when {
                    isCurrent -> Color(0xFF2563EB)
                    isCompleted -> Color(0xFF10B981)
                    isReached -> Color(0xFF34D399)
                    else -> Color.White
                },
                border = BorderStroke(
                    width = if (isCurrent) 3.dp else 2.dp,
                    color = when {
                        isCurrent -> Color(0xFFDBEAFE)
                        isReached -> Color(0xFFA7F3D0)
                        else -> Color(0xFFE2E8F0)
                    }
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted && isClaimed) {
                         Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "$level",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = if (isCurrent) 15.sp else 13.sp,
                            color = if (isReached) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        
        // Status text label
        Text(
            text = if (isCurrent) "SEKARANG" else if (isReached) "TERCAPAI" else "LOCKED",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFontFamily,
            color = if (isCurrent) Color(0xFF2563EB) else if (isReached) Color(0xFF10B981) else Color(0xFF94A3B8),
            letterSpacing = 0.5.sp
        )
    }
}
