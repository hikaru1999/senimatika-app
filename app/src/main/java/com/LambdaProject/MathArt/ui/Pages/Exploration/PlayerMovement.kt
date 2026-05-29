package com.LambdaProject.MathArt.ui.Pages.Exploration

import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MovementController(
    modifier: Modifier = Modifier,
    onMove: (Int, Int) -> Unit,
) {
    val iconSize = 48.dp
    val moveCooldown = 350L
    var isLocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    val currentTime = System.currentTimeMillis()
    /* val isLocked = currentTime - lastMoveTime < moveCooldown */

    val attemptMove: (Int, Int) -> Unit = { dx, dy ->
        if (!isLocked) {
            onMove(dx, dy)

            isLocked = true

            scope.launch {
                delay(moveCooldown)
                isLocked = false
            }
        }
    }

    /* val handleMove: (Int, Int) -> Unit = { dx, dy ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMoveTime >= moveCooldown) {
            onMove(dx, dy)
            lastMoveTime = currentTime
        }
    } */

    Box(modifier = modifier.size(160.dp)) {
        // Up Button
        IconButton(
            onClick = { attemptMove(0, -1) },
            enabled = !isLocked,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(iconSize)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Atas",
                modifier = Modifier.size(40.dp),
                tint = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f)
            )
        }

        // Down Button
        IconButton(
            onClick = { attemptMove(0, 1) },
            enabled = !isLocked,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(iconSize)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Bawah",
                modifier = Modifier.size(40.dp),
                tint = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f)
            )
        }

        // Left Button
        IconButton(
            onClick = { attemptMove(-1, 0) },
            enabled = !isLocked,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(iconSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Kiri",
                modifier = Modifier.size(40.dp),
                tint = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f)
            )
        }

        // Right Button
        IconButton(
            onClick = { attemptMove(1, 0) },
            enabled = !isLocked,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(iconSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Kanan",
                modifier = Modifier.size(40.dp),
                tint = if (isLocked) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
