package com.LambdaProject.MathArt.ViewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.LambdaProject.MathArt.Data.sampleMaterials
import com.LambdaProject.MathArt.model.MaterialItem
import com.LambdaProject.MathArt.model.StudyDurationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class  ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _username = mutableStateOf("User")
    val username: State<String> = _username

    private val _email = mutableStateOf("Email Tidak Diketahui")
    val email: State<String> = _email

    private val _studyDuration = mutableLongStateOf(0L)
    val studyDuration: State<Long> = _studyDuration

    private val _activeSessions = mutableStateListOf<MaterialItem>()
    val activeSessions: List<MaterialItem> = _activeSessions

    private val _unlockedAchievements = mutableStateListOf<String>()
    val unlockedAchievements: List<String> = _unlockedAchievements

    val userId = auth.currentUser?.uid ?: ""

    init {
        loadUserProfile()
        loadActiveSessions()
        observeStudyDuration()
        observeAchievements()
    }

    private fun loadUserProfile() {
        val uid = userId
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _username.value = doc.getString("username") ?: "User"
                _email.value = doc.getString("email") ?: "Email Tidak Diketahui"
            }
    }

    private fun loadActiveSessions() {
        firestore.collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { docs ->
                val materialIds = docs.mapNotNull { it.getString("materialId") }
                _activeSessions.clear()
                _activeSessions.addAll(sampleMaterials.filter { it.id in materialIds })
            }
    }

    private fun observeStudyDuration() {
        StudyDurationManager.observeStudyDuration(userId) {
            _studyDuration.value = it
        }
    }

    private fun observeAchievements() {
        firestore.collection("userAchievements")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e == null && snapshots != null) {
                    _unlockedAchievements.clear()
                    _unlockedAchievements.addAll(
                        snapshots.mapNotNull { it.getString("achievementName") }
                    )
                }
            }
    }

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}