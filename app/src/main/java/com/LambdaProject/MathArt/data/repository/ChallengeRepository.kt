package com.LambdaProject.MathArt.data.repository

import com.LambdaProject.MathArt.data.model.Challenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChallengeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun sendChallengeRequest(toUserId: String, materialId: String): Boolean {
        val fromUserId = auth.currentUser?.uid ?: return false
        return try {
            val challengeData = mapOf(
                "fromUserId" to fromUserId,
                "toUserId" to toUserId,
                "materailId" to materialId,
                "status" to "pending",
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("challenges").add(challengeData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun listenForIncomingChallenges(
        onChallengeReceived: (List<Challenge>) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("challenges")
            .whereEqualTo("toUserId", currentUserId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val challenges = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Challenge(
                        id = doc.id,
                        fromUserId = data["fromUserId"] as? String ?: "",
                        toUserId = data["toUserId"] as? String ?: "",
                        materialId = data["materialId"] as? String ?: "",
                        status = data["status"] as? String ?: "",
                        timestamp = data["timestamp"] as? Long ?: 0L
                    )
                } ?: emptyList()
                onChallengeReceived(challenges)
            }
    }
}