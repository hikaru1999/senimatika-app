package com.LambdaProject.MathArt.data.model

import com.LambdaProject.MathArt.data.PowerUpType

data class ExplorationStats(
    val coinsCollected: Int = 0,
    val scrollsCollected: List<String> = emptyList(),
    val powerUpsCollected: List<PowerUpType> = emptyList(),
    val bossesDefeated: Int = 0,
    val isSuccess: Boolean = false,
    val baseCoins: Int = coinsCollected,
    val missionBonusCoins: Int = 0,
    val completedMissions: List<MissionProgress> = emptyList(),
    val expEarned: Int = 0,
    val explorationExp: Int = 0,
    val missionExp: Int = 0,
    val missionBonusExp: Int = 0,
    val bossExp: Int = 0,
    val stationExp: Int = 0,
    val repeatMultiplier: Float = 1.0f
)
