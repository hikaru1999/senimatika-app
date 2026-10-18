package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.data.model.OnlineQuizDesc
import com.LambdaProject.MathArt.data.model.OnlineUser
import com.LambdaProject.MathArt.data.model.QuizQuestionRef
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.Pages.NotFoundScreen
import com.LambdaProject.MathArt.utils.AvatarHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineQuizLobby(
    viewModel: OnlineQuizViewModel,
    onUserSelected: (String) -> Unit,
    navController: NavController,
    userId: String,
    initialTab: String = "description"
) {
    val selectedMaterial by viewModel.selectedMaterial.collectAsState()
    val isLearningMode = selectedMaterial?.isLearningMode == true
    val tabKeys = if (isLearningMode) listOf("description") else listOf("description", "leaderboard")
    val tabLabels = if (isLearningMode) listOf("Deskripsi") else listOf("Deskripsi", "Leaderboard")
    val initialPageIndex = tabKeys.indexOf(initialTab.lowercase()).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { tabKeys.size })
    val userOnlineList by viewModel.userOnlineList.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val activeFrame by viewModel.activeFrame.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val isQuizNotFound by viewModel.isQuizNotFound.collectAsState()
    var checkComplete by remember { mutableStateOf(false) }
    val isCurrentlyNotFound = (selectedMaterial == null && checkComplete) || isQuizNotFound

    var isVerifying by remember { mutableStateOf(true) }
    var docJenjang by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.loadUserAvatar(userId)
    }

    LaunchedEffect(isCurrentlyNotFound) {
        if (isCurrentlyNotFound) {
            navController.currentBackStackEntry?.savedStateHandle?.set("is_not_found", true)
        }
    }

    LaunchedEffect(selectedMaterial?.id) {
        val id = selectedMaterial?.id
        if (id == null) {
            isVerifying = false
            checkComplete = false
            return@LaunchedEffect
        }

        isVerifying = true
        checkComplete = false

        db.collection("online_quizzes").document(id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val doc = task.result
                    if (doc != null && doc.exists() && doc.getBoolean("active") == true) {
                        val j = doc.getString("jenjang") ?: doc.getString("classLevel") ?: ""
                        if (j.isNotBlank() && !j.equals("Umum", ignoreCase = true)) {
                            docJenjang = j
                        }

                        val rawQuestionsList = doc.get("questions") as? List<*>
                        val parsedQuestionRefs = rawQuestionsList?.mapNotNull { item ->
                            when (item) {
                                is Map<*, *> -> {
                                    val qId = item["id"] as? String ?: return@mapNotNull null
                                    val points = (item["points"] as? Number)?.toInt() ?: 10
                                    val timer = (item["timer"] as? Number)?.toInt() ?: 30
                                    QuizQuestionRef(id = qId, points = points, timer = timer)
                                }
                                is String -> QuizQuestionRef(id = item, points = 10, timer = 30)
                                else -> null
                            }
                        } ?: emptyList()

                        val currentMat = selectedMaterial
                        val questionRefs = parsedQuestionRefs.ifEmpty { currentMat?.questions ?: emptyList() }
                        val numQ = questionRefs.size.takeIf { it > 0 }
                            ?: (rawQuestionsList?.size ?: (currentMat?.numberQuestion ?: 0))
                        val totalSeconds = if (questionRefs.isNotEmpty()) questionRefs.sumOf { it.timer } else (numQ * 30)
                        val docDur = doc.getLong("durationMinutes")?.toInt() ?: 0
                        val durMin = if (docDur > 0) docDur else ((totalSeconds + 59) / 60).coerceAtLeast(1)
                        val isLearningDoc = doc.getBoolean("isLearningMode") ?: (currentMat?.isLearningMode ?: false)

                        val baseMat = currentMat ?: OnlineQuizDesc(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            active = doc.getBoolean("active") == true,
                            isRandomized = doc.getBoolean("isRandomized") == true
                        )

                        val updatedMat = baseMat.copy(
                            questions = questionRefs,
                            numberQuestion = numQ,
                            durationMinutes = durMin,
                            classLevel = if (j.isNotBlank()) j else baseMat.classLevel,
                            description = doc.getString("description") ?: baseMat.description,
                            rewardCoin = doc.getLong("rewardCoin")?.toInt() ?: baseMat.rewardCoin,
                            exp = doc.getLong("exp")?.toInt() ?: baseMat.exp,
                            isLearningMode = isLearningDoc
                        )
                        viewModel.selectMaterial(updatedMat)
                        isVerifying = false
                    } else {
                        viewModel.clearSelectedMaterial()
                        isVerifying = false
                    }
                    checkComplete = true
                } else {
                    isVerifying = false
                    checkComplete = true
                }
            }
    }

    LaunchedEffect(true) {
        viewModel.loadUsersOnline()
    }

    LaunchedEffect(Unit) {
        viewModel.startListeningForChallenges()
    }

    LaunchedEffect(userOnlineList, selectedMaterial, isLearningMode) {
        if (!isLearningMode) {
            selectedMaterial?.let { material ->
                val currentMaterialId = material.id
                viewModel.fetchLeaderboardForMaterial(currentMaterialId)
            }
        }
    }

    var showMatchmaking by remember { mutableStateOf(false) }
    val currentFirebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val userProfileName by viewModel.userName.collectAsState()
    val playerName = userProfileName.takeIf { it.isNotBlank() } ?: (currentFirebaseUser?.displayName?.takeIf { it.isNotBlank() } ?: "Player")

    if (isVerifying) {
        OnlineQuizLobbySkeleton()
    } else if ((selectedMaterial == null && checkComplete) || isQuizNotFound) {
        NotFoundScreen(
            title = "Yah, Kuis Tidak Ditemukan",
            message = "Maaf ya, konten ini sedang tidak tersedia.",
            onBack = {
                navController.currentBackStackEntry?.savedStateHandle?.set("is_not_found", false)
                viewModel.refreshMaterials()
                navController.popBackStack()
            }
        )
    } else if (selectedMaterial != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color(0xFFF8FAFC),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = selectedMaterial!!.title,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF0F172A),
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
//                                if (isLearningMode) {
//                                    Surface(
//                                        color = Color(0xFF059669).copy(alpha = 0.12f),
//                                        shape = RoundedCornerShape(6.dp),
//                                        border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.35f))
//                                    ) {
//                                        Text(
//                                            text = "Mode Belajar",
//                                            fontSize = 10.5.sp,
//                                            fontWeight = FontWeight.Bold,
//                                            fontFamily = interFontFamily,
//                                            color = Color(0xFF059669),
//                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
//                                        )
//                                    }
//                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color(0xFF1E293B)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.White
                        )
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF8FAFC))
                ) {
                    if (!isLearningMode) {
                        // Modern Tab Row
                        Surface(
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            TabRow(
                                selectedTabIndex = pagerState.currentPage,
                                containerColor = Color.White,
                                indicator = { tabPositions ->
                                    SecondaryIndicator(
                                        Modifier
                                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                                        color = Color(0xFF4F46E5)
                                    )
                                },
                                divider = {
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            ) {
                                tabLabels.forEachIndexed { index, title ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (index == 0) Icons.Default.Description else Icons.Default.EmojiEvents,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (pagerState.currentPage == index) Color(0xFF4F46E5) else Color(0xFF94A3B8)
                                                )
                                                Text(
                                                    text = title,
                                                    fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium,
                                                    fontFamily = interFontFamily,
                                                    fontSize = 13.5.sp,
                                                    color = if (pagerState.currentPage == index) Color(0xFF4F46E5) else Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = false,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            when (tabKeys[page]) {
                                "description" -> DeskripsiContent(
                                    material = selectedMaterial!!,
                                    docJenjang = docJenjang,
                                    userOnlineList = userOnlineList,
                                    onUserSelected = onUserSelected,
                                    navController = navController,
                                    userId = userId,
                                    onStartDuel = { showMatchmaking = true }
                                )
                                "leaderboard" -> LeaderboardTab(materialId = selectedMaterial?.id ?: "")
                            }
                        }
                    } else {
                        // Learning Mode: Direct content without TabRow or Leaderboard
                        Box(modifier = Modifier.weight(1f)) {
                            DeskripsiContent(
                                material = selectedMaterial!!,
                                docJenjang = docJenjang,
                                userOnlineList = userOnlineList,
                                onUserSelected = onUserSelected,
                                navController = navController,
                                userId = userId,
                                onStartDuel = { }
                            )
                        }
                    }
                }
            }

            if (showMatchmaking) {
                val totalQ = selectedMaterial?.questions?.size?.takeIf { it > 0 }
                    ?: selectedMaterial?.numberQuestion
                    ?: 0
                DuelMatchmakingOverlay(
                    playerName = playerName,
                    playerAvatarRes = AvatarHelper.getAvatarDrawableRes(userAvatar),
                    playerFrameId = activeFrame,
                    totalQuestions = totalQ,
                    onMatchFound = { matchedBot ->
                        viewModel.setSelectedBot(matchedBot)
                        showMatchmaking = false
                        navController.navigate("StartQuiz/${selectedMaterial!!.id}/${userId}?isDuel=true&botId=${matchedBot.id}")
                    },
                    onCancel = {
                        viewModel.setSelectedBot(null)
                        showMatchmaking = false
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun DeskripsiContent(
    material: OnlineQuizDesc,
    docJenjang: String = "",
    userOnlineList: List<OnlineUser>,
    onUserSelected: (String) -> Unit,
    navController: NavController,
    userId: String,
    onStartDuel: () -> Unit
) {
    val viewModel: OnlineQuizViewModel = hiltViewModel()
    var isLoading by remember { mutableStateOf(false) }

    val capsuleJenjang = remember(docJenjang, material) {
        when {
            docJenjang.isNotBlank() && !docJenjang.equals("Umum", ignoreCase = true) -> docJenjang
            material.classLevel.isNotBlank() && !material.classLevel.equals("Umum", ignoreCase = true) -> material.classLevel
            material.title.contains("VII", ignoreCase = true) || material.title.contains("Kelas 7") -> "VII SMP"
            material.title.contains("VIII", ignoreCase = true) || material.title.contains("Kelas 8") -> "VIII SMP"
            material.title.contains("IX", ignoreCase = true) || material.title.contains("Kelas 9") -> "IX SMP"
            material.title.contains("XII", ignoreCase = true) || material.title.contains("Kelas 12") -> "XII SMA"
            material.title.contains("XI", ignoreCase = true) || material.title.contains("Kelas 11") -> "XI SMA"
            material.title.contains("X", ignoreCase = true) || material.title.contains("Kelas 10") -> "X SMA"
            else -> "Semua Jenjang"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp)
        ) {
            // 1. Hero Map Banner Card dengan quiz_banner.png
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(22.dp),
                            spotColor = Color(0xFF0284C7).copy(alpha = 0.25f)
                        ),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Banner Image
                        Image(
                            painter = painterResource(id = R.drawable.quiz_banner),
                            contentDescription = material.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.CenterEnd
                        )

                        // Gradient Scrim Horizontal dari kiri agar teks terbaca jelas dan unsur grafis kanan tetap utuh
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color(0xFF0F172A).copy(alpha = 0.92f),
                                            0.55f to Color(0xFF0F172A).copy(alpha = 0.70f),
                                            0.75f to Color(0xFF0F172A).copy(alpha = 0.20f),
                                            1.0f to Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Gradient Scrim Vertikal di bagian bawah
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color.Transparent,
                                            0.6f to Color.Black.copy(alpha = 0.25f),
                                            1.0f to Color.Black.copy(alpha = 0.75f)
                                        )
                                    )
                                )
                        )

                        // Title on Banner (vertically left-centered, dibatasi 68% agar tidak overlap dengan desain ilustrasi kanan)
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxWidth(0.68f)
                                .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = Color(0xFF0284C7).copy(alpha = 0.95f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF7DD3FC))
                            ) {
                                Text(
                                    text = capsuleJenjang,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = material.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = interFontFamily,
                                color = Color.White,
                                lineHeight = 23.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 2. Main Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Stat Cards Row
                val questionCount = material.questions.size.takeIf { it > 0 }
                    ?: material.numberQuestion.takeIf { it > 0 }
                    ?: 0

                val totalTimeMinutes = when {
                    material.durationMinutes > 0 -> material.durationMinutes
                    material.questions.isNotEmpty() -> {
                        val totalSec = material.questions.sumOf { it.timer }
                        ((totalSec + 59) / 60).coerceAtLeast(1)
                    }
                    questionCount > 0 -> {
                        ((questionCount * 30 + 59) / 60).coerceAtLeast(1)
                    }
                    else -> 0
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuizStatItem(
                        icon = R.drawable.ic_no_question,
                        label = "Banyak Soal",
                        value = "$questionCount Soal",
                        modifier = Modifier.weight(1f)
                    )
                    QuizStatItem(
                        icon = R.drawable.ic_stopwatch,
                        label = "Total Waktu",
                        value = "$totalTimeMinutes Menit",
                        modifier = Modifier.weight(1f)
                    )
                    QuizStatItem(
                        icon = R.drawable.ic_coin,
                        label = "First time reward",
                        value = "+${material.rewardCoin}",
//                      badge = "Hadiah pertama kali",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Description Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Deskripsi Kuis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = interFontFamily,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Text(
                            text = material.description,
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            fontFamily = interFontFamily,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                // Quiz Rules Card
                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Aturan Pengerjaan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            fontFamily = interFontFamily,
                            color = Color(0xFF3730A3)
                        )
                        val rules = if (material.isLearningMode) {
                            listOf(
                                "Mode Belajar: fokus pada pemahaman konsep dan langkah penyelesaian.",
                                "Seusai menjawab setiap soal, pembahasan solusi akan ditampilkan untuk membantumu belajar.",
                                "Waktu pengerjaan lebih santai dan hasil kuis tidak dicatat di Leaderboard."
                            )
                        } else {
                            listOf(
                                "Setiap soal memiliki batas waktu yang bisa berbeda-beda.",
                                "Poin dihitung berdasarkan ketepatan, kecepatan, dan streak dalam menjawab.",
                                "Skor terbaik akan otomatis dicatat di Leaderboard."
                            )
                        }

                        rules.forEach { rule ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(5.dp)
                                        .background(Color(0xFF4F46E5), CircleShape)
                                )
                                Text(
                                    text = rule,
                                    fontSize = 12.sp,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF4338CA),
                                    lineHeight = 17.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 16.dp,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            if (material.isLearningMode) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            viewModel.selectMaterial(material)
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1200)
                                navController.navigate("StartQuiz/${material.id}/${userId}")
                                isLoading = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "MULAI BELAJAR",
                                    fontWeight = FontWeight.Black,
                                    fontFamily = interFontFamily,
                                    fontSize = 13.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            viewModel.selectMaterial(material)
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1200)
                                navController.navigate("StartQuiz/${material.id}/${userId}")
                                isLoading = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(52.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "MULAI KUIS",
                                    fontWeight = FontWeight.Black,
                                    fontFamily = interFontFamily,
                                    fontSize = 13.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.selectMaterial(material)
                            onStartDuel()
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFFFDE047)
                            )
                            Text(
                                text = "DUEL",
                                fontWeight = FontWeight.Black,
                                fontFamily = interFontFamily,
                                fontSize = 13.sp,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizStatItem(
    icon: Int,
    label: String,
    value: String,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
            if (badge != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFFCD34D))
                ) {
                    Text(
                        text = badge,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        textAlign = TextAlign.Center,
                        lineHeight = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardTab(
    materialId: String,
    viewModel: OnlineQuizViewModel = hiltViewModel()
) {
    val leaderboard by viewModel.leaderboard.collectAsState(initial = emptyList())
    val sortedLeaderboard = leaderboard.sortedByDescending { it.points }
    val topThree = sortedLeaderboard.take(3)
    val others = sortedLeaderboard.drop(3).take(50)
    val scrollState = rememberScrollState()

    LaunchedEffect(materialId) {
        viewModel.fetchLeaderboardForMaterial(materialId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Rank Podium Section (Top 50% height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Leaderboard",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        "3 Besar Skor Tertinggi",
                        fontSize = 11.5.sp,
                        fontFamily = interFontFamily,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val maxHeightDp = maxHeight

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Rank 2: Silver (Left)
                            LeaderBox(
                                username = topThree.getOrNull(1)?.username ?: "-",
                                points = topThree.getOrNull(1)?.points ?: 0,
                                avatar = topThree.getOrNull(1)?.avatar ?: "avatar_5",
                                medalType = MedalType.SILVER,
                                medalColor = Color(0xFF94A3B8),
                                modifier = Modifier.weight(1f),
                                heightFraction = 0.55f,
                                maxHeightDp = maxHeightDp
                            )

                            // Rank 1: Gold (Center - Tallest)
                            LeaderBox(
                                username = topThree.getOrNull(0)?.username ?: "-",
                                points = topThree.getOrNull(0)?.points ?: 0,
                                avatar = topThree.getOrNull(0)?.avatar ?: "avatar_5",
                                medalColor = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1.1f),
                                heightFraction = 0.85f,
                                medalType = MedalType.GOLD,
                                maxHeightDp = maxHeightDp
                            )

                            // Rank 3: Bronze (Right)
                            LeaderBox(
                                username = topThree.getOrNull(2)?.username ?: "-",
                                points = topThree.getOrNull(2)?.points ?: 0,
                                avatar = topThree.getOrNull(2)?.avatar ?: "avatar_5",
                                medalColor = Color(0xFFD97706),
                                modifier = Modifier.weight(1f),
                                heightFraction = 0.42f,
                                medalType = MedalType.BRONZE,
                                maxHeightDp = maxHeightDp
                            )
                        }
                    }
                }
            }

            // Top 50 Leaderboard Bottom Sheet Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color(0xFF1E1B4B),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Peringkat 50 Besar",
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${sortedLeaderboard.size} Peserta",
                                color = Color(0xFFC7D2FE),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (others.isNotEmpty()) {
                            others.forEachIndexed { index, user ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                color = Color.White.copy(alpha = 0.15f),
                                                shape = CircleShape,
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${index + 4}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Black,
                                                        fontFamily = interFontFamily,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = CircleShape,
                                                modifier = Modifier.size(32.dp),
                                                color = Color.White.copy(alpha = 0.15f),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                            ) {
                                                Image(
                                                    painter = painterResource(id = AvatarHelper.getAvatarDrawableRes(user.avatar)),
                                                    contentDescription = user.username,
                                                    modifier = Modifier
                                                        .padding(2.dp)
                                                        .fillMaxSize()
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Text(
                                                text = user.username,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interFontFamily,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Surface(
                                            color = Color(0xFF4F46E5).copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "${user.points} pts",
                                                color = Color(0xFFE0E7FF),
                                                fontWeight = FontWeight.Black,
                                                fontFamily = interFontFamily,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Belum ada peserta lainnya di papan peringkat",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = interFontFamily,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderBox(
    username: String,
    points: Int,
    avatar: String = "avatar_5",
    medalType: MedalType,
    medalColor: Color,
    maxHeightDp: Dp,
    modifier: Modifier = Modifier,
    heightFraction: Float
) {
    val heightAnim = remember { Animatable(0f) }

    LaunchedEffect(maxHeightDp, heightFraction) {
        heightAnim.animateTo(
            targetValue = (maxHeightDp * heightFraction).value,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val gradientColors = when (medalType) {
        MedalType.GOLD -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
        MedalType.SILVER -> listOf(Color(0xFF94A3B8), Color(0xFF64748B))
        MedalType.BRONZE -> listOf(Color(0xFFD97706), Color(0xFF92400E))
    }

    val rankEmoji = when (medalType) {
        MedalType.GOLD -> "👑 #1"
        MedalType.SILVER -> "🥈 #2"
        MedalType.BRONZE -> "🥉 #3"
    }

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Rank tag
        Surface(
            color = medalColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, medalColor.copy(alpha = 0.3f))
        ) {
            Text(
                text = rankEmoji,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                fontFamily = interFontFamily,
                color = medalColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Avatar
        Surface(
            modifier = Modifier.size(if (medalType == MedalType.GOLD) 52.dp else 44.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(2.dp, medalColor),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = AvatarHelper.getAvatarDrawableRes(avatar)),
                    contentDescription = username,
                    modifier = Modifier
                        .padding(3.dp)
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = username,
            fontSize = 11.5.sp,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Column
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightAnim.value.dp.coerceAtLeast(40.dp))
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(brush = Brush.verticalGradient(gradientColors)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "${points} pts",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}

enum class MedalType {
    GOLD, SILVER, BRONZE
}

@Composable
private fun Modifier.onlineLobbyShimmer(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF1F5F9)
): Modifier {
    val transition = rememberInfiniteTransition(label = "onlineLobbyShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "onlineLobbyShimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(baseColor, highlightColor, baseColor),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 350f, translateAnim + 350f)
            )
        )
}

/**
 * Loading Skeleton untuk OnlineQuizLobby
 */
@Composable
fun OnlineQuizLobbySkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Bar Shimmer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .onlineLobbyShimmer(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(20.dp)
                        .onlineLobbyShimmer(RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .onlineLobbyShimmer(CircleShape)
                )
            }
        }

        // Tab Row Shimmer (Deskripsi / Leaderboard)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .onlineLobbyShimmer(RoundedCornerShape(10.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .onlineLobbyShimmer(RoundedCornerShape(10.dp))
                )
            }
        }

        // Body Content Shimmer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp)
        ) {
            // Hero Map Banner Card Shimmer (Tinggi 200dp tepat sesuai banner kuis)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .onlineLobbyShimmer(RoundedCornerShape(22.dp))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 20.dp, end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(75.dp)
                                    .height(22.dp)
                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .height(20.dp)
                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }

            // Details / Rules Info Card Shimmer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(18.dp)
                                .onlineLobbyShimmer(RoundedCornerShape(4.dp))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .onlineLobbyShimmer(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }

                // Action Buttons Shimmer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .onlineLobbyShimmer(RoundedCornerShape(14.dp))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .onlineLobbyShimmer(RoundedCornerShape(14.dp))
                    )
                }
            }
        }
    }
}
