package com.LambdaProject.MathArt.Data

import com.LambdaProject.MathArt.ViewModels.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun loginUser(
        identifier: String,
        password: String,
        onComplete: (Boolean, String?, String?) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        if (identifier.contains("@")) {
            auth.signInWithEmailAndPassword(identifier, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    val username = document.getString("username") ?: "User"
                                    onComplete(true, null, username)  // Kirim username ke callback
                                }
                                .addOnFailureListener { e -> onComplete(false, e.message, null) }
                        } else {
                            onComplete(false, "User ID tidak ditemukan", null)
                        }
                    } else {
                        onComplete(false, task.exception?.message, null)
                    }
                }
        } else {
            db.collection("users").whereEqualTo("username", identifier).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val email = documents.documents[0].getString("email") ?: return@addOnSuccessListener
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        db.collection("users").document(userId).get()
                                            .addOnSuccessListener { document ->
                                                val username = document.getString("username") ?: "User"
                                                onComplete(true, null, username)
                                            }
                                            .addOnFailureListener { e -> onComplete(false, e.message, null) }
                                    } else {
                                        onComplete(false, "User ID tidak ditemukan", null)
                                    }
                                } else {
                                    onComplete(false, task.exception?.message, null)
                                }
                            }
                    } else {
                        onComplete(false, "Username tidak ditemukan", null)
                    }
                }
                .addOnFailureListener { e -> onComplete(false, e.message, null) }
        }
    }

    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        fullName: String,
        grade: String,
        kelas: String
    ) : RegisterViewModel.RegisterState {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val userId = auth.currentUser?.uid ?: throw Exception("User ID Not Found")

            val userData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "email" to email,
                "fullname" to fullName,
                "grade" to grade,
                "kelas" to kelas,
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users").document(userId).set(userData).await()
            RegisterViewModel.RegisterState.Success(userId)
        } catch  (e: Exception) {
            RegisterViewModel.RegisterState.Error(e.message ?: "Unknown Error")
        }
    }

}