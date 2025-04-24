package com.LambdaProject.MathArt.Data

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