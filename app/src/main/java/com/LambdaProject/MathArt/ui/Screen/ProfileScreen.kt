package com.LambdaProject.MathArt.ui.Screen

import com.LambdaProject.MathArt.Data.sampleMaterials
import com.LambdaProject.MathArt.model.MaterialItem
import com.LambdaProject.MathArt.BottomNavigationMenu
import com.LambdaProject.MathArt.Data.sampleAchievements
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.AchievementItem
import com.LambdaProject.MathArt.model.StudyDurationManager
import com.LambdaProject.MathArt.model.*

import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun ProfileScreen(navController: NavController) {
    var studyDuration by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val unlockedAchievementNames = remember { mutableStateListOf<String>() }

    var username by remember { mutableStateOf("User") }
    var email by remember { mutableStateOf("email") }
    var activeSessions by remember { mutableStateOf<List<MaterialItem>>(emptyList()) }
    var durationReady by remember { mutableStateOf(false) }
    var sessionReady by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        user?.let {
            val userDoc = db.collection("users").document(it.uid)

            userDoc.get().addOnSuccessListener { document ->
                username = document.getString("username") ?: "User"
                email = document.getString("email") ?: "Email Tidak Diketahui"
            }

            Firebase.firestore.collection("sessions")
                .whereEqualTo("userId", it.uid)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener { documents ->
                    val activeMaterialIds = documents.mapNotNull { doc -> doc.getString("materialId") }
                    val filtered = sampleMaterials.filter { it.id in activeMaterialIds }
                    Log.d("ProfileScreen", "Active materials: $filtered")
                    activeSessions = filtered
                }

            StudyDurationManager.checkAndResetWeeklyDuration(it.uid)

            StudyDurationManager.observeStudyDuration(it.uid) { duration ->
                studyDuration = duration
                durationReady = true
            }
        }
    }

    LaunchedEffect(durationReady) {
        if (durationReady && user != null) {
            unlockExplorerAchievement(user.uid, studyDuration)
            val unlockedNames = getUnlockedAchievements(user.uid)
            unlockedAchievementNames.clear()
            unlockedAchievementNames.addAll(unlockedNames)
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
            ProfileBody(navController, studyDuration, activeSessions = activeSessions)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileHeader(username: String, email: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xffF7FAFF))
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Foto Profil",
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = username,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = email,
                    fontFamily = interFontFamily,
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ProfileBody(navController: NavController, studyDuration: Long, activeSessions: List<MaterialItem>) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: ""
    var isLoggingOut by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggingOut) {
        if (isLoggingOut) {
            delay(1200)
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "Berhasil Logout", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("dashboard/{username}") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(125.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF)
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_clock_yellow),
                        contentDescription = "Durasi Belajar",
                        modifier = Modifier
                            .size(80.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Durasi Belajar Minggu Ini",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatDuration(studyDuration),
                            fontFamily = interFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Keep it up!",
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Materi Aktif",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFontFamily
        )

        Spacer(modifier = Modifier.height(16.dp))

        activeSessions.forEach { material ->
            CompactMaterialCard(userId, material = material)
        }

        if (activeSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .border(
                        width = 0.dp,
                        brush = Brush.linearGradient(colors = listOf(Color.Gray, Color.Gray)),
                        shape = RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada materi yang sedang dipelajari",
                    fontFamily = interFontFamily,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AchievementSection(navController, userId)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Lainnya",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFontFamily
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            border = BorderStroke(1.dp, Color.Black),
            onClick = { navController.navigate("Senimatika_screen") },
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
                ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Button Info",
                    modifier = Modifier.height(20.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tentang Senimatika",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(0.dp),
            border = BorderStroke(1.dp, Color.Black),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC40300),
                disabledContainerColor = Color(0xFFC40300),
                ),
            onClick = {
                isLoggingOut = true

            },
            enabled = !isLoggingOut,
            shape = RoundedCornerShape(4.dp)
        ) {
            if (isLoggingOut) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 4.dp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(0.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logout_exit),
                        contentDescription = "Button Info",
                        modifier = Modifier.height(20.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Akhiri Sesi (Log Out)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily
                    )
                }
            }
        }
    }
}


@Composable
fun AchievementSection(navController: NavController, userId: String) {
    val db = Firebase.firestore
    val unlockedAchievementNames = remember { mutableStateListOf<String>() }

    LaunchedEffect(userId) {
        val achievementsRef = db.collection("userAchievements")
            .whereEqualTo("userId", userId)

        achievementsRef.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                Log.e("Firestore", "Error fetching achievements: $exception")
                return@addSnapshotListener
            }

            snapshots?.let {
                unlockedAchievementNames.clear()
                unlockedAchievementNames.addAll(it.documents.mapNotNull { doc ->
                    doc.getString("achievementName")
                })
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Lencanaku",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = interFontFamily
        )
        Text(
            text = "Lihat Semua",
            fontSize = 14.sp,
            fontFamily = interFontFamily,
            color = Color(0xFF4397E2),
            modifier = Modifier
                .clickable { navController.navigate("achievement") }
                .padding(4.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(sampleAchievements) { achievement ->
                val isUnlocked = achievement.name in unlockedAchievementNames
                AchievementCard(achievement, isUnlocked = isUnlocked)
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: AchievementItem, isUnlocked: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(90.dp)
    ) {
        Image(
            painter = painterResource(id = achievement.imageRes),
            contentDescription = achievement.name,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .graphicsLayer { alpha = if(isUnlocked) 1f else 0.5f },
            colorFilter = if (isUnlocked) null else ColorFilter.tint(Color.Gray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = achievement.name,
            fontFamily = interFontFamily,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            style = TextStyle(letterSpacing = 0.5.sp),
            color = if (isUnlocked) Color.Black else Color.Gray)
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CompactMaterialCard(userId: String, material: MaterialItem) {
    val db = Firebase.firestore
    var progress by remember { mutableStateOf(0f) }
    val totalPage = 6

    LaunchedEffect(userId) {
        db.collection("userProgress").document(userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val maxPage = it.getLong("maxPage")?.toInt() ?: 0
                    progress = ((maxPage + 1).toFloat() / totalPage).coerceIn(0f, 1f)
                }
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F7FF)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(
                text = material.title,
                fontFamily = interFontFamily,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = {
                        progress
                    },
                    modifier = Modifier
                        .height(6.dp)
                        .width(260.dp),
                    color = Color(0xFF6781FF),
                    trackColor = Color.LightGray,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6781FF)
                )
            }
        }
    }
}

fun formatDuration(durationInMillis: Long): String {
    val totalMinutes = durationInMillis / 1000 / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "${hours} jam ${minutes} menit"
}

suspend fun getUnlockedAchievements(userId: String): List<String> = suspendCoroutine { cont ->
    Firebase.firestore.collection("userAchievements")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { documents ->
            val unlockedNames = documents.mapNotNull { it.getString("achievementName") }
            cont.resume(unlockedNames)
        }
        .addOnFailureListener { exception ->
            Log.e("Firestore", "Error getting achievements", exception)
            cont.resume(emptyList())
        }
}