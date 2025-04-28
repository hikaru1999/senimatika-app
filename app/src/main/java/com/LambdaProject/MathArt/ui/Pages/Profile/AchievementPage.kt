package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LambdaProject.MathArt.Data.sampleAchievements
import com.LambdaProject.MathArt.ViewModels.AchievementViewModel
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
                title = { Text("Lencanaku") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(21.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sampleAchievements) { achievement ->
                    val isUnlocked = achievement.name in unlockedAchievements
                    AchievementCard(achievement, isUnlocked = isUnlocked)
                }
            }
        }
    }
}