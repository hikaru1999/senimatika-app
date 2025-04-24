package com.LambdaProject.MathArt.model

data class NotificationItem(
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val iconResId: Int
)