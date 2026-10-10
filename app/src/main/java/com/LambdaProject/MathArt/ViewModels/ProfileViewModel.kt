package com.LambdaProject.MathArt.ViewModels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.LambdaProject.MathArt.data.DataMaterials
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.data.model.unlockExplorerAchievement
import com.LambdaProject.MathArt.data.model.StudyDurationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class  ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _username = mutableStateOf("User")
    val username: State<String> = _username

    private val _fullName = mutableStateOf("User")
    val fullName: State<String> = _fullName

    private val _email = mutableStateOf("Email Tidak Diketahui")
    val email: State<String> = _email

    private val _coins = mutableIntStateOf(0)
    val coins: State<Int> = _coins

    private val _exp = mutableLongStateOf(0L)
    val exp: State<Long> = _exp

    private val _level = mutableIntStateOf(1)
    val level: State<Int> = _level

    private val _activeTitle = mutableStateOf("Novice")
    val activeTitle: State<String> = _activeTitle

    private val _activeEmblem = mutableStateOf("")
    val activeEmblem: State<String> = _activeEmblem

    private val _activeFrame = mutableStateOf("")
    val activeFrame: State<String> = _activeFrame

    private val _unlockedTitles = mutableStateListOf<String>("Novice")
    val unlockedTitles: List<String> = _unlockedTitles

    private val _unlockedEmblems = mutableStateListOf<String>()
    val unlockedEmblems: List<String> = _unlockedEmblems

    private val _unlockedFrames = mutableStateListOf<String>()
    val unlockedFrames: List<String> = _unlockedFrames

    private val _claimedLevelRewards = mutableStateListOf<Int>()
    val claimedLevelRewards: List<Int> = _claimedLevelRewards

    private val _avatar = mutableStateOf(com.LambdaProject.MathArt.utils.AvatarHelper.DEFAULT_AVATAR)
    val avatar: State<String> = _avatar

    private val _studyDuration = mutableLongStateOf(0L)
    val studyDuration: State<Long> = _studyDuration

    private val _activeSessions = mutableStateListOf<MaterialItem>()
    val activeSessions: List<MaterialItem> = _activeSessions

    private val _unlockedAchievements = mutableStateListOf<String>()

    private var achievementListener: ListenerRegistration? = null
    private var userDocListener: ListenerRegistration? = null
    val unlockedAchievements: List<String> = _unlockedAchievements

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    // Cached Learning Report State
    private val _onlineQuizCount = mutableIntStateOf(0)
    val onlineQuizCount: State<Int> = _onlineQuizCount

    private val _highestOnlineScore = mutableIntStateOf(0)
    val highestOnlineScore: State<Int> = _highestOnlineScore

    private val _totalOnlinePoints = mutableIntStateOf(0)
    val totalOnlinePoints: State<Int> = _totalOnlinePoints

    private val _recentOnlineQuizzes = mutableStateListOf<Pair<String, Int>>()
    val recentOnlineQuizzes: List<Pair<String, Int>> = _recentOnlineQuizzes

    private val _standardQuizCount = mutableIntStateOf(0)
    val standardQuizCount: State<Int> = _standardQuizCount

    private val _isReportLoading = mutableStateOf(false)
    val isReportLoading: State<Boolean> = _isReportLoading

    private val _isReportLoaded = mutableStateOf(false)
    val isReportLoaded: State<Boolean> = _isReportLoaded

    val userId = auth.currentUser?.uid ?: ""

    init {
        checkAndResetDuration()
        loadUserProfile()
        loadActiveSessions()
        observeStudyDuration()
        observeAchievements()
        loadLearningReport()
    }

    private fun loadUserProfile() {
        val uid = userId
        if (uid.isEmpty()) {
            _isLoading.value = false
            return
        }

        userDocListener?.remove()
        userDocListener = firestore.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    _username.value = doc.getString("username") ?: "User"
                    _fullName.value = doc.getString("fullname") ?: "User"
                    _email.value = doc.getString("email") ?: "Email Belum Didaftarkan"
                    _coins.intValue = doc.getLong("coins")?.toInt() ?: 0
                    _avatar.value = doc.getString("avatar") ?: com.LambdaProject.MathArt.utils.AvatarHelper.DEFAULT_AVATAR
                    
                    val rawExp = doc.getLong("exp") ?: 0L
                    _exp.longValue = rawExp
                    val calcLevelInfo = com.LambdaProject.MathArt.data.model.getLevelInfoForExp(rawExp)
                    val userLvl = doc.getLong("level")?.toInt() ?: calcLevelInfo.level
                    _level.intValue = userLvl

                    _activeTitle.value = doc.getString("activeTitle") ?: "Novice"
                    val loadedEmblem = doc.getString("activeEmblem")?.takeIf { !it.equals("Novice", ignoreCase = true) }
                    _activeEmblem.value = loadedEmblem ?: ""
                    _activeFrame.value = doc.getString("activeFrame") ?: ""

                    // Tag terbuka dari data tersimpan (hanya yang sudah diklaim, default Novice)
                    val savedTitles = (doc.get("unlockedTitles") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    val allTitles = (listOf("Novice") + savedTitles).distinct()
                    _unlockedTitles.clear()
                    _unlockedTitles.addAll(allTitles)

                    // Emblem terbuka dari data tersimpan (hanya yang sudah diklaim)
                    val savedEmblems = (doc.get("unlockedEmblems") as? List<*>)?.mapNotNull { it as? String }?.filter { !it.equals("Novice", ignoreCase = true) } ?: emptyList()
                    _unlockedEmblems.clear()
                    _unlockedEmblems.addAll(savedEmblems.distinct())

                    // Bingkai Avatar terbuka dari data tersimpan
                    val savedFrames = (doc.get("unlockedFrames") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    _unlockedFrames.clear()
                    _unlockedFrames.addAll(savedFrames.distinct())

                    // Level 1 selalu berstatus auto-klaim
                    val claimed = (doc.get("claimedLevelRewards") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                    val allClaimed = (listOf(1) + claimed).distinct()
                    _claimedLevelRewards.clear()
                    _claimedLevelRewards.addAll(allClaimed)

                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                }
            }
    }

    fun claimLevelReward(level: Int, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val uid = userId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            val res = com.LambdaProject.MathArt.data.model.claimLevelReward(uid, level)
            res.onSuccess { msg -> onResult(true, msg) }
               .onFailure { err -> onResult(false, err.message ?: "Gagal klaim hadiah") }
        }
    }

    fun setActiveTitle(title: String) {
        val uid = userId
        if (uid.isEmpty()) return
        _activeTitle.value = title
        firestore.collection("users").document(uid).update("activeTitle", title)
    }

    fun setActiveEmblem(emblem: String) {
        val uid = userId
        if (uid.isEmpty()) return
        _activeEmblem.value = emblem
        firestore.collection("users").document(uid).update("activeEmblem", emblem)
    }

    fun setActiveFrame(frameId: String) {
        val uid = userId
        if (uid.isEmpty()) return
        _activeFrame.value = frameId
        firestore.collection("users").document(uid).update("activeFrame", frameId)
    }

    fun updateAvatar(newAvatar: String, onComplete: (Boolean) -> Unit = {}) {
        val uid = userId
        if (uid.isEmpty()) return
        _avatar.value = newAvatar
        firestore.collection("users").document(uid)
            .update("avatar", newAvatar)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    private fun loadActiveSessions() {
        firestore.collection("sessions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "active")
            .get()
            .addOnSuccessListener { docs ->
                val materialIds = docs.mapNotNull { it.getString("materialId") }
                _activeSessions.clear()
                _activeSessions.addAll(DataMaterials.filter { it.id in materialIds })
            }
    }

    private fun observeStudyDuration() {
        StudyDurationManager.observeStudyDuration(userId) { duration ->
            _studyDuration.longValue = duration
            unlockExplorerAchievement(userId, duration)
        }
    }

    private fun observeAchievements() {
        if (userId.isEmpty()) return
        achievementListener?.remove()

        achievementListener = firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .whereEqualTo("isUnlocked", true)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ProfileViewModel", "Error listening to achievements", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    _unlockedAchievements.clear()
                    _unlockedAchievements.addAll(
                        snapshots.mapNotNull { it.getString("name") }
                    )
                    Log.d("ProfileViewModel", "Achievements updated: ${_unlockedAchievements.size}")
                }
            }
    }

    private fun checkAndResetDuration() {
        if (userId.isNotEmpty()) {
            StudyDurationManager.checkAndResetWeeklyDuration(userId) {
                observeStudyDuration()
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid)
                .update("isOnline", false)
                .addOnSuccessListener {
                    achievementListener?.remove()
                    auth.signOut()
                    onSuccess()
                }
                .addOnFailureListener{
                    Log.d("ProfileModel", "Ada kesalahan")
                }
        }
    }

    fun loadLearningReport(force: Boolean = false) {
        val uid = userId
        if (uid.isEmpty()) {
            _isReportLoading.value = false
            return
        }

        if (_isReportLoaded.value && !force) {
            return
        }

        _isReportLoading.value = true

        firestore.collection("online_quiz_results")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val documents = snapshot.documents
                _onlineQuizCount.intValue = documents.size
                var maxScore = 0
                var sumPoints = 0
                _recentOnlineQuizzes.clear()

                documents.forEach { doc ->
                    val points = doc.getLong("totalPoints")?.toInt() ?: 0
                    val materialId = doc.getString("materialId") ?: ""
                    sumPoints += points
                    if (points > maxScore) {
                        maxScore = points
                    }
                    if (_recentOnlineQuizzes.size < 3) {
                        _recentOnlineQuizzes.add(Pair(materialId, points))
                    }
                }
                _highestOnlineScore.intValue = maxScore
                _totalOnlinePoints.intValue = sumPoints
                _isReportLoading.value = false
                _isReportLoaded.value = true
            }
            .addOnFailureListener {
                _isReportLoading.value = false
            }

        firestore.collection("quizResults")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                _standardQuizCount.intValue = snapshot.documents.size
            }
    }

    override fun onCleared() {
        super.onCleared()
        achievementListener?.remove()
        userDocListener?.remove()
    }
}