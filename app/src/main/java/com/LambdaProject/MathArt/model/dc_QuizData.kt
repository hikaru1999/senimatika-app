package com.LambdaProject.MathArt.model

data class QuizQuestion(
    val id: String,
    val imageResId: Int,
    val correctAnswers: List<String>
)

data class UserAnswer(
    val questionId: String,
    val selectedAnswers: List<String>,
    val correctAnswers: List<String>,
    val isCorrect: Boolean
)

data class Challenge(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val materialId: String = "",
    val status: String = "pending",
    val timestamp: Long = 0L
)