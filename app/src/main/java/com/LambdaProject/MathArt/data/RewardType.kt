package com.LambdaProject.MathArt.data

enum class RewardType {
    COIN,
    SCROLL,
    POWER_UP
}

enum class PowerUpType {
    FREEZE_TIMER,
    DOUBLE_COIN,
    REMOVE_TWO_OPTIONS
}

data class Reward(
    val type: RewardType,
    val amount: Int = 0,
    val powerUp: PowerUpType? = null,
    val content: String = ""
)