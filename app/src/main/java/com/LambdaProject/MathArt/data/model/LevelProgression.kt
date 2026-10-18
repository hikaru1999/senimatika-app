package com.LambdaProject.MathArt.data.model

import android.util.Log
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.PowerUpType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Representasi Definisi Level & Hadiah di Senimatika
 */
data class LevelInfo(
    val level: Int,
    val title: String,
    val requiredExp: Long,
    val rewardDescription: String,
    val rewardType: LevelRewardType,
    val rewardPowerUps: List<Pair<PowerUpType, Int>> = emptyList(),
    val emblemName: String? = null,
    val frameId: String? = null,
    val frameName: String? = null,
    val unlocksExploration: Boolean = false
)

enum class LevelRewardType {
    TITLE,
    EMBLEM,
    BADGE,
    POWER_UP,
    EXPLORATION_UNLOCK,
    FRAME,
    LEGENDARY
}

/**
 * 15 Level Progression di Senimatika berdasarkan Saldo EXP Kumulatif
 */
val SENIMATIKA_LEVELS = listOf(
    LevelInfo(
        level = 1,
        title = "Novice",
        requiredExp = 0L,
        rewardDescription = "Tag Awal \"Novice\"",
        rewardType = LevelRewardType.TITLE,
        emblemName = null
    ),
    LevelInfo(
        level = 2,
        title = "Apprentice",
        requiredExp = 300L,
        rewardDescription = "Emblem \"Book of Beginnings\" & Mode Eksplorasi Terbuka",
        rewardType = LevelRewardType.EXPLORATION_UNLOCK,
        emblemName = "Book of Beginnings",
        unlocksExploration = true
    ),
    LevelInfo(
        level = 3,
        title = "Learner",
        requiredExp = 700L,
        rewardDescription = "Emblem \"Curious Eye\" & Bingkai \"Bronze Rune\"",
        rewardType = LevelRewardType.FRAME,
        emblemName = "Curious Eye",
        frameId = "frame_1",
        frameName = "Bronze Rune"
    ),
    LevelInfo(
        level = 4,
        title = "Explorer",
        requiredExp = 1200L,
        rewardDescription = "Power-up Chrono Freeze (2x) & Tag Explorer",
        rewardType = LevelRewardType.POWER_UP,
        rewardPowerUps = listOf(PowerUpType.FREEZE_TIMER to 2)
    ),
    LevelInfo(
        level = 5,
        title = "Thinker",
        requiredExp = 1800L,
        rewardDescription = "Emblem \"Crystal of Insight\" & Tag Thinker",
        rewardType = LevelRewardType.EMBLEM,
        emblemName = "Crystal of Insight"
    ),
    LevelInfo(
        level = 6,
        title = "Solver",
        requiredExp = 2500L,
        rewardDescription = "Emblem \"Breaker of Barriers\" & Bingkai \"Celestial Ring\"",
        rewardType = LevelRewardType.FRAME,
        emblemName = "Breaker of Barriers",
        frameId = "frame_2",
        frameName = "Celestial Ring"
    ),
    LevelInfo(
        level = 7,
        title = "Strategist",
        requiredExp = 3400L,
        rewardDescription = "Power-up Torch (1x) & Tag Strategist",
        rewardType = LevelRewardType.POWER_UP,
        rewardPowerUps = listOf(PowerUpType.TORCH to 1)
    ),
    LevelInfo(
        level = 8,
        title = "Trailblazer",
        requiredExp = 4350L,
        rewardDescription = "Power-up Healing Vial (3x) & Tag Trailblazer",
        rewardType = LevelRewardType.POWER_UP,
        rewardPowerUps = listOf(PowerUpType.HEALING_VIAL to 3)
    ),
    LevelInfo(
        level = 9,
        title = "Mathematician",
        requiredExp = 5500L,
        rewardDescription = "Emblem \"Order of Numbers\" & Tag Mathematician",
        rewardType = LevelRewardType.EMBLEM,
        emblemName = "Order of Numbers"
    ),
    LevelInfo(
        level = 10,
        title = "Expert",
        requiredExp = 6700L,
        rewardDescription = "Magic Key (2x) & Bingkai \"Sun Crown\"",
        rewardType = LevelRewardType.FRAME,
        rewardPowerUps = listOf(PowerUpType.MAGIC_KEY to 2),
        frameId = "frame_3",
        frameName = "Sun Crown"
    ),
    LevelInfo(
        level = 11,
        title = "Virtuoso",
        requiredExp = 8400L,
        rewardDescription = "Emblem \"Seal of Mastery\" & Tag Mewah Virtuoso",
        rewardType = LevelRewardType.EMBLEM,
        emblemName = "Seal of Mastery"
    ),
    LevelInfo(
        level = 12,
        title = "Grandmaster",
        requiredExp = 10200L,
        rewardDescription = "Power-up Battle Shield (3x) & Tag Mewah Grandmaster",
        rewardType = LevelRewardType.POWER_UP,
        rewardPowerUps = listOf(PowerUpType.STREAK_PROTECTION to 3)
    ),
    LevelInfo(
        level = 13,
        title = "Prodigy",
        requiredExp = 12300L,
        rewardDescription = "Item Lantern (1x) & Tag Mewah Prodigy",
        rewardType = LevelRewardType.POWER_UP,
        rewardPowerUps = listOf(PowerUpType.LANTERN to 1)
    ),
    LevelInfo(
        level = 14,
        title = "Sage",
        requiredExp = 14750L,
        rewardDescription = "Tag Mewah Sage & Paket Power-up Tempur Lengkap",
        rewardType = LevelRewardType.POWER_UP,
        rewardPowerUps = listOf(
            PowerUpType.STREAK_PROTECTION to 2,
            PowerUpType.REMOVE_TWO_OPTIONS to 3,
            PowerUpType.BINOCULAR to 2,
            PowerUpType.LEATHER_STRAPS to 1
        )
    ),
    LevelInfo(
        level = 15,
        title = "Scholar",
        requiredExp = 17500L,
        rewardDescription = "Emblem \"The Myth & The Legend\" & Bingkai \"Mystic Blossom\"",
        rewardType = LevelRewardType.LEGENDARY,
        emblemName = "The Myth & The Legend",
        frameId = "frame_4",
        frameName = "Mystic Blossom"
    )
)

