package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.Data.sampleCategories
import com.LambdaProject.MathArt.Data.sampleMaterials
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.MaterialItem
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SectionTitle(title: String) {
    Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun DashboardBody (navController: NavController, viewModel: DashboardViewModel = viewModel(), onMaterialSelected: (MaterialItem) -> Unit) {
    val materialStatusMap by viewModel.materialStatusMap.collectAsState()
    val isLoading = materialStatusMap.isEmpty()
    /* var selectedMaterial by remember { mutableStateOf<MaterialItem?>(null) }
    var showRangkumanCard by remember { mutableStateOf(false) } */
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(Color(0xFFF7FAFF))
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Rekomendasi Materi")
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally))
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
                                        onMaterialSelected(it)
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
                    EmptyKategoriBox()
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

@Composable
fun EmptyKategoriBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        DashedBorderBox(
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
fun DashedBorderBox(
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