package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.interFontFamily
import kotlinx.coroutines.delay

@Composable
fun StationModal(
    sessionCode: String,
    collectedDigits: List<Char>,
    cooldownEndMillis: Long = 0L,
    onTriggerCooldown: ((Long) -> Unit)? = null,
    onSolve: () -> Unit,
    onClose: () -> Unit
) {
    var inputCode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var wrongAttempts by remember { mutableIntStateOf(0) }
    var localCooldownEnd by remember { mutableLongStateOf(cooldownEndMillis) }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Sync with external cooldownEndMillis if updated
    LaunchedEffect(cooldownEndMillis) {
        if (cooldownEndMillis > localCooldownEnd) {
            localCooldownEnd = cooldownEndMillis
        }
    }

    val remaining = (localCooldownEnd - currentTime).coerceAtLeast(0L)
    val secondsLeft = (remaining / 1000).coerceAtLeast(0L)
    val isCooldownActive = remaining > 0L

    // Timer loop for active cooldown
    LaunchedEffect(localCooldownEnd) {
        while (localCooldownEnd > System.currentTimeMillis()) {
            currentTime = System.currentTimeMillis()
            delay(500)
        }
        currentTime = System.currentTimeMillis()
        if (wrongAttempts >= 3) {
            wrongAttempts = 0
        }
    }

    // Shake offset on error
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-14f) at 50
                    14f at 100
                    (-10f) at 150
                    10f at 200
                    (-6f) at 250
                    6f at 300
                    0f at 400
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .systemBarsPadding()
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF181A20),
            border = BorderStroke(
                2.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFD700),
                        Color(0xFFB8860B).copy(alpha = 0.6f)
                    )
                )
            ),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Box {
                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF262A34),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Lock icon with glowing ring
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFB8860B), Color(0xFF5D4037))
                                ),
                                shape = CircleShape
                            )
                            .border(2.5.dp, Color(0xFFFFF7C2), CircleShape)
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Area Terkunci",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = interFontFamily,
                        color = Color.White
                    )

                    Text(
                        text = "Masukkan 3 digit kode untuk membuka akses. Kalahkan boss di wilayah ini untuk mendapatkan kode.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        fontFamily = interFontFamily,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                    )

                    // Revealed Clue Boxes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        sessionCode.forEach { digit ->
                            val isFound = collectedDigits.contains(digit)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFound) Color(0xFF2E2412) else Color(0xFF1E222B),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isFound) Color(0xFFFFD700) else Color(0xFF334155)
                                ),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isFound) digit.toString() else "?",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = interFontFamily,
                                        color = if (isFound) Color(0xFFFFD700) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    // PIN Input Display Box
                    Surface(
                        color = Color(0xFF0F1115),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            2.dp,
                            if (isError) Color(0xFFEF4444) else if (isCooldownActive) Color(0xFFF59E0B) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(52.dp)
                            .offset(x = shakeOffset.value.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCooldownActive) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${secondsLeft}s",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = interFontFamily,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 0 until 3) {
                                        val char = inputCode.getOrNull(i)
                                        Box(
                                            modifier = Modifier.size(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (char != null) {
                                                Text(
                                                    text = char.toString(),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = interFontFamily,
                                                    color = if (isError) Color(0xFFEF4444) else Color(0xFF38BDF8)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(
                                                            if (i == inputCode.length) Color(0xFF64748B) else Color(0xFF334155),
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cooldown / Safety Notice
                    AnimatedVisibility(
                        visible = isCooldownActive,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            color = Color(0xFF451A03),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFB45309)),
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Coba lagi dalam $secondsLeft detik",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }
                    }

                    // Wrong attempt indicator (when not yet in cooldown)
                    if (!isCooldownActive && wrongAttempts > 0) {
                        Text(
                            text = "Percobaan salah: $wrongAttempts/3",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interFontFamily,
                            color = Color(0xFFF87171),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Keypad Grid
                    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "DEL")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(keys) { key ->
                            KeypadButton(
                                label = key,
                                enabled = !isCooldownActive,
                                onClick = {
                                    if (isCooldownActive) return@KeypadButton
                                    isError = false
                                    when (key) {
                                        "C" -> inputCode = ""
                                        "DEL" -> if (inputCode.isNotEmpty()) inputCode = inputCode.dropLast(1)
                                        else -> if (inputCode.length < 3) inputCode += key
                                    }

                                    if (inputCode.length == 3) {
                                        if (inputCode == sessionCode) {
                                            wrongAttempts = 0
                                            onSolve()
                                        } else {
                                            isError = true
                                            wrongAttempts++
                                            if (wrongAttempts >= 3) {
                                                val cooldownDuration = 60_000L // 1 Menit
                                                localCooldownEnd = System.currentTimeMillis() + cooldownDuration
                                                currentTime = System.currentTimeMillis()
                                                onTriggerCooldown?.invoke(cooldownDuration)
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
    }
}

@Composable
fun KeypadButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = when {
        !enabled -> Color(0xFF1E222B)
        label == "C" -> Color(0xFF7F1D1D)
        label == "DEL" -> Color(0xFF334155)
        else -> Color(0xFF262A34)
    }

    val contentColor = when {
        !enabled -> Color(0xFF475569)
        label == "C" -> Color(0xFFFCA5A5)
        label == "DEL" -> Color(0xFFCBD5E1)
        else -> Color.White
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(
            1.dp,
            if (enabled) Color(0xFF3B4252) else Color(0xFF262A34)
        ),
        modifier = Modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "DEL") {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Hapus",
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            } else {
                Text(
                    text = label,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = contentColor
                )
            }
        }
    }
}
