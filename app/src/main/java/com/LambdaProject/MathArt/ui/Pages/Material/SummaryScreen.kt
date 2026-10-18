package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.QuizViewModel
import com.LambdaProject.MathArt.data.model.MaterialDocument
import com.LambdaProject.MathArt.data.model.claimMaterialExpReward
import com.LambdaProject.MathArt.data.model.getExpProgressInCurrentLevel
import com.LambdaProject.MathArt.data.model.getLevelInfo
import com.LambdaProject.MathArt.data.model.getLevelInfoForExp
import com.LambdaProject.MathArt.data.repository.SessionRepository.markSessionCompleted
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

@Composable
fun SummaryScreen(
    viewModel: QuizViewModel,
    materialDoc: MaterialDocument? = null,
    materialId: String = "transformasi_geometri",
    onRepeatMaterial: (() -> Unit)? = null,
    onBackToDashboard: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val userAnswers = viewModel.userAnswers
    val db = remember { FirebaseFirestore.getInstance() }
    var showExitDialog by remember { mutableStateOf(false) }
    var currentMaterialDoc by remember { mutableStateOf(materialDoc) }
    val hasEvaluationQuiz = remember(currentMaterialDoc, materialDoc, userAnswers) {
        val doc = currentMaterialDoc ?: materialDoc
        if (doc != null) {
            !doc.quizId.isNullOrBlank() || doc.quiz != null || userAnswers.isNotEmpty()
        } else {
            userAnswers.isNotEmpty() || materialId == "transformasi_geometri"
        }
    }
    var isLoading by remember { mutableStateOf(userAnswers.isEmpty() && hasEvaluationQuiz) }
    var claimedExpAmount by remember { mutableIntStateOf(-1) }
    var userExp by remember { mutableLongStateOf(0L) }
    var userLevel by remember { mutableIntStateOf(1) }

    LaunchedEffect(materialId) {
        if (currentMaterialDoc == null) {
            com.LambdaProject.MathArt.data.repository.MaterialRepository.getMaterial(
                materialId = materialId,
                onSuccess = { doc ->
                    currentMaterialDoc = doc
                }
            )
        }
    }

    val targetExp = currentMaterialDoc?.exp ?: materialDoc?.exp ?: 150

    // Klaim first-time reward EXP saat sampai di SummaryScreen
    LaunchedEffect(materialId, userId, targetExp) {
        if (userId != null && claimedExpAmount == -1) {
            val result = claimMaterialExpReward(
                userId = userId,
                materialId = materialId,
                expAmount = targetExp
            )
            claimedExpAmount = result.getOrDefault(0)
        }
    }

    // Real-time listener untuk update exp dan level akun pengguna
    DisposableEffect(userId) {
        if (userId == null) return@DisposableEffect onDispose {}
        val listener = Firebase.firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val exp = snapshot.getLong("exp") ?: 0L
                    userExp = exp
                    val level = snapshot.getLong("level")?.toInt()
                        ?: getLevelInfoForExp(exp).level
                    userLevel = level
                }
            }
        onDispose { listener.remove() }
    }

    LaunchedEffect(materialId, userId, hasEvaluationQuiz) {
        if (hasEvaluationQuiz && userAnswers.isEmpty() && userId != null) {
            viewModel.loadQuizResults(userId, materialId) {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading && userAnswers.isEmpty()) {
        SummaryScreenSkeleton()
        return
    }

    val totalQuestions = if (viewModel.questions.isNotEmpty()) {
        viewModel.questions.size
    } else if (userAnswers.isNotEmpty()) {
        userAnswers.size
    } else {
        1
    }
    val correctCount = userAnswers.count { it.isCorrect }
    val accuracy = if (totalQuestions > 0 && userAnswers.isNotEmpty()) {
        ((correctCount.toFloat() / totalQuestions) * 100).toInt()
    } else {
        0
    }
    val displayMaterialTitle = materialDoc?.title?.ifBlank { "Transformasi Geometri" } ?: "Transformasi Geometri"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. Hero Achievement Card ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 6.dp,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF3B82F6))
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Trophy Badge
                    Image(
                        painter = painterResource(id = R.drawable.ic_trophy),
                        contentDescription = "Piala Keberhasilan",
                        modifier = Modifier.size(76.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val heroTitle = when {
                        !hasEvaluationQuiz -> "Materi Selesai Dipelajari!"
                        accuracy == 100 -> "Luar Biasa, Sempurna!"
                        accuracy >= 60 -> "Selamat, Kamu Berhasil!"
                        else -> "Sesi Selesai, Tetap Semangat!"
                    }

                    val heroSubtitle = when {
                        !hasEvaluationQuiz -> {
                            "Kamu telah menyelesaikan seluruh materi pembelajaran $displayMaterialTitle dengan sangat baik."
                        }
                        accuracy >= 60 -> {
                            "Kamu telah menyelesaikan materi $displayMaterialTitle beserta evaluasi kuisnya dengan baik."
                        }
                        else -> {
                            "Kamu telah menyelesaikan materi $displayMaterialTitle. Yuk, pelajari kembali materinya dan coba kuis lagi agar nilaimu tuntas!"
                        }
                    }

                    Text(
                        text = heroTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = heroSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = interFontFamily,
                        color = Color(0xFFDBEAFE),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // --- 2. EXP Reward & Account Level Progression Card ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, if (claimedExpAmount > 0) Color(0xFFFBBF24) else Color(0xFFE2E8F0)),
            shadowElevation = if (claimedExpAmount > 0) 4.dp else 2.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header EXP Reward status
                if (claimedExpAmount > 0) {
                    // Celebration Banner (First-Time Claim)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF59E0B),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFD97706)
                                ) {
                                    Text(
                                        text = "FIRST-TIME REWARD",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = interFontFamily,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "+$claimedExpAmount EXP Berhasil Diklaim!",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Reward otomatis ditambahkan ke progress akunmu.",
                                    fontFamily = interFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                } else if (claimedExpAmount == 0) {
                    // Already Claimed Banner (2nd+ visit or non-first-time)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hadiah: +$targetExp EXP (Sudah Diklaim)",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "Hadiah EXP bersifat 1x reward dan sudah pernah ditambahkan ke akunmu.",
                                    fontFamily = interFontFamily,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Level Progress Details
                val levelInfo = getLevelInfo(userLevel)
                val (expInLevel, expNeededForNext) = getExpProgressInCurrentLevel(userExp)
                val progressRatio = if (expNeededForNext > 0L) {
                    (expInLevel.toFloat() / expNeededForNext.toFloat()).coerceIn(0f, 1f)
                } else {
                    1f
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Text(
                                text = "Level $userLevel",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color(0xFF1D4ED8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = levelInfo.title,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "$userExp EXP",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (userLevel >= 15) "Level Maksimal" else "$expInLevel / $expNeededForNext EXP",
                        fontFamily = interFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    if (userLevel < 15) {
                        val remaining = expNeededForNext - expInLevel
                        Text(
                            text = "Tersisa $remaining EXP ke Level ${userLevel + 1}",
                            fontFamily = interFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }

        if (hasEvaluationQuiz) {
            // --- 3. Score & Stats Overview Card ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Hasil Evaluasi Pembelajaran",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stat 1: Skor Akurasi
                        StatItemBox(
                            label = "Nilai Akhir",
                            value = "$accuracy",
                            subValue = "dari 100",
                            accentColor = Color(0xFF2563EB),
                            bgColor = Color(0xFFEFF6FF),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        // Stat 2: Jumlah Benar
                        StatItemBox(
                            label = "Jawaban Benar",
                            value = "$correctCount",
                            subValue = "dari $totalQuestions soal",
                            accentColor = Color(0xFF16A34A),
                            bgColor = Color(0xFFDCFCE7),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        // Stat 3: Status
                        StatItemBox(
                            label = "Status",
                            value = if (accuracy >= 60) "Tuntas" else "Perlu Ulang",
                            subValue = if (accuracy >= 60) "Lulus KKM" else "Pelajari Lagi",
                            accentColor = if (accuracy >= 60) Color(0xFF0D9488) else Color(0xFFE11D48),
                            bgColor = if (accuracy >= 60) Color(0xFFCCFBF1) else Color(0xFFFFF1F2),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Motivational Feedback Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = when {
                                    accuracy == 100 -> "Sempurna! Kamu memahami seluruh konsep materi dengan sangat baik."
                                    accuracy >= 65 -> "Bagus Sekali! Pemahamanmu tentang materi ini sudah sangat mantap dan memuaskan."
                                    else -> "Tetap Semangat! Kamu bisa meninjau ulang materi kapan saja untuk memperdalam pemahamanmu."
                                },
                                fontFamily = interFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } else {
            // --- 3. Materi Mandiri Overview Card (Tanpa Kuis Evaluasi) ---
            val totalSections = currentMaterialDoc?.sections?.size ?: materialDoc?.sections?.size ?: 1
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ringkasan Pembelajaran Mandiri",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatItemBox(
                            label = "Bab Materi",
                            value = "$totalSections",
                            subValue = "Bab Tuntas",
                            accentColor = Color(0xFF2563EB),
                            bgColor = Color(0xFFEFF6FF),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        StatItemBox(
                            label = "Progres",
                            value = "100%",
                            subValue = "Selesai Baca",
                            accentColor = Color(0xFF16A34A),
                            bgColor = Color(0xFFDCFCE7),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        StatItemBox(
                            label = "Evaluasi",
                            value = "Mandiri",
                            subValue = "Non-Kuis",
                            accentColor = Color(0xFF0D9488),
                            bgColor = Color(0xFFCCFBF1),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Materi ini berfokus pada pendalaman konsep dan teori interaktif secara mandiri tanpa memerlukan kuis evaluasi.",
                                fontFamily = interFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF166534),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Dynamic Section Completion Checklist ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Daftar Materi yang Telah Dipelajari",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val sectionTitles = if (!materialDoc?.sections.isNullOrEmpty()) {
                    materialDoc!!.sections.map { "Materi: ${it.title}" } + (if (hasEvaluationQuiz) listOf("Evaluasi Pembelajaran") else emptyList())
                } else if (!currentMaterialDoc?.sections.isNullOrEmpty()) {
                    currentMaterialDoc!!.sections.map { "Materi: ${it.title}" } + (if (hasEvaluationQuiz) listOf("Evaluasi Pembelajaran") else emptyList())
                } else {
                    listOf(
                        "Materi: Pengantar Etnomatematika",
                        "Materi: Translasi (Pergeseran)",
                        "Materi: Refleksi (Pencerminan)",
                        "Materi: Rotasi (Perputaran)",
                        "Materi: Dilatasi (Perubahan Ukuran)"
                    ) + (if (hasEvaluationQuiz) listOf("Evaluasi Pembelajaran") else emptyList())
                }

                sectionTitles.forEach { sectionTitle ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = sectionTitle,
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Selesai",
                            fontFamily = interFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }
        }

        // --- 5. Quiz Review Recap ---
        if (userAnswers.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Rangkuman Jawaban Soal",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    userAnswers.forEachIndexed { index, ans ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = if (ans.isCorrect) Color(0xFFF0FDF4) else Color(0xFFFFF1F2),
                            border = BorderStroke(1.dp, if (ans.isCorrect) Color(0xFFBBF7D0) else Color(0xFFFECDD3))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Soal Evaluasi #${index + 1}",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = if (ans.isCorrect) Color(0xFF166534) else Color(0xFF9F1239)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (ans.isCorrect) Color(0xFF16A34A) else Color(0xFFE11D48)
                                    ) {
                                        Text(
                                            text = if (ans.isCorrect) "✓ Tepat" else "✕ Belum Tepat",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            fontFamily = interFontFamily
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Jawabanmu: ",
                                        fontFamily = interFontFamily,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = ans.selectedAnswers.joinToString(", ").ifBlank { "-" },
                                        fontFamily = interFontFamily,
                                        fontSize = 12.sp,
                                        color = if (ans.isCorrect) Color(0xFF166534) else Color(0xFF9F1239),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Kunci Jawaban: ",
                                        fontFamily = interFontFamily,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = ans.correctAnswers.joinToString(", ").ifBlank { "-" },
                                        fontFamily = interFontFamily,
                                        fontSize = 12.sp,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Action Buttons ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { showExitDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Selesai & Kembali ke Beranda",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            if (onRepeatMaterial != null) {
                OutlinedButton(
                    onClick = {
                        viewModel.resetQuiz()
                        onRepeatMaterial()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Pelajari Ulang Materi",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Kembali ke Beranda?",
                    fontWeight = FontWeight.Black,
                    fontFamily = interFontFamily,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Sesi belajarmu telah disimpan dengan sukses. Kamu bisa mempelajari materi ini lagi kapan saja dari dashboard.",
                    fontFamily = interFontFamily,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        if (userId != null) {
                            markSessionCompleted(userId = userId, materialId = materialId)
                        }
                        viewModel.resetQuiz()
                        viewModel.resetQuizReadyState()
                        onBackToDashboard()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text(
                        "Ya, Kembali",
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFontFamily,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, fontFamily = interFontFamily, color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
private fun StatItemBox(
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            val valueFontSize = when {
                value.length > 8 -> 13.sp
                value.length > 5 -> 15.sp
                else -> 18.sp
            }
            Text(
                text = value,
                fontSize = valueFontSize,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                color = accentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subValue,
                fontSize = 9.5.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                color = accentColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
