package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.data.DataMaterials
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.LambdaProject.MathArt.ui.components.EmblemSize
import com.LambdaProject.MathArt.ui.components.LevelEmblem
import com.LambdaProject.MathArt.ui.components.LevelTagBadge
import com.LambdaProject.MathArt.ui.components.TagBadgeSize
import com.LambdaProject.MathArt.ui.components.getUsernameWordArtStyle

@Composable
fun DashboardHeader(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel(),
    overrideHour: Int? = null
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return
    val username by viewModel.username.collectAsState()
    val userAvatar by viewModel.avatar.collectAsState()
    val exp by viewModel.exp.collectAsState()
    val level by viewModel.level.collectAsState()
    val activeTitle by viewModel.activeTitle.collectAsState()
    val activeEmblem by viewModel.activeEmblem.collectAsState()
    val activeFrame by viewModel.activeFrame.collectAsState()
    val claimedRewards by viewModel.claimedLevelRewards.collectAsState()
    val unlockedTitles by viewModel.unlockedTitles.collectAsState()
    val unlockedEmblems by viewModel.unlockedEmblems.collectAsState()
    val unlockedFrames by viewModel.unlockedFrames.collectAsState()
    val hasNewNotification by viewModel.hasNewNotification.observeAsState(false)
    val materialStatusMap by viewModel.materialStatusMap.collectAsState()
    var showLevelRoadModal by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
        viewModel.listenForNewNotifications(userId)
    }

    if (showLevelRoadModal) {
        com.LambdaProject.MathArt.ui.Pages.Profile.LevelRoadModal(
            currentExp = exp,
            currentLevel = level,
            claimedRewards = claimedRewards,
            activeTitle = activeTitle,
            activeEmblem = activeEmblem,
            activeFrame = activeFrame,
            unlockedTitles = unlockedTitles,
            unlockedEmblems = unlockedEmblems,
            unlockedFrames = unlockedFrames,
            onClaimReward = { viewModel.claimLevelReward(userId, it) },
            onSelectTitle = { viewModel.setActiveTitle(userId, it) },
            onSelectEmblem = { viewModel.setActiveEmblem(userId, it) },
            onSelectFrame = { viewModel.setActiveFrame(userId, it) },
            onDismiss = { showLevelRoadModal = false }
        )
    }

    val activeMaterialEntry = materialStatusMap.entries.firstOrNull { it.value }
    val hasActiveSession = activeMaterialEntry != null
    val targetMaterial: MaterialItem? = if (hasActiveSession) {
        DataMaterials.find { it.id == activeMaterialEntry.key }
    } else {
        DataMaterials.firstOrNull()
    }

    // WordArt effect config based on active emblem (Novice tidak memiliki emblem/wordart)
    val wordArtStyle = if (activeEmblem.isNotBlank() && !activeEmblem.equals("Novice", ignoreCase = true)) {
        getUsernameWordArtStyle(activeEmblem)
    } else {
        null
    }

    // Real-time automatic system hour ticker (updates every 30 seconds)
    val currentSystemHour by produceState(initialValue = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        while (true) {
            kotlinx.coroutines.delay(30_000L)
            value = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        }
    }

    // Greeting & Ikon dinamis berdasarkan 4 waktu (Pagi Sunrise, Siang Daylight, Sore Sunset, Malam Night)
    val hour = overrideHour ?: currentSystemHour
    val skyTheme = when (hour) {
        in 4..10 -> DashboardSkyTheme.PAGI
        in 11..14 -> DashboardSkyTheme.SIANG
        in 15..17 -> DashboardSkyTheme.SORE
        else -> DashboardSkyTheme.MALAM
    }

    val greetingTime = when (skyTheme) {
        DashboardSkyTheme.PAGI -> "Selamat pagi,"
        DashboardSkyTheme.SIANG -> "Selamat siang,"
        DashboardSkyTheme.SORE -> "Selamat sore,"
        DashboardSkyTheme.MALAM -> "Selamat malam,"
    }

    val (timeIcon, timeIconColor) = when (skyTheme) {
        DashboardSkyTheme.PAGI -> Icons.Filled.WbSunny to Color(0xFFF97316)          // Vibrant Sunrise Orange
        DashboardSkyTheme.SIANG -> Icons.Filled.WbSunny to Color(0xFFF59E0B)         // Radiant Golden Sun
        DashboardSkyTheme.SORE -> Icons.Filled.WbSunny to Color(0xFFEA580C)          // Fiery Sunset Orange
        DashboardSkyTheme.MALAM -> Icons.Filled.NightlightRound to Color(0xFFD97706) // Warm Amber Golden Moon
    }

    // Header gradient colors based on time (rich pastel skies with sunrise/sunset orange ambience blending into 0xFFF8FAFC)
    val headerGradientColors = when (skyTheme) {
        DashboardSkyTheme.PAGI -> listOf(
            Color(0xFFFFEDD5).copy(alpha = 0.88f), // Warm Sunrise Peach / Light Orange
            Color(0xFFE0F2FE).copy(alpha = 0.85f), // Soft Morning Sky Blue
            Color(0xFFF8FAFC)                      // Seamless blend to body background
        )
        DashboardSkyTheme.SIANG -> listOf(
            Color(0xFFDBEAFE).copy(alpha = 0.90f), // Clear Royal Sky Blue
            Color(0xFFEFF6FF).copy(alpha = 0.95f), // Pale Blue Sky
            Color(0xFFF8FAFC)                      // Seamless blend to body background
        )
        DashboardSkyTheme.SORE -> listOf(
            Color(0xFFFED7AA).copy(alpha = 0.85f), // Warm Sunset Golden Amber / Orange
            Color(0xFFFCE7F3).copy(alpha = 0.88f), // Soft Sunset Rose
            Color(0xFFEDE9FE).copy(alpha = 0.92f), // Twilight Lavender
            Color(0xFFF8FAFC)                      // Seamless blend to body background
        )
        DashboardSkyTheme.MALAM -> listOf(
            Color(0xFFE0E7FF).copy(alpha = 0.90f), // Soft Moonlit Indigo Mist
            Color(0xFFEDE9FE).copy(alpha = 0.95f), // Pale Lavender Sky
            Color(0xFFF8FAFC)                      // Seamless blend to body background
        )
    }

    val dateTextColor = when (skyTheme) {
        DashboardSkyTheme.PAGI -> Color(0xFFC2410C)
        DashboardSkyTheme.SIANG -> Color(0xFF1E40AF)
        DashboardSkyTheme.SORE -> Color(0xFF9A3412)
        DashboardSkyTheme.MALAM -> Color(0xFF4338CA)
    }

    val greetingColor = when (skyTheme) {
        DashboardSkyTheme.PAGI -> Color(0xFFEA580C)
        DashboardSkyTheme.SIANG -> Color(0xFF2563EB)
        DashboardSkyTheme.SORE -> Color(0xFFC026D3)
        DashboardSkyTheme.MALAM -> Color(0xFF4F46E5)
    }

    val bellIconTint = when (skyTheme) {
        DashboardSkyTheme.PAGI -> Color(0xFFEA580C)
        DashboardSkyTheme.SIANG -> Color(0xFF1E3A8A)
        DashboardSkyTheme.SORE -> Color(0xFFEA580C)
        DashboardSkyTheme.MALAM -> Color(0xFF4338CA)
    }

    val bellBorderColor = when (skyTheme) {
        DashboardSkyTheme.PAGI -> Color(0xFFFED7AA)
        DashboardSkyTheme.SIANG -> Color(0xFFBFDBFE)
        DashboardSkyTheme.SORE -> Color(0xFFFED7AA)
        DashboardSkyTheme.MALAM -> Color(0xFFC7D2FE)
    }

    val avatarBorderColor = when (skyTheme) {
        DashboardSkyTheme.PAGI -> Color(0xFFFB923C)
        DashboardSkyTheme.SIANG -> Color(0xFF38BDF8)
        DashboardSkyTheme.SORE -> Color(0xFFF97316)
        DashboardSkyTheme.MALAM -> Color(0xFF818CF8)
    }

    // Format Tanggal Hari Ini (e.g. "Kamis, 8 Oktober") - Otomatis ter-update secara real-time
    val currentDateText by produceState(
        initialValue = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID")).format(Date())
    ) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            value = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID")).format(Date())
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = headerGradientColors))
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)
        ) {
            // Siluet Langit Artistik (Matahari & Pendaran Sunrise Pagi / Siang / Sunset Sore / Bulan Malam)
            DashboardSkySilhouette(
                skyTheme = skyTheme,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
                // --- 1. Top Bar: Date & Notification Toggle ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Plain Clean Date Row with Dynamic Sun / Moon Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = timeIcon,
                            contentDescription = greetingTime,
                            tint = timeIconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = currentDateText,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = dateTextColor,
                            letterSpacing = 0.1.sp
                        )
                    }

                    // Notification Button with Badge
                    Box(
                        modifier = Modifier.wrapContentSize(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Surface(
                            onClick = {
                                viewModel.clearNotifications()
                                navController.navigate("notification")
                            },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, bellBorderColor),
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_bell),
                                    contentDescription = "Notifikasi",
                                    modifier = Modifier.size(18.dp),
                                    tint = bellIconTint
                                )
                            }
                        }

                        if (hasNewNotification) {
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .background(Color.White, CircleShape)
                                    .padding(2.dp)
                                    .zIndex(3f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFF334B), CircleShape)
                                        .border(0.5.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- 2. Middle Row: Greeting Typography + User Avatar on Right ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    ) {
                        Text(
                            text = greetingTime,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = wordArtStyle?.subtitleAccent ?: greetingColor,
                            lineHeight = 19.sp
                        )

                        // Username with WordArt Effect if Emblem is Active
                        if (wordArtStyle != null) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            brush = Brush.horizontalGradient(colors = wordArtStyle.textGradient),
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp,
                                            shadow = Shadow(
                                                color = wordArtStyle.glowColor.copy(alpha = 0.35f),
                                                offset = Offset(0f, 4f),
                                                blurRadius = 8f
                                            )
                                        )
                                    ) {
                                        append(username.ifEmpty { "Siswa" })
                                    }
                                },
                                fontFamily = interFontFamily,
                                fontSize = 28.sp,
                                lineHeight = 32.sp,
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = username.ifEmpty { "Siswa" },
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                color = Color(0xFF0F172A),
                                lineHeight = 30.sp,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Level, Tag & Emblem Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Level Badge (Color coded based on level tiers)
                            val (lvlBadgeBg, lvlBadgeTextColor, lvlBadgeBorder) = when {
                                level in 1..5 -> Triple(Color(0xFF14532D), Color.White, null) // Hijau tua
                                level in 6..10 -> Triple(Color(0xFF2563EB), Color.White, null) // Biru
                                level in 11..14 -> Triple(Color(0xFFC026D3), Color.White, null) // Magenta
                                level >= 15 -> Triple(Color(0xFF2E1065), Color(0xFFFBBF24), BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.7f))) // Ungu gelap - golden
                                else -> Triple(Color(0xFF14532D), Color.White, null)
                            }

                            Surface(
                                color = lvlBadgeBg,
                                shape = RoundedCornerShape(8.dp),
                                border = lvlBadgeBorder
                            ) {
                                Text(
                                    text = "$level",
                                    fontFamily = interFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = lvlBadgeTextColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                )
                            }

                            // Tag Badge (Custom Tag Shape, authentic to the equipped Tag design)
                            LevelTagBadge(
                                tag = activeTitle.ifEmpty { "Novice" },
                                size = TagBadgeSize.SMALL
                            )

                            // Animated Emblem with Running Text (Marquee) - Novice tidak memiliki emblem
                            if (activeEmblem.isNotBlank() && !activeEmblem.equals("Novice", ignoreCase = true)) {
                                LevelEmblem(
                                    emblemName = activeEmblem,
                                    size = EmblemSize.SMALL
                                )
                            }
                        }
                    }

                    // User Avatar on Right (clickable to profile)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { navController.navigate("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        com.LambdaProject.MathArt.utils.UserAvatarWithFrame(
                            avatarName = userAvatar,
                            frameId = activeFrame,
                            avatarSize = 52.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 3. Bottom Row: Inspiring Call-To-Action Subtitle ---
                Text(
                    text = if (hasActiveSession) {
                        "Ayo lanjutkan belajar ${targetMaterial?.title ?: "Matematika"} hari ini!"
                    } else {
                        "Yuk, mulai petualangan matematikamu dan jelajahi materi baru hari ini!"
                    },
                    fontFamily = interFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569),
                    lineHeight = 18.5.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // --- 4. Mini EXP Progress Bar (Interactive -> Tap to open Level Road Modal) ---
                val (expInCurrentLvl, expNeeded) = com.LambdaProject.MathArt.data.model.getExpProgressInCurrentLevel(exp)
                val progressRatio = if (expNeeded > 0) (expInCurrentLvl.toFloat() / expNeeded.toFloat()).coerceIn(0f, 1f) else 1f

                Surface(
                    onClick = { showLevelRoadModal = true },
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
//                                    text = if (level >= 15) "Level 15" else "Menuju Level ${level + 1}",
                                    text = if (level >= 15) "Lvl. 15" else "Lvl. $level",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    fontFamily = interFontFamily
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = if (level >= 15) "Max" else "$expInCurrentLvl / $expNeeded Exp",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                fontFamily = interFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // Progress Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF3B82F6),
                                                Color(0xFF60A5FA),
                                                Color(0xFF38BDF8)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Representasi 4 tema waktu langit di Dashboard
 */
enum class DashboardSkyTheme {
    PAGI,   // 04:00 - 10:59: Sunrise (Pendaran oranye fajar / peach + langit pagi cerah)
    SIANG,  // 11:00 - 14:59: Siang (Langit biru cerah + matahari bersinar)
    SORE,   // 15:00 - 17:59: Sunset (Lembayung senja oranye keemasan + mawar senja)
    MALAM   // 18:00 - 03:59: Malam (Kabut indigo lembut + bulan sabit amber & bintang)
}

/**
 * Siluet Langit Artistik yang beradaptasi dengan tema 4 waktu
 */
@Composable
private fun DashboardSkySilhouette(
    skyTheme: DashboardSkyTheme,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val celestialCenterX = width * 0.76f
        val celestialCenterY = height * 0.28f

        when (skyTheme) {
            DashboardSkyTheme.PAGI -> {
                // === MODE PAGI: Sunrise Fajar - Sinar Matahari Radial Blur Halus (Tanpa Garis Lingkar) ===
                val sunGlowRadius = 38.dp.toPx()

                // Pendaran radial blur matahari fajar halus keemasan-oranye
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFF97316).copy(alpha = 0.35f), // Inti oranye fajar hangat
                            0.35f to Color(0xFFFB923C).copy(alpha = 0.22f), // Pendaran oranye lembut
                            0.65f to Color(0xFFFED7AA).copy(alpha = 0.10f), // Sebaran peach pudar
                            1.0f to Color.Transparent                       // Blur mulus menyatu ke langit
                        ),
                        center = androidx.compose.ui.geometry.Offset(celestialCenterX, celestialCenterY),
                        radius = sunGlowRadius
                    ),
                    center = androidx.compose.ui.geometry.Offset(celestialCenterX, celestialCenterY),
                    radius = sunGlowRadius
                )

                // Sunrise Sparkles (Oranye fajar, emas, dan biru muda)
                drawSkySparkles(
                    width = width,
                    height = height,
                    primaryColor = Color(0xFFEA580C).copy(alpha = 0.22f),
                    secondaryColor = Color(0xFFF59E0B).copy(alpha = 0.20f)
                )
            }

            DashboardSkyTheme.SIANG -> {
                // === MODE SIANG: Matahari Bersinar - Sinar Matahari Radial Blur Halus (Tanpa Garis Lingkar) ===
                val sunGlowRadius = 38.dp.toPx()

                // Pendaran radial blur matahari siang keemasan cerah
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFF59E0B).copy(alpha = 0.32f), // Inti surya keemasan
                            0.35f to Color(0xFFFDE047).copy(alpha = 0.22f), // Cahaya kuning hangat
                            0.65f to Color(0xFF38BDF8).copy(alpha = 0.08f), // Sebaran biru muda
                            1.0f to Color.Transparent                       // Blur mulus menyatu ke langit
                        ),
                        center = androidx.compose.ui.geometry.Offset(celestialCenterX, celestialCenterY),
                        radius = sunGlowRadius
                    ),
                    center = androidx.compose.ui.geometry.Offset(celestialCenterX, celestialCenterY),
                    radius = sunGlowRadius
                )

                // Daylight Sparkles (Biru langit & emas)
                drawSkySparkles(
                    width = width,
                    height = height,
                    primaryColor = Color(0xFF0284C7).copy(alpha = 0.18f),
                    secondaryColor = Color(0xFFF59E0B).copy(alpha = 0.18f)
                )
            }

            DashboardSkyTheme.SORE -> {
                // === MODE SORE: Sunset Senja - Sinar Matahari Radial Blur Halus (Tanpa Garis Lingkar) ===
                val sunGlowRadius = 40.dp.toPx()

                // Pendaran radial blur matahari senja oranye merona & mawar senja
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFEA580C).copy(alpha = 0.38f), // Inti oranye senja menyala
                            0.35f to Color(0xFFF97316).copy(alpha = 0.24f), // Pendaran senja keemasan
                            0.65f to Color(0xFFF43F5E).copy(alpha = 0.12f), // Sebaran mawar senja
                            1.0f to Color.Transparent                       // Blur mulus menyatu ke langit
                        ),
                        center = androidx.compose.ui.geometry.Offset(celestialCenterX, celestialCenterY),
                        radius = sunGlowRadius
                    ),
                    center = androidx.compose.ui.geometry.Offset(celestialCenterX, celestialCenterY),
                    radius = sunGlowRadius
                )

                // Sunset Sparkles (Oranye senja & magenta)
                drawSkySparkles(
                    width = width,
                    height = height,
                    primaryColor = Color(0xFFEA580C).copy(alpha = 0.22f),
                    secondaryColor = Color(0xFFC026D3).copy(alpha = 0.18f)
                )
            }

            DashboardSkyTheme.MALAM -> {
                // === MODE MALAM: Bulan Sabit Amber & Bintang Berpendar ===
                val moonRadius = 24.dp.toPx()

                // Ambient radial glow around moon
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFEF08A).copy(alpha = 0.22f),
                            Color(0xFFA855F7).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(celestialCenterX, celestialCenterY),
                        radius = moonRadius * 2.6f
                    ),
                    center = androidx.compose.ui.geometry.Offset(celestialCenterX, celestialCenterY),
                    radius = moonRadius * 2.6f
                )

                // Crescent Moon Silhouette Path
                val moonPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(celestialCenterX, celestialCenterY - moonRadius)
                    cubicTo(
                        celestialCenterX + moonRadius * 1.1f, celestialCenterY - moonRadius * 0.6f,
                        celestialCenterX + moonRadius * 1.1f, celestialCenterY + moonRadius * 0.6f,
                        celestialCenterX, celestialCenterY + moonRadius
                    )
                    cubicTo(
                        celestialCenterX + moonRadius * 0.45f, celestialCenterY + moonRadius * 0.5f,
                        celestialCenterX + moonRadius * 0.45f, celestialCenterY - moonRadius * 0.5f,
                        celestialCenterX, celestialCenterY - moonRadius
                    )
                    close()
                }

                drawPath(
                    path = moonPath,
                    color = Color(0xFFF59E0B).copy(alpha = 0.16f)
                )

                // Night Stars & Sparkles (Indigo & Amber)
                drawSkySparkles(
                    width = width,
                    height = height,
                    primaryColor = Color(0xFF6366F1).copy(alpha = 0.20f),
                    secondaryColor = Color(0xFFD97706).copy(alpha = 0.18f)
                )
            }
        }
    }
}

