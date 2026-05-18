package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.activity.result.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.model.AchievementItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AchievementViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _unlockedAchievements = mutableStateListOf<String>()
    private val _achievementQueue = mutableStateListOf<AchievementItem>()
    private val _currentAchievement = MutableStateFlow<AchievementItem?>(null)
    val currentAchievement: StateFlow<AchievementItem?> = _currentAchievement
    val unlockedAchievements: List<String> = _unlockedAchievements
    private var achievementListener: ListenerRegistration? = null

    fun triggerAchievement(item: AchievementItem) {
        Log.d("ACH_DEBUG", "Fungsi trigger dipanggil untuk: ${item.name}")
        val isAlreadyInQueue = _achievementQueue.any { it.name == item.name }
        val isCurrentlyShowing = _currentAchievement.value?.name == item.name

        if (!isAlreadyInQueue && !isCurrentlyShowing) {
            _achievementQueue.add(item)
            Log.d("ACH_DEBUG", "Berhasil masuk antrean. Sisa antrean: ${_achievementQueue.size}")

            if (_currentAchievement.value == null) {
                processNext()
            }
        }
    }

    /* private fun processNext() {
        viewModelScope.launch {
            if (_achievementQueue.isNotEmpty() && _currentAchievement.value == null) {
                val nextItem = _achievementQueue.removeAt(0)
                _currentAchievement.value = nextItem
                delay(2500)
                _currentAchievement.value = null

                delay(500)

                if (_achievementQueue.isNotEmpty()) {
                    processNext()
                }
            }
        }
    } */

    private fun processNext() {
        if (_achievementQueue.isEmpty()) return

        viewModelScope.launch {
            val nextItem = _achievementQueue.removeAt(0)
            Log.d("ACH_DEBUG", "UI SHOWN: ${nextItem.name}")

            _currentAchievement.value = nextItem

            delay(3000)

            Log.d("ACH_DEBUG", "UI HIDDEN: ${nextItem.name}")
            _currentAchievement.value = null

            delay(500)

            if (_achievementQueue.isNotEmpty()) {
                processNext()
            }
        }
    }

    fun listenForNewAchievements(userId: String) {
        if (userId.isEmpty()) return

        db.collection("users").document(userId)
            .collection("achievements")
            .whereEqualTo("isUnlocked", true)
            .whereEqualTo("notified", false)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                        change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {

                        val ach = change.document.toObject(AchievementItem::class.java)

                        triggerAchievement(ach)

                        db.collection("users").document(userId)
                            .collection("achievements").document(change.document.id)
                            .update("notified", true)
                    }
                }
            }
    }

    fun fetchUserAchievements(userId: String) {
        if (userId.isEmpty()) return
        achievementListener?.remove()

        achievementListener = db.collection("users").document(userId)
            .collection("achievements")
            .whereEqualTo("isUnlocked", true)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) return@addSnapshotListener

                snapshots?. let {
                    val newList = it.documents.mapNotNull { doc -> doc.getString("name") }
                    _unlockedAchievements.clear()
                    _unlockedAchievements.addAll(newList)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        achievementListener?.remove()
        achievementListener = null
    }
}