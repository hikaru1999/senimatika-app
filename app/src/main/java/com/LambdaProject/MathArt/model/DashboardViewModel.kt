package com.LambdaProject.MathArt.model

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.*
import kotlinx.coroutines.flow.*

class DashboardViewModel : ViewModel() {
    private val _materialStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val materialStatusMap: StateFlow<Map<String, Boolean>> = _materialStatusMap.asStateFlow()

    private val _hasNewNotification = MutableStateFlow(false)
    val hasNewNotification: StateFlow<Boolean> = _hasNewNotification

    private var lastSeenTime: Long = System.currentTimeMillis()

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

    fun listenForNotifications(userId: String) {
        Firebase.firestore.collection("userAchievements")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val newAchievements = snapshot.documents.any {
                        val time = it.getTimestamp("timeStamp")?.toDate()?.time ?: 0L
                        time > lastSeenTime
                    }

                    _hasNewNotification.value = newAchievements
                }
            }
    }

    fun clearNotifications() {
        lastSeenTime = System.currentTimeMillis()
        _hasNewNotification.value = false
    }
}