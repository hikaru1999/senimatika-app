package com.LambdaProject.MathArt.ui.Pages.Exploration.Lobby

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.PowerUpType
import com.LambdaProject.MathArt.data.mallItems
import com.LambdaProject.MathArt.data.model.MallItem
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MallModal(
    onClose: () -> Unit,
    isDisabled: Boolean = false
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userCoins by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        userId?.let {
            db.collection("users").document(it).get().addOnSuccessListener { doc ->
                userCoins = doc.getLong("coins")?.toInt() ?: 0
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.86f)
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Header Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A), // Slate 900
                                        Color(0xFF1E3A8A)  // Blue 900
                                    )
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(
                                        text = "Exchange Center",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = Color.White,
                                        fontFamily = interFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tukarkan koin belajarmu dengan Power-Up dan Item",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFBAE6FD),
                                        fontFamily = interFontFamily,
                                        lineHeight = 15.sp
                                    )
                                }

                                IconButton(
                                    onClick = onClose,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup",
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // User Coin Balance Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_coin),
                                            contentDescription = "Koin",
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Koin Kamu",
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFF1F5F9)
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFF0F172A).copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = NumberFormat.getNumberInstance(Locale.GERMAN).format(userCoins),
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = Color(0xFFFDE047),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Disabled Warning if any
                    if (isDisabled) {
                        Surface(
                            color = Color(0xFFFFF1F2),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Exchange Center sedang tidak tersedia saat ini",
                                    color = Color(0xFFE11D48),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }

                    // 3. Power-Up Items List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(mallItems) { item ->
                            var isBuying by remember { mutableStateOf(false) }

                            ModernMallItemCard(
                                item = item,
                                canAfford = userCoins >= item.price && !isDisabled,
                                isBuying = isBuying,
                                onBuy = {
                                    if (!isDisabled && !isBuying) {
                                        isBuying = true
                                        scope.launch {
                                            delay(1000)
                                            userId?.let { uid ->
                                                val userRef = db.collection("users").document(uid)
                                                db.runTransaction { transaction ->
                                                    val snapshot = transaction.get(userRef)
                                                    val currentCoins = snapshot.getLong("coins") ?: 0
                                                    if (currentCoins >= item.price) {
                                                        transaction.update(userRef, "coins", currentCoins - item.price)
                                                        if (item.type is PowerUpType) {
                                                            val currentPUs = (snapshot.get("powerUps") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
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

                // 4. Snackbar Host
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    SnackbarHost(hostState = snackbarHostState)
                }
            }
        }
    }
}

@Composable
fun ModernMallItemCard(
    item: MallItem,
    canAfford: Boolean,
    isBuying: Boolean,
    onBuy: () -> Unit
) {
    Surface(
        color = if (canAfford) Color(0xFFF8FAFC) else Color(0xFFFDFDFD),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (canAfford) Color(0xFFE2E8F0) else Color(0xFFF1F5F9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.size(52.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(item.icon),
                        contentDescription = item.name,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Item Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    fontFamily = interFontFamily,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Purchase Button
            Button(
                onClick = onBuy,
                enabled = canAfford && !isBuying,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    disabledContainerColor = Color(0xFFF1F5F9)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(38.dp)
                    .widthIn(min = 78.dp)
            ) {
                if (isBuying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = NumberFormat.getNumberInstance(Locale.GERMAN).format(item.price),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFontFamily,
                            color = if (canAfford) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
