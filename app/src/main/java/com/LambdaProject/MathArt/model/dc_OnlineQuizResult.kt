package com.LambdaProject.MathArt.model

data class QuizResult(
    val materialId: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val totalBasePoints: Int = 0,
    val totalTimeBonus: Int = 0,
    val totalStreakBonus: Int = 0,
    val totalPoints: Int = 0,
    val answers: List<UserAnswerState> = emptyList()
)