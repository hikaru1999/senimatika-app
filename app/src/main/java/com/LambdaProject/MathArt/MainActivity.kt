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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import com.LambdaProject.MathArt.data.AppLifeCycleObserver
import com.LambdaProject.MathArt.ui.Pages.Profile.AchievementPage
import com.LambdaProject.MathArt.ui.Pages.Dashboard.DashboardScreen
import com.LambdaProject.MathArt.ui.Pages.IntroScreen
import com.LambdaProject.MathArt.ui.Pages.LoginScreen
import com.LambdaProject.MathArt.ui.Pages.Material.MaterialScreen
import com.LambdaProject.MathArt.ui.Pages.Dashboard.NotificationScreen
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizLobby
import com.LambdaProject.MathArt.ui.Pages.Profile.ProfileScreen
import com.LambdaProject.MathArt.ui.Pages.Register.RegisterScreen
import com.LambdaProject.MathArt.ui.Pages.Profile.SenimatikaScreen
import com.LambdaProject.MathArt.ui.Pages.Register.ProfileForm
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizPage
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizMaster
import com.LambdaProject.MathArt.ui.Pages.SplashScreen
import com.LambdaProject.MathArt.ui.Pages.Register.SuccessPage
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizResult
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
import com.google.firebase.auth.FirebaseAuth

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private fun forceLightMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceLightMode()
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifeCycleObserver())
        val userPreferences = UserPreferences(this)
        lifecycleScope.launch {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPreferences.getString("USERNAME_KEY", null)

            setContent {
                val navController = rememberNavController()
                val quizViewModel = remember { QuizViewModel() }
                val viewModel: OnlineQuizViewModel = hiltViewModel()
                val quizList by viewModel.materials.collectAsState()
                val mapViewModel: MapViewModel = viewModel()

                LearnApplicationTheme {
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                    ) {
                        composable("splash") { SplashScreen(navController) }
                        composable("intro") { IntroScreen(navController, userPreferences) }
                        composable("profile") { ProfileScreen(navController) }
                        composable(
                            route = "material_screen/{userId}/{materialId}",
                            arguments = listOf(
                                navArgument("userId") { type = NavType.StringType },
                                navArgument("materialId") { type = NavType.StringType }
                            )
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
                            route = "StartQuiz/{materialId}/{userId}",
                            arguments = listOf(
                                navArgument("materialId") { type = NavType.StringType },
                                navArgument("userId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val materialId = backStackEntry.arguments?.getString("materialId") ?: return@composable
                            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                            val questions by viewModel.questions.collectAsState()
                            val currentIndex by viewModel.currentQuestionIndex.collectAsState()
                            val viewModel: OnlineQuizViewModel = hiltViewModel()
                            var showScoreSnackbar by remember { mutableStateOf(false) }
                            var scoreState by remember { mutableStateOf<ScorestreakState?>(null) }

                            OnlineQuizMaster(
                                navController = navController,
                                materialId = materialId,
                                userId = userId,
                                onFinishQuiz = {
                                    navController.navigate("OnlineQuizResult/${materialId}/${userId}") {
                                        popUpTo("OnlineQuizMaster") {
                                            inclusive = true
                                        }
                                    }
                                },
                                viewModel = viewModel,
                                onNextClick = {
                                    if (currentIndex < questions.lastIndex) {
                                        viewModel.nextQuestion()
                                    }
                                },
                                onTriggerScorestreak = { streakState ->
                                    scoreState = streakState
                                    showScoreSnackbar = true
                                },
                                onBackPressed = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "OnlineQuizResult/{materialId}/{userId}",
                            arguments = listOf(
                                navArgument("materialId") { type = NavType.StringType },
                                navArgument("userId") { type = NavType.StringType}
                            )
                        ) { backStackEntry ->
                            val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""

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
                                materialId = materialId,
                                userId = userId,
                                quizList = quizList
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
                                onBack = { navController.popBackStack()}
                            )
                        }
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