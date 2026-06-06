package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.data.model.GameMapInfo

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

    var isRefreshing by remember { mutableStateOf(false) }
//    val scope = rememberCoroutineScope()

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
//            db.collection("game_maps")
//                .whereEqualTo("isActive", true)
//                .get()
//                .addOnSuccessListener { result ->
//                    val maps = result.documents.map { doc ->
//                        GameMapInfo(
//                            id = doc.id,
//                            name = doc.getString("name") ?: "Unnamed Map",
//                            thumbnailUrl = doc.getString("thumbnailUrl")
//                        )
//                    }
//
//                    availableMaps = maps.sortedWith(compareBy { map ->
//                        when {
//                            map.id == "tutorial_eksplorasi" -> 0
//                            map.name.contains("Level", ignoreCase = true) -> {
//                                val levelNumber = map.name
//                                    .substringAfter("Level ")
//                                    .filter { it.isDigit() }
//                                    .toIntOrNull() ?: 99
//                                100 + levelNumber
//                            }
//                            else -> 999
//                        }
//                    })
//                    isRefreshing = false
//                }
//                .addOnFailureListener {
//                    isRefreshing = false
//                }
//        } else {
//            isRefreshing = false
//        }
    }

    val globalActiveMapId by viewModel.globalActiveMapId.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        loadData()
    }

    LaunchedEffect(Unit) {
        viewModel.checkGlobalSessionStatus()
    }

    Scaffold(
        containerColor = Color(0xFFFFFFFF),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pilih Wilayah",
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily,
                        color = Color(0xFF1A237E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isScrolled) Color.White else Color.Transparent,
                    scrolledContainerColor = Color.White
                )
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                loadData()
                viewModel.checkGlobalSessionStatus()
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text(
                            "Wilayah Eksplorasi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily,
                            color = Color(0xFF1A237E)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Pilih wilayah untuk melihat detail dan mempersiapkan tas jelajahmu.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontFamily = interFontFamily
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (availableMaps.isEmpty() && !isRefreshing) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Belum ada wilayah tersedia", color = Color.Gray)
                            Text("Tarik ke bawah untuk memuat ulang", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                }

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
                                fadeInSpec = tween(durationMillis = 500),
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