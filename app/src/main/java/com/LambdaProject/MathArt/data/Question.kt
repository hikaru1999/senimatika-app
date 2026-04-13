package com.LambdaProject.MathArt.data

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswer: Int
)
