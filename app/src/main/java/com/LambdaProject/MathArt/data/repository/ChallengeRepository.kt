package com.LambdaProject.MathArt.data.repository

import com.LambdaProject.MathArt.data.model.Challenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    fun listenForIncomingChallenges(onChallengeReceived: (List<Challenge>) -> Unit): ListenerRegistration {
        val userId = auth.currentUser?.uid ?: ""

        return firestore.collection("challenges")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val challenges = snapshot?.documents?.mapNotNull {
                    it.toObject(Challenge::class.java)?.copy(id = it.id)
                } ?: emptyList()

                onChallengeReceived(challenges)
            }
    }
}