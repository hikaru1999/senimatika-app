package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.model.BossQuestion
import com.google.firebase.firestore.FieldPath.documentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class BossBattlePhase {
    INTRO, COUNTDOWN, QUIZ, WAITING, SUMMARY
}

data class QuestionResult(
    val isCorrect: Boolean,
    val playerDamageDealt: Float,
    val playerDamageTaken: Float,
    val wasFast: Boolean,
    val streak: Int = 0
)

class BossQuizViewModel : ViewModel() {
    var isQuizOpen by mutableStateOf(false)
    var phase by mutableStateOf(BossBattlePhase.INTRO)
    var countdownValue by mutableIntStateOf(3)
    
    private var allQuestions = mutableListOf<BossQuestion>()
    var currentQuestionIndex by mutableIntStateOf(0)
    var currentQuestion by mutableStateOf<BossQuestion?>(null)
    val selectedAnswers = mutableStateListOf<String>()
    val powerUpCooldowns = mutableStateMapOf<PowerUpType, Long>()
    
    var totalQuestions by mutableIntStateOf(0)
    
    var playerHp by mutableFloatStateOf(100f)
    var bossHp by mutableFloatStateOf(100f)
    var bossThinkingProgress by mutableFloatStateOf(0f)
    var bossTimeLeftMillis by mutableLongStateOf(0L)

    var bossStatus by mutableStateOf("Boss sedang berpikir...")
    var currentBossType by mutableStateOf("boss_1")
    val questionResults = mutableStateListOf<QuestionResult>()
    
    private var bossDurationMillis = 15000L
    private var elapsedTimeMillis = 0L
    private var isBossStunned = false
    private var isBossHaste = false
    
    var showPlayerImpact by mutableStateOf(false)
    var showBossImpact by mutableStateOf(false)
    var bossShake by mutableStateOf(false)

    val removedOptions = mutableStateListOf<String>()
    var isTimerPaused by mutableStateOf(false)
    var isChronoFreezeActive by mutableStateOf(false)

    // Streak Logic
    var currentStreak by mutableIntStateOf(0)
    var isStreakProtected by mutableStateOf(false)
    
    private var bossThinkingJob: Job? = null
    private var onDefeated: (() -> Unit)? = null

    fun startQuiz(quizId: String, bossType: String, onBossDefeated: () -> Unit) {
        this.currentBossType = bossType
        onDefeated = onBossDefeated
        isQuizOpen = true
        phase = BossBattlePhase.INTRO
        resetBattleState()
        
        viewModelScope.launch {
            delay(2500)
            phase = BossBattlePhase.COUNTDOWN
            for (i in 3 downTo 1) {
                countdownValue = i
                delay(1000)
            }
            loadQuestions(quizId)
        }
    }

    private fun loadQuestions(quizId: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val fetchedQuestions = mutableListOf<BossQuestion>()

                if (quizId.isEmpty()) {
                    val snapshot = db.collection("questions").limit(5).get().await()
                    fetchedQuestions.addAll(parseQuestionsFromSnapshot(snapshot))
                } else {
                    val quizDoc = db.collection("boss_quizzes").document(quizId).get().await()
                    if (quizDoc.exists()) {
                        val questionsArray = quizDoc.get("questions") as? List<Map<String, Any>>
                        if (questionsArray != null) {
                            val questionIds = questionsArray.mapNotNull { it["id"] as? String }

                            val questionsSnapshot = db.collection("questions")
                                .whereIn(documentId(), questionIds)
                                .get(Source.DEFAULT)
                                .await()

                            val questionMap = questionsSnapshot.documents.associateBy { it.id }

                            for (item in questionsArray) {
                                val qId = item["id"] as? String ?: continue
                                val qDoc = questionMap[qId] ?: continue

                                val options = qDoc.get("options") as? List<String> ?: continue
                                fetchedQuestions.add(
                                    BossQuestion(
                                        id = qDoc.id,
                                        question = qDoc.getString("question") ?: "",
                                        options = options,
                                        answerKey = parseAnswerKey(qDoc.get("answerKey"), options),
                                        questionType = qDoc.getString("questionType") ?: "multiple_choice",
                                        timer = (item["timer"] as? Number)?.toInt() ?: 0
                                    )
                                )
                            }
                        }
                    }
                    /* if (quizDoc.exists()) {
                        val questionsArray = quizDoc.get("questions") as? List<Map<String, Any>>
                        if (questionsArray != null) {
                            for (item in questionsArray) {
                                val qId = item["id"] as? String ?: continue
                                val qTimer = (item["timer"] as? Number)?.toInt() ?: 0
                                
                                val qDoc = db.collection("questions").document(qId).get().await()
                                if (qDoc.exists()) {
                                    val options = qDoc.get("options") as? List<String> ?: continue
                                    val rawAnswerKey = qDoc.get("answerKey")
                                    fetchedQuestions.add(
                                        BossQuestion(
                                            id = qDoc.id,
                                            question = qDoc.getString("question") ?: "",
                                            options = options,
                                            answerKey = parseAnswerKey(rawAnswerKey, options),
                                            questionType = qDoc.getString("questionType") ?: "multiple_choice",
                                            timer = qTimer
                                        )
                                    )
                                }
                            }
                        }
                    } */
                }

                if (fetchedQuestions.isEmpty()) {
                    loadFallbackQuestions()
                } else {
                    allQuestions = fetchedQuestions
                    allQuestions.shuffle()
                    totalQuestions = allQuestions.size
                    bossHp = (totalQuestions * 20f).coerceAtLeast(20f)
                    showNextQuestion()
                }
            } catch (e: Exception) {
                Log.e("BossQuizViewModel", "Error loading questions", e)
                loadFallbackQuestions()
            }
        }
    }

    private fun getCooldownDuration(type: PowerUpType): Long {
        return when (type) {
            PowerUpType.REMOVE_TWO_OPTIONS -> 45000L // 45 Detik (Bermanfaat)
            PowerUpType.FREEZE_TIMER -> 60000L       // 60 Detik (Sangat Bermanfaat)
            PowerUpType.STREAK_PROTECTION -> 90000L   // 90 Detik (Paling Worth It)
        }
    }

    private fun reduceAllCooldowns(percent: Float) {
        val now = System.currentTimeMillis()
        powerUpCooldowns.forEach { (type, readyTime) ->
            val remaining = readyTime - now
            if (remaining > 0) {
                val reduction = (remaining * percent).toLong()
                powerUpCooldowns[type] = readyTime - reduction
            }
        }
    }

    private fun parseQuestionsFromSnapshot(snapshot: com.google.firebase.firestore.QuerySnapshot): List<BossQuestion> {
        return snapshot.documents.mapNotNull { doc ->
            val options = doc.get("options") as? List<String> ?: return@mapNotNull null
            val rawAnswerKey = doc.get("answerKey")
            BossQuestion(
                id = doc.id,
                question = doc.getString("question") ?: "",
                options = options,
                answerKey = parseAnswerKey(rawAnswerKey, options),
                questionType = doc.getString("questionType") ?: "multiple_choice",
                timer = doc.getLong("timer")?.toInt() ?: 0
            )
        }
    }

    private fun loadFallbackQuestions() {
        allQuestions = mutableListOf(
            BossQuestion(
                id = "fallback_1",
                question = "Apa hasil dari refleksi titik (2,3) terhadap sumbu X?",
                options = listOf("(2, -3)", "(-2, 3)", "(2, 3)", "(-2, -3)"),
                answerKey = listOf("(2, -3)"),
                questionType = "multiple_choice",
                timer = 15
            )
        )
        totalQuestions = allQuestions.size
        bossHp = (totalQuestions * 20f).coerceAtLeast(20f)
        showNextQuestion()
    }

    private fun parseAnswerKey(raw: Any?, options: List<String>): List<String> {
        return when (raw) {
            is List<*> -> raw.mapNotNull {
                val index = (it as? Number)?.toInt() ?: -1
                options.getOrNull(index) ?: (it as? String)
            }
            else -> emptyList()
        }
    }

    private fun showNextQuestion() {
        if (currentQuestionIndex < allQuestions.size && playerHp > 0 && bossHp > 0) {
            currentQuestion = allQuestions[currentQuestionIndex]
            selectedAnswers.clear()
            removedOptions.clear()
            isTimerPaused = false
            isChronoFreezeActive = false
            phase = BossBattlePhase.QUIZ
            initializeBossTimer()
            startBossThinking()
        } else {
            phase = BossBattlePhase.SUMMARY
        }
    }

    private fun initializeBossTimer() {
        val firestoreTimer = currentQuestion?.timer ?: 0
        var duration = if (firestoreTimer > 0) firestoreTimer * 1000L else 12000L

        if (isBossStunned) {
            duration += 5000 
            isBossStunned = false
        }
        if (isBossHaste) {
            duration = (duration * 0.7f).toLong()
            isBossHaste = false
        }
        if (playerHp < 30f) {
            duration = (duration * 1.3f).toLong()
        }

        bossDurationMillis = duration
        bossThinkingProgress = 0f
        elapsedTimeMillis = 0L
        bossTimeLeftMillis = duration
        bossStatus = "Boss sedang berpikir..."
    }

    private fun startBossThinking() {
        bossThinkingJob?.cancel()
        bossThinkingJob = viewModelScope.launch {
            while (elapsedTimeMillis < bossDurationMillis) {
                delay(100)
                if (!isTimerPaused) {
                    elapsedTimeMillis += 100
                    bossThinkingProgress = (elapsedTimeMillis.toFloat() / bossDurationMillis).coerceIn(0f, 1f)
                    bossTimeLeftMillis = (bossDurationMillis - elapsedTimeMillis).coerceAtLeast(0L)
                    if (bossThinkingProgress >= 1f) bossStatus = "Boss siap menyerang!"
                }
            }
            if (phase == BossBattlePhase.QUIZ) handleTimeOut()
        }
    }

    fun submitAnswer() {
        if (phase != BossBattlePhase.QUIZ) return
        bossThinkingJob?.cancel()
        
        val question = currentQuestion ?: return
        val isCorrect = selectedAnswers.toSet() == question.answerKey.toSet()
        
        val playerVeryFast = bossThinkingProgress < 0.5f
        val playerLate = bossThinkingProgress >= 1.0f

        var playerDamageTaken = 0f
        var bossDamageTaken = 0f
        
        if (isCorrect) {
            currentStreak++

            if (currentStreak > 0 && currentStreak % 3 == 0) {
                reduceAllCooldowns(0.25f)
                Log.d("Battle", "Streak Bonus! Cooldowns reduced by 25%")
            }

            if (playerVeryFast) {
                bossDamageTaken = 30f + (currentStreak * 2f)
                isBossStunned = true 
            } else if (!playerLate) {
                // Base 20 + Streak Bonus
                bossDamageTaken = 20f + currentStreak.toFloat()
            } else {
                bossDamageTaken = 20f 
                playerDamageTaken = (5..10).random().toFloat() 
            }

            isStreakProtected = false

        } else {
            if (isStreakProtected) {
                isStreakProtected = false
                playerDamageTaken = 0f
            } else {
                currentStreak = 0
                playerDamageTaken = 20f
            }
            bossDamageTaken = 0f
            isBossHaste = true
        }

        applyImpact(bossDamageTaken, playerDamageTaken, isCorrect, !playerLate)
    }

    private fun handleTimeOut() {
        if (phase != BossBattlePhase.QUIZ) return

        var playerDamageTaken = 20f

        if (isStreakProtected) {
            isStreakProtected = false
            playerDamageTaken = 0f

        } else {
            currentStreak = 0
        }
        applyImpact(0f, playerDamageTaken, false, false)
        isBossHaste = true
    }

    private fun applyImpact(bossDamage: Float, playerDamage: Float, isCorrect: Boolean, isFast: Boolean) {
        viewModelScope.launch {
            if (isCorrect) {
                showPlayerImpact = true
                bossShake = true
                delay(200)
                bossHp = (bossHp - bossDamage).coerceAtLeast(0f)
                delay(800)
                showPlayerImpact = false
                bossShake = false
            } else {
                showBossImpact = true
                delay(200)
                playerHp = (playerHp - playerDamage).coerceAtLeast(0f)
                delay(800)
                showBossImpact = false
            }

            questionResults.add(QuestionResult(isCorrect, bossDamage, playerDamage, isFast, currentStreak))
            currentQuestionIndex++
            showNextQuestion()
        }
    }

    fun usePowerUp(
        type: PowerUpType,
        inventory: Inventory,
        onUpdateInventory: (Inventory) -> Unit
    ) {
        if (!inventory.powerUps.contains(type)) return

        val now = System.currentTimeMillis()
        val readyTime = powerUpCooldowns[type] ?: 0L
        if (now < readyTime) {
            Log.d("Battle", "Item $type masih dalam cooldown!")
            return
        }

        val newPowerUps = inventory.powerUps.toMutableList()
        newPowerUps.remove(type)
        onUpdateInventory(inventory.copy(powerUps = newPowerUps))

        when (type) {
            PowerUpType.FREEZE_TIMER -> {
                viewModelScope.launch {
                    isTimerPaused = true
                    isChronoFreezeActive = true
                    delay(5000)
                    isTimerPaused = false
                    isChronoFreezeActive = false
                }
            }
            PowerUpType.REMOVE_TWO_OPTIONS -> {
                val question = currentQuestion ?: return
                val incorrectOptions = question.options.filter {
                    !question.answerKey.contains(it)
                }
                removedOptions.addAll(incorrectOptions.shuffled().take(2))
            }
            PowerUpType.STREAK_PROTECTION -> {
                isStreakProtected = true
            }
        }
        powerUpCooldowns[type] = now + getCooldownDuration(type)
    }

    fun toggleAnswer(answer: String) {
        if (phase != BossBattlePhase.QUIZ) return
        if (currentQuestion?.questionType == "multiple_choice") {
            selectedAnswers.clear()
            selectedAnswers.add(answer)
        } else {
            if (selectedAnswers.contains(answer)) selectedAnswers.remove(answer) else selectedAnswers.add(answer)
        }
    }

    private fun resetBattleState() {
        playerHp = 100f
        bossHp = 100f
        currentQuestionIndex = 0
        totalQuestions = 0
        questionResults.clear()
        bossThinkingProgress = 0f
        bossTimeLeftMillis = 0L
        isBossStunned = false
        isBossHaste = false
        showPlayerImpact = false
        showBossImpact = false
        isTimerPaused = false
        isChronoFreezeActive = false
        currentStreak = 0
        isStreakProtected = false
    }

    fun closeQuiz() {
        isQuizOpen = false
        bossThinkingJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        bossThinkingJob?.cancel()
    }
}
