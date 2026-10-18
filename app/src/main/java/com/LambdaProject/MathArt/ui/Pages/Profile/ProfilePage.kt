package com.LambdaProject.MathArt.ui.Pages.Profile

import com.LambdaProject.MathArt.BottomNavigationMenu
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.ViewModels.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(navController: NavController) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val isLoading by profileViewModel.isLoading
    val username by profileViewModel.username
    val email by profileViewModel.email
    val fullName by profileViewModel.fullName
    val coins by profileViewModel.coins
    val avatar by profileViewModel.avatar
    val studyDuration by profileViewModel.studyDuration
    val activeSessions = profileViewModel.activeSessions
    val unlockedAchievements = profileViewModel.unlockedAchievements

    var isLoggingOut by remember { mutableStateOf(false) }
    var showEditProfileModal by remember { mutableStateOf(false) }
    var showVerificationModalDirectly by remember { mutableStateOf(false) }

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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ProfilePageSkeleton()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF8FAFC)),
                verticalArrangement = Arrangement.Top
            ) {
                if (showEditProfileModal) {
                    EditProfileModal(
                        currentFullName = fullName,
                        currentGrade = profileViewModel.grade.value,
                        currentKelas = profileViewModel.kelas.value,
                        currentEmail = email,
                        isEmailVerified = profileViewModel.isEmailVerified.value,
                        onSaveProfile = { name, grade, kelas, onResult ->
                            profileViewModel.updateProfileInfo(name, grade, kelas, onResult)
                        },
                        onUpdateEmail = { newEmail, currentPassword, keepLoggedIn, onResult ->
                            profileViewModel.updateEmail(newEmail, currentPassword, keepLoggedIn) { success, msg ->
                                onResult(success, msg)
                                if (success && !keepLoggedIn) {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onUpdatePassword = { currentPassword, newPassword, keepLoggedIn, onResult ->
                            profileViewModel.updatePassword(currentPassword, newPassword, keepLoggedIn) { success, msg ->
                                onResult(success, msg)
                                if (success && !keepLoggedIn) {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onOpenEmailVerification = {
                            showEditProfileModal = false
                            showVerificationModalDirectly = true
                        },
                        onDismiss = { showEditProfileModal = false }
                    )
                }

                if (showVerificationModalDirectly) {
                    EmailVerificationModal(
                        isEmailVerified = profileViewModel.isEmailVerified.value,
                        email = email,
                        onResendEmail = { profileViewModel.resendVerificationEmail(it) },
                        onCheckStatus = { profileViewModel.checkAndReloadEmailVerification(it) },
                        onDismiss = { showVerificationModalDirectly = false }
                    )
                }

                ProfileHeader(
                    fullName = fullName,
                    email = email,
                    coins = coins,
                    avatar = avatar,
                    exp = profileViewModel.exp.value,
                    level = profileViewModel.level.value,
                    activeTitle = profileViewModel.activeTitle.value,
                    activeEmblem = profileViewModel.activeEmblem.value,
                    activeFrame = profileViewModel.activeFrame.value,
                    claimedLevelRewards = profileViewModel.claimedLevelRewards,
                    unlockedTitles = profileViewModel.unlockedTitles,
                    unlockedEmblems = profileViewModel.unlockedEmblems,
                    unlockedFrames = profileViewModel.unlockedFrames,
                    onClaimReward = { profileViewModel.claimLevelReward(it) },
                    onSelectTitle = { profileViewModel.setActiveTitle(it) },
                    onSelectEmblem = { profileViewModel.setActiveEmblem(it) },
                    onSelectFrame = { profileViewModel.setActiveFrame(it) },
                    onAvatarChanged = { newAvatar ->
                        profileViewModel.updateAvatar(newAvatar)
                    },
                    isEmailVerified = profileViewModel.isEmailVerified.value,
                    onResendVerification = { onResult ->
                        profileViewModel.resendVerificationEmail(onResult)
                    },
                    onCheckVerificationStatus = { onResult ->
                        profileViewModel.checkAndReloadEmailVerification(onResult)
                    }
                )
                ProfileBody(
                    navController = navController,
                    studyDuration = studyDuration,
                    activeSessions = activeSessions,
                    unlockedAchievements = unlockedAchievements,
                    isLoggingOut = isLoggingOut,
                    onEditProfileClicked = { showEditProfileModal = true },
                    onLogoutClicked = { isLoggingOut = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
