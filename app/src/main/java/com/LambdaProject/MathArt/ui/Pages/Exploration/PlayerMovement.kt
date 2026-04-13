package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MovementController(
    modifier: Modifier = Modifier,
    onMove: (Int, Int) -> Unit
) {
    val iconSize = 48.dp
    
    // State to track the last movement time
    var lastMoveTime by remember { mutableLongStateOf(0L) }
    
    // Cooldown 200ms to sync with map animation (180ms)
    val moveCooldown = 200L 

    val handleMove: (Int, Int) -> Unit = { dx, dy ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMoveTime >= moveCooldown) {
            onMove(dx, dy)
            lastMoveTime = currentTime
        }
    }

    Box(modifier = modifier.size(160.dp)) {
        // Up Button
        IconButton(
            onClick = { handleMove(0, -1) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(iconSize)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Atas",
                modifier = Modifier.size(40.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        // Down Button
        IconButton(
            onClick = { handleMove(0, 1) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(iconSize)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Bawah",
                modifier = Modifier.size(40.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        // Left Button
        IconButton(
            onClick = { handleMove(-1, 0) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(iconSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Kiri",
                modifier = Modifier.size(40.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        // Right Button
        IconButton(
            onClick = { handleMove(1, 0) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(iconSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Kanan",
                modifier = Modifier.size(40.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
