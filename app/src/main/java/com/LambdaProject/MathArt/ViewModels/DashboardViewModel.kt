package com.LambdaProject.MathArt.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.data.model.CategoryItem
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.data.DataCategories
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class DashboardViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _materialStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val materialStatusMap: StateFlow<Map<String, Boolean>> = _materialStatusMap.asStateFlow()

    private val _materialsList = MutableStateFlow<List<MaterialItem>>(emptyList())
    val materialsList: StateFlow<List<MaterialItem>> = _materialsList.asStateFlow()

    private val _categoriesList = MutableStateFlow<List<CategoryItem>>(DataCategories)
    val categoriesList: StateFlow<List<CategoryItem>> = _categoriesList.asStateFlow()

    private var rawCategories: List<CategoryItem> = DataCategories
    private var categoriesListener: ListenerRegistration? = null

    private val _isMaterialsLoading = MutableStateFlow<Boolean>(true)
    val isMaterialsLoading: StateFlow<Boolean> = _isMaterialsLoading.asStateFlow()

    private var materialsListener: ListenerRegistration? = null

    private val _hasNewNotification = MutableLiveData(false)
    val hasNewNotification: LiveData<Boolean> = _hasNewNotification

    private val _isEmailVerified = MutableStateFlow(FirebaseAuth.getInstance().currentUser?.isEmailVerified == true)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

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

    fun checkAndReloadEmailVerification() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                user?.reload()?.await()
                val firestoreVerified = user?.uid?.let { uid ->
                    try {
                        firestore.collection("users").document(uid).get().await().getBoolean("isEmailVerified") == true
                    } catch (_: Exception) { false }
                } ?: false
                _isEmailVerified.value = (user?.isEmailVerified == true) || firestoreVerified
            } catch (_: Exception) {}
        }
    }

    fun loadUserProfile(userId: String) {
        checkAndReloadEmailVerification()
        userDocListener?.remove()
        userDocListener = firestore.collection("users").document(userId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    _username.value = doc.getString("username") ?: "User"
                    _email.value = doc.getString("email") ?: "Email Tidak Diketahui"
                    if (doc.getBoolean("isEmailVerified") == true) {
                        _isEmailVerified.value = true
                    }
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
    }

    fun listenToActiveMaterials(userId: String = "") {
        _isMaterialsLoading.value = true
        materialsListener?.remove()
        materialsListener = firestore.collection("materials")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _isMaterialsLoading.value = false
                    return@addSnapshotListener
                }

                val fetched = snapshot.documents.mapNotNull { doc ->
                    try {
                        try {
                            val matDoc = com.LambdaProject.MathArt.data.model.MaterialDocument.fromSnapshot(doc)
                            com.LambdaProject.MathArt.data.repository.MaterialRepository.cacheMaterial(matDoc)
                        } catch (_: Exception) {}

                        val id = doc.getString("id") ?: doc.id
                        val rawTitle = doc.getString("title") ?: doc.getString("name") ?: ""
                        val title = if (rawTitle.isNotBlank()) rawTitle else {
                            when (id.lowercase()) {
                                "transformasi_geometri" -> "Transformasi Geometri"
                                "lingkaran" -> "Lingkaran"
                                else -> id.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                            }
                        }
                        val classLevel = doc.getString("classLevel") ?: "11"
                        val coverUrl = (doc.getString("coverUrl")
                            ?: (doc.get("coverUrl") as? String)
                            ?: doc.getString("cover_url")
                            ?: doc.getString("imageUrl")
                            ?: doc.getString("thumbnailUrl")
                            ?: "").trim()
                        val order = doc.getLong("order")?.toInt() ?: 0
                        val isDraft = doc.getBoolean("isDraft") ?: false
                        val status = doc.getString("status") ?: if (isDraft) "draft" else "published"
                        val category = doc.getString("category") ?: "Geometri"
                        val exp = doc.getLong("exp")?.toInt() ?: 100
                        val isHighlight = doc.getBoolean("isHighlight")
                            ?: (doc.get("isHighlight") as? Boolean)
                            ?: (doc.getString("isHighlight")?.toBooleanStrictOrNull())
                            ?: false
                        val defaultFallbackRes = when (id.lowercase()) {
                            "transformasi_geometri" -> com.LambdaProject.MathArt.R.drawable.img_geo
                            else -> com.LambdaProject.MathArt.R.drawable.img_kain_besurek
                        }

                        MaterialItem(
                            id = id,
                            title = title,
                            imageRes = defaultFallbackRes,
                            classLevel = classLevel,
                            coverUrl = coverUrl.ifBlank { null },
                            order = order,
                            isDraft = isDraft,
                            status = status,
                            category = category,
                            exp = exp,
                            isHighlight = isHighlight
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardViewModel", "Error parsing doc ${doc.id}", e)
                        null
                    }
                }.sortedBy { it.order }

                // Materi hanya muncul jika status bernilai "published"
                val publishedMaterials = fetched.filter { item ->
                    item.status.equals("published", ignoreCase = true)
                }

                android.util.Log.d("DashboardViewModel", "Fetched ${fetched.size} materials, published: ${publishedMaterials.size}")
                publishedMaterials.forEach {
                    android.util.Log.d("DashboardViewModel", "Material item: id=${it.id}, title=${it.title}, isHighlight=${it.isHighlight}, coverUrl=${it.coverUrl}")
                }

                _materialsList.value = publishedMaterials
                _isMaterialsLoading.value = false
                updateCategoriesLockedStatus()

                val uid = userId.ifBlank { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }
                if (uid.isNotEmpty()) {
                    checkActiveSessions(uid, publishedMaterials)
                }
            }
    }

    init {
        listenToCategories()
    }

    fun listenToCategories() {
        categoriesListener?.remove()
        categoriesListener = firestore.collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    // Fallback ke DataCategories jika koleksi Firestore categories belum dibuat atau kosong
                    rawCategories = DataCategories
                    updateCategoriesLockedStatus()
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        val name = doc.getString("name") ?: doc.id
                        val iconUrl = (doc.getString("iconUrl")
                            ?: doc.getString("icon")
                            ?: doc.getString("imageUrl")
                            ?: "").trim()
                        val order = doc.getLong("order")?.toInt() ?: 0
                        val description = (doc.getString("description")
                            ?: doc.getString("deskripsi")
                            ?: "").trim()
                        val defaultRes = when (name.lowercase().trim()) {
                            "geometri" -> com.LambdaProject.MathArt.R.drawable.ico_geometry
                            "kalkulus" -> com.LambdaProject.MathArt.R.drawable.ico_calculus
                            "trigonometri" -> com.LambdaProject.MathArt.R.drawable.ico_trigo
                            "statistika" -> com.LambdaProject.MathArt.R.drawable.ico_statistic
                            else -> com.LambdaProject.MathArt.R.drawable.ic_material
                        }
                        val category = CategoryItem(
                            id = doc.id,
                            name = name,
                            imageRes = defaultRes,
                            iconUrl = iconUrl.ifBlank { null },
                            order = order,
                            description = description
                        )
                        android.util.Log.d("DashboardViewModel", "Category parsed: id=${doc.id}, name=$name, desc=$description")
                        category
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardViewModel", "Error parsing category ${doc.id}", e)
                        null
                    }
                }.sortedBy { it.order }

                rawCategories = if (list.isNotEmpty()) list else DataCategories
                updateCategoriesLockedStatus()
            }
    }

    private fun updateCategoriesLockedStatus() {
        val published = _materialsList.value
        val updated = rawCategories.map { cat ->
            val count = published.count { it.category.trim().equals(cat.name.trim(), ignoreCase = true) }
            cat.copy(
                isLocked = (count == 0),
                materialCount = count
            )
        }
        _categoriesList.value = updated
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
        materialsListener?.remove()
        categoriesListener?.remove()
    }
}