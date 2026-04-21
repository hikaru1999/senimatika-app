package com.LambdaProject.MathArt.data

enum class RewardType {
    COIN,
    SCROLL,
    POWER_UP
}

enum class PowerUpType {
    FREEZE_TIMER,
    STREAK_PROTECTION,
    REMOVE_TWO_OPTIONS
}

data class Reward(
    val type: RewardType,
    val amount: Int = 0,
    val powerUp: PowerUpType? = null,
    val content: String = "",
    val title: String = "",
    val id: String = ""
)