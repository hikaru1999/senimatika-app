package com.LambdaProject.MathArt.ui.Pages.Multiplayer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.BottomNavigationMenu
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.OnlineQuizViewModel
import com.LambdaProject.MathArt.data.model.OnlineQuizDesc
import com.LambdaProject.MathArt.data.model.QuizQuestionRef
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

// --- Data Model Kuis Online (sesuai Firestore) ---
data class OnlineQuizItem(
    val id: String = "",
    val title: String = "",
    val materi: String = "",
    val jenjang: String = "", // atau kelas
    val description: String = "",
    val rewardCoin: Long = 0,
    val exp: Long = 0,
    val isRandomized: Boolean = false,
    val isFeatured: Boolean = false, // Kuis Mingguan
    val isLearningMode: Boolean = false, // Mode Belajar
    val active: Boolean = true,
    val questions: List<Any> = emptyList(),
    val isCompleted: Boolean = false, // Status pengerjaan siswa
    val highestScore: Int = 0,
    val totalQuestions: Int = 0,
    val wrongCount: Int = 0,
    val correctCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineQuizLanding(
    quizzes: List<OnlineQuizItem>,
    userGrade: String = "Semua", // Kelas default siswa
    isLoading: Boolean = false,
    onQuizClick: (OnlineQuizItem) -> Unit,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // Dua level filter tingkat: Jenjang utama (Semua, SMP, SMA) -> Sub-kelas
    var selectedJenjang by remember {
        mutableStateOf(
            when {
                userGrade.contains("SMP", ignoreCase = true) -> "SMP"
                userGrade.contains("SMA", ignoreCase = true) -> "SMA"
                else -> "Semua"
            }
        )
    }

    var selectedKelas by remember {
        mutableStateOf(
            if (userGrade.isNotBlank() && userGrade != "Semua") userGrade else "Semua"
        )
    }

    // 1. Dapatkan Kuis Mingguan (isFeatured == true)
    val weeklyQuizzes = remember(quizzes) {
        val featured = quizzes.filter { it.isFeatured && it.active }
        if (featured.isNotEmpty()) {
            featured
        } else {
            quizzes.filter { it.active }.take(1)
        }
    }

    // 2. Filter daftar kuis berdasarkan search dan hierarki jenjang & kelas
    val filteredQuizzes = remember(quizzes, searchQuery, selectedJenjang, selectedKelas) {
        quizzes.filter { quiz ->
            // Filter Pencarian Teks
            val matchesSearch = searchQuery.isBlank() ||
                    quiz.title.contains(searchQuery, ignoreCase = true) ||
                    quiz.materi.contains(searchQuery, ignoreCase = true)

            // Filter Jenjang & Kelas Berjenjang
            val matchesGrade = when (selectedJenjang) {
                "Semua" -> true
                "SMP" -> {
                    val isSmpQuiz = quiz.jenjang.contains("SMP", ignoreCase = true) ||
                            quiz.jenjang.contains("Umum", ignoreCase = true) ||
                            quiz.jenjang.isBlank()
                    if (!isSmpQuiz) {
                        false
                    } else {
                        when (selectedKelas) {
                            "Semua SMP", "Semua" -> true
                            "VII SMP" -> quiz.jenjang.contains("VII", ignoreCase = true) || quiz.jenjang.contains("7") || quiz.jenjang.contains("Umum", ignoreCase = true)
                            "VIII SMP" -> quiz.jenjang.contains("VIII", ignoreCase = true) || quiz.jenjang.contains("8") || quiz.jenjang.contains("Umum", ignoreCase = true)
                            "IX SMP" -> quiz.jenjang.contains("IX", ignoreCase = true) || quiz.jenjang.contains("9") || quiz.jenjang.contains("Umum", ignoreCase = true)
                            else -> quiz.jenjang.equals(selectedKelas, ignoreCase = true) || quiz.jenjang.contains("Umum", ignoreCase = true)
                        }
                    }
                }
                "SMA" -> {
                    val isSmaQuiz = quiz.jenjang.contains("SMA", ignoreCase = true) ||
                            quiz.jenjang.contains("Umum", ignoreCase = true) ||
                            quiz.jenjang.isBlank()
                    if (!isSmaQuiz) {
                        false
                    } else {
                        when (selectedKelas) {
                            "Semua SMA", "Semua" -> true
                            "X SMA" -> (quiz.jenjang.contains("X SMA", ignoreCase = true) || (quiz.jenjang.contains("X", ignoreCase = true) && !quiz.jenjang.contains("XI") && !quiz.jenjang.contains("XII") && !quiz.jenjang.contains("IX")) || quiz.jenjang.contains("10")) || quiz.jenjang.contains("Umum", ignoreCase = true)
                            "XI SMA" -> (quiz.jenjang.contains("XI SMA", ignoreCase = true) || (quiz.jenjang.contains("XI", ignoreCase = true) && !quiz.jenjang.contains("XII")) || quiz.jenjang.contains("11")) || quiz.jenjang.contains("Umum", ignoreCase = true)
                            "XII SMA" -> quiz.jenjang.contains("XII", ignoreCase = true) || quiz.jenjang.contains("12") || quiz.jenjang.contains("Umum", ignoreCase = true)
                            else -> quiz.jenjang.equals(selectedKelas, ignoreCase = true) || quiz.jenjang.contains("Umum", ignoreCase = true)
                        }
                    }
                }
                else -> true
            }

            matchesSearch && matchesGrade
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Math ",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Arena",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0284C7)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = Color(0xFF0284C7)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = bottomBar,
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        if (isLoading && quizzes.isEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item(key = "skeleton_banner") {
                    OnlineQuizBannerSkeleton()
                }
                item(key = "skeleton_filter") {
                    OnlineQuizFilterSkeleton()
                }
                item(key = "skeleton_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                                .quizLandingShimmer(RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(14.dp)
                                .quizLandingShimmer(RoundedCornerShape(4.dp))
                        )
                    }
                }
                items(3) {
                    OnlineQuizCardSkeleton()
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (isLoading) {
                    item(key = "refresh_progress_indicator") {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = Color(0xFF0284C7),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
                // --- 1. HERO SPOTLIGHT: KUIS MINGGUAN (PERMANEN MUNCUL DI SEMUA TAB/JENJANG) ---
                if (weeklyQuizzes.isNotEmpty()) {
                    item(key = "featured_quiz_section") {
                    if (weeklyQuizzes.size > 1) {
                        // Carousel Mode jika kuis mingguan lebih dari 1
                        val pagerState = rememberPagerState(pageCount = { weeklyQuizzes.size })
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                pageSpacing = 12.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                val quiz = weeklyQuizzes[page]
                                FeaturedWeeklyQuizCard(
                                    quiz = quiz,
                                    pageIndex = page,
                                    pageCount = weeklyQuizzes.size,
                                    onClick = { onQuizClick(quiz) }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Indicator Dots Carousel
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(weeklyQuizzes.size) { index ->
                                    val isCurrent = pagerState.currentPage == index
                                    val dotWidth by animateDpAsState(
                                        targetValue = if (isCurrent) 22.dp else 6.dp,
                                        label = "dotWidthAnim"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .height(6.dp)
                                            .width(dotWidth)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCurrent) Color(0xFF0284C7) else Color(0xFFCBD5E1)
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        // Single Hero Card
                        val singleQuiz = weeklyQuizzes[0]
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            FeaturedWeeklyQuizCard(
                                quiz = singleQuiz,
                                pageIndex = 0,
                                pageCount = 1,
                                onClick = { onQuizClick(singleQuiz) }
                            )
                        }
                    }
                }
            }

            // --- 2. SEARCH & ENHANCED FILTER SECTION ---
            item(key = "search_and_filters") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        placeholder = {
                            Text(
                                "Cari judul kuis atau materi matematika...",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.5.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus",
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
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    // 1. Selector Jenjang Utama (Semua, SMP, SMA) Menggunakan Icon Standard
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val jenjangList = listOf(
                            Triple("Semua", "Semua", Icons.Default.Dashboard),
                            Triple("SMP", "SMP", Icons.Default.School),
                            Triple("SMA", "SMA", Icons.Default.WorkspacePremium)
                        )
                        jenjangList.forEach { (key, label, icon) ->
                            val isSelected = selectedJenjang == key
                            Surface(
                                onClick = {
                                    selectedJenjang = key
                                    selectedKelas = when (key) {
                                        "SMP" -> if (selectedKelas.contains("SMP")) selectedKelas else "Semua SMP"
                                        "SMA" -> if (selectedKelas.contains("SMA")) selectedKelas else "Semua SMA"
                                        else -> "Semua"
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF0284C7) else Color.White,
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Selector Sub-Kelas Langsung (Tanpa Animasi Mengganggu saat Layar Pertama Terbuka)
                    if (selectedJenjang != "Semua") {
                        val classOptions = if (selectedJenjang == "SMP") {
                            listOf(
                                "Semua SMP" to "Semua Kelas",
                                "VII SMP" to "Kelas VII",
                                "VIII SMP" to "Kelas VIII",
                                "IX SMP" to "Kelas IX"
                            )
                        } else {
                            listOf(
                                "Semua SMA" to "Semua Kelas",
                                "X SMA" to "Kelas X",
                                "XI SMA" to "Kelas XI",
                                "XII SMA" to "Kelas XII"
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            classOptions.forEach { (value, label) ->
                                val isClassSelected = selectedKelas == value
                                Surface(
                                    onClick = { selectedKelas = value },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isClassSelected) Color(0xFFE0F2FE) else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isClassSelected) Color(0xFF7DD3FC) else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        if (isClassSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isClassSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isClassSelected) Color(0xFF0369A1) else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Header Daftar & Counter
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
                                    )
                                )
                        )
                        Text(
                            text = "Daftar Tantangan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A),
                            fontFamily = interFontFamily
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = "${filteredQuizzes.size} Kuis Ditemukan",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            fontFamily = interFontFamily,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // --- 3. DAFTAR KUIS LIST ---
            if (filteredQuizzes.isEmpty()) {
                item {
                    EmptyQuizView(
                        searchQuery = searchQuery,
                        onRefresh = onRefresh
                    )
                }
            } else {
                items(
                    items = filteredQuizzes,
                    key = { it.id }
                ) { quiz ->
                    QuizCardItem(
                        quiz = quiz,
                        onClick = { onQuizClick(quiz) }
                    )
                }
            }
        }
    }
}
}

// --- Komponen Hero Banner Kuis Mingguan (Mendukung Carousel, Background Banner & Non-overlapping Layout) ---
@Composable
fun FeaturedWeeklyQuizCard(
    quiz: OnlineQuizItem,
    pageIndex: Int = 0,
    pageCount: Int = 1,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // 1. Background Banner Image
            Image(
                painter = painterResource(id = R.drawable.quiz_banner),
                contentDescription = quiz.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd
            )

            // 2. Gradient Scrim Horizontal (Gelap di kiri agar teks kontras & transparan di kanan agar ilustrasi buku/alat terlihat jernih)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF0F172A).copy(alpha = 0.94f),
                                0.52f to Color(0xFF0F172A).copy(alpha = 0.82f),
                                0.70f to Color(0xFF0F172A).copy(alpha = 0.25f),
                                1.0f to Color.Transparent
                            )
                        )
                    )
            )

            // 3. Gradient Scrim Vertikal di dasar kartu
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.65f to Color.Black.copy(alpha = 0.25f),
                                1.0f to Color.Black.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            // 4. Konten Teks & Tombol (Dibatasi 66% lebar agar TIDAK overlap dengan gambar/unsur ilustrasi di sebelah kanan)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Baris Atas: Badge Kuis Mingguan, Jenjang & Carousel Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFFEF08A),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFA16207),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "MINGGUAN",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                color = Color(0xFFA16207),
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    if (quiz.jenjang.isNotBlank()) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = quiz.jenjang,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

//                    if (quiz.isLearningMode) {
//                        Surface(
//                            color = Color(0xFF059669).copy(alpha = 0.95f),
//                            shape = RoundedCornerShape(6.dp),
//                            border = BorderStroke(1.dp, Color(0xFF6EE7B7))
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
//                                horizontalArrangement = Arrangement.spacedBy(3.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.School,
//                                    contentDescription = null,
//                                    tint = Color.White,
//                                    modifier = Modifier.size(11.dp)
//                                )
//                                Text(
//                                    text = "Mode Belajar",
//                                    color = Color.White,
//                                    fontSize = 10.sp,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                    }

//                    if (pageCount > 1) {
//                        Surface(
//                            color = Color.White.copy(alpha = 0.2f),
//                            shape = RoundedCornerShape(6.dp)
//                        ) {
//                            Text(
//                                text = "${pageIndex + 1}/$pageCount",
//                                color = Color.White,
//                                fontSize = 10.sp,
//                                fontWeight = FontWeight.Bold,
//                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
//                            )
//                        }
//                    }

                    // Tampilkan Skor jika kuis mingguan ini sudah pernah dikerjakan
                    if (quiz.isCompleted || quiz.highestScore > 0) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Skor: ${quiz.highestScore}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }

                // Bagian Tengah: Judul & Materi Kuis
                Column {
                    Text(
                        text = quiz.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (quiz.materi.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Topik: ${quiz.materi}",
                            color = Color(0xFFBAE6FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Baris Bawah: Rewards Capsule & Tombol Tantang
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reward Info
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "${quiz.rewardCoin}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "${quiz.exp}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Tombol Aksi
                    val isDone = quiz.isCompleted || quiz.highestScore > 0
                    Surface(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isDone) "Main Lagi" else "Tantang",
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Komponen Kartu Kuis Reguler ---
@Composable
fun QuizCardItem(
    quiz: OnlineQuizItem,
    onClick: () -> Unit
) {
    val isCompleted = quiz.isCompleted || quiz.highestScore > 0

    val cardJenjang = when {
        quiz.jenjang.isNotBlank() && !quiz.jenjang.equals("Umum", ignoreCase = true) -> quiz.jenjang
        quiz.title.contains("VII", ignoreCase = true) || quiz.title.contains("Kelas 7") -> "VII SMP"
        quiz.title.contains("VIII", ignoreCase = true) || quiz.title.contains("Kelas 8") -> "VIII SMP"
        quiz.title.contains("IX", ignoreCase = true) || quiz.title.contains("Kelas 9") -> "IX SMP"
        quiz.title.contains("XII", ignoreCase = true) || quiz.title.contains("Kelas 12") -> "XII SMA"
        quiz.title.contains("XI", ignoreCase = true) || quiz.title.contains("Kelas 11") -> "XI SMA"
        quiz.title.contains("X", ignoreCase = true) || quiz.title.contains("Kelas 10") -> "X SMA"
        else -> "Semua Jenjang"
    }

    val isSmp = cardJenjang.contains("SMP", ignoreCase = true) || cardJenjang.contains("VII") || cardJenjang.contains("VIII") || cardJenjang.contains("IX")
    val isSma = cardJenjang.contains("SMA", ignoreCase = true) || cardJenjang.contains("X") || cardJenjang.contains("XI") || cardJenjang.contains("XII")

    val (jenjangBg, jenjangBorder, jenjangText) = when {
        isSmp -> Triple(Color(0xFFF0F9FF), Color(0xFFBAE6FD), Color(0xFF0284C7))
        isSma -> Triple(Color(0xFFFAF5FF), Color(0xFFE9D5FF), Color(0xFF7E22CE))
        else -> Triple(Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFF475569))
    }

    val cardBorder = when {
        quiz.isLearningMode -> Color(0xFFA7F3D0)
        isCompleted -> Color(0xFFBAE6FD)
        else -> Color(0xFFE2E8F0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (quiz.isLearningMode) 2.5.dp else 1.5.dp,
            pressedElevation = 4.dp
        ),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle top decorative ambient tint untuk Mode Belajar
            if (quiz.isLearningMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF10B981).copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
                // Baris Atas: Jenjang, Mode Belajar & Skor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Badge Jenjang
                        Surface(
                            color = jenjangBg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, jenjangBorder)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = jenjangText,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = cardJenjang,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = jenjangText,
                                    fontFamily = interFontFamily
                                )
                            }
                        }

                        // Badge Mode Belajar (Bila Ada)
                        if (quiz.isLearningMode) {
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "Mode Belajar",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    }

                    // Badge Skor Penyelesaian
                    if (isCompleted) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Skor: ${quiz.highestScore}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D),
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Judul Kuis
                Text(
                    text = quiz.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp,
                    color = Color(0xFF0F172A),
                    lineHeight = 22.sp,
                    fontFamily = interFontFamily
                )

                // Subtitle / Materi & Badge Pembahasan
                if (quiz.materi.isNotBlank() || quiz.isLearningMode) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (quiz.materi.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (quiz.isLearningMode) Color(0xFF10B981) else Color(0xFF0284C7)
                                        )
                                )
                                Text(
                                    text = quiz.materi,
                                    color = Color(0xFF64748B),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = interFontFamily
                                )
                            }
                        }

//                        if (quiz.isLearningMode) {
//                            Surface(
//                                color = Color(0xFFFFFBEB),
//                                shape = RoundedCornerShape(6.dp),
//                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
//                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Default.Lightbulb,
//                                        contentDescription = null,
//                                        tint = Color(0xFFD97706),
//                                        modifier = Modifier.size(11.dp)
//                                    )
//                                    Text(
//                                        text = "Pembahasan",
//                                        fontSize = 10.5.sp,
//                                        color = Color(0xFFB45309),
//                                        fontWeight = FontWeight.Bold,
//                                        fontFamily = interFontFamily
//                                    )
//                                }
//                            }
//                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                Spacer(modifier = Modifier.height(11.dp))

                // Baris Reward & Tombol Navigasi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val questionCount = quiz.questions.size.takeIf { it > 0 } ?: quiz.totalQuestions.takeIf { it > 0 } ?: 0

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Soal Count Capsule
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "$questionCount Soal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569),
                                    fontFamily = interFontFamily
                                )
                            }
                        }

                        // 2. Koin Capsule
                        Surface(
                            color = Color(0xFFFFFBEB),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "+${quiz.rewardCoin}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    fontFamily = interFontFamily
                                )
                            }
                        }

                        // 3. EXP Capsule
                        Surface(
                            color = Color(0xFFFAF5FF),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE9D5FF))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFF9333EA),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "+${quiz.exp} EXP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7E22CE),
                                    fontFamily = interFontFamily
                                )
                            }
                        }
                    }

                    // Tombol Aksi Bulat Modern dengan Gradient & Elevation
                    val buttonBrush = when {
                        quiz.isLearningMode -> Brush.linearGradient(
                            listOf(Color(0xFF10B981), Color(0xFF059669))
                        )
                        isCompleted -> Brush.linearGradient(
                            listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))
                        )
                        else -> Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(buttonBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = if (quiz.isLearningMode) "Belajar" else if (isCompleted) "Main Lagi" else "Mulai",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Empty State View yang Lebih Bersahabat ---
@Composable
fun EmptyQuizView(
    searchQuery: String,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (searchQuery.isNotEmpty()) "Kuis Tidak Ditemukan" else "Belum Ada Kuis",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF0F172A)
            )

            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "Tidak ada kuis yang cocok dengan kata kunci '$searchQuery'."
                } else {
                    "Belum ada kuis pada kategori kelas ini."
                },
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF0284C7)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7))
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refresh", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
        }
    }
}

