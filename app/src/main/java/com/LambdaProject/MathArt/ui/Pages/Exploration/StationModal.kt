package com.LambdaProject.MathArt.ui.Pages.Exploration

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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.LambdaProject.MathArt.interFontFamily

@Composable
fun StationModal(
    sessionCode: String,
    collectedDigits: List<Char>,
    onSolve: () -> Unit,
    onClose: () -> Unit
) {
    var inputCode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF212121),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
        ) {
            Box {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color(0xFFFFD700)
                    )
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(Color(0xFFD4AF37), Color(0xFF8B4513))),
                                shape = CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Area Terkunci",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = interFontFamily,
                        color = Color.White,
                    )

                    Text(
                        "Masukkan 3 digit kode untuk membuka akses. Kode tersebar pada boss-boss di wilayah ini.",
                        fontSize = 12.sp,
                        color = Color(0xFFBDBDBD),
                        textAlign = TextAlign.Center,
                        fontFamily = interFontFamily,
                        modifier = Modifier.padding(top = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hint display
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        sessionCode.forEach { digit ->
                            val isFound = collectedDigits.contains(digit)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                                    .border(
                                        2.dp,
                                        if (isFound) Color(0xFFFFD700) else Color(0xFF424242),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isFound) digit.toString() else "?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isFound) Color(0xFFFFD700) else Color(0xFF616161)
                                )
                            }
                        }
                    }

                    // Input Display
                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(2.dp, if (isError) Color.Red else Color(0xFF424242)),
                        modifier = Modifier.fillMaxWidth(0.6f).height(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = inputCode.padEnd(3, '•'),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 10.sp,
                                color = if (isError) Color.Red else Color(0xFF00E5FF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    // Keypad
                    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "DEL")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(keys) { key ->
                            KeypadButton(
                                label = key,
                                onClick = {
                                    isError = false
                                    when (key) {
                                        "C" -> inputCode = ""
                                        "DEL" -> if (inputCode.isNotEmpty()) inputCode = inputCode.dropLast(1)
                                        else -> if (inputCode.length < 3) inputCode += key
                                    }

                                    if (inputCode.length == 3) {
                                        if (inputCode == sessionCode) {
                                            onSolve()
                                        } else {
                                            isError = true
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
fun KeypadButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = when (label) {
            "C" -> Color(0xFFB71C1C)
            "DEL" -> Color(0xFF424242)
            else -> Color(0xFF333333)
        },
        border = BorderStroke(1.dp, Color(0xFF616161)),
        modifier = Modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "DEL") {
                Icon(Icons.AutoMirrored.Filled.Backspace, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
            } else {
                Text(
                    text = label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
