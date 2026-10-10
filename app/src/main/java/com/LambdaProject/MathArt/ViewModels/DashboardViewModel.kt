package com.LambdaProject.MathArt.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class DashboardViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _materialStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val materialStatusMap: StateFlow<Map<String, Boolean>> = _materialStatusMap.asStateFlow()

    private val _hasNewNotification = MutableLiveData(false)
    val hasNewNotification: LiveData<Boolean> = _hasNewNotification

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _avatar = MutableStateFlow(com.LambdaProject.MathArt.utils.AvatarHelper.DEFAULT_AVATAR)
    val avatar: StateFlow<String> = _avatar.asStateFlow()

    private val _userProgressMaxPage = MutableStateFlow<Int>(0)
    val userProgressMaxPage: StateFlow<Int> = _userProgressMaxPage.asStateFlow()

    private val _exp = MutableStateFlow<Long>(0L)
    val exp: StateFlow<Long> = _exp.asStateFlow()

    private val _level = MutableStateFlow<Int>(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _activeTitle = MutableStateFlow<String>("Novice")
    val activeTitle: StateFlow<String> = _activeTitle.asStateFlow()

    private val _activeEmblem = MutableStateFlow<String>("")
    val activeEmblem: StateFlow<String> = _activeEmblem.asStateFlow()

    private val _activeFrame = MutableStateFlow<String>("")
    val activeFrame: StateFlow<String> = _activeFrame.asStateFlow()

    private val _claimedLevelRewards = MutableStateFlow<List<Int>>(listOf(1))
    val claimedLevelRewards: StateFlow<List<Int>> = _claimedLevelRewards.asStateFlow()

    private val _unlockedTitles = MutableStateFlow<List<String>>(listOf("Novice"))
    val unlockedTitles: StateFlow<List<String>> = _unlockedTitles.asStateFlow()

    private val _unlockedEmblems = MutableStateFlow<List<String>>(emptyList())
    val unlockedEmblems: StateFlow<List<String>> = _unlockedEmblems.asStateFlow()

    private val _unlockedFrames = MutableStateFlow<List<String>>(emptyList())
    val unlockedFrames: StateFlow<List<String>> = _unlockedFrames.asStateFlow()

    private var userDocListener: ListenerRegistration? = null
    private var sessionsListener: ListenerRegistration? = null
    private var achievementListener: ListenerRegistration? = null
    private var challengeListener: ListenerRegistration? = null
    private var progressListener: ListenerRegistration? = null

    fun loadUserProfile(userId: String) {
        userDocListener?.remove()
        userDocListener = firestore.collection("users").document(userId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    _username.value = doc.getString("username") ?: "User"
                    _email.value = doc.getString("email") ?: "Email Tidak Diketahui"
                    _avatar.value = doc.getString("avatar") ?: com.LambdaProject.MathArt.utils.AvatarHelper.DEFAULT_AVATAR
                    
                    val rawExp = doc.getLong("exp") ?: 0L
                    _exp.value = rawExp
                    val calcLevelInfo = com.LambdaProject.MathArt.data.model.getLevelInfoForExp(rawExp)
                    val userLvl = doc.getLong("level")?.toInt() ?: calcLevelInfo.level
                    _level.value = userLvl
                    _activeTitle.value = doc.getString("activeTitle") ?: "Novice"
                    val loadedEmblem = doc.getString("activeEmblem")?.takeIf { !it.equals("Novice", ignoreCase = true) }
                    _activeEmblem.value = loadedEmblem ?: ""
                    _activeFrame.value = doc.getString("activeFrame") ?: ""

                    val savedTitles = (doc.get("unlockedTitles") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    _unlockedTitles.value = (listOf("Novice") + savedTitles).distinct()

                    val savedEmblems = (doc.get("unlockedEmblems") as? List<*>)?.mapNotNull { it as? String }?.filter { !it.equals("Novice", ignoreCase = true) } ?: emptyList()
                    _unlockedEmblems.value = savedEmblems.distinct()

                    val savedFrames = (doc.get("unlockedFrames") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    _unlockedFrames.value = savedFrames.distinct()

                    val claimed = (doc.get("claimedLevelRewards") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                    _claimedLevelRewards.value = (listOf(1) + claimed).distinct()
                }
            }
    }

    fun claimLevelReward(userId: String, level: Int) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            com.LambdaProject.MathArt.data.model.claimLevelReward(userId, level)
        }
    }

    fun setActiveTitle(userId: String, title: String) {
        if (userId.isEmpty()) return
        firestore.collection("users").document(userId).update("activeTitle", title)
    }

    fun setActiveEmblem(userId: String, emblem: String) {
        if (userId.isEmpty()) return
        firestore.collection("users").document(userId).update("activeEmblem", emblem)
    }

    fun setActiveFrame(userId: String, frameId: String) {
        if (userId.isEmpty()) return
        firestore.collection("users").document(userId).update("activeFrame", frameId)
    }

    fun checkActiveSessions(userId: String, materials: List<MaterialItem>) {
        sessionsListener?.remove()

        sessionsListener = firestore.collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val activeIds = snapshot.documents.mapNotNull { it.getString("materialId") }
                _materialStatusMap.value = materials.associate { it.id to (it.id in activeIds) }
            }


        /* FirebaseFirestore.getInstance()
            .collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _materialStatusMap.value = materials.associate { it.id to false }
                    return@addSnapshotListener
                }
                val activeIds = snapshot.documents.mapNotNull { it.getString("materialId") }
                val statusMap = materials.associate { it.id to (it.id in activeIds) }
                _materialStatusMap.value = statusMap
            } */
    }

    private var levelNotifListener: ListenerRegistration? = null
    private var lastSeenListener: ListenerRegistration? = null
    private var lastSeenTimestamp: Long = 0L
    private var hasUnreadAchievements = false
    private var hasUnreadLevelNotifications = false
    private var hasPendingChallenges = false
    private var currentAchievementsSnapshot: com.google.firebase.firestore.QuerySnapshot? = null
    private var currentLevelNotifSnapshot: com.google.firebase.firestore.QuerySnapshot? = null
    private var currentChallengeSnapshot: com.google.firebase.firestore.QuerySnapshot? = null

    private fun reevaluateNotifications() {
        val lastSeen = lastSeenTimestamp

        hasUnreadAchievements = currentAchievementsSnapshot?.documents?.any { doc ->
            val isRead = doc.getBoolean("isRead") ?: false
            if (isRead) return@any false
            val rawTimestamp = doc.get("timestamp")
            val timestamp = when (rawTimestamp) {
                is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                is Long -> rawTimestamp
                is Number -> rawTimestamp.toLong()
                else -> 0L
            }
            timestamp > lastSeen
        } ?: false

        hasUnreadLevelNotifications = currentLevelNotifSnapshot?.documents?.any { doc ->
            val isRead = doc.getBoolean("isRead") ?: false
            if (isRead) return@any false
            val rawTimestamp = doc.get("timestamp")
            val timestamp = when (rawTimestamp) {
                is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                is Long -> rawTimestamp
                is Number -> rawTimestamp.toLong()
                else -> 0L
            }
            timestamp > lastSeen
        } ?: false

        hasPendingChallenges = currentChallengeSnapshot?.documents?.any { doc ->
            val rawTimestamp = doc.get("timestamp")
            val timestamp = when (rawTimestamp) {
                is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                is Long -> rawTimestamp
                is Number -> rawTimestamp.toLong()
                else -> 0L
            }
            timestamp > lastSeen
        } ?: false

        _hasNewNotification.value = hasUnreadAchievements || hasUnreadLevelNotifications || hasPendingChallenges
    }

    fun listenForNewNotifications(userId: String) {
        lastSeenListener?.remove()
        achievementListener?.remove()
        challengeListener?.remove()
        levelNotifListener?.remove()

        val userDocRef = firestore.collection("users").document(userId)

        lastSeenListener = userDocRef.addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                val lastSeenDate = snapshot.getTimestamp("lastSeenNotification")?.toDate()
                lastSeenTimestamp = lastSeenDate?.time ?: 0L
                reevaluateNotifications()
            }
        }

        // 1. Level up notifications (if subcollection exists)
        levelNotifListener = firestore.collection("users").document(userId)
            .collection("notifications")
            .addSnapshotListener { snapshot, error ->
                currentLevelNotifSnapshot = if (error == null) snapshot else null
                reevaluateNotifications()
            }

        // 2. Achievements & Level Up in achievements subcollection
        achievementListener = firestore.collection("users").document(userId)
            .collection("achievements")
            .addSnapshotListener { snapshot, error ->
                currentAchievementsSnapshot = if (error == null) snapshot else null
                reevaluateNotifications()
            }

        // 3. Multiplayer Challenges
        challengeListener = firestore.collection("challenges")
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                currentChallengeSnapshot = if (error == null) snapshot else null
                reevaluateNotifications()
            }
    }

    fun clearNotifications() {
        lastSeenTimestamp = System.currentTimeMillis()
        _hasNewNotification.value = false
        reevaluateNotifications()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("lastSeenNotification", FieldValue.serverTimestamp())
    }

    fun listenForProgress(userId: String) {
        progressListener?.remove()
        progressListener = firestore.collection("userProgress").document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _userProgressMaxPage.value = snapshot.getLong("maxPage")?.toInt() ?: 0
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        userDocListener?.remove()
        sessionsListener?.remove()
        lastSeenListener?.remove()
        achievementListener?.remove()
        challengeListener?.remove()
        levelNotifListener?.remove()
        progressListener?.remove()
    }
}