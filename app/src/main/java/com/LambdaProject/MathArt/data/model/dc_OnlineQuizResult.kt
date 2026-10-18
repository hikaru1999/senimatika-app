package com.LambdaProject.MathArt.data.model

data class QuizResult(
    val materialId: String = "",
    val quizTitle: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val totalBasePoints: Int = 0,
    val totalTimeBonus: Int = 0,
    val totalStreakBonus: Int = 0,
    val totalPoints: Int = 0,
    val answers: List<UserAnswerState> = emptyList()
)

data class RecentQuizActivity(
    val id: String = "",
    val title: String = "",
    val points: Int = 0,
    val category: String = "Math Arena"
) {
    val first: String get() = id
    val second: Int get() = points
}