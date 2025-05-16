package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.OnlineQuizDesc
import com.LambdaProject.MathArt.model.OnlineUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


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

   /*  val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size }) */
    val selectedMaterial by viewModel.selectedMaterial.collectAsState()
    val userOnlineList by viewModel.userOnlineList.collectAsState()

    val questions by viewModel.questions.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(questions) {
        if (questions.isNotEmpty()) {
            Log.d("QuizLobby", "Navigasi ke startQuiz karena soal sudah terisi")
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
        // Pastikan selectedMaterial tidak null dan mengambil ID untuk fetch leaderboard
        selectedMaterial?.let { material ->
            val currentMaterialId = material.id
            Log.d("QuizLobby", "Fetching leaderboard for material: $currentMaterialId")
            /* Log.d("QuizLobby", "Online users: ${userOnlineList.size}") */
            viewModel.fetchLeaderboardForMaterial(currentMaterialId)
        }
    }

    selectedMaterial?.let { material ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {Text(material.title, fontWeight = FontWeight.Bold, fontFamily = interFontFamily) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack()}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFf7f7f7),
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFf7f7f7))
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color(0xFFf7f7f7),
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .height(3.dp),
                            color = Color(0xFF5294FF)
                        )
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
                                Text(title, fontWeight = FontWeight.Bold, fontFamily = interFontFamily, color = Color.Black)
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
            Text("Tidak ada kuis")
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(id = material.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(16.dp))

        Text("Deskripsi", fontWeight = FontWeight.Bold, fontSize = 28.sp, fontFamily = interFontFamily)

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min), // opsional: membuat semua box setinggi konten tertinggi
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color(0xFFC9C9C9), RoundedCornerShape(5.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_assignment), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${material.numberQuestion} Soal", fontSize = 12.sp, fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color(0xFFC9C9C9), RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_stopwatch), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${material.durationMinutes} Menit", fontSize = 12.sp, fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color(0xFFC9C9C9), RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.ic_coin), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("+${material.coints} Coin", fontSize = 12.sp, fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = material.description,
            fontSize = 14.sp,
            color = Color.DarkGray,
            fontFamily = interFontFamily,
            textAlign = TextAlign.Justify
        )
        Spacer(modifier = Modifier.height(24.dp))

        /* Text("Pengguna Online", fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = interFontFamily)
        Text("Tantang pengguna menyelesaiakan kuis (duel 1v1)", fontWeight = FontWeight.Medium, fontSize = 12.sp, fontFamily = interFontFamily)
        Spacer(modifier = Modifier.height(8.dp))

        if (userOnlineList.isEmpty()) {
            Text("Tidak ada user yang online", fontSize = 12.sp, color = Color.Gray, fontFamily = interFontFamily, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                items(userOnlineList) { user ->
                    OnlineUserCard(
                        username = user.username,
                        onUserClick = {
                            onUserSelected(user.uid)
                        }
                    )
                }
            }
        } */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = {
                    isLoading = true
                    viewModel.selectMaterial(material)
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2500)
                        Log.d("QuizDebug", "Material selected before navigate: ${viewModel.selectedMaterial.value?.id}")
                        navController.navigate("StartQuiz/${material.id}/${userId}")
                        isLoading = false
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E60DD))
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .widthIn(min = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Text(
                            text = "Mulai Mengerjakan",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {  },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF0E60DD)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0E60DD)),
                enabled = false
            ) {
                Text("Mode Duel 1v1", fontFamily = interFontFamily, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LeaderboardTab(
    materialId: String,
    viewModel: OnlineQuizViewModel = hiltViewModel()
) {
    val leaderboard by viewModel.leaderboard.collectAsState(initial = emptyList())
    val sortedLeaderboard = leaderboard.sortedByDescending { it.points }
    val topThree = sortedLeaderboard.take(3)
    val others = sortedLeaderboard.drop(3)

    /* var isExpanded by remember { mutableStateOf(false) } */
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val maxHeightFraction = 0.5f
    val fullHeightFraction = 1f

    /* val scrollState = rememberScrollState()
    val isScrolled = scrollState.value> 50 */

    val swipeableState = rememberSwipeableState(initialValue = maxHeightFraction) { newValue ->
        true
    }

    val anchors = mapOf(
        with(density) { (LocalConfiguration.current.screenHeightDp * density.density * maxHeightFraction).toFloat() to maxHeightFraction },
        with(density) { 0f to fullHeightFraction },
    )

    val goldHeight = remember { mutableStateOf(0.dp) }
    val silverHeight = remember { mutableStateOf(0.dp) }
    val bronzeHeight = remember { mutableStateOf(0.dp) }

    /* LaunchedEffect(scrollState.value) {
        isExpanded = scrollState.value > 50
    } */

    LaunchedEffect(Unit) {
        silverHeight.value = 80.dp
        goldHeight.value = 200.dp
        bronzeHeight.value = 60.dp
    }

    LaunchedEffect(materialId) {
        viewModel.fetchLeaderboardForMaterial(materialId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Leaderboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                LeaderBox(
                    username = topThree.getOrNull(1)?.username ?: "-",
                    points = topThree.getOrNull(1)?.points ?: 0,
                    medalColor = Color(0xFFC0C0C0),
                    modifier = Modifier.weight(1f),
                    targetHeight = silverHeight.value,
                    medalType = MedalType.SILVER
                )

                LeaderBox(
                    username = topThree.getOrNull(0)?.username ?: "-",
                    points = topThree.getOrNull(0)?.points ?: 0,
                    medalColor = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f),
                    targetHeight = goldHeight.value,
                    medalType = MedalType.GOLD
                )

                LeaderBox(
                    username = topThree.getOrNull(2)?.username ?: "—",
                    points = topThree.getOrNull(2)?.points ?: 0,
                    medalColor = Color(0xFFCD7F32),
                    modifier = Modifier.weight(1f),
                    targetHeight = bronzeHeight.value,
                    medalType = MedalType.BRONZE
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .background(Color.Transparent)
                .align(Alignment.BottomCenter),
                /* .padding(top = with(density) { (LocalConfiguration.current.screenHeightDp * density.density * maxHeightFraction - swipeableState.offset.value).coerceAtLeast(0f).toDp() }),*/
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color(0xFF53A8F1),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp, bottom = 0.dp, start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = "Peringkat 4 dan seterusnya",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFontFamily
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .fillMaxHeight()
                    ) {
                        if (others.isNotEmpty()) {
                            others.forEachIndexed { index, user ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 5.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp, horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${index + 4}",
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interFontFamily,
                                                modifier = Modifier.padding(end = 24.dp)
                                            )
                                            Text(
                                                text = user.username,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = interFontFamily,
                                            )
                                        }
                                        Text(
                                            text = "${user.points} pts",
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = interFontFamily
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Belum ada peserta lainnya",
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }

        /*AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    orientation = Orientation.Vertical,
                    resistance = null
                )
        ) {

        } */
    }
}

@Composable
private fun LeaderBox(
    username: String,
    points: Int,
    medalType: MedalType,
    medalColor: Color,
    modifier: Modifier = Modifier,
    targetHeight: Dp
) {
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "AnimatedHeight"
    )

    val (gradientColors) = when (medalType) {
        MedalType.GOLD -> listOf(Color(0xFFFFD700), Color(0xFFFFE135), Color(0xFFFFA500)) to Color(0xFFFFD700)
        MedalType.SILVER -> listOf(Color(0xFFD3D3D3), Color(0xFFE0E0E0), Color(0xFFB0C4DE)) to Color(0xFFC0C0C0)
        MedalType.BRONZE -> listOf(Color(0xFFCD7F32), Color(0xFFE6B280), Color(0xFF8B4513)) to Color(0xFFCD7F32)
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Medal Icon",
            tint = medalColor
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = username, style = MaterialTheme.typography.bodySmall, fontFamily = interFontFamily, fontWeight = FontWeight.Black)

        Spacer(modifier = Modifier.height(8.dp))
        // Box container
        Box(
            modifier = Modifier
                .height(animatedHeight)
                .width(100.dp)
                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                .background(brush = Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White)
                        .padding(5.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            AnimatedPts(value = points.toString())
                        }
                    }
                }
            }
        }
    }
}

enum class MedalType {
    GOLD, SILVER, BRONZE
}

@Composable
fun AnimatedPts(value: String) {
    val targetValue = value.toIntOrNull() ?: 0
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = 2500, easing = LinearOutSlowInEasing)
        )
    }

    Text(
        text = "${animatedValue.value.toInt()} pts",
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        fontFamily = interFontFamily,
        fontSize = 10.sp
    )
}