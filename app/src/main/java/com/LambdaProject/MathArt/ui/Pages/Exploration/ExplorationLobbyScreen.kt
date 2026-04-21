package com.LambdaProject.MathArt.ui.Pages.Exploration

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationLobbyScreen(
    mapId: String,
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val tabLabels = listOf("Deskripsi", "Inventory")
    val tabIcons = listOf(Icons.Default.Info, Icons.Default.Inventory)
    val pagerState = rememberPagerState(pageCount = { tabLabels.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var inventory by remember { mutableStateOf(Inventory()) }
    var isPrepareModalOpen by remember { mutableStateOf(false) }

    // State for Map Info
    var mapName by remember { mutableStateOf("Memuat...") }
    var mapDescription by remember { mutableStateOf("Memuat deskripsi wilayah...") }
    var levelType by remember { mutableStateOf("NORMAL") }

    // Gunakan fungsi isSessionActive dari ViewModel
    /* val isSessionActive = mapViewModel.isSessionActive(mapId) */

    val isSessionActive by mapViewModel.isSessionActiveFlow(mapId).collectAsStateWithLifecycle(initialValue = false)

    // SINKRONISASI STATUS SESI DAN DATA MAP
    LaunchedEffect(mapId) {
        mapViewModel.syncSessionStatus(mapId) // Trigger sinkronisasi cloud
        db.collection("game_maps").document(mapId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                mapName = doc.getString("name") ?: "Wilayah Tanpa Nama"
                mapDescription = doc.getString("description") ?: "Tidak ada deskripsi tersedia."
                levelType = doc.getString("levelType") ?: "NORMAL"
            }
        }
    }

    /* LaunchedEffect(Unit) {
        mapViewModel.syncSessionStatus(mapId)

        // Fetch data map tetap menggunakan mapId sebagai key
        db.collection("game_maps").document(mapId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                mapName = doc.getString("name") ?: "Wilayah Tanpa Nama"
                mapDescription = doc.getString("description") ?: "Tidak ada deskripsi tersedia."
                levelType = doc.getString("levelType") ?: "NORMAL"
            }
        }
    } */

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

                    // Update inventory lokal lobby
                    inventory = Inventory(
                        coins = coins,
                        scrolls = detailedScrolls,
                        powerUps = powerUps
                    )
                }
            }
        }

        onDispose {
            // PENTING: Matikan listener saat pindah screen agar tidak terjadi double sync
            listener.remove()
        }
    }

    /* LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect
        db.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val coins = snapshot.getLong("coins")?.toInt() ?: 0
                val scrollIds = snapshot.get("scrolls") as? List<String> ?: emptyList()
                val powerUpsStrings = snapshot.get("powerUps") as? List<String> ?: emptyList()
                val powerUps = powerUpsStrings.mapNotNull {
                    try { PowerUpType.valueOf(it) } catch (e: Exception) { null }
                }
                coroutineScope.launch {
                    val detailedScrolls = if (scrollIds.isNotEmpty()) {
                        try {
                            // Ambil detail dari koleksi learning_contents berdasarkan ID
                            val scrollSnapshots = db.collection("learning_contents")
                                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), scrollIds)
                                .get()
                                .await() // Membutuhkan import kotlinx.coroutines.tasks.await

                            scrollSnapshots.documents.mapNotNull { doc ->
                                com.LambdaProject.MathArt.data.Reward(
                                    type = com.LambdaProject.MathArt.data.RewardType.SCROLL,
                                    id = doc.id,
                                    title = doc.getString("title") ?: "Materi Baru",
                                    content = doc.getString("content") ?: ""
                                )
                            }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }

                    // Sekarang tipe data sudah sesuai: List<Reward>
                    inventory = Inventory(
                        coins = coins,
                        scrolls = detailedScrolls,
                        powerUps = powerUps
                    )
                }
            }
        }
    } */

    Scaffold(
        containerColor = Color(0xFFE7E8EF),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Persiapan Eksplorasi",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        color = Color(0xFF1A237E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = Color(0xFF1976D2)
                    )
                }
            ) {
                tabLabels.forEachIndexed { index, label ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(tabIcons[index], null, modifier = Modifier.size(18.dp), tint = Color(0xFF1A237E))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontFamily = interFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
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
                        levelType = levelType,
                        onBack = { navController.popBackStack() },
                        onStartExploration = { 
                            if (isSessionActive) {
                                if (mapViewModel.fullMapData.isEmpty() || mapViewModel.currentMapId != mapId) {
                                    mapViewModel.loadMap(mapId, context)
                                }
                                navController.navigate("ExplorationLoading/$mapId?bagItems=")
                            } else {
                                isPrepareModalOpen = true 
                            }
                        },
                        isResume = isSessionActive
                    )
                    1 -> InventoryTabContent(inventory, isMallDisabled = isSessionActive)
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

                                // Ambil daftar power up saat ini di gudang
                                val currentPUs = snapshot.get("powerUps") as? List<String> ?: emptyList()
                                val updatedWarehousePUs = currentPUs.toMutableList()

                                // Hapus satu per satu item yang dijadikan bekal
                                bagItems.forEach { bekal ->
                                    updatedWarehousePUs.remove(bekal.name)
                                }

                                // Update dokumen user (Gudang sekarang sudah berkurang)
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
}

suspend fun fetchScrollDetails(db: FirebaseFirestore, scrollIds: List<String>): List<com.LambdaProject.MathArt.data.Reward> {
    if (scrollIds.isEmpty()) return emptyList()

    return try {
        // Menggunakan whereIn untuk mengambil maksimal 30 dokumen dalam 1 permintaan (hemat kuota READ)
        val scrollSnapshots = db.collection("learning_contents")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), scrollIds)
            .get()
            .await()

        scrollSnapshots.documents.mapNotNull { doc ->
            com.LambdaProject.MathArt.data.Reward(
                type = com.LambdaProject.MathArt.data.RewardType.SCROLL,
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
