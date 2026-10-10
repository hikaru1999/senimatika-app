package com.LambdaProject.MathArt.ViewModels

import androidx.lifecycle.ViewModel
import android.util.Log
import com.LambdaProject.MathArt.data.model.NotificationItem
import com.LambdaProject.MathArt.data.model.NotificationType
import com.LambdaProject.MathArt.data.model.getAchievementIcon
import com.LambdaProject.MathArt.data.model.getLevelNotificationIcon
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var achievementListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var levelNotifListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private val achievementItems = mutableListOf<NotificationItem>()
    private val levelNotifItems = mutableListOf<NotificationItem>()

    init {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null && userId.isNotBlank()) {
                attachListeners(userId)
            } else {
                _isLoading.value = false
                _notifications.value = emptyList()
            }
        }
        auth.addAuthStateListener(authStateListener!!)

        val currentUid = auth.currentUser?.uid
        if (currentUid != null && currentUid.isNotBlank()) {
            attachListeners(currentUid)
        }
    }

    private fun attachListeners(userId: String) {
        if (userId.isBlank()) {
            _isLoading.value = false
            return
        }

        achievementListener?.remove()
        levelNotifListener?.remove()

        // 1. Listen for achievement unlocks and level up notifications
        achievementListener = db.collection("users").document(userId).collection("achievements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationViewModel", "Achievement Firestore error: ${error.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifList = snapshot.documents.mapNotNull { doc ->
                        val isRead = doc.getBoolean("isRead") ?: false
                        if (isRead) return@mapNotNull null

                        val docType = doc.getString("type")
                        val isLevelUp = docType == "LEVEL_UP" || doc.id.startsWith("level_up_") || doc.id.startsWith("debug_lvl_")

                        val rawTimestamp = doc.get("timestamp")
                        val timestamp = when (rawTimestamp) {
                            is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                            is Long -> rawTimestamp
                            is Number -> rawTimestamp.toLong()
                            else -> System.currentTimeMillis()
                        }

                        if (isLevelUp) {
                            val title = doc.getString("title") ?: doc.getString("name") ?: "Naik Level!"
                            val message = doc.getString("message") ?: "Selamat! Kamu telah mencapai level baru."
                            val level = (doc.get("level") as? Number)?.toInt() ?: 1
                            NotificationItem(
                                id = "ach_${doc.id}",
                                title = title,
                                message = message,
                                timestamp = timestamp,
                                iconResId = getLevelNotificationIcon(level),
                                type = NotificationType.LEVEL_UP
                            )
                        } else {
                            val title = doc.getString("name") ?: doc.getString("title") ?: return@mapNotNull null
                            NotificationItem(
                                id = "ach_${doc.id}",
                                title = title,
                                message = "Hore!! Ada Lencana Baru Terbuka",
                                timestamp = timestamp,
                                iconResId = getAchievementIcon(title),
                                type = NotificationType.ACHIEVEMENT
                            )
                        }
                    }
                    synchronized(achievementItems) {
                        achievementItems.clear()
                        achievementItems.addAll(notifList)
                    }
                    updateCombinedNotifications()
                }
                _isLoading.value = false
            }

        // 2. Listen for custom notifications subcollection (if rules configured)
        levelNotifListener = db.collection("users").document(userId).collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Suppress error if subcollection is restricted by Firestore rules
                    Log.d("NotificationViewModel", "Notifications subcollection: ${error.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifList = snapshot.documents.mapNotNull { doc ->
                        val isRead = doc.getBoolean("isRead") ?: false
                        if (isRead) return@mapNotNull null

                        val title = doc.getString("title") ?: return@mapNotNull null
                        val message = doc.getString("message") ?: "Selamat atas progres levelmu!"
                        val level = (doc.get("level") as? Number)?.toInt() ?: 1
                        val rawTimestamp = doc.get("timestamp")
                        val timestamp = when (rawTimestamp) {
                            is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                            is Long -> rawTimestamp
                            is Number -> rawTimestamp.toLong()
                            else -> System.currentTimeMillis()
                        }

                        NotificationItem(
                            id = "lvl_${doc.id}",
                            title = title,
                            message = message,
                            timestamp = timestamp,
                            iconResId = getLevelNotificationIcon(level),
                            type = NotificationType.LEVEL_UP
                        )
                    }
                    synchronized(levelNotifItems) {
                        levelNotifItems.clear()
                        levelNotifItems.addAll(notifList)
                    }
                    updateCombinedNotifications()
                }
                _isLoading.value = false
            }
    }

    private fun updateCombinedNotifications() {
        val combined = mutableListOf<NotificationItem>()
        synchronized(achievementItems) { combined.addAll(achievementItems) }
        synchronized(levelNotifItems) { combined.addAll(levelNotifItems) }
        combined.sortByDescending { it.timestamp }
        _notifications.value = combined
    }

    fun deleteNotification(notification: NotificationItem) {
        val userId = auth.currentUser?.uid ?: return
        if (notification.id.startsWith("ach_")) {
            val docId = notification.id.removePrefix("ach_")
            db.collection("users").document(userId)
                .collection("achievements")
                .document(docId)
                .update("isRead", true)
        } else if (notification.id.startsWith("lvl_")) {
            val docId = notification.id.removePrefix("lvl_")
            db.collection("users").document(userId)
                .collection("notifications")
                .document(docId)
                .update("isRead", true)
        }

        _notifications.value = _notifications.value.filterNot { it.id == notification.id }
    }

    fun markAllAsRead() {
        val current = _notifications.value.toList()
        current.forEach { deleteNotification(it) }
    }

    fun markAsSeen() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .update("lastSeenNotification", com.google.firebase.firestore.FieldValue.serverTimestamp())
    }

    override fun onCleared() {
        super.onCleared()
        authStateListener?.let { auth.removeAuthStateListener(it) }
        achievementListener?.remove()
        levelNotifListener?.remove()
    }
}