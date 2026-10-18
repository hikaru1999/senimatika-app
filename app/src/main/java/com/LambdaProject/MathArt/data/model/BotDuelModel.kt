package com.LambdaProject.MathArt.data.model

import androidx.annotation.DrawableRes
import com.LambdaProject.MathArt.R
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class BotDifficulty(
    val title: String,
    val minAccuracy: Float,
    val maxAccuracy: Float,
    val minThinkTimeSec: Int,
    val maxThinkTimeSec: Int,
    val streakRetention: Float
) {
    EASY("Pemula", 0.45f, 0.65f, 6, 14, 0.3f),
    MEDIUM("Amatir", 0.65f, 0.85f, 4, 9, 0.6f),
    HARD("Master", 0.85f, 0.95f, 2, 6, 0.85f)
}

data class BotOpponent(
    val id: String,
    val name: String,
    val title: String,
    @DrawableRes val avatarRes: Int,
    val difficulty: BotDifficulty,
    val frameId: String? = null,
    var currentScore: Int = 0,
    var currentStreak: Int = 0,
    var lastAnswerCorrect: Boolean? = null,
    var isThinking: Boolean = false
) {
    companion object {
        val BOT_PRESETS = listOf(
            BotOpponent(
                id = "bot_1",
                name = "Hakimi",
                title = "Master Aljabar",
                avatarRes = R.drawable.avatar_14,
                difficulty = BotDifficulty.HARD,
                frameId = "frame_3"
            ),
            BotOpponent(
                id = "bot_2",
                name = "Pythagoras",
                title = "Penakluk Segitiga",
                avatarRes = R.drawable.avatar_15,
                difficulty = BotDifficulty.MEDIUM,
                frameId = "frame_1"
            ),
            BotOpponent(
                id = "bot_3",
                name = "Ada Lovelace",
                title = "Logika & Algoritma",
                avatarRes = R.drawable.avatar_13,
                difficulty = BotDifficulty.HARD,
                frameId = "frame_4"
            ),
            BotOpponent(
                id = "bot_4",
                name = "RoboMath AI",
                title = "Kalkulator Cepat",
                avatarRes = R.drawable.avatar_22,
                difficulty = BotDifficulty.MEDIUM,
                frameId = "frame_2"
            ),
            BotOpponent(
                id = "bot_5",
                name = "Sobat Belajar",
                title = "Pemula Antusias",
                avatarRes = R.drawable.avatar_23,
                difficulty = BotDifficulty.EASY,
                frameId = null
            ),
            BotOpponent(
                id = "bot_6",
                name = "Euler Master",
                title = "Grandmaster Analisis",
                avatarRes = R.drawable.avatar_17,
                difficulty = BotDifficulty.HARD,
                frameId = "frame_3"
            ),
            BotOpponent(
                id = "bot_7",
                name = "Gauss Cilik",
                title = "Pangeran Aritmatika",
                avatarRes = R.drawable.avatar_3,
                difficulty = BotDifficulty.HARD,
                frameId = "frame_4"
            ),
            BotOpponent(
                id = "bot_8",
                name = "Bintang Math",
                title = "Bintang Olimpiade",
                avatarRes = R.drawable.avatar_29,
                difficulty = BotDifficulty.MEDIUM,
                frameId = "frame_2"
            ),
            BotOpponent(
                id = "bot_9",
                name = "Arya Matika",
                title = "Penjelajah Rumus",
                avatarRes = R.drawable.avatar_31,
                difficulty = BotDifficulty.EASY,
                frameId = "frame_1"
            ),
            BotOpponent(
                id = "bot_10",
                name = "Euclid Junior",
                title = "Arsitek Geometri",
                avatarRes = R.drawable.avatar_9,
                difficulty = BotDifficulty.MEDIUM,
                frameId = "frame_2"
            ),
            BotOpponent(
                id = "bot_11",
                name = "Rina Solver",
                title = "Pemburu Soal Cepat",
                avatarRes = R.drawable.avatar_32,
                difficulty = BotDifficulty.MEDIUM,
                frameId = "frame_1"
            ),
            BotOpponent(
                id = "bot_12",
                name = "Byte Hunter",
                title = "Pencari Pola Logika",
                avatarRes = R.drawable.avatar_19,
                difficulty = BotDifficulty.EASY,
                frameId = null
            ),
            BotOpponent(
                id = "bot_13",
                name = "Bayu Logika",
                title = "Strategis Aritmatika",
                avatarRes = R.drawable.avatar_21,
                difficulty = BotDifficulty.MEDIUM,
                frameId = "frame_2"
            ),
            BotOpponent(
                id = "bot_14",
                name = "Archimedes",
                title = "Penemu Solusi",
                avatarRes = R.drawable.avatar_6,
                difficulty = BotDifficulty.HARD,
                frameId = "frame_3"
            ),
            BotOpponent(
                id = "bot_15",
                name = "Luna Math",
                title = "Pembelajar Hebat",
                avatarRes = R.drawable.avatar_27,
                difficulty = BotDifficulty.EASY,
                frameId = null
            ),
            BotOpponent(
                id = "bot_16",
                name = "Fermat Hunter",
                title = "Penantang Teorema",
                avatarRes = R.drawable.avatar_24,
                difficulty = BotDifficulty.HARD,
                frameId = "frame_4"
            )
        )

        fun getRandomBot(difficulty: BotDifficulty? = null): BotOpponent {
            val pool = if (difficulty != null) {
                BOT_PRESETS.filter { it.difficulty == difficulty }
            } else {
                BOT_PRESETS
            }
            val base = pool.randomOrNull() ?: BOT_PRESETS.random()
            return base.copy(
                currentScore = 0,
                currentStreak = 0,
                lastAnswerCorrect = null,
                isThinking = false
            )
        }

        fun findById(idOrName: String?): BotOpponent {
            if (idOrName.isNullOrBlank()) return getRandomBot()
            val base = BOT_PRESETS.find { it.id.equals(idOrName, ignoreCase = true) || it.name.equals(idOrName, ignoreCase = true) }
                ?: getRandomBot()
            return base.copy(
                currentScore = 0,
                currentStreak = 0,
                lastAnswerCorrect = null,
                isThinking = false
            )
        }
    }
}

data class BotQuestionResult(
    val isCorrect: Boolean,
    val basePoints: Int,
    val timeBonus: Int,
    val streakBonus: Int
) {
    val totalBonus: Int get() = timeBonus + streakBonus
    val totalPoints: Int get() = if (isCorrect) basePoints + totalBonus else 0
}

data class BotDuelSummary(
    val bot: BotOpponent,
    val questionResults: List<BotQuestionResult> = emptyList(),
    val isSurrendered: Boolean = false
) {
    val totalCorrect: Int get() = questionResults.count { it.isCorrect }
    val totalBasePoints: Int get() = questionResults.filter { it.isCorrect }.sumOf { it.basePoints }
    val totalTimeBonus: Int get() = questionResults.filter { it.isCorrect }.sumOf { it.timeBonus }
    val totalStreakBonus: Int get() = questionResults.filter { it.isCorrect }.sumOf { it.streakBonus }
    val totalBonusPoints: Int get() = totalTimeBonus + totalStreakBonus
    val totalScore: Int get() = totalBasePoints + totalBonusPoints
}

class BotDuelEngine(
    val bot: BotOpponent
) {
    private val simulatedResults = mutableMapOf<Int, BotQuestionResult>()

    init {
        bot.currentScore = 0
        bot.currentStreak = 0
        bot.lastAnswerCorrect = null
        bot.isThinking = false
    }

    fun recordBotAnswer(
        questionIndex: Int,
        question: OnlineQuizQuestion,
        botTimeLeft: Int
    ): BotQuestionResult {
        return simulatedResults.getOrPut(questionIndex) {
            val minAcc = bot.difficulty.minAccuracy
            val maxAcc = bot.difficulty.maxAccuracy
            val streakBonusProbability = (bot.currentStreak * 0.02f).coerceAtMost(0.08f)
            val accuracyThreshold = (Random.nextFloat() * (maxAcc - minAcc) + minAcc + streakBonusProbability).coerceAtMost(maxAcc)
            val roll = Random.nextFloat()
            val isCorrect = roll <= accuracyThreshold

            val qResult: BotQuestionResult
            if (isCorrect) {
                val basePoint = question.basePoints
                val duration = question.durationSeconds.coerceAtLeast(1)
                val speedRatio = (botTimeLeft.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                val maxTimeBonus = (question.basePoints * 0.5f).toInt().coerceAtLeast(5)
                val timeBonus = (maxTimeBonus * speedRatio).toInt()
                val streakMultiplier = when (bot.currentStreak) {
                    1 -> 0.10f
                    2 -> 0.20f
                    in 3..Int.MAX_VALUE -> 0.35f
                    else -> 0f
                }
                val streakBonus = (basePoint * streakMultiplier).toInt()
                val pointsEarned = basePoint + timeBonus + streakBonus

                bot.currentStreak++
                bot.currentScore += pointsEarned
                qResult = BotQuestionResult(
                    isCorrect = true,
                    basePoints = basePoint,
                    timeBonus = timeBonus,
                    streakBonus = streakBonus
                )
            } else {
                bot.currentStreak = 0
                qResult = BotQuestionResult(
                    isCorrect = false,
                    basePoints = 0,
                    timeBonus = 0,
                    streakBonus = 0
                )
            }
            bot.lastAnswerCorrect = isCorrect
            qResult
        }
    }

    fun getOrSimulateCurrentResult(
        questionIndex: Int,
        question: OnlineQuizQuestion
    ): Boolean {
        val existing = simulatedResults[questionIndex]
        if (existing != null) {
            bot.lastAnswerCorrect = existing.isCorrect
            return existing.isCorrect
        }

        val minTime = bot.difficulty.minThinkTimeSec.coerceAtMost(question.durationSeconds)
        val maxTime = bot.difficulty.maxThinkTimeSec.coerceAtMost(question.durationSeconds)
        val thinkTime = if (maxTime > minTime) {
            Random.nextInt(minTime, maxTime + 1)
        } else {
            minTime
        }
        val timeLeft = (question.durationSeconds - thinkTime).coerceAtLeast(0)
        val result = recordBotAnswer(questionIndex, question, timeLeft)
        return result.isCorrect
    }

    fun getSummary(questions: List<OnlineQuizQuestion>, isSurrendered: Boolean = false): BotDuelSummary {
        val results = questions.indices.map { index ->
            val q = questions[index]
            simulatedResults[index] ?: run {
                if (isSurrendered) {
                    // Fair simulation for unplayed rounds on surrender
                    val isCorr = Random.nextFloat() <= bot.difficulty.maxAccuracy
                    val base = if (isCorr) q.basePoints else 0
                    val time = if (isCorr) (q.basePoints * 0.2f).toInt() else 0
                    BotQuestionResult(
                        isCorrect = isCorr,
                        basePoints = base,
                        timeBonus = time,
                        streakBonus = 0
                    )
                } else {
                    BotQuestionResult(
                        isCorrect = false,
                        basePoints = 0,
                        timeBonus = 0,
                        streakBonus = 0
                    )
                }
            }
        }
        return BotDuelSummary(
            bot = bot,
            questionResults = results,
            isSurrendered = isSurrendered
        )
    }

    fun getSummary(totalQuestions: Int, isSurrendered: Boolean = false): BotDuelSummary {
        val results = (0 until totalQuestions).map { index ->
            simulatedResults[index] ?: run {
                if (isSurrendered) {
                    val isCorr = Random.nextFloat() <= bot.difficulty.maxAccuracy
                    BotQuestionResult(
                        isCorrect = isCorr,
                        basePoints = if (isCorr) 100 else 0,
                        timeBonus = if (isCorr) 15 else 0,
                        streakBonus = 0
                    )
                } else {
                    BotQuestionResult(
                        isCorrect = false,
                        basePoints = 0,
                        timeBonus = 0,
                        streakBonus = 0
                    )
                }
            }
        }
        return BotDuelSummary(
            bot = bot,
            questionResults = results,
            isSurrendered = isSurrendered
        )
    }
}
