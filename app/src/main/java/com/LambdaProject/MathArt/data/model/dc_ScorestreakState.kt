package com.LambdaProject.MathArt.data.model

data class ScorestreakState(
    val type: ScoreType,
    val imageRes: Int,
    val title: String,
    val subtitle: String
)

enum class ScoreType {
    GOOD, COOL, AWESOME, UPS, NOT_FOCUSED, TIME_OUT
}