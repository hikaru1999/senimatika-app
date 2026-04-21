package com.LambdaProject.MathArt.ViewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.*
import com.LambdaProject.MathArt.data.model.ExplorationAudioManager
import com.LambdaProject.MathArt.data.model.TileData
import com.LambdaProject.MathArt.data.model.getInteraction
import com.LambdaProject.MathArt.data.model.isWalkable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import toObjectType
import toTileType
import java.io.File
import kotlin.math.abs

data class ExplorationStats(
    val coinsCollected: Int = 0,
    val scrollsCollected: List<String> = emptyList(),
    val powerUpsCollected: List<PowerUpType> = emptyList(),
    val bossesDefeated: Int = 0,
    val isSuccess: Boolean = false
)

sealed class ExplorationPhase {
    object Playing : ExplorationPhase()
    object Extracting : ExplorationPhase()
    object Finished : ExplorationPhase()
}

class MapViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var mapWidth by mutableStateOf(0)
    var mapHeight by mutableStateOf(0)
    var fullMapData by mutableStateOf<List<List<TileData>>>(emptyList())
    var playerX by mutableStateOf(0)
    var playerY by mutableStateOf(0)

    var currentMapId by mutableStateOf("")

    // Chest Interaction
    var isChestOpen by mutableStateOf(false)
    var currentReward by mutableStateOf<Reward?>(null)
    var currentChestPos by mutableStateOf<Pair<Int, Int>?>(null)

    // Station Interaction
    var isStationOpen by mutableStateOf(false)
    var sessionCode by mutableStateOf("482") // Default code
    val collectedDigits = mutableStateListOf<Char>()

    // Station Quiz Interaction
    var isStationQuizOpen by mutableStateOf(false)
    var stationQuestion by mutableStateOf<Question?>(null)
    var stationCooldownEnd by mutableLongStateOf(0L)
    var showStationCooldownMessage by mutableStateOf(false)

    val remainingCooldownSeconds: Int
        get() = ((stationCooldownEnd - System.currentTimeMillis()) / 1000).coerceAtLeast(0).toInt()

    // Proximity Trigger
    var showStationInteractButton by mutableStateOf(false)
    var currentStationPos by mutableStateOf<Pair<Int, Int>?>(null)

    // Boss Proximity (Tutorial use)
    var isNearBoss by mutableStateOf(false)

    // Extraction / Summary
    var isExplorationFinished by mutableStateOf(false)
    var explorationSummary by mutableStateOf<ExplorationStats?>(null)
    var bossesDefeatedCount by mutableIntStateOf(0)

    var currentBossPos by mutableStateOf<Pair<Int, Int>?>(null)

    var inventory by mutableStateOf(Inventory())
    var isInventoryOpen by mutableStateOf(false)
    var isInteractionLocked by mutableStateOf(false)

    var phase by mutableStateOf<ExplorationPhase>(ExplorationPhase.Playing)
        private set

    val openedChests = mutableStateListOf<Pair<Int, Int>>()
    val defeatedBosses = mutableStateListOf<Pair<Int, Int>>()
    val solvedStations = mutableStateListOf<Pair<Int, Int>>()
    val visibleTiles = mutableStateSetOf<Pair<Int, Int>>()
    val discoveredTiles = mutableStateSetOf<Pair<Int, Int>>()

    var onBossTriggered: ((Int, Int, String) -> Unit)? = null

    // Tutorial flag
    var shouldShowTutorialOverlay by mutableStateOf(false)

    private val quizIdMap = mutableMapOf<Pair<Int, Int>, String>()

    private val _currentSessionActive = MutableStateFlow(false)
    private var loadedUserId: String? = null
    private var saveJob: Job? = null

    // Cloud Session Cache
    var hasActiveCloudSession by mutableStateOf<Boolean?>(null)
    private var isCloudChecking = false

    fun isSessionActiveFlow(mapId: String) = _currentSessionActive.asStateFlow()


    fun setInitialInventory(items: List<PowerUpType>) {
        inventory = inventory.copy(powerUps = items)
    }

    /**
     * Memperbarui status sesi aktif dari cloud.
     */
    fun syncSessionStatus(mapId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            hasActiveCloudSession = false
            _currentSessionActive.value = false
            return
        }

        if (isCloudChecking) return
        isCloudChecking = true

        FirebaseFirestore.getInstance()
            .collection("exploration_sessions")
            .document(userId)
            .get(Source.SERVER)
            .addOnSuccessListener { doc ->
                val savedMapId = doc.getString("mapId")
                val isFinished = doc.getBoolean("isFinished") ?: true
                val isActive = (savedMapId == mapId && !isFinished)

                hasActiveCloudSession = isActive
                _currentSessionActive.value = isActive

                isCloudChecking = false
            }
            .addOnFailureListener {
                hasActiveCloudSession = false
                isCloudChecking = false
            }
    }

    fun isSessionActive(mapId: String): Boolean {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Cek RAM (Jika ViewModel masih hidup dan milik user yang sama)
        val isInMemoryValid = currentMapId == mapId &&
                              fullMapData.isNotEmpty() &&
                              loadedUserId == currentUserId &&
                              !isExplorationFinished &&
                              phase == ExplorationPhase.Playing
        
        if (isInMemoryValid) return true

        // Jika tidak di RAM, gunakan status dari cloud
        return hasActiveCloudSession ?: false
    }

    /**
     * Memuat peta dengan strategi: Load Cache Instan ke RAM -> Sync Server -> Selesai.
     * isLoading akan tetap TRUE sampai sinkronisasi server selesai.
     */
    fun loadMap(mapId: String, context: Context? = null) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) return

        val wasFinished = isExplorationFinished || phase == ExplorationPhase.Finished

        // Reset volatile UI state
        isExplorationFinished = false
        explorationSummary = null
        phase = ExplorationPhase.Playing
        isInteractionLocked = false

        // Optimasi: Jika map sudah ada di RAM milik user yang sama, tidak perlu reload
        if (currentMapId == mapId && fullMapData.isNotEmpty() && loadedUserId == currentUserId && !wasFinished) {
            isLoading = false
            return
        }

        isLoading = true
        currentMapId = mapId
        loadedUserId = currentUserId

        val db = FirebaseFirestore.getInstance()

        // 1. Ambil data sesi dari cloud
        db.collection("exploration_sessions").document(currentUserId).get(Source.DEFAULT)
            .addOnSuccessListener { sessionDoc ->
                val isResuming = (sessionDoc.getString("mapId") == mapId && !(sessionDoc.getBoolean("isFinished") ?: true))

                // 2. LOAD DARI PERMANENT CACHE (INSTAN KE RAM AGAR SIAP PAKAI)
                var cachedData: Map<String, Any>? = null
                val cacheFile = context?.let { File(it.filesDir, "map_cache_$mapId.json") }
                if (cacheFile != null && cacheFile.exists()) {
                    try {
                        val jsonStr = cacheFile.readText()
                        cachedData = jsonToMap(JSONObject(jsonStr))
                        processLoadedMapData(mapId, cachedData!!, sessionDoc, isResuming)
                    } catch (e: Exception) {
                        cacheFile.delete()
                    }
                }

                // 3. FORCE SERVER SYNC (CEK PERUBAHAN DI FIRESTORE)
                // Kita tidak mematikan isLoading sampai block ini selesai
                db.collection("game_maps").document(mapId).get(Source.SERVER)
                    .addOnSuccessListener { mapDoc ->
                        val serverData = mapDoc.data
                        if (serverData != null) {
                            if (serverData != cachedData) {
                                viewModelScope.launch(Dispatchers.IO) {
                                    context?.let { ctx ->
                                        try {
                                            val file = File(ctx.filesDir, "map_cache_$mapId.json")
                                            file.writeText(JSONObject(serverData).toString())
                                        } catch (e: Exception) {}
                                    }
                                }
                                processLoadedMapData(mapId, serverData, sessionDoc, isResuming)
                            }
                        }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        isLoading = false
                    }
            }
            .addOnFailureListener { isLoading = false }
    }

    private suspend fun fetchScrollDetails(scrollIds: List<String>): List<Reward> {
        if (scrollIds.isEmpty()) return emptyList()
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshots = db.collection("learning_contents") // Pastikan nama koleksi benar
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), scrollIds)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                Reward(
                    type = RewardType.SCROLL,
                    id = doc.id,
                    title = doc.getString("title") ?: "Materi Baru",
                    content = doc.getString("content") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processLoadedMapData(
        mapId: String,
        mapData: Map<String, Any>,
        sessionDoc: com.google.firebase.firestore.DocumentSnapshot,
        isResuming: Boolean
    ) {
        val width = (mapData["width"] as? Number)?.toInt() ?: 0
        val height = (mapData["height"] as? Number)?.toInt() ?: 0
        val tiles1D = mapData["tiles"] as? List<Map<String, Any>>
        val player = mapData["playerStart"] as? Map<String, Any>

        if (width == 0 || height == 0 || tiles1D == null) return

        mapWidth = width
        mapHeight = height
        quizIdMap.clear()

        if (isResuming) {
            sessionCode = sessionDoc.getString("sessionCode") ?: "482"
            // RESTORE FROM CLOUD
            playerX = sessionDoc.getLong("playerX")?.toInt() ?: 0
            playerY = sessionDoc.getLong("playerY")?.toInt() ?: 0

            openedChests.clear()
            (sessionDoc.get("openedChests") as? List<String>)?.forEach { s ->
                val p = s.split(",")
                openedChests.add(p[0].toInt() to p[1].toInt())
            }

            defeatedBosses.clear()
            (sessionDoc.get("defeatedBosses") as? List<String>)?.forEach { s ->
                val p = s.split(",")
                defeatedBosses.add(p[0].toInt() to p[1].toInt())
            }

            solvedStations.clear()
            (sessionDoc.get("solvedStations") as? List<String>)?.forEach { s ->
                val p = s.split(",")
                solvedStations.add(p[0].toInt() to p[1].toInt())
            }

            bossesDefeatedCount = sessionDoc.getLong("bossesDefeatedCount")?.toInt() ?: defeatedBosses.size

            val invData = sessionDoc.get("inventory") as? Map<String, Any>
            val invCoins = (invData?.get("coins") as? Long)?.toInt() ?: 0
            val invScrollIds = invData?.get("scrolls") as? List<String> ?: emptyList()
            val invPUs = (invData?.get("powerUps") as? List<String>)?.mapNotNull {
                try { PowerUpType.valueOf(it) } catch(e: Exception) { null }
            } ?: emptyList()
            viewModelScope.launch {
                val detailedScrolls = fetchScrollDetails(invScrollIds)
                inventory = Inventory(invCoins, detailedScrolls, invPUs)
            }

            // FIX: Muat collectedDigits dari Firestore ke State List
            val savedDigits = sessionDoc.getString("collectedDigits") ?: ""
            collectedDigits.clear()
            savedDigits.forEach { collectedDigits.add(it) }

            shouldShowTutorialOverlay = false
        } else {
            resetRAMState()
            currentMapId = mapId
            sessionCode = (100..999).random().toString()
            playerX = (player?.get("x") as? Number)?.toInt() ?: (width / 2)
            playerY = (player?.get("y") as? Number)?.toInt() ?: (height / 2)
            
            if (mapId.contains("tutorial")) {
                shouldShowTutorialOverlay = true
            }
        }

        // --- PATCHING TILE KOSONG ---
        val treeVariants = listOf("obj_tree_1", "obj_tree_2", "obj_tree_3")

        fullMapData = List(height) { y ->
            List(width) { x ->
                val index = y * width + x
                val tileMap = tiles1D.getOrNull(index)
                val pos = Pair(x, y)
                val isCleared = pos in openedChests || pos in defeatedBosses || pos in solvedStations

                if (tileMap == null) {
                    val treeIdx = abs(x * 31 + y * 17) % treeVariants.size
                    TileData(TileType.GROUND, "tile_ground_1", if (isCleared) ObjectType.NONE else ObjectType.TREE_MEDIUM, if (isCleared) "" else treeVariants[treeIdx])
                } else {
                    val objTypeStr = tileMap["object"] as? String ?: "NONE"
                    val objType = objTypeStr.toObjectType()
                    val qId = tileMap["quizId"] as? String
                    if (qId != null) quizIdMap[pos] = qId

                    TileData(
                        ground = (tileMap["ground"] as? String)?.toTileType() ?: TileType.GROUND,
                        groundVariant = tileMap["groundVariant"] as? String ?: "tile_ground_1",
                        obj = if (isCleared) ObjectType.NONE else objType,
                        objectVariant = if (isCleared) "" else (tileMap["objectVariant"] as? String ?: "")
                    )
                }
            }
        }

        revealArea(playerX, playerY, 3)
        checkProximity(playerX, playerY)
    }

    private fun jsonToMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            if (value is JSONObject) {
                map[key] = jsonToMap(value)
            } else if (value is JSONArray) {
                val list = mutableListOf<Any>()
                for (i in 0 until value.length()) {
                    val item = value.get(i)
                    if (item is JSONObject) list.add(jsonToMap(item))
                    else list.add(item)
                }
                map[key] = list
            } else {
                map[key] = value
            }
        }
        return map
    }

    fun move(dx: Int, dy: Int, context: Context? = null, audio: ExplorationAudioManager? = null) {
        if (isLoading || isChestOpen || isInventoryOpen || isExplorationFinished || isStationOpen || isStationQuizOpen || isInteractionLocked) return

        val newX = playerX + dx
        val newY = playerY + dy

        if (newX !in 0 until mapWidth || newY !in 0 until mapHeight) return

        val targetTile = fullMapData[newY][newX]
        if(!targetTile.isWalkable()) return

        val prevX = playerX
        val prevY = playerY

        playerX = newX
        playerY = newY

        revealArea(playerX, playerY, 3)
        checkProximity(playerX, playerY)

        val interaction = targetTile.getInteraction()
        if (interaction == InteractionType.BOSS) {
            saveToCloud(customX = prevX, customY = prevY)
            handleInteraction(targetTile, newX, newY, context, audio)
        } else {
            handleInteraction(targetTile, newX, newY, context, audio)
            saveProgressDebounced()
        }
    }

    private fun checkProximity(px: Int, py: Int) {
        val adjacentOffsets = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
        var nearStation = false
        var stationPos: Pair<Int, Int>? = null
        var nearBoss = false
        for ((dx, dy) in adjacentOffsets) {
            val nx = px + dx
            val ny = py + dy
            if (nx in 0 until mapWidth && ny in 0 until mapHeight) {
                if (fullMapData[ny][nx].obj == ObjectType.STATION) {
                    nearStation = true
                    stationPos = nx to ny
                }
                if (fullMapData[ny][nx].obj == ObjectType.BOSS) {
                    nearBoss = true
                }
            }
        }
        showStationInteractButton = nearStation
        currentStationPos = stationPos
        isNearBoss = nearBoss
    }

    fun revealArea(centerX: Int, centerY: Int, radius: Int) {
        visibleTiles.clear()
        for (y in -radius..radius) {
            for (x in -radius..radius) {
                val worldX = centerX + x
                val worldY = centerY + y
                if (worldX in 0 until mapWidth && worldY in 0 until mapHeight) {
                    visibleTiles.add(worldX to worldY)
                    discoveredTiles.add(worldX to worldY)
                }
            }
        }
    }

    fun handleInteraction(tile: TileData, x: Int, y: Int, context: Context? = null, audio: ExplorationAudioManager?) {
        if (isInteractionLocked) return

        when (tile.getInteraction()) {
            InteractionType.CHEST -> {
                isInteractionLocked = true
                viewModelScope.launch {
                    delay(300)
                    openChest(x, y)
                    isInteractionLocked = false
                }
            }
            InteractionType.BOSS -> {
                isInteractionLocked = true
                viewModelScope.launch {
                    delay(300)
                    currentBossPos = x to y
                    val quizId = quizIdMap[x to y] ?: ""
                    onBossTriggered?.invoke(x, y, quizId)
                    isInteractionLocked = false
                }
            }
            InteractionType.FINISH -> {
                if (phase != ExplorationPhase.Playing) return
                isInteractionLocked = true
                audio?.stopBGM()
                viewModelScope.launch {
                    delay(500)
                    audio?.playSfx("success")
                    extractItems(context)
                }
            }
            else -> Unit
        }
    }

    fun triggerStationInteraction() {
        if (showStationInteractButton && !isInteractionLocked) {
             isStationOpen = true
        }
    }

    fun onBossDefeated(context: Context? = null) {
        bossesDefeatedCount++
        currentBossPos?.let { pos ->
            defeatedBosses.add(pos)
            removeObjectAt(pos.first, pos.second)
            inventory = inventory.copy(coins = inventory.coins + 100)
            
            if (collectedDigits.size < sessionCode.length) {
                val nextDigit = sessionCode[collectedDigits.size]
                if (!collectedDigits.contains(nextDigit)) {
                    collectedDigits.add(nextDigit)
                }
            }
        }
        checkProximity(playerX, playerY)
        saveToCloud()
    }

    private fun extractItems(context: Context? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        val db = FirebaseFirestore.getInstance()

        val finalBagCoins = inventory.coins
        val finalBagScrolls = inventory.scrolls.map { it.id }
        val finalBagPUs = inventory.powerUps.map { it.name }

        // 1. Siapkan summary untuk UI (Data apa yang berhasil dibawa pulang)
        val summary = ExplorationStats(
            coinsCollected = finalBagCoins,
            scrollsCollected = finalBagScrolls,
            powerUpsCollected = inventory.powerUps,
            bossesDefeated = bossesDefeatedCount,
            isSuccess = true
        )

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            // AMBIL DATA GUDANG SAAT INI (Sisa yang tidak dibawa tadi)
            val currentCoins = snapshot.getLong("coins") ?: 0
            val currentScrolls = snapshot.get("scrolls") as? List<String> ?: emptyList()
            val currentPowerUpsStrings = snapshot.get("powerUps") as? List<String> ?: emptyList()

            val newCoins = currentCoins + finalBagCoins
            val newScrolls = (currentScrolls + inventory.scrolls.map { it.id }).distinct()
            val newPowerUpsStrings = currentPowerUpsStrings + finalBagPUs

            transaction.update(userRef, mapOf(
                "coins" to newCoins,
                "scrolls" to newScrolls,
                "powerUps" to newPowerUpsStrings
            ))

            val sessionRef = db.collection("exploration_sessions").document(userId)
            transaction.update(sessionRef, "isFinished", true)
        }.addOnSuccessListener {
            explorationSummary = summary
            isExplorationFinished = true
            phase = ExplorationPhase.Finished

            context?.let { ctx ->
                File(ctx.filesDir, "map_cache_$currentMapId.json").delete()
            }
        }.addOnFailureListener { e ->
            Log.e("MapViewModel", "Ekstraksi Gagal: ${e.message}")
        }
    }

    /* private fun extractItems(context: Context? = null) {

        val summary = ExplorationStats(
            coinsCollected = inventory.coins,
            scrollsCollected = inventory.scrolls.map { it.id },
            powerUpsCollected = inventory.powerUps,
            bossesDefeated = bossesDefeatedCount,
            isSuccess = true
        )

        fun proceedToSummary() {
            explorationSummary = summary
            phase = ExplorationPhase.Extracting
            viewModelScope.launch {
                delay(3500)
                phase = ExplorationPhase.Finished
                isExplorationFinished = true
                isInteractionLocked = false
            }
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            proceedToSummary()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentCoins = snapshot.getLong("coins") ?: 0
            val currentScrolls = snapshot.get("scrolls") as? List<String> ?: emptyList()
            /* val currentPowerUps = (snapshot.get("powerUps") as? List<String>)?.map { PowerUpType.valueOf(it) } ?: emptyList()*/
            val currentPowerUps = (snapshot.get("powerUps") as? List<String>)?.mapNotNull {
                try { PowerUpType.valueOf(it) } catch(e: Exception) { null }
            } ?: emptyList()

            val newCoins = currentCoins + inventory.coins
            /* val newScrolls = (currentScrolls + inventory.scrolls).distinct() */
            val newScrolls = (currentScrolls + inventory.scrolls.map { it.id }).distinct()
            val newPowerUps = currentPowerUps + inventory.powerUps

            val updates = mutableMapOf<String, Any>(
                "coins" to newCoins,
                "scrolls" to newScrolls,
                "powerUps" to newPowerUps.map { it.name }
            )
            
            if (currentMapId.contains("tutorial")) {
                updates["tutorial_completed"] = true
            }
            transaction.set(userRef, updates, SetOptions.merge())
        }.addOnSuccessListener {
            proceedToSummary()
        }.addOnFailureListener {
            proceedToSummary()
        }
    } */

    fun failExploration() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()


        explorationSummary = ExplorationStats(
            coinsCollected = 0,
            scrollsCollected = emptyList(),
            powerUpsCollected = emptyList(),
            bossesDefeated = bossesDefeatedCount,
            isSuccess = false
        )

        inventory = Inventory()
        isExplorationFinished = true
        phase = ExplorationPhase.Finished

        viewModelScope.launch {
            try {
                db.collection("exploration_sessions").document(userId)
                    .update("isFinished", true)
                    .await()

                // 4. Update Flow agar Lobby langsung berubah tombolnya
                _currentSessionActive.value = false
                hasActiveCloudSession = false

                Log.d("MapVM", "Game Over: Sesi ditutup tanpa ekstraksi item.")
            } catch (e: Exception) {
                Log.e("MapVM", "Gagal menutup sesi game over: ${e.message}")
            }
        }
    }

    fun clearSession(context: Context? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        _currentSessionActive.value = false
        hasActiveCloudSession = false

        if (userId != null) {
            viewModelScope.launch {
                delay(300)
                deleteCloudSession(userId)
            }
        }
        resetRAMState()

        fullMapData = emptyList()
        currentMapId = ""
        mapWidth = 0
        mapHeight = 0
        isExplorationFinished = false
        isInteractionLocked = false
        phase = ExplorationPhase.Playing
        explorationSummary = null
        loadedUserId = null

        isNearBoss = false
        showStationInteractButton = false
    }

    private fun deleteCloudSession(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("exploration_sessions")
            .document(userId)
            .delete()
    }

    private fun resetRAMState() {
        openedChests.clear()
        defeatedBosses.clear()
        solvedStations.clear()
        playerX = 0
        playerY = 0
        bossesDefeatedCount = 0
        collectedDigits.clear()
        shouldShowTutorialOverlay = false
        inventory = Inventory()
        visibleTiles.clear()
        discoveredTiles.clear()
        isExplorationFinished = false
        phase = ExplorationPhase.Playing
        explorationSummary = null
    }

    private fun removeObjectAt(x: Int, y: Int) {
        val updatedMap = fullMapData.mapIndexed { rowIdx, row ->
            if (rowIdx == y) {
                row.mapIndexed { colIdx, tile ->
                    if (colIdx == x) {
                        tile.copy(obj = ObjectType.NONE, objectVariant = "")
                    } else tile
                }
            } else row
        }
        fullMapData = updatedMap
    }

    fun openChest(x: Int, y: Int) {
        currentChestPos = Pair(x, y)
        generateChestReward { reward ->
            currentReward = reward
            isChestOpen = true
            saveToCloud()
        }
    }

    fun collectReward() {
        val reward = currentReward ?: return
        val chestPos = currentChestPos

        inventory = when (reward.type) {
            RewardType.COIN -> {
                inventory.copy(coins = inventory.coins + reward.amount)
            }
            RewardType.SCROLL -> {
                inventory.copy(scrolls = (inventory.scrolls + reward).distinctBy { it.id })
            }
            RewardType.POWER_UP -> {
                inventory.copy(powerUps = inventory.powerUps + reward.powerUp!!)
            }
        }

        chestPos?.let { pos ->
            openedChests.add(pos)
            removeObjectAt(pos.first, pos.second)
        }

        isChestOpen = false
        currentReward = null
        saveToCloud()
    }

    fun toggleInventory() {
        isInventoryOpen = !isInventoryOpen
    }
    
    fun usePowerUp(type: PowerUpType) {
        val currentPowerUps = inventory.powerUps.toMutableList()
        if (currentPowerUps.remove(type)) {
            inventory = inventory.copy(powerUps = currentPowerUps)
            saveToCloud()
        }
    }
    
    fun onStationSolved() {
        if (System.currentTimeMillis() < stationCooldownEnd) {
            isStationOpen = false
            showStationCooldownMessage = true
            return
        }

        isStationOpen = false
        val quizId = currentStationPos?.let { quizIdMap[it] } ?: ""
        
        if (quizId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("unique_quizzes").document(quizId).get()
                .addOnSuccessListener { quizDoc ->
                    if (quizDoc.exists()) {
                        val questionsList = quizDoc.get("questions") as? List<Map<String, Any>>
                        val firstQuestionData = questionsList?.firstOrNull()
                        val qId = firstQuestionData?.get("id") as? String

                        if (qId != null) {
                            db.collection("questions").document(qId).get()
                                .addOnSuccessListener { qDoc ->
                                    if (qDoc.exists()) {
                                        val options = qDoc.get("options") as? List<String> ?: emptyList()
                                        val answerKeyList = qDoc.get("answerKey") as? List<*>
                                        val correctIndex = (answerKeyList?.firstOrNull() as? Number)?.toInt() ?: 0
                                        stationQuestion = Question(
                                            text = qDoc.getString("question") ?: "",
                                            options = options,
                                            correctAnswer = correctIndex
                                        )
                                        isStationQuizOpen = true
                                    } else {
                                        loadFallbackQuestion()
                                    }
                                }
                                .addOnFailureListener { loadFallbackQuestion() }
                        } else {
                            loadFallbackQuestion()
                        }
                    } else {
                        loadFallbackQuestion()
                    }
                }
                .addOnFailureListener { loadFallbackQuestion() }
        } else {
            loadFallbackQuestion()
        }
    }
    
    private fun loadFallbackQuestion() {
        stationQuestion = Question(
            text = "Sebuah motif batik mengalami transformasi rotasi 180 derajat terhadap titik pusat (0,0). Jika titik awal adalah (3, 4), di manakah posisi titik bayangannya?",
            options = listOf("(3, -4)", "(-2, 3)", "(-3, -4)", "(4, 3)"),
            correctAnswer = 2
        )
        isStationQuizOpen = true
    }

    fun onStationQuizAnswer(isCorrect: Boolean) {
        if (isCorrect) {
            inventory = inventory.copy(coins = inventory.coins + 500)
            currentStationPos?.let { pos ->
                solvedStations.add(pos)
                removeObjectAt(pos.first, pos.second)
            }
            checkProximity(playerX, playerY)
            saveToCloud()
        } else {
            stationCooldownEnd = System.currentTimeMillis() + 10000
        }
    }

    fun closeStationQuiz() {
        isStationQuizOpen = false
    }

    fun saveToCloud(customX: Int? = null, customY: Int? = null) {
        saveJob?.cancel()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (currentMapId.isEmpty() || isExplorationFinished) return
        val sessionData = hashMapOf(
            "mapId" to currentMapId,
            "sessionCode" to sessionCode,
            "playerX" to (customX ?: playerX),
            "playerY" to (customY ?: playerY),
            "openedChests" to openedChests.toList().map { "${it.first},${it.second}" },
            "defeatedBosses" to defeatedBosses.toList().map { "${it.first},${it.second}" },
            "solvedStations" to solvedStations.toList().map { "${it.first},${it.second}" },
            "inventory" to hashMapOf(
                "coins" to inventory.coins,
                "scrolls" to inventory.scrolls,
                "powerUps" to inventory.powerUps.map { it.name }
            ),
            "collectedDigits" to collectedDigits.joinToString(""),
            "bossesDefeatedCount" to bossesDefeatedCount,
            "isFinished" to false,
            "lastUpdated" to com.google.firebase.Timestamp.now()
        )
        FirebaseFirestore.getInstance()
            .collection("exploration_sessions")
            .document(userId)
            .set(sessionData, SetOptions.merge())
    }

    fun saveProgressDebounced() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(2000)
            saveToCloud()
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveJob?.cancel()
    }

    fun generateChestReward(onResult: (Reward) -> Unit) {
        val roll = (1..100).random()
        if (roll <= 50) {
            onResult(Reward(type = RewardType.COIN, amount = (10..100).random()))
        } else if (roll <= 80) {
            FirebaseFirestore.getInstance().collection("learning_contents")
                .get()
                .addOnSuccessListener { snapshots ->
                    val docs = snapshots.documents

                    val ownedScrollIds = inventory.scrolls.map { it.id }

                    // Filter dokumen yang belum ada di inventory (id dokumen digunakan sebagai pembanding)
                    val availableDocs = docs.filter { doc ->
                    !ownedScrollIds.contains(doc.id)
                    }

                    if (availableDocs.isEmpty()) {
                    // Jika semua scroll sudah didapatkan, berikan reward alternatif (misal: koin tambahan)
                    onResult(Reward(type = RewardType.COIN, amount = (50..100).random()))

                    } else {
                        val selectedDoc = availableDocs.random()
                        onResult(Reward(
                            type = RewardType.SCROLL,
                            id = selectedDoc.id,
                            title = selectedDoc.getString("title") ?: "Materi Baru",
                            content = selectedDoc.getString("content") ?: ""
                        ))
                    }
                }
            } else {
                onResult(Reward(type = RewardType.POWER_UP, powerUp = PowerUpType.values().random()))
        }
    }
}
