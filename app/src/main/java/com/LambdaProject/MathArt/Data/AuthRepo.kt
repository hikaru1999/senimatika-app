package com.LambdaProject.MathArt.Data

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun loginUser(identifier: String, password: String, onComplete: (Boolean, String?, String?) -> Unit) {
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
                    onComplete(false, "Username tidak ditemukan", null)
                }
            }
            .addOnFailureListener { e -> onComplete(false, e.message, null) }
    }
}

fun registerUser(
    context: Context,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    email: String,
    password: String,
    username: String,
    confirmPasswordError: Boolean,
    usernameError: Boolean,
    passwordStrength: Int,
    onSuccess: (String) -> Unit
) {
    if (confirmPasswordError || usernameError || passwordStrength == 0 || passwordStrength == - 1){
        Toast.makeText(context, "Periksa kembali input Anda!", Toast.LENGTH_SHORT).show()
        return
    }

    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->
            val user = result.user
            if(user != null) {
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "username" to username,
                    "email" to email
                )

                db.collection("users").document(user.uid).set(userData)
                    .addOnSuccessListener {
                        val durationData = mapOf(
                            "duration" to 0L,
                            "startTimestamp" to System.currentTimeMillis()
                        )

                        db.collection("durations").document(user.uid)
                            .set(durationData)
                            .addOnSuccessListener {
                                onSuccess("Selamat akunmu telah dibuat. Yuk coba login!")
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Gagal menyimpan data durasi!", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Registrasi Gagal!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Registrasi Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}

fun checkPasswordStrength(password: String): Int {
    return when {
        password.length > 20 -> -1
        password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 2
        password.length >= 6 -> 1
        else -> 0
    }
}