// --- Wrapper Page untuk Integrasi Navigasi dan ViewModel ---
data class CompletedQuizInfo(
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val wrongCount: Int = 0,
    val correctCount: Int = 0
)

@Composable
fun OnlineQuizPage(
    navController: NavController,
    viewModel: OnlineQuizViewModel = hiltViewModel()
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val userId = auth.currentUser?.uid ?: ""
    val firestore = remember { FirebaseFirestore.getInstance() }

    var userGrade by remember { mutableStateOf("Semua") }
    var quizzes by remember { mutableStateOf<List<OnlineQuizItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val completedResults = remember { mutableMapOf<String, CompletedQuizInfo>() }

    fun refreshData() {
        isLoading = true
        if (userId.isNotBlank()) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    val kelas = doc.getString("class")
                        ?: doc.getString("kelas")
                        ?: doc.getString("jenjang")
                    if (!kelas.isNullOrBlank()) {
                        userGrade = kelas
                    }
                }
            firestore.collection("online_quiz_results")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    completedResults.clear()
                    snapshot?.documents?.forEach { doc ->
                        val matId = doc.getString("materialId")
                            ?: doc.id.substringAfter("${userId}_", "")
                        val score = doc.getLong("totalPoints")?.toInt()
                            ?: doc.getLong("score")?.toInt()
                            ?: 0
                        val answersList = doc.get("answers") as? List<Map<String, Any>>
                        val totalQ = doc.getLong("totalQuestions")?.toInt()
                            ?: answersList?.size
                            ?: 0
                        val correct = doc.getLong("correctCount")?.toInt()
                            ?: answersList?.count { it["correct"] == true || it["isCorrect"] == true }
                            ?: 0
                        val wrong = doc.getLong("wrongCount")?.toInt()
                            ?: (totalQ - correct).coerceAtLeast(0)

                        if (matId.isNotBlank()) {
                            val currHigh = completedResults[matId]?.score ?: -1
                            if (!completedResults.containsKey(matId) || score > currHigh) {
                                completedResults[matId] = CompletedQuizInfo(
                                    score = score,
                                    totalQuestions = totalQ,
                                    wrongCount = wrong,
                                    correctCount = correct
                                )
                            }
                        }
                    }
                }
        }
        firestore.collection("online_quizzes")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.map { doc ->
                    mapDocToQuizItem(doc, completedResults)
                }
                quizzes = items
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    // Dapatkan data jenjang/kelas siswa dari profil jika tersedia
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    val kelas = doc.getString("class")
                        ?: doc.getString("kelas")
                        ?: doc.getString("jenjang")
                    if (!kelas.isNullOrBlank()) {
                        userGrade = kelas
                    }
                }
        }
    }

    // Listener real-time untuk kuis online dan hasil pengerjaan siswa
    DisposableEffect(userId) {
        fun updateQuizListWithCompletions(rawList: List<OnlineQuizItem>): List<OnlineQuizItem> {
            return rawList.map { item ->
                val info = completedResults[item.id]
                val isCompleted = info != null
                item.copy(
                    isCompleted = isCompleted,
                    highestScore = info?.score ?: item.highestScore,
                    totalQuestions = info?.totalQuestions ?: item.totalQuestions,
                    wrongCount = info?.wrongCount ?: item.wrongCount,
                    correctCount = info?.correctCount ?: item.correctCount
                )
            }
        }

        val resultsListener = if (userId.isNotBlank()) {
            firestore.collection("online_quiz_results")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, _ ->
                    completedResults.clear()
                    snapshot?.documents?.forEach { doc ->
                        val matId = doc.getString("materialId")
                            ?: doc.id.substringAfter("${userId}_", "")
                        val score = doc.getLong("totalPoints")?.toInt()
                            ?: doc.getLong("score")?.toInt()
                            ?: 0
                        val answersList = doc.get("answers") as? List<Map<String, Any>>
                        val totalQ = doc.getLong("totalQuestions")?.toInt()
                            ?: answersList?.size
                            ?: 0
                        val correct = doc.getLong("correctCount")?.toInt()
                            ?: answersList?.count { it["correct"] == true || it["isCorrect"] == true }
                            ?: 0
                        val wrong = doc.getLong("wrongCount")?.toInt()
                            ?: (totalQ - correct).coerceAtLeast(0)

                        if (matId.isNotBlank()) {
                            val currHigh = completedResults[matId]?.score ?: -1
                            if (!completedResults.containsKey(matId) || score > currHigh) {
                                completedResults[matId] = CompletedQuizInfo(
                                    score = score,
                                    totalQuestions = totalQ,
                                    wrongCount = wrong,
                                    correctCount = correct
                                )
                            }
                        }
                    }
                    quizzes = updateQuizListWithCompletions(quizzes)
                }
        } else null

        val quizListener = firestore.collection("online_quizzes")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                val items = snapshot.documents.map { doc ->
                    mapDocToQuizItem(doc, completedResults)
                }
                quizzes = items
                isLoading = false
            }

        onDispose {
            quizListener.remove()
            resultsListener?.remove()
        }
    }

    OnlineQuizLanding(
        quizzes = quizzes,
        userGrade = userGrade,
        isLoading = isLoading,
        onQuizClick = { quiz ->
            val parsedQuestions = quiz.questions.mapNotNull { item ->
                when (item) {
                    is Map<*, *> -> {
                        val qId = item["id"] as? String ?: return@mapNotNull null
                        val points = (item["points"] as? Number)?.toInt() ?: 10
                        val timer = (item["timer"] as? Number)?.toInt() ?: 30
                        QuizQuestionRef(id = qId, points = points, timer = timer)
                    }
                    is String -> QuizQuestionRef(id = item, points = 10, timer = 30)
                    else -> null
                }
            }
            val numQ = parsedQuestions.size.takeIf { it > 0 } ?: quiz.questions.size
            val totalSeconds = if (parsedQuestions.isNotEmpty()) parsedQuestions.sumOf { it.timer } else (numQ * 30)
            val durMinutes = ((totalSeconds + 59) / 60).coerceAtLeast(1)

            val materialDesc = OnlineQuizDesc(
                id = quiz.id,
                title = quiz.title,
                active = quiz.active,
                description = quiz.description,
                questions = parsedQuestions,
                durationMinutes = durMinutes,
                rewardCoin = quiz.rewardCoin.toInt(),
                exp = quiz.exp.toInt(),
                classLevel = quiz.jenjang.ifBlank { "Umum" },
                isRandomized = quiz.isRandomized,
                isLearningMode = quiz.isLearningMode,
                numberQuestion = numQ
            )
            viewModel.selectMaterial(materialDesc)
            navController.navigate("OnlineQuizLobby/${userId}/description")
        },
        onBackClick = {
            if (!navController.popBackStack()) {
                navController.navigate("dashboard") {
                    popUpTo(0)
                }
            }
        },
        onRefresh = { refreshData() },
        bottomBar = {
            BottomNavigationMenu(navController = navController)
        }
    )
}

