package com.LambdaProject.MathArt.data.model

data class CategoryItem(
    val name: String,
    val imageRes: Int = 0,
    val isLocked: Boolean = false,
    val id: String = "",
    val iconUrl: String? = null,
    val order: Int = 0,
    val materialCount: Int = 0,
    val description: String = ""
)