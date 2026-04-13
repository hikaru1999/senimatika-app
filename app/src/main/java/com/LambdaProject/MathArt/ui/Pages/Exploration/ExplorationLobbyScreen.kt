package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationLobbyScreen(
    mapId: String,
    navController: NavController
) {
    val tabLabels = listOf("Deskripsi", "Inventory")
    val tabIcons = listOf(Icons.Default.Info, Icons.Default.Inventory)
    val pagerState = rememberPagerState(pageCount = { tabLabels.size })
    val coroutineScope = rememberCoroutineScope()
    
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var inventory by remember { mutableStateOf(Inventory()) }
    var isPrepareModalOpen by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect
        db.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val coins = snapshot.getLong("coins")?.toInt() ?: 0
                val scrolls = snapshot.get("scrolls") as? List<String> ?: emptyList()
                val powerUpsStrings = snapshot.get("powerUps") as? List<String> ?: emptyList()
                val powerUps = powerUpsStrings.mapNotNull {
                    try { PowerUpType.valueOf(it) } catch (e: Exception) { null }
                }
                inventory = Inventory(coins, scrolls, powerUps)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Persiapan Jelajah",
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
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = Color(0xFF1976D2)
                    )
                }
            ) {
                tabLabels.forEachIndexed { index, label ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(label, fontFamily = interFontFamily, fontWeight = FontWeight.Bold) },
                        icon = { Icon(tabIcons[index], null) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> GameDescriptionTab(
                        onBack = { navController.popBackStack() },
                        onStartExploration = { isPrepareModalOpen = true }
                    )
                    1 -> InventoryTabContent(inventory)
                }
            }
        }

        if (isPrepareModalOpen) {
            PrepareExplorationModal(
                permanentPowerUps = inventory.powerUps,
                onCancel = { isPrepareModalOpen = false },
                onConfirm = { bagItems ->
                    isPrepareModalOpen = false
                    val bagString = bagItems.joinToString(",") { it.name }
                    
                    // Transaksi hapus item permanen dari Firestore sebelum dibawa ke map
                    userId?.let { uid ->
                        val userRef = db.collection("users").document(uid)
                        db.runTransaction { transaction ->
                            val snapshot = transaction.get(userRef)
                            val currentPUs = (snapshot.get("powerUps") as? List<String>)?.toMutableList() ?: mutableListOf()
                            bagItems.forEach { item ->
                                currentPUs.remove(item.name)
                            }
                            transaction.update(userRef, "powerUps", currentPUs)
                        }.addOnSuccessListener {
                            navController.navigate("map/$mapId/$bagString")
                        }
                    }
                }
            )
        }
    }
}
