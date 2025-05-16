package com.LambdaProject.MathArt

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationMenu(navController: NavController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val currentRoute = navController.currentDestination?.route

    BottomNavigation (
        backgroundColor = Color.White,
        modifier = Modifier.height(70.dp)
    ) {
        BottomNavigationItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_home_blue),
                        contentDescription = "Beranda",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Beranda",
                        fontSize = 12.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            },
            selected = currentRoute?.startsWith("dashboard") == true,
            onClick = {
                val targetRoute = "dashboard/{userName}"
                if (currentRoute != targetRoute) {
                    navController.navigate(targetRoute) {
                        popUpTo(targetRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            /* onClick = {
                if (currentRoute?.startsWith("dashboard") != true) {
                    navController.navigate("dashboard/{userName}") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            } */
        )

        BottomNavigationItem(
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Quiz",
                        fontSize = 12.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                }
            },
            selected = currentRoute == "OnlineQuizPage",
            onClick = {
                if (currentRoute != "OnlineQuizPage") {
                    navController.navigate("OnlineQuizPage") {
                        popUpTo("OnlineQuizPage") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        )

        // ==== Menu (Locked) ==== //
        LockedMenuItem(
            iconRes = R.drawable.ic_book_blue,
            label = "Belajarku"
        )

        /* LockedMenuItem(
            iconRes = R.drawable.ic_bookmark_blue,
            label = "Kuis"
        ) */


        BottomNavigationItem(
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_user_blue),
                        contentDescription = "Profilku",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Profil",
                        fontSize = 12.sp,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            },
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo("profile") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            /* onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            } */
        )
    }
}

@Composable
fun MenuIcon(iconRes: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
    }
}

@Composable
fun RowScope.LockedMenuItem(iconRes: Int, label: String) {
    BottomNavigationItem(
        icon = {
            Box(contentAlignment = Alignment.Center) {
                MenuIcon(iconRes = iconRes, label = label)

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Terkunci",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(20.dp),
                        tint = Color.Gray
                    )
                }
            }
        },
        selected = false,
        onClick = { } // Tidak melakukan navigasi
    )
}