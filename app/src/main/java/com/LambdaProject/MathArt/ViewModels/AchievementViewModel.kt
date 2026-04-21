package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class AchievementViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _unlockedAchievements = mutableStateListOf<String>()
    val unlockedAchievements: List<String> = _unlockedAchievements

    private var achievementListener: ListenerRegistration? = null

    fun fetchUserAchievements(userId: String) {
        if (userId.isEmpty()) return
        achievementListener?.remove()
        achievementListener = db.collection("userAchievements")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("Firestore", "Error fetching achievements: $exception")
                    return@addSnapshotListener
                }

                snapshots?. let {
                    val newList = it.documents.mapNotNull { doc -> doc.getString("achievementName") }

                    if (_unlockedAchievements.toList() != newList) {
                        _unlockedAchievements.clear()
                        _unlockedAchievements.addAll(newList)
                    }
                }
            }

        /* db.collection("userAchievements")
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
            } */
    }

    override fun onCleared() {
        super.onCleared()
        achievementListener?.remove()
        achievementListener = null
    }
}