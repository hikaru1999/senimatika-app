package com.LambdaProject.MathArt.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class OnlineQuizData (
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val questions: List<OnlineQuizQuestion> = emptyList(),
    val totalPoints: Int = 0
)

@Keep
data class OnlineQuizDesc (
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val questions: List<QuizQuestionRef> = emptyList(),
    // UI Metadata
    val durationMinutes: Int = 0,
    val rewardCoin: Int = 0,
    val imageRes: Int = 0,
    val imageUrl: String? = null,
    val classLevel: String = "Umum",
    val numberQuestion: Int = 0
)

@Keep
data class QuizQuestionRef(
    val id: String = "",
    val points: Int = 100,
    val timer: Int = 60
)

@Keep
data class OnlineQuizQuestion (
    val questionNumber: Int = 0,
    val questionText: String = "",
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val choices: List<String> = emptyList(),
    val correctAnswers: List<Int> = emptyList(),
    val correctTextAnswers: List<String> = emptyList(),
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val durationSeconds: Int = 60,
    val basePoints: Int = 100
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