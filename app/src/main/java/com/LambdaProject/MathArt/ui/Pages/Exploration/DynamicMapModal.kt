package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.data.ObjectType
import com.LambdaProject.MathArt.data.model.TileData
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.utils.toDrawableResId
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun DynamicMapModal(
    viewModel: MapViewModel,
    onClose: () -> Unit
) {
    val fullMap = viewModel.fullMapData
    val width = viewModel.mapWidth
    val height = viewModel.mapHeight
    val playerX = viewModel.playerX
    val playerY = viewModel.playerY
    val discoveredTiles = viewModel.discoveredTiles
    val openedChests = viewModel.openedChests
    val defeatedBosses = viewModel.defeatedBosses
    val solvedStations = viewModel.solvedStations

    val isNightMode = viewModel.isNightModeEnabled || viewModel.isCombinedEnabled
    val isFogMode = viewModel.isFogOfWarEnabled || viewModel.isCombinedEnabled
    val isDefaultMode = !isNightMode && !isFogMode

    // Calculate exploration statistics
    var totalChestsInMap = 0
    var totalBossesInMap = 0

    if (fullMap.isNotEmpty()) {
        fullMap.forEach { row ->
            row.forEach { tile ->
                if (tile.obj == ObjectType.CHEST) totalChestsInMap++
                if (tile.obj == ObjectType.BOSS) totalBossesInMap++
            }
        }
    }
    // Also count already cleared items
    totalChestsInMap += openedChests.size
    totalBossesInMap += defeatedBosses.size

    val totalTiles = max(1, width * height)
    val exploredCount = if (isDefaultMode) totalTiles else discoveredTiles.size
    val explorationPercentage = min(100, (exploredCount * 100) / totalTiles)

    // Pulse animation for player pin
    val infiniteTransition = rememberInfiniteTransition(label = "playerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.90f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF5F0E6),
                border = BorderStroke(4.dp, Color(0xFF8B4513)),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF8B4513),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Peta Wilayah",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4E342E),
                                fontFamily = interFontFamily
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF8B4513).copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF5D4037),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Exploration Progress Bar & Stats
                    Surface(
                        color = Color(0xFFEDE0D4),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFD7CCC8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Progress
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Eksplorasi",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D4C41),
                                    fontFamily = interFontFamily
                                )
                                Text(
                                    text = "$explorationPercentage%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2E7D32),
                                    fontFamily = interFontFamily
                                )
                            }

                            // Chests
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Peti Terbuka",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D4C41),
                                    fontFamily = interFontFamily
                                )
                                Text(
                                    text = "${openedChests.size} / $totalChestsInMap",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFE65100),
                                    fontFamily = interFontFamily
                                )
                            }

                            // Bosses
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Boss Dikalahkan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D4C41),
                                    fontFamily = interFontFamily
                                )
                                Text(
                                    text = "${defeatedBosses.size} / $totalBossesInMap",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB71C1C),
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Map Viewport (Scrollable Grid with Real Ground Assets, Dimmed Night Effect & Fog of War Overlay)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1B18))
                            .border(2.dp, Color(0xFF8B4513), RoundedCornerShape(16.dp))
                            .clipToBounds(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (width > 0 && height > 0 && fullMap.isNotEmpty()) {
                            InteractiveDynamicMapCanvas(
                                fullMap = fullMap,
                                width = width,
                                height = height,
                                playerX = playerX,
                                playerY = playerY,
                                discoveredTiles = discoveredTiles,
                                openedChests = openedChests,
                                defeatedBosses = defeatedBosses,
                                solvedStations = solvedStations,
                                isNightMode = isNightMode,
                                isFogMode = isFogMode,
                                isDefaultMode = isDefaultMode,
                                isLanternActive = viewModel.isLanternActive,
                                isTorchActive = viewModel.isTorchActive,
                                pulseScale = pulseScale
                            )
                        } else {
                            Text(
                                text = "Peta belum dimuat.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontFamily = interFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEDE0D4), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(
                            iconRes = R.drawable.player_forward,
                            label = "Kamu",
                            isPlayer = true
                        )
                        LegendItem(
                            iconRes = R.drawable.obj_chest,
                            badgeColor = Color(0xFF4CAF50),
                            label = "Peti Terbuka"
                        )
                        LegendItem(
                            iconRes = R.drawable.ic_swords,
                            badgeColor = Color(0xFF9E9E9E),
                            label = "Boss Kalah"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513))
                    ) {
                        Text(
                            text = "Tutup Peta",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveDynamicMapCanvas(
    fullMap: List<List<TileData>>,
    width: Int,
    height: Int,
    playerX: Int,
    playerY: Int,
    discoveredTiles: Set<Pair<Int, Int>>,
    openedChests: List<Pair<Int, Int>>,
    defeatedBosses: List<Pair<Int, Int>>,
    solvedStations: List<Pair<Int, Int>>,
    isNightMode: Boolean,
    isFogMode: Boolean,
    isDefaultMode: Boolean,
    isLanternActive: Boolean,
    isTorchActive: Boolean,
    pulseScale: Float
) {
    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()

    // Base cell size matching asset proportions
    val cellSize = 44.dp
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportWidthPx = constraints.maxWidth
        val viewportHeightPx = constraints.maxHeight
        val cellSizePx = with(density) { cellSize.roundToPx() }
        val paddingPx = with(density) { 12.dp.roundToPx() }

        val totalMapWidthDp = cellSize * width
        val totalMapHeightDp = cellSize * height

        // Position player exactly in the center of viewport when opened
        LaunchedEffect(playerX, playerY, viewportWidthPx, viewportHeightPx) {
            val playerCenterPxX = paddingPx + (playerX * cellSizePx) + (cellSizePx / 2)
            val playerCenterPxY = paddingPx + (playerY * cellSizePx) + (cellSizePx / 2)

            val targetScrollX = (playerCenterPxX - (viewportWidthPx / 2)).coerceAtLeast(0)
            val targetScrollY = (playerCenterPxY - (viewportHeightPx / 2)).coerceAtLeast(0)

            hScrollState.scrollTo(targetScrollX)
            vScrollState.scrollTo(targetScrollY)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(hScrollState)
                .verticalScroll(vScrollState)
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier.size(width = totalMapWidthDp, height = totalMapHeightDp)
            ) {
                // 1. Base Map Layer (Tiles and Objects)
                Column {
                    for (y in 0 until height) {
                        Row {
                            for (x in 0 until width) {
                                val pos = Pair(x, y)
                                val tile = fullMap.getOrNull(y)?.getOrNull(x)
                                val isOpenedChest = pos in openedChests
                                val isDefeatedBoss = pos in defeatedBosses
                                val isSolvedStation = pos in solvedStations

                                MapCell(
                                    x = x,
                                    y = y,
                                    cellSize = cellSize,
                                    tile = tile,
                                    isOpenedChest = isOpenedChest,
                                    isDefeatedBoss = isDefeatedBoss,
                                    isSolvedStation = isSolvedStation
                                )
                            }
                        }
                    }
                }

                // 2. Dimmed Night Mode Lighting Layer (If Night Mode or Night+Fog Mode)
                if (isNightMode) {
                    val playerScreenX = (playerX * cellSizePx) + (cellSizePx / 2f)
                    val playerScreenY = (playerY * cellSizePx) + (cellSizePx / 2f)
                    val spotlightMultiplier = when {
                        isLanternActive -> 2.6f
                        isTorchActive -> 2.2f
                        else -> 1.7f
                    }
                    val playerLightRadius = cellSizePx * spotlightMultiplier

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(alpha = 0.99f)
                    ) {
                        // Ambient dimmed night tint across the whole map
                        drawRect(color = Color(0xFF000000).copy(alpha = 0.58f))

                        // Soft warm lantern spotlight on the player's immediate area
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                                center = Offset(playerScreenX, playerScreenY),
                                radius = playerLightRadius
                            ),
                            radius = playerLightRadius,
                            center = Offset(playerScreenX, playerScreenY),
                            blendMode = BlendMode.DstOut
                        )
                    }
                }

                // 3. Fog of War / Exploration Darkness Overlay
                if (!isDefaultMode) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(alpha = 0.99f)
                    ) {
                        val fogColor = if (isNightMode) {
                            if (isFogMode) Color(0xFF8899A6).copy(alpha = 0.95f) else Color.Black.copy(alpha = 0.92f)
                        } else {
                            Color(0xFFDEE4EB)
                        }

                        // Base fog / dark shroud over unvisited areas
                        drawRect(color = fogColor)

                        // Punch smooth organic holes around all discovered tiles
                        discoveredTiles.forEach { (tx, ty) ->
                            val screenX = (tx * cellSizePx) + (cellSizePx / 2f)
                            val screenY = (ty * cellSizePx) + (cellSizePx / 2f)
                            val holeRadius = cellSizePx * 1.6f

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.Transparent, Color.White),
                                    center = Offset(screenX, screenY),
                                    radius = holeRadius
                                ),
                                radius = holeRadius,
                                center = Offset(screenX, screenY),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
                }

                // 4. Player Character Indicator on Top
                val playerOffsetX = (cellSize * playerX)
                val playerOffsetY = (cellSize * playerY)

                Box(
                    modifier = Modifier
                        .offset(x = playerOffsetX, y = playerOffsetY)
                        .size(cellSize)
                        .zIndex(20f),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing highlight aura
                    Box(
                        modifier = Modifier
                            .size(cellSize * 0.85f)
                            .graphicsLayer(
                                scaleX = pulseScale,
                                scaleY = pulseScale,
                                alpha = if (isNightMode) 0.75f else 0.55f
                            )
                            .background(Color(0xFFFFD700), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )

                    // Player character sprite
                    Image(
                        painter = painterResource(id = R.drawable.player_forward),
                        contentDescription = "Player",
                        modifier = Modifier.size(cellSize * 0.95f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun MapCell(
    x: Int,
    y: Int,
    cellSize: Dp,
    tile: TileData?,
    isOpenedChest: Boolean,
    isDefeatedBoss: Boolean,
    isSolvedStation: Boolean
) {
    val context = LocalContext.current
    val treeVariants = listOf("obj_tree_small", "obj_tree_medium", "obj_tree_large")
    val treeIndex = abs(x * 31 + y * 17) % treeVariants.size
    val fallbackTreeVariant = treeVariants[treeIndex]

    val isPath = tile?.groundVariant?.startsWith("tile_path") == true
    val groundVariantName = tile?.groundVariant?.ifEmpty { "tile_ground_1" } ?: "tile_ground_1"
    val groundRes = groundVariantName.toDrawableResId(context).takeIf { it != 0 } ?: R.drawable.tile_ground_1

    // Automatically fill empty grass areas with default trees exactly like ExplorationMapScreen
    val rawObjVariant = when {
        tile == null -> fallbackTreeVariant
        tile.objectVariant.startsWith("obj_tree_") && tile.objectVariant.toDrawableResId(context) == 0 -> fallbackTreeVariant
        tile.obj == ObjectType.TREE || tile.obj == ObjectType.TREE_MEDIUM || tile.obj == ObjectType.TREE_SMALL || tile.obj == ObjectType.TREE_LARGE -> {
            tile.objectVariant.ifEmpty { fallbackTreeVariant }
        }
        tile.obj == ObjectType.NONE && !isPath -> fallbackTreeVariant
        else -> tile.objectVariant
    }

    val objRes = rawObjVariant.toDrawableResId(context)

    Box(
        modifier = Modifier.size(cellSize),
        contentAlignment = Alignment.Center
    ) {
        // 1. Render Actual Ground Texture Asset (tile_ground_1, tile_path_1 .. 15)
        Image(
            painter = painterResource(id = groundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Render Objects / Markers
        if (isOpenedChest) {
            // Opened / collected chest
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.obj_chest),
                    contentDescription = "Opened Chest",
                    modifier = Modifier
                        .size(cellSize * 0.75f)
                        .graphicsLayer(alpha = 0.5f),
                    contentScale = ContentScale.Fit
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Cleared",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier
                        .size(cellSize * 0.45f)
                        .align(Alignment.BottomEnd)
                )
            }
        } else if (isDefeatedBoss) {
            // Defeated boss: Show crossed swords with checkmark
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_swords),
                    contentDescription = "Defeated Boss",
                    tint = Color.Gray.copy(alpha = 0.8f),
                    modifier = Modifier.size(cellSize * 0.7f)
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Cleared",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier
                        .size(cellSize * 0.45f)
                        .align(Alignment.BottomEnd)
                )
            }
        } else if (tile != null && (tile.obj != ObjectType.NONE || objRes != 0)) {
            // Undefeated Boss: Hide until defeated
            val shouldHideBoss = (tile.obj == ObjectType.BOSS)
            // Unopened Chest: Hide until opened
            val shouldHideChest = (tile.obj == ObjectType.CHEST)

            if (!shouldHideBoss && !shouldHideChest && objRes != 0) {
                val isSack = rawObjVariant.contains("sack")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = objRes),
                        contentDescription = null,
                        modifier = if (isSack) Modifier.fillMaxSize(0.65f) else Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    if (tile.obj == ObjectType.STATION && isSolvedStation) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Solved",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier
                                .size(cellSize * 0.45f)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    iconRes: Int = 0,
    iconColor: Color = Color.Unspecified,
    badgeColor: Color = Color.Transparent,
    label: String,
    isPlayer: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isPlayer) {
            Image(
                painter = painterResource(id = R.drawable.player_forward),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.Fit
            )
        } else if (iconRes != 0) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    contentScale = ContentScale.Fit
                )
                if (badgeColor != Color.Transparent) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(badgeColor, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E342E),
            fontFamily = interFontFamily
        )
    }
}
