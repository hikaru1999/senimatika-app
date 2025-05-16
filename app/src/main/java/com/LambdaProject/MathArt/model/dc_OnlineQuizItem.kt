package com.LambdaProject.MathArt.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

data class OnlineQuizDesc (
    val id: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val coints: Int,
    val imageRes: Int,
    val classLevel: String,
    val numberQuestion: Int
)

data class OnlineQuizQuestion (
    val questionNumber: Int,
    val questionText: String,
    val imageRes: Int? = null,
    val choices: List<String> = emptyList(),
    val correctAnswers: List<Int> = emptyList(),
    val correctTextAnswers: List<String> = emptyList(),
    val type: QuestionType,
    val durationSeconds: Int,
    val basePoints: Int
)

@Keep
data class UserAnswerState(
    val questionNumber: Int = 0,
    val selectedAnswers: List<Int> = emptyList(),
    val userTextAnswer: String? = null,

    @get:PropertyName("correct")
    @set:PropertyName("correct")
    var isCorrect: Boolean = false,

    val timeTaken: Int = 0,
    val basePointsEarned: Int = 0,
    val timeBonusEarned: Int = 0,
    val streakBonusEarned: Int = 0
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    CHECKBOX,
    SHORT_ANSWER
}