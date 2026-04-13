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
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) { }, // Prevent clicking through to the map
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Box {
                // Close Button in Top Right
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.Gray
                    )
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF3F51B5))),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Area Terkunci",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily,
                        color = Color(0xFF1A237E)
                    )

                    Text(
                        "Masukkan 3 digit kode untuk membuka akses. Kode tersebar pada boss-boss di wilayah ini.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontFamily = interFontFamily,
                        modifier = Modifier.padding(top = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clue Display (Hints)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        sessionCode.forEach { digit ->
                            val isFound = collectedDigits.contains(digit)
                            Surface(
                                color = if (isFound) Color(0xFFE8EAF6) else Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isFound) Color(0xFF1A237E) else Color(0xFFEEEEEE)),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isFound) digit.toString() else "?",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFound) Color(0xFF1A237E) else Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    // Input Display
                    Surface(
                        color = Color(0xFFF8F9FE),
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(2.dp, if (isError) Color.Red else Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth(0.5f).height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = inputCode.padEnd(3, '•'),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 8.sp,
                                color = if (isError) Color.Red else Color(0xFF1A237E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Keypad
                    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "DEL")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            "C" -> Color(0xFFFFEBEE)
            "DEL" -> Color(0xFFF5F5F5)
            else -> Color.White
        },
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "DEL") {
                Icon(Icons.AutoMirrored.Filled.Backspace, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
            } else {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (label) {
                        "C" -> Color.Red
                        else -> Color(0xFF1A237E)
                    }
                )
            }
        }
    }
}
