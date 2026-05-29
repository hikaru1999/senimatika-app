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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import com.LambdaProject.MathArt.data.model.ExplorationStats
import com.LambdaProject.MathArt.data.model.unlockGeneralAchievement
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.json.JSONArray
import org.json.JSONObject
import toObjectType
import toTileType
import java.io.File
import kotlin.math.abs

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
    var playerHp: Float by mutableFloatStateOf(100f)
    var legendaryPityCounter by mutableIntStateOf(0)
    var currentMapId by mutableStateOf("")
    var isDroppedSackOpen by mutableStateOf(false)
    var currentSackPos by mutableStateOf<Pair<Int, Int>?>(null)
    var sackInventoryPreview by mutableStateOf<Inventory?>(null)
    var isChestOpen by mutableStateOf(false)
    var currentReward by mutableStateOf<Reward?>(null)
    var currentChestPos by mutableStateOf<Pair<Int, Int>?>(null)
    var isStationOpen by mutableStateOf(false)
    var sessionCode by mutableStateOf("482")
    val collectedDigits = mutableStateListOf<Char>()
    var isStationQuizOpen by mutableStateOf(false)
    var stationQuestion by mutableStateOf<Question?>(null)
    var stationCooldownEnd by mutableLongStateOf(0L)
    var showStationCooldownMessage by mutableStateOf(false)
    var deathSackInventory by mutableStateOf<Inventory?>(null)
    var deathSackPos by mutableStateOf<Pair<Int, Int>?>(null)
    var isDeathSackOpen by mutableStateOf(false)
    var maxBagWeight by mutableFloatStateOf(10.0f)
    var isBinocularActive by mutableStateOf(false)
    var isLeatherStrapsActive by mutableStateOf(false)
    var isFogOfWarEnabled by mutableStateOf(false)
    var isNightModeEnabled by mutableStateOf(false)
    var isCombinedEnabled by mutableStateOf(false)
    var isNormalEnabled by mutableStateOf(true)
    var showStationInteractButton by mutableStateOf(false)
    var currentStationPos by mutableStateOf<Pair<Int, Int>?>(null)
    var isNearBoss by mutableStateOf(false)
    var isTorchActive by mutableStateOf(false)
    var isLanternActive by mutableStateOf(false)
    var torchTimeLeft by mutableIntStateOf(0)
    var isExplorationFinished by mutableStateOf(false)
    var explorationSummary by mutableStateOf<ExplorationStats?>(null)
    var bossesDefeatedCount by mutableIntStateOf(0)
    var currentBossPos by mutableStateOf<Pair<Int, Int>?>(null)
    var binocularTimeLeft by mutableIntStateOf(0)
    var inventory by mutableStateOf(Inventory())
    var isInventoryOpen by mutableStateOf(false)
    var isInteractionLocked by mutableStateOf(false)
    var isTutorialReplayByStatus by mutableStateOf(false)
    var phase by mutableStateOf<ExplorationPhase>(ExplorationPhase.Playing)
        private set
    var selectedArtifact by mutableStateOf<ArtifactData?>(null)
        private set
    var unlockedArtifactIds = mutableStateListOf<String>()
        private set
    var unlockedArtifactDetails = mutableStateListOf<ArtifactData>()
        private set
    var isArtifactModalVisible by mutableStateOf(false)
        private set
    val openedChests = mutableStateListOf<Pair<Int, Int>>()
    val defeatedBosses = mutableStateListOf<Pair<Int, Int>>()
    val solvedStations = mutableStateListOf<Pair<Int, Int>>()
    val visibleTiles = mutableStateSetOf<Pair<Int, Int>>()
    val discoveredTiles = mutableStateSetOf<Pair<Int, Int>>()
    var droppedItemsMap = mutableStateMapOf<String, Inventory>()
    var onBossTriggered: ((Int, Int, String) -> Unit)? = null
    var isDropping by mutableStateOf(false)
    var shouldShowTutorialOverlay by mutableStateOf(false)
    private val quizIdMap = mutableMapOf<Pair<Int, Int>, String>()
    private val _currentSessionActive = MutableStateFlow(false)
    private var loadedUserId: String? = null
    private var saveJob: Job? = null
    private var lastSafeX by mutableIntStateOf(0)
    private var lastSafeY by mutableIntStateOf(0)
    private var itemsBroughtFromLobby = listOf<PowerUpType>()
    var hasActiveCloudSession by mutableStateOf<Boolean?>(null)
    private var isCloudChecking = false
    var isOnline by mutableStateOf(true)
    var isSyncingData by mutableStateOf(false)
    data class ArtifactData(
        val title: String = "",
        val materi: String = "",
        val content: String = "",
        val variant: String = "obj_landmark_tugu"
    )

    fun isSessionActiveFlow(mapId: String) = _currentSessionActive.asStateFlow()
    fun setInitialInventory(items: List<PowerUpType>) {
        inventory = inventory.copy(powerUps = items)
        itemsBroughtFromLobby = items

        val strapsCount = items.count { it == PowerUpType.LEATHER_STRAPS }
        isLeatherStrapsActive = strapsCount > 0
        refreshPowerUpEffects()

        /* if (strapsCount > 0) {
            isLeatherStrapsActive = true
            maxBagWeight = MAX_BAG_WEIGHT + (strapsCount * ADD_STRAP)
        } else {
            isLeatherStrapsActive = false
            maxBagWeight = MAX_BAG_WEIGHT
        } */

        isLanternActive = false
    }

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

    fun loadMap(mapId: String, context: Context? = null) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val wasFinished = isExplorationFinished || phase == ExplorationPhase.Finished

        if (currentMapId == mapId && fullMapData.isNotEmpty() && loadedUserId == currentUserId && !wasFinished) {
            isLoading = false
            return
        }

        viewModelScope.launch {
            isLoading = true
            currentMapId = mapId
            loadedUserId = currentUserId
            val db = FirebaseFirestore.getInstance()

            try {
                val userTask = db.collection("users").document(currentUserId).get()
                val sessionTask = db.collection("exploration_sessions").document(currentUserId).get()

                val userDoc = userTask.await()
                val sessionDoc = sessionTask.await()

                val isTutorialMap = mapId.contains("tutorial")
                val isTutorialAlreadyDone = userDoc.getBoolean("tutorial_completed") ?: false
                isTutorialReplayByStatus = isTutorialMap && isTutorialAlreadyDone

                legendaryPityCounter = userDoc.getLong("legendaryPityCounter")?.toInt() ?: 0
                val lostData = userDoc.get("lostInventory") as? Map<String, Any>
                val isResuming = (sessionDoc.getString("mapId") == mapId && !(sessionDoc.getBoolean("isFinished") ?: true))

                var cachedData: Map<String, Any>? = null
                val cacheFile = File(context?.filesDir, "map_cache_$mapId.json")

                if (cacheFile.exists()) {
                    withContext(Dispatchers.IO) {
                        val jsonStr = cacheFile.readText()
                        cachedData = jsonToMap(JSONObject(jsonStr))
                    }
                    processLoadedMapData(mapId, cachedData!!, sessionDoc, isResuming, lostData)
                }

                val mapDoc = db.collection("game_maps").document(mapId).get(Source.SERVER).await()
                val serverData = mapDoc.data

                if (serverData != null && serverData != cachedData) {
                    processLoadedMapData(mapId, serverData, sessionDoc, isResuming, lostData)
                    saveCacheToFile(context, mapId, serverData)
                }

            } catch (e: Exception) {
                Log.e("MapVM", "Error loading map: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    private fun syncPityToCloud() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("legendaryPityCounter", legendaryPityCounter)
            .addOnFailureListener { Log.e("MapVM", "Gagal sync pity counter") }
    }

    private suspend fun saveCacheToFile(context: Context?, mapId: String, data: Map<String, Any>) {
        context?.let { ctx ->
            withContext(Dispatchers.IO) {
                try {
                    val file = File(ctx.filesDir, "map_cache_$mapId.json")
                    file.writeText(JSONObject(data).toString())
                } catch (e: Exception) {
                    Log.e("Cache", "Gagal tulis cache")
                }
            }
        }
    }

    private suspend fun fetchScrollDetails(scrollIds: List<String>): List<Reward> {
        if (scrollIds.isEmpty()) return emptyList()
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshots = db.collection("learning_contents")
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

    private fun triggerSackOpening(x: Int, y: Int) {
        val posKey = "$x,$y"
        val items = droppedItemsMap[posKey]
        if (items != null) {
            currentSackPos = x to y
            sackInventoryPreview = items
            isDroppedSackOpen = true
        }
    }

    fun triggerArtifact(contentId: String, variant: String) {
        if (isInteractionLocked) return
        println("DEBUG VIEWMODEL: Memulai pengambilan data firestore untuk $contentId")

        viewModelScope.launch {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("artifact_contents")
                    .document(contentId)
                    .get()
                    .await()

                if (doc.exists()) {
                    val data = doc.toObject(ArtifactData::class.java)?.copy(variant = variant)
                    println("DEBUG VIEWMODEL: Data ditemukan: ${data?.title}")

                    selectedArtifact = data
                    isArtifactModalVisible = true

                    saveArtifactDiscovery(contentId, variant)

                    if (!unlockedArtifactIds.contains(contentId)) {
                        unlockedArtifactIds.add(contentId)
                        fetchUnlockedArtifactDetails()
                    }

                } else {
                    println("DEBUG VIEWMODEL: Dokumen $contentId TIDAK ADA di Firestore")
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Gagal mengambil artefak: ${e.message}")
            }
        }
    }

    private suspend fun saveArtifactDiscovery(contentId: String, variant: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val data = mapOf(
            "contentId" to contentId,
            "variant" to variant,
            "unlockedAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(uid)
            .collection("unlocked_artifacts").document(contentId)
            .set(data, SetOptions.merge())
            .await()
    }

    fun loadUserUnlockedArtifactIds() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .collection("unlocked_artifacts")
            .get()
            .addOnSuccessListener { snapshot ->
                unlockedArtifactIds.clear()
                snapshot.documents.forEach { doc ->
                    unlockedArtifactIds.add(doc.id)
                }
                fetchUnlockedArtifactDetails()
            }
    }

    fun fetchUnlockedArtifactDetails() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val userUnlockedSnapshot = db.collection("users").document(uid)
                    .collection("unlocked_artifacts").get().await()

                val variantMap = userUnlockedSnapshot.documents.associate {
                    it.id to (it.getString("variant") ?: "obj_landmark_tugu")
                }

                val details = mutableListOf<ArtifactData>()

                unlockedArtifactIds.forEach { id ->
                    try {
                        val doc = db.collection("artifact_contents").document(id).get().await()

                        if (doc.exists()) {
                            val data = ArtifactData(
                                title = doc.getString("title") ?: "",
                                materi = doc.getString("materi") ?: "",
                                content = doc.getString("content") ?: "",
                                variant = variantMap[id] ?: "obj_landmark_tugu"
                            )
                            details.add(data)
                        }
                    } catch (e: Exception) {
                        Log.e("MapVM", "Gagal load konten artefak $id: ${e.message}")
                    }
                }

                unlockedArtifactDetails.clear()
                unlockedArtifactDetails.addAll(details)

            } catch (e: Exception) {
                Log.e("MapVM", "Gagal load riwayat artefak user: ${e.message}")
            }
        }

//        viewModelScope.launch {
//            val details = mutableListOf<ArtifactData>()
//            unlockedArtifactIds.forEach { id ->
//                try {
//                    val doc = db.collection("artifact_contents").document(id).get().await()
//                    if (doc.exists()) {
//                        val data = ArtifactData(
//                            title = doc.getString("title") ?: "",
//                            materi = doc.getString("materi") ?: "",
//                            content = doc.getString("content") ?: "",
//                            variant = doc.getString("variant") ?: "obj_landmark_tugu"
//                        )
//                        details.add(data)
//                    }
//                } catch (e: Exception) {
//                    Log.e("MapVM", "Error fetch detail: ${e.message}")
//                }
//            }
//            unlockedArtifactDetails.clear()
//            unlockedArtifactDetails.addAll(details)
//        }
    }

    fun openArtifactFromInventory(artifact: ArtifactData) {
        selectedArtifact = artifact
        isArtifactModalVisible = true
    }

    fun closeArtifactModal() {
        isArtifactModalVisible = false
        selectedArtifact = null
    }

    fun pickItemFromSack(pu: PowerUpType, context: Context?) {
        val pos = currentSackPos ?: return
        val posKey = "${pos.first},${pos.second}"
        val itemsInSack = droppedItemsMap[posKey] ?: return

        val itemWeight = getPowerUpWeight(pu)
        val currentWeight = inventory.calculateTotalWeight()

        if (currentWeight + itemWeight <= MAX_BAG_WEIGHT) {

            inventory = inventory.copy(powerUps = inventory.powerUps + pu)


            val updatedSackList = itemsInSack.powerUps.toMutableList()
            updatedSackList.remove(pu)

            if (updatedSackList.isEmpty()) {

                droppedItemsMap.remove(posKey)
                removeObjectAt(pos.first, pos.second)
                isDroppedSackOpen = false
                currentSackPos = null
                sackInventoryPreview = null
            } else {
                val newSackInv = itemsInSack.copy(powerUps = updatedSackList)
                droppedItemsMap[posKey] = newSackInv
                sackInventoryPreview = newSackInv
            }

            refreshPowerUpEffects()
            saveToCloud()
        } else {
            android.widget.Toast.makeText(context, "Tas terlalu penuh!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processLoadedMapData(
        mapId: String,
        mapData: Map<String, Any>,
        sessionDoc: DocumentSnapshot,
        isResuming: Boolean,
        lostData: Map<String, Any>? = null
    ) {
        val visibility = mapData["visibility"] as? Map<String, Any>
        isFogOfWarEnabled = visibility?.get("fogOfWar") as? Boolean ?: false
        isNightModeEnabled = visibility?.get("nightMode") as? Boolean ?: false
        isCombinedEnabled = visibility?.get("fogNightCombined") as? Boolean ?: false
        isNormalEnabled = visibility?.get("normal") as? Boolean ?: true

        val width = (mapData["width"] as? Number)?.toInt() ?: 0
        val height = (mapData["height"] as? Number)?.toInt() ?: 0
        val tiles1D = mapData["tiles"] as? List<Map<String, Any>>
        val player = mapData["playerStart"] as? Map<String, Any>

        if (width == 0 || height == 0 || tiles1D == null) return

        mapWidth = width
        mapHeight = height
        quizIdMap.clear()

        deathSackInventory = null
        deathSackPos = null

        if (lostData != null && lostData["mapId"] == mapId) {
            val sX = (lostData["x"] as? Number)?.toInt() ?: -1
            val sY = (lostData["y"] as? Number)?.toInt() ?: -1
            val invMap = lostData["inventory"] as? Map<String, Any>

            if (sX != -1 && sY != -1 && invMap != null) {
                deathSackPos = sX to sY

                val coins = (invMap["coins"] as? Number)?.toInt() ?: 0
                val puNames = invMap["powerUps"] as? List<String> ?: emptyList()
                val pus = puNames.mapNotNull { try { PowerUpType.valueOf(it) } catch(e: Exception) { null } }

                deathSackInventory = Inventory(coins = coins, powerUps = pus)
            }
        }

        if (isResuming) {
            stationCooldownEnd = sessionDoc.getLong("stationCooldownEnd") ?: 0L
            sessionCode = sessionDoc.getString("sessionCode") ?: "482"
            playerHp = sessionDoc.getDouble("playerHp")?.toFloat() ?: 100f
            playerX = sessionDoc.getLong("playerX")?.toInt() ?: 0
            playerY = sessionDoc.getLong("playerY")?.toInt() ?: 0

            lastSafeX = playerX
            lastSafeY = playerY

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

            val strapsCount = invPUs.count { it == PowerUpType.LEATHER_STRAPS }
            maxBagWeight = MAX_BAG_WEIGHT + (strapsCount * ADD_STRAP)
            isLeatherStrapsActive = strapsCount > 0

            viewModelScope.launch {
                val detailedScrolls = fetchScrollDetails(invScrollIds)
                inventory = Inventory(invCoins, detailedScrolls, invPUs)
            }

            droppedItemsMap.clear()
            val cloudDropped = sessionDoc.get("droppedItems") as? Map<String, Map<String, Any>>
            cloudDropped?.forEach { (posKey, data) ->
                val puNames = data["powerUps"] as? List<String> ?: emptyList()
                val pus = puNames.mapNotNull { try { PowerUpType.valueOf(it) } catch(e: Exception) { null } }
                droppedItemsMap[posKey] = Inventory(powerUps = pus)
            }

            val savedFog = sessionDoc.get("discoveredTiles") as? List<String> ?: emptyList()
            discoveredTiles.clear()
            savedFog.forEach { coords ->
                val parts = coords.split(",")
                if (parts.size == 2) {
                    val x = parts[0].toIntOrNull() ?: 0
                    val y = parts[1].toIntOrNull() ?: 0
                    discoveredTiles.add(x to y)
                }
            }

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

            discoveredTiles.clear()
            updateDiscovery(playerX, playerY,0, 0)
            
            if (mapId.contains("tutorial")) {
                shouldShowTutorialOverlay = true
            }

            playerHp = if (mapId.contains("large") || mapId.contains("level_3")) {
                40f
            } else {
                100f
            }
        }

        val treeVariants = listOf("obj_tree_1", "obj_tree_2", "obj_tree_3")

        fullMapData = List(height) { y ->
            List(width) { x ->
                val index = y * width + x
                val tileMap = tiles1D.getOrNull(index)
                val pos = Pair(x, y)
                val posKey = "$x,$y"
                val isCleared = pos in openedChests || pos in defeatedBosses || pos in solvedStations

                val hasDroppedSack = droppedItemsMap.containsKey(posKey)
                val isDeathSack = pos == deathSackPos

                if (tileMap == null) {
                    val treeIdx = abs(x * 31 + y * 17) % treeVariants.size
                    TileData(TileType.GROUND, "tile_ground_1",
                        if (isDeathSack || hasDroppedSack || isCleared) ObjectType.NONE else ObjectType.TREE_MEDIUM,
                        when {
                            isDeathSack -> "obj_sack"
                            hasDroppedSack -> "obj_sack_inv"
                            isCleared -> ""
                            else -> treeVariants[treeIdx]
                        }
                    )
                } else {
                    val objTypeStr = tileMap["object"] as? String ?: "NONE"
                    val objType = objTypeStr.toObjectType()
                    val qId = tileMap["quizId"] as? String
                    val isCollected = unlockedArtifactIds.contains(tileMap["contentId"] as? String)
                    if (qId != null) quizIdMap[pos] = qId

                    TileData(
                        ground = (tileMap["ground"] as? String)?.toTileType() ?: TileType.GROUND,
                        groundVariant = tileMap["groundVariant"] as? String ?: "tile_ground_1",
                        obj = if (isDeathSack) ObjectType.NONE else if (hasDroppedSack) ObjectType.NONE else if (isCleared) ObjectType.NONE else objType,
                        objectVariant = when {
                            isDeathSack -> "obj_sack"
                            hasDroppedSack -> "obj_sack_inv"
                            isCleared -> ""
                            else -> tileMap["objectVariant"] as? String ?: ""
                        },
                        contentId = tileMap["contentId"] as? String,
                        isCollected = isCollected
                    )
                }
            }
        }

        /* revealArea(playerX, playerY, 1) */
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
        if(!targetTile.isWalkable()) {
            audio?.playSfx("block")
            return
        }

        lastSafeX = playerX
        lastSafeY = playerY

        playerX = newX
        playerY = newY
        audio?.playSfx("move")

        updateDiscovery(playerX, playerY, dx, dy)

        checkProximity(playerX, playerY)

        val interaction = targetTile.getInteraction()
        if (interaction == InteractionType.BOSS) {
            saveToCloud(customX = lastSafeX, customY = lastSafeY)
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

    fun dropPowerUp(pu: PowerUpType) {
        if (isDropping) return

        viewModelScope.launch {
            isDropping = true
            delay(1000)

            val currentPus = inventory.powerUps.toMutableList()
            val wasRemoved = currentPus.remove(pu)

            if (wasRemoved) {
                inventory = inventory.copy(powerUps = currentPus)

                val strapsCount = currentPus.count { it == PowerUpType.LEATHER_STRAPS }

                if (strapsCount > 0) {
                    isLeatherStrapsActive = true
                    maxBagWeight = MAX_BAG_WEIGHT + (strapsCount * ADD_STRAP)
                } else {
                    isLeatherStrapsActive = false
                    maxBagWeight = MAX_BAG_WEIGHT
                }

                if (pu == PowerUpType.LANTERN && !currentPus.contains(PowerUpType.LANTERN)) {
                    isLanternActive = false
                }

                if (pu == PowerUpType.LEATHER_STRAPS && !currentPus.contains(PowerUpType.LEATHER_STRAPS)) {
                    isLeatherStrapsActive = false
                }

                refreshPowerUpEffects()

                val x = playerX
                val y = playerY
                val posKey = "$x,$y"

                val existingSack = droppedItemsMap[posKey] ?: Inventory()
                val updatedSackPUs = existingSack.powerUps + pu
                droppedItemsMap[posKey] = existingSack.copy(powerUps = updatedSackPUs)

                fullMapData = fullMapData.mapIndexed { rowIdx, row ->
                    if (rowIdx == y) {
                        row.mapIndexed { colIdx, tile ->
                            if (colIdx == x) tile.copy(objectVariant = "obj_sack_inv") else tile
                        }
                    } else row
                }
                saveToCloud()
            }
            isDropping = false
        }
    }

    fun handleInteraction(tile: TileData, x: Int, y: Int, context: Context? = null, audio: ExplorationAudioManager?) {
        if (isInteractionLocked) return

        if (tile.objectVariant == "obj_sack") {
            isInteractionLocked = true
            viewModelScope.launch {
                delay(300)
                isDeathSackOpen = true
                isInteractionLocked = false
            }
            return
        }

        if (tile.objectVariant == "obj_sack_inv") {
            isInteractionLocked = true
            viewModelScope.launch {
                delay(300)
                triggerSackOpening(x, y)
                isInteractionLocked = false
            }
            return
        }

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

            val isEasyLevel = currentMapId.contains("tutorial")
            val isReplay = isTutorialReplayByStatus

            inventory = inventory.copy(coins = inventory.coins + if (isReplay && isEasyLevel) 0 else 100)
            
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

        val summary = ExplorationStats(
            coinsCollected = finalBagCoins,
            scrollsCollected = finalBagScrolls,
            powerUpsCollected = inventory.powerUps,
            bossesDefeated = bossesDefeatedCount,
            isSuccess = true
        )

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            val currentCoins = snapshot.getLong("coins") ?: 0
            val currentScrolls = snapshot.get("scrolls") as? List<String> ?: emptyList()
            val currentPowerUpsStrings = snapshot.get("powerUps") as? List<String> ?: emptyList()

            val newCoins = currentCoins + finalBagCoins
            val newScrolls = (currentScrolls + inventory.scrolls.map { it.id }).distinct()
            val newPowerUpsStrings = currentPowerUpsStrings + finalBagPUs

            transaction.update(userRef, mapOf(
                "coins" to newCoins,
                "scrolls" to newScrolls,
                "powerUps" to newPowerUpsStrings,
                "lostInventory" to null
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

    fun pickItemFromDeathSack(pu: PowerUpType, context: Context?) {
        val items = deathSackInventory ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (inventory.calculateTotalWeight() + getPowerUpWeight(pu) <= MAX_BAG_WEIGHT) {

            inventory = inventory.copy(powerUps = inventory.powerUps + pu)

            refreshPowerUpEffects()

            val updatedList = items.powerUps.toMutableList()
            updatedList.remove(pu)
            deathSackInventory = items.copy(powerUps = updatedList)

            viewModelScope.launch {
                if (updatedList.isEmpty() && (deathSackInventory?.coins ?: 0) <= 0) {
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                        .update("lostInventory", null).await()
                    removeObjectAt(deathSackPos!!.first, deathSackPos!!.second)
                    deathSackPos = null
                    isDeathSackOpen = false
                } else {
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                        .update("lostInventory.inventory.powerUps", updatedList.map { it.name }).await()
                }
            }
        }
    }

    fun pickCoinsFromDeathSack(context: Context?) {
        val sack = deathSackInventory ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val pos = deathSackPos ?: return

        if (sack.coins > 0) {

            inventory = inventory.copy(coins = inventory.coins + sack.coins)

            val updatedSack = sack.copy(coins = 0)
            deathSackInventory = updatedSack

            viewModelScope.launch {
                try {
                    if (updatedSack.powerUps.isEmpty() && updatedSack.coins == 0) {
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("lostInventory", null).await()
                        removeObjectAt(pos.first, pos.second)
                        deathSackPos = null
                        isDeathSackOpen = false
                    } else {
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("lostInventory.inventory.coins", 0).await()
                    }
                } catch (e: Exception) {
                    Log.e("MapVM", "Gagal ambil koin: ${e.message}")
                }
            }
        }
    }

    fun failExploration(lastBossX: Int? = null, lastBossY: Int? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val sackX = lastSafeX
        val sackY = lastSafeY

        val lostData = hashMapOf(
            "mapId" to currentMapId,
            "x" to sackX,
            "y" to sackY,
            "inventory" to hashMapOf(
                "coins" to inventory.coins,
                "scrolls" to inventory.scrolls.map { it.id },
                "powerUps" to inventory.powerUps.map { it.name }
            )
        )

        explorationSummary = ExplorationStats(
            coinsCollected = 0,
            scrollsCollected = emptyList(),
            powerUpsCollected = emptyList(),
            bossesDefeated = bossesDefeatedCount,
            isSuccess = false
        )

        viewModelScope.launch {
            try {
                if (!inventory.isEmpty()) {
                    val lostData = hashMapOf(
                        "mapId" to currentMapId,
                        "x" to lastSafeX,
                        "y" to lastSafeY,
                        "inventory" to hashMapOf(
                            "coins" to inventory.coins,
                            "powerUps" to inventory.powerUps.map { it.name },
                            "scrolls" to inventory.scrolls.map { it.id }
                        )
                    )
                    db.collection("users").document(userId).update("lostInventory", lostData).await()
                    Log.d("MapVM", "Death sack disimpan di $lastSafeX, $lastSafeY")
                } else {
                    db.collection("users").document(userId).update("lostInventory", null).await()
                    Log.d("MapVM", "Tas kosong, tidak ada death sack yang dibuat.")
                }

                db.collection("exploration_sessions").document(userId)
                    .update("isFinished", true)
                    .await()

                isExplorationFinished = true
                phase = ExplorationPhase.Finished

                inventory = Inventory()

                _currentSessionActive.value = false
                hasActiveCloudSession = false
            } catch (e: Exception) {
                Log.e("MapVM", "Gagal menutup sesi game over: ${e.message}")
            }
        }
    }

    fun clearSession(context: Context? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        droppedItemsMap.clear()
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
        droppedItemsMap.clear()
        inventory = Inventory()
        visibleTiles.clear()
        discoveredTiles.clear()
        isExplorationFinished = false
        phase = ExplorationPhase.Playing
        explorationSummary = null
    }

    private fun openDroppedSack(x: Int, y: Int, context: Context?) {
        val posKey = "$x,$y"
        val itemsInSack = droppedItemsMap[posKey] ?: return

        val currentWeight = inventory.calculateTotalWeight()
        val sackWeight = itemsInSack.calculateTotalWeight()

        if (currentWeight + sackWeight <= MAX_BAG_WEIGHT) {
            inventory = inventory.copy(
                powerUps = inventory.powerUps + itemsInSack.powerUps,
                scrolls = inventory.scrolls + itemsInSack.scrolls
            )

            droppedItemsMap.remove(posKey)

            removeObjectAt(x, y)
            saveToCloud()

            android.widget.Toast.makeText(context, "Item diambil kembali!", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Tas terlalu berat!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeObjectAt(x: Int, y: Int) {
        val updatedMap = fullMapData.toMutableList()
        val updatedRow = updatedMap[y].toMutableList()

        updatedRow[x] = updatedRow[x].copy(obj = ObjectType.NONE, objectVariant = "")
        updatedMap[y] = updatedRow

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
    
    fun usePowerUp(type: PowerUpType, context: Context?, audio: ExplorationAudioManager?) {
        val currentPowerUps = inventory.powerUps.toMutableList()
        if (!currentPowerUps.contains(type)) return

        when (type) {
            PowerUpType.HEALING_VIAL-> {
                currentPowerUps.remove(type)
                playerHp = (playerHp + 20f).coerceAtMost(100f)
                audio?.playSfx("drink")
            }

            PowerUpType.LEATHER_STRAPS -> {
                if (isLeatherStrapsActive) {
                    if (inventory.calculateTotalWeight() <= MAX_BAG_WEIGHT) {
                        isLeatherStrapsActive = false
                        refreshPowerUpEffects()
                        /* maxBagWeight = MAX_BAG_WEIGHT */
                        audio?.playSfx("")
                    } else {
                        Log.d("Inventory", "Gagal melepas: Tas terlalu penuh!")
                    }
                } else {
                    isLeatherStrapsActive = true
                    /* maxBagWeight = MAX_BAG_WEIGHT + ADD_STRAP */
                    refreshPowerUpEffects()
                    audio?.playSfx("wear")
                }
            }

            PowerUpType.MAGIC_KEY -> {
                if (showStationInteractButton && currentStationPos != null) {
                    currentPowerUps.remove(type)
                    useMagicKeyOnStation(context, audio)
                } else {
                    return
                }
            }

            PowerUpType.BINOCULAR -> {
                currentPowerUps.remove(type)
                viewModelScope.launch {
                    isBinocularActive = true
                    binocularTimeLeft = 5
                    audio?.playSfx("zoom_out")

                    while (binocularTimeLeft > 0) {
                        delay(1000)
                        binocularTimeLeft--
                    }


                    isBinocularActive = false
                }
            }

            PowerUpType.TORCH -> {
                currentPowerUps.remove(type)
                viewModelScope.launch {
                    isTorchActive = true
                    torchTimeLeft = 5
                    audio?.playSfx("light_on")
                    while (torchTimeLeft > 0) {
                        delay(1000)
                        torchTimeLeft--
                    }
                    isTorchActive = false
                }
            }

            PowerUpType.LANTERN -> {
                isLanternActive = !isLanternActive
                audio?.playSfx(if (isLanternActive) "light_on" else "")
            }

            else -> {
                return
            }
        }

        inventory = inventory.copy(powerUps = currentPowerUps)
        saveProgressDebounced()
    }

    private fun useMagicKeyOnStation(context: Context?, audio: ExplorationAudioManager?) {
        currentStationPos?.let { pos ->
            solvedStations.add(pos)
            removeObjectAt(pos.first, pos.second)

            inventory = inventory.copy(coins = inventory.coins + 100)
            audio?.playSfx("unlocked")

            checkProximity(playerX, playerY)
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
                                        val type = qDoc.getString("questionType") ?: "multiple_choice"

                                        val rawAnswerKey = qDoc.get("answerKey") as? List<*>
                                        val answerKeyList = rawAnswerKey?.map { it.toString() } ?: emptyList()

                                        val correctIndex = (rawAnswerKey?.firstOrNull() as? Number)?.toInt() ?: 0
                                        stationQuestion = Question(
                                            text = qDoc.getString("question") ?: "",
                                            options = options,
                                            answerKey = answerKeyList,
                                            correctAnswer = correctIndex,
                                            questionType = type
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

            val isEasyLevel = currentMapId.contains("tutorial")
            val isReplay = isTutorialReplayByStatus
            val newCoins = inventory.coins + if (isEasyLevel && isReplay) 0 else 500

            currentStationPos?.let { pos ->
                solvedStations.add(pos)
                removeObjectAt(pos.first, pos.second)
            }
            inventory = inventory.copy(coins = newCoins)

            isStationQuizOpen = false

            checkProximity(playerX, playerY)
            saveToCloud()
        } else {
            val penaltyMinutes = (3..5).random()
            stationCooldownEnd = System.currentTimeMillis() + (penaltyMinutes * 60 * 1000L)
        }
    }

    fun closeStationQuiz() {
        isStationQuizOpen = false
    }

    fun saveToCloud(customX: Int? = null, customY: Int? = null) {
        saveJob?.cancel()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val droppedItemsData = droppedItemsMap.mapValues { (_, inv) ->
            mapOf(
                "powerUps" to inv.powerUps.map { it.name },
                "scrolls" to inv.scrolls.map { it.id }
            )
        }

        if (currentMapId.isEmpty() || isExplorationFinished) return

        val inventoryData = if (isTutorialReplayByStatus) {
            mapOf(
                "coins" to 0,
                "scrolls" to emptyList<String>(),
                "powerUps" to itemsBroughtFromLobby.map { it.name }
            )
        } else {
            mapOf(
                "coins" to inventory.coins,
                "scrolls" to inventory.scrolls.map { it.id },
                "powerUps" to inventory.powerUps.map { it.name }
            )
        }

        val sessionData = hashMapOf(
            "mapId" to currentMapId,
            "playerHp" to playerHp,
            "isTutorialReplay" to isTutorialReplayByStatus,
            "maxBagWeight" to maxBagWeight,
            "droppedItems" to droppedItemsData,
            "sessionCode" to sessionCode,
            "playerX" to (customX ?: playerX),
            "playerY" to (customY ?: playerY),
            "stationCooldownEnd" to stationCooldownEnd,
            "openedChests" to openedChests.toList().map { "${it.first},${it.second}" },
            "defeatedBosses" to defeatedBosses.toList().map { "${it.first},${it.second}" },
            "solvedStations" to solvedStations.toList().map { "${it.first},${it.second}" },
            "discoveredTiles" to discoveredTiles.toList().map { "${it.first},${it.second}" },
            "inventory" to inventoryData,

            /* "inventory" to hashMapOf(
                "coins" to inventory.coins,
                "scrolls" to inventory.scrolls.map { it.id },
                "powerUps" to inventory.powerUps.map { it.name }
            ), */
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

    private fun refreshPowerUpEffects() {
        val strapsInBagCount = inventory.powerUps.count { it == PowerUpType.LEATHER_STRAPS }

        if (isLeatherStrapsActive) {
            maxBagWeight = MAX_BAG_WEIGHT + (strapsInBagCount * ADD_STRAP)
        } else {
            maxBagWeight = MAX_BAG_WEIGHT
        }

        if (!inventory.powerUps.contains(PowerUpType.LANTERN)) {
            isLanternActive = false
        }
    }

    fun generateChestReward(onResult: (Reward) -> Unit) {
        val roll = (1..100).random()
        val isEasyLevel = currentMapId.contains("tutorial")
        val isReplay = isTutorialReplayByStatus

        if (isEasyLevel && isReplay) {
            when {
                roll <= 50 -> {
                    onResult(Reward(type = RewardType.COIN, amount = 0))
                }

                else -> {
                    fetchAvailableScroll { reward -> onResult(reward) }
                }
            }
            return
        }

        legendaryPityCounter++
        syncPityToCloud()

        val getLegendaryPool = {
            val pool = mutableListOf(PowerUpType.MAGIC_KEY, PowerUpType.LEATHER_STRAPS)
            if (!inventory.powerUps.contains(PowerUpType.LANTERN)) {
                pool.add(PowerUpType.LANTERN)
            }
            pool
        }

        if (legendaryPityCounter >= 60) {
            val selectedLegendary = getLegendaryPool().random()
            onResult(Reward(type = RewardType.POWER_UP, powerUp = selectedLegendary))
            legendaryPityCounter = 0
            syncPityToCloud()
            return
        }

        if (isEasyLevel) {
            when {
                roll <= 70 -> {
                    onResult(Reward(type = RewardType.COIN, amount = (150..300).random()))
                }
                roll <= 90 -> {
                    fetchAvailableScroll { reward -> onResult(reward) }
                }
                else -> {
                    val basicPUs = listOf(PowerUpType.FREEZE_TIMER, PowerUpType.HEALING_VIAL, PowerUpType.BINOCULAR)
                    onResult(Reward(type = RewardType.POWER_UP, powerUp = basicPUs.random()))
                }
            }
            return
        }

        when {
            roll <= 50 -> {
                fetchAvailableScroll { reward -> onResult(reward) }
            }

            roll <= 85 -> {
                onResult(Reward(type = RewardType.COIN, amount = (20..99).random()))
            }

            else -> {
                val puRoll = (1..100).random()

                val selectedPU = when {
                    puRoll <= 5 -> {
                        legendaryPityCounter = 0
                        syncPityToCloud()
                        getLegendaryPool().random()
                    }

                    puRoll <= 30 -> {
                        val tacticalPUs = listOf(
                            PowerUpType.HEALING_VIAL,
                            PowerUpType.STREAK_PROTECTION,
                            PowerUpType.BINOCULAR,
                            PowerUpType.TORCH
                        )
                        tacticalPUs.random()
                    }

                    else -> {
                        if ((1..2).random() == 1) PowerUpType.FREEZE_TIMER else PowerUpType.REMOVE_TWO_OPTIONS
                    }
                }
                onResult(Reward(type = RewardType.POWER_UP, powerUp = selectedPU))
                saveToCloud()
            }
        }
    }

    fun addLearningPityBonus(context: Context?) {
        legendaryPityCounter = (legendaryPityCounter + 5).coerceAtMost(40)
        syncPityToCloud()

        viewModelScope.launch {
            saveProgressDebounced()
        }
    }

    suspend fun processExplorationResults(
        userId: String,
        currentMapId: String,
        itemsUsed: List<PowerUpType>,
        achievementViewModel: AchievementViewModel
    ) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)
        val sessionRef = db.collection("exploration_sessions").document(userId)
        val achSubColRef = userRef.collection("achievements")

        try {
            val userSnapshot = userRef.get().await()
            val isTutorialMap = currentMapId.contains("tutorial", ignoreCase = true)
            val isTutorialAlreadyDone = userSnapshot.getBoolean("tutorial_completed") ?: false
            val isReplay = isTutorialReplayByStatus || (isTutorialMap && isTutorialAlreadyDone)

            if (isReplay) {
                sessionRef.update("isFinished", true).await()

                inventory = Inventory(
                    coins = 0,
                    scrolls = emptyList(),
                    powerUps = itemsBroughtFromLobby
                )

                explorationSummary = explorationSummary?.copy(
                    coinsCollected = 0,
                    powerUpsCollected = emptyList(),
                    scrollsCollected = emptyList()
                )
                Log.d("Exploration", "Replay Tutorial: Reward dibatalkan.")
                return
            }

            val existingAchievements = achSubColRef.get().await().documents.map { it.id }
            val newlyEarned = mutableListOf<String>()

            db.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)

                /* val currentCoins = userDoc.getLong("coins") ?: 0L */
                val currentCoins = userDoc.getLong("coins") ?: 0L
                val totalBoss = (userDoc.getLong("totalBossDefeated") ?: 0L) + bossesDefeatedCount
                val totalChests = (userDoc.getLong("totalChestsOpened") ?: 0L) + openedChests.size
                val finishedMaps = (userDoc.get("finishedMapIds") as? List<String> ?: emptyList()).toMutableList()

                if (!finishedMaps.contains(currentMapId)) finishedMaps.add(currentMapId)

                fun checkAndTrigger(name: String) {
                    if (!existingAchievements.contains(name) && !newlyEarned.contains(name)) {
                        newlyEarned.add(name)
                        val achDocRef = achSubColRef.document(name)
                        val data = hashMapOf(
                            "name" to name,
                            "timestamp" to com.google.firebase.Timestamp.now(),
                            "isRead" to false,
                            "isUnlocked" to true,
                            "notified" to false
                        )
                        transaction.set(achDocRef, data)
                    }
                }

                if (isTutorialMap && !isTutorialAlreadyDone) {
                    checkAndTrigger("Apprentice No More")
                }

                if (totalChests >= 10) checkAndTrigger("The Fortune Finder")
                if (totalChests >= 20) checkAndTrigger("Indiana Jones’ Grocery List")

                if (totalBoss >= 3) checkAndTrigger("No More Lessons")
                if (totalBoss >= 10) checkAndTrigger("Who is the Boss Now?")

                if (finishedMaps.size >= 3) checkAndTrigger("Beyond the Guided Path")

                val activeItems = itemsUsed.filter { it != PowerUpType.LEATHER_STRAPS }
                if (activeItems.isEmpty() && !currentMapId.contains("tutorial")) {
                    checkAndTrigger("Lone Wolf Archivist")
                }

                val updates = mutableMapOf<String, Any>(
                    "totalBossDefeated" to totalBoss,
                    "totalChestsOpened" to totalChests,
                    "finishedMapIds" to finishedMaps,
                    "coins" to currentCoins + inventory.coins,
                )

                if (isTutorialMap) {
                    updates["tutorial_completed"] = true
                }

                transaction.update(userRef, updates)
                transaction.update(sessionRef, "isFinished", true)

                /* fun trigger(name: String) {
                    unlockGeneralAchievement(userId, name)
                    DataAchievements.find { it.name == name }?.let { item ->
                        achievementViewModel.triggerAchievement(item)
                    }
                } */
                null
            }.await()
            if (newlyEarned.isNotEmpty()) {
                val nameToShow = newlyEarned.first()
                com.LambdaProject.MathArt.data.DataAchievements.find { it.name == nameToShow }?.let { item ->
                    achievementViewModel.triggerAchievement(item)
                }
            }
        } catch (e: Exception) {
            Log.e("Exploration", "Error processing results: ${e.message}")
        }
    }

    /* fun updateDiscovery(x: Int, y: Int) {
        val radius = 1
        for (i in -radius..radius) {
            for (j in -radius..radius) {
                discoveredTiles.add(x + i to y + j)
            }
        }
    } */

    fun updateDiscovery(px: Int, py: Int, dx: Int = 0, dy: Int = 0) {
        discoveredTiles.add(px to py)

        if (dx != 0 || dy != 0) {
            val nx = px + dx
            val ny = py + dy

            if (nx in 0 until mapWidth && ny in 0 until mapHeight) {
                discoveredTiles.add(nx to ny)
            }

            /*
            val nx2 = px + (dx * 2)
            val ny2 = py + (dy * 2)
            if (nx2 in 0 until mapWidth && ny2 in 0 until mapHeight) {
                discoveredTiles.add(nx2 to ny2)
            }
            */
        }

    }

    private fun fetchAvailableScroll(onComplete: (Reward) -> Unit) {
        FirebaseFirestore.getInstance().collection("learning_contents")
            .get()
            .addOnSuccessListener { snapshots ->
                val docs = snapshots.documents
                val ownedScrollIds = inventory.scrolls.map { it.id }
                val availableDocs = docs.filter { !ownedScrollIds.contains(it.id) }

                if (availableDocs.isEmpty()) {
                    onComplete(Reward(type = RewardType.COIN, amount = (100..200).random()))
                } else {
                    val selectedDoc = availableDocs.random()
                    onComplete(Reward(
                        type = RewardType.SCROLL,
                        id = selectedDoc.id,
                        title = selectedDoc.getString("title") ?: "Materi Baru",
                        content = selectedDoc.getString("content") ?: ""
                    ))
                }
            }
    }

    var globalActiveMapId by mutableStateOf<String?>(null)

    fun checkGlobalSessionStatus() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("exploration_sessions")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val isFinished = doc.getBoolean("isFinished") ?: true
                if (!isFinished) {
                    globalActiveMapId = doc.getString("mapId")
                } else {
                    globalActiveMapId = null
                }
            }
    }
}
