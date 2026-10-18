package com.LambdaProject.MathArt.ui.Pages.Material

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.*
import com.LambdaProject.MathArt.ViewModels.QuizViewModel
import kotlinx.coroutines.launch
import com.LambdaProject.MathArt.data.repository.MaterialRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.LambdaProject.MathArt.data.model.MaterialDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialScreen(userId: String, materialId: String, navController: NavController) {

    val auth = remember { FirebaseAuth.getInstance() }
    val currentUserId = remember { auth.currentUser?.uid ?: "" }
    val currentMaterialId = materialId.ifBlank { "transformasi_geometri" }

    var isLoadingProgress by remember { mutableStateOf(true) }
    var isLoadingMaterial by remember { mutableStateOf(true) }
    var materialDoc by remember { mutableStateOf<MaterialDocument?>(null) }
    val maxAccessiblePage = remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val quizViewModel = remember { QuizViewModel() }

    LaunchedEffect(currentMaterialId) {
        isLoadingMaterial = true
        MaterialRepository.getMaterial(
            materialId = currentMaterialId,
            onSuccess = { doc ->
                materialDoc = doc
                isLoadingMaterial = false
            },
            onError = {
                isLoadingMaterial = false
            }
        )
    }

    LaunchedEffect(currentUserId, currentMaterialId) {
        if (currentUserId.isNotEmpty()) {
            MaterialRepository.getUserMaxPage(currentUserId, currentMaterialId) { maxPage ->
                maxAccessiblePage.intValue = maxPage
                isLoadingProgress = false
            }
        } else {
            isLoadingProgress = false
        }
    }

    if (isLoadingProgress || isLoadingMaterial) {
        MaterialPageSkeleton()
        return
    }

    val sections = materialDoc?.sections ?: emptyList()
    val hasQuiz = !materialDoc?.quizId.isNullOrBlank() || materialDoc?.quiz != null

    val tabs = remember(sections, hasQuiz) {
        val list = mutableListOf<String>()
        if (sections.isNotEmpty()) {
            list.addAll(sections.map { it.title })
        } else {
            list.add("Materi")
        }
        if (hasQuiz) {
            list.add("Kuis")
        }
        list.add("Hasil Belajar")
        list
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = materialDoc?.title?.ifBlank { "Transformasi Geometri" } ?: "Transformasi Geometri",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFontFamily,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        YouTubePlayerManager.pauseAll()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A237E)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FE))
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                containerColor = Color.White,
                divider = {},
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .background(
                                color = Color(0xFF5294FF).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF5294FF),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = pagerState.currentPage == index
                    val isLocked = index > maxAccessiblePage.intValue
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF1976D2) else if (isLocked) Color.LightGray else Color.Gray,
                        label = "tabColor"
                    )

                    Tab(
                        selected = isSelected,
                        onClick = {
                            if (!isLocked) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        enabled = !isLocked,
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = interFontFamily,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = contentColor
                            )
                            if (isLocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Terkunci",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }

            // Konten Materi
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    fun goToNextPage(nextPage: Int) {
                        if (currentUserId.isNotEmpty() && nextPage > maxAccessiblePage.intValue) {
                            MaterialRepository.updateAccessiblePage(currentUserId, currentMaterialId, nextPage)
                            maxAccessiblePage.intValue = nextPage
                        }
                        coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
                    }

                    val totalSections = if (sections.isNotEmpty()) sections.size else 1
                    val quizPageIndex = if (hasQuiz) totalSections else -1
                    val summaryPageIndex = if (hasQuiz) totalSections + 1 else totalSections

                    when {
                        page < totalSections -> {
                            if (sections.isNotEmpty()) {
                                val currentSection = sections[page]
                                val isLast = page == totalSections - 1
                                DynamicSectionScreen(
                                    section = currentSection,
                                    currentPage = pagerState.currentPage,
                                    myPage = page,
                                    isLastSection = isLast,
                                    nextSectionTitle = if (!isLast) sections.getOrNull(page + 1)?.title else null,
                                    hasQuiz = hasQuiz,
                                    onNext = { goToNextPage(page + 1) },
                                    onQuizNavigate = {
                                        if (hasQuiz) {
                                            goToNextPage(quizPageIndex)
                                        } else {
                                            goToNextPage(summaryPageIndex)
                                        }
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Konten materi belum tersedia.",
                                        fontFamily = interFontFamily,
                                        fontSize = 15.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        hasQuiz && page == quizPageIndex -> {
                            val isQuizReady by quizViewModel.isQuizReady
                            val targetQuizId = materialDoc?.quizId?.ifBlank { currentMaterialId } ?: currentMaterialId
                            LaunchedEffect(page, currentUserId) {
                                if (currentUserId.isNotEmpty() && !isQuizReady) {
                                    quizViewModel.prepareQuiz(currentUserId, targetQuizId)
                                }
                            }

                            if (isQuizReady) {
                                QuizScreen(
                                    currentPage = pagerState.currentPage,
                                    userId = currentUserId,
                                    myPage = quizPageIndex,
                                    viewModel = quizViewModel,
                                    onQuizFinished = {
                                        quizViewModel.resetQuizReadyState()
                                        goToNextPage(summaryPageIndex)
                                    },
                                    materialId = targetQuizId
                                )
                            } else {
                                QuizScreenSkeleton()
                            }
                        }
                        page == summaryPageIndex -> {
                            SummaryScreen(
                                viewModel = quizViewModel,
                                materialDoc = materialDoc,
                                materialId = currentMaterialId,
                                onRepeatMaterial = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                },
                                onBackToDashboard = {
                                    val currentUserName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "User"
                                    navController.navigate("dashboard/$currentUserName") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun updateAccessiblePage(userId: String, newMaxPage: Int) {
    MaterialRepository.updateAccessiblePage(userId, "", newMaxPage)
}

fun resetAccessiblePage(userId: String) {
    MaterialRepository.resetAccessiblePage(userId)
}