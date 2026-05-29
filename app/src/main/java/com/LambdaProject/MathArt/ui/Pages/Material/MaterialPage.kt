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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialScreen(userId: String, materialId: String, navController: NavController) {

    val tabs = listOf("Pengantar", "Translasi", "Refleksi", "Rotasi", "Dilatasi", "Kuis", "Hasil Belajar")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val quizViewModel = remember { QuizViewModel() }
    val maxAccessiblePage = remember { mutableIntStateOf(0) }

    val auth = remember { FirebaseAuth.getInstance() }
    val currentUserId = remember { auth.currentUser?.uid ?: "" }
    val currentMaterialId = materialId.ifBlank { "transformasi_geometri" }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            Firebase.firestore.collection("userProgress").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    val maxPage = document.getLong("maxPage")?.toInt() ?: 0
                    maxAccessiblePage.intValue = maxPage
                }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Transformasi Geometri",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFontFamily,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                            updateAccessiblePage(currentUserId, nextPage)
                            maxAccessiblePage.intValue = nextPage
                        }
                        coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
                    }

                    when (page) {
                        0 -> MateriPengantar(currentPage = pagerState.currentPage, myPage = 0, onNext = { goToNextPage(1) })
                        1 -> MateriTranslasi(currentPage = pagerState.currentPage, myPage = 1, onNext = { goToNextPage(2) })
                        2 -> MateriRefleksi(currentPage = pagerState.currentPage, myPage = 2, onNext = { goToNextPage(3) })
                        3 -> MateriRotasi(currentPage = pagerState.currentPage, myPage = 3, onNext = { goToNextPage(4) })
                        4 -> MateriDilatasi(
                            currentPage = pagerState.currentPage,
                            myPage = 4,
                            onNext = { goToNextPage(5) },
                            onQuizNavigate = { goToNextPage(5) }
                        )
                        5 -> {
                            val isQuizReady by quizViewModel.isQuizReady
                            LaunchedEffect(page, currentUserId) {
                                if (currentUserId.isNotEmpty() && !isQuizReady) {
                                    quizViewModel.prepareQuiz(currentUserId, currentMaterialId)
                                }
                            }

                            if (isQuizReady) {
                                QuizScreen(
                                    currentPage = pagerState.currentPage,
                                    userId = currentUserId,
                                    myPage = 5,
                                    viewModel = quizViewModel,
                                    onQuizFinished = {
                                        quizViewModel.resetQuizReadyState()
                                        goToNextPage(6)
                                    },
                                    materialId = currentMaterialId
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(strokeWidth = 3.dp, color = Color(0xFF5294FF))
                                }
                            }
                        }
                        6 -> SummaryScreen(
                            viewModel = quizViewModel,
                            onBackToDashboard = {
                                navController.navigate("dashboard/{userName}") {
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

fun updateAccessiblePage(userId: String, newMaxPage: Int) {
    val db = Firebase.firestore
    val docRef = db.collection("userProgress").document(userId)

    docRef.get()
        .addOnSuccessListener { document ->
            val currentMaxPage = document.getLong("maxPage")?.toInt() ?: 0

            if (newMaxPage > currentMaxPage) {
                docRef.set(mapOf("maxPage" to newMaxPage), SetOptions.merge())
            }
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Failed to get maxPage", exception)
        }
}

fun resetAccessiblePage(userId: String) {
    val db = Firebase.firestore
    db.collection("userProgress").document(userId)
        .set(mapOf("maxPage" to 0), SetOptions.merge())
        .addOnSuccessListener {
            Log.d("Firestore", "maxPage reset to 0")
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Failed to reset maxPage", exception)
        }
}