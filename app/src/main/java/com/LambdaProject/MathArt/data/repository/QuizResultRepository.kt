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
            val docRef = firestore.collection("online_quiz_results").document(documentId)
            
            val existingDoc = docRef.get(Source.DEFAULT).await()
            val existingScore = existingDoc.getLong("totalPoints")?.toInt() ?: 0
            
            if (!existingDoc.exists() || result.totalPoints >= existingScore) {
                docRef.set(result).await()
                Log.d("QuizResultRepository", "Saved new high score for ${result.userId}: ${result.totalPoints}")
            } else {
                Log.d("QuizResultRepository", "Retained higher existing score ($existingScore) over current (${result.totalPoints})")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("QuizResultRepository", "Error saving quiz result: ${e.message}", e)
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
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUsernamesForUserIds(userIds: List<String>): Map<String, String> {
        return getUserProfilesForUserIds(userIds).mapValues { it.value.first }
    }

    suspend fun getUserProfilesForUserIds(userIds: List<String>): Map<String, Pair<String, String>> {
        return try {
            val chunks = userIds.chunked(30)
            val results = mutableMapOf<String, Pair<String, String>>()

            for (chunk in chunks) {
                val snapshot = firestore.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get(Source.DEFAULT)
                    .await()

                for (doc in snapshot.documents) {
                    val username = doc.getString("username")
                        ?: doc.getString("name")
                        ?: doc.getString("displayName")
                        ?: doc.getString("email")?.substringBefore("@")
                        ?: "Guest_${doc.id.take(4)}"
                    val avatar = doc.getString("avatar") ?: "avatar_5"
                    results[doc.id] = Pair(username, avatar)
                }
            }

            results
        } catch (e: Exception) {
            Log.e("QuizResultRepository", "Error getting user profiles: ${e.message}", e)
            emptyMap()
        }
    }

    suspend fun getLeaderBoardForMaterial(materialId: String, limit: Long = 50): Result<List<QuizResult>> {
        return try {
            val snapshot = try {
                firestore.collection("online_quiz_results")
                    .whereEqualTo("materialId", materialId)
                    .orderBy("totalPoints", Query.Direction.DESCENDING)
                    .limit(limit)
                    .get(Source.DEFAULT)
                    .await()
            } catch (indexEx: Exception) {
                Log.w("LeaderboardRepository", "Direct index query failed, using in-memory sort fallback: ${indexEx.message}")
                firestore.collection("online_quiz_results")
                    .whereEqualTo("materialId", materialId)
                    .get(Source.DEFAULT)
                    .await()
            }

            val results = snapshot.toObjects(QuizResult::class.java)
                .sortedByDescending { it.totalPoints }
                .take(limit.toInt())

            Log.d("LeaderboardRepository", "Loaded ${results.size} leaderboard entries for material: $materialId")
            Result.success(results)
        } catch (e: Exception) {
            Log.e("LeaderboardRepository", "Error fetching leaderboard: ${e.message}", e)
            Result.failure(e)
        }
    }
}
