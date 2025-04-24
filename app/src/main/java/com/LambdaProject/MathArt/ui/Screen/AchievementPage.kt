package com.LambdaProject.MathArt.ui.Screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

import com.LambdaProject.MathArt.Data.sampleAchievements

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementPage(navController: NavController) {
    val db = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val unlockedAchievementNames = remember { mutableStateListOf<String>() }

    if (userId == null) {
        Text("User belum login")
        return
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lencanaku") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    val isUnlocked = achievement.name in unlockedAchievementNames
                    AchievementCard(achievement, isUnlocked = isUnlocked)
                }
            }
        }
    }
}