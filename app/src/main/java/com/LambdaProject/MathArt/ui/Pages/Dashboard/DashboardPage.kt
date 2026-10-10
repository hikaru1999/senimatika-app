package com.LambdaProject.MathArt.ui.Pages.Dashboard

import com.LambdaProject.MathArt.data.*
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.data.model.AchievementItem
import com.LambdaProject.MathArt.UniversalSnackbar
import com.LambdaProject.MathArt.R

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.LambdaProject.MathArt.BottomNavigationMenu
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// =========================================================================
// [DEBUG] Flag pengujian tema waktu DashboardHeader (Pagi/Siang/Sore/Malam).
// Ubah menjadi false sebelum rilis/deploy.
// =========================================================================
private const val ENABLE_TEST_HEADER_THEME = false

// =========================================================================
// [DEBUG] Flag pengujian UniversalSnackbar.
// Ubah menjadi false atau hapus blok terkait sebelum deploy/rilis.
// =========================================================================
private const val ENABLE_TEST_SNACKBAR = false

@Composable
fun DashboardScreen(navController: NavController, userName: String, viewModel: DashboardViewModel = viewModel()) {
    var showRangkumanCard by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<MaterialItem?>(null) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    // State untuk pengujian tema waktu DashboardHeader
    var testTimeIndex by remember { mutableIntStateOf(0) } // 0: Auto, 1: Pagi (8), 2: Siang (13), 3: Sore (16), 4: Malam (20)
    val testOverrideHour = when (testTimeIndex) {
        1 -> 8
        2 -> 13
        3 -> 16
        4 -> 20
        else -> null
    }

    // State untuk pengujian UniversalSnackbar
    var testSnackbarVisible by remember { mutableStateOf(false) }
    var testSnackbarMode by remember { mutableIntStateOf(0) } // 0: Achievement, 1: Network Lost, 2: Connected

    LaunchedEffect(testSnackbarVisible, testSnackbarMode) {
        if (testSnackbarVisible) {
            delay(4000)
            testSnackbarVisible = false
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.loadUserProfile(currentUserId)
            viewModel.checkActiveSessions(currentUserId, DataMaterials)
            viewModel.listenForProgress(currentUserId)
        } else {
            navController.navigate("login") {
                popUpTo("dashboard/${viewModel.username.value}") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationMenu(navController) },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                DashboardHeader(
                    navController = navController,
                    viewModel = viewModel,
                    overrideHour = if (ENABLE_TEST_HEADER_THEME) testOverrideHour else null
                )
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
                    RangkumanCard(
                        material = material,
                        userId = currentUserId,
                        onClose = { showRangkumanCard = false },
                        onSesiDisimpan = {
                            showRangkumanCard = false
                            navController.navigate("material_screen/${userId}/${material.id}")
                        }
                    )
                }
            }

            // =========================================================================
            // [DEBUG / TEST] Tombol Floating untuk menguji Tema Waktu DashboardHeader
            // =========================================================================
            if (ENABLE_TEST_HEADER_THEME) {
                Surface(
                    onClick = {
                        testTimeIndex = (testTimeIndex + 1) % 5
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.2.dp, Color(0xFF6366F1))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (testTimeIndex) {
                                1 -> "🌅"
                                2 -> "☀️"
                                3 -> "🌇"
                                4 -> "🌙"
                                else -> "⏱️"
                            },
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (testTimeIndex) {
                                1 -> "Uji: Pagi (08:00)"
                                2 -> "Uji: Siang (13:00)"
                                3 -> "Uji: Sore (16:00)"
                                4 -> "Uji: Malam (20:00)"
                                else -> "Waktu Sistem (Auto)"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.LambdaProject.MathArt.interFontFamily
                        )
                    }
                }
            }

            // =========================================================================
            // [DEBUG / TEST] Tombol Floating untuk menguji UniversalSnackbar
            // =========================================================================
            if (ENABLE_TEST_SNACKBAR) {
                Surface(
                    onClick = {
                        testSnackbarMode = (testSnackbarMode + 1) % 3
                        testSnackbarVisible = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.2.dp, Color(0xFF3B82F6))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧪", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Snackbar (${when (testSnackbarMode) {
                                0 -> "Achievement"
                                1 -> "Network Lost"
                                else -> "Connected"
                            }})",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Render UniversalSnackbar untuk pengetesan di Dashboard
                UniversalSnackbar(
                    achievementItem = if (testSnackbarMode == 0) AchievementItem(name = "Master Geometri", imageRes = R.drawable.ic_geometry_master, isUnlocked = true) else null,
                    isNetworkLost = testSnackbarMode == 1,
                    isSuccess = testSnackbarMode == 2,
                    remainingSeconds = 12,
                    visible = testSnackbarVisible
                )
            }
        }
    }
}