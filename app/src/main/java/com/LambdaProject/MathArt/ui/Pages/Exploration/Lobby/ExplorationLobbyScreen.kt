package com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.ViewModels.AchievementViewModel
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.Reward
import com.LambdaProject.MathArt.data.RewardType
import com.LambdaProject.MathArt.ui.Pages.Exploration.ArtifactInfoModal
import com.LambdaProject.MathArt.ui.Pages.NotFoundScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.LambdaProject.MathArt.data.model.Mission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationLobbyScreen(
    mapId: String,
    navController: NavController,
    mapViewModel: MapViewModel,
    achViewModel: AchievementViewModel = viewModel()
) {
    val tabLabels = listOf("Deskripsi", "Inventory")
    val tabIcons = listOf(Icons.Default.Info, Icons.Default.Inventory)
    val pagerState = rememberPagerState(pageCount = { tabLabels.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val isSessionActive by mapViewModel.isSessionActiveFlow(mapId).collectAsStateWithLifecycle(initialValue = false)

    var inventory by remember { mutableStateOf(Inventory()) }
    var isPrepareModalOpen by remember { mutableStateOf(false) }
    var mapName by remember { mutableStateOf("Memuat...") }
    var mapDescription by remember { mutableStateOf("Memuat deskripsi wilayah...") }
    var levelType by remember { mutableStateOf("NORMAL") }
    var isLoading by remember { mutableStateOf(false)}
    var hasFog by remember { mutableStateOf(false) }
    var hasNight by remember { mutableStateOf(false) }
    var hasCombined by remember { mutableStateOf(false) }
    var mapMissions by remember { mutableStateOf<List<Mission>>(emptyList()) }
    var selectedArtifactForModal by remember { mutableStateOf<MapViewModel.ArtifactData?>(null) }
    var checkComplete by remember { mutableStateOf(false) }
    var mapPlayerHp by remember { mutableFloatStateOf(100f) }

    var isVerifying by remember { mutableStateOf(true) }
    var isNotFound by remember { mutableStateOf(false) }

    val actuallyNotFound = isNotFound || (mapName == "Memuat..." && checkComplete)

    LaunchedEffect(actuallyNotFound) {
        if (actuallyNotFound) {
            navController.currentBackStackEntry?.savedStateHandle?.set("is_not_found", true)
        }
    }

    LaunchedEffect(Unit) {
        delay(4000)
        checkComplete = true
    }

    LaunchedEffect(mapId) {
        isVerifying = true
        mapViewModel.syncSessionStatus(mapId)

        db.collection("game_maps").document(mapId).get(Source.SERVER)
            .addOnSuccessListener { doc ->
            if (doc.exists() && doc.getBoolean("isActive") == true) {
                mapName = doc.getString("name") ?: "Wilayah Tanpa Nama"
                mapPlayerHp = doc.getDouble("playerHp")?.toFloat() ?: 100f
                mapDescription = doc.getString("description") ?: "Tidak ada deskripsi tersedia."
                levelType = doc.getString("levelType") ?: "NORMAL"

                val visibility = doc.get("visibility") as? Map<String, Any>
                hasFog = visibility?.get("fogOfWar") as? Boolean ?: false
                hasNight = visibility?.get("nightMode") as? Boolean ?: false
                hasCombined = visibility?.get("fogNightCombined") as? Boolean ?: false

                val rawMissions = doc.get("missions") as? List<Map<String, Any>> ?: emptyList()
                mapMissions = rawMissions.map { Mission.fromMap(it) }

                isVerifying = false
            } else {
                isNotFound = true
                isVerifying = false
            }
        }.addOnFailureListener {
                isNotFound = true
                isVerifying = false
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            mapViewModel.loadUserUnlockedArtifactIds()
        }
    }

    DisposableEffect(userId) {
        if (userId == null) return@DisposableEffect onDispose {}

        val listener = db.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val coins = snapshot.getLong("coins")?.toInt() ?: 0
                val scrollIds = snapshot.get("scrolls") as? List<String> ?: emptyList()
                val powerUpsStrings = snapshot.get("powerUps") as? List<String> ?: emptyList()

                val powerUps = powerUpsStrings.mapNotNull {
                    try { PowerUpType.valueOf(it) } catch (e: Exception) { null }
                }

                coroutineScope.launch {
                    val detailedScrolls = if (scrollIds.isNotEmpty()) {
                        fetchScrollDetails(db, scrollIds)
                    } else emptyList()

                    inventory = Inventory(
                        coins = coins,
                        scrolls = detailedScrolls,
                        powerUps = powerUps
                    )
                }
            }
        }

        onDispose {
            listener.remove()
        }
    }

    if (isVerifying) {
        ExplorationLobbySkeleton()
    } else if (isNotFound || (mapName == "Memuat..." && checkComplete)) {
        NotFoundScreen(
            title = "Yah, Peta Tidak Tersedia",
            message = "Maaf ya, peta eksplorasi ini sedang tidak tersedia.",
            onBack = {
                navController.currentBackStackEntry?.savedStateHandle?.set("is_not_found", false)
                if (isSessionActive) {
                    mapViewModel.forceClearActiveSession()
                }
                navController.popBackStack()
            }
        )
    } else {
        Scaffold(
            containerColor = Color(0xFFF8FAFC),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Persiapan Eksplorasi",
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color(0xFF0F172A)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.White,
                    divider = {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color(0xFFE2E8F0)
                        )
                    },
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            height = 3.dp,
                            color = Color(0xFF1976D2)
                        )
                    }
                ) {
                    tabLabels.forEachIndexed { index, label ->
                        val isSelected = pagerState.currentPage == index
                        Tab(
                            selected = isSelected,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        tabIcons[index],
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (isSelected) Color(0xFF1976D2) else Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        label,
                                        fontFamily = interFontFamily,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color(0xFF1976D2) else Color(0xFF64748B)
                                    )
                                }
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = false
                ) { page ->
                    when (page) {
                        0 -> GameDescriptionTab(
                            name = mapName,
                            description = mapDescription,
                            hasFog = hasFog,
                            hasNight = hasNight,
                            hasCombined = hasCombined,
                            levelType = levelType,
                            playerHp = mapPlayerHp,
                            missions = mapMissions,
                            onBack = { navController.popBackStack() },
                            onStartExploration = {
                                coroutineScope.launch {
                                    try {
                                        if (isSessionActive) {
                                            if (mapViewModel.fullMapData.isEmpty() || mapViewModel.currentMapId != mapId) {
                                                mapViewModel.loadMap(mapId, context)
                                            }
                                            navController.navigate("ExplorationLoading/$mapId?bagItems=")
                                        } else {
                                            isPrepareModalOpen = true
                                            isLoading = false
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        Log.e("ExplorationLobby", "Error: ${e.message}")
                                    }
                                }
                            },
                            isResume = isSessionActive
                        )
                        1 -> InventoryTabContent(
                            inventory = inventory,
                            unlockedArtifacts = mapViewModel.unlockedArtifactDetails,
                            onArtifactClick = { artifact ->
                                selectedArtifactForModal = artifact
                            },
                            isMallDisabled = isSessionActive)
                    }
                }
            }

            if (isPrepareModalOpen && !isSessionActive) {
                PrepareExplorationModal(
                    permanentPowerUps = inventory.powerUps,
                    onCancel = { isPrepareModalOpen = false },
                    onConfirm = { bagItems ->
                        coroutineScope.launch {
                            try {
                                db.runTransaction { transaction ->
                                    val userDocRef = db.collection("users").document(userId!!)
                                    val snapshot = transaction.get(userDocRef)

                                    val currentPUs =
                                        snapshot.get("powerUps") as? List<String> ?: emptyList()
                                    val updatedWarehousePUs = currentPUs.toMutableList()

                                    bagItems.forEach { bekal ->
                                        updatedWarehousePUs.remove(bekal.name)
                                    }

                                    transaction.update(userDocRef, "powerUps", updatedWarehousePUs)
                                }.await()

                                isPrepareModalOpen = false
                                val bagString = bagItems.joinToString(",") { it.name }
                                navController.navigate("ExplorationLoading/$mapId?bagItems=$bagString")
                            } catch (e: Exception) {
                                Log.e("Lobby", "Gagal memproses bekal: ${e.message}")
                            }
                        }
                    }
                )
            }
        }
        if (selectedArtifactForModal != null) {
            ArtifactInfoModal(
                artifact = selectedArtifactForModal!!,
                onClose = { selectedArtifactForModal = null }
            )
        }
    }
}

suspend fun fetchScrollDetails(db: FirebaseFirestore, scrollIds: List<String>): List<Reward> {
    if (scrollIds.isEmpty()) return emptyList()

    return try {
        val scrollSnapshots = db.collection("learning_contents")
            .whereIn(FieldPath.documentId(), scrollIds)
            .get()
            .await()

        scrollSnapshots.documents.mapNotNull { doc ->
            Reward(
                type = RewardType.SCROLL,
                id = doc.id,
                title = doc.getString("title") ?: "Materi Baru",
                content = doc.getString("content") ?: ""
            )
        }
    } catch (e: Exception) {
        Log.e("Lobby", "Gagal mengambil detail scroll: ${e.message}")
        emptyList()
    }
}

@Composable
private fun Modifier.explorationLobbyShimmer(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF1F5F9)
): Modifier {
    val transition = rememberInfiniteTransition(label = "explorationLobbyShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "explorationLobbyShimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(baseColor, highlightColor, baseColor),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 350f, translateAnim + 350f)
            )
        )
}

/**
 * Loading Skeleton untuk ExplorationLobbyScreen
 */
@Composable
fun ExplorationLobbySkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top App Bar Shimmer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .explorationLobbyShimmer(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(20.dp)
                        .explorationLobbyShimmer(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.size(36.dp))
            }
        }

        // Tab Row Shimmer (Deskripsi / Inventory)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .explorationLobbyShimmer(RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .explorationLobbyShimmer(RoundedCornerShape(12.dp))
                )
            }
        }

        // Body Content Shimmer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Map Hero Card Shimmer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .explorationLobbyShimmer(RoundedCornerShape(14.dp))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(22.dp)
                                .explorationLobbyShimmer(RoundedCornerShape(6.dp))
                        )
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(24.dp)
                                .explorationLobbyShimmer(RoundedCornerShape(8.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(14.dp)
                            .explorationLobbyShimmer(RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(14.dp)
                            .explorationLobbyShimmer(RoundedCornerShape(4.dp))
                    )
                }
            }

            // Player HP & Status Card Shimmer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(16.dp)
                                .explorationLobbyShimmer(RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(16.dp)
                                .explorationLobbyShimmer(RoundedCornerShape(4.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .explorationLobbyShimmer(RoundedCornerShape(5.dp))
                    )
                }
            }

            // Missions Header & List Shimmer
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(20.dp)
                    .explorationLobbyShimmer(RoundedCornerShape(4.dp))
            )

            repeat(2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .explorationLobbyShimmer(CircleShape)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(14.dp)
                                    .explorationLobbyShimmer(RoundedCornerShape(4.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(12.dp)
                                    .explorationLobbyShimmer(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }

            // Start Exploration Button Shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .explorationLobbyShimmer(RoundedCornerShape(16.dp))
            )
        }
    }
}
