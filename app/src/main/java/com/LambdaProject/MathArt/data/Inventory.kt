package com.LambdaProject.MathArt.data

data class Inventory(
    val coins: Int = 0,
    /* val scrolls: List<String> = emptyList(), */
    val scrolls: List<Reward> = emptyList(),
    val powerUps: List<PowerUpType> = emptyList()
)

val MAX_BAG_WEIGHT = 20.0f
val ADD_STRAP = 5.0f

fun getPowerUpWeight(type: PowerUpType): Float {
    return when (type) {
        PowerUpType.STREAK_PROTECTION -> 2.5f
        PowerUpType.MAGIC_KEY -> 2.0f
        PowerUpType.FREEZE_TIMER -> 1.5f
        PowerUpType.REMOVE_TWO_OPTIONS -> 1.0f
        PowerUpType.HEALING_VIAL -> 0.5f
        PowerUpType.LEATHER_STRAPS -> 0.0f
        PowerUpType.BINOCULAR -> 2.0f
        PowerUpType.TORCH -> 1.5f
        PowerUpType.LANTERN -> 0.75f
    }
}

fun getCooldownDuration(type: PowerUpType): Long {
    return when (type) {
        PowerUpType.REMOVE_TWO_OPTIONS -> 45000L
        PowerUpType.FREEZE_TIMER -> 60000L
        PowerUpType.STREAK_PROTECTION -> 90000L
        PowerUpType.HEALING_VIAL -> 45000L
        PowerUpType.LEATHER_STRAPS -> 0L
        PowerUpType.MAGIC_KEY -> 0L
        PowerUpType.TORCH -> 30000L
        PowerUpType.LANTERN -> 0L
        PowerUpType.BINOCULAR -> 30000L
    }
}

val SCROLL_WEIGHT = 0.2f

fun Inventory.calculateTotalWeight(): Float {
    val puWeight = powerUps.sumOf { getPowerUpWeight(it).toDouble() }.toFloat()
    val scrollWeight = scrolls.size * SCROLL_WEIGHT
    return puWeight + scrollWeight
}

fun Inventory.isEmpty(): Boolean {
    return coins <= 0 && powerUps.isEmpty() && scrolls.isEmpty()
}