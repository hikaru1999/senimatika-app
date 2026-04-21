package com.LambdaProject.MathArt.data.repository

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
    private val db = Firebase.firestore

    // Menggunakan Deterministic ID: userId_materialId
    fun saveLearningSession(userId: String, material: MaterialItem, onComplete: () -> Unit) {
        val documentId = "${userId}_${material.id}"
        val docRef = db.collection("sessions").document(documentId)

        // Cek Cache Lokal dulu (Source.DEFAULT) - Hemat Read
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
            // Jika gagal (misal offline dan cache kosong), coba buat baru
            onComplete()
        }
    }

    fun markSessionCompleted(userId: String, materialId: String, onComplete: () -> Unit = {}) {
        val documentId = "${userId}_$materialId"
        db.collection("sessions")
            .document(documentId)
            .update("status", "completed")
            .addOnSuccessListener {
                Log.d("Firestore", "Session status updated to completed")
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
            .get(Source.DEFAULT) // Utamakan Cache
            .addOnSuccessListener { result ->
                val status = result.getString("status")
                onResult(status == "active")
            }
            .addOnFailureListener { onResult(false) }
    }
}

/* class SessionRepository (
    private val db: FirebaseFirestore = Firebase.firestore
) {
    suspend fun saveLearningSession(userId: String, material: MaterialItem): Result<Unit> {
        return try {
            val documentId = "${userId}_${material.id}"
            val docRef = db.collection("sessions").document(documentId)

            // Gunakan Source.DEFAULT agar mengecek cache lokal dulu
            val snapshot = docRef.get(Source.DEFAULT).await()

            if (!snapshot.exists()) {
                // Jika belum ada, buat sesi baru (1 Write)
                val sessionData = hashMapOf(
                    "userId" to userId,
                    "materialId" to material.id,
                    "title" to material.title,
                    "status" to "active",
                    "startedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(sessionData).await()
            } else {
                // Jika sudah ada, cek statusnya
                val existingStatus = snapshot.getString("status")
                if (existingStatus == "completed") {
                    // Hanya update jika sebelumnya sudah selesai (1 Write)
                    docRef.update(
                        mapOf(
                            "status" to "active",
                            "startedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SessionRepo", "Error saving session", e)
            Result.failure(e)
        }
    }

    suspend fun markSessionCompleted(userId: String, materialId: String): Result<Unit> {
        return try {
            val documentId = "${userId}_$materialId"
            val docRef = db.collection("sessions").document(documentId)

            // Gunakan update langsung (Hanya memakan 1 Write jika dokumen ada)
            docRef.update("status", "completed").await()

            Log.d("Firestore", "Session status updated to completed")
            Result.success(Unit)
        } catch (e: Exception) {
            // Jika dokumen tidak ditemukan (jarang terjadi dengan deterministic ID), log error
            Log.e("Firestore", "Failed to update Sessions: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun checkActiveSession(userId: String, materialId: String): Boolean {
        return try {
            val documentId = "${userId}_$materialId"
            val snapshot = db.collection("sessions")
                .document(documentId)
                .get(Source.DEFAULT)
                .await()

            snapshot.getString("status") == "active"
        } catch (e: Exception) {
            false
        }
    }
} */

/* object SessionRepository {
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
} */