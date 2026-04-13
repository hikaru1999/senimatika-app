package com.LambdaProject.MathArt.data

data class Inventory(
    val coins: Int = 0,
    val scrolls: List<String> = emptyList(),
    val powerUps: List<PowerUpType> = emptyList()
)