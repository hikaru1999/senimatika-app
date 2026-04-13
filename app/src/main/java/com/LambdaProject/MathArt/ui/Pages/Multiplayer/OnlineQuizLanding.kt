package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.BottomNavigationMenu
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineQuizPage(
    navController: NavController,
    viewModel: OnlineQuizViewModel = hiltViewModel(),
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val materials by viewModel.materials.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredMaterials = remember(searchQuery, materials) {
        materials.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Kuis Online",
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily,
                        color = Color(0xFF1A237E)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavigationMenu(navController) },
        containerColor = Color(0xFFF8F9FE)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    placeholder = { 
                        Text("Cari materi kuis...", fontFamily = interFontFamily, color = Color.Gray) 
                    },
                    leadingIcon = { 
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF1976D2)) 
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color(0xFFEEEEEE),
                        focusedContainerColor = Color(0xFFF8F9FE),
                        unfocusedContainerColor = Color(0xFFF8F9FE)
                    ),
                    singleLine = true
                )
            }

            if (filteredMaterials.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Materi tidak ditemukan",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            "Coba kata kunci lain",
                            fontFamily = interFontFamily,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Tantang Dirimu!",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF1A237E),
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                        Text(
                            text = "Pilih materi dan kumpulkan skor tertinggi",
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                        )
                    }

                    items(filteredMaterials) { material ->
                        OnlineQuizCard(
                            material = material,
                            onQuizClick = {
                                val initialTab = "description"
                                viewModel.selectMaterial(material)
                                navController.navigate("OnlineQuizLobby/${userId}/${initialTab}")
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
