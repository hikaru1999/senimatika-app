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
import com.LambdaProject.MathArt.data.model.QuizQuestionRef
import com.LambdaProject.MathArt.data.model.QuestionType
import com.LambdaProject.MathArt.data.model.BotDuelSummary
import com.LambdaProject.MathArt.data.model.BotOpponent
import com.LambdaProject.MathArt.data.model.QuizResult
import com.LambdaProject.MathArt.data.model.ScoreType
import com.LambdaProject.MathArt.data.model.ScorestreakState
import com.LambdaProject.MathArt.data.model.UserAnswerState
import com.LambdaProject.MathArt.data.model.unlockGeneralAchievement
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

    private val _selectedBot = MutableStateFlow<BotOpponent?>(null)
    val selectedBot: StateFlow<BotOpponent?> = _selectedBot

    fun setSelectedBot(bot: BotOpponent?) {
        _selectedBot.value = bot
    }

    private val _userName = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "")
    val userName: StateFlow<String> = _userName

    private val _userAvatar = MutableStateFlow(com.LambdaProject.MathArt.utils.AvatarHelper.DEFAULT_AVATAR)
    val userAvatar: StateFlow<String> = _userAvatar

    private val _activeFrame = MutableStateFlow("")
    val activeFrame: StateFlow<String> = _activeFrame

    fun loadUserAvatar(userId: String) {
        if (userId.isBlank()) return
        firestore.collection("users").document(userId).get(Source.DEFAULT)
            .addOnSuccessListener { doc ->
                _userAvatar.value = doc.getString("avatar") ?: com.LambdaProject.MathArt.utils.AvatarHelper.DEFAULT_AVATAR
                _activeFrame.value = doc.getString("activeFrame") ?: ""
                val name = doc.getString("username") ?: doc.getString("name") ?: FirebaseAuth.getInstance().currentUser?.displayName ?: ""
                if (name.isNotBlank()) {
                    _userName.value = name
                }
            }
    }

    private val _botDuelSummary = MutableStateFlow<BotDuelSummary?>(null)
    val botDuelSummary: StateFlow<BotDuelSummary?> = _botDuelSummary

    fun setBotDuelSummary(summary: BotDuelSummary) {
        _botDuelSummary.value = summary
    }

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

    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    private val _challengeStatus = MutableStateFlow<String?>(null)
    val challengeStatus: StateFlow<String?> = _challengeStatus

    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    val quizResult: StateFlow<QuizResult?> = _quizResult

    private val _quizExpBreakdown = MutableStateFlow<QuizExpBreakdown?>(null)
    val quizExpBreakdown: StateFlow<QuizExpBreakdown?> = _quizExpBreakdown

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

    private val _isQuizNotFound = MutableStateFlow(false)
    val isQuizNotFound: StateFlow<Boolean> = _isQuizNotFound

    private var challengeListener: ListenerRegistration? = null
    private var materialsListener: ListenerRegistration? = null

    init {
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser !== null) {
                loadAllQuizzes()
            } else {
                _materials.value = emptyList()
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener!!)
//        loadAllQuizzes()
    }

    private fun loadAllQuizzes() {
        materialsListener?.remove()

        materialsListener = firestore.collection("online_quizzes")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("OnlineQuizViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val quizList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(OnlineQuizDesc::class.java)?.let { quiz ->
                            val fixedQuiz = if (quiz.imageRes == 0) {
                                quiz.copy(imageRes = R.drawable.img_geo)
                            } else {
                                quiz
                            }
                            val isLearning = doc.getBoolean("isLearningMode") ?: quiz.isLearningMode
                            fixedQuiz.copy(id = doc.id, isLearningMode = isLearning)
                        }
                    }
                    _materials.value = quizList
                    Log.d("OnlineQuizViewModel", "Real-time update: ${quizList.size} quizzes")
                }
            }

