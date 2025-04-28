package com.LambdaProject.MathArt.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.LambdaProject.MathArt.model.MaterialItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.flow.*
import java.util.Date

class DashboardViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _materialStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val materialStatusMap: StateFlow<Map<String, Boolean>> = _materialStatusMap.asStateFlow()

    private val _hasNewNotification = MutableLiveData(false)
    val hasNewNotification: LiveData<Boolean> = _hasNewNotification

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                _username.value = doc.getString("username") ?: "User"
                _email.value = doc.getString("email") ?: "Email Tidak Diketahui"
            }
    }

    fun checkActiveSessions(userId: String, materials: List<MaterialItem>) {
        FirebaseFirestore.getInstance()
            .collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _materialStatusMap.value = materials.associate { it.id to false }
                    return@addSnapshotListener
                }
                val activeIds = snapshot.documents.mapNotNull { it.getString("materialId") }
                val statusMap = materials.associate { it.id to (it.id in activeIds) }
                _materialStatusMap.value = statusMap
            }
    }

    fun listenForNewNotifications(userId: String) {
        val userDocRef = Firebase.firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { userSnapshot ->
            val lastSeen = userSnapshot.getTimestamp("lastSeenNotification")?.toDate() ?: Date(0)

            Firebase.firestore.collection("userAchievements")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val latest = snapshot.documents.first()
                        val newTimestamp = latest.getLong("timestamp") ?: 0L
                        if (Date(newTimestamp) > lastSeen) {
                            _hasNewNotification.value = true
                        } else {
                            _hasNewNotification.value = false
                        }
                    }
                }
        }
    }

    fun clearNotifications() {
        _hasNewNotification.value = false
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users").document(userId)
            .update("lastSeenNotification", FieldValue.serverTimestamp())
    }
}