package com.LambdaProject.MathArt.data.model

data class AchievementItem(
    val name: String = "",
    val imageRes: Int = 0,
    val description: String = "",
    val imageName: String = "obj_sack",
    val isUnlocked: Boolean = false,
    val notified: Boolean = false,
    val isRead: Boolean = false
)