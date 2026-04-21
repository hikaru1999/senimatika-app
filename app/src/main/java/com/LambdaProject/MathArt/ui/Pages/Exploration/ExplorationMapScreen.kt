package com.LambdaProject.MathArt.ui.Pages.Exploration

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.ViewModels.BossQuizViewModel
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.ExplorationPhase
import com.LambdaProject.MathArt.data.ObjectType
import com.LambdaProject.MathArt.data.TileType
import com.LambdaProject.MathArt.data.model.TileData
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle.BossQuizModal
import kotlinx.coroutines.delay
import kotlin.math.abs

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ExplorationMapScreen(
    mapId: String = "level_1",
    initialBag: String = "",
    viewModel: MapViewModel,
    bossViewModel: BossQuizViewModel = viewModel(),
    onBack: () -> Unit
) {
    var tutorialStep by remember { mutableIntStateOf(0) }
    var bossTutorialTriggered by remember { mutableStateOf(false) }
    var chestTutorialTriggered by remember { mutableStateOf(false) }
    var stationTutorialTriggered by remember { mutableStateOf(false) }
    var finishTutorialTriggered by remember { mutableStateOf(false) }
    
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showExtractionAnim by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // ANIMASI FADE IN SAAT MASUK
    var mapVisible by remember { mutableStateOf(false) }
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (mapVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
        label = "fadeIn"
    )

    // Inisialisasi Map dan Bag
    LaunchedEffect(mapId) {
        if (viewModel.currentMapId != mapId || viewModel.fullMapData.isEmpty()) {
            viewModel.loadMap(mapId, context)
        }
        // Tunggu sebentar lalu munculkan map
        delay(100)
        mapVisible = true
    }

    LaunchedEffect(initialBag) {
        if (initialBag.isNotEmpty()) {
            val items = initialBag.split(",").mapNotNull {
                try { PowerUpType.valueOf(it) } catch (e: Exception) { null }
            }
            viewModel.setInitialInventory(items)
        }
    }

    // Monitor Finish State untuk Animasi
    LaunchedEffect(viewModel.isExplorationFinished, viewModel.explorationSummary) {
        if (viewModel.isExplorationFinished && viewModel.explorationSummary?.isSuccess == true) {
            showExtractionAnim = true
            delay(3500)
            showExtractionAnim = false
        }
    }

    // Trigger Proximity Tutorials
    LaunchedEffect(viewModel.playerX, viewModel.playerY) {
        if (mapId.contains("tutorial")) {
            val adjacentOffsets = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)

            for ((dx, dy) in adjacentOffsets) {
                val nx = viewModel.playerX + dx
                val ny = viewModel.playerY + dy

                if (nx in 0 until viewModel.mapWidth && ny in 0 until viewModel.mapHeight) {
                    val obj = viewModel.fullMapData[ny][nx].obj

                    if (obj == ObjectType.STATION && !stationTutorialTriggered) {
                        tutorialStep = 14
                        viewModel.shouldShowTutorialOverlay = true
                        stationTutorialTriggered = true
                    }

                    if (obj == ObjectType.FLAG && !finishTutorialTriggered) {
                        tutorialStep = 15
                        viewModel.shouldShowTutorialOverlay = true
                        finishTutorialTriggered = true
                    }
                }
            }
        }
    }

    // 1. First Chest Reward Tutorial
    LaunchedEffect(viewModel.openedChests.size) {
        if (mapId.contains("tutorial") && viewModel.openedChests.size == 1 && !chestTutorialTriggered) {
            tutorialStep = 13
            viewModel.shouldShowTutorialOverlay = true
            chestTutorialTriggered = true
        }
    }

    // Trigger Boss Tutorial Proximity
    LaunchedEffect(viewModel.isNearBoss) {
        if (mapId.contains("tutorial") && viewModel.isNearBoss && !bossTutorialTriggered) {
            tutorialStep = 5 // Boss Sequence starts at index 5
            viewModel.shouldShowTutorialOverlay = true
            bossTutorialTriggered = true
        }
    }

    // Sync Boss Trigger
    LaunchedEffect(Unit) {
        viewModel.onBossTriggered = { x, y, quizId ->
            bossViewModel.startQuiz(quizId) {
                viewModel.onBossDefeated(context)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = fadeInAlpha) // Mengaplikasikan Fade In
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val tileSizePx = with(LocalDensity.current) { 64.dp.roundToPx()}

            var tilesX = (constraints.maxWidth / tileSizePx).coerceAtLeast(3)
            var tilesY = (constraints.maxHeight / tileSizePx).coerceAtLeast(5)

            if (tilesX % 2 == 0) tilesX -= 1
            if (tilesY % 2 == 0) tilesY -= 1

            val tileSize = minOf(
                maxWidth / tilesX,
                maxHeight / tilesY,
            )

            val horizontalRange = (tilesX / 2) + 2
            val verticalRange = (tilesY / 2) + 2

            // Viewport Culling
            val startX = (viewModel.playerX - horizontalRange)
            val endX = (viewModel.playerX + horizontalRange)
            val startY = (viewModel.playerY - verticalRange)
            val endY = (viewModel.playerY + verticalRange)

            val centerOffsetX = maxWidth / 2 - tileSize / 2
            val centerOffsetY = maxHeight / 2 - tileSize / 2

            val animatedOffsetX by animateDpAsState(
                targetValue = centerOffsetX - (viewModel.playerX * tileSize),
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                label = "mapOffsetX"
            )

            val animatedOffsetY by animateDpAsState(
                targetValue = centerOffsetY - (viewModel.playerY * tileSize),
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                label = "mapOffsetY"
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().clipToBounds()
                ) {
                    Box(
                        modifier = Modifier.offset(animatedOffsetX, animatedOffsetY)
                    ) {
                        // Varian pohon untuk menambal area kosong
                        val treeVariants = listOf("obj_tree_small", "obj_tree_medium", "obj_tree_large")

                        for (y in startY..endY) {
                            for (x in startX..endX) {
                                key(x, y) {
                                    val isInsideMap = x in 0 until viewModel.mapWidth && y in 0 until viewModel.mapHeight
                                    
                                    val tile = if (isInsideMap) {
                                        val existingTile = viewModel.fullMapData.getOrNull(y)?.getOrNull(x)
                                        // Jika tile di dalam map ternyata tidak memiliki data ground (kosong)
                                        if (existingTile == null || existingTile.groundVariant.isEmpty()) {
                                            val treeIndex = abs(x * 31 + y * 17) % treeVariants.size
                                            TileData(TileType.GROUND, "tile_ground_1", ObjectType.TREE, treeVariants[treeIndex])
                                        } else {
                                            existingTile
                                        }
                                    } else {
                                        // Tambal area di luar batas map dengan ground dan pohon random
                                        val treeIndex = abs(x * 31 + y * 17) % treeVariants.size
                                        TileData(TileType.GROUND, "tile_ground_1", ObjectType.TREE_MEDIUM, treeVariants[treeIndex])
                                    }

                                    Box(
                                        modifier = Modifier
                                            .offset(x = tileSize * x, y = tileSize * y)
                                            .size(tileSize)
                                    ) {
                                        TileView(tile = tile)
                                    }
                                }
                            }
                        }
                    }

                    Player(
                        tileSize = tileSize,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // HUD
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(painterResource(R.drawable.ic_coin), null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(viewModel.inventory.coins.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = { viewModel.toggleInventory() },
                    modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(painterResource(R.drawable.ic_backpack), "Bag", tint = Color.White, modifier = Modifier.size(24.dp))
                }

                if (mapId.contains("tutorial")) {
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { tutorialStep = 0; viewModel.shouldShowTutorialOverlay = true },
                        modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.HelpOutline, "Tutorial Info", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            IconButton(
                onClick = { showExitConfirmation = true }, 
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }

            if (viewModel.showStationInteractButton && !viewModel.isStationOpen && !viewModel.isStationQuizOpen) {
                Box(modifier = Modifier.align(Alignment.Center).offset(y = 80.dp)) {
                    Button(
                        onClick = { viewModel.triggerStationInteraction() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.TouchApp, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Station", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            MovementController(modifier = Modifier.align(Alignment.Center), onMove = { dx, dy -> viewModel.move(dx, dy, context) })
        }

        // Modals
        if (viewModel.isInventoryOpen) {
            BagModal(inventory = viewModel.inventory, onClose = { viewModel.toggleInventory() }, onUsePowerUp = { viewModel.usePowerUp(it) })
        }

        if (viewModel.isChestOpen && viewModel.currentReward != null) {
            ChestPopup(reward = viewModel.currentReward!!, onCollect = { viewModel.collectReward() })
        }

        if (viewModel.isStationOpen) {
            StationModal(sessionCode = viewModel.sessionCode, collectedDigits = viewModel.collectedDigits, onSolve = { viewModel.onStationSolved() }, onClose = { viewModel.isStationOpen = false })
        }

        if (viewModel.isStationQuizOpen && viewModel.stationQuestion != null) {
            StationQuizModal(question = viewModel.stationQuestion!!, onAnswer = { viewModel.onStationQuizAnswer(it) }, onClose = { viewModel.closeStationQuiz() })
        }

        if (bossViewModel.isQuizOpen) {
            BossQuizModal(
                viewModel = bossViewModel,
                inventory = viewModel.inventory,
                onUpdateInventory = { viewModel.inventory = it },
                onFinish = { isWin ->
                    if (isWin) {
                        viewModel.onBossDefeated(context) // Mark as defeated in map
                    } else {
                        viewModel.failExploration() // Triggers Game Over modal
                    }
                }
            )
        }

        if (viewModel.phase == ExplorationPhase.Finished && viewModel.explorationSummary != null) {
            ExplorationSummaryModal(
                stats = viewModel.explorationSummary!!,
                onClose = {
                    viewModel.clearSession(context)
                    onBack()
                }
            )
        }

        if (viewModel.phase == ExplorationPhase.Extracting) {
            ExtractionIntroAnimation()
        }

        if (viewModel.shouldShowTutorialOverlay) {
            TutorialOverlay(
                step = tutorialStep,
                onNext = {
                    if (tutorialStep < 4) {
                        tutorialStep++
                    } else if (tutorialStep == 4) {
                        viewModel.shouldShowTutorialOverlay = false
                    } else if (tutorialStep < 12) {
                        tutorialStep++
                    } else if (tutorialStep == 12) {
                        viewModel.shouldShowTutorialOverlay = false
                    } else {
                        viewModel.shouldShowTutorialOverlay = false
                    }
                },
                onSkip = { viewModel.shouldShowTutorialOverlay = false },
                onOpenBag = { viewModel.toggleInventory() }
            )
        }

        if (showExitConfirmation) {
            AlertDialog(
                onDismissRequest = { showExitConfirmation = false },
                icon = { Icon(Icons.Default.Save, null, tint = Color(0xFF1976D2)) },
                title = {
                    Text(
                        "Simpan Progress?",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Kamu akan kembali ke Lobby. Progress eksplorasi saat ini akan disimpan dan dapat dilanjutkan nanti.",
                        fontFamily = interFontFamily,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitConfirmation = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Simpan & Keluar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmation = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}
