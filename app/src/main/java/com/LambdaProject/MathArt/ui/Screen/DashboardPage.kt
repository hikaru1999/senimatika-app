package com.LambdaProject.MathArt.ui.Screen

import com.LambdaProject.MathArt.Data.*
import com.LambdaProject.MathArt.model.MaterialItem

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.LambdaProject.MathArt.BottomNavigationMenu
import com.LambdaProject.MathArt.CategoryCard
import com.LambdaProject.MathArt.MaterialCard
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.RangkumanCard
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DashboardScreen(navController: NavController, userName: String, viewModel: DashboardViewModel = viewModel()) {
    val materialStatusMap by viewModel.materialStatusMap.collectAsState()
    val isLoading = materialStatusMap.isEmpty()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val savedUserName = sharedPreferences.getString("USERNAME_KEY", userName)
    var showRangkumanCard by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<MaterialItem?>(null) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val hasNewNotification by viewModel.hasNewNotification.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.checkActiveSessions(currentUserId, sampleMaterials)
            viewModel.listenForNotifications(currentUserId)
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationMenu(navController) },
        containerColor = Color(0xFFf7f7f7)
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues))
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, start = 0.dp, end = 0.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xffF7FAFF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Halo, $savedUserName!",
                                fontFamily = interFontFamily,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Mau belajar apa hari ini?",
                                fontFamily = interFontFamily,
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }

                        Box (
                            modifier = Modifier.wrapContentSize(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.clearNotifications()
                                    navController.navigate("notification")
                                }
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_bell),
                                    contentDescription = "Notifikasi",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            if (hasNewNotification) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.Red, shape = CircleShape)
                                        .offset(x = (-15).dp, y = (-5).dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFFF7FAFF))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionTitle(title = "Rekomendasi Materi")
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                            ) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    items(sampleMaterials, key = { it.id }) { material ->
                                        val isActive = materialStatusMap[material.id] == true
                                        MaterialCard(
                                            material = material,
                                            isActive = isActive,
                                            onClickLearn = {
                                                if (!isActive) {
                                                    selectedMaterial = it
                                                    showRangkumanCard = true
                                                } else {
                                                    navController.navigate("material_screen/${userId}/${material.id}")
                                                }
                                            }
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(75.dp)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, Color(0xFFf7f7f7).copy(alpha = 0.9f))
                                            )
                                        )
                                        .blur(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionTitle(title = "Kategori")
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(100.dp)
                        ) {
                            LazyRow (
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                items(sampleCategories) { category ->
                                    CategoryCard(category)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(75.dp)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color.Transparent, Color(0xFFf7f7f7).copy(alpha = 0.9f))
                                        )
                                    )
                                    .blur(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionTitle(title = "Yang Sedang Dipelajari")
                        val activeSessions = remember(materialStatusMap) {
                            sampleMaterials.filter { materialStatusMap[it.id] == true }
                        }
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            if (activeSessions.isEmpty()) {
                                EmptySectionBox()
                            } else {
                                LazyRow {
                                    items(activeSessions, key = { it.id }) { material ->
                                        MaterialCard(
                                            material = material,
                                            isActive = true,
                                            onClickLearn = {
                                                navController.navigate("material_screen/${userId}/${material.id}")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = showRangkumanCard,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                selectedMaterial?.let { material ->
                    if (showRangkumanCard && selectedMaterial != null)
                    {
                        RangkumanCard(
                            material = selectedMaterial!!,
                            userId = currentUserId,
                            onClose = { showRangkumanCard = false },
                            onSesiDisimpan = {
                                showRangkumanCard = false
                                navController.navigate("material_screen/${userId}/${material.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySectionBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CanvasDashedBorderBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

            }
        }
        Text(
            text = "Belum ada materi yang kamu pelajari",
            fontFamily = interFontFamily,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            color = Color.DarkGray
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun CanvasDashedBorderBox(
    modifier: Modifier = Modifier,
    dashWidth: Float = 10f,
    dashGap: Float = 10f,
    cornerRadius: Float = 24f,
    borderColor: Color = Color.Gray,
    borderWidth: Float = 3f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.drawBehind {
            val stroke = Stroke(
                width = borderWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap))
            )
            drawRoundRect(
                color = borderColor,
                size = size,
                style = stroke,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        },
        content = content
    )
}