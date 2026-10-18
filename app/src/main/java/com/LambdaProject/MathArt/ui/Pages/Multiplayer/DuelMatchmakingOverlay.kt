package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.AppAudioManager
import com.LambdaProject.MathArt.data.model.BotOpponent
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.LevelTagBadge
import com.LambdaProject.MathArt.ui.components.TagBadgeSize
import com.LambdaProject.MathArt.utils.UserAvatarWithFrame
import kotlinx.coroutines.delay

@Composable
fun DuelMatchmakingOverlay(
    playerName: String,
    playerAvatarRes: Int,
    playerFrameId: String? = null,
    playerTitle: String = "Novice",
    playerLevel: Int = 1,
    totalQuestions: Int = 0,
    onMatchFound: (BotOpponent) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchState by remember { mutableStateOf("searching") } // "searching" -> "found" -> "starting"
    var currentDisplayBot by remember { mutableStateOf(BotOpponent.BOT_PRESETS.random()) }
    val selectedBot = remember { BotOpponent.getRandomBot() }
    var countdown by remember { mutableIntStateOf(5) }

    // Subtle pulsing animation for matchmaking radar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val vsScale = remember { Animatable(0f) }

    DisposableEffect(Unit) {
        onDispose {
            AppAudioManager.stopMatchmakingSearching()
        }
    }

    LaunchedEffect(Unit) {
        // Cycle random bot avatars for exactly 3.5 seconds (14 x 250ms), starting immediately
        val searchCycles = 14
        for (i in 0 until searchCycles) {
            currentDisplayBot = BotOpponent.BOT_PRESETS.random()
            AppAudioManager.playMatchmakingSearching()
            delay(250L)
        }

        AppAudioManager.stopMatchmakingSearching()
        currentDisplayBot = selectedBot
        searchState = "found"
        AppAudioManager.playMatchFound()
        vsScale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        vsScale.animateTo(1f)

        // 5-Second Countdown after match is found
        countdown = 5
        while (countdown > 0) {
            delay(1000L)
            countdown -= 1
        }

        searchState = "starting"
        delay(350)
        onMatchFound(selectedBot)
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(350)) + scaleIn(initialScale = 0.96f, animationSpec = tween(350)),
        exit = fadeOut(tween(250)) + scaleOut(targetScale = 1.04f, animationSpec = tween(250)),
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A), // Dark slate
                            Color(0xFF0C4A6E), // Deep ocean
                            Color(0xFF0284C7)  // Vibrant blue
                        )
                    )
                )
        ) {
            // Cancel button (top right)
            if (searchState == "searching") {
                IconButton(
                    onClick = {
                        AppAudioManager.stopMatchmakingSearching()
                        onCancel()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 16.dp, end = 20.dp)
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Batal",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Main Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                // Top placeholder to balance vertical spacing
                Spacer(Modifier.height(24.dp))

                // Center Section: Title, Subtitle, and Duel Matchup Showcase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (searchState) {
                            "searching" -> "Mencari Lawan Duel..."
                            "found" -> "Lawan Ditemukan!"
                            else -> "Memasuki Arena..."
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = interFontFamily,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    val roundSubtitle = if (totalQuestions > 0) {
                        val qPerRound = DuelRoundHelper.getQuestionsPerRound(totalQuestions)
                        val totalRounds = DuelRoundHelper.getTotalRounds(totalQuestions)

                        if (qPerRound > 1) {
                            "Format: $totalRounds Ronde ($qPerRound Soal/Ronde)"
                        } else {
                            "Format: $totalRounds Ronde (1 Soal/Ronde)"
                        }
//                        if (qPerRound > 1) {
//                            "Format: $totalRounds Ronde ($qPerRound Soal/Ronde) • Total $totalQuestions Soal"
//                        } else {
//                            "Format: $totalRounds Ronde (1 Soal/Ronde) • Total $totalQuestions Soal"
//                        }
                    } else {
                        "Bersiaplah, duel akan segera dimulai!"
                    }

                    Text(
                        text = if (searchState == "searching") "Menghubungkan ke sistem..." else roundSubtitle,
                        fontSize = 14.sp,
                        fontFamily = interFontFamily,
                        color = Color(0xFFBAE6FD),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(36.dp))

                    // Duel Matchup Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player Avatar & Info
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(110.dp)
                        ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatarWithFrame(
                                avatarRes = playerAvatarRes,
                                frameId = playerFrameId,
                                avatarSize = 88.dp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = playerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

//                        Surface(
//                            shape = RoundedCornerShape(12.dp),
//                            color = Color.White.copy(alpha = 0.2f)
//                        ) {
//                            Text(
//                                text = "Kamu",
//                                fontSize = 11.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.White,
//                                fontFamily = interFontFamily,
//                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
//                            )
//                        }

                          LevelTagBadge(
                                tag = playerTitle,
                                level = playerLevel,
                                size = TagBadgeSize.SMALL,
                                isAnimated = true
                          )

                    }

                    // VS Emblem
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(if (searchState != "searching") vsScale.value else 1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .scale(if (searchState == "searching") pulseScale else 1f)
                                .shadow(8.dp, CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                                    ),
                                    CircleShape
                                )
                                .border(2.dp, Color(0xFFFEF08A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "VS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = Color(0xFFFDE047)
                        )
                    }

                    // Opponent Avatar & Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(110.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(96.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsing radar ring behind opponent while searching
                            if (searchState == "searching") {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .scale(pulseScale)
                                        .border(2.dp, Color.White.copy(alpha = pulseAlpha), CircleShape)
                                )
                            }

                            UserAvatarWithFrame(
                                avatarRes = currentDisplayBot.avatarRes,
                                frameId = if (searchState != "searching") currentDisplayBot.frameId else null,
                                avatarSize = 88.dp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = if (searchState == "searching") "Mencari..." else currentDisplayBot.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (searchState != "searching") Color(0xFFE11D48) else Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (searchState == "searching") "..." else currentDisplayBot.difficulty.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 3. Bottom Progress / Countdown Indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    if (searchState == "searching") {
                        LinearProgressIndicator(
                            color = Color(0xFF38BDF8),
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Mulai dalam $countdown detik",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f))
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    )
                                ) {
                                    append("Math ")
                                }
                                withStyle(
                                    SpanStyle(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF2563EB), // Royal Blue
                                                Color(0xFF0284C7), // Sky Blue
                                                Color(0xFF06B6D4)  // Cyan
                                            )
                                        ),
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    )
                                ) {
                                    append("Arena")
                                }
                            },
                            fontFamily = interFontFamily,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}


