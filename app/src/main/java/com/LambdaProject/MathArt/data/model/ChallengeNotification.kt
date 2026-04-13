package com.LambdaProject.MathArt.data.model

import com.LambdaProject.MathArt.R

data class ChallengeNotification (
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val iconResId: Int = R.drawable.ic_swords
)