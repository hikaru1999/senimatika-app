package com.LambdaProject.MathArt.Data

import android.util.Log
import com.LambdaProject.MathArt.model.MaterialItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

object SessionRepository {
    fun saveLearningSession(userId: String, material: MaterialItem, onComplete: () -> Unit) {
        val db = Firebase.firestore
        val sessionData = hashMapOf(
            "userId" to userId,
            "materialId" to material.id,
            "title" to material.title,
            "status" to "active",
            "startedAt" to FieldValue.serverTimestamp()
        )

        db.collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("materialId", material.id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    db.collection("sessions")
                        .add(sessionData)
                        .addOnSuccessListener { onComplete() }
                } else {
                    val doc = documents.first()
                    val existingStatus = doc.getString("status")

                    if (existingStatus == "completed") {
                        // Update status dari completed ke active
                        db.collection("sessions")
                            .document(doc.id)
                            .update(
                                mapOf(
                                    "status" to "active",
                                    "startedAt" to FieldValue.serverTimestamp()
                                )
                            )
                            .addOnSuccessListener { onComplete() }
                    } else {
                        onComplete()
                    }
                }
            }
    }
    
    fun markSessionCompleted(userId: String, materialId: String, onComplete: () -> Unit = {}) {
        val db = Firebase.firestore
        db.collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("materialId", materialId)
            .get()
            .addOnSuccessListener { result ->
                if(!result.isEmpty) {
                    val document = result.documents.first()
                    db.collection("sessions")
                        .document(document.id)
                        .update("status", "completed")
                        .addOnSuccessListener { 
                            Log.d("Firestore", "Session status updated to completed")
                            onComplete()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Failed to update Sessions", e)
                        }
                } else {
                    Log.w("Firestore", "Failed to fetch session")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch session", e)
            }
    }

    fun checkActiveSession(userId: String, materialId: String, onResult: (Boolean) -> Unit) {
        val db = Firebase.firestore
        db.collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("materialId", materialId)
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { result ->
                onResult(result.isEmpty.not())
            }
    }
}