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

    init {
        fetchNotifications()
        listenForChallengeNotifications()
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

    private fun listenForChallengeNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Firebase.firestore.collection("challenges")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationViewModel", "Firestore error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val challengeList = snapshot.documents.mapNotNull { doc ->
                        val fromUserId = doc.getString("fromUserId") ?: return@mapNotNull null
                        val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null

                        Firebase.firestore.collection("users")
                            .document(fromUserId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val username = userDoc.getString("username") ?: "Pengguna"
                                val newNotif = NotificationItem(
                                    title = "Tantangan PvP Baru!",
                                    message = "Kamu ditantang oleh $username",
                                    timestamp = timestamp,
                                    iconResId = R.drawable.ic_swords,
                                    type = NotificationType.PVP_CHALLENGE,
                                    challengeId = doc.id
                                )
                                _notifications.value = (_notifications.value + newNotif)
                                    .sortedByDescending { it.timestamp }
                            }
                    }
                }
            }
    }

    fun deleteNotification(notification: NotificationItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Firebase.firestore.collection("userAchievements")
            .whereEqualTo("userId", userId)
            .whereEqualTo("timestamp", notification.timestamp)
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    doc.reference.delete()
                }
            }

        _notifications.value = _notifications.value.filterNot { it.timestamp == notification.timestamp }
    }

}