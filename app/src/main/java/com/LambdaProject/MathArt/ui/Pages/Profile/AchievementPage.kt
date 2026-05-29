package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LambdaProject.MathArt.data.DataAchievements
import com.LambdaProject.MathArt.ViewModels.AchievementViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.AchievementItem
import androidx.compose.ui.tooling.preview.Preview
import com.LambdaProject.MathArt.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementPage(navController: NavController) {
    val viewModel: AchievementViewModel = viewModel()
    val unlockedAchievements = viewModel.unlockedAchievements
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    val sortedDataAchievements by remember(unlockedAchievements) {
        derivedStateOf {
            DataAchievements.sortedWith(
                compareByDescending<AchievementItem> { it.name in unlockedAchievements }
                    .thenBy { DataAchievements.indexOf(it) }
            )
        }
    }

    if (userId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User belum login", fontFamily = interFontFamily)
        }
        return
    }

    LaunchedEffect(userId) {
        viewModel.fetchUserAchievements(userId)
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FE),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lencanaku",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A237E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color(0xFF1A237E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isScrolled) Color.White else Color.Transparent,
                    scrolledContainerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = 24.dp,
                start = 20.dp,
                end = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                AchievementHeaderSummary(
                    total = DataAchievements.size,
                    unlocked = unlockedAchievements.size
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(sortedDataAchievements) { achievement ->
                val isUnlocked = achievement.name in unlockedAchievements
                AchievementRowItem(achievement, isUnlocked = isUnlocked)
            }
        }
    }
}

@Composable
fun AchievementHeaderSummary(total: Int, unlocked: Int) {
    val progress = if (total > 0) unlocked.toFloat() / total else 0f
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Progres Koleksi",
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "$unlocked/$total Lencana",
                        fontFamily = interFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A237E)
                    )
                }
                
                // Progress Circle
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(56.dp),
                        color = Color(0xFFF5F5F5),
                        strokeWidth = 6.dp,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(56.dp),
                        color = Color(0xFF1976D2),
                        strokeWidth = 6.dp,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementRowItem(achievement: AchievementItem, isUnlocked: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isUnlocked) 6.dp else 2.dp, 
                shape = RoundedCornerShape(20.dp)
            ),
        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp),
        border = if (isUnlocked) null else BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        color = if (isUnlocked) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = achievement.imageRes),
                    contentDescription = achievement.name,
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer { 
                            alpha = if (isUnlocked) 1f else 0.4f
                            if (!isUnlocked) {
                                rotationZ = -10f
                            }
                        },
                    colorFilter = if (isUnlocked) null else ColorFilter.tint(Color.Gray),
                    contentScale = ContentScale.Fit
                )
                
                if (!isUnlocked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = "Locked",
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    fontSize = 16.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUnlocked) Color(0xFF1A237E) else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    color = if (isUnlocked) Color.DarkGray else Color.LightGray
                )
                
                if (isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF81C784).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "DIBUKA",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
