package com.LambdaProject.MathArt.utils

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.LambdaProject.MathArt.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * ProgressionDebugManager
 * Utilitas pengujian dan debugging sistem Level & Progression Senimatika.
 * Dapat diaktifkan / dinonaktifkan secara fleksibel oleh developer / QA tester.
 */
object ProgressionDebugManager {

    /**
     * Flag status aktif/nonaktif fitur debug.
     */
    var isDebugEnabled by mutableStateOf(false)

    /**
     * Tambah EXP sejumlah tertentu ke user aktif melalui grantExpToUser
     */
    suspend fun addExp(userId: String, amount: Int): Result<Int> {
        return try {
            val levelUpCount = grantExpToUser(userId, amount, "Debug Injection")
            Result.success(levelUpCount)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to add exp: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Lompat langsung ke level tertentu (1 - 15)
     */
    suspend fun setLevel(userId: String, targetLevel: Int): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val safeLevel = targetLevel.coerceIn(1, 15)
        val levelInfo = getLevelInfo(safeLevel)
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)

        return try {
            val updates = mutableMapOf<String, Any>(
                "exp" to levelInfo.requiredExp,
                "level" to safeLevel
            )

            userRef.update(updates).await()

            // Buat notifikasi level up untuk test
            if (safeLevel > 1) {
                sendTestLevelNotification(userId, safeLevel)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to set level: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Atur saldo EXP kustom
     */
    suspend fun setCustomExp(userId: String, targetExp: Long): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val safeExp = targetExp.coerceAtLeast(0L)
        val calculatedLevelInfo = getLevelInfoForExp(safeExp)
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)

        return try {
            val updates = mapOf(
                "exp" to safeExp,
                "level" to calculatedLevelInfo.level
            )
            userRef.update(updates).await()
            if (calculatedLevelInfo.level > 1) {
                sendTestLevelNotification(userId, calculatedLevelInfo.level)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to set custom exp: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Reset total seluruh progres ke Level 1 (0 EXP)
     */
    suspend fun resetProgression(userId: String): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)

        return try {
            val updates = mapOf<String, Any>(
                "exp" to 0L,
                "level" to 1,
                "claimedLevelRewards" to emptyList<Int>(),
                "unlockedTitles" to listOf("Novice"),
                "unlockedEmblems" to emptyList<String>(),
                "activeTitle" to "Novice",
                "activeEmblem" to "",
                "quizPlayCounts" to emptyMap<String, Int>(),
                "mapPlayCounts" to emptyMap<String, Int>()
            )
            userRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to reset progression: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Reset riwayat reward yang telah diklaim (claimedLevelRewards = [])
     */
    suspend fun resetClaimedRewards(userId: String): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)

        return try {
            userRef.update("claimedLevelRewards", emptyList<Int>()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to reset claimed rewards: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Klaim semua reward yang memenuhi syarat untuk level saat ini
     */
    suspend fun claimAllAvailableRewards(userId: String, currentLevel: Int): Result<Int> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        return try {
            var claimedCount = 0
            for (lvl in 1..currentLevel) {
                val res = claimLevelReward(userId, lvl)
                if (res.isSuccess) claimedCount++
            }
            Result.success(claimedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Simulasikan jumlah sesi pengulangan kuis / eksplorasi (Repeat-Play Multipliers)
     * Session 1 (count=0, mult=1.0x), Session 2 (count=1, mult=0.8x), dst.
     */
    suspend fun setSimulatedPlayCount(userId: String, count: Int): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)

        return try {
            val dummyQuizMap = mapOf("debug_quiz_master" to count)
            val dummyExplorationMap = mapOf("default_map" to count)
            userRef.update(
                mapOf(
                    "quizPlayCounts" to dummyQuizMap,
                    "mapPlayCounts" to dummyExplorationMap
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to set play counts: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Reset riwayat jumlah penyelesaian kuis & peta
     */
    suspend fun resetPlayCounts(userId: String): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)

        return try {
            userRef.update(
                mapOf(
                    "quizPlayCounts" to emptyMap<String, Int>(),
                    "mapPlayCounts" to emptyMap<String, Int>()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to reset play counts: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Buka semua Gelar dan Emblem untuk testing WordArt & Emblems
     */
    suspend fun unlockAllTitlesAndEmblems(userId: String): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)

        return try {
            val allTitles = SENIMATIKA_LEVELS.map { it.title }.distinct()
            val allEmblems = SENIMATIKA_LEVELS.mapNotNull { it.emblemName }.distinct()

            userRef.update(
                mapOf(
                    "unlockedTitles" to allTitles,
                    "unlockedEmblems" to allEmblems
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to unlock all titles/emblems: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Kirim notifikasi uji coba kenaikan level ke subcollection users/{userId}/achievements
     */
    suspend fun sendTestLevelNotification(userId: String, level: Int): Result<Unit> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val db = Firebase.firestore
        val userRef = db.collection("users").document(userId)
        val levelInfo = getLevelInfo(level)

        return try {
            val docId = "level_up_${level}_${System.currentTimeMillis()}"
            val notifData = hashMapOf(
                "name" to "Naik ke Level $level! (${levelInfo.title})",
                "title" to "Naik ke Level $level! (${levelInfo.title})",
                "message" to "Selamat! Kamu telah mencapai Level $level. Reward level siap diklaim di Profil!",
                "type" to "LEVEL_UP",
                "level" to level,
                "timestamp" to Timestamp.now(),
                "isUnlocked" to true,
                "isRead" to false,
                "notified" to true
            )
            userRef.collection("achievements").document(docId).set(notifData, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to send test notification: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Hapus semua notifikasi level up di users/{userId}/achievements
     */
    suspend fun clearNotifications(userId: String): Result<Int> {
        if (userId.isBlank()) return Result.failure(Exception("User ID tidak valid"))
        val db = Firebase.firestore
        val achCollection = db.collection("users").document(userId).collection("achievements")

        return try {
            val snapshot = achCollection.get().await()
            var deletedCount = 0
            for (doc in snapshot.documents) {
                val isLevelUp = doc.getString("type") == "LEVEL_UP" || doc.id.startsWith("level_up_") || doc.id.startsWith("debug_lvl_")
                if (isLevelUp) {
                    doc.reference.delete().await()
                    deletedCount++
                }
            }
            Result.success(deletedCount)
        } catch (e: Exception) {
            Log.e("ProgressionDebug", "Failed to clear notifications: ${e.message}", e)
            Result.failure(e)
        }
    }
}
