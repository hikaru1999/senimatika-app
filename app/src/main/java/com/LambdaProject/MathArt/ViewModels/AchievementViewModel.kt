package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class AchievementViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _unlockedAchievements = mutableStateListOf<String>()
    val unlockedAchievements: List<String> = _unlockedAchievements

    fun fetchUserAchievements(userId: String) {
        db.collection("userAchievements")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("Firestore", "Error fetching achievements: $exception")
                    return@addSnapshotListener
                }

                snapshots?.let {
                    _unlockedAchievements.clear()
                    _unlockedAchievements.addAll(
                        it.documents.mapNotNull { doc -> doc.getString("achievementName")}
                    )
                }
            }
    }
}