package com.LambdaProject.MathArt.Data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.*
import com.LambdaProject.MathArt.model.KuesionerResult
import com.LambdaProject.MathArt.model.ValidatorRole
import com.LambdaProject.MathArt.model.ValidatorSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class KuesionerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>
) : KuesionerRepository {

    override suspend fun submitKuesionerResult(result: KuesionerResult) {
        val path = "kuesioner_results/${result.role.lowercase()}/responses"
        firestore
            .collection(path)
            .document(result.userId)
            .set(result)
            .await()
    }

    override suspend fun getSubmittedRole(userId: String): ValidatorRole? {
        val rolesToCheck = listOf(ValidatorRole.MATERI, ValidatorRole.MEDIA)
        for (role in rolesToCheck) {
            val path = "kuesioner_results/${role.name.lowercase()}/responses"
            val doc = firestore.collection(path).document(userId).get().await()
            if (doc.exists()) return role
        }
        return null
    }

    override suspend fun cacheSession(userId: String, role: ValidatorRole, session: ValidatorSession) {
        val key = stringPreferencesKey("session_${userId}_${role.name.uppercase()}")
        val json = Json.encodeToString(session)
        Log.d("CacheWrite", "✅ Menyimpan session ke key = $key. Total jawaban: ${session.responses.size}")
        dataStore.edit { prefs ->
            prefs[key] = json
        }
        delay(200)
    }

    override suspend fun getCachedSession(userId: String, role: ValidatorRole): ValidatorSession? {
        val key = stringPreferencesKey("session_${userId}_${role.name.uppercase()}")
        val prefs = dataStore.data.first()
        val json = prefs[key] ?: return null
        return try {
            Json.decodeFromString<ValidatorSession>(json)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getKuesionerResult(userId: String, role: ValidatorRole): KuesionerResult? {
        val path = "kuesioner_results/${role.name.lowercase()}/responses"
        return try {
            val snapshot = firestore
                .collection(path)
                .document(userId)
                .get()
                .await()
            Log.d("FirestoreCheck", "📄 Data snapshot = ${snapshot.data}")
            snapshot.toObject(KuesionerResult::class.java)
        } catch (e: Exception) {
            Log.e("Repo", "❌ Gagal mengambil hasil: ${e.message}")
            null
        }
    }
}
