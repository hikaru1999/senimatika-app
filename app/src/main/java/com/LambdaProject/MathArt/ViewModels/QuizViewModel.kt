package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.model.UserAnswer
import com.LambdaProject.MathArt.data.model.QuizQuestionItem
import com.LambdaProject.MathArt.data.model.MaterialDocument
import com.LambdaProject.MathArt.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {
    private val defaultQuestions = listOf(
        QuizQuestionItem(
            id = "q1",
            imageUrl = "https://i.ibb.co/fYWyyccR/img-kain-besurek.jpg",
            instruction = "Pilih jenis transformasi yang terjadi pada motif batik tersebut!",
            options = listOf("Translasi", "Refleksi", "Rotasi", "Dilatasi"),
            correctAnswers = listOf("Dilatasi", "Rotasi", "Translasi"),
            questionType = "complex",
            type = "complex",
            imageResId = R.drawable.img_kain_besurek
        ),
        QuizQuestionItem(
            id = "q2",
            imageUrl = "https://i.ibb.co/q3xLxsFt/img-batik-tujuh-rupa.jpg",
            instruction = "Pilih jenis transformasi yang terjadi pada motif batik tersebut!",
            options = listOf("Translasi", "Refleksi", "Rotasi", "Dilatasi"),
            correctAnswers = listOf("Translasi", "Rotasi"),
            questionType = "complex",
            type = "complex",
            imageResId = R.drawable.img_batik_tujuh_rupa
        ),
        QuizQuestionItem(
            id = "q3",
            imageUrl = "https://i.ibb.co/ycvjK0Cd/img-batik-bunga-melati.jpg",
            instruction = "Pilih jenis transformasi yang terjadi pada motif batik tersebut!",
            options = listOf("Translasi", "Refleksi", "Rotasi", "Dilatasi"),
            correctAnswers = listOf("Translasi", "Refleksi", "Rotasi"),
            questionType = "complex",
            type = "complex",
            imageResId = R.drawable.img_batik_bunga_melati
        )
    )

    private val _questions = mutableStateListOf<QuizQuestionItem>().apply {
        addAll(defaultQuestions)
    }
    val questions: List<QuizQuestionItem> get() = _questions

    private val _correctAnswers = mutableListOf<List<String>>()
    val correctAnswers: List<List<String>> get() = _correctAnswers

    var currentQuestionIndex by mutableIntStateOf(0)
    var isAnswerChecked by mutableStateOf(false)
    var lastAnswerIsCorrect by mutableStateOf(false)

    var selectedAnswers by mutableStateOf(listOf<String>())
        private set

    private val _userAnswers = mutableStateListOf<UserAnswer>()
    val userAnswers: List<UserAnswer> get() = _userAnswers

    private val _isQuizReady = mutableStateOf(false)
    val isQuizReady: State<Boolean> get() = _isQuizReady

    val isFinished: Boolean
        get() = currentQuestionIndex >= questions.lastIndex

    val currentQuestion: QuizQuestionItem
        get() = if (questions.isNotEmpty() && currentQuestionIndex in questions.indices) {
            questions[currentQuestionIndex]
        } else {
            defaultQuestions[0]
        }

    val totalQuestions: Int
        get() = questions.size

    fun loadQuizFromDocument(materialDoc: MaterialDocument) {
        val serverQuestions = materialDoc.quiz?.questions
        if (!serverQuestions.isNullOrEmpty()) {
            _questions.clear()
            _questions.addAll(serverQuestions)
            _correctAnswers.clear()
            serverQuestions.forEach { _correctAnswers.add(it.correctAnswers) }
        }
    }

    fun toggleAnswer(answer: String) {
        if (isAnswerChecked) return

        val q = currentQuestion
        val isSingle = q.questionType.equals("single", ignoreCase = true) || q.type.equals("single", ignoreCase = true)

        selectedAnswers = if (isSingle) {
            listOf(answer)
        } else {
            if (selectedAnswers.contains(answer)) {
                selectedAnswers - answer
            } else {
                selectedAnswers + answer
            }
        }
    }

    /**
     * Memeriksa jawaban soal yang aktif saat ini secara instan (seperti OnlineQuizMaster).
     * Menghitung kebenaran jawaban dan menyimpan hasil ke _userAnswers.
     */
    fun checkCurrentAnswer(): Boolean {
        if (selectedAnswers.isEmpty() || currentQuestionIndex >= questions.size || isAnswerChecked) {
            return false
        }

        val current = currentQuestion
        val userSet = selectedAnswers.map { it.trim().lowercase() }.toSet()
        val correctSet = current.correctAnswers.map { it.trim().lowercase() }.toSet()
        val isCorrect = userSet == correctSet

        lastAnswerIsCorrect = isCorrect
        isAnswerChecked = true

        val existingIdx = _userAnswers.indexOfFirst { it.questionId == current.id }
        val userAnswer = UserAnswer(
            questionId = current.id,
            selectedAnswers = selectedAnswers.toList(),
            correctAnswers = current.correctAnswers,
            isCorrect = isCorrect
        )

        if (existingIdx >= 0) {
            _userAnswers[existingIdx] = userAnswer
        } else {
            _userAnswers.add(userAnswer)
        }

        Log.d("QuizViewModel", "Jawaban diperiksa: isCorrect=$isCorrect, selected=$selectedAnswers, correct=${current.correctAnswers}")
        return isCorrect
    }

    /**
     * Melangkah ke soal berikutnya setelah jawaban diperiksa.
     */
    fun nextQuestion() {
        if (currentQuestionIndex < questions.lastIndex) {
            currentQuestionIndex++
            selectedAnswers = emptyList()
            isAnswerChecked = false
            lastAnswerIsCorrect = false
        }
    }

    /**
     * Menyelesaikan kuis: menyimpan hasil ke Firestore dan membuka achievement.
     */
    fun finishQuiz(userId: String?, materialId: String, onFinished: () -> Unit = {}) {
        if (userId != null && userId.isNotBlank()) {
            saveQuizResultsToFirestore(userId, materialId)
            com.LambdaProject.MathArt.data.model.unlockProAchievement(userId)
        }
        onFinished()
    }

    // Untuk kompatibilitas kode lama jika dipanggil
    fun submitAnswer(userId: String?, materialId: String) {
        if (!isAnswerChecked) {
            checkCurrentAnswer()
        }
        if (currentQuestionIndex < questions.lastIndex) {
            nextQuestion()
        } else {
            finishQuiz(userId, materialId)
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

        val totalCount = _questions.size.coerceAtLeast(_userAnswers.size)
        val correctCount = _userAnswers.count { it.isCorrect }
        val score = if (totalCount > 0) (correctCount * 100) / totalCount else 0

        val resultDoc = hashMapOf(
            "userId" to userId,
            "materialId" to materialId,
            "score" to score,
            "accuracy" to score,
            "correctCount" to correctCount,
            "totalQuestions" to totalCount,
            "isPassed" to (score >= 60),
            "results" to data,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("quizResults")
            .add(resultDoc)
            .addOnSuccessListener {
                Log.d("QuizViewModel", "Hasil kuis berhasil disimpan ke Firestore (skor: $score)")
            }
            .addOnFailureListener {
                Log.e("QuizViewModel", "Gagal menyimpan hasil kuis: ${it.message}")
            }

        // Tandai sesi selesai di Firestore secara otomatis
        com.LambdaProject.MathArt.data.repository.SessionRepository.markSessionCompleted(userId, materialId)
    }

    fun loadCorrectAnswers() {
        _correctAnswers.clear()
        questions.forEach { question ->
            _correctAnswers.add(question.correctAnswers)
        }
    }

    fun loadQuizResults(userId: String, materialId: String, onLoaded: () -> Unit) {
        val db = Firebase.firestore

        // Query tanpa orderBy agar tidak membutuhkan pembuatan composite index di Firebase Console
        db.collection("quizResults")
            .whereEqualTo("userId", userId)
            .whereEqualTo("materialId", materialId)
            .get()
            .addOnSuccessListener { result ->
                val document = result.documents.maxByOrNull { doc ->
                    doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                }
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
                }
                onLoaded()
            }
            .addOnFailureListener {
                Log.e("QuizViewModel", "Gagal memuat hasil kuis: ${it.message}")
                onLoaded()
            }
    }

    fun prepareQuiz(userId: String, materialId: String, onReady: () -> Unit = {}) {
        viewModelScope.launch {
            _isQuizReady.value = false

            // Fetch dynamic quiz from MaterialRepository cache or Firestore
            try {
                com.LambdaProject.MathArt.data.repository.MaterialRepository.getMaterial(
                    materialId = materialId,
                    onSuccess = { materialDoc ->
                        loadQuizFromDocument(materialDoc)
                        getSessionStatus(userId, materialId) { status ->
                            if (status == "active") {
                                currentQuestionIndex = 0
                                selectedAnswers = emptyList()
                                isAnswerChecked = false
                                _isQuizReady.value = true
                                onReady()
                            } else {
                                loadQuizResults(userId, materialId) {
                                    val lastIndex = _userAnswers.size
                                    currentQuestionIndex = if (lastIndex < questions.size) lastIndex else 0
                                    selectedAnswers = emptyList()
                                    isAnswerChecked = false
                                    _isQuizReady.value = true
                                    onReady()
                                }
                            }
                        }
                    },
                    onError = {
                        _isQuizReady.value = true
                        onReady()
                    }
                )
            } catch (_: Exception) {
                _isQuizReady.value = true
                onReady()
            }
        }
    }

    fun resetQuizReadyState() {
        _isQuizReady.value = false
    }

    fun resetQuiz() {
        _userAnswers.clear()
        selectedAnswers = emptyList()
        currentQuestionIndex = 0
        isAnswerChecked = false
        lastAnswerIsCorrect = false
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
            .addOnFailureListener {
                onComplete("inactive")
            }
    }

    fun clearPreviousQuizResults(userId: String, materialId: String, onComplete: () -> Unit = {}) {
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
