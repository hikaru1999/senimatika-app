package com.LambdaProject.MathArt.data

data class Question(
    val text: String,
    val options: List<String> = emptyList(),
    val answerKey: List<String> = emptyList(),
    val correctAnswer: Int = 0,
    val questionType: String = "multiple_choice",
)
