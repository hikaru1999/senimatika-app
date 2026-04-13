package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.LambdaProject.MathArt.data.model.UserAnswer
import com.LambdaProject.MathArt.data.model.QuizQuestion
import com.LambdaProject.MathArt.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class QuizViewModel : ViewModel() {
    private val _correctAnswers = mutableListOf<List<String>>()
    val correctAnswers: List<List<String>> get() = _correctAnswers

    private val _questions = listOf(
        QuizQuestion(
            id = "q1",
            imageResId = R.drawable.img_kain_besurek,
            correctAnswers = listOf("Dilatasi", "Rotasi", "Translasi")

        ),
        QuizQuestion(
            id = "q2",
            imageResId = R.drawable.img_batik_tujuh_rupa,
            correctAnswers = listOf("Translasi", "Rotasi")
        ),
        QuizQuestion(
            id = "q3",
            imageResId = R.drawable.img_batik_bunga_melati,
            correctAnswers = listOf("Translasi, Rotasi, Refleksi")
        )
    )
    private val questions: List<QuizQuestion> get() = _questions

    var currentQuestionIndex by mutableIntStateOf(0)
    private var showValidationError by mutableStateOf(false)

    var selectedAnswers by mutableStateOf(listOf<String>())
        private set

    private val _userAnswers = mutableStateListOf<UserAnswer>()
    val userAnswers: List<UserAnswer> get() = _userAnswers

    private val _isQuizReady = mutableStateOf(false)
    val isQuizReady: State<Boolean> get() = _isQuizReady

    val isFinished: Boolean
        get() = currentQuestionIndex >= questions.lastIndex

    val currentQuestion: QuizQuestion
        get() = questions[currentQuestionIndex]

    val totalQuestions: Int
        get() = questions.size

    fun initializeQuiz(userId: String, materialId: String, onReady: () -> Unit) {
        _userAnswers.clear()
        selectedAnswers = emptyList()
        currentQuestionIndex = 0

        getSessionStatus(userId, materialId) { status ->
            if (status == "active") {
                currentQuestionIndex = 0
                onReady()
            } else {
                loadQuizResults(userId, materialId) {
                    val lastIndex = _userAnswers.size
                    if (lastIndex < questions.size) {
                        currentQuestionIndex = lastIndex
                    } else {
                        onReady()
                    }
                }
            }
        }
    }

    fun toggleAnswer(answer: String) {
        selectedAnswers = if (selectedAnswers.contains(answer)) {
            selectedAnswers - answer
        } else {
            selectedAnswers + answer
        }
    }

    fun submitAnswer(userId: String?, materialId: String) {
        if (currentQuestionIndex >= questions.size) {
            Log.d("QuizViewModel", "currentQuestionIndex: $currentQuestionIndex, jumlah soal: ${questions.size}")
            return
        }

        if (selectedAnswers.isEmpty()) {
            showValidationError = true
            return
        }

        val current = questions[currentQuestionIndex]
        val correct = current.correctAnswers
        val isCorrect = selectedAnswers.toSet() == correct.toSet()

        _userAnswers.add(
            UserAnswer(
                questionId = current.id,
                selectedAnswers = selectedAnswers,
                correctAnswers = correct,
                isCorrect = isCorrect
            )
        )

        selectedAnswers = emptyList()

        Log.d("QuizViewModel", "Jawaban soal ${currentQuestionIndex + 1} disimpan")

        if (currentQuestionIndex < questions.lastIndex) {
            currentQuestionIndex++
        } else {
            if (userId != null) {
                Log.d("QuizViewModel", "Menyimpan hasil kuis ke Firestore untuk userId: $userId, materialId: $materialId")
                saveQuizResultsToFirestore(userId, materialId)
            }
        }
    }

    private fun saveQuizResultsToFirestore(userId: String, materialId: String) {
        val db = Firebase.firestore

        val data = _userAnswers.map {
            hashMapOf(
                "questionId" to it.questionId,
                "selectedAnswers" to it.selectedAnswers,
                "correctAnswers" to it.correctAnswers,
                "isCorrect" to it.isCorrect
            )
        }

        val resultDoc = hashMapOf(
            "userId" to userId,
            "materialId" to materialId,
            "results" to data,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("quizResults")
            .add(resultDoc)
            .addOnSuccessListener {
                Log.d("QuizViewModel", "Hasil kuis berhasil disimpan ke Firestore")
            }
            .addOnFailureListener {
                Log.e("QuizViewModel", "Gagal menyimpan hasil kuis: ${it.message}")
            }
    }

    fun loadCorrectAnswers() {
        questions.forEach { question ->
            _correctAnswers.add(question.correctAnswers)
        }
    }

    fun loadQuizResults(userId: String, materialId: String, onLoaded: () -> Unit) {
        val db = Firebase.firestore

        db.collection("quizResults")
            .whereEqualTo("userId", userId)
            .whereEqualTo("materialId", materialId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val document = result.documents.firstOrNull()
                val results = document?.get("results") as? List<Map<String, *>>

                if (results != null) {
                    _userAnswers.clear()
                    Log.d("QuizViewModel", "Memuat ${results.size} hasil kuis")
                    results.forEach { item ->
                        val questionId = item["questionId"] as? String ?: return@forEach
                        val selected = item["selectedAnswers"] as? List<String> ?: emptyList()
                        val correct = item["correctAnswers"] as? List<String> ?: emptyList()
                        val isCorrect = item["isCorrect"] as? Boolean ?: false

                        _userAnswers.add(
                            UserAnswer(
                                questionId = questionId,
                                selectedAnswers = selected,
                                correctAnswers = correct,
                                isCorrect = isCorrect
                            )
                        )
                    }
                    Log.d("QuizViewModel", "Hasil kuis berhasil dimuat: ${_userAnswers.size} soal")
                    onLoaded()
                }
            }
            .addOnFailureListener {
                Log.e("QuizViewModel", "Gagal memuat hasil kuis: ${it.message}")
            }
    }

    fun prepareQuiz(userId: String, materialId: String) {
        clearPreviousQuizResults(userId, materialId) {
            resetQuiz()
            _isQuizReady.value = true
        }
    }

    fun resetQuizReadyState() {
        _isQuizReady.value = false
    }

    fun resetQuiz() {
        _userAnswers.clear()
        selectedAnswers = emptyList()
        currentQuestionIndex = 0
    }

    private fun getSessionStatus(userId: String, materialId: String, onComplete: (String) -> Unit) {
        val db = Firebase.firestore
        db.collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("materialId", materialId)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val document = result.documents.firstOrNull()
                val status = document?.getString("status") ?: "inactive"
                onComplete(status)
            }
    }

    private fun clearPreviousQuizResults(userId: String, materialId: String, onComplete: () -> Unit = {}) {
        val db = Firebase.firestore
        db.collection("quizResults")
            .whereEqualTo("userId", userId)
            .whereEqualTo("materialId", materialId)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.delete(document.reference)
                }
                batch.commit().addOnSuccessListener {
                    onComplete()
                }
            }
    }
}
