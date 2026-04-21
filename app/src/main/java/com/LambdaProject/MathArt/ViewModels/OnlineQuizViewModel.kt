package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.repository.AuthRepository
import com.LambdaProject.MathArt.data.model.Challenge
import com.LambdaProject.MathArt.data.repository.ChallengeRepository
import com.LambdaProject.MathArt.data.repository.QuizResultRepository
import com.LambdaProject.MathArt.data.model.OnlineUser
import com.LambdaProject.MathArt.data.DataStates
import com.LambdaProject.MathArt.data.model.LeaderboardEntry
import com.LambdaProject.MathArt.data.model.OnlineQuizDesc
import com.LambdaProject.MathArt.data.model.OnlineQuizQuestion
import com.LambdaProject.MathArt.data.model.QuestionType
import com.LambdaProject.MathArt.data.model.QuizResult
import com.LambdaProject.MathArt.data.model.ScoreType
import com.LambdaProject.MathArt.data.model.ScorestreakState
import com.LambdaProject.MathArt.data.model.UserAnswerState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class OnlineQuizViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val challengeRepo: ChallengeRepository,
    private val repository: QuizResultRepository
): ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _materials = MutableStateFlow<List<OnlineQuizDesc>>(emptyList())
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

    private var challengeListener: ListenerRegistration? = null

    init {
        loadAllQuizzes()
    }

    private fun loadAllQuizzes() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("online_quizzes").get(Source.DEFAULT).await()
                /* val snapshot = firestore.collection("online_quizzes").get().await() */
                val quizList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(OnlineQuizDesc::class.java)?.let { quiz ->
                        // Fallback image for Firestore-loaded quizzes that don't have local resource IDs
                        val fixedQuiz = if (quiz.imageRes == 0) {
                            quiz.copy(imageRes = R.drawable.img_geo)
                        } else {
                            quiz
                        }
                        fixedQuiz.copy(id = doc.id)
                    }
                }
                _materials.value = quizList
            } catch (e: Exception) {
                Log.e("OnlineQuizViewModel", "Error loading quizzes: ${e.message}")
            }
        }
    }

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

    fun resetChallengeStatus() {
        _challengeStatus.value = null
    }

    fun startListeningForChallenges() {
        // challengeRepo.listenForIncomingChallenges harus mengembalikan ListenerRegistration
        challengeListener?.remove()
        challengeListener = challengeRepo.listenForIncomingChallenges { challenges ->
            _incomingChallenges.value = challenges
        }
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

    fun loadQuizForSelectedMaterial(userId: String, materialId: String) {
        viewModelScope.launch {
            try {
                val quizDoc = firestore.collection("online_quizzes").document(materialId).get(Source.DEFAULT).await()
                val quizData = quizDoc.toObject(OnlineQuizDesc::class.java) ?: return@launch
                
                val questionRefs = quizData.questions
                if (questionRefs.isEmpty()) {
                    Log.d("OnlineQuizViewModel", "No questions found for kuis: $materialId")
                    return@launch
                }

                val questionIds = questionRefs.map { it.id }

                val questionsSnapshot = firestore.collection("questions")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), questionIds)
                    .get(Source.DEFAULT) // Utamakan cache jika user pernah main sebelumnya
                    .await()

                val questionMap = questionsSnapshot.documents.associateBy { it.id }

                val fullQuestions = questionRefs.mapIndexedNotNull { index, ref ->
                    val qDoc = questionMap[ref.id] ?: return@mapIndexedNotNull null

                    val choices = qDoc.get("options") as? List<String> ?: emptyList()
                    val correctAnswers = (qDoc.get("answerKey") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                    val qTypeStr = qDoc.getString("questionType") ?: "multiple_choice"

                    val type = when(qTypeStr.lowercase()) {
                        "checkbox" -> QuestionType.CHECKBOX
                        "short_answer" -> QuestionType.SHORT_ANSWER
                        else -> QuestionType.MULTIPLE_CHOICE
                    }

                    OnlineQuizQuestion(
                        questionNumber = index + 1,
                        questionText = qDoc.getString("question") ?: "",
                        imageUrl = qDoc.getString("imageUrl"),
                        choices = choices,
                        correctAnswers = correctAnswers,
                        correctTextAnswers = if (type == QuestionType.SHORT_ANSWER) qDoc.get("answerKey") as? List<String> ?: emptyList() else emptyList(),
                        type = type,
                        durationSeconds = ref.timer,
                        basePoints = ref.points
                    )
                }

                /* val fullQuestions = mutableListOf<OnlineQuizQuestion>()
                
                questionRefs.forEachIndexed { index, ref ->
                    val qDoc = firestore.collection("questions").document(ref.id).get().await()
                    if (qDoc.exists()) {
                        val choices = qDoc.get("options") as? List<String> ?: emptyList()
                        val correctAnswers = (qDoc.get("answerKey") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                        val qTypeStr = qDoc.getString("questionType") ?: "multiple_choice"
                        
                        val type = when(qTypeStr.lowercase()) {
                            "checkbox" -> QuestionType.CHECKBOX
                            "short_answer" -> QuestionType.SHORT_ANSWER
                            else -> QuestionType.MULTIPLE_CHOICE
                        }

                        fullQuestions.add(OnlineQuizQuestion(
                            questionNumber = index + 1,
                            questionText = qDoc.getString("question") ?: "",
                            imageUrl = qDoc.getString("imageUrl"),
                            choices = choices,
                            correctAnswers = correctAnswers,
                            correctTextAnswers = if (type == QuestionType.SHORT_ANSWER) qDoc.get("answerKey") as? List<String> ?: emptyList() else emptyList(),
                            type = type,
                            durationSeconds = ref.timer,
                            basePoints = ref.points
                        ))
                    }
                } */

                _questions.value = fullQuestions
                _currentQuestionIndex.value = 0
            } catch (e: Exception) {
                Log.e("OnlineQuizViewModel", "Error loading quiz questions: ${e.message}")
            }
        }
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

        if (alreadyAnswered) return

        val timeTaken = currentQuestion.durationSeconds - timeLeft
        val isTimeout = timeLeft == 0 && selectedAnswers.isEmpty()

        val isCorrect = when (currentQuestion.type) {
            QuestionType.MULTIPLE_CHOICE -> {
                !isTimeout && currentQuestion.correctAnswers.toList() == selectedAnswers
            }

            QuestionType.CHECKBOX -> {
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
            DataStates.find { it.type == scoreType }?.let { state ->
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
                _rewardCoins.value = quiz?.rewardCoin ?: 0
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
            _leaderboard.value = emptyList()
            repository.getLeaderBoardForMaterial(materialId)
                .onSuccess { results ->
                    Log.d("Leaderboard", "Leaderboard fetched successfully: ${results.size} entries")

                    val userIds = results.map { it.userId }.distinct()
                    val usernameMap = repository.getUsernamesForUserIds(userIds)

                    val leaderboardEntries = results.map { result ->
                        /* val username = usernameMap[result.userId] ?: (result.userId.take(6) + "...") */
                        val username = usernameMap[result.userId] ?: "Guest_${result.userId.take(4)}"

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

    override fun onCleared() {
        super.onCleared()
        challengeListener?.remove()
        challengeListener = null
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }
}
