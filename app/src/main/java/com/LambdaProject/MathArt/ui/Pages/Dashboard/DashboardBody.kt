package com.LambdaProject.MathArt.ui.Pages.Dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.DataCategories
import com.LambdaProject.MathArt.data.DataMaterials
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .background(Color(0xFF2563EB), RoundedCornerShape(2.dp))
            )
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFontFamily,
                color = Color(0xFF0F172A)
            )
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontFamily = interFontFamily,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun DashboardBody(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel(),
    onMaterialSelected: (MaterialItem) -> Unit
) {
    val materialStatusMap by viewModel.materialStatusMap.collectAsState()
    val materials by viewModel.materialsList.collectAsState()
    val isMaterialsLoading by viewModel.isMaterialsLoading.collectAsState()
    val userLevel by viewModel.level.collectAsState()
    val isLoading = materialStatusMap.isEmpty() && isMaterialsLoading
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    // Filter materi rekomendasi / carousel berdasarkan isHighlight == true
    val highlightedMaterials = remember(materials) {
        materials.filter { it.isHighlight }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Rekomendasi Materi (Hanya materi dengan isHighlight == true)
            SectionTitle(title = "Rekomendasi Materi")
            if (isMaterialsLoading) {
                MaterialCardSkeleton(
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (highlightedMaterials.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(130.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Belum ada materi rekomendasi",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val pagerState = rememberPagerState(pageCount = { highlightedMaterials.size })

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        val material = highlightedMaterials.getOrNull(page) ?: return@HorizontalPager
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

                    if (highlightedMaterials.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(highlightedMaterials.size) { iteration ->
                                val isSelected = pagerState.currentPage == iteration
                                val color = if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                                val width = if (isSelected) 18.dp else 6.dp
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .height(5.dp)
                                        .width(width)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Kategori (Carousel: 4 kategori per page)
            SectionTitle(title = "Kategori")
            val categories by viewModel.categoriesList.collectAsState()
            val categoryChunks = remember(categories) {
                if (categories.isEmpty()) listOf(emptyList()) else categories.chunked(4)
            }
            val categoryPagerState = rememberPagerState(pageCount = { categoryChunks.size.coerceAtLeast(1) })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                HorizontalPager(
                    state = categoryPagerState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) { pageIndex ->
                    val chunk = categoryChunks.getOrNull(pageIndex) ?: emptyList()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        chunk.forEach { category ->
                            CategoryCard(
                                category = category,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val encodedCategory = java.net.URLEncoder.encode(category.name, "UTF-8")
                                    navController.navigate("category_materials/$encodedCategory")
                                }
                            )
                        }
                        // Isi sisa slot jika kurang dari 4 kategori agar lebar tetap proporsional
                        if (chunk.size < 4) {
                            repeat(4 - chunk.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Dot Indicator jika total kategori > 4 (1 dot mewakili 1 halaman / 4 kategori)
            if (categoryChunks.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(categoryChunks.size) { iteration ->
                        val isSelected = categoryPagerState.currentPage == iteration
                        val color = if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                        val width = if (isSelected) 18.dp else 6.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(5.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Sedang Dipelajari
            SectionTitle(title = "Sedang Dipelajari")
            val activeSessions = remember(materialStatusMap, materials) {
                val combined = materials.toMutableList()
                DataMaterials.forEach { dm ->
                    if (combined.none { it.id == dm.id }) {
                        combined.add(dm)
                    }
                }
                combined.filter { materialStatusMap[it.id] == true }
            }
            val maxPage by viewModel.userProgressMaxPage.collectAsState()

            if (isLoading) {
                ActiveMaterialCardSkeleton(
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                if (activeSessions.isEmpty()) {
                    EmptyKategoriBox()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(265.dp)
                    ) {
                        val pagerState = rememberPagerState(pageCount = { activeSessions.size })

                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 16.dp),
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
                                    maxPage = maxPage,
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

            Spacer(modifier = Modifier.height(18.dp))

            val context = LocalContext.current
            // 4. Berburu Harta Karun / Eksplorasi
            SectionTitle(title = "Mini Games")
            ExplorationCard(
                isLocked = userLevel < 2,
                requiredLevel = 2,
                onClick = {
                    if (userLevel >= 2) {
                        navController.navigate("ExplorationLandingPage")
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "Mode Eksplorasi terkunci! Capai Level 2 (Apprentice) untuk membuka.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun EmptyKategoriBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        DashedBorderBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp),
            borderColor = Color(0xFFBFDBFE),
            borderWidth = 1.5f,
            cornerRadius = 16f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_material),
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Belum ada materi yang aktif dipelajari",
                    fontFamily = interFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }
        }
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

@Composable
fun Modifier.dashboardShimmer(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF1F5F9)
): Modifier {
    val transition = rememberInfiniteTransition(label = "dashboardShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(baseColor, highlightColor, baseColor),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 300f, translateAnim + 300f)
            )
        )
}

@Composable
fun MaterialCardSkeleton(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 16f / 9f
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .dashboardShimmer(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(18.dp)
                    .dashboardShimmer(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(26.dp)
                        .dashboardShimmer(RoundedCornerShape(8.dp))
                )

                Box(
                    modifier = Modifier
                        .width(95.dp)
                        .height(34.dp)
                        .dashboardShimmer(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}

@Composable
fun ActiveMaterialCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0xFF6D28D9).copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF4C1D95).copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1035),
                            Color(0xFF28134E),
                            Color(0xFF180D2E)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(18.dp)
                            .dashboardShimmer(
                                shape = RoundedCornerShape(4.dp),
                                baseColor = Color(0xFF3B1D6E),
                                highlightColor = Color(0xFF5B2E9E)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(22.dp)
                            .dashboardShimmer(
                                shape = RoundedCornerShape(8.dp),
                                baseColor = Color(0xFF3B1D6E),
                                highlightColor = Color(0xFF5B2E9E)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .dashboardShimmer(
                            shape = CircleShape,
                            baseColor = Color(0xFF3B1D6E),
                            highlightColor = Color(0xFF5B2E9E)
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .dashboardShimmer(
                            shape = RoundedCornerShape(12.dp),
                            baseColor = Color(0xFF3B1D6E),
                            highlightColor = Color(0xFF5B2E9E)
                        )
                )
            }
        }
    }
}
