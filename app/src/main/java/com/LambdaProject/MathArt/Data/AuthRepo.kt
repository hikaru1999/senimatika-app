package com.LambdaProject.MathArt.Data

import com.LambdaProject.MathArt.ViewModels.RegisterViewModel
import com.LambdaProject.MathArt.model.OnlineUser
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
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
                                    onComplete(true, null, username)
                                }
                                .addOnFailureListener { e ->
                                    onComplete(false, "Gagal mengambil data pengguna", null) }
                        } else {
                            onComplete(false, "Kredensial tidak ditemukan", null)
                        }
                    } else {
                        val errorMsg = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "Email tidak terdaftar"
                            is FirebaseAuthInvalidCredentialsException -> "Password tidak tepat"
                            else -> task.exception?.message ?: "Terjadi kesalahan saat login"
                        }
                        /* onComplete(false, task.exception?.message, null) */
                        onComplete(false, errorMsg, null)
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
                                        onComplete(false, "Kredensial tidak ditemukan", null)
                                    }
                                } else {
                                    val errorMsg = when (task.exception) {
                                        is FirebaseAuthInvalidUserException -> "Username tidak terdaftar"
                                        is FirebaseAuthInvalidCredentialsException -> "Password tidak tepat"
                                        else -> task.exception?.message ?: "Terjadi kesalahan saat login"
                                    }
                                    /* onComplete(false, task.exception?.message, null) */
                                    onComplete(false, errorMsg, null)
                                }
                            }
                    } else {
                        onComplete(false, "Kredensial tidak ditemukan", null)
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
        kelas: String,
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
                "isOnline" to true,
                "coins" to 0,
                "completedQuizzes" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users").document(userId).set(userData).await()

            RegisterViewModel.RegisterState.Success(userId)
        } catch  (e: Exception) {
            RegisterViewModel.RegisterState.Error(e.message ?: "Unknown Error")
        }
    }

    suspend fun sendResetPassword(identifier: String): Result<String> {
        return try {
            val email = if (identifier.contains("@")) {
                identifier
            } else {
                val userSnapshot = firestore.collection("users")
                    .whereEqualTo("username", identifier)
                    .limit(1)
                    .get()
                    .await()

                if (userSnapshot.isEmpty) {
                    return Result.failure(Exception("Username tidak ditemukan."))
                }

                val emailFromDb = userSnapshot.documents.first().getString("email")
                if (emailFromDb.isNullOrBlank()) {
                    return Result.failure(Exception("Email tidak tersedia untuk username tersebut."))
                }

                emailFromDb
            }
            auth.sendPasswordResetEmail(email).await()
            Result.success("Link reset berhasil dikirim ke $email")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersOnline(): List<OnlineUser> {
        val currentUserUid = auth.currentUser?.uid

        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("isOnline", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                val username = doc.getString("username") ?: return@mapNotNull null
                if (uid != currentUserUid) OnlineUser(uid = uid, username = username) else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}