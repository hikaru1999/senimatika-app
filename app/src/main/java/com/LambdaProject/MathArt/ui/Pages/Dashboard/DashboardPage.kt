package com.LambdaProject.MathArt.ui.Pages.Dashboard

import com.LambdaProject.MathArt.Data.*
import com.LambdaProject.MathArt.model.MaterialItem

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.LambdaProject.MathArt.BottomNavigationMenu
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

@Composable
fun DashboardScreen(navController: NavController, userName: String, viewModel: DashboardViewModel = viewModel()) {
    var showRangkumanCard by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<MaterialItem?>(null) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            val userDoc = Firebase.firestore.collection("users").document(currentUserId).get().await()

            if (userDoc.exists())
            {
                viewModel.loadUserProfile(currentUserId)
                viewModel.checkActiveSessions(currentUserId, sampleMaterials)
            } else {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo("dashboard/${viewModel.username}") { inclusive = true }
                }
            }
        } else {
            navController.navigate("login") {
                popUpTo("dashboard/${viewModel.username}") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationMenu(navController) },
        containerColor = Color(0xFFf7f7f7)
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues))
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                DashboardHeader(navController, userName, viewModel)
                DashboardBody(
                    navController = navController,
                    viewModel = viewModel,
                    onMaterialSelected = { material ->
                        selectedMaterial = material
                        showRangkumanCard = true
                    }
                )
            }
            AnimatedVisibility(
                visible = showRangkumanCard,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                selectedMaterial?.let { material ->
                    if (showRangkumanCard /* && selectedMaterial != null */)
                    {
                        RangkumanCard(
                            material = selectedMaterial!!,
                            userId = currentUserId,
                            onClose = { showRangkumanCard = false },
                            onSesiDisimpan = {
                                showRangkumanCard = false
                                navController.navigate("material_screen/${userId}/${material.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}