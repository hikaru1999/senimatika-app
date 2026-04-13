package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class MallItem(
    val name: String,
    val type: Any, // PowerUpType or String for Scroll
    val price: Int,
    val icon: Int,
    val description: String
)

@Composable
fun MallModal(onClose: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userCoins by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        userId?.let {
            db.collection("users").document(it).get().addOnSuccessListener { doc ->
                userCoins = doc.getLong("coins")?.toInt() ?: 0
            }
        }
    }

    val mallItems = listOf(
        MallItem("Freeze Timer", PowerUpType.FREEZE_TIMER, 150, R.drawable.ic_pu_freeze, "Hentikan waktu selama 10 detik"),
        MallItem("Truth Filter", PowerUpType.REMOVE_TWO_OPTIONS, 200, R.drawable.ic_pu_magic, "Hapus 2 opsi salah"),
        MallItem("Double Point", PowerUpType.DOUBLE_COIN, 250, R.drawable.ic_pu_shield, "Skor x2 untuk soal ini"),
        MallItem("Random Scroll", "SCROLL", 300, R.drawable.ic_scroll_open, "Dapatkan materi random")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClose, indication = null, interactionSource = remember { MutableInteractionSource() }),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingBag, null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "SENIMATIKA MALL",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                fontFamily = interFontFamily
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Saldo Card
                    Surface(
                        color = Color(0xFFFFF9C4),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Image(painterResource(R.drawable.ic_coin), null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Saldo: $userCoins Koin", fontWeight = FontWeight.Bold, color = Color(0xFFFBC02D))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(mallItems) { item ->
                            var isBuying by remember { mutableStateOf(false) }
                            MallItemCard(item, userCoins >= item.price, isBuying) {
                                isBuying = true
                                userId?.let { uid ->
                                    val userRef = db.collection("users").document(uid)
                                    db.runTransaction { transaction ->
                                        val snapshot = transaction.get(userRef)
                                        val currentCoins = snapshot.getLong("coins") ?: 0
                                        if (currentCoins >= item.price) {
                                            transaction.update(userRef, "coins", currentCoins - item.price)
                                            // Agar stackable/duplikat bisa tersimpan di array Firestore
                                            if (item.type is PowerUpType) {
                                                val currentPUs = snapshot.get("powerUps") as? List<String> ?: emptyList()
                                                transaction.update(userRef, "powerUps", currentPUs + item.type.name)
                                            } else if (item.type == "SCROLL") {
                                                val currentSc = snapshot.get("scrolls") as? List<String> ?: emptyList()
                                                transaction.update(userRef, "scrolls", currentSc + "Materi Mall")
                                            }
                                        } else {
                                            throw Exception("Koin tidak cukup")
                                        }
                                    }.addOnSuccessListener {
                                        userCoins -= item.price
                                        isBuying = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Pembelian ${item.name} berhasil!")
                                        }
                                    }.addOnFailureListener { e ->
                                        isBuying = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(e.message ?: "Pembelian gagal")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        )
    }
}

@Composable
fun MallItemCard(item: MallItem, canAfford: Boolean, isBuying: Boolean, onBuy: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFE3F2FD)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(item.icon), null, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onBuy,
                enabled = canAfford && !isBuying,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isBuying) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("${item.price} Koin", fontSize = 12.sp)
                }
            }
        }
    }
}
