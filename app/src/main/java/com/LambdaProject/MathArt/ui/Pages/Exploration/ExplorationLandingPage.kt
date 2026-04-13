package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.LambdaProject.MathArt.R

data class GameMapInfo(
    val id: String = "",
    val name: String = "",
    val thumbnailUrl: String? = null,
    val isLocked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationLandingScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var availableMaps by remember { mutableStateOf<List<GameMapInfo>>(emptyList()) }
    var isTutorialCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect

        db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
            isTutorialCompleted = snapshot.getBoolean("tutorial_completed") ?: false
        }

        // Fetch Maps: Menggunakan doc.id sebagai ID Map
        db.collection("game_maps").get().addOnSuccessListener { result ->
            val maps = result.documents.map { doc ->
                GameMapInfo(
                    id = doc.id,
                    name = doc.getString("name") ?: "Unnamed Map",
                    thumbnailUrl = doc.getString("thumbnailUrl")
                )
            }
            availableMaps = maps
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pilih Wilayah",
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FE)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                "Wilayah Eksplorasi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                color = Color(0xFF1A237E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Pilih wilayah untuk melihat detail dan mempersiapkan tas jelajahmu.",
                fontSize = 14.sp,
                color = Color.Gray,
                fontFamily = interFontFamily
            )

            Spacer(modifier = Modifier.height(24.dp))

            availableMaps.forEach { map ->
                // Logika kunci: level_1 terkunci jika tutorial belum selesai
                val isLocked = map.id == "level_1" && !isTutorialCompleted

                MapThumbnailCard(
                    map = map,
                    isLocked = isLocked,
                    onClick = { 
                        if (!isLocked) {
                            // NAVIGASI KE LOBBY, BUKAN LANGSUNG KE MAP
                            navController.navigate("ExplorationLobby/${map.id}")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MapThumbnailCard(
    map: GameMapInfo,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (isLocked) Color.Transparent else Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLocked
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(if (isLocked) Color.LightGray else Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_geo),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = map.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        fontFamily = interFontFamily,
                        color = if (isLocked) Color.Gray else Color(0xFF1A237E)
                    )
                    Text(
                        text = if (isLocked) "Selesaikan Tutorial Terlebih Dahulu" else "Ketuk untuk Persiapan",
                        fontSize = 12.sp,
                        color = if (isLocked) Color.Red.copy(alpha = 0.6f) else Color.Gray,
                        fontFamily = interFontFamily
                    )
                }

                if (!isLocked) {
                    Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF3F51B5))
                }
            }
        }
    }
}