@Suppress("UNCHECKED_CAST")
private fun mapDocToQuizItem(
    doc: DocumentSnapshot,
    completedMap: Map<String, CompletedQuizInfo> = emptyMap()
): OnlineQuizItem {
    val id = doc.id
    val title = doc.getString("title") ?: ""
    val materi = doc.getString("materi") ?: ""
    val jenjang = doc.getString("jenjang")
        ?: doc.getString("classLevel")
        ?: ""
    val description = doc.getString("description") ?: ""
    val rewardCoin = doc.getLong("rewardCoin") ?: 0L
    val exp = doc.getLong("exp") ?: 0L
    val isRandomized = doc.getBoolean("isRandomized") ?: false
    val isFeatured = doc.getBoolean("isFeatured") ?: false
    val isLearningMode = doc.getBoolean("isLearningMode") ?: false
    val active = doc.getBoolean("active") ?: true
    val questions = doc.get("questions") as? List<Any> ?: emptyList()
    val info = completedMap[id]
    val isCompleted = info != null
    val highestScore = info?.score ?: 0
    val totalQuestions = info?.totalQuestions ?: 0
    val wrongCount = info?.wrongCount ?: 0
    val correctCount = info?.correctCount ?: 0

    return OnlineQuizItem(
        id = id,
        title = title,
        materi = materi,
        jenjang = jenjang,
        description = description,
        rewardCoin = rewardCoin,
        exp = exp,
        isRandomized = isRandomized,
        isFeatured = isFeatured,
        isLearningMode = isLearningMode,
        active = active,
        questions = questions,
        isCompleted = isCompleted,
        highestScore = highestScore,
        totalQuestions = totalQuestions,
        wrongCount = wrongCount,
        correctCount = correctCount
    )
}

