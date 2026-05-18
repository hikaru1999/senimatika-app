package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.MapViewModel

data class GameMapInfo(
    val id: String = "",
    val name: String = "",
    val thumbnailUrl: String? = null,
    val isLocked: Boolean = false
)

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

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect

        db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
            isTutorialCompleted = snapshot.getBoolean("tutorial_completed") ?: false
        }

        db.collection("game_maps")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { result ->
                val maps = result.documents.map { doc ->
                    GameMapInfo(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed Map",
                        thumbnailUrl = doc.getString("thumbnailUrl")
                    )
                }

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
            }
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
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

            Spacer(modifier = Modifier.height(24.dp))

            availableMaps.forEach { map ->
                /* val isLevelMap = map.id.startsWith("eksplorasi_level_") /* || map.id == "level_1" */
                val isLocked = isLevelMap && !isTutorialCompleted */

                val isTutorialLock = map.name.contains("Level", ignoreCase = true) && !isTutorialCompleted

                /* val isTutorialLock = map.id.startsWith("eksplorasi_level_") && !isTutorialCompleted */

                val isLockedByOtherSession = viewModel.globalActiveMapId != null && viewModel.globalActiveMapId != map.id
                val isActuallyLocked = isTutorialLock || isLockedByOtherSession


                MapThumbnailCard(
                    map = map,
                    isLocked = isActuallyLocked,
                    lockReason = when {
                        isTutorialLock -> "Selesaikan Tutorial Terlebih Dahulu"
                        isLockedByOtherSession -> "Selesaikan Eksplorasi di Wilayah Lain"
                        else -> ""
                    },
                    isActiveSession = viewModel.globalActiveMapId == map.id,
                    onClick = { 
                        if (!isActuallyLocked) {
                            navController.navigate("ExplorationLobby/${map.id}")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MapThumbnailCard(
    map: GameMapInfo,
    isLocked: Boolean,
    lockReason: String,
    isActiveSession: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(enabled = !isLocked) { onClick() }
            .shadow(
                elevation = if (isLocked) 0.dp else 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF1A237E)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_explore),
                contentDescription = map.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = if (isLocked) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.2f) }) else null
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    )
            )

            // Dipindah ke sini agar bisa menggunakan .align(Alignment.TopEnd)
            /* if (isActiveSession && !isLocked) {
                Surface(
                    color = Color(0xFFFBC02D),
                    shape = RoundedCornerShape(bottomStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd) // SEKARANG BEKERJA
                ) {
                    Text(
                        text = "LANJUTKAN",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily
                    )
                }
            } */

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = map.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    fontFamily = interFontFamily,
                    color = if (isLocked) Color.Gray else Color.White,
                    letterSpacing = 1.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Info else Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isLocked) Color.Gray else Color(0xFFFFD600)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLocked) lockReason else "Ketuk untuk eksplorasi wilayah",
                        fontSize = 12.sp,
                        color = if (isLocked) Color.Gray else Color.White.copy(alpha = 0.8f),
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (isLocked) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        Text(lockReason, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                    }
                }
            }

            if (!isLocked) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 16.dp)
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
