package com.LambdaProject.MathArt.data.repository

import android.util.Log
import com.LambdaProject.MathArt.data.model.OnlineQuizDesc
import com.LambdaProject.MathArt.data.model.QuizResult
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class QuizResultRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun saveQuizResult(result: QuizResult): Result<Unit> {
        return try {
            val documentId = "${result.userId}_${result.materialId}"
            firestore.collection("online_quiz_results")
                .document(documentId)
                .set(result)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserQuizResult(userId: String, materialId: String): Result<QuizResult> {
        return try {
            val snapshot = Firebase.firestore
                .collection("online_quiz_results")
                .document("${userId}_$materialId")
                .get(Source.DEFAULT)
                .await()

            val result = snapshot.toObject(QuizResult::class.java)
            if (result != null) Result.success(result)
            else Result.failure(Exception("No result found"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rewardIfFirstTime(
        userId: String,
        materialId: String,
        quizList: List<OnlineQuizDesc>
    ): Boolean {
        return try {
            val userRef = Firebase.firestore.collection("users").document(userId)
            val snapshot = userRef.get(Source.SERVER).await()

            val completed = snapshot.get("completedQuizzes") as? List<*> ?: emptyList<Any>()
            val quiz = quizList.find { it.id == materialId }
            val rewardCoins = quiz?.rewardCoin ?: 0

            if (!completed.contains(materialId)) {
                userRef.update(
                    "completedQuizzes", FieldValue.arrayUnion(materialId),
                    "coins", FieldValue.increment(rewardCoins.toLong())
                ).await()
                Log.d("Reward", "Reward diberikan: $rewardCoins koin untuk kuis $materialId")
                true
            } else {
                Log.d("Reward", "Kuis $materialId sudah pernah diselesaikan. Tidak ada reward.")
                false
            }
        } catch (e: Exception) {
            Log.e("RewardError", "Gagal memberikan reward: ${e.message}")
            false
        }
    }

    suspend fun getUsernamesForUserIds(userIds: List<String>): Map<String, String> {
        return try {
            val chunks = userIds.chunked(30) // karena Firestore batasi `in` query max 10
            val results = mutableMapOf<String, String>()

            for (chunk in chunks) {
                val snapshot = firestore.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get(Source.DEFAULT)
                    .await()

                for (doc in snapshot.documents) {
                    doc.getString("username")?.let {
                        results[doc.id] = it
                    }
                }
            }

            results
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getLeaderBoardForMaterial(materialId: String, limit: Long = 50): Result<List<QuizResult>> {
        return try {
            val snapshot = firestore.collection("online_quiz_results")
                .whereEqualTo("materialId", materialId)
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .limit(limit)
                .get(Source.DEFAULT)
                .await()

            val results = snapshot.toObjects(QuizResult::class.java)

            Result.success(results)
        } catch (e: Exception) {
            Log.e("LeaderboardRepository", "Error fetching leaderboard: ${e.message}", e)

            Result.failure(e)
        }
    }
}
