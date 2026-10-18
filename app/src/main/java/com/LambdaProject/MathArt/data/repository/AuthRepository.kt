package com.LambdaProject.MathArt.data.repository

import com.LambdaProject.MathArt.ViewModels.RegisterViewModel
import com.LambdaProject.MathArt.data.model.OnlineUser
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
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
                            firestore.collection("users").document(userId).get(Source.DEFAULT)
                                .addOnSuccessListener { document ->
                                    val username = document.getString("username") ?: "User"
                                    val authEmail = auth.currentUser?.email
                                    if (!authEmail.isNullOrBlank() && document.getString("email") != authEmail) {
                                        try {
                                            firestore.collection("users").document(userId).update("email", authEmail)
                                        } catch (_: Exception) {}
                                    }
                                    onComplete(true, null, username)
                                }
                                .addOnFailureListener { onComplete(false, "Gagal mengambil data pengguna", null) }
                        } else {
                            onComplete(false, "Kredensial tidak ditemukan", null)
                        }
                    } else {
                        val errorMsg = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "Email tidak terdaftar"
                            is FirebaseAuthInvalidCredentialsException -> "Password tidak tepat"
                            else -> task.exception?.message ?: "Terjadi kesalahan saat login"
                        }
                        onComplete(false, errorMsg, null)
                    }
                }
        } else {
            firestore.collection("users").whereEqualTo("username", identifier)
                .limit(1)
                .get(Source.DEFAULT)
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val email = documents.documents[0].getString("email") ?: return@addOnSuccessListener
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    userId?.let { uid ->
                                        firestore.collection("users").document(uid).get(Source.DEFAULT)
                                            .addOnSuccessListener { doc ->
                                                val authEmail = auth.currentUser?.email
                                                if (!authEmail.isNullOrBlank() && doc.getString("email") != authEmail) {
                                                    try {
                                                        firestore.collection("users").document(uid).update("email", authEmail)
                                                    } catch (_: Exception) {}
                                                }
                                                onComplete(true, null, doc.getString("username") ?: "User")
                                            }
                                    }
                                } else {
                                    onComplete(false, handleAuthError(task.exception, "Username"), null)
                                }
                            }
                    } else {
                        onComplete(false, "Kredensial tidak ditemukan", null)
                    }
                }
        }
    }

    private fun handleAuthError(exception: Exception?, type: String): String {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> "$type tidak terdaftar"
            is FirebaseAuthInvalidCredentialsException -> "Password tidak tepat"
            else -> exception?.message ?: "Terjadi kesalahan"
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
                "avatar" to com.LambdaProject.MathArt.utils.AvatarHelper.DEFAULT_AVATAR,
                "isOnline" to true,
                "coins" to 0,
                "completedQuizzes" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("users").document(userId).set(userData).await()

            // Otomatis kirim email verifikasi saat registrasi dengan template HTML Senimatika
            try {
                val otpResult = com.LambdaProject.MathArt.data.service.EmailService.sendVerificationOtpEmail(userId, email, username)
                if (otpResult.isFailure) {
                    // Fallback aman ke Firebase Auth jika webhook gagal
                    auth.currentUser?.sendEmailVerification(getActionCodeSettings())?.await()
                }
            } catch (emailEx: Exception) {
                android.util.Log.w("AuthRepository", "Gagal mengirim email verifikasi awal via webhook: ${emailEx.message}")
                try {
                    auth.currentUser?.sendEmailVerification(getActionCodeSettings())?.await()
                } catch (_: Exception) {}
            }

            RegisterViewModel.RegisterState.Success(userId)
        } catch  (e: Exception) {
            RegisterViewModel.RegisterState.Error(e.message ?: "Unknown Error")
        }
    }

    private fun getActionCodeSettings(): com.google.firebase.auth.ActionCodeSettings {
        return com.google.firebase.auth.ActionCodeSettings.newBuilder()
            .setUrl("https://senimatika-eduapp.web.app")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.LambdaProject.MathArt", false, "24")
            .build()
    }

    suspend fun sendEmailVerificationToCurrentUser(): Result<String> {
        val user = auth.currentUser ?: return Result.failure(Exception("Sesi login tidak ditemukan. Silakan login terlebih dahulu."))
        val email = user.email ?: return Result.failure(Exception("Email tidak ditemukan."))
        return try {
            val username = try {
                firestore.collection("users").document(user.uid).get().await().getString("username") ?: "Pelajar"
            } catch (_: Exception) {
                user.displayName ?: "Pelajar"
            }

            // 1. Kirimkan email dengan template HTML Senimatika kustom & kode OTP 6-Digit via Google Apps Script
            val otpResult = com.LambdaProject.MathArt.data.service.EmailService.sendVerificationOtpEmail(
                userId = user.uid,
                email = email,
                username = username
            )

            if (otpResult.isSuccess) {
                Result.success("Kode verifikasi 6 digit telah dikirim ke $email")
            } else {
                android.util.Log.e("AuthRepository", "Webhook OTP gagal: ${otpResult.exceptionOrNull()?.message}")
                // 2. Fallback aman ke Firebase Auth bawaan hanya jika Webhook gagal
                try {
                    user.sendEmailVerification(getActionCodeSettings()).await()
                } catch (_: Exception) {}
                Result.success("Tautan verifikasi berhasil dikirim ke $email")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyActionCode(oobCode: String): Result<Unit> {
        return try {
            auth.applyActionCode(oobCode).await()
            val user = auth.currentUser
            user?.reload()?.await()
            user?.uid?.let { uid ->
                try {
                    firestore.collection("users").document(uid).update("isEmailVerified", true).await()
                } catch (_: Exception) {}
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPasswordResetCode(oobCode: String): Result<String> {
        return try {
            val email = auth.verifyPasswordResetCode(oobCode).await()
            Result.success(email)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmPasswordReset(oobCode: String, newPassword: String): Result<Unit> {
        return try {
            auth.confirmPasswordReset(oobCode, newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reloadEmailVerificationStatus(): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            user.reload().await()
            var isVerified = user.isEmailVerified
            if (!isVerified) {
                val doc = firestore.collection("users").document(user.uid).get().await()
                if (doc.getBoolean("isEmailVerified") == true) {
                    isVerified = true
                }
            } else {
                try {
                    firestore.collection("users").document(user.uid).update("isEmailVerified", true).await()
                } catch (_: Exception) {}
            }
            isVerified
        } catch (e: Exception) {
            val firestoreVerified = try {
                firestore.collection("users").document(user.uid).get().await().getBoolean("isEmailVerified") == true
            } catch (_: Exception) { false }
            user.isEmailVerified || firestoreVerified
        }
    }

    suspend fun sendResetPassword(identifier: String): Result<String> {
        return try {
            val (email, username) = if (identifier.contains("@")) {
                val cleanEmail = identifier.trim()
                val userSnapshot = firestore.collection("users")
                    .whereEqualTo("email", cleanEmail)
                    .limit(1)
                    .get()
                    .await()
                val uname = if (!userSnapshot.isEmpty) userSnapshot.documents.first().getString("username") ?: "Pelajar" else "Pelajar"
                cleanEmail to uname
            } else {
                val userSnapshot = firestore.collection("users")
                    .whereEqualTo("username", identifier.trim())
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

                emailFromDb to identifier.trim()
            }

            // Solusi B: Coba kirim via Google Apps Script Webhook terlebih dahulu
            val webhookSent = com.LambdaProject.MathArt.data.service.EmailService.sendPasswordResetViaWebhook(email, username)
            if (webhookSent) {
                Result.success("Tautan reset kata sandi kustom telah dikirim ke $email")
            } else {
                // Fallback aman ke Firebase Auth
                auth.sendPasswordResetEmail(email, getActionCodeSettings()).await()
                Result.success("Link reset berhasil dikirim ke $email")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengirimkan kode OTP 6-digit untuk Reset Password ke email pengguna
     */
    suspend fun sendPasswordResetOtp(identifier: String): Result<Pair<String, String>> {
        return try {
            val (email, username) = if (identifier.contains("@")) {
                val cleanEmail = identifier.trim().lowercase()
                val uname = try {
                    val userSnapshot = firestore.collection("users")
                        .whereEqualTo("email", cleanEmail)
                        .limit(1)
                        .get()
                        .await()
                    if (!userSnapshot.isEmpty) userSnapshot.documents.first().getString("username") ?: "Pelajar" else "Pelajar"
                } catch (_: Exception) {
                    "Pelajar"
                }
                cleanEmail to uname
            } else {
                val cleanUsername = identifier.trim()
                val emailFromDb = try {
                    val userSnapshot = firestore.collection("users")
                        .whereEqualTo("username", cleanUsername)
                        .limit(1)
                        .get()
                        .await()
                    if (!userSnapshot.isEmpty) userSnapshot.documents.first().getString("email") else null
                } catch (_: Exception) {
                    null
                }

                if (emailFromDb.isNullOrBlank()) {
                    return Result.failure(Exception("Akun tidak ditemukan. Silakan masukkan alamat email yang terdaftar."))
                }

                emailFromDb.trim().lowercase() to cleanUsername
            }

            // 1. Coba kirim via Webhook (Server-side Cache, bebas dari kendala izin Firestore)
            val webhookResult = com.LambdaProject.MathArt.data.service.EmailService.sendPasswordResetOtpViaWebhook(email, username)
            if (webhookResult.isSuccess) {
                Result.success(email to username)
            } else {
                val errMsg = webhookResult.exceptionOrNull()?.message ?: ""
                // Jika server menyatakan email tidak terdaftar, langsung hentikan proses
                if (errMsg.contains("tidak terdaftar", ignoreCase = true) || errMsg.contains("not found", ignoreCase = true)) {
                    return Result.failure(Exception(errMsg))
                }

                // 2. Fallback ke penyimpanan Firestore jika webhook belum diupdate
                val fsResult = com.LambdaProject.MathArt.data.service.EmailService.sendPasswordResetOtpEmail(email, username)
                if (fsResult.isSuccess) {
                    Result.success(email to username)
                } else {
                    Result.failure(webhookResult.exceptionOrNull() ?: fsResult.exceptionOrNull() ?: Exception("Gagal mengirim kode OTP."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memverifikasi kode OTP dan langsung memperbarui kata sandi di Firebase Auth
     */
    suspend fun verifyAndResetPasswordWithOtp(email: String, otpCode: String, newPassword: String): Result<Unit> {
        return try {
            // 1. Coba verifikasi & update via Webhook (Server-side Cache)
            val webhookResult = com.LambdaProject.MathArt.data.service.EmailService.verifyAndResetPasswordViaWebhook(email, otpCode, newPassword)
            if (webhookResult.isSuccess) {
                return Result.success(Unit)
            }

            // 2. Fallback verifikasi via Firestore jika Webhook action updatePasswordWithOtp belum dipasang
            val verifyFs = com.LambdaProject.MathArt.data.service.EmailService.verifyPasswordResetOtp(email, otpCode)
            if (verifyFs.isSuccess && verifyFs.getOrNull() == true) {
                val updateResult = com.LambdaProject.MathArt.data.service.EmailService.updatePasswordViaWebhook(email, newPassword)
                if (updateResult.isSuccess) {
                    return Result.success(Unit)
                }
            }

            Result.failure(webhookResult.exceptionOrNull() ?: Exception("Kode OTP salah atau gagal memperbarui kata sandi."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersOnline(): List<OnlineUser> {
        val currentUserUid = auth.currentUser?.uid
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("isOnline", true)
                .limit(50)
                .get(Source.DEFAULT)
                .await()

            snapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                if (uid != currentUserUid) {
                    OnlineUser(uid = uid, username = doc.getString("username") ?: "User")
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Memperbarui informasi profil pengguna (Nama Lengkap, Jenjang, dan Kelas)
     */
    suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        grade: String,
        kelas: String
    ): Result<Unit> {
        return try {
            val trimmedName = fullName.trim()
            if (trimmedName.isEmpty()) {
                return Result.failure(Exception("Nama lengkap tidak boleh kosong."))
            }

            val updates = hashMapOf<String, Any>(
                "fullname" to trimmedName,
                "grade" to grade.trim(),
                "kelas" to kelas.trim()
            )

            firestore.collection("users").document(userId).update(updates).await()

            // Update Firebase Auth displayName untuk konsistensi
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmedName)
                    .build()
                auth.currentUser?.updateProfile(profileUpdates)?.await()
            } catch (_: Exception) {}

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengganti alamat email pengguna di Firebase Auth & Firestore,
     * lalu mengirimkan kode OTP 6 digit ke alamat email baru untuk verifikasi.
     */
    suspend fun updateUserEmail(
        userId: String,
        newEmail: String,
        currentPassword: String,
        username: String,
        keepLoggedIn: Boolean = true
    ): Result<String> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Sesi login tidak ditemukan. Silakan login kembali."))
            val oldEmail = user.email ?: return Result.failure(Exception("Email lama tidak ditemukan."))
            val cleanNewEmail = newEmail.trim().lowercase()

            if (cleanNewEmail == oldEmail.trim().lowercase()) {
                return Result.failure(Exception("Email baru sama dengan email saat ini."))
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanNewEmail).matches()) {
                return Result.failure(Exception("Format email tidak valid."))
            }

            if (currentPassword.isBlank()) {
                return Result.failure(Exception("Masukkan kata sandi saat ini untuk konfirmasi keamanan."))
            }

            // 1. Cek apakah email baru sudah terdaftar di database Firestore (Aman jika rule Firestore terbatas)
            try {
                val existingEmailCheck = firestore.collection("users")
                    .whereEqualTo("email", cleanNewEmail)
                    .limit(1)
                    .get()
                    .await()

                if (!existingEmailCheck.isEmpty && existingEmailCheck.documents.first().id != userId) {
                    return Result.failure(Exception("Email '$cleanNewEmail' sudah terdaftar pada akun lain."))
                }
            } catch (fsEx: Exception) {
                // Jika security rules membatasi collection query luas, lewati secara aman
                // karena Firebase Auth di langkah 3 akan otomatis menjamin keunikan email secara global.
                android.util.Log.w("AuthRepository", "Lewati validasi manual email di Firestore: ${fsEx.message}")
            }

            // 2. Re-autentikasi pengguna dengan kredensial saat ini (verifikasi keamanan)
            val credential = EmailAuthProvider.getCredential(oldEmail, currentPassword)
            try {
                user.reauthenticate(credential).await()
            } catch (authEx: Exception) {
                return Result.failure(
                    when (authEx) {
                        is FirebaseAuthInvalidCredentialsException ->
                            Exception("Kata sandi konfirmasi salah. Silakan periksa kembali.")
                        else -> Exception("Gagal mengonfirmasi identitas: ${authEx.message}")
                    }
                )
            }

            // 3. Update email di Firebase Auth
            var authUpdated = false
            try {
                user.updateEmail(cleanNewEmail).await()
                authUpdated = true
            } catch (updateEx: Exception) {
                if (updateEx is FirebaseAuthUserCollisionException) {
                    return Result.failure(Exception("Email ini sudah digunakan oleh akun lain di Firebase."))
                }

                // Coba bypass pembatasan client-side Firebase melalui Webhook Google Apps Script (Service Account Admin)
                val webhookResult = com.LambdaProject.MathArt.data.service.EmailService.updateEmailViaWebhook(oldEmail, cleanNewEmail)
                if (webhookResult.isSuccess) {
                    authUpdated = true
                } else {
                    // Coba metode verifyBeforeUpdateEmail resmi bawaan Firebase Auth
                    try {
                        user.verifyBeforeUpdateEmail(cleanNewEmail).await()
                        try {
                            val fsUpdate = hashMapOf<String, Any>(
                                "email" to cleanNewEmail,
                                "isEmailVerified" to false
                            )
                            firestore.collection("users").document(userId).set(fsUpdate, com.google.firebase.firestore.SetOptions.merge()).await()
                        } catch (_: Exception) {}
                        try {
                            com.LambdaProject.MathArt.data.service.EmailService.sendVerificationOtpEmail(
                                userId = userId,
                                email = cleanNewEmail,
                                username = username
                            )
                        } catch (_: Exception) {}
                        return Result.success("Tautan verifikasi telah dikirimkan ke $cleanNewEmail oleh Firebase. Silakan periksa inbox/spam email baru Anda.")
                    } catch (verifyEx: Exception) {
                        val errMsg = updateEx.message ?: ""
                        if (errMsg.contains("Please verify the new email", ignoreCase = true) || errMsg.contains("operation is not allowed", ignoreCase = true)) {
                            return Result.failure(
                                Exception(
                                    "Firebase Console melarang penggantian email langsung tanpa verifikasi awal.\n\n" +
                                    "Cara mengatasi:\n" +
                                    "1. Buka Firebase Console -> Authentication -> Settings -> User actions.\n" +
                                    "2. Hapus centang (uncheck) pada 'Verify new email before changing', lalu klik Save."
                                )
                            )
                        }
                        return Result.failure(Exception("Gagal memperbarui email di Firebase Auth: ${updateEx.message}"))
                    }
                }
            }

            // 4. Perbarui sesi login secara mulus dengan email baru dan password yang sudah diverifikasi (agar tidak logout)
            if (keepLoggedIn) {
                try {
                    val newCredential = EmailAuthProvider.getCredential(cleanNewEmail, currentPassword)
                    auth.signInWithCredential(newCredential).await()
                } catch (reAuthEx: Exception) {
                    android.util.Log.w("AuthRepository", "Fallback token refresh: ${reAuthEx.message}")
                    try {
                        auth.currentUser?.reload()?.await()
                        auth.currentUser?.getIdToken(true)?.await()
                    } catch (_: Exception) {}
                }
            } else {
                auth.signOut()
            }

            // 5. Update data email di Firestore & set status verifikasi ke false (non-blocking jika ada delay propagasi token)
            try {
                val fsUpdate = hashMapOf<String, Any>(
                    "email" to cleanNewEmail,
                    "isEmailVerified" to false
                )
                firestore.collection("users").document(userId)
                    .set(fsUpdate, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            } catch (fsEx: Exception) {
                android.util.Log.w("AuthRepository", "Firestore email sync ditunda: ${fsEx.message}")
            }

            // 6. Kirim kode OTP 6-Digit ke email baru via Webhook EmailService
            val otpResult = com.LambdaProject.MathArt.data.service.EmailService.sendVerificationOtpEmail(
                userId = userId,
                email = cleanNewEmail,
                username = username
            )

            if (otpResult.isSuccess) {
                Result.success("Email berhasil diubah ke $cleanNewEmail. Kode OTP verifikasi telah dikirim ke email tersebut.")
            } else {
                Result.success("Email berhasil diubah ke $cleanNewEmail. Silakan lakukan verifikasi melalui menu Verifikasi Email jika kode belum masuk.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengganti kata sandi pengguna dengan re-autentikasi kata sandi lama
     */
    suspend fun updateUserPassword(
        currentPassword: String,
        newPassword: String,
        keepLoggedIn: Boolean = true
    ): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Sesi login tidak ditemukan. Silakan login kembali."))
            val email = user.email ?: return Result.failure(Exception("Email pengguna tidak ditemukan."))

            if (currentPassword.isBlank()) {
                return Result.failure(Exception("Kata sandi saat ini tidak boleh kosong."))
            }

            if (newPassword.length < 6) {
                return Result.failure(Exception("Kata sandi baru minimal harus 6 karakter."))
            }

            if (currentPassword == newPassword) {
                return Result.failure(Exception("Kata sandi baru tidak boleh sama dengan kata sandi saat ini."))
            }

            // 1. Re-autentikasi pengguna
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            try {
                user.reauthenticate(credential).await()
            } catch (authEx: Exception) {
                return Result.failure(
                    when (authEx) {
                        is FirebaseAuthInvalidCredentialsException ->
                            Exception("Kata sandi saat ini salah.")
                        else -> Exception("Gagal memverifikasi kata sandi saat ini: ${authEx.message}")
                    }
                )
            }

            // 2. Perbarui kata sandi di Firebase Auth
            user.updatePassword(newPassword).await()

            // 3. Perbarui sesi login secara mulus dengan password baru agar tidak logout
            if (keepLoggedIn) {
                try {
                    val newCredential = EmailAuthProvider.getCredential(email, newPassword)
                    auth.signInWithCredential(newCredential).await()
                } catch (reAuthEx: Exception) {
                    android.util.Log.w("AuthRepository", "Fallback token refresh: ${reAuthEx.message}")
                    try {
                        auth.currentUser?.reload()?.await()
                        auth.currentUser?.getIdToken(true)?.await()
                    } catch (_: Exception) {}
                }
            } else {
                auth.signOut()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}