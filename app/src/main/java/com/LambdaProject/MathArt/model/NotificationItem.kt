package com.LambdaProject.MathArt.model

data class NotificationItem(
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val iconResId: Int,
    val type: NotificationType = NotificationType.ACHIEVEMENT,
    val challengeId: String? = null
)

enum class NotificationType {
    ACHIEVEMENT,
    PVP_CHALLENGE
}