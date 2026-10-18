package com.LambdaProject.MathArt.data.model

data class MaterialItem(
    val id: String,
    val title: String,
    val imageRes: Int = 0,
    val classLevel: String = "",
    val coverUrl: String? = null,
    val order: Int = 0,
    val isDraft: Boolean = false,
    val status: String = "published",
    val category: String = "Geometri",
    val exp: Int = 100,
    val isHighlight: Boolean = false
)