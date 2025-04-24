package com.LambdaProject.MathArt.model

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

fun unlockPemulaAchievement(userId: String) {
    val db = Firebase.firestore
    val achievementsRef = db.collection("userAchievements")
    val achievementName = "Pemula"

    achievementsRef
        .whereEqualTo("userId", userId)
        .whereEqualTo("achievementName", achievementName)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                val newAchievement = hashMapOf(
                    "userId" to userId,
                    "achievementName" to achievementName,
                    "timestamp" to System.currentTimeMillis()
                )
                achievementsRef.add(newAchievement)
            } else {
                Log.d("Achievement", "Lencana '$achievementName' sudah terbuka")
            }
        }
}

fun unlockProAchievement(userId: String) {
    val db = Firebase.firestore
    val achievementsRef = db.collection("userAchievements")
    val achievementName = "Profesional"

    achievementsRef
        .whereEqualTo("userId", userId)
        .whereEqualTo("achievementName", achievementName)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                val newAchievement = hashMapOf(
                    "userId" to userId,
                    "achievementName" to achievementName,
                    "timestamp" to System.currentTimeMillis()
                )
                achievementsRef.add(newAchievement)
            } else {
                Log.d("Achievement", "Lencana '$achievementName' sudah terbuka")
            }
        }
}

fun unlockExplorerAchievement(userId: String, studyDuration: Long) {
    val db = Firebase.firestore
    val achievementsRef = db.collection("userAchievements")
    val achievementName = "Penjelajah"

    if (studyDuration >= 600_000) {
        achievementsRef
            .whereEqualTo("userId", userId)
            .whereEqualTo("achievementName", achievementName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val newAchievement = hashMapOf(
                        "userId" to userId,
                        "achievementName" to achievementName,
                        "timestamp" to System.currentTimeMillis()
                    )
                    achievementsRef.add(newAchievement)
                } else {
                    Log.d("Achievement", "Lencana '$achievementName' sudah terbuka")
                }
            }
    }
}