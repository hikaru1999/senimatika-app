package com.LambdaProject.MathArt.ViewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.Data.KuesionerRepository
import com.LambdaProject.MathArt.Data.toKuesionerResult
import com.LambdaProject.MathArt.dataStore
import com.LambdaProject.MathArt.model.Decision
import com.LambdaProject.MathArt.model.KuesionerResult
import com.LambdaProject.MathArt.model.ValidatorResponse
import com.LambdaProject.MathArt.model.ValidatorRole
import com.LambdaProject.MathArt.model.ValidatorSession
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ValidatorViewModel @Inject constructor(
    private val repository: KuesionerRepository,
    private val dataStore: DataStore<Preferences>,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var session by mutableStateOf<ValidatorSession?>(null)
        private set
    val cachedSessions = mutableStateMapOf<ValidatorRole, ValidatorSession>()

    private val _result = MutableStateFlow<KuesionerResult?>(null)
    val result: StateFlow<KuesionerResult?> = _result.asStateFlow()

    private val _submittedRole = MutableStateFlow<ValidatorRole?>(null)
    val submittedRole: StateFlow<ValidatorRole?> = _submittedRole.asStateFlow()

    fun loadSession(userId: String, role: ValidatorRole) {
        viewModelScope.launch {
            session = repository.getCachedSession(userId, role)
                ?: ValidatorSession(
                    validatorId = userId,
                    role = role.name.lowercase(),
                    responses = emptyList()
                )
        }
    }

    fun loadKuesionerResult(role: ValidatorRole) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val result = repository.getKuesionerResult(userId, role)
            _result.value = result
        }
    }

    fun loadOwnKuesionerResult(onFound: (ValidatorRole) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        viewModelScope.launch {
            for (role in ValidatorRole.entries) {
                val result = repository.getKuesionerResult(userId, role)
                if (result != null) {
                    _result.value = result
                    onFound(role)
                    break // cukup ambil satu, karena validator hanya boleh satu role
                }
            }
        }
    }

    fun cacheSessionFromUI(
        name: String,
        institution: String,
        comment: String?,
        responses: Map<String, Int>
    ) {
        val updatedSession = session?.copy(
            name = name,
            institution = institution,
            comment = comment,
            responses = responses.map { ValidatorResponse(it.key, it.value) },
            lastUpdated = System.currentTimeMillis()
        )

        updatedSession?.let {
            session = it
            viewModelScope.launch {
                repository.cacheSession(
                    userId = it.validatorId,
                    role = ValidatorRole.valueOf(it.role.uppercase()),
                    session = it
                )
            }
        }
    }

    fun checkCachedSessions(context: Context, userId: String) {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            ValidatorRole.entries.forEach { role ->
                val key = stringPreferencesKey("session_${userId}_${role.name.uppercase()}")
                Log.d("CacheRead", "🔍 Mencoba membaca session dari key=$key")
                val json = prefs[key]

                if (json != null) {
                    Log.d("ValidatorCache", "Found cache JSON for role=${role.name.uppercase()}")
                    try {
                        val session = Json.decodeFromString<ValidatorSession>(json)

                        if (session.decision == null) {
                            cachedSessions[role] = session
                            Log.d("ValidatorCache", "✅ Cached session loaded for $role: ${session.responses.size} responses")
                        } else {
                            Log.d("ValidatorCache", "ℹ️ Session for $role already submitted, not resumed")
                        }
                    } catch (e: Exception) {
                        Log.e("ValidatorCache", "❌ Failed to decode session for $role: ${e.message}")
                    }
                } else {
                    Log.d("ValidatorCache", "🚫 No session found for $role")
                }
            }
        }
    }

    fun clearCachedSession(userId: String, role: ValidatorRole) {
        viewModelScope.launch {
            val key = stringPreferencesKey("session_${userId}_${role.name.uppercase()}")
            dataStore.edit { it.remove(key) }
            cachedSessions.remove(role)
            Log.d("ValidatorCache", "🧹 Session cleared for $role")
        }
    }

    fun updateResponse(questionId: String, value: Int) {
        session = session?.copy(
            responses = session!!.responses
                .filterNot { it.questionId == questionId } + ValidatorResponse(questionId, value),
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun checkSubmittedRole(userId: String) {
        viewModelScope.launch {
            _submittedRole.value = repository.getSubmittedRole(userId)
        }
    }

    fun submit(name: String, institution: String, decision: Decision, comment: String?, deviceInfo: String?, onSuccess: () -> Unit) {
        val result = session?.toKuesionerResult(name, institution, decision, comment, deviceInfo)
        result?.let {
            viewModelScope.launch {
                repository.submitKuesionerResult(it)
                val roleEnum = try {
                    ValidatorRole.valueOf(it.role.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }

                roleEnum?.let { role ->
                    repository.cacheSession(it.userId, role, session!!.copy(comment = comment))
                }
                onSuccess()
            }
        }
    }
}
