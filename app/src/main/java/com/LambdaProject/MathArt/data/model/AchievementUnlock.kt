package com.LambdaProject.MathArt.data.model

import android.util.Log
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.DataAchievements
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

private val inFlightUnlocks = ConcurrentHashMap.newKeySet<String>()

fun unlockGeneralAchievement(userId: String, achievementName: String) {
    if (userId.isEmpty() || achievementName.isEmpty()) return
    val lockKey = "$userId-$achievementName"
    if (!inFlightUnlocks.add(lockKey)) {
        // Mencegah duplicate call yang bersamaan (race condition)
        return
    }

    val db = Firebase.firestore
    val achievementRef = db.collection("users").document(userId)
        .collection("achievements").document(achievementName)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val shouldGrantExp = db.runTransaction { transaction ->
                val snapshot = transaction.get(achievementRef)
                if (snapshot.exists()) {
                    return@runTransaction false
                }

                val localData = DataAchievements.find { it.name == achievementName }
                val expReward = localData?.exp ?: 100
                val newAchievement = hashMapOf(
                    "name" to achievementName,
                    "imageRes" to (localData?.imageRes ?: R.drawable.obj_sack),
                    "timestamp" to Timestamp.now(),
                    "isUnlocked" to true,
                    "isRead" to false,
                    "notified" to false,
                    "exp" to expReward
                )

                transaction.set(achievementRef, newAchievement)
                true
            }.await()

            if (shouldGrantExp) {
                val localData = DataAchievements.find { it.name == achievementName }
                val expReward = localData?.exp ?: 100
                Log.d("Firestore", "Achievement '$achievementName' sukses disimpan dengan EXP: $expReward")
                grantExpToUser(userId, expReward, "Achievement: $achievementName")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Gagal simpan achievement $achievementName", e)
            inFlightUnlocks.remove(lockKey)
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