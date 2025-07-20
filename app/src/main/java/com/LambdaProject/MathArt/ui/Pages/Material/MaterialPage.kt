package com.LambdaProject.MathArt.ui.Pages.Material

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
import com.LambdaProject.MathArt.BottomNavigationMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialScreen(userId: String, materialId: String, navController: NavController) {

    val tabs = listOf("Pengantar", "Translasi", "Refleksi", "Rotasi", "Dilatasi", "Kuis", "Hasil Belajar")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val quizViewModel = remember { QuizViewModel() }
    val maxAccessiblePage = remember { mutableIntStateOf(0) }
    val materialId = "transformasi_geometri"
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(true) {
        if (userId != null) {
            Firebase.firestore.collection("userProgress").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val maxPage = document.getLong("maxPage")?.toInt() ?: 0
                    maxAccessiblePage.intValue = maxPage
                }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (userId != null && pagerState.currentPage > maxAccessiblePage.intValue) {
            updateAccessiblePage(userId, pagerState.currentPage)
            maxAccessiblePage.intValue = pagerState.currentPage
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transformasi Geometri", fontWeight = FontWeight.Bold, fontFamily = interFontFamily) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFf7f7f7))
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                containerColor = Color.White,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .height(3.dp),
                        color = Color(0xFF5294FF)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    val isLocked = index > maxAccessiblePage.intValue

                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            if (!isLocked) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(title, fontFamily = interFontFamily, fontWeight = FontWeight.Bold, color = Color.Black)
                                if (isLocked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        enabled = !isLocked
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                fun goToNextPage(nextPage: Int) {
                    if (userId != null && nextPage > maxAccessiblePage.intValue) {
                        updateAccessiblePage(userId, nextPage)
                        maxAccessiblePage.intValue = nextPage
                    }
                    coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
                }
                when (page) {
                    0 -> MateriPengantar(
                        currentPage = pagerState.currentPage,
                        myPage = 0,
                        onNext = {
                            goToNextPage(1)
                        }
                    )
                    1 -> MateriTranslasi(
                        currentPage = pagerState.currentPage,
                        myPage = 1,
                        onNext = {
                            goToNextPage(2)
                        }
                    )
                    2 -> MateriRefleksi(
                        currentPage = pagerState.currentPage,
                        myPage = 2,
                        onNext = {
                            goToNextPage(3)
                        }
                    )
                    3 -> MateriRotasi(
                        currentPage = pagerState.currentPage,
                        myPage = 3,
                        onNext = {
                            goToNextPage(4)
                        }
                    )
                    4 -> MateriDilatasi(
                        currentPage = pagerState.currentPage,
                        myPage = 4,
                        onNext = {
                            goToNextPage(5)
                        },
                        onQuizNavigate = {
                            goToNextPage(5)
                        }
                    )

                    5 -> {
                        val isQuizReady by quizViewModel.isQuizReady

                        LaunchedEffect(page) {
                            quizViewModel.prepareQuiz(requireNotNull(userId), materialId)
                        }

                        if (isQuizReady) {
                            QuizScreen(
                                currentPage = pagerState.currentPage,
                                myPage = 5,
                                viewModel = quizViewModel,
                                onQuizFinished = {
                                    quizViewModel.resetQuizReadyState()
                                    goToNextPage(6)
                                },
                                userId = requireNotNull(userId),
                                materialId = materialId
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    6 -> SummaryScreen(
                        viewModel = quizViewModel,
                        onBackToDashboard = {
                            navController.navigate("dashboard/{userName}") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
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