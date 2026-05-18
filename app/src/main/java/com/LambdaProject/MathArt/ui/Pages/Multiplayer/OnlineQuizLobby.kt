package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.OnlineQuizDesc
import com.LambdaProject.MathArt.data.model.OnlineUser
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
    val tabKeys = listOf("description", "leaderboard")
    val tabLabels = listOf("Deskripsi", "Leaderboard")
    val initialPageIndex = tabKeys.indexOf(initialTab.lowercase()).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { tabKeys.size })
    val selectedMaterial by viewModel.selectedMaterial.collectAsState()
    val userOnlineList by viewModel.userOnlineList.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(questions) {
        if (questions.isNotEmpty()) {
            navController.navigate("StartQuiz")
        }
    }

    LaunchedEffect(true) {
        viewModel.loadUsersOnline()
    }

    LaunchedEffect(Unit) {
        viewModel.startListeningForChallenges()
    }

    LaunchedEffect(userOnlineList, selectedMaterial) {
        selectedMaterial?.let { material ->
            val currentMaterialId = material.id
            viewModel.fetchLeaderboardForMaterial(currentMaterialId)
        }
    }

    selectedMaterial?.let { material ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            material.title,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFontFamily,
                            color = Color(0xFF1A237E)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack()}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF1A237E))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FE))
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.White,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .height(3.dp),
                            color = Color(0xFF1976D2)
                        )
                    },
                    divider = {}
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
                                Text(
                                    title, 
                                    fontWeight = if (pagerState.currentPage == index) FontWeight.Black else FontWeight.Bold, 
                                    fontFamily = interFontFamily, 
                                    color = if (pagerState.currentPage == index) Color(0xFF1976D2) else Color.Gray
                                )
                            }
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (tabKeys[page]) {
                        "description" -> DeskripsiContent(
                            material = material,
                            userOnlineList = userOnlineList,
                            onUserSelected = onUserSelected,
                            navController = navController,
                            userId = userId
                        )
                        "leaderboard" -> LeaderboardTab(materialId = selectedMaterial?.id ?: "")
                    }
                }
            }
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF1976D2))
        }
    }
}

@Composable
private fun DeskripsiContent(
    material: OnlineQuizDesc,
    userOnlineList: List<OnlineUser>,
    onUserSelected: (String) -> Unit,
    navController: NavController,
    userId: String
) {
    val viewModel: OnlineQuizViewModel = hiltViewModel()
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Hero Image Section with shadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                if (material.imageRes != 0) {
                    Image(
                        painter = painterResource(id = material.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(12.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp,vertical = 24.dp)
            ) {
                Text(
                    text = "Detail Kuis",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )

                Spacer(Modifier.height(20.dp))

                // Modern Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuizStatItem(
                        icon = R.drawable.ic_no_question,
                        label = "Soal",
                        value = "${material.questions.size}",
                        modifier = Modifier.weight(1f)
                    )
                    QuizStatItem(
                        icon = R.drawable.ic_stopwatch,
                        label = "Menit",
                        value = "${material.questions.sumOf { it.timer } / 60}",
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        QuizStatItem(
                            icon = R.drawable.ic_coin,
                            label = "Coin",
                            value = "${material.rewardCoin}",
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Tag Badge
                        Surface(
                            color = Color(0xFFFFD600),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-12).dp)
                                /* .shadow(4.dp, RoundedCornerShape(4.dp)) */
                        ) {
                            Text(
                                text = "First Time Reward",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = interFontFamily,
                                color = Color(0xFF1A237E),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Deskripsi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = material.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontFamily = interFontFamily,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Justify
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Action Bar at Bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 16.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        isLoading = true
                        viewModel.selectMaterial(material)
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(2000)
                            navController.navigate("StartQuiz/${material.id}/${userId}")
                            isLoading = false
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF1976D2))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text("MULAI KUIS", fontWeight = FontWeight.Black, fontFamily = interFontFamily, letterSpacing = 1.sp)
                    }
                }

                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    enabled = false
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DUEL", fontWeight = FontWeight.Bold, fontFamily = interFontFamily, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun QuizStatItem(icon: Int, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontFamily = interFontFamily, fontWeight = FontWeight.Black, color = Color(0xFF1A237E))
            Text(label, fontSize = 10.sp, fontFamily = interFontFamily, fontWeight = FontWeight.Bold, color = Color.Gray)
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Top Rank",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val maxHeightDp = maxHeight

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        LeaderBox(
                            username = topThree.getOrNull(1)?.username ?: "-",
                            points = topThree.getOrNull(1)?.points ?: 0,
                            medalType = MedalType.SILVER,
                            medalColor = Color(0xFFC0C0C0),
                            modifier = Modifier.weight(1f),
                            heightFraction = 0.5f,
                            maxHeightDp = maxHeightDp
                        )

                        LeaderBox(
                            username = topThree.getOrNull(0)?.username ?: "-",
                            points = topThree.getOrNull(0)?.points ?: 0,
                            medalColor = Color(0xFFFFD700),
                            modifier = Modifier.weight(1f),
                            heightFraction = 0.8f,
                            medalType = MedalType.GOLD,
                            maxHeightDp = maxHeightDp
                        )

                        LeaderBox(
                            username = topThree.getOrNull(2)?.username ?: "—",
                            points = topThree.getOrNull(2)?.points ?: 0,
                            medalColor = Color(0xFFCD7F32),
                            modifier = Modifier.weight(1f),
                            heightFraction = 0.4f,
                            medalType = MedalType.BRONZE,
                            maxHeightDp = maxHeightDp
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color(0xFF1976D2),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Text(
                    text = "Peringkat 50 Besar",
                    fontWeight = FontWeight.Black,
                    fontFamily = interFontFamily,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    if (others.isNotEmpty()) {
                        others.forEachIndexed { index, user ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${index + 4}",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Black,
                                            fontFamily = interFontFamily,
                                            modifier = Modifier.width(32.dp)
                                        )
                                        Text(
                                            text = user.username,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFontFamily,
                                        )
                                    }
                                    Text(
                                        text = "${user.points} pts",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada peserta lainnya",
                                color = Color.White.copy(alpha = 0.6f),
                                fontStyle = FontStyle.Italic,
                                fontFamily = interFontFamily
                            )
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
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    val (gradientColors) = when (medalType) {
        MedalType.GOLD -> listOf(Color(0xFFFFD700), Color(0xFFFFA500)) to Color(0xFFFFD700)
        MedalType.SILVER -> listOf(Color(0xFFD3D3D3), Color(0xFFB0C4DE)) to Color(0xFFC0C0C0)
        MedalType.BRONZE -> listOf(Color(0xFFCD7F32), Color(0xFF8B4513)) to Color(0xFFCD7F32)
    }

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_profile),
            contentDescription = null,
            modifier = Modifier
                .size(if (medalType == MedalType.GOLD) 56.dp else 48.dp)
                .clip(CircleShape)
                .border(2.dp, medalColor, CircleShape)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = username,
            fontSize = 11.sp,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1A237E),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .height(heightAnim.value.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(brush = Brush.verticalGradient(gradientColors)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${points} pts",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily,
                        color = Color(0xFF1A237E)
                    )
                }
            }
        }
    }
}

enum class MedalType {
    GOLD, SILVER, BRONZE
}
