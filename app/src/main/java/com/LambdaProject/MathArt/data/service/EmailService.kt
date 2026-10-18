package com.LambdaProject.MathArt.data.service

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

object EmailService {

    // URL Webhook Google Apps Script (sama dengan yang digunakan pada project dashboard-tutor)
    private const val GOOGLE_APPS_SCRIPT_URL =
        "https://script.google.com/macros/s/AKfycbzlsuYulVFgG6e1RrrM_QjoH64VqdFU_HgNNCMEZHJriUbzK5iDsx2oKX7HTdT3FXIpIg/exec"

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * Eksekusi request ke Google Apps Script Webhook dengan penanganan redirect HTTP 302/307
     */
    private fun executeWebhookRequest(
        urlStr: String,
        jsonBody: String?,
        isPost: Boolean = true,
        redirectCount: Int = 0
    ): Pair<Int, String> {
        if (redirectCount > 5) return Pair(500, "Too many redirects")
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.connectTimeout = 15000
        conn.readTimeout = 15000

        if (isPost && jsonBody != null) {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "text/plain;charset=utf-8")
            val bytes = jsonBody.toByteArray(Charsets.UTF_8)
            conn.setRequestProperty("Content-Length", bytes.size.toString())
            conn.doOutput = true
            conn.outputStream.use { os ->
                os.write(bytes)
                os.flush()
            }
        } else {
            conn.requestMethod = "GET"
        }

        val responseCode = conn.responseCode
        if (responseCode in 300..399) {
            val location = conn.getHeaderField("Location")
            if (!location.isNullOrBlank()) {
                return executeWebhookRequest(location, null, isPost = false, redirectCount = redirectCount + 1)
            }
        }

        val responseStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val responseBody = responseStream?.bufferedReader()?.use { it.readText() } ?: ""
        return Pair(responseCode, responseBody)
    }

    /**
     * Kirim email bebas via Webhook Google Apps Script (Gmail)
     */
    suspend fun sendEmailViaWebhook(to: String, subject: String, htmlContent: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("to", to.trim().lowercase())
                    put("subject", subject)
                    put("html", htmlContent)
                }.toString()

                val (code, body) = executeWebhookRequest(GOOGLE_APPS_SCRIPT_URL, json, isPost = true)
                Log.d("EmailService", "sendEmailViaWebhook result: code=$code, body=$body")

                if (code in 200..299) {
                    try {
                        val respJson = JSONObject(body)
                        respJson.optString("status") == "success"
                    } catch (_: Exception) {
                        true
                    }
                } else false
            } catch (e: Exception) {
                Log.e("EmailService", "Gagal mengirim email via Google Apps Script: ${e.message}", e)
                false
            }
        }

    /**
     * Meminta Google Apps Script untuk menghasilkan link reset resmi Firebase
     * dan mengirimkannya menggunakan template HTML Senimatika
     */
    suspend fun sendPasswordResetViaWebhook(email: String, username: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("action", "sendPasswordReset")
                    put("email", email.trim().lowercase())
                    put("username", username)
                }.toString()

                val (code, body) = executeWebhookRequest(GOOGLE_APPS_SCRIPT_URL, json, isPost = true)
                Log.d("EmailService", "sendPasswordResetViaWebhook result: code=$code, body=$body")

                if (code in 200..299) {
                    val respJson = JSONObject(body)
                    respJson.optString("status") == "success"
                } else false
            } catch (e: Exception) {
                Log.w("EmailService", "Webhook password reset tidak aktif atau fallback: ${e.message}")
                false
            }
        }

    /**
     * Generate kode angka acak 6 digit
     */
    fun generateOtpCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    // =========================================================================
    // 1. SITUASI 1: KONFIRMASI / VERIFIKASI EMAIL
    // =========================================================================

    /**
     * Mengirimkan Kode OTP 6-Digit Verifikasi Email dengan Template HTML Senimatika
     */
    suspend fun sendVerificationOtpEmail(
        userId: String,
        email: String,
        username: String = "Pelajar"
    ): Result<String> {
        return try {
            val otpCode = generateOtpCode()
            val expiresAt = System.currentTimeMillis() + (10 * 60 * 1000) // 10 menit
            val cleanEmail = email.trim().lowercase()

            // 1. Simpan OTP ke document user di Firestore jika memungkinkan (non-blocking jika ada delay token auth)
            try {
                val otpUpdate = hashMapOf<String, Any>(
                    "verificationOtpCode" to otpCode,
                    "verificationOtpExpiresAt" to expiresAt
                )
                firestore.collection("users").document(userId)
                    .set(otpUpdate, com.google.firebase.firestore.SetOptions.merge())
                    .await()
            } catch (fsEx: Exception) {
                Log.w("EmailService", "Peringatan simpan OTP di Firestore: ${fsEx.message}")
            }

            // 2. Susun template HTML Senimatika
            val subject = "[Senimatika] Kode Verifikasi Email Akun - $otpCode"
            val htmlContent = buildEmailVerificationHtml(username, otpCode)

            // 3. Kirim via Webhook Google Apps Script (membawa payload komplit agar kompatibel baik webhook baru maupun lama)
            val sent = sendVerificationOtpViaWebhook(userId, cleanEmail, otpCode, subject, htmlContent)
            if (sent) {
                Result.success("Kode verifikasi 6 digit telah dikirim ke $email")
            } else {
                Log.e("EmailService", "Gagal mengirim email via webhook untuk: $email")
                Result.failure(Exception("Gagal mengirim email melalui server mail."))
            }
        } catch (e: Exception) {
            Log.e("EmailService", "Error saat sendVerificationOtpEmail: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Memverifikasi kode OTP yang diinputkan pengguna di aplikasi
     * Mendukung verifikasi ganda: Firestore lokal & Webhook Google Apps Script (CacheService)
     */
    suspend fun verifyEmailOtp(userId: String, inputCode: String, email: String = ""): Result<Boolean> {
        return try {
            val cleanCode = inputCode.trim()

            // 1. Cek dari Firestore lokal jika tersimpan
            var verified = false
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    val savedCode = doc.getString("verificationOtpCode") ?: ""
                    val expiresAt = doc.getLong("verificationOtpExpiresAt") ?: 0L

                    if (savedCode.isNotEmpty() && System.currentTimeMillis() <= expiresAt && savedCode.trim() == cleanCode) {
                        verified = true
                        val updates = hashMapOf<String, Any>(
                            "isEmailVerified" to true,
                            "verificationOtpCode" to FieldValue.delete(),
                            "verificationOtpExpiresAt" to FieldValue.delete()
                        )
                        try {
                            firestore.collection("users").document(userId).update(updates).await()
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w("EmailService", "Verifikasi Firestore lokal dilewati: ${e.message}")
            }

            if (verified) {
                return Result.success(true)
            }

            // 2. Cek verifikasi via Webhook Google Apps Script (CacheService / Service Account)
            val webhookVerifyResult = verifyEmailOtpViaWebhook(userId, email, cleanCode)
            if (webhookVerifyResult.isSuccess && webhookVerifyResult.getOrNull() == true) {
                // Pastikan state lokal Firestore juga diperbarui jika memungkinkan
                try {
                    firestore.collection("users").document(userId).update("isEmailVerified", true).await()
                } catch (_: Exception) {}
                return Result.success(true)
            }

            Result.failure(webhookVerifyResult.exceptionOrNull() ?: Exception("Kode verifikasi salah atau telah kedaluwarsa."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengirimkan OTP verifikasi email via Google Apps Script (dan menyimpan di CacheService)
     */
    private suspend fun sendVerificationOtpViaWebhook(
        userId: String,
        email: String,
        otpCode: String,
        subject: String,
        htmlContent: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("action", "sendEmailVerificationOtp")
                put("userId", userId)
                put("email", email.trim().lowercase())
                put("to", email.trim().lowercase())
                put("otpCode", otpCode)
                put("subject", subject)
                put("html", htmlContent)
            }.toString()

            val (code, body) = executeWebhookRequest(GOOGLE_APPS_SCRIPT_URL, json, isPost = true)
            Log.d("EmailService", "sendVerificationOtpViaWebhook result: code=$code, body=$body")

            if (code in 200..299) {
                try {
                    val respJson = JSONObject(body)
                    respJson.optString("status") == "success"
                } catch (_: Exception) {
                    true
                }
            } else false
        } catch (e: Exception) {
            Log.e("EmailService", "Gagal mengirim verification OTP via webhook: ${e.message}", e)
            false
        }
    }

    /**
     * Memverifikasi kode OTP email via Google Apps Script Webhook
     */
    suspend fun verifyEmailOtpViaWebhook(userId: String, email: String, otpCode: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("action", "verifyEmailOtp")
                    put("userId", userId)
                    put("email", email.trim().lowercase())
                    put("otpCode", otpCode.trim())
                }.toString()

                val (code, body) = executeWebhookRequest(GOOGLE_APPS_SCRIPT_URL, json, isPost = true)
                Log.d("EmailService", "verifyEmailOtpViaWebhook result: code=$code, body=$body")

                if (code in 200..299) {
                    val respJson = JSONObject(body)
                    if (respJson.optString("status") == "success") {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(respJson.optString("message", "Kode verifikasi tidak cocok.")))
                    }
                } else {
                    Result.failure(Exception("Gagal menghubungi server verifikasi (HTTP $code)."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // =========================================================================
    // 2. SITUASI 2: LUPA PASSWORD (RESET KATA SANDI)
    // =========================================================================

    /**
     * Mengirimkan Kode OTP Reset Kata Sandi dengan Template HTML Senimatika
     */
    suspend fun sendPasswordResetOtpEmail(
        email: String,
        username: String = "Pelajar"
    ): Result<String> {
        return try {
            val otpCode = generateOtpCode()
            val expiresAt = System.currentTimeMillis() + (10 * 60 * 1000) // 10 menit
            val sanitizedEmail = email.trim().lowercase()

            val otpData = hashMapOf(
                "email" to sanitizedEmail,
                "code" to otpCode,
                "expiresAt" to expiresAt,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("password_resets").document(sanitizedEmail).set(otpData).await()

            val subject = "[Senimatika] Kode Pemulihan Kata Sandi Akun - $otpCode"
            val htmlContent = buildPasswordResetHtml(username, otpCode)

            val sent = sendEmailViaWebhook(sanitizedEmail, subject, htmlContent)
            if (sent) {
                Result.success("Kode reset kata sandi telah dikirim ke $email")
            } else {
                Result.failure(Exception("Gagal mengirim email reset melalui server mail."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memverifikasi kode OTP Lupa Password
     */
    suspend fun verifyPasswordResetOtp(email: String, inputCode: String): Result<Boolean> {
        return try {
            val sanitizedEmail = email.trim().lowercase()
            val doc = firestore.collection("password_resets").document(sanitizedEmail).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Kode reset belum diminta atau sudah kedaluwarsa."))
            }

            val savedCode = doc.getString("code") ?: ""
            val expiresAt = doc.getLong("expiresAt") ?: 0L

            if (System.currentTimeMillis() > expiresAt) {
                return Result.failure(Exception("Kode reset sudah kedaluwarsa. Silakan minta kode baru."))
            }

            if (savedCode.trim() != inputCode.trim()) {
                return Result.failure(Exception("Kode reset tidak cocok. Silakan periksa kembali."))
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memperbarui kata sandi pengguna langsung di Firebase Auth melalui Google Apps Script Webhook
     */
    suspend fun updatePasswordViaWebhook(email: String, newPassword: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("action", "updatePassword")
                    put("email", email.trim().lowercase())
                    put("newPassword", newPassword)
                }.toString()

                val (code, body) = executeWebhookRequest(GOOGLE_APPS_SCRIPT_URL, json, isPost = true)
                Log.d("EmailService", "updatePasswordViaWebhook result: code=$code, body=$body")

                if (code in 200..299) {
                    val respJson = JSONObject(body)
                    if (respJson.optString("status") == "success") {
                        // Bersihkan dokumen OTP setelah berhasil reset kata sandi jika ada
                        try {
                            firestore.collection("password_resets").document(email.trim().lowercase()).delete()
                        } catch (_: Exception) {}
                        Result.success(true)
                    } else {
                        Result.failure(Exception(respJson.optString("message", "Gagal memperbarui kata sandi.")))
                    }
                } else {
                    Result.failure(Exception("Gagal menghubungi server pembaruan kata sandi (HTTP $code)."))
                }
            } catch (e: Exception) {
                Log.e("EmailService", "Error saat updatePasswordViaWebhook: ${e.message}", e)
                Result.failure(e)
            }
        }

    /**
     * Memperbarui email pengguna di Firebase Auth melalui Google Apps Script Webhook (Service Account)
     */
    suspend fun updateEmailViaWebhook(oldEmail: String, newEmail: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("action", "updateEmail")
                    put("oldEmail", oldEmail.trim().lowercase())
                    put("newEmail", newEmail.trim().lowercase())
                }.toString()

                val (code, body) = executeWebhookRequest(GOOGLE_APPS_SCRIPT_URL, json, isPost = true)
                Log.d("EmailService", "updateEmailViaWebhook result: code=$code, body=$body")

                if (code in 200..299) {
                    val respJson = JSONObject(body)
                    if (respJson.optString("status") == "success") {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(respJson.optString("message", "Gagal memperbarui email via webhook.")))
                    }
                } else {
                    Result.failure(Exception("Gagal menghubungi mail server (HTTP $code)."))
                }
            } catch (e: Exception) {
                Log.e("EmailService", "Error saat updateEmailViaWebhook: ${e.message}", e)
                Result.failure(e)
            }
        }

    /**
     * Mengirimkan kode OTP Reset Password via Google Apps Script (Bebas dari kendala permission Firestore)
     */
    suspend fun sendPasswordResetOtpViaWebhook(
        email: String,
        username: String = "Pelajar"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("action", "sendPasswordResetOtp")
                put("email", email.trim().lowercase())
                put("username", username)
            }.toString()

            val (code, body) = executeWebhookRequest(GOOGLE_APPS_SCRIPT_URL, json, isPost = true)
            Log.d("EmailService", "sendPasswordResetOtpViaWebhook result: code=$code, body=$body")

            if (code in 200..299) {
                if (body.trim().startsWith("<")) {
                    Result.failure(Exception("Server mengembalikan HTML (Error Script atau Login Google). Periksa kode script dan akses deployment."))
                } else {
                    val respJson = JSONObject(body)
                    if (respJson.optString("status") == "success") {
                        Result.success(respJson.optString("message", "Kode OTP berhasil dikirim"))
                    } else {
                        Result.failure(Exception(respJson.optString("message", "Gagal mengirim kode OTP.")))
                    }
                }
            } else {
                Result.failure(Exception("Gagal menghubungi server mail (HTTP $code)."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memverifikasi kode OTP dan mengubah kata sandi via Google Apps Script (Server-side)
     */
    suspend fun verifyAndResetPasswordViaWebhook(
        email: String,
        otpCode: String,
        newPassword: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("action", "updatePasswordWithOtp")
                put("email", email.trim().lowercase())
                put("otpCode", otpCode.trim())
                put("newPassword", newPassword)
            }.toString()

            val (code, body) = executeWebhookRequest(GOOGLE_APPS_SCRIPT_URL, json, isPost = true)
            Log.d("EmailService", "verifyAndResetPasswordViaWebhook result: code=$code, body=$body")

            if (code in 200..299) {
                if (body.trim().startsWith("<")) {
                    Result.failure(Exception("Server mengembalikan HTML. Periksa kode script dan akses deployment."))
                } else {
                    val respJson = JSONObject(body)
                    if (respJson.optString("status") == "success") {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(respJson.optString("message", "Gagal memperbarui kata sandi.")))
                    }
                }
            } else {
                Result.failure(Exception("Gagal menghubungi server pembaruan kata sandi (HTTP $code)."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // TEMPLATE HTML RESMI SENIMATIKA
    // =========================================================================

    private fun buildEmailVerificationHtml(username: String, otpCode: String): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>Verifikasi Email Akun Senimatika</title>
        </head>
        <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f1f5f9; color: #334155; margin: 0; padding: 28px 12px;">
          <div style="max-width: 560px; margin: 0 auto; background-color: #ffffff; border-radius: 24px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 10px 25px rgba(15, 23, 42, 0.05);">
            
            <!-- Header Top Logo -->
            <div style="padding: 36px 36px 12px 36px; text-align: center;">
              <table cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto; display: inline-table;">
                <tr>
                  <td style="vertical-align: middle;">
                    <img src="https://i.ibb.co/Ld3wBCHX/img-logo-wo-name.png" alt="Logo Senimatika" width="36" height="36" style="display: block; width: 36px; height: 36px; border-radius: 8px; object-fit: contain; border: 0;" />
                  </td>
                  <td style="padding-left: 10px; vertical-align: middle; text-align: left;">
                    <span style="font-family: Arial, sans-serif; font-size: 26px; font-weight: 900; color: #0f172a; letter-spacing: -0.5px;">Senimatika</span>
                  </td>
                </tr>
              </table>
            </div>

            <!-- Main Content Area -->
            <div style="padding: 12px 36px 36px 36px; text-align: center;">
              <!-- Category Eyebrow -->
              <div style="color: #2563eb; font-size: 11.5px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 12px;">
                VERIFIKASI AKUN
              </div>

              <!-- Main Headline with Italic Accent -->
              <h1 style="color: #0f172a; font-size: 27px; font-weight: 900; line-height: 1.3; margin: 0 0 16px 0; letter-spacing: -0.5px;">
                Konfirmasi Email untuk <span style="color: #2563eb;">Akun Senimatikamu</span>
              </h1>

              <!-- Subtext / Description -->
              <p style="font-size: 14.5px; line-height: 1.65; color: #64748b; margin: 0 0 24px 0;">
                Halo, <strong style="color: #0f172a;">${username}</strong>!<br/>
                Terima kasih telah bergabung di <strong>Senimatika</strong>. Untuk menyelesaikan pendaftaran dan mengamankan akunmu, gunakan kode 6-digit di bawah ini pada aplikasi:
              </p>

              <!-- OTP Card Box (Bever Style) -->
              <div style="background-color: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 16px; padding: 22px 16px; margin: 24px 0; text-align: center;">
                <div style="font-size: 40px; font-weight: 900; letter-spacing: 10px; color: #0f172a; font-family: 'Courier New', Courier, monospace; margin: 0;">
                  ${otpCode}
                </div>
                <div style="font-size: 12px; color: #64748b; margin-top: 10px; font-weight: 600;">
                  <span style="display: inline-block; width: 6px; height: 6px; background-color: #2563eb; border-radius: 50%; margin-right: 6px; vertical-align: middle;"></span>Berlaku selama 10 menit
                </div>
              </div>

              <!-- Sub-message -->
              <p style="font-size: 12.5px; color: #94a3b8; line-height: 1.6; margin: 0;">
                Tidak merasa mendaftar akun di Senimatika? Jangan khawatir &mdash; abaikan saja email ini. Akunmu tetap aman.
              </p>
            </div>

            <!-- Dark Navy Footer (Bever Style) -->
            <div style="background-color: #0b0f19; padding: 28px 36px; text-align: center; border-top: 1px solid #1e293b;">
              <table cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto 8px auto; display: inline-table;">
                <tr>
                  <td style="vertical-align: middle;">
                    <img src="https://i.ibb.co/fYTq0DHt/logo-white.png" alt="Logo Senimatika" width="22" height="22" style="display: block; width: 22px; height: 22px; border-radius: 5px; object-fit: contain; border: 0;" />
                  </td>
                  <td style="padding-left: 8px; vertical-align: middle; text-align: left;">
                    <span style="font-family: Arial, sans-serif; font-size: 18px; font-weight: 900; color: #ffffff; letter-spacing: 0.5px;">Senimatika</span>
                  </td>
                </tr>
              </table>
              <p style="font-size: 12px; color: #94a3b8; margin: 0 0 14px 0;">
                Platform Pembelajaran Matematika Interaktif
              </p>
              <div style="font-size: 12px; margin-bottom: 14px;">
                <a href="https://senimatika-eduapp.web.app" style="color: #38bdf8; text-decoration: none; font-weight: 600;">senimatika-eduapp.web.app</a>
              </div>
              <p style="font-size: 11px; color: #64748b; margin: 0;">
                &copy; 2026 Senimatika. Hak Cipta Dilindungi.
              </p>
            </div>

          </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun buildPasswordResetHtml(username: String, otpCode: String): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>Pemulihan Kata Sandi Akun Senimatika</title>
        </head>
        <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f1f5f9; color: #334155; margin: 0; padding: 28px 12px;">
          <div style="max-width: 560px; margin: 0 auto; background-color: #ffffff; border-radius: 24px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 10px 25px rgba(15, 23, 42, 0.05);">
            
            <!-- Header Top Logo -->
            <div style="padding: 36px 36px 12px 36px; text-align: center;">
              <table cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto; display: inline-table;">
                <tr>
                  <td style="vertical-align: middle;">
                    <img src="https://i.ibb.co/Ld3wBCHX/img-logo-wo-name.png" alt="Logo Senimatika" width="36" height="36" style="display: block; width: 36px; height: 36px; border-radius: 8px; object-fit: contain; border: 0;" />
                  </td>
                  <td style="padding-left: 10px; vertical-align: middle; text-align: left;">
                    <span style="font-family: Arial, sans-serif; font-size: 26px; font-weight: 900; color: #0f172a; letter-spacing: -0.5px;">Senimatika</span>
                  </td>
                </tr>
              </table>
            </div>

            <!-- Main Content Area -->
            <div style="padding: 12px 36px 36px 36px; text-align: center;">
              <!-- Category Eyebrow -->
              <div style="color: #2563eb; font-size: 11.5px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 12px;">
                PEMULIHAN KATA SANDI
              </div>

              <!-- Main Headline with Italic Accent -->
              <h1 style="color: #0f172a; font-size: 27px; font-weight: 900; line-height: 1.3; margin: 0 0 16px 0; letter-spacing: -0.5px;">
                Atur Ulang Kata Sandi untuk <span style="color: #2563eb;">Akun Senimatikamu</span>
              </h1>

              <!-- Subtext / Description -->
              <p style="font-size: 14.5px; line-height: 1.65; color: #64748b; margin: 0 0 24px 0;">
                Halo, <strong style="color: #0f172a;">${username}</strong>!<br/>
                Kami menerima permintaan untuk mengatur ulang kata sandi akun Senimatika kamu. Masukkan kode 6-digit berikut di aplikasi untuk membuat kata sandi baru:
              </p>

              <!-- OTP Card Box (Bever Style) -->
              <div style="background-color: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 16px; padding: 22px 16px; margin: 24px 0; text-align: center;">
                <div style="font-size: 40px; font-weight: 900; letter-spacing: 10px; color: #0f172a; font-family: 'Courier New', Courier, monospace; margin: 0;">
                  ${otpCode}
                </div>
                <div style="font-size: 12px; color: #64748b; margin-top: 10px; font-weight: 600;">
                  <span style="display: inline-block; width: 6px; height: 6px; background-color: #2563eb; border-radius: 50%; margin-right: 6px; vertical-align: middle;"></span>Berlaku selama 10 menit
                </div>
              </div>

              <!-- Sub-message -->
              <p style="font-size: 12.5px; color: #94a3b8; line-height: 1.6; margin: 0;">
                <strong>Perhatian:</strong> Jangan berikan kode ini kepada siapa pun. Jika kamu tidak meminta pengaturan ulang kata sandi, abaikan email ini.
              </p>
            </div>

            <!-- Dark Navy Footer (Bever Style) -->
            <div style="background-color: #0b0f19; padding: 28px 36px; text-align: center; border-top: 1px solid #1e293b;">
              <table cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto 8px auto; display: inline-table;">
                <tr>
                  <td style="vertical-align: middle;">
                    <img src="https://i.ibb.co/fYTq0DHt/logo-white.png" alt="Logo Senimatika" width="22" height="22" style="display: block; width: 22px; height: 22px; border-radius: 5px; object-fit: contain; border: 0;" />
                  </td>
                  <td style="padding-left: 8px; vertical-align: middle; text-align: left;">
                    <span style="font-family: Arial, sans-serif; font-size: 18px; font-weight: 900; color: #ffffff; letter-spacing: 0.5px;">Senimatika</span>
                  </td>
                </tr>
              </table>
              <p style="font-size: 12px; color: #94a3b8; margin: 0 0 14px 0;">
                Platform Pembelajaran Matematika Interaktif
              </p>
              <div style="font-size: 12px; margin-bottom: 14px;">
                <a href="https://senimatika-eduapp.web.app" style="color: #38bdf8; text-decoration: none; font-weight: 600;">senimatika-eduapp.web.app</a>
              </div>
              <p style="font-size: 11px; color: #64748b; margin: 0;">
                &copy; 2026 Senimatika. Hak Cipta Dilindungi.
              </p>
            </div>

          </div>
        </body>
        </html>
        """.trimIndent()
    }
}
