package com.LambdaProject.MathArt.data.repository

import android.annotation.SuppressLint
import android.util.Log
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlin.io.path.exists

object SessionRepository {
    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore

    fun saveLearningSession(userId: String, material: MaterialItem, onComplete: () -> Unit) {
        val documentId = "${userId}_${material.id}"
        val docRef = db.collection("sessions").document(documentId)

        docRef.get(Source.DEFAULT).addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val sessionData = hashMapOf(
                    "userId" to userId,
                    "materialId" to material.id,
                    "title" to material.title,
                    "status" to "active",
                    "startedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(sessionData).addOnSuccessListener { onComplete() }
            } else {
                val existingStatus = snapshot.getString("status")
                if (existingStatus == "completed") {
                    docRef.update(
                        mapOf(
                            "status" to "active",
                            "startedAt" to FieldValue.serverTimestamp()
                        )
                    ).addOnSuccessListener { onComplete() }
                } else {
                    onComplete()
                }
            }
        }.addOnFailureListener {
            onComplete()
        }
    }

    fun markSessionCompleted(userId: String, materialId: String, onComplete: () -> Unit = {}) {
        val documentId = "${userId}_$materialId"
        db.collection("sessions")
            .document(documentId)
            .update("status", "completed")
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Gagal update session: ${e.message}")
            }
    }

    fun checkActiveSession(userId: String, materialId: String, onResult: (Boolean) -> Unit) {
        val documentId = "${userId}_$materialId"
        db.collection("sessions")
            .document(documentId)
            .get(Source.DEFAULT)
            .addOnSuccessListener { result ->
                val status = result.getString("status")
                onResult(status == "active")
            }
            .addOnFailureListener { onResult(false) }
    }
}