package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.activity.result.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.model.AchievementItem
import com.LambdaProject.MathArt.data.DataAchievements
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AchievementViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _unlockedAchievements = mutableStateListOf<String>()
    private val _achievementQueue = mutableStateListOf<AchievementItem>()
    private val _currentAchievement = MutableStateFlow<AchievementItem?>(null)
    val currentAchievement: StateFlow<AchievementItem?> = _currentAchievement
    val unlockedAchievements: List<String> = _unlockedAchievements
    private var achievementListener: ListenerRegistration? = null

    val isShowingAchievement: StateFlow<Boolean> = _currentAchievement
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun triggerAchievement(item: AchievementItem) {
        val isAlreadyInQueue = _achievementQueue.any { it.name == item.name }
        val isCurrentlyShowing = _currentAchievement.value?.name == item.name

        if (!isAlreadyInQueue && !isCurrentlyShowing) {
            _achievementQueue.add(item)
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
            _currentAchievement.value = nextItem

            delay(3000)
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
                val validAchievementNames = DataAchievements.map { it.name }.toSet()
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                        change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {

                        val ach = change.document.toObject(AchievementItem::class.java)
                        val type = change.document.getString("type")

                        if (type != "LEVEL_UP" && ach.name in validAchievementNames) {
                            triggerAchievement(ach)
                        }

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

                snapshots?.let {
                    val validAchievementNames = DataAchievements.map { it.name }.toSet()
                    val newList = it.documents
                        .mapNotNull { doc -> doc.getString("name") }
                        .filter { name -> name in validAchievementNames }
                        .distinct()
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