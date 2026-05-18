package com.LambdaProject.MathArt.ViewModels

import androidx.lifecycle.ViewModel
import android.util.Log
import com.LambdaProject.MathArt.data.model.NotificationItem
import com.LambdaProject.MathArt.data.model.getAchievementIcon
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.model.NotificationType

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("achievements")
            .whereEqualTo("isRead", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationViewModel", "Firestore error: ${error.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifList = snapshot.documents.mapNotNull { doc ->
                        val title = doc.getString("name") ?: return@mapNotNull null
                        
                        val rawTimestamp = doc.get("timestamp")
                        val timestamp = when (rawTimestamp) {
                            is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                            is Long -> rawTimestamp
                            is Number -> rawTimestamp.toLong()
                            else -> System.currentTimeMillis()
                        }

                        NotificationItem(
                            id = doc.id,
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

    fun deleteNotification(notification: NotificationItem) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("achievements")
            .document(notification.id)
            .update("isRead", true)
            .addOnSuccessListener {
                Log.d("Notification", "Notifikasi ${notification.title} ditandai sudah dibaca")
            }

        _notifications.value = _notifications.value.filterNot { it.id == notification.id }
    }

}