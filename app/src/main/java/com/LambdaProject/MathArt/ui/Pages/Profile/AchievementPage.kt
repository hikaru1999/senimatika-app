package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
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
import com.LambdaProject.MathArt.Data.sampleAchievements
import com.LambdaProject.MathArt.ViewModels.AchievementViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.AchievementItem
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementPage(navController: NavController) {
    val viewModel: AchievementViewModel = viewModel()
    val unlockedAchievements = viewModel.unlockedAchievements
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId == null) {
        Text("User belum login")
        return
    }

    LaunchedEffect(userId) {
        viewModel.fetchUserAchievements(userId)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lencanaku",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(sampleAchievements) { achievement ->
                val isUnlocked = achievement.name in unlockedAchievements
                AchievementRowItem(achievement, isUnlocked = isUnlocked)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun AchievementRowItem(achievement: AchievementItem, isUnlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(72.dp)
        ) {
            Image(
                painter = painterResource(id = achievement.imageRes),
                contentDescription = achievement.name,
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer { alpha = if (isUnlocked) 1f else 0.5f },
                colorFilter = if (isUnlocked) null else ColorFilter.tint(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.name,
                fontSize = 12.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.Black else Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(26.dp))

        Text(
            text = achievement.description,
            fontSize = 13.sp,
            fontFamily = interFontFamily,
            color = if (isUnlocked) Color.Black else Color.Gray,
            modifier = Modifier.weight(1f),
        )
    }
}
