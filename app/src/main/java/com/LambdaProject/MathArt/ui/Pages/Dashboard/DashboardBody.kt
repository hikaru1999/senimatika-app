package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.Data.sampleCategories
import com.LambdaProject.MathArt.Data.sampleMaterials
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.MaterialItem
import com.google.accompanist.pager.HorizontalPager
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun DashboardBody (navController: NavController, viewModel: DashboardViewModel = viewModel(), onMaterialSelected: (MaterialItem) -> Unit) {
    val materialStatusMap by viewModel.materialStatusMap.collectAsState()
    val isLoading = materialStatusMap.isEmpty()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidth - 300.dp) / 2

    Box(
        modifier = Modifier
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
                    val pagerState = rememberPagerState(pageCount = { sampleMaterials.size })
                    val itemCount = sampleMaterials.size

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = if (itemCount == 1)
                            PaddingValues(start = horizontalPadding, end = horizontalPadding)
                        else
                            PaddingValues(start = 20.dp, end = 48.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        val material = sampleMaterials[page]
                        val isActive = materialStatusMap[material.id] == true

                        Box(modifier = Modifier.padding(end = if (page == itemCount - 1) 0.dp else 20.dp)) {
                            MaterialCard(
                                material = material,
                                isActive = isActive,
                                onClickLearn = {
                                    if (!isActive) {
                                        onMaterialSelected(material)
                                    } else {
                                        navController.navigate("material_screen/${userId}/${material.id}")
                                    }
                                }
                            )
                        }

                    }
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
                    itemsIndexed(sampleCategories) { index, category ->
                        Row {
                            if (index == 0) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            CategoryCard(category)

                            if (index == sampleCategories.lastIndex) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Sedang Dipelajari")
            val activeSessions = remember(materialStatusMap) {
                sampleMaterials.filter { materialStatusMap[it.id] == true }
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                if (activeSessions.isEmpty()) {
                    EmptyKategoriBox()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp),
                    ) {
                        val pagerState = rememberPagerState(pageCount = { activeSessions.size })
                        val itemCount = activeSessions.size

                        HorizontalPager(
                            state = pagerState,
                            contentPadding = /*if (itemCount == 1)
                                PaddingValues(start = horizontalPadding, end = horizontalPadding)
                            else */
                                PaddingValues(start = 20.dp, end = 48.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) { page ->
                            val material = activeSessions[page]

                            Box(modifier = Modifier.padding(end = if (page == itemCount - 1) 0.dp else 20.dp)) {
                                userId?.let { uid ->
                                    ActiveMaterialCard(
                                        userId = uid,
                                        material = material,
                                        isActive = true,
                                        onClickLearn = {
                                            navController.navigate("material_screen/$uid/${material.id}")
                                        }
                                    )
                                }
                            }
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