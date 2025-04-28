package com.LambdaProject.MathArt.ui.Pages.Profile

import com.LambdaProject.MathArt.BottomNavigationMenu
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.ViewModels.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(navController: NavController) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val username by profileViewModel.username
    val email by profileViewModel.email
    val studyDuration by profileViewModel.studyDuration
    val activeSessions = profileViewModel.activeSessions
    val unlockedAchievements = profileViewModel.unlockedAchievements

    var isLoggingOut by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggingOut) {
        if (isLoggingOut) {
            delay(1200)
            profileViewModel.logout {
                /* Toast.makeText(context, "Berhasil Logout", Toast.LENGTH_SHORT).show() */
                navController.navigate("login") {
                    popUpTo("dashboard/{username}") { inclusive = true}
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationMenu(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFFFFFFF)),
            verticalArrangement = Arrangement.Top
        ) {
            ProfileHeader(username, email)
            ProfileBody(
                navController = navController,
                studyDuration = studyDuration,
                activeSessions = activeSessions,
                unlockedAchievements = unlockedAchievements,
                isLoggingOut = isLoggingOut,
                onLogoutClicked = { isLoggingOut = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun formatDuration(durationInMillis: Long): String {
    val totalMinutes = durationInMillis / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "$hours jam $minutes menit"
}