package com.LambdaProject.MathArt.data.model

import com.LambdaProject.MathArt.data.PowerUpType

data class ExplorationStats(
    val coinsCollected: Int = 0,
    val scrollsCollected: List<String> = emptyList(),
    val powerUpsCollected: List<PowerUpType> = emptyList(),
    val bossesDefeated: Int = 0,
    val isSuccess: Boolean = false
)
