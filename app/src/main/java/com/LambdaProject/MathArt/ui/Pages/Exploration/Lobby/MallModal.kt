package com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.mallItems
import com.LambdaProject.MathArt.data.model.MallItem
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MallModal(
    onClose: () -> Unit,
    isDisabled: Boolean = false
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userCoins by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

//    val primaryDark = Color(0xFF1A237E)
//    val accentGold = Color(0xFFFBC02D)
//    val bgCanvas = Color(0xFFF5F7FA)

    LaunchedEffect(userId) {
        userId?.let {
            db.collection("users").document(it).get().addOnSuccessListener { doc ->
                userCoins = doc.getLong("coins")?.toInt() ?: 0
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    onClick = onClose,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Box {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(32.dp)
                        .zIndex(1f)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray.copy(alpha = 0.6f)
                    )
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Exchange Center",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color(0xFF000B3A),
                        fontFamily = interFontFamily,
                    )
                    Text(
                        text = "Tukarkan koin dengan pilihan PowerUp ",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontFamily = interFontFamily,
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        color = Color(0xFF1A237E),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Koin Terkumpul",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(5.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(0.3f)),

                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(painterResource(R.drawable.ic_coin), null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = userCoins.toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color.DarkGray,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isDisabled) {
                        Text(
                            "EXCHANGE CENTER NOT AVAILABLE",
                            color = Color.Red.copy(0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .graphicsLayer { alpha = if (isDisabled) 0.4f else 1f }
                    ) {
                        items(mallItems) { item ->
                            var isBuying by remember { mutableStateOf(false) }

                            MinimalItemCard(
                                item = item,
                                canAfford = userCoins >= item.price && !isDisabled,
                                isBuying = isBuying,
                                onBuy = {
                                    if (!isDisabled && !isBuying) {
                                        isBuying = true
                                        scope.launch {
                                            delay(2500)
                                            userId?.let { uid ->
                                                val userRef = db.collection("users").document(uid)
                                                db.runTransaction { transaction ->
                                                    val snapshot = transaction.get(userRef)
                                                    val currentCoins = snapshot.getLong("coins") ?: 0
                                                    if (currentCoins >= item.price) {
                                                        transaction.update(userRef, "coins", currentCoins - item.price)
                                                        if (item.type is PowerUpType) {
                                                            val currentPUs = snapshot.get("powerUps") as? List<String> ?: emptyList()
                                                            transaction.update(userRef, "powerUps", currentPUs + item.type.name)
                                                        }
                                                    } else {
                                                        throw Exception("Koin tidak cukup")
                                                    }
                                                }.addOnSuccessListener {
                                                    userCoins -= item.price
                                                    isBuying = false
                                                    scope.launch { snackbarHostState.showSnackbar("Berhasil membeli ${item.name}!") }
                                                }.addOnFailureListener { e ->
                                                    isBuying = false
                                                    scope.launch { snackbarHostState.showSnackbar(e.message ?: "Gagal") }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp)) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Composable
fun MinimalItemCard(
    item: MallItem,
    canAfford: Boolean,
    isBuying: Boolean,
    onBuy: () -> Unit
) {
    Surface(
        color = Color(0xFFFAFAFA),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        border = if (canAfford) null else BorderStroke(1.dp, Color.Black.copy(0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFE8EAF6),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(50.dp)
            ) {
                Image(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }

            Button(
                onClick = onBuy,
                enabled = canAfford && !isBuying,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) Color(0xFF1A237E) else Color(0xFFF5F5F5),
                    contentColor = if (canAfford) Color.White else Color.Gray,
                    disabledContainerColor = if (isBuying) Color(0xFF1A237E) else Color(0xFFF5F5F5)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(38.dp).widthIn(min = 80.dp)
            ) {
                if (isBuying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.price.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
