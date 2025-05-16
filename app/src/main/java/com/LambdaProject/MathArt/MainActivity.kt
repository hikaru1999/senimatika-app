package com.LambdaProject.MathArt

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
/* import android.util.Log */
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.ViewModels.QuizViewModel
import com.LambdaProject.MathArt.model.StudyDurationManager
import com.LambdaProject.MathArt.ui.Pages.Material.QuizScreen
import com.LambdaProject.MathArt.ui.theme.LearnApplicationTheme
/* import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first */
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.LambdaProject.MathArt.Data.AppLifeCycleObserver
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import androidx.lifecycle.viewModelScope
import com.LambdaProject.MathArt.model.ScorestreakState
import com.LambdaProject.MathArt.ui.Pages.Multiplayer.OnlineQuizResult

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /* private var sessionStartTime: Long = 0L
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() */

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
            /* val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid */

            /* val isIntroAlreadyShown = userPreferences.isIntroShown.first() */
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPreferences.getString("USERNAME_KEY", null)

            setContent {
                val navController = rememberNavController()
                val quizViewModel = remember { QuizViewModel() }
                val viewModel: OnlineQuizViewModel = hiltViewModel()
                val quizList by viewModel.materials.collectAsState()

                LearnApplicationTheme {
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                    ) {
                        composable("splash") { SplashScreen(navController) }
                        composable("intro") { IntroScreen(navController, userPreferences) }
                        composable(route = "profile") { ProfileScreen(navController) }
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
                        composable(route = "achievement") { AchievementPage(navController) }
                        composable(route = "Senimatika_screen") { SenimatikaScreen(navController) }
                        composable("notification") { /* navBackStackEntry ->
                            val viewModel: DashboardViewModel = viewModel() */
                            NotificationScreen(navController)
                        }
                        composable("login") { LoginScreen(navController) }
                        composable("register") { RegisterScreen(navController) }
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
                            val viewModel: DashboardViewModel = viewModel()
                            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
                            DashboardScreen(navController, userName, viewModel)
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

                            QuizScreen(
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
                            var showScoreSnackbar by remember { mutableStateOf(false) }
                            var scoreState by remember { mutableStateOf<ScorestreakState?>(null) }
                            val viewModel: OnlineQuizViewModel = hiltViewModel()


                            Log.d("QuizMaster", "Material aktif saat ini: $materialId")
                            Log.d("ViewModelCheck", "ViewModel instance in MainActivity hash: ${viewModel.hashCode()}")

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
                                    Log.d("onNextClick", "Material aktif saat ini: $materialId")
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

                            Log.d("QuizResult", "Material aktif saat ini: $materialId")
                            Log.d("QuizResult", "UserId aktif saat ini: $userId")

                            OnlineQuizResult(
                                onClickLeaderboard = {
                                    navController.navigate("OnlineQuizLobby/$userId/leaderboard") {
                                        popUpTo("OnlineQuizPage") {
                                            inclusive = false // Jangan hapus landing-nya
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