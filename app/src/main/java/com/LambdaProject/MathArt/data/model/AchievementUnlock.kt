package com.LambdaProject.MathArt.data.model

import android.util.Log
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.DataAchievements
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore

fun unlockGeneralAchievement(userId: String, achievementName: String) {
    if (userId.isEmpty()) return

    val db = Firebase.firestore
    val achievementRef = db.collection("users").document(userId)
        .collection("achievements").document(achievementName)

    achievementRef.get().addOnSuccessListener { document ->
        if (!document.exists()) {
            val localData = DataAchievements.find { it.name == achievementName }
            val imageName = when(achievementName) {
                "Penjelajah" -> "ic_explorer"
                "Pemula" -> "img_rocket_pemula"
                "Profesional" -> "img_pros"
                "Ilmuwan" -> "ic_scientist"
                "Math Magician" -> "ic_magician"
                "Master Aljabar" -> "ic_algebra_master"
                "Master Geometri" -> "ic_geometry_master"
                "Ahli Ramalan" -> "ic_statistic_master"
                "Ahli Matematika" -> "ic_expert"
                "Ahli Kalkulus" -> "ic_calculus_master"
                "Jenius Matematika" -> "ic_genius"
                "Apprentice No More" -> "ic_beginner_exploration"
                "The Fortune Finder" -> "ic_treasure_hunter_1"
                "No More Lessons" -> "ic_boss_hunter_1"
                "Indiana Jones’ Grocery List" -> "ic_treasure_hunter_2"
                "Who is the Boss Now?" -> "ic_boss_hunter_2"
                "Beyond the Guided Path" -> "ic_master_exploration"
                "Lone Wolf Archivist" -> "ic_wolf"
                else -> "ic_coin"
            }

            val newAchievement = hashMapOf(
                "name" to achievementName,
                "imageRes" to getAchievementIcon(imageName),
                "imageName" to imageName,
                "timestamp" to Timestamp.now(),
                "isUnlocked" to true,
                "isRead" to false,
                "notified" to false,
            )

            achievementRef.set(newAchievement)
                .addOnSuccessListener {
                    Log.d("Firestore", "Achievement '$achievementName' disimpan di path users/$userId/achievements")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Gagal simpan achievement", e)
                }
        }
    }
}

fun unlockPemulaAchievement(userId: String) {
    unlockGeneralAchievement(userId, "Pemula")
}

fun unlockProAchievement(userId: String) {
    unlockGeneralAchievement(userId, "Profesional")
}

fun unlockExplorerAchievement(userId: String, studyDuration: Long) {
    if (studyDuration >= 600_000) {
        unlockGeneralAchievement(userId, "Penjelajah")
    }
}

//fun getAchievementIcon(imageName: String?): Int {
//    return when (imageName) {
//        "ic_explorer" -> R.drawable.ic_explorer
//        "img_rocket_pemula" -> R.drawable.img_rocket_pemula
//        "img_pros" -> R.drawable.img_pros
//        "ic_scientist" -> R.drawable.ic_scientist
//        "ic_magician" -> R.drawable.ic_magician
//        "ic_algebra_master" -> R.drawable.ic_algebra_master
//        "ic_geometry_master" -> R.drawable.ic_geometry_master
//        "ic_statistic_master" -> R.drawable.ic_statistic_master
//        "ic_expert" -> R.drawable.ic_expert
//        "ic_calculus_master" -> R.drawable.ic_calculus_master
//        "ic_genius" -> R.drawable.ic_genius
//        "ic_beginner_exploration" -> R.drawable.ic_beginner_exploration
//        "ic_treasure_hunter_1" -> R.drawable.ic_treasure_hunter_1
//        "ic_boss_hunter_1" -> R.drawable.ic_boss_hunter_1
//        "ic_treasure_hunter_2" -> R.drawable.ic_treasure_hunter_2
//        "ic_boss_hunter_2" -> R.drawable.ic_boss_hunter_2
//        "ic_master_exploration" -> R.drawable.ic_master_exploration
//        "ic_wolf" -> R.drawable.ic_wolf
//        else -> R.drawable.obj_sack
//    }
//}