/**
 * Menghitung LevelInfo berdasarkan saldo EXP kumulatif
 */
fun getLevelInfoForExp(exp: Long): LevelInfo {
    val safeExp = exp.coerceAtLeast(0L)
    for (i in SENIMATIKA_LEVELS.indices.reversed()) {
        if (safeExp >= SENIMATIKA_LEVELS[i].requiredExp) {
            return SENIMATIKA_LEVELS[i]
        }
    }
    return SENIMATIKA_LEVELS.first()
}

/**
 * Mendapatkan LevelInfo berdasarkan level integer (1 - 15)
 */
fun getLevelInfo(level: Int): LevelInfo {
    val safeLevel = level.coerceIn(1, 15)
    return SENIMATIKA_LEVELS.find { it.level == safeLevel } ?: SENIMATIKA_LEVELS.first()
}

/**
 * Menghitung progres EXP pada level saat ini
 * Return: Pair(expDiLevelIni, expDibutuhkanUntukNaikLevel)
 */
fun getExpProgressInCurrentLevel(currentExp: Long): Pair<Long, Long> {
    val currentLevelInfo = getLevelInfoForExp(currentExp)
    if (currentLevelInfo.level >= 15) {
        return Pair(currentExp - currentLevelInfo.requiredExp, 0L) // Max level
    }
    val nextLevelInfo = getLevelInfo(currentLevelInfo.level + 1)
    val expInLevel = currentExp - currentLevelInfo.requiredExp
    val expNeededForNext = nextLevelInfo.requiredExp - currentLevelInfo.requiredExp
    return Pair(expInLevel.coerceAtLeast(0L), expNeededForNext.coerceAtLeast(1L))
}

/**
 * Menghitung Multiplier Pengulangan (Repeat-Play Multiplier)
 * Sesi 1: 1.00
 * Sesi 2: 0.80
 * Sesi 3: 0.60
 * Sesi 4: 0.40
 * Sesi 5+: 0.30
 */
fun getRepeatPlayMultiplier(completionCount: Int): Float {
    return when (completionCount) {
        0 -> 1.00f // Pertama kali menyelesaikan
        1 -> 0.80f // Sesi 2
        2 -> 0.60f // Sesi 3
        3 -> 0.40f // Sesi 4
        else -> 0.30f // Sesi 5 dan seterusnya
    }
}

/**
 * Menghitung Bonus EXP Kuis (Akurasi, Kecepatan, Streak)
 * Proporsi total bonus dibatasi maksimal 20% dari Base EXP.
 */
fun calculateQuizBonusExp(
    baseExp: Int,
    totalQuestions: Int,
    correctCount: Int,
    avgSpeedRatio: Float, // 0.0f .. 1.0f (semakin cepat, semakin mendekati 1)
    maxStreak: Int
): Int {
    if (baseExp <= 0 || totalQuestions <= 0) return 0
    val maxBonus = (baseExp * 0.20f) // Maksimal 20% dari base exp

    val accuracyFactor = (correctCount.toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)
    val speedFactor = avgSpeedRatio.coerceIn(0f, 1f)
    val streakFactor = (maxStreak.toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)

    // Bobot: Akurasi 50%, Kecepatan 25%, Streak 25% dari alokasi bonus
    val calculatedBonus = maxBonus * (accuracyFactor * 0.50f + speedFactor * 0.25f + streakFactor * 0.25f)
    return calculatedBonus.toInt().coerceIn(0, maxBonus.toInt())
}

/**
 * Mendapatkan ID resource ikon notifikasi untuk kenaikan level
 */
fun getLevelNotificationIcon(level: Int): Int {
    return when (level) {
        1 -> R.drawable.img_rocket_pemula
        2 -> R.drawable.img_explore
        3 -> R.drawable.ic_star
        4 -> R.drawable.ic_pu_freeze
        5 -> R.drawable.ic_genius
        6 -> R.drawable.ic_pu_shield
        7 -> R.drawable.ic_pu_torch
        8 -> R.drawable.ic_pu_vial
        9 -> R.drawable.ico_statistic
        10 -> R.drawable.ic_expert
        11 -> R.drawable.ic_algebra_master
        12 -> R.drawable.ic_calculus_master
        13 -> R.drawable.ic_pu_lantern
        14 -> R.drawable.ic_pu_belt
        15 -> R.drawable.ic_trophy
        else -> R.drawable.ic_star
    }
}

/**
 * Menambahkan EXP ke profil user, mengecek kenaikan level,
 * dan mengirim notifikasi jika terjadi Level Up.
 */
suspend fun grantExpToUser(
    userId: String,
    expToAdd: Int,
    sourceDescription: String = ""
): Int {
    if (userId.isBlank() || expToAdd <= 0) return 0
    val db = Firebase.firestore
    val userRef = db.collection("users").document(userId)

    return try {
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentExp = snapshot.getLong("exp") ?: 0L
            val oldLevel = snapshot.getLong("level")?.toInt() ?: getLevelInfoForExp(currentExp).level

            val newExp = currentExp + expToAdd
            val newLevelInfo = getLevelInfoForExp(newExp)
            val newLevel = newLevelInfo.level

            val currentTitles = (snapshot.get("unlockedTitles") as? List<*>)?.mapNotNull { it as? String } ?: listOf("Novice")
            val currentEmblems = (snapshot.get("unlockedEmblems") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            var activeTitle = snapshot.getString("activeTitle") ?: "Novice"
            var activeEmblem = snapshot.getString("activeEmblem") ?: ""

            val updates = mutableMapOf<String, Any>(
                "exp" to newExp,
                "level" to newLevel
            )

            // Jika naik level
            if (newLevel > oldLevel) {
                // Kirim Notifikasi Level Up ke subcollection users/{userId}/achievements (sesuai security rules)
                val notifDocId = "level_up_${newLevel}_${System.currentTimeMillis()}"
                val notifRef = userRef.collection("achievements").document(notifDocId)
                val notifData = hashMapOf(
                    "name" to "Naik ke Level $newLevel! (${newLevelInfo.title})",
                    "title" to "Naik ke Level $newLevel! (${newLevelInfo.title})",
                    "message" to "Selamat! Kamu telah mencapai Level $newLevel. Klaim hadiah pada menu Profil!",
                    "type" to "LEVEL_UP",
                    "level" to newLevel,
                    "timestamp" to Timestamp.now(),
                    "isUnlocked" to true,
                    "isRead" to false,
                    "notified" to true
                )
                transaction.set(notifRef, notifData, SetOptions.merge())
                Log.d("LevelProgression", "Level Up Notification queued for Level $newLevel")
            }

            transaction.update(userRef, updates)
            newLevel - oldLevel
        }.await()
    } catch (e: Exception) {
        Log.e("LevelProgression", "Gagal menambahkan EXP: ${e.message}", e)
        0
    }
}

/**
 * Klaim manual reward level (1x per level) oleh pemain
 */
suspend fun claimLevelReward(
    userId: String,
    levelToClaim: Int
): Result<String> {
    if (userId.isBlank()) return Result.failure(Exception("User ID kosong"))
    val levelInfo = getLevelInfo(levelToClaim)
    val db = Firebase.firestore
    val userRef = db.collection("users").document(userId)

    return try {
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentExp = snapshot.getLong("exp") ?: 0L
            val currentLevel = getLevelInfoForExp(currentExp).level

            if (currentLevel < levelToClaim) {
                throw Exception("Level Anda belum mencapai Level $levelToClaim")
            }

            val claimedList = (snapshot.get("claimedLevelRewards") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
            if (claimedList.contains(levelToClaim)) {
                throw Exception("Reward Level $levelToClaim sudah diklaim sebelumnya")
            }

            val currentPowerUps = (snapshot.get("powerUps") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val newPowerUps = currentPowerUps.toMutableList()

            // Tambahkan item power-ups jika ada
            levelInfo.rewardPowerUps.forEach { (puType, count) ->
                repeat(count) {
                    newPowerUps.add(puType.name)
                }
            }

            val updates = mutableMapOf<String, Any>(
                "claimedLevelRewards" to FieldValue.arrayUnion(levelToClaim),
                "powerUps" to newPowerUps,
                "unlockedTitles" to FieldValue.arrayUnion(levelInfo.title)
            )

            if (levelInfo.emblemName != null) {
                updates["unlockedEmblems"] = FieldValue.arrayUnion(levelInfo.emblemName)
            }

            if (levelInfo.frameId != null) {
                updates["unlockedFrames"] = FieldValue.arrayUnion(levelInfo.frameId)
            }

            transaction.update(userRef, updates)
            "Reward Level $levelToClaim (${levelInfo.rewardDescription}) berhasil diklaim!"
        }.await().let { Result.success(it) }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Klaim reward EXP materi (bersifat first-time reward / 1x per materi per akun).
 * Hanya berhasil jika pengguna mengunjungi SummaryScreen pertama kalinya.
 * Mengembalikan jumlah EXP yang berhasil diberikan (misal 150), atau 0 jika sudah pernah diklaim sebelumnya.
 */
suspend fun claimMaterialExpReward(
    userId: String,
    materialId: String,
    expAmount: Int
): Result<Int> {
    if (userId.isBlank() || materialId.isBlank() || expAmount <= 0) {
        return Result.success(0)
    }
    val db = Firebase.firestore
    val userRef = db.collection("users").document(userId)

    return try {
        val granted = db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val claimedList = (snapshot.get("claimedMaterialRewards") as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()

            // Jika sudah pernah diklaim untuk materialId ini, jangan berikan EXP lagi
            if (claimedList.contains(materialId)) {
                return@runTransaction 0
            }

            val currentExp = snapshot.getLong("exp") ?: 0L
            val oldLevel = snapshot.getLong("level")?.toInt() ?: getLevelInfoForExp(currentExp).level

            val newExp = currentExp + expAmount
            val newLevelInfo = getLevelInfoForExp(newExp)
            val newLevel = newLevelInfo.level

            val updates = mutableMapOf<String, Any>(
                "exp" to newExp,
                "level" to newLevel,
                "claimedMaterialRewards" to FieldValue.arrayUnion(materialId)
            )

            // Jika naik level dari reward materi
            if (newLevel > oldLevel) {
                val notifDocId = "level_up_${newLevel}_${System.currentTimeMillis()}"
                val notifRef = userRef.collection("achievements").document(notifDocId)
                val notifData = hashMapOf(
                    "name" to "Naik ke Level $newLevel! (${newLevelInfo.title})",
                    "title" to "Naik ke Level $newLevel! (${newLevelInfo.title})",
                    "message" to "Selamat! Kamu telah mencapai Level $newLevel setelah menyelesaikan materi!",
                    "type" to "LEVEL_UP",
                    "level" to newLevel,
                    "timestamp" to Timestamp.now(),
                    "isUnlocked" to true,
                    "isRead" to false,
                    "notified" to true
                )
                transaction.set(notifRef, notifData, SetOptions.merge())
            }

            transaction.update(userRef, updates)
            expAmount
        }.await()

        Result.success(granted)
    } catch (e: Exception) {
        Log.e("LevelProgression", "Gagal klaim EXP materi $materialId: ${e.message}", e)
        Result.failure(e)
    }
}