/**
 * Helper untuk menggambar gugusan kilau bintang / percikan cahaya di langit
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSkySparkles(
    width: Float,
    height: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val sparklePositions = listOf(
        Triple(0.08f, 0.18f, 3.0f),
        Triple(0.22f, 0.10f, 2.4f),
        Triple(0.42f, 0.24f, 3.6f),
        Triple(0.60f, 0.10f, 2.5f),
        Triple(0.90f, 0.38f, 2.8f),
        Triple(0.18f, 0.50f, 2.2f),
        Triple(0.36f, 0.56f, 3.0f),
        Triple(0.65f, 0.42f, 2.0f),
        Triple(0.92f, 0.55f, 2.4f),
        Triple(0.05f, 0.38f, 1.8f),
        Triple(0.52f, 0.44f, 2.0f)
    )

    sparklePositions.forEachIndexed { i, (relX, relY, radiusDp) ->
        val cx = width * relX
        val cy = height * relY
        val radiusPx = radiusDp.dp.toPx()

        if (i % 2 == 0) {
            val flarePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx, cy - radiusPx * 1.8f)
                quadraticTo(cx, cy, cx + radiusPx * 1.8f, cy)
                quadraticTo(cx, cy, cx + radiusPx * 1.8f, cy)
                quadraticTo(cx, cy, cx - radiusPx * 1.8f, cy)
                quadraticTo(cx, cy, cx - radiusPx * 1.8f, cy)
                close()
            }
            drawPath(
                path = flarePath,
                color = primaryColor
            )
        } else {
            drawCircle(
                color = secondaryColor,
                radius = radiusPx,
                center = androidx.compose.ui.geometry.Offset(cx, cy)
            )
        }
    }
}
