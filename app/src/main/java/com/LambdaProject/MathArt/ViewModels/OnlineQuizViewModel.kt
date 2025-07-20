package com.LambdaProject.MathArt.ViewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.Data.AuthRepo
import com.LambdaProject.MathArt.model.Challenge
import com.LambdaProject.MathArt.Data.ChallengeRepo
import com.LambdaProject.MathArt.Data.QuizResultRepository
import com.LambdaProject.MathArt.Data.ScoreSoundManager
import com.LambdaProject.MathArt.model.OnlineUser
import com.LambdaProject.MathArt.Data.sampleOnlineQuiz
import com.LambdaProject.MathArt.Data.sampleStates
import com.LambdaProject.MathArt.Data.transform_geo
import com.LambdaProject.MathArt.model.LeaderboardEntry
import com.LambdaProject.MathArt.model.OnlineQuizDesc
import com.LambdaProject.MathArt.model.OnlineQuizQuestion
import com.LambdaProject.MathArt.model.QuestionType
import com.LambdaProject.MathArt.model.QuizResult
import com.LambdaProject.MathArt.model.ScoreType
import com.LambdaProject.MathArt.model.ScorestreakState
import com.LambdaProject.MathArt.model.UserAnswerState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineQuizViewModel @Inject constructor(
    private val authRepo: AuthRepo,
    private val challengeRepo: ChallengeRepo,
    private val repository: QuizResultRepository
): ViewModel() {

    private val _materials = MutableStateFlow(sampleOnlineQuiz)
    val materials: StateFlow<List<OnlineQuizDesc>> = _materials

    private val _scorestreakState = MutableStateFlow<ScorestreakState?>(null)
    val scorestreakState: StateFlow<ScorestreakState?> = _scorestreakState

    private val _selectedMaterial = MutableStateFlow<OnlineQuizDesc?>(null)
    val selectedMaterial: StateFlow<OnlineQuizDesc?> = _selectedMaterial

    private val _userOnlineList = MutableStateFlow<List<OnlineUser>>(emptyList())
    val userOnlineList: StateFlow<List<OnlineUser>> = _userOnlineList

    private var totalPoints by mutableStateOf(0)
        private set

    private var totalBasePoints = 0
    val currentBasePoints = MutableStateFlow(0)

    private val _currentPoints = MutableStateFlow(0)
    val currentPoints: StateFlow<Int> = _currentPoints

    private var totalCoins by mutableStateOf(0)
        private set

    private var correctStreak = 0
    private var wrongStreak = 0

    private val _challengeStatus = MutableStateFlow<String?>(null)
    val challengeStatus: StateFlow<String?> = _challengeStatus

    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    val quizResult: StateFlow<QuizResult?> = _quizResult

    private val _incomingChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val incomingChallenges: StateFlow<List<Challenge>> = _incomingChallenges

    private val _questions = MutableStateFlow<List<OnlineQuizQuestion>>(emptyList())
    val questions: StateFlow<List<OnlineQuizQuestion>> = _questions

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _userAnswers = mutableStateListOf<UserAnswerState>()
    val userAnswers: List<UserAnswerState> = _userAnswers

    private val _rewardStatus = MutableLiveData<String>()
    val rewardStatus: LiveData<String> = _rewardStatus

    private val _rewardCoins = MutableLiveData<Int>()
    val rewardCoins: LiveData<Int> = _rewardCoins

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard

    fun sendChallenge(toUserId: String) {
        val selected = selectedMaterial.value ?: return

        viewModelScope.launch {
            val success = challengeRepo.sendChallengeRequest(
                toUserId = toUserId,
                materialId = selected.id
            )
            _challengeStatus.value = if (success) "sent" else "failed"
        }
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun startListeningForChallenges() {
        challengeRepo.listenForIncomingChallenges { challenges ->
            _incomingChallenges.value = challenges
        }
    }

    fun resetChallengeStatus() {
        _challengeStatus.value = null
    }

    fun selectMaterial(material: OnlineQuizDesc) {
        _selectedMaterial.value = material
    }

    fun loadUsersOnline() {
        viewModelScope.launch {
            val users = authRepo.getUsersOnline()
            _userOnlineList.value = users
        }
    }

    fun loadQuizForSelectedMaterial(UserId: String, materialId: String) {
        Log.d("OnlineQuizViewModel","Loading quiz for: $UserId")
        Log.d("OnlineQuizViewModel","Loading quiz for: $materialId")

        val quiz = transform_geo[materialId] ?: run {
            Log.d("OnlineQuizViewModel","Soal tidak ditemukan untuk id: $materialId") // debug
            return
        }

        /* val shuffledQuiz = quiz.shuffled().mapIndexed { index, question ->
            question.copy(questionNumber = index + 1) // supaya nomor soal tetap urut dari 1
        } */

        _questions.value = quiz
        _currentQuestionIndex.value = 0
        Log.d("OnlineQuizViewModel", "Soal berhasil dimuat: ${_questions.value.size} soal")
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.lastIndex) {
            _currentQuestionIndex.value += 1
        }
    }

    fun checkAnswer(
        selectedAnswers: List<Int>,
        userTextAnswer: String? = null,
        timeLeft: Int,
        onScoreUpdated: (pointsEarned: Int, coinsEarned: Int) -> Unit = { _, _ -> },
        onStreakUpdate: (ScorestreakState) -> Unit = {}
    ) {
        val currentQuestion = _questions.value[_currentQuestionIndex.value]

        val alreadyAnswered = _userAnswers.any { it.questionNumber == currentQuestion.questionNumber }

        /* val alreadyAnswered = _userAnswers.indices.contains(_currentQuestionIndex.value) */

        if (alreadyAnswered) return

        val timeTaken = currentQuestion.durationSeconds - timeLeft
        val isTimeout = timeLeft == 0 && selectedAnswers.isEmpty()
        /* val isCorrect = !isTimeout && currentQuestion.correctAnswers.toList() == selectedAnswers */

        val isCorrect = when (currentQuestion.type) {
            QuestionType.MULTIPLE_CHOICE -> {
                !isTimeout && currentQuestion.correctAnswers.toList() == selectedAnswers
            }

            QuestionType.CHECKBOX -> {
                /* !isTimeout && currentQuestion.correctAnswers.toList() == selectedAnswers */
                !isTimeout && currentQuestion.correctAnswers.toSet() == selectedAnswers.toSet()
            }

            QuestionType.SHORT_ANSWER -> {
                !isTimeout && userTextAnswer != null && currentQuestion.correctTextAnswers.any {
                    it.trim().equals(userTextAnswer.trim(), ignoreCase = true)
                }
            }
        }

        val basePoint = if (isCorrect) currentQuestion.basePoints else 0
        val timeBonus = if (isCorrect) (timeLeft.coerceAtMost(25) * 2) else 0

        val streakBonusPercent = when (correctStreak) {
            1 -> 0.10f
            2 -> 0.20f
            in 3..Int.MAX_VALUE -> 0.35f
            else -> 0f
        }

        val streakBonus = if (isCorrect) (currentQuestion.basePoints * streakBonusPercent).toInt() else 0
        val pointsEarned = basePoint + timeBonus + streakBonus
        val coinsEarned = if (isCorrect) 5 else 0

        val userAnswer = UserAnswerState(
            questionNumber = currentQuestion.questionNumber,
            selectedAnswers = selectedAnswers,
            userTextAnswer = userTextAnswer,
            isCorrect = isCorrect,
            timeTaken = timeTaken,
            basePointsEarned = basePoint,
            timeBonusEarned = timeBonus,
            streakBonusEarned = streakBonus
        )

        _userAnswers.add(userAnswer)
        totalPoints += pointsEarned
        _currentPoints.value = totalPoints
        totalCoins += coinsEarned

        if (isCorrect) {
            totalBasePoints += basePoint
            currentBasePoints.value = totalBasePoints
            correctStreak++
            wrongStreak = 0
        } else {
            wrongStreak++
            correctStreak = 0
        }

        val scoreType = when {
            isTimeout -> ScoreType.TIME_OUT
            correctStreak >= 3 -> ScoreType.AWESOME
            correctStreak == 2 -> ScoreType.COOL
            correctStreak == 1 -> ScoreType.GOOD
            wrongStreak >= 2 -> ScoreType.NOT_FOCUSED
            wrongStreak == 1 -> ScoreType.UPS
            else -> null
        }

        scoreType?.let {
            sampleStates.find { it.type == scoreType }?.let { state ->
                _scorestreakState.value = state
                onStreakUpdate(state)
            }
        }
        onScoreUpdated(pointsEarned, coinsEarned)
    }

    fun saveQuizResult(userId: String, materialId: String) {
        Log.d("PvPQuizModel", "Invoked saveQuizResult() with userId=$userId, materialId=$materialId")
        val totalBasePoints = _userAnswers.sumOf { it.basePointsEarned }
        val totalTimeBonus = _userAnswers.sumOf { it.timeBonusEarned }
        val totalStreakBonus = _userAnswers.sumOf { it.streakBonusEarned }

        val result = QuizResult(
            materialId = materialId,
            userId = userId,
            totalBasePoints = totalBasePoints,
            totalTimeBonus = totalTimeBonus,
            totalStreakBonus = totalStreakBonus,
            totalPoints = totalPoints,
            answers = _userAnswers.map {
                it.copy(
                    selectedAnswers = it.selectedAnswers.toList(),
                    userTextAnswer = it.userTextAnswer)
            }
        )

        viewModelScope.launch {
            repository.saveQuizResult(result)
                .onSuccess {
                    Log.d("PvPQuizModel", "Quiz result saved.")
                }
                .onFailure {
                    Log.e("PvPQuizModel", "Failed to save quiz result: ${it.message}")
                }
        }
    }

    fun fetchQuizResult(userId: String, materialId: String) {
        viewModelScope.launch {
            repository.getUserQuizResult(userId, materialId)
                .onSuccess { result ->
                    Log.d("PvPQuizModel", "Fetched quiz result: $result")
                    _quizResult.value = result
                }
                .onFailure { e ->
                    Log.e("PvPQuizModel", "Failed to fetch quiz result: ${e.message}")
                    _quizResult.value = null
                }
        }
    }

    fun rewardIfFirstTime(userId: String, materialId: String, quizList: List<OnlineQuizDesc>) {
        viewModelScope.launch {
            val isGranted = repository.rewardIfFirstTime(userId, materialId, quizList)
            if (isGranted) {
                _rewardStatus.value = "Reward Diberikan"
                val quiz = quizList.find { it.id == materialId }
                _rewardCoins.value = quiz?.coints ?: 0
            } else {
                _rewardStatus.value = ""
                _rewardCoins.value = 0
            }
        }
    }

    fun fetchLeaderboardForMaterial(
        materialId: String
    ) {
        viewModelScope.launch {
            Log.d("Leaderboard", "Fetching leaderboard for material: $materialId")
            repository.getLeaderBoardForMaterial(materialId)
                .onSuccess { results ->
                    Log.d("Leaderboard", "Leaderboard fetched successfully: ${results.size} entries")

                    val userIds = results.map { it.userId }.distinct()
                    val usernameMap = repository.getUsernamesForUserIds(userIds)

                    /* val userMap = onlineUsers.associateBy { it.uid } */
                    val leaderboardEntries = results.map { result ->
                        /* val username = userMap[result.userId]?.username ?: (result.userId.take(6) + "...") */
                        val username = usernameMap[result.userId] ?: (result.userId.take(6) + "...")

                        LeaderboardEntry(
                            username = username,
                            points = result.totalPoints
                        )
                    }.sortedByDescending { it.points }

                    _leaderboard.value = leaderboardEntries
                }
                .onFailure { e -> Log.e("Leaderboard", "Failed to fetch leaderboard: ${e.message}") }
        }
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }
}