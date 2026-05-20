package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.LambdaProject.MathArt.data.DataCategories
import com.LambdaProject.MathArt.data.DataMaterials
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.MaterialItem
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

@SuppressLint("ConfigurationScreenWidthHeight")
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
                    val pagerState = rememberPagerState(pageCount = { DataMaterials.size })

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        val material = DataMaterials[page]
                        val isActive = materialStatusMap[material.id] == true

                        MaterialCard(
                            modifier = Modifier.fillMaxWidth(),
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
                    itemsIndexed(DataCategories) { index, category ->
                        Row {
                            if (index == 0) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            CategoryCard(category)

                            if (index == DataCategories.lastIndex) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Sedang Dipelajari")
            val activeSessions = remember(materialStatusMap) {
                DataMaterials.filter { materialStatusMap[it.id] == true }
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
                            .height(190.dp),
                    ) {
                        val pagerState = rememberPagerState(pageCount = { activeSessions.size })
                        val itemCount = activeSessions.size

                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            pageSpacing = 16.dp,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) { page ->
                            val material = activeSessions[page]
                            userId?.let { uid ->
                                ActiveMaterialCard(
                                    modifier = Modifier.fillMaxWidth(),
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
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(title = "Berburu Harta Karun")
            ExplorationCard(
                onClick = {
                    navController.navigate("ExplorationLandingPage")
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
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