// --- Skeleton Shimmer Utilities ---
@Composable
fun Modifier.quizLandingShimmer(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF1F5F9)
): Modifier {
    val transition = rememberInfiniteTransition(label = "quizLandingShimmer")
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

// --- Skeleton Components untuk OnlineQuizLanding ---
@Composable
fun OnlineQuizBannerSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .quizLandingShimmer(RoundedCornerShape(22.dp))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(85.dp)
                            .height(20.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(20.dp)
                                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(14.dp)
                                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(26.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun OnlineQuizFilterSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search bar shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .quizLandingShimmer(RoundedCornerShape(16.dp))
        )
        // Jenjang tabs shimmer (3 columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .quizLandingShimmer(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
fun OnlineQuizCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp)
                            .quizLandingShimmer(RoundedCornerShape(6.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp)
                            .quizLandingShimmer(RoundedCornerShape(6.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .width(45.dp)
                        .height(20.dp)
                        .quizLandingShimmer(RoundedCornerShape(6.dp))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(18.dp)
                    .quizLandingShimmer(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .quizLandingShimmer(RoundedCornerShape(4.dp))
            )
            HorizontalDivider(color = Color(0xFFF8FAFC), thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(16.dp)
                            .quizLandingShimmer(RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(45.dp)
                            .height(16.dp)
                            .quizLandingShimmer(RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(16.dp)
                            .quizLandingShimmer(RoundedCornerShape(4.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .quizLandingShimmer(CircleShape)
                )
            }
        }
    }
}
