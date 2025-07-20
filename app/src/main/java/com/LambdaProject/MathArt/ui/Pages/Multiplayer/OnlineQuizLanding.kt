package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.LambdaProject.MathArt.BottomNavigationMenu
import com.google.firebase.auth.FirebaseAuth

@Composable
fun OnlineQuizPage(
    navController: NavController,
    viewModel: OnlineQuizViewModel = hiltViewModel(),
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val materials by viewModel.materials.collectAsState()

    Scaffold(
        bottomBar = { BottomNavigationMenu(navController) },
        containerColor = Color(0xFFF7F7F7)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(top = 10.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Optional header
                /* item {
                    Text(
                        text = "Pilih Kuis Online",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } */

                items(materials) { material ->
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