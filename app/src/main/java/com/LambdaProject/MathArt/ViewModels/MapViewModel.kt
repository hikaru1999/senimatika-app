package com.LambdaProject.MathArt.ViewModels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.*
import com.LambdaProject.MathArt.data.model.TileData
import com.LambdaProject.MathArt.data.model.getInteraction
import com.LambdaProject.MathArt.data.model.isWalkable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import toObjectType
import toTileType

data class ExplorationStats(
    val coinsCollected: Int = 0,
    val scrollsCollected: List<String> = emptyList(),
    val powerUpsCollected: List<PowerUpType> = emptyList(),
    val bossesDefeated: Int = 0,
    val isSuccess: Boolean = false
)

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
    
    val openedChests = mutableStateListOf<Pair<Int, Int>>()
    val defeatedBosses = mutableStateListOf<Pair<Int, Int>>()
    val visibleTiles = mutableStateSetOf<Pair<Int, Int>>()
    val discoveredTiles = mutableStateSetOf<Pair<Int, Int>>()

    var onBossTriggered: ((Int, Int, String) -> Unit)? = null

    // Simpan quizId per posisi tile jika ada
    private val quizIdMap = mutableMapOf<Pair<Int, Int>, String>()

    fun setInitialInventory(items: List<PowerUpType>) {
        inventory = inventory.copy(powerUps = items)
    }

    fun loadMap(mapId: String) {
        isLoading = true
        currentMapId = mapId
        FirebaseFirestore.getInstance()
            .collection("game_maps")
            .document(mapId)
            .get()
            .addOnSuccessListener { doc ->
                val width = doc.getLong("width")?.toInt()
                val height = doc.getLong("height")?.toInt()
                val tiles1D = doc.get("tiles") as? List<Map<String, Any>>
                val player = doc.get("playerStart") as? Map<*, *>

                if (width == null || height == null || tiles1D == null) {
                    isLoading = false
                    return@addOnSuccessListener
                }

                mapWidth = width
                mapHeight = height
                quizIdMap.clear()
                
                sessionCode = (100..999).random().toString()

                fullMapData = List(height) { y ->
                    List(width) { x ->
                        val index = y * width + x
                        val tileMap = tiles1D[index]
                        val objTypeStr = tileMap["object"] as? String ?: "NONE"
                        val objType = objTypeStr.toObjectType()
                        val pos = Pair(x, y)
                        
                        val qId = tileMap["quizId"] as? String
                        if (qId != null) {
                            quizIdMap[pos] = qId
                        }

                        val isCleared = pos in openedChests || pos in defeatedBosses

                        TileData(
                            ground = (tileMap["ground"] as? String)?.toTileType() ?: TileType.GROUND,
                            groundVariant = tileMap["groundVariant"] as? String ?: "tile_ground_1",
                            obj = if (isCleared) ObjectType.NONE else objType,
                            objectVariant = if (isCleared) "" else (tileMap["objectVariant"] as? String ?: "")
                        )
                    }
                }

                playerX = (player?.get("x") as? Long)?.toInt() ?: (width / 2)
                playerY = (player?.get("y") as? Long)?.toInt() ?: (height / 2)
                
                revealArea(playerX, playerY, 3)
                checkProximity(playerX, playerY)
                
                viewModelScope.launch {
                    delay(2000)
                    isLoading = false
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun move(dx: Int, dy: Int) {
        if (isLoading || isChestOpen || isInventoryOpen || isExplorationFinished || isStationOpen || isStationQuizOpen) return

        val newX = playerX + dx
        val newY = playerY + dy

        if (newX !in 0 until mapWidth || newY !in 0 until mapHeight) return

        val targetTile = fullMapData[newY][newX]

        if(!targetTile.isWalkable()) return

        playerX = newX
        playerY = newY

        revealArea(playerX, playerY, 3)
        checkProximity(playerX, playerY)
        handleInteraction(targetTile, newX, newY)
    }

    private fun checkProximity(px: Int, py: Int) {
        var nearStation = false
        var stationPos: Pair<Int, Int>? = null
        var nearBoss = false
        // Check surrounding 8 tiles
        for (dy in -1..1) {
            for (dx in -1..1) {
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

    fun handleInteraction(tile: TileData, x: Int, y: Int) {
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
                isInteractionLocked = true
                viewModelScope.launch {
                    delay(500)
                    extractItems()
                    isInteractionLocked = false
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

    fun onBossDefeated() {
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
        checkProximity(playerX, playerY) // Update near boss state after defeat
    }

    fun onBossVictory() {
        failExploration()
    }

    private fun extractItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        
        explorationSummary = ExplorationStats(
            coinsCollected = inventory.coins,
            scrollsCollected = inventory.scrolls,
            powerUpsCollected = inventory.powerUps,
            bossesDefeated = bossesDefeatedCount,
            isSuccess = true
        )

        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentCoins = snapshot.getLong("coins") ?: 0
            val currentScrolls = snapshot.get("scrolls") as? List<String> ?: emptyList()
            val currentPowerUps = (snapshot.get("powerUps") as? List<String>)?.map { PowerUpType.valueOf(it) } ?: emptyList()

            val newCoins = currentCoins + inventory.coins
            val newScrolls = (currentScrolls + inventory.scrolls).distinct()
            val newPowerUps = currentPowerUps + inventory.powerUps

            transaction.update(userRef, "coins", newCoins)
            transaction.update(userRef, "scrolls", newScrolls)
            transaction.update(userRef, "powerUps", newPowerUps.map { it.name })
            
            if (currentMapId == "tutorial") {
                transaction.update(userRef, "tutorial_completed", true)
            }
        }.addOnSuccessListener {
            isExplorationFinished = true
        }
    }

    fun failExploration() {
        explorationSummary = ExplorationStats(
            coinsCollected = 0,
            scrollsCollected = emptyList(),
            powerUpsCollected = emptyList(),
            bossesDefeated = bossesDefeatedCount,
            isSuccess = false
        )
        inventory = Inventory() 
        isExplorationFinished = true
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
        }
    }

    fun collectReward() {
        val reward = currentReward ?: return
        val chestPos = currentChestPos

        inventory = when (reward.type) {
            RewardType.COIN -> inventory.copy(coins = inventory.coins + reward.amount)
            RewardType.SCROLL -> inventory.copy(scrolls = inventory.scrolls + reward.content)
            RewardType.POWER_UP -> inventory.copy(powerUps = inventory.powerUps + reward.powerUp!!)
        }

        chestPos?.let { pos ->
            openedChests.add(pos)
            removeObjectAt(pos.first, pos.second)
        }

        isChestOpen = false
        currentReward = null
    }

    fun toggleInventory() {
        isInventoryOpen = !isInventoryOpen
    }
    
    fun usePowerUp(type: PowerUpType) {
        val currentPowerUps = inventory.powerUps.toMutableList()
        if (currentPowerUps.remove(type)) {
            inventory = inventory.copy(powerUps = currentPowerUps)
        }
    }
    
    fun onStationSolved() {
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
                                        val rawAnswerKey = qDoc.get("answerKey")
                                        val answerKeyStrings = when (rawAnswerKey) {
                                            is List<*> -> {
                                                if (rawAnswerKey.firstOrNull() is Number) {
                                                    rawAnswerKey.mapNotNull { (it as? Number)?.toInt()?.let { idx -> options.getOrNull(idx) } }
                                                } else {
                                                    rawAnswerKey.filterIsInstance<String>()
                                                }
                                            }
                                            else -> emptyList()
                                        }

                                        val correctIdx = if (answerKeyStrings.isNotEmpty()) {
                                            options.indexOf(answerKeyStrings.first())
                                        } else 0

                                        stationQuestion = Question(
                                            text = qDoc.getString("question") ?: "",
                                            options = options,
                                            correctAnswer = if (correctIdx != -1) correctIdx else 0
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
                removeObjectAt(pos.first, pos.second)
            }
            checkProximity(playerX, playerY)
        }
    }
    
    fun closeStationQuiz() {
        isStationQuizOpen = false
    }
}

fun generateChestReward(onResult: (Reward) -> Unit) {
    val roll = (1..100).random()
    when {
        roll <= 50 -> {
            onResult(Reward(type = RewardType.COIN, amount = (10..100).random()))
        }
        roll <= 80 -> {
            FirebaseFirestore.getInstance()
                .collection("learning_contents")
                .get()
                .addOnSuccessListener { snapshots ->
                    val docs = snapshots.documents
                    if (docs.isEmpty()) {
                        onResult(Reward(type = RewardType.SCROLL, content = "Materi Tidak Tersedia"))
                        return@addOnSuccessListener
                    }
                    val randomDoc = docs.random()
                    val content = randomDoc.getString("content") ?: "tidak ada isi"
                    onResult(Reward(type = RewardType.SCROLL, content = content))
                }
        }
        else -> {
            onResult(Reward(type = RewardType.POWER_UP, powerUp = PowerUpType.values().random()))
        }
    }
}
