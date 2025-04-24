package com.LambdaProject.MathArt.model

import com.LambdaProject.MathArt.R

fun getAchievementIcon(achievementName: String): Int {
    return when (achievementName) {
        "Penjelajah" -> R.drawable.ic_explorer
        "Pemula" -> R.drawable.img_rocket_pemula
        "Profesional" -> R.drawable.img_pros
        "Ilmuan" -> R.drawable.ic_scientist
        "Math Magician" -> R.drawable.ic_magician
        "Master Aljabar" -> R.drawable.ic_algebra_master
        "Master Geometri" -> R.drawable.ic_geometry_master
        "Ahli Ramalan" -> R.drawable.ic_statistic_master
        "Ahli Matematika" -> R.drawable.ic_expert
        "Ahli Kalkulus" -> R.drawable.ic_calculus_master
        "Jenius Matematika" -> R.drawable.ic_genius
        else -> R.drawable.ic_trophy
    }
}