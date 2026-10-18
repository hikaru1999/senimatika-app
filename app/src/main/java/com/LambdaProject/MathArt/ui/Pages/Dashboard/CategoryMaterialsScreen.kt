package com.LambdaProject.MathArt.ui.Pages.Dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.DashboardViewModel
import com.LambdaProject.MathArt.data.model.CategoryItem
import com.LambdaProject.MathArt.data.model.MaterialItem
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryMaterialsScreen(
    navController: NavController,
    categoryName: String,
    viewModel: DashboardViewModel
) {
    val context = LocalContext.current
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    val materials by viewModel.materialsList.collectAsState()
    val isMaterialsLoading by viewModel.isMaterialsLoading.collectAsState()
    val materialStatusMap by viewModel.materialStatusMap.collectAsState()
    val categories by viewModel.categoriesList.collectAsState()

    var showRangkumanCard by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<MaterialItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var firestoreCategoryDescription by remember { mutableStateOf("") }

    // Find the category info
    val categoryItem = remember(categories, categoryName) {
        categories.find {
            it.name.trim().equals(categoryName.trim(), ignoreCase = true) ||
            it.id.trim().equals(categoryName.trim(), ignoreCase = true)
        } ?: CategoryItem(
            name = categoryName,
            imageRes = when (categoryName.lowercase().trim()) {
                "geometri" -> R.drawable.ico_geometry
                "kalkulus" -> R.drawable.ico_calculus
                "trigonometri" -> R.drawable.ico_trigo
                "statistika" -> R.drawable.ico_statistic
                else -> R.drawable.ic_material
            }
        )
    }

    LaunchedEffect(categoryName) {
        viewModel.listenToCategories()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val catId = categoryName.trim().lowercase()
        db.collection("categories").document(catId).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                val desc = (doc.getString("description") ?: doc.getString("deskripsi") ?: "").trim()
                if (desc.isNotEmpty()) {
                    firestoreCategoryDescription = desc
                }
            } else {
                db.collection("categories")
                    .whereEqualTo("name", categoryName.trim())
                    .addSnapshotListener { snap, _ ->
                        val found = snap?.documents?.firstOrNull()
                        val desc = (found?.getString("description") ?: found?.getString("deskripsi") ?: "").trim()
                        if (desc.isNotEmpty()) {
                            firestoreCategoryDescription = desc
                        }
                    }
            }
        }
    }

    // Filter materials for this category
    val categoryMaterials = remember(materials, categoryName) {
        val serverFiltered = materials.filter {
            it.category.trim().equals(categoryName.trim(), ignoreCase = true)
        }
        if (serverFiltered.isNotEmpty() || isMaterialsLoading) {
            serverFiltered
        } else {
            com.LambdaProject.MathArt.data.DataMaterials.filter {
                it.category.trim().equals(categoryName.trim(), ignoreCase = true)
            }
        }
    }

    // Filter materials based on search query within this specific category
    val filteredMaterials = remember(categoryMaterials, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            categoryMaterials
        } else {
            categoryMaterials.filter { material ->
                material.title.lowercase().contains(query) ||
                material.id.lowercase().replace("_", " ").contains(query) ||
                material.classLevel.lowercase().contains(query) ||
                "kelas ${material.classLevel}".lowercase().contains(query) ||
                com.LambdaProject.MathArt.data.repository.MaterialRepository.getCachedMaterial(material.id)
                    ?.description.orEmpty().lowercase().contains(query)
            }
        }
    }

    val totalExpReward = remember(categoryMaterials) {
        categoryMaterials.sumOf { it.exp }
    }

    val fallbackIconRes = remember(categoryItem.name, categoryItem.imageRes) {
        if (categoryItem.imageRes != 0) {
            categoryItem.imageRes
        } else {
            when (categoryItem.name.lowercase().trim()) {
                "geometri" -> R.drawable.ico_geometry
                "kalkulus" -> R.drawable.ico_calculus
                "trigonometri" -> R.drawable.ico_trigo
                "statistika" -> R.drawable.ico_statistic
                else -> R.drawable.ic_material
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.listenToActiveMaterials(currentUserId)
            viewModel.listenToCategories()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val cleanIconUrl = categoryItem.iconUrl?.trim()
                                if (!cleanIconUrl.isNullOrBlank()) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(cleanIconUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = categoryItem.name,
                                        modifier = Modifier.size(20.dp),
                                        contentScale = ContentScale.Fit,
                                        error = {
                                            Image(
                                                painter = painterResource(id = fallbackIconRes),
                                                contentDescription = categoryItem.name,
                                                modifier = Modifier.size(20.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = fallbackIconRes),
                                        contentDescription = categoryItem.name,
                                        modifier = Modifier.size(20.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                        Column {
                            Text(
                                text = "Kategori: ${categoryItem.name}",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF0F172A)
                            )
//                            Text(
//                                text = "Daftar Materi Pembelajaran",
//                                fontFamily = interFontFamily,
//                                fontWeight = FontWeight.Normal,
//                                fontSize = 11.5.sp,
//                                color = Color(0xFF64748B)
//                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // 1. Hero Header Banner
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(22.dp),
                                spotColor = Color(0xFF1E3A8A).copy(alpha = 0.2f)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF1E3A8A),
                                            Color(0xFF2563EB),
                                            Color(0xFF3B82F6)
                                        )
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
//                                        Surface(
//                                            shape = RoundedCornerShape(8.dp),
//                                            color = Color.White.copy(alpha = 0.18f),
//                                            border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.35f))
//                                        ) {
//                                            Row(
//                                                verticalAlignment = Alignment.CenterVertically,
//                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
//                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
//                                            ) {
//                                                Icon(
//                                                    imageVector = Icons.Default.AutoAwesome,
//                                                    contentDescription = null,
//                                                    tint = Color(0xFFFDE047),
//                                                    modifier = Modifier.size(12.dp)
//                                                )
//                                                Text(
//                                                    text = "Topik Eksplorasi",
//                                                    fontFamily = interFontFamily,
//                                                    fontSize = 11.sp,
//                                                    fontWeight = FontWeight.SemiBold,
//                                                    color = Color.White
//                                                )
//                                            }
//                                        }
//
//                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = categoryItem.name,
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            color = Color.White
                                        )

                                        val bannerDescription = categoryItem.description.ifBlank { firestoreCategoryDescription }
                                        if (bannerDescription.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = bannerDescription,
                                                fontFamily = interFontFamily,
                                                fontSize = 12.sp,
                                                color = Color(0xFFDBEAFE),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Category Icon Avatar
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White,
                                        modifier = Modifier.size(54.dp),
                                        shadowElevation = 4.dp
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val cleanIconUrl = categoryItem.iconUrl?.trim()
                                            if (!cleanIconUrl.isNullOrBlank()) {
                                                SubcomposeAsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(cleanIconUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = categoryItem.name,
                                                    modifier = Modifier.size(34.dp),
                                                    contentScale = ContentScale.Fit,
                                                    error = {
                                                        Image(
                                                            painter = painterResource(id = fallbackIconRes),
                                                            contentDescription = categoryItem.name,
                                                            modifier = Modifier.size(34.dp),
                                                            contentScale = ContentScale.Fit
                                                        )
                                                    }
                                                )
                                            } else {
                                                Image(
                                                    painter = painterResource(id = fallbackIconRes),
                                                    contentDescription = categoryItem.name,
                                                    modifier = Modifier.size(34.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Stats pills
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.Black.copy(alpha = 0.25f),
                                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                contentDescription = null,
                                                tint = Color(0xFF93C5FD),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "${categoryMaterials.size} Materi",
                                                fontFamily = interFontFamily,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.Black.copy(alpha = 0.25f),
                                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "+$totalExpReward EXP Potensial",
                                                fontFamily = interFontFamily,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFEF3C7)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Search Field (Mempertimbangkan kategori yang sedang muncul)
                if (categoryMaterials.isNotEmpty() || searchQuery.isNotEmpty()) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = Color(0xFF1E3A8A).copy(alpha = 0.08f)
                                ),
                            placeholder = {
                                Text(
                                    text = "Cari materi di ${categoryItem.name}...",
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Cari",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus pencarian",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                cursorColor = Color(0xFF2563EB)
                            )
                        )
                    }
                }

                // 3. Section Subtitle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Hasil Pencarian" else "Materi Tersedia",
                            fontFamily = interFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "${filteredMaterials.size} ditemukan"
                            } else {
                                "${categoryMaterials.size} materi"
                            },
                            fontFamily = interFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // 4. Materials List / Loading / Empty / No Search Result
                if (isMaterialsLoading) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(2) {
                                MaterialCardSkeleton(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                } else if (categoryMaterials.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Belum Ada Materi Diterbitkan",
                                    fontFamily = interFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Materi untuk kategori ${categoryItem.name} sedang dalam penyusunan oleh tutor. Pantau berkala untuk pembaruan!",
                                    fontFamily = interFontFamily,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { navController.popBackStack() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Text(
                                        text = "Kembali ke Dashboard",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (filteredMaterials.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Materi Tidak Ditemukan",
                                    fontFamily = interFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tidak ada materi di kategori ${categoryItem.name} yang cocok dengan \"$searchQuery\". Coba gunakan kata kunci lain.",
                                    fontFamily = interFontFamily,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                OutlinedButton(
                                    onClick = { searchQuery = "" },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2563EB)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF2563EB)
                                    )
                                ) {
                                    Text(
                                        text = "Reset Pencarian",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredMaterials, key = { it.id }) { material ->
                        val isActive = materialStatusMap[material.id] == true
                        MaterialCard(
                            modifier = Modifier.fillMaxWidth(),
                            material = material,
                            isActive = isActive,
                            aspectRatio = 16f / 9f,
                            onClickLearn = {
                                if (isActive) {
                                    navController.navigate("material_screen/$currentUserId/${material.id}")
                                } else {
                                    selectedMaterial = material
                                    showRangkumanCard = true
                                }
                            }
                        )
                    }
                }
            }

            // Bottom sheet modal for starting unlearned material
            AnimatedVisibility(
                visible = showRangkumanCard,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                selectedMaterial?.let { material ->
                    RangkumanCard(
                        material = material,
                        userId = currentUserId,
                        onClose = { showRangkumanCard = false },
                        onSesiDisimpan = {
                            showRangkumanCard = false
                            navController.navigate("material_screen/$currentUserId/${material.id}")
                        }
                    )
                }
            }
        }
    }
}
