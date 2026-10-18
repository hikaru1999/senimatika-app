package com.LambdaProject.MathArt

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.lifecycle.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.ViewModels.QuizViewModel
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.data.model.StudyDurationManager
import com.LambdaProject.MathArt.data.model.ScorestreakState
import com.LambdaProject.MathArt.ui.Pages.Material.QuizScreen
import com.LambdaProject.MathArt.ui.theme.LearnApplicationTheme
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.copy
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import com.LambdaProject.MathArt.data.AppLifeCycleObserver
import com.LambdaProject.MathArt.ui.Pages.Profile.AchievementPage
import com.LambdaProject.MathArt.ui.Pages.Dashboard.DashboardScreen
import com.LambdaProject.MathArt.ui.Pages.IntroScreen
import com.LambdaProject.MathArt.ui.Pages.LoginScreen
import com.LambdaProject.MathArt.ui.Pages.Material.MaterialScreen
import com.LambdaProject.MathArt.ui.Pages.Dashboard.NotificationScreen
import com.LambdaProject.MathArt.ui.Pages.Dashboard.CategoryMaterialsScreen
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizLobby
import com.LambdaProject.MathArt.ui.Pages.Profile.ProfileScreen
import com.LambdaProject.MathArt.ui.Pages.Profile.LearningReportScreen
import com.LambdaProject.MathArt.ui.Pages.Register.RegisterScreen
import com.LambdaProject.MathArt.ui.Pages.Profile.SenimatikaScreen
import com.LambdaProject.MathArt.ui.Pages.Register.ProfileForm
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizPage
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizMaster
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.ScorestreakSnackbar
import com.LambdaProject.MathArt.ui.Pages.SplashScreen
import com.LambdaProject.MathArt.ui.Pages.Register.SuccessPage
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizResult
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.LambdaProject.MathArt.ViewModels.AchievementViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.data.validatorQuestion
import com.LambdaProject.MathArt.ViewModels.ValidatorViewModel
import com.LambdaProject.MathArt.data.model.ValidatorRole
import com.LambdaProject.MathArt.ui.Pages.Exploration.*
import com.LambdaProject.MathArt.ui.Pages.ForgotPasswordScreen
import com.LambdaProject.MathArt.ui.Pages.Profile.QuestionnaireMasterScreen
import com.LambdaProject.MathArt.ui.Pages.Profile.ValidationSummaryScreen
import com.LambdaProject.MathArt.ui.Pages.Profile.ValidatorRoleScreen
import com.LambdaProject.MathArt.ViewModels.MapViewModel
import com.LambdaProject.MathArt.data.NetworkObserver
import com.LambdaProject.MathArt.data.model.AppAudioManager
import android.content.Intent
import android.net.Uri
import com.LambdaProject.MathArt.ui.Pages.ResetPasswordScreen
import com.LambdaProject.MathArt.ui.Pages.EmailVerificationResultScreen
import com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby.ExplorationLobbyScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingDeepLinkUri by mutableStateOf<Uri?>(null)

    private fun forceLightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLinkUri = intent.data
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeepLinkUri = intent?.data
        AppAudioManager.initialize(this)
        forceLightMode()
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifeCycleObserver())
        val userPreferences = UserPreferences(this)
        lifecycleScope.launch {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPreferences.getString("USERNAME_KEY", null)

            setContent {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val quizViewModel = remember { QuizViewModel() }
                val viewModel: OnlineQuizViewModel = hiltViewModel()
                val quizList by viewModel.materials.collectAsState()
                val mapViewModel: MapViewModel = viewModel()

                val achViewModel: AchievementViewModel = viewModel()
                val activeBadge by achViewModel.currentAchievement.collectAsState()
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                val context = LocalContext.current
                val networkObserver = remember { NetworkObserver(context) }
                val isOnline by networkObserver.isConnected.collectAsState(initial = true)
                val isNetworkLost = !isOnline

                var countdown by remember { mutableIntStateOf(15) }
                var showNetworkSnackbar by remember { mutableStateOf(false) }
                var isReconnecting by remember { mutableStateOf(false) }

                val isSuccessState = !isNetworkLost && isReconnecting
                val currentAchievement by achViewModel.currentAchievement.collectAsState()

                val isNestedNotFound by navBackStackEntry?.savedStateHandle
                    ?.getLiveData<Boolean>("is_not_found")
                    ?.observeAsState(false) ?: remember { mutableStateOf(false)}

                val isSafeZone = currentRoute == "splash" ||
                        currentRoute == "login" ||
                        currentRoute == "register" ||
                        currentRoute == "ForgotPassword" ||
                        currentRoute?.startsWith("reset_password") == true ||
                        currentRoute?.startsWith("verify_email_result") == true ||
                        isNestedNotFound == true

                LaunchedEffect(pendingDeepLinkUri) {
                    val uri = pendingDeepLinkUri ?: return@LaunchedEffect
                    val mode = uri.getQueryParameter("mode")
                    val oobCode = uri.getQueryParameter("oobCode")

                    if (!oobCode.isNullOrBlank()) {
                        when (mode) {
                            "resetPassword" -> {
                                // Fitur redirect tautan reset password dinonaktifkan sementara (eksplorasi alur OTP)
                                /*
                                navController.navigate("reset_password/$oobCode") {
                                    launchSingleTop = true
                                }
                                */
                            }
                            "verifyEmail" -> {
                                navController.navigate("verify_email_result/$oobCode") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                    pendingDeepLinkUri = null
                }

                val isSnackbarVisible = (showNetworkSnackbar || currentAchievement != null) && !isSafeZone

                LaunchedEffect(userId) {
                    if (userId != null) {
                        achViewModel.listenForNewAchievements(userId)
                    }
                }

                LaunchedEffect(isOnline, isSafeZone) {
                    if (isSafeZone) {
                        showNetworkSnackbar = false
                        isReconnecting = false
                        return@LaunchedEffect
                    }

                    if (!isOnline) {
                        showNetworkSnackbar = true
                        isReconnecting = true
                        countdown = 30

                        while (countdown > 0 && !isOnline) {
                            delay(1000)
                            countdown--
                        }

                        if (countdown <= 0 && !isOnline) {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    } else {
                        if (isReconnecting) {
                            countdown = 0
                            delay(1500)
                            showNetworkSnackbar = false
                            isReconnecting = false
                        }
                    }
                }

                LearnApplicationTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = "splash",
                            enterTransition = { fadeIn(animationSpec = tween(180)) },
                            exitTransition = { fadeOut(animationSpec = tween(180)) },
                            popEnterTransition = { fadeIn(animationSpec = tween(180)) },
                            popExitTransition = { fadeOut(animationSpec = tween(180)) }
                        ) {
                            composable("splash") { SplashScreen(navController) }
                            composable("intro") { IntroScreen(navController, userPreferences) }
                            composable("profile") { ProfileScreen(navController) }
                            composable(
                                route = "material_screen/{userId}/{materialId}",
                                arguments = listOf(
                                    navArgument("userId") { type = NavType.StringType },
                                    navArgument("materialId") { type = NavType.StringType }
                                ),
                                enterTransition = { EnterTransition.None },
                                exitTransition = { ExitTransition.None },
                                popEnterTransition = { EnterTransition.None },
                                popExitTransition = { ExitTransition.None }
                            ) { backStackEntry ->
                                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                                val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
                                MaterialScreen(
                                    userId = userId,
                                    materialId = materialId,
                                    navController = navController
                                )
                            }
                            composable(
                                route = "kuesioner_screen/{role}",
                                arguments = listOf(navArgument("role") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val roleArg = backStackEntry.arguments?.getString("role") ?: return@composable
                                val role = ValidatorRole.valueOf(roleArg.uppercase())
                                val viewModel: ValidatorViewModel = hiltViewModel()
                                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                val allQuestions = validatorQuestion

                                QuestionnaireMasterScreen(
                                    viewModel = viewModel,
                                    role = role,
                                    allQuestions = allQuestions,
                                    userId = userId,
                                    onSuccessSubmit = { navController.popBackStack() },
                                    navController = navController
                                )
                            }
                            composable(
                                route = "validation_summary/{role}",
                                arguments = listOf(navArgument("role") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val roleArg = backStackEntry.arguments?.getString("role")
                                val role = ValidatorRole.valueOf(roleArg?.uppercase() ?: "MATERI")
                                ValidationSummaryScreen(role = role, navController = navController)
                            }

                            composable("validator_screen") {
                                ValidatorRoleScreen(
                                    navController = navController,
                                    onSelectMediaExpert = { navController.navigate("kuesioner_screen/media") },
                                    onSelectMaterialExpert = { navController.navigate("kuesioner_screen/materi") }
                                )
                            }
                            composable(route = "achievement") { AchievementPage(navController) }
                            composable(route = "Senimatika_screen") { SenimatikaScreen(navController) }
                            composable("notification") { NotificationScreen(navController) }
                            composable("login") { LoginScreen(navController) }
                            composable("register") { RegisterScreen(navController) }
                            composable("ForgotPassword") { ForgotPasswordScreen(navController) }
                            composable(
                                route = "reset_password/{oobCode}",
                                arguments = listOf(navArgument("oobCode") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val oobCode = backStackEntry.arguments?.getString("oobCode") ?: ""
                                ResetPasswordScreen(navController = navController, oobCode = oobCode)
                            }
                            composable(
                                route = "verify_email_result/{oobCode}",
                                arguments = listOf(navArgument("oobCode") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val oobCode = backStackEntry.arguments?.getString("oobCode") ?: ""
                                EmailVerificationResultScreen(navController = navController, oobCode = oobCode)
                            }
                            composable("ExplorationLandingPage") { ExplorationLandingScreen(navController) }

                            composable(
                                route = "ExplorationLobby/{mapId}",
                                arguments = listOf(navArgument("mapId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val mapId = backStackEntry.arguments?.getString("mapId") ?: ""
                                ExplorationLobbyScreen(
                                    mapId = mapId,
                                    navController = navController,
                                    mapViewModel = mapViewModel
                                )
                            }

                            composable(
                                route = "ExplorationLoading/{mapId}?bagItems={bagItems}",
                                arguments = listOf(
                                    navArgument("mapId") { type = NavType.StringType },
                                    navArgument("bagItems") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    }
                                )
                            ) { backStackEntry ->
                                val mapId = backStackEntry.arguments?.getString("mapId") ?: ""
                                val bagItems = backStackEntry.arguments?.getString("bagItems") ?: ""
                                ExplorationLoadingScreen(
                                    mapId = mapId,
                                    bagItems = bagItems,
                                    navController = navController,
                                    mapViewModel = mapViewModel
                                )
                            }

                            composable("ProfileForm/{username}/{email}/{password}",
                                arguments = listOf(
                                    navArgument("username") { type = NavType.StringType },
                                    navArgument("email") { type = NavType.StringType },
                                    navArgument("password") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val username = backStackEntry.arguments?.getString("username") ?: ""
                                val email = backStackEntry.arguments?.getString("email") ?: ""
                                val password = backStackEntry.arguments?.getString("password") ?: ""

                                ProfileForm(
                                    navController = navController,
                                    username = username,
                                    email = email,
                                    password = password
                                )
                            }
                            composable("success/{username}") { backStackEntry ->
                                val username = backStackEntry.arguments?.getString("username") ?: ""
                                SuccessPage(navController = navController, username = username)
                            }
                            composable(
                                route = "dashboard/{userName}",
                                arguments = listOf(
                                    navArgument("userName") {
                                        type = NavType.StringType
                                        nullable = false
                                    }
                                )
                            ) { backStackEntry ->
                                val DashViewModel: DashboardViewModel = viewModel()
                                val userName = backStackEntry.arguments?.getString("userName") ?: "User"
                                DashboardScreen(navController, userName, DashViewModel)
                            }

                            composable(
                                route = "category_materials/{categoryName}",
                                arguments = listOf(
                                    navArgument("categoryName") {
                                        type = NavType.StringType
                                    }
                                )
                            ) { backStackEntry ->
                                val rawCategory = backStackEntry.arguments?.getString("categoryName") ?: ""
                                val categoryName = try {
                                    java.net.URLDecoder.decode(rawCategory, "UTF-8")
                                } catch (_: Exception) {
                                    rawCategory
                                }
                                val dashViewModel: DashboardViewModel = viewModel()
                                CategoryMaterialsScreen(
                                    navController = navController,
                                    categoryName = categoryName,
                                    viewModel = dashViewModel
                                )
                            }

                            composable(
                                route = "quiz/{userId}/{materialId}",
                                arguments = listOf(
                                    navArgument("userId") { type = NavType.StringType },
                                    navArgument("materialId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                                val materialId = backStackEntry.arguments?.getString("materialId") ?: "transformasi_geometri"
                                val tabs = listOf("Pengantar", "Translasi", "Refleksi", "Rotasi", "Dilatasi", "Kuis", "Hasil Belajar")
                                val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })

                                QuizScreen(
                                    currentPage = pagerState.currentPage,
                                    myPage = 5,
                                    viewModel = quizViewModel,
                                    onQuizFinished = { navController.navigate("summary") },
                                    userId = userId,
                                    materialId = materialId
                                )
                            }

                            composable(
                                route = "OnlineQuizPage",
                            ) {
                                OnlineQuizPage(
                                    navController = navController,
                                    viewModel = viewModel,
                                )
                            }

                            composable(
                                route = "OnlineQuizLobby/{userId}/{initialTab}",
                                arguments = listOf(
                                    navArgument("userId") { type = NavType.StringType },
                                    navArgument("initialTab") {
                                        type = NavType.StringType
                                        defaultValue = "desciption"
                                        nullable = true
                                    }
                                )
                            ) { backStackEntry ->
                                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                                val initialTab = backStackEntry.arguments?.getString("initialTab") ?: "description"


                                OnlineQuizLobby(
                                    viewModel = viewModel,
                                    onUserSelected = { username ->
                                    },
                                    navController = navController,
                                    userId = userId,
                                    initialTab = initialTab
                                )
                            }

                            composable(
                                route = "StartQuiz/{materialId}/{userId}?isDuel={isDuel}&botId={botId}",
                                arguments = listOf(
                                    navArgument("materialId") { type = NavType.StringType },
                                    navArgument("userId") { type = NavType.StringType },
                                    navArgument("isDuel") {
                                        type = NavType.BoolType
                                        defaultValue = false
                                    },
                                    navArgument("botId") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    }
                                )
                            ) { backStackEntry ->
                                val materialId = backStackEntry.arguments?.getString("materialId") ?: return@composable
                                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                                val isDuel = backStackEntry.arguments?.getBoolean("isDuel") ?: false
                                val botId = backStackEntry.arguments?.getString("botId") ?: ""

                                OnlineQuizMaster(
                                    navController = navController,
                                    materialId = materialId,
                                    userId = userId,
                                    isDuel = isDuel,
                                    initialBotId = botId,
                                    onFinishQuiz = { finalBotId, isSurrendered ->
                                        val route = if (isDuel) {
                                            "OnlineQuizResult/${materialId}/${userId}?isDuel=true&botId=${finalBotId}&isSurrendered=${isSurrendered}"
                                        } else {
                                            "OnlineQuizResult/${materialId}/${userId}"
                                        }
                                        navController.navigate(route) {
                                            popUpTo("StartQuiz") {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    viewModel = viewModel,
                                    onNextClick = {
                                        viewModel.nextQuestion()
                                    },
                                    onTriggerScorestreak = {},
                                    onBackPressed = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = "OnlineQuizResult/{materialId}/{userId}?isDuel={isDuel}&botId={botId}&isSurrendered={isSurrendered}",
                                arguments = listOf(
                                    navArgument("materialId") { type = NavType.StringType },
                                    navArgument("userId") { type = NavType.StringType },
                                    navArgument("isDuel") {
                                        type = NavType.BoolType
                                        defaultValue = false
                                    },
                                    navArgument("botId") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    },
                                    navArgument("isSurrendered") {
                                        type = NavType.BoolType
                                        defaultValue = false
                                    }
                                )
                            ) { backStackEntry ->
                                val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
                                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                                val isDuel = backStackEntry.arguments?.getBoolean("isDuel") ?: false
                                val botId = backStackEntry.arguments?.getString("botId") ?: ""
                                val isSurrendered = backStackEntry.arguments?.getBoolean("isSurrendered") ?: false

                                OnlineQuizResult(
                                    onClickLeaderboard = {
                                        navController.navigate("OnlineQuizLobby/$userId/leaderboard") {
                                            popUpTo("OnlineQuizPage") {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    },
                                    onClickHome = {
                                        navController.navigate("OnlineQuizLobby/$userId/description") {
                                            popUpTo("OnlineQuizPage") {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    },
                                    onClickRematch = {
                                        navController.navigate("StartQuiz/$materialId/$userId?isDuel=true&botId=$botId") {
                                            popUpTo("OnlineQuizLobby/$userId/description") {
                                                inclusive = false
                                            }
                                        }
                                    },
                                    materialId = materialId,
                                    userId = userId,
                                    quizList = quizList,
                                    isDuel = isDuel,
                                    botId = botId,
                                    isSurrendered = isSurrendered,
                                    viewModel = viewModel
                                )
                            }

                            composable(
                                route = "map/{mapId}?bagItems={bagItems}",
                                arguments = listOf(
                                    navArgument("mapId") { type = NavType.StringType },
                                    navArgument("bagItems") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    }
                                )
                            ) { backStackEntry ->
                                val mapId = backStackEntry.arguments?.getString("mapId") ?: "level_1"
                                val bagItems = backStackEntry.arguments?.getString("bagItems") ?: ""
                                ExplorationMapScreen(
                                    mapId = mapId,
                                    initialBag = bagItems,
                                    viewModel = mapViewModel,
                                    achViewModel = achViewModel,
                                    navController = navController,
                                    onBack = { navController.popBackStack()}
                                )
                            }

                            composable("learning_report") {
                                LearningReportScreen(navController = navController)
                            }
                        }

//                        if (isNetworkLost) {
//                        }

                        AnimatedVisibility(
                            visible = showNetworkSnackbar && !isSafeZone,
                            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                            exit = fadeOut(animationSpec = tween(durationMillis = 300))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(998f)
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .pointerInput(Unit) {
                                        detectTapGestures {

                                        }
                                    }
                            )
                        }

                        UniversalSnackbar(
                            achievementItem = currentAchievement,
                            isNetworkLost = isNetworkLost,
                            isSuccess = isSuccessState,
                            remainingSeconds = countdown,
                            visible = isSnackbarVisible
                        )



//                        activeBadge?.let { badge ->
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .zIndex(9999f)
//                            ) {
//                                AchievementSnackbar(
//                                    item = badge,
//                                    visible = true
//                                )
//                            }
//                        }
                    }
                }
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        StudyDurationManager.onAppForegrounded()
                    }
                    Lifecycle.Event.ON_STOP -> {
                        StudyDurationManager.onAppBackgrounded()
                    }
                    else -> {}
                }
            }
        )
    }
}