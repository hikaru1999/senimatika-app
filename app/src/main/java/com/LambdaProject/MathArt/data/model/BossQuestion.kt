package com.LambdaProject.MathArt.data.model

data class BossQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val answerKey: List<String> = emptyList(),
    val questionType: String = "",
    val timer: Int = 0
)