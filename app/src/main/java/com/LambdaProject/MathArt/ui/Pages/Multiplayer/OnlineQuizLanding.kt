package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.BottomNavigationMenu
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.LambdaProject.MathArt.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineQuizPage(
    navController: NavController,
    viewModel: OnlineQuizViewModel = hiltViewModel(),
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val materials by viewModel.materials.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val filteredMaterials = remember(searchQuery, materials) {
        materials.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        bottomBar = { BottomNavigationMenu(navController) },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.refreshMaterials()
                    kotlinx.coroutines.delay(800)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header & Search Box Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
                    ) {
                        // Title, Subtitle, & Themed Quiz Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = (-0.5).sp
                                            )
                                        ) {
                                            append("Math ")
                                        }
                                        withStyle(
                                            SpanStyle(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFF2563EB), // Royal Blue
                                                        Color(0xFF0284C7), // Sky Blue
                                                        Color(0xFF06B6D4)  // Cyan
                                                    )
                                                ),
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-0.5).sp
                                            )
                                        ) {
                                            append("Arena")
                                        }
                                    },
                                    fontFamily = interFontFamily,
                                    fontSize = 28.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Sudah sejauh mana pengetahuan matematikamu?",
                                    fontFamily = interFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B),
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Themed Quiz Icon Container
                            Surface(
                                modifier = Modifier.size(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_quiz_icon),
                                        contentDescription = "Math Quiz Icon",
                                        modifier = Modifier.size(28.dp),
                                        tint = Color(0xFF2563EB)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Cari kuis atau materi matematika...",
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Hapus",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            singleLine = true
                        )
                    }
                }

                val isInitialLoading = materials.isEmpty() && searchQuery.isEmpty()

                if (isRefreshing || isInitialLoading) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 16.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            OnlineQuizBannerSkeleton()
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(18.dp)
                                        .quizLandingShimmer(RoundedCornerShape(4.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(18.dp)
                                        .quizLandingShimmer(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                        items(3) {
                            OnlineQuizCardSkeleton()
                        }
                    }
                } else if (filteredMaterials.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEEF2FF),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Kuis Tidak Ditemukan",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Tidak ada kuis yang cocok dengan kata kunci '$searchQuery'." else "Tarik ke bawah untuk memuat ulang daftar kuis.",
                                fontFamily = interFontFamily,
                                fontSize = 12.5.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 16.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header Banner
                        item {
                            Surface(
                                color = Color(0xFFEEF2FF),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Quiz,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = buildAnnotatedString {
                                                append("Tantang Dirimu di ")
                                                withStyle(
                                                    SpanStyle(
                                                        color = Color(0xFF0F172A),
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                ) {
                                                    append("Math ")
                                                }
                                                withStyle(
                                                    SpanStyle(
                                                        brush = Brush.horizontalGradient(
                                                            colors = listOf(
                                                                Color(0xFF2563EB), // Royal Blue
                                                                Color(0xFF0284C7), // Sky Blue
                                                                Color(0xFF06B6D4)  // Cyan
                                                            )
                                                        ),
                                                        fontWeight = FontWeight.Black
                                                    )
                                                ) {
                                                    append("Arena")
                                                }
                                                append("!")
                                            },
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Selesaikan kuis, raih poin, dan jadilah nomor satu di Leaderboard!",
                                            fontFamily = interFontFamily,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Section Title
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Kuis Tersedia",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.5.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${filteredMaterials.size} Kuis",
                                    fontFamily = interFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6366F1)
                                )
                            }
                        }

                        items(
                            items = filteredMaterials,
                            key = { it.id }
                        ) { material ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(
                                        fadeInSpec = tween(durationMillis = 400),
                                        placementSpec = spring(stiffness = Spring.StiffnessLow),
                                        fadeOutSpec = tween(durationMillis = 200)
                                    )
                            ) {
                                OnlineQuizCard(
                                    material = material,
                                    onQuizClick = {
                                        val initialTab = "description"
                                        viewModel.selectMaterial(material)
                                        navController.navigate("OnlineQuizLobby/${userId}/${initialTab}")
                                    }
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
fun Modifier.quizLandingShimmer(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF1F5F9)
): Modifier {
    val transition = rememberInfiniteTransition(label = "quizLandingShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(baseColor, highlightColor, baseColor),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 300f, translateAnim + 300f)
            )
        )
}

@Composable
fun OnlineQuizBannerSkeleton() {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .quizLandingShimmer(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(16.dp)
                        .quizLandingShimmer(RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(12.dp)
                        .quizLandingShimmer(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun OnlineQuizCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0xFF4F46E5).copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column {
            // Thumbnail Skeleton with Badge Skeletons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .quizLandingShimmer(
                        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                        baseColor = Color(0xFFE2E8F0),
                        highlightColor = Color(0xFFF8FAFC)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(65.dp)
                            .height(24.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(24.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    )
                }
            }

            // Card Content Skeleton
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(18.dp)
                            .quizLandingShimmer(RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(14.dp)
                            .quizLandingShimmer(RoundedCornerShape(4.dp))
                    )
                }

                // Action Button Skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .quizLandingShimmer(RoundedCornerShape(14.dp))
                )
            }
        }
    }
}
