package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.data.model.GameMapInfo

// =========================================================================
// [DEBUG] Flag pengujian untuk mereset/menghapus sesi eksplorasi aktif.
// Ubah menjadi false sebelum deploy/rilis.
// =========================================================================
private const val ENABLE_DEBUG_RESET_SESSION = true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationLandingScreen(
    navController: NavController,
    viewModel: MapViewModel = viewModel()
) {
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var availableMaps by remember { mutableStateOf<List<GameMapInfo>>(emptyList()) }
    var isTutorialCompleted by remember { mutableStateOf(false) }
    var isGuideExpanded by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val loadData = {
        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
                isTutorialCompleted = snapshot.getBoolean("tutorial_completed") ?: false
            }

            db.collection("game_maps")
                .whereEqualTo("isActive", true)
                .addSnapshotListener { result, error ->
                    if (error != null) {
                        isRefreshing = false
                        return@addSnapshotListener
                    }

                    val maps = result?.documents?.map { doc ->
                        GameMapInfo(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unnamed Map",
                            thumbnailUrl = doc.getString("thumbnailUrl")
                        )
                    } ?: emptyList()

                    availableMaps = maps.sortedWith(compareBy { map ->
                        when {
                            map.id == "tutorial_eksplorasi" -> 0
                            map.name.contains("Level", ignoreCase = true) -> {
                                val levelNumber = map.name
                                    .substringAfter("Level ")
                                    .filter { it.isDigit() }
                                    .toIntOrNull() ?: 99
                                100 + levelNumber
                            }
                            else -> 999
                        }
                    })
                    isRefreshing = false
                }
        }
    }

    val globalActiveMapId by viewModel.globalActiveMapId.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        loadData()
    }

    LaunchedEffect(Unit) {
        viewModel.checkGlobalSessionStatus()
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                loadData()
                viewModel.checkGlobalSessionStatus()
            },
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Mobile Hero Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 18.dp)
                    ) {
                        // Top Nav Row: Back Button & Guide Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { navController.popBackStack() },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Kembali",
                                        tint = Color(0xFF1E293B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Quick Guide Toggle Action Button
                            //Surface(
                                // onClick = { isGuideExpanded = !isGuideExpanded },
                                // shape = RoundedCornerShape(12.dp),
                                // color = if (isGuideExpanded) Color(0xFFECFDF5) else Color(0xFFF1F5F9),
                                // border = BorderStroke(1.dp, if (isGuideExpanded) Color(0xFFA7F3D0) else Color(0xFFE2E8F0)),
                                // modifier = Modifier.size(40.dp)
                            // ) {
                                // Box(
                                    // modifier = Modifier.fillMaxSize(),
                                    // contentAlignment = Alignment.Center
                                // ) {
                                    // Icon(
                                        // imageVector = if (isGuideExpanded) Icons.Default.MenuBook else Icons.AutoMirrored.Filled.HelpOutline,
                                        // contentDescription = "Panduan Eksplorasi",
                                        // tint = if (isGuideExpanded) Color(0xFF059669) else Color(0xFF64748B),
                                        // modifier = Modifier.size(20.dp)
                                    // )
                                // }
                            //}
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Title, Subtitle, & Adventure Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
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
                                            append("Math")
                                        }
                                        withStyle(
                                            SpanStyle(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFF059669), // Emerald Green
                                                        Color(0xFF0D9488), // Teal
                                                        Color(0xFFF59E0B)  // Golden Amber
                                                    )
                                                ),
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-0.5).sp
                                            )
                                        ) {
                                            append("venture")
                                        }
                                    },
                                    fontFamily = interFontFamily,
                                    fontSize = 28.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Jelajahi wilayah Numeria, buka wilayah terkunci, dan taklukkan Boss matematika!",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontFamily = interFontFamily,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Adventure Theme Icon/Badge
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.2.dp, Color(0xFFA7F3D0)),
                                shadowElevation = 2.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_explorer),
                                        contentDescription = "Adventure Icon",
                                        modifier = Modifier.size(32.dp),
                                        tint = Color(0xFF059669)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Scrollable Content
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dedicated Guide Card Section
                    item {
                        ExplorationGuideCard(
                            isExpanded = isGuideExpanded,
                            onToggle = { isGuideExpanded = !isGuideExpanded },
                            globalActiveMapId = globalActiveMapId,
                            onResetSession = {
                                viewModel.forceClearActiveSession()
                                viewModel.checkGlobalSessionStatus()
                                loadData()
                            }
                        )
                    }

                val isInitialLoading = availableMaps.isEmpty()

                // 3. Section Title for Available Maps
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Wilayah Tersedia",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily,
                            color = Color(0xFF1E293B)
                        )

                        if (isRefreshing || isInitialLoading) {
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(24.dp)
                                    .explorationLandingShimmer(RoundedCornerShape(12.dp))
                            )
                        } else if (availableMaps.isNotEmpty()) {
                            Surface(
                                color = Color(0xFFEEF2FF),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${availableMaps.size} Wilayah Tersedia",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF4F46E5),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Skeleton Loading or Empty State or Map Cards
                if (isRefreshing || isInitialLoading) {
                    items(3) {
                        MapThumbnailCardSkeleton()
                    }
                } else if (availableMaps.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExploreOff,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "Belum Ada Wilayah Tersedia",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF334155),
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Tarik ke bawah untuk memuat ulang peta petualangan.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = availableMaps,
                        key = { it.id }
                    ) { map ->
                        val isTutorialLock = map.name.contains("Level", ignoreCase = true) && !isTutorialCompleted
                        val isLockedByOtherSession = globalActiveMapId != null && globalActiveMapId != map.id
                        val isActuallyLocked = isTutorialLock || isLockedByOtherSession

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = tween(durationMillis = 400),
                                    placementSpec = spring(
                                        stiffness = Spring.StiffnessLow,
                                        dampingRatio = Spring.DampingRatioLowBouncy
                                    ),
                                    fadeOutSpec = tween(durationMillis = 200)
                                )
                        ) {
                            MapThumbnailCard(
                                map = map,
                                isLocked = isActuallyLocked,
                                lockReason = when {
                                    isTutorialLock -> "Selesaikan Tutorial Terlebih Dahulu"
                                    isLockedByOtherSession -> "Selesaikan Eksplorasi di Wilayah Lain"
                                    else -> ""
                                },
                                isActiveSession = globalActiveMapId == map.id,
                                onClick = {
                                    if (!isActuallyLocked) {
                                        navController.navigate("ExplorationLobby/${map.id}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

/**
 * Modern expandable exploration guide card that explains core game mechanics.
 */
@Composable
private fun ExplorationGuideCard(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    globalActiveMapId: String? = null,
    onResetSession: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isExpanded) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF4F46E5).copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                if (isExpanded) Color(0xFFC7D2FE) else Color(0xFFE2E8F0),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header row with toggle action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEF2FF),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Panduan Eksplorasi",
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (isExpanded) "Ketuk untuk menutup panduan" else "Pelajari cara bermain & tips petualang",
                            fontSize = 11.5.sp,
                            fontFamily = interFontFamily,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isExpanded) Color(0xFFEEF2FF) else Color(0xFFF1F5F9),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Tutup" else "Buka",
                            tint = if (isExpanded) Color(0xFF4F46E5) else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 5 Step Guide
                    GuideStepItem(
                        number = "1",
                        title = "Siapkan Tas",
                        description = "Sebelum masuk, pilih item/power up pendukung seperti Healing Vial, Lantern, atau Battle Shield ke dalam tasmu.",
                        badgeColor = Color(0xFF3B82F6),
                        icon = Icons.Default.Backpack
                    )

                    GuideStepItem(
                        number = "2",
                        title = "Jelajahi Area",
                        description = "Jelajahi area menggunakan karaktermu. Hati-hati! Efek kabut atau malam hari mempengaruhi jarak pandang.",
                        badgeColor = Color(0xFF8B5CF6),
                        icon = Icons.Default.Explore
                    )

                    GuideStepItem(
                        number = "3",
                        title = "Selesaikan Misi",
                        description = "Terdapat misi khusus untuk beberapa wilayah. Selesaikan untuk mendapatkan bonus EXP dan koin.",
                        badgeColor = Color(0xFFF59E0B),
                        icon = Icons.Default.Psychology
                    )

                    GuideStepItem(
                        number = "4",
                        title = "Capai Bendera Finish",
                        description = "Tiba di titik bendera akhir sebelum HP habis untuk menyelesaikan wilayah & membawa hadiah! Gagal mencapai finish mengakibatkan item-itemmu tertinggal!",
                        badgeColor = Color(0xFF10B981),
                        icon = Icons.Default.Flag
                    )

                    GuideStepItem(
                        number = "5",
                        title = "Selesaikan Petualanganmu!",
                        description = "Kamu tidak dapat menjelajahi beberapa wilayah dalam satu waktu. Selesaikan eksplorasi sebelumnya untuk memulai sesi baru.",
                        badgeColor = Color(0xFFEC4899),
                        icon = Icons.Default.LockReset
                    )

                    if (ENABLE_DEBUG_RESET_SESSION) {
                        var showResetDialog by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (globalActiveMapId != null) Color(0xFFDC2626) else Color(0xFF475569),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Reset Sesi Eksplorasi",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (globalActiveMapId != null) "Hapus Sesi Aktif" else "Hapus Sesi",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (showResetDialog) {
                            AlertDialog(
                                onDismissRequest = { showResetDialog = false },
                                title = {
                                    Text(
                                        text = "Reset Sesi Eksplorasi?",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFontFamily
                                    )
                                },
                                text = {
                                    Text(
                                        text = if (globalActiveMapId != null) {
                                            "Apakah kamu yakin ingin menghapus sesi eksplorasi yang sedang berlangsung di '$globalActiveMapId'? Semua status sesi aktif akan dibersihkan dari database."
                                        } else {
                                            "Tidak ada sesi eksplorasi aktif saat ini. Tetap jalankan pembersihan sesi cloud?"
                                        },
                                        fontFamily = interFontFamily,
                                        fontSize = 13.sp
                                    )
                                },

                                confirmButton = {
                                    Button(
                                        onClick = {
                                            onResetSession()
                                            showResetDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                    ) {
                                        Text("Ya, Reset Sesi", fontWeight = FontWeight.Bold)
                                    }
                                },

                                dismissButton = {
                                    TextButton(onClick = { showResetDialog = false }) {
                                        Text("Batal")
                                    }
                                },
                                containerColor = Color.White,
                                shape = RoundedCornerShape(28.dp)
                            )
                        }
                    }

                    // Pro-tip Callout Banner
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("💡", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "Tips Petualang",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    text = "Selesaikan sesi wilayah aktif untuk membuka wilayah baru dan membuka akses belanja di Exchange Center!",
                                    fontSize = 11.sp,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF166534),
                                    lineHeight = 15.sp
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
private fun GuideStepItem(
    number: String,
    title: String,
    description: String,
    badgeColor: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = badgeColor.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$number. $title",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.5.sp,
                fontFamily = interFontFamily,
                color = Color(0xFF64748B),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun Modifier.explorationLandingShimmer(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFF1E293B),
    highlightColor: Color = Color(0xFF334155)
): Modifier {
    val transition = rememberInfiniteTransition(label = "explorationLandingShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(baseColor, highlightColor, baseColor),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 300f, translateAnim + 300f)
            )
        )
}

@Composable
fun MapThumbnailCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(188.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0xFF1E3A8A).copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .explorationLandingShimmer(
                    shape = RoundedCornerShape(22.dp),
                    baseColor = Color(0xFF0F172A),
                    highlightColor = Color(0xFF1E293B)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tag Skeleton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(24.dp)
                            .background(Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    )
                }

                // Bottom Info Skeleton
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(20.dp)
                            .background(Color(0xFF334155).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(14.dp)
                            .background(Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(16.dp)
                                .background(Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(36.dp)
                                .background(Color(0xFF059669).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        }
    }
}