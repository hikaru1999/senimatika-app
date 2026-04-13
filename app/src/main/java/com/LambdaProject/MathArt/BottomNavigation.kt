package com.LambdaProject.MathArt

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationMenu(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(80.dp),
            windowInsets = WindowInsets.navigationBars
        ) {
            // Beranda
            CustomNavItem(
                label = "Beranda",
                iconRes = R.drawable.ic_home_blue,
                isSelected = currentRoute?.startsWith("dashboard") == true,
                onClick = {
                    val targetRoute = "dashboard/{userName}"
                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )

            // Quiz
            CustomNavItem(
                label = "Quiz",
                iconRes = R.drawable.ic_play,
                isSelected = currentRoute == "OnlineQuizPage",
                onClick = {
                    if (currentRoute != "OnlineQuizPage") {
                        navController.navigate("OnlineQuizPage") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )

            // Belajarku (Locked)
            CustomNavItem(
                label = "Belajarku",
                iconRes = R.drawable.ic_book_blue,
                isSelected = false,
                isLocked = true,
                onClick = { /* Locked */ }
            )

            // Profil
            CustomNavItem(
                label = "Profil",
                iconRes = R.drawable.ic_user_blue,
                isSelected = currentRoute == "profile",
                onClick = {
                    if (currentRoute != "profile") {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun RowScope.CustomNavItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1A237E) else Color.Gray.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "color"
    )

    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 0.dp,
        animationSpec = tween(300),
        label = "width"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                enabled = !isLocked,
                onClick = onClick,
                interactionSource = null,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
                
                if (isLocked) {
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(14.dp)
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape),
                        shadowElevation = 2.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = label,
                fontSize = 11.sp,
                fontFamily = interFontFamily,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = contentColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Minimalist dot/line indicator
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF1A237E) else Color.Transparent)
            )
        }
    }
}
