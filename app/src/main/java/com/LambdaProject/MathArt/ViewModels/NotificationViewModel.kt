package com.LambdaProject.MathArt.ViewModels

import androidx.lifecycle.ViewModel
import android.util.Log
import com.LambdaProject.MathArt.model.NotificationItem
import com.LambdaProject.MathArt.model.getAchievementIcon
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Firebase.firestore.collection("userAchievements")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationViewModel", "Firestore error: ${error.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifList = snapshot.documents.mapNotNull { doc ->
                        val title = doc.getString("achievementName") ?: return@mapNotNull null
                        val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                        NotificationItem(
                            title = title,
                            message = "Hore!! Ada Lencana Baru Terbuka",
                            timestamp = timestamp,
                            iconResId = getAchievementIcon(title)
                        )
                    }
                    _notifications.value = notifList
                }
                _isLoading.value = false
            }
    }
}