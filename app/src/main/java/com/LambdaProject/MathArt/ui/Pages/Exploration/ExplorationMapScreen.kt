package com.LambdaProject.MathArt.ui.Pages.Exploration

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.ViewModels.BossQuizViewModel
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.AchievementViewModel
import com.LambdaProject.MathArt.ViewModels.ExplorationPhase
import com.LambdaProject.MathArt.data.NetworkObserver
import com.LambdaProject.MathArt.data.ObjectType
import com.LambdaProject.MathArt.data.TileType
import com.LambdaProject.MathArt.data.model.ExplorationAudioManager
import com.LambdaProject.MathArt.data.model.TileData
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle.BossQuizModal
import com.LambdaProject.MathArt.ui.components.FogOfWarOverlay
import com.LambdaProject.MathArt.ui.components.NightModeOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ExplorationMapScreen(
    mapId: String = "level_1",
    initialBag: String = "",
    viewModel: MapViewModel,
    achViewModel: AchievementViewModel,
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
    var mapVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var isInitialLoad by remember { mutableStateOf(true) }

    var buttonTicker by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val isStationCooldown = buttonTicker < viewModel.stationCooldownEnd
    var isProcessingResults by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val audio = remember { ExplorationAudioManager(context) }
    val scope = rememberCoroutineScope()
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (mapVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
        label = "fadeIn"
    )

    val networkObserver = remember { NetworkObserver(context) }
    val isOnline by networkObserver.isConnected.collectAsState(initial = true)

    val isNight = viewModel.isNightModeEnabled || viewModel.isCombinedEnabled
    val buttonBgColor = if (isNight) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.6f)
    val iconTintColor = if (isNight) Color.White else Color.Black

    var lastDx by remember { mutableIntStateOf(0) }
    var lastDy by remember { mutableIntStateOf(0) }

    DisposableEffect(mapId) {
        onDispose {
            audio.stopBGM()
            audio.stopIntenseWarning()
        }
    }

    LaunchedEffect(viewModel.currentMapId, viewModel.isNightModeEnabled, viewModel.isCombinedEnabled) {
        val isDark = viewModel.isNightModeEnabled || viewModel.isCombinedEnabled

        val bgmResource = if (isDark) {
            R.raw.bgm_slow_ambient
        } else {
            R.raw.bgm_master
        }

        if (isInitialLoad) {
            delay(500)
            isInitialLoad = false
        }

        audio.stopBGM()
        delay(100)
        audio.playBGM(bgmResource, isAmbient = true)

        mapVisible = true
    }

    LaunchedEffect(viewModel.currentBossPos) {
        if (viewModel.currentBossPos != null) {
            audio.stopBGMWithFade(duration = 500)
        }
    }

    LaunchedEffect(initialBag) {
        if (initialBag.isNotEmpty()) {
            val items = initialBag.split(",").mapNotNull {
                try { PowerUpType.valueOf(it) } catch (e: Exception) { null }
            }
            viewModel.setInitialInventory(items)
        }
    }

    LaunchedEffect(viewModel.isExplorationFinished, viewModel.explorationSummary) {
        if (viewModel.isExplorationFinished &&
            viewModel.explorationSummary?.isSuccess == true &&
            !isProcessingResults ) {

            isProcessingResults = true
            audio.stopBGM()
            showExtractionAnim = true

            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUserId = auth.currentUser?.uid

            if (currentUserId != null) {
                scope.launch {
                    try {
                        viewModel.processExplorationResults(
                            userId = currentUserId,
                            currentMapId = mapId,
                            itemsUsed = viewModel.inventory.powerUps,
                            achievementViewModel = achViewModel
                        )
                        delay(4000)
                    } finally {
                        showExtractionAnim = false
                        isProcessingResults = false
                    }
                }
            }

            /* if (currentUserId != null) {
                viewModel.processExplorationResults(
                    userId = currentUserId,
                    currentMapId = mapId,
                    itemsUsed = viewModel.inventory.powerUps,
                    achievementViewModel = achViewModel
                )
            }

            delay(4000)
            showExtractionAnim = false
            isProcessingResults = false */
        }
    }

    LaunchedEffect(viewModel.playerX, viewModel.playerY) {
        viewModel.updateDiscovery(viewModel.playerX, viewModel.playerY)

        val currentTile = viewModel.fullMapData
            .getOrNull(viewModel.playerY)
            ?.getOrNull(viewModel.playerX)

        currentTile?.let {
            println("DEBUG ARTEFAK: Pos(${viewModel.playerX},${viewModel.playerY}) " +
                    "| Obj: ${it.obj} " +
                    "| Variant: ${it.objectVariant} " +
                    "| ContentID: ${it.contentId}")
        }

        if (currentTile != null &&
            currentTile.obj == ObjectType.ARTIFACT &&
            currentTile.objectVariant.startsWith("obj_landmark_")
        ) {
            val contentId = currentTile.contentId
            val variant = currentTile.objectVariant

            if (!contentId.isNullOrEmpty()) {
                println("DEBUG: Memicu triggerArtifact untuk ID: $contentId")
                viewModel.triggerArtifact(contentId, variant)
            } else {
                println("DEBUG ERROR: Artefak terdeteksi tapi ContentID KOSONG!")
            }
        }

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

    LaunchedEffect(viewModel.openedChests.size) {
        if (mapId.contains("tutorial") && viewModel.openedChests.size == 1 && !chestTutorialTriggered) {
            tutorialStep = 13
            viewModel.shouldShowTutorialOverlay = true
            chestTutorialTriggered = true
        }
    }

    LaunchedEffect(viewModel.isNearBoss) {
        if (mapId.contains("tutorial") && viewModel.isNearBoss && !bossTutorialTriggered) {
            tutorialStep = 5 // Boss Sequence starts at index 5
            viewModel.shouldShowTutorialOverlay = true
            bossTutorialTriggered = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onBossTriggered = { x, y, quizId ->
            val bossTile = viewModel.fullMapData.getOrNull(y)?.getOrNull(x)
            val bossType = bossTile?.objectVariant ?: "obj_boss_1"

            bossViewModel.onFastStreakTrigger = {
                viewModel.addLearningPityBonus(context)
            }

            bossViewModel.startQuiz(quizId, bossType, startHp = viewModel.playerHp) {
                viewModel.onBossDefeated(context)
            }
        }
    }

    LaunchedEffect(viewModel.stationCooldownEnd) {
        while (System.currentTimeMillis() < viewModel.stationCooldownEnd) {
            delay(1000)
            buttonTicker = System.currentTimeMillis()
        }
    }

    if (!isOnline) {
        Surface(
            modifier = Modifier.fillMaxWidth().zIndex(100f),
            color = Color.Red.copy(alpha = 0.8f)
        ) {
            Text(
                "Koneksi Terputus. Progress terkini mungkin tidak tersimpan.",
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(2.dp)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = fadeInAlpha)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                ) {
                    Box(
                        modifier = Modifier.offset(animatedOffsetX, animatedOffsetY)
                    ) {
                        val treeVariants = listOf("obj_tree_small", "obj_tree_medium", "obj_tree_large")

                        for (y in startY..endY) {
                            for (x in startX..endX) {
                                key(x, y) {
                                    val isInsideMap = x in 0 until viewModel.mapWidth && y in 0 until viewModel.mapHeight
                                    
                                    val tile = if (isInsideMap) {
                                        val existingTile = viewModel.fullMapData.getOrNull(y)?.getOrNull(x)

                                        if (existingTile == null || existingTile.groundVariant.isEmpty()) {
                                            val treeIndex = abs(x * 31 + y * 17) % treeVariants.size
                                            TileData(TileType.GROUND, "tile_ground_1", ObjectType.TREE, treeVariants[treeIndex])
                                        } else {
                                            existingTile
                                        }
                                    } else {
                                        val treeIndex = abs(x * 31 + y * 17) % treeVariants.size
                                        TileData(TileType.GROUND, "tile_ground_1", ObjectType.TREE_MEDIUM, treeVariants[treeIndex])
                                    }

                                    Box(
                                        modifier = Modifier
                                            .offset(x = tileSize * x, y = tileSize * y)
                                            .size(tileSize)
                                    ) {
                                        TileView(tile = tile)

                                        if (tile.obj == ObjectType.ARTIFACT &&
                                            !tile.contentId.isNullOrEmpty() &&
                                            viewModel.unlockedArtifactIds.contains(tile.contentId)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.TopEnd
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Collected",
                                                    tint = Color(0xFF4CAF50), // Warna hijau sukses
                                                    modifier = Modifier
                                                        .size(tileSize / 3) // Ukuran proporsional dengan tile (sekitar 1/3)
                                                        .background(Color.White, CircleShape)
                                                        .border(1.dp, Color.White, CircleShape)
                                                        .zIndex(5f) // Pastikan paling depan
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (viewModel.isFogOfWarEnabled || viewModel.isCombinedEnabled) {
                        /* FogOfWarOverlay(
                            mapId = mapId,
                            playerX = viewModel.playerX,
                            playerY = viewModel.playerY,
                            discoveredTiles = viewModel.discoveredTiles,
                            isBinocularActive = viewModel.isBinocularActive,
                            isLanternActive = viewModel.isLanternActive,
                            isTorchActive = viewModel.isTorchActive,
                            isCombined = viewModel.isCombinedEnabled,
                            tileSize = tileSize,
                            offsetX = animatedOffsetX,
                            offsetY = animatedOffsetY,
                            maxWidth = this@BoxWithConstraints.maxWidth,
                            maxHeight = this@BoxWithConstraints.maxHeight
                        ) */

                        FogOfWarOverlay(
                            mapId = mapId,
                            playerX = viewModel.playerX,
                            playerY = viewModel.playerY,
                            discoveredTiles = viewModel.discoveredTiles,
                            isBinocularActive = viewModel.isBinocularActive,
                            isLanternActive = viewModel.isLanternActive,
                            isTorchActive = viewModel.isTorchActive,
                            isCombined = viewModel.isCombinedEnabled,
                            tileSize = tileSize,
                            offsetX = animatedOffsetX,
                            offsetY = animatedOffsetY,
                            maxWidth = this@BoxWithConstraints.maxWidth,
                            maxHeight = this@BoxWithConstraints.maxHeight
                        )
                    }

                    if (viewModel.isNightModeEnabled || viewModel.isCombinedEnabled) {
                        NightModeOverlay(
                            mapId = mapId,
                            playerX = viewModel.playerX,
                            playerY = viewModel.playerY,
                            tileSize = tileSize,
                            offsetX = animatedOffsetX,
                            offsetY = animatedOffsetY,
                            isTorchActive = viewModel.isTorchActive,
                            isLanternActive = viewModel.isLanternActive,
                            isCombined = viewModel.isCombinedEnabled
                        )
                    }

                    Player(
                        tileSize = tileSize,
                        dx = lastDx,
                        dy = lastDy,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            AnimatedVisibility(
                visible = viewModel.isBinocularActive,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            ) {
                Surface(
                    color = Color(0xFF1A237E).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Color(0xFFFFD600)),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pu_binocular),
                            contentDescription = null,
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Binokular Aktif!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = interFontFamily
                            )
                            Text(
                                text = "Area pandang diperluas (${viewModel.binocularTimeLeft}s)",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontFamily = interFontFamily
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = viewModel.isTorchActive,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            ) {
                Surface(
                    color = Color(0xFFE65100).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Color(0xFFFFCC80)),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pu_torch),
                            contentDescription = null,
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Obor Menyala! (${viewModel.torchTimeLeft}s)", color = Color.White)
                    }
                }
            }

            // HUD
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF424242), Color(0xFF212121))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_coin),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        val animatedCoins by animateIntAsState(
                            targetValue = viewModel.inventory.coins,
                            label = "coinAnimation"
                        )

                        Text(
                            text = animatedCoins.toString(),
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            style = TextStyle(
                                shadow = Shadow(color = Color.Black, blurRadius = 4f)
                            )
                        )
                    }
                }

                HUDGameButton(
                    icon = R.drawable.ic_backpack,
                    onClick = { viewModel.toggleInventory() },
                    borderColor = Color(0xFFBDBDBD)
                )

                if (mapId.contains("tutorial")) {
                    HUDGameButton(
                        vectorIcon = Icons.Default.HelpOutline,
                        onClick = { tutorialStep = 0; viewModel.shouldShowTutorialOverlay = true },
                        borderColor = Color(0xFF4CAF50)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(45.dp) // Ukuran lingkaran
                    .background(buttonBgColor, CircleShape)
                    .border(1.dp, iconTintColor.copy(alpha = 0.3f), CircleShape)
                    .clickable { showExitConfirmation = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (viewModel.showStationInteractButton && !viewModel.isStationOpen && !viewModel.isStationQuizOpen) {
                val hasMagicKey = viewModel.inventory.powerUps.contains(PowerUpType.MAGIC_KEY)

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 120.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(
                            onClick = { viewModel.triggerStationInteraction() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.TouchApp, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka Area", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (hasMagicKey) {
                            Button(
                                onClick = {
                                    viewModel.usePowerUp(PowerUpType.MAGIC_KEY, context, audio)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700),
                                    contentColor = Color(0xFF3E2723)
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(Icons.Default.VpnKey, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gunakan Magic Key", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            MovementController(
                modifier = Modifier.align(Alignment.Center),
                onMove = { dx, dy ->
                    lastDx = dx
                    lastDy = dy
                    viewModel.move(dx, dy, context, audio)
                }
            )
        }

        if (viewModel.isInventoryOpen) {
            BagModal(
                viewModel = viewModel,
                inventory = viewModel.inventory,
                playerHp = viewModel.playerHp,
                isLanternActive = viewModel.isLanternActive,
                isLeatherStrapsActive = viewModel.isLeatherStrapsActive,
                maxBagWeight = viewModel.maxBagWeight,
                isDropping = viewModel.isDropping,
                onClose = { viewModel.toggleInventory() },
                onDropPowerUp = { pu -> viewModel.dropPowerUp(pu) },
                onUsePowerUp = { viewModel.usePowerUp(it, context, audio) }
            )
        }

        if (viewModel.isChestOpen && viewModel.currentReward != null) {
            ChestPopup(
                reward = viewModel.currentReward!!,
                isReplay = viewModel.isTutorialReplayByStatus,
                audio = audio,
                onCollect = { viewModel.collectReward() })
        }

        if (viewModel.isStationOpen) {
            StationModal(sessionCode = viewModel.sessionCode, collectedDigits = viewModel.collectedDigits, onSolve = { viewModel.onStationSolved() }, onClose = { viewModel.isStationOpen = false })
        }

        if (viewModel.isStationQuizOpen && viewModel.stationQuestion != null) {
            val currentTime = System.currentTimeMillis()
            val remaining = (viewModel.stationCooldownEnd - currentTime).coerceAtLeast(0L)

            StationQuizModal(
                question = viewModel.stationQuestion!!,
                cooldownEndMillis = viewModel.stationCooldownEnd,
                onAnswer = { viewModel.onStationQuizAnswer(it) },
                onClose = { viewModel.closeStationQuiz() })
        }

        if (viewModel.isDroppedSackOpen && viewModel.sackInventoryPreview != null) {
            DroppedSackModal(
                sackInventory = viewModel.sackInventoryPreview!!,
                playerInventory = viewModel.inventory,
                maxBagWeight = viewModel.maxBagWeight,
                isAfterDeadSack = false,
                onClose = { viewModel.isDroppedSackOpen = false },
                onPickItem = { pu ->
                    audio.playSfx("click")
                    viewModel.pickItemFromSack(pu, context)
                },
                onPickCoins = {

                },
            )
        }

        if (viewModel.isDeathSackOpen && viewModel.deathSackInventory != null) {
            DroppedSackModal(
                sackInventory = viewModel.deathSackInventory!!,
                playerInventory = viewModel.inventory,
                isAfterDeadSack = true,
                onClose = { viewModel.isDeathSackOpen = false },
                onPickItem = { pu ->
                    audio.playSfx("click")
                    viewModel.pickItemFromDeathSack(pu, context)
                },
                onPickCoins = {
                    viewModel.pickCoinsFromDeathSack(context)
                }
            )
        }

        if (bossViewModel.isQuizOpen) {
            BossQuizModal(
                viewModel = bossViewModel,
                viewModelMAP = viewModel,
                inventory = viewModel.inventory,
                onUpdateInventory = { viewModel.inventory = it },
                onFinish = { isWin ->
                    viewModel.playerHp = bossViewModel.playerHp
                    if (isWin) {
                        viewModel.onBossDefeated(context)
                    } else {
                        viewModel.failExploration()
                    }
                },
                audio = audio
            )
        }

        if (viewModel.phase == ExplorationPhase.Finished && viewModel.explorationSummary != null) {
            ExplorationSummaryModal(
                stats = viewModel.explorationSummary!!,
                isTutorialReplay = viewModel.isTutorialReplayByStatus,
                onClose = {
                    audio.forceStopAll()
                    viewModel.clearSession(context)
                    onBack()
                },
                audio = audio
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
                containerColor = Color(0xFF212121),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.border(2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp)), // Border Emas
                icon = {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        tint = Color(0xFFFFD700), // Icon warna emas
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        "Simpan Sesi?",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        "Kamu akan kembali ke Lobby. Progress eksplorasi saat ini akan disimpan dan dapat dilanjutkan nanti.",
                        fontFamily = interFontFamily,
                        color = Color(0xFFE0E0E0),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            audio.stopBGM()
                            isLoading = true
                            /* ShowExitConfirmation = false */
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000)
                                onBack()
                                isLoading = false
                            }
                            /* showExitConfirmation = false */
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF81C784)),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Text("SIMPAN & KELUAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitConfirmation = false },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            "BATAL",
                            color = Color.LightGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            )
        }
        Text(
            text = "Preview Mode. Work in Progress",
            style = TextStyle(
                fontFamily = interFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isNight) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
                .zIndex(1000f)
        )
    }
    if (viewModel.isArtifactModalVisible) {
        viewModel.selectedArtifact?.let { artifact ->
            ArtifactInfoModal(
                artifact = artifact,
                onClose = { viewModel.closeArtifactModal() }
            )
        }
    }
}