//        viewModelScope.launch {
//            try {
//                val snapshot = firestore.collection("online_quizzes")
//                    .whereEqualTo("active", true)
//                    .get(Source.SERVER)
//                    .await()
//
//                val quizList = snapshot.documents.mapNotNull { doc ->
//                    doc.toObject(OnlineQuizDesc::class.java)?.let { quiz ->
//                        val fixedQuiz = if (quiz.imageRes == 0) {
//                            quiz.copy(imageRes = R.drawable.img_geo)
//                        } else {
//                            quiz
//                        }
//                        fixedQuiz.copy(id = doc.id)
//                    }
//                }
//                _materials.value = quizList
//            } catch (e: Exception) {
//                Log.e("OnlineQuizViewModel", "Error loading quizzes: ${e.message}")
//            }
//        }
    }

    fun refreshMaterials() {
        loadAllQuizzes()
    }

    fun clearSelectedMaterial() {
        _selectedMaterial.value = null
    }

    /* fun sendChallenge(toUserId: String) {
        val selected = selectedMaterial.value ?: return

        viewModelScope.launch {
            val success = challengeRepo.sendChallengeRequest(
                toUserId = toUserId,
                materialId = selected.id
            )
            _challengeStatus.value = if (success) "sent" else "failed"
        }
    }

    fun resetChallengeStatus() {
        _challengeStatus.value = null
    } */

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun startListeningForChallenges() {
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

    fun resetQuizState() {
        _userAnswers.clear()
        totalPoints = 0
        totalBasePoints = 0
        currentBasePoints.value = 0
        _currentPoints.value = 0
        totalCoins = 0
        correctStreak = 0
        wrongStreak = 0
        _quizResult.value = null
        _quizExpBreakdown.value = null
        _botDuelSummary.value = null
        _currentQuestionIndex.value = 0
        _scorestreakState.value = null
    }

    fun loadQuizForSelectedMaterial(userId: String, materialId: String) {
        resetQuizState()
        viewModelScope.launch {
            try {
                _isQuizNotFound.value = false

                val quizDoc = firestore.collection("online_quizzes")
                    .document(materialId)
                    .get(Source.SERVER)
                    .await()

                val quizData = quizDoc.toObject(OnlineQuizDesc::class.java)
                val isActivated = quizDoc.getBoolean("active") ?: (quizData?.active ?: true)
                val isRandomized = quizDoc.getBoolean("isRandomized") ?: quizDoc.getBoolean("randomized") ?: (quizData?.isRandomized ?: false)
                val isLearningMode = quizDoc.getBoolean("isLearningMode") ?: (quizData?.isLearningMode ?: false)
                val rawQuestionsList = quizDoc.get("questions") as? List<*>
                val parsedQuestionRefs = rawQuestionsList?.mapNotNull { item ->
                    when (item) {
                        is Map<*, *> -> {
                            val id = item["id"] as? String ?: return@mapNotNull null
                            val points = (item["points"] as? Number)?.toInt() ?: 10
                            val timer = (item["timer"] as? Number)?.toInt() ?: 30
                            val pembahasan = (item["pembahasan"] as? String) ?: (item["explanation"] as? String)
                            QuizQuestionRef(id = id, points = points, timer = timer, pembahasan = pembahasan)
                        }
                        else -> null
                    }
                } ?: emptyList()

                if (!isActivated) {
                    _isQuizNotFound.value = true
                    _questions.value = emptyList()
                    return@launch
                }

                val questionRefs = if (parsedQuestionRefs.isNotEmpty()) parsedQuestionRefs else (quizData?.questions ?: emptyList())
                if (questionRefs.isEmpty()) {
                    _questions.value = emptyList()
                    return@launch
                }

                val questionIds = questionRefs.map { it.id }

                val questionsSnapshot = firestore.collection("questions")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), questionIds)
                    .get(Source.DEFAULT)
                    .await()

                val questionMap = questionsSnapshot.documents.associateBy { it.id }

                val fullQuestions = questionRefs.mapIndexedNotNull { index, ref ->
                    val qDoc = questionMap[ref.id] ?: return@mapIndexedNotNull null
                    val choices = qDoc.get("options") as? List<String> ?: emptyList()
                    val qTypeStr = qDoc.getString("questionType") ?: "multiple_choice"

                    val type = when(qTypeStr.lowercase()) {
                        "checkbox","check_boxes" -> QuestionType.CHECKBOX
                        "short_answer" -> QuestionType.SHORT_ANSWER
                        else -> QuestionType.MULTIPLE_CHOICE
                    }

                    val rawAnswerKey = qDoc.get("answerKey")
                    val correctAnswers = mutableListOf<Int>()
                    val correctTextAnswers = mutableListOf<String>()

                    if (type == QuestionType.SHORT_ANSWER) {
                        (rawAnswerKey as? List<*>)?.forEach { correctTextAnswers.add(it.toString()) }
                    } else {
                        (rawAnswerKey as? List<*>)?.forEach { (it as? Number)?.let { num -> correctAnswers.add(it.toInt()) } }
                    }

                    val qPoints = ref.points.takeIf { it > 0 } ?: (qDoc.getLong("points")?.toInt() ?: 10)
                    val qTimer = ref.timer.takeIf { it > 0 } ?: (qDoc.getLong("timer")?.toInt() ?: 30)
                    val qPembahasan = ref.pembahasan ?: qDoc.getString("pembahasan") ?: qDoc.getString("explanation")

                    OnlineQuizQuestion(
                        questionNumber = index + 1,
                        questionText = qDoc.getString("question") ?: "",
                        imageUrl = qDoc.getString("imageUrl"),
                        choices = choices,
                        correctAnswers = correctAnswers,
                        correctTextAnswers = correctTextAnswers,
                        type = type,
                        durationSeconds = qTimer,
                        basePoints = qPoints,
                        pembahasan = qPembahasan
                    )
                }

                val finalQuestions = if (isRandomized) {
                    fullQuestions.shuffled().mapIndexed { index, q ->
                        q.copy(questionNumber = index + 1)
                    }
                } else {
                    fullQuestions
                }

                _questions.value = finalQuestions
                _selectedMaterial.value = (quizData ?: OnlineQuizDesc()).copy(
                    id = quizDoc.id,
                    isLearningMode = isLearningMode,
                    isRandomized = isRandomized
                )
                _currentQuestionIndex.value = 0
            } catch (e: Exception) {
                _isQuizNotFound.value = true
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

        val isLearningMode = _selectedMaterial.value?.isLearningMode == true
        val basePoint = if (isCorrect) currentQuestion.basePoints else 0
        val duration = currentQuestion.durationSeconds.coerceAtLeast(1)
        val speedRatio = (timeLeft.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        val maxTimeBonus = (currentQuestion.basePoints * 0.5f).toInt().coerceAtLeast(5)
        val timeBonus = if (isCorrect && !isLearningMode) (maxTimeBonus * speedRatio).toInt() else 0

        val streakBonusPercent = when (correctStreak) {
            1 -> 0.10f
            2 -> 0.20f
            in 3..Int.MAX_VALUE -> 0.35f
            else -> 0f
        }

        val streakBonus = if (isCorrect && !isLearningMode) (currentQuestion.basePoints * streakBonusPercent).toInt() else 0
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
        val totalBasePoints = _userAnswers.sumOf { it.basePointsEarned }
        val totalTimeBonus = _userAnswers.sumOf { it.timeBonusEarned }
        val totalStreakBonus = _userAnswers.sumOf { it.streakBonusEarned }
        val calculatedPoints = if (totalPoints > 0) totalPoints else (_currentPoints.value.takeIf { it > 0 } ?: (totalBasePoints + totalTimeBonus + totalStreakBonus))

        val resolvedTitle = _selectedMaterial.value?.title?.takeIf { it.isNotBlank() }
            ?: _materials.value.find { it.id == materialId }?.title
            ?: ""

        val result = QuizResult(
            materialId = materialId,
            quizTitle = resolvedTitle,
            userId = userId,
            totalBasePoints = totalBasePoints,
            totalTimeBonus = totalTimeBonus,
            totalStreakBonus = totalStreakBonus,
            totalPoints = calculatedPoints,
            answers = _userAnswers.map {
                it.copy(
                    selectedAnswers = it.selectedAnswers.toList(),
                    userTextAnswer = it.userTextAnswer)
            }
        )

        _quizResult.value = result

        val totalQuestions = _questions.value.size.coerceAtLeast(1)
        val correctCount = _userAnswers.count { it.isCorrect }
        val accuracy = correctCount.toFloat() / totalQuestions.toFloat()
        val avgSpeedRatio = if (_userAnswers.isNotEmpty()) {
            _userAnswers.map { ans ->
                val q = _questions.value.find { it.questionNumber == ans.questionNumber }
                val dur = (q?.durationSeconds ?: 30).coerceAtLeast(1)
                ((dur - ans.timeTaken).toFloat() / dur.toFloat()).coerceIn(0f, 1f)
            }.average().toFloat()
        } else 0f

        var curStreak = 0
        var maxStreakCalc = 0
        for (ans in _userAnswers) {
            if (ans.isCorrect) {
                curStreak++
                if (curStreak > maxStreakCalc) maxStreakCalc = curStreak
            } else {
                curStreak = 0
            }
        }

        val baseExp = _selectedMaterial.value?.exp ?: 100
        val bonusExp = com.LambdaProject.MathArt.data.model.calculateQuizBonusExp(
            baseExp = baseExp,
            totalQuestions = totalQuestions,
            correctCount = correctCount,
            avgSpeedRatio = avgSpeedRatio,
            maxStreak = maxStreakCalc
        )
        val rawExp = baseExp + bonusExp

        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val quizPlayCounts = (userDoc.get("quizPlayCounts") as? Map<String, Any>)?.toMutableMap() ?: mutableMapOf()
                val currentPlayCount = (quizPlayCounts[materialId] as? Number)?.toInt() ?: 0
                val multiplier = com.LambdaProject.MathArt.data.model.getRepeatPlayMultiplier(currentPlayCount)
                val finalExp = (rawExp * multiplier).toInt()
                val nextPlayCount = currentPlayCount + 1
                quizPlayCounts[materialId] = nextPlayCount
                firestore.collection("users").document(userId).update("quizPlayCounts", quizPlayCounts)

                _quizExpBreakdown.value = QuizExpBreakdown(
                    baseExp = baseExp,
                    bonusExp = bonusExp,
                    totalRawExp = rawExp,
                    repeatMultiplier = multiplier,
                    finalExp = finalExp,
                    accuracy = accuracy,
                    speedRatio = avgSpeedRatio,
                    maxStreak = maxStreakCalc,
                    sessionCount = nextPlayCount
                )

                com.LambdaProject.MathArt.data.model.grantExpToUser(userId, finalExp)
            } catch (e: Exception) {
                Log.e("OnlineQuizViewModel", "Error updating quiz EXP: ${e.message}")
            }

            repository.saveQuizResult(result)
                .onSuccess {
                    checkAndUnlockQuizAchievements(userId)
                }
                .onFailure {
                    Log.e("PvPQuizModel", "Failed to save quiz result: ${it.message}")
                }
        }
    }

    fun fetchQuizResult(userId: String, materialId: String) {
        if (_quizResult.value != null && _quizResult.value?.materialId == materialId) {
            return
        }
        viewModelScope.launch {
            repository.getUserQuizResult(userId, materialId)
                .onSuccess { result ->
                    if (_quizResult.value == null) {
                        _quizResult.value = result
                    }
                }
                .onFailure { e ->
                    if (_quizResult.value == null) {
                        _quizResult.value = null
                    }
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
                    val profileMap = repository.getUserProfilesForUserIds(userIds)

                    val leaderboardEntries = results.map { result ->
                        val profile = profileMap[result.userId]
                        val username = profile?.first ?: "Guest_${result.userId.take(4)}"
                        val avatar = profile?.second ?: com.LambdaProject.MathArt.utils.AvatarHelper.DEFAULT_AVATAR

                        LeaderboardEntry(
                            username = username,
                            points = result.totalPoints,
                            avatar = avatar
                        )
                    }.sortedByDescending { it.points }

                    _leaderboard.value = leaderboardEntries
                }
                .onFailure { e -> Log.e("Leaderboard", "Failed to fetch leaderboard: ${e.message}") }
        }
    }

    private fun checkAndUnlockQuizAchievements(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("online_quiz_results")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val quizCount = snapshot.size()

                if (quizCount >= 1) {
                    unlockGeneralAchievement(userId, "Math Magician")
                }

                if (quizCount >= 5) {
                    unlockGeneralAchievement(userId, "Ahli Matematika")
                }

                if (quizCount >= 10) {
                    unlockGeneralAchievement(userId, "Jenius Matematika")
                }

            } catch (e: Exception) {
                Log.e("Achievement_ERROR", "Gagal menghitung hasil kuis: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        challengeListener?.remove()
        challengeListener = null
        materialsListener?.remove()
        authStateListener?.let {
            FirebaseAuth.getInstance().removeAuthStateListener(it)
        }
    }

    /* fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    } */
}

data class QuizExpBreakdown(
    val baseExp: Int = 100,
    val bonusExp: Int = 0,
    val totalRawExp: Int = 100,
    val repeatMultiplier: Float = 1.0f,
    val finalExp: Int = 100,
    val accuracy: Float = 0f,
    val speedRatio: Float = 0f,
    val maxStreak: Int = 0,
    val sessionCount: Int = 1
)
