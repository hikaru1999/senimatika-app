package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.LambdaProject.MathArt.data.model.ContentBlock
import com.LambdaProject.MathArt.data.model.MaterialSection
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.ui.components.MathText
import com.LambdaProject.MathArt.utils.ZoomableImageOverlay

@Composable
fun DynamicSectionScreen(
    section: MaterialSection,
    currentPage: Int,
    myPage: Int,
    isLastSection: Boolean,
    nextSectionTitle: String? = null,
    hasQuiz: Boolean = true,
    isLoading: Boolean = false,
    onNext: () -> Unit,
    onQuizNavigate: () -> Unit
) {
    if (isLoading) {
        DynamicSectionSkeleton()
        return
    }

    if (section.blocks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Konten untuk bagian ini sedang disiapkan.",
                fontFamily = interFontFamily,
                fontSize = 15.sp,
                color = Color.Gray
            )
        }
        return
    }

    val scrollState = rememberScrollState()
    val isPageActive = currentPage == myPage
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentPage) {
        if (!isPageActive) {
            YouTubePlayerManager.pauseAll()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            section.blocks.forEachIndexed { index, block ->
                key("${block.type}_$index") {
                    DynamicBlockRenderer(
                        block = block,
                        isPageActive = isPageActive,
                        onImageClick = { url -> zoomedImageUrl = url }
                    )
                }
            }

        // Action Button
        Button(
            onClick = {
                if (isLastSection) {
                    onQuizNavigate()
                } else {
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLastSection && !hasQuiz) Color(0xFF10B981) else Color(0xFF1976D2)
            )
        ) {
            val buttonText = when {
                !isLastSection -> "Buka: ${nextSectionTitle ?: "Bab Berikutnya"}"
                hasQuiz -> "Ambil Kuis Evaluasi"
                else -> "Selesaikan Materi & Lihat Hasil Belajar"
            }
            Text(
                text = buttonText,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    if (zoomedImageUrl != null) {
        ZoomableImageOverlay(
            imageUrl = zoomedImageUrl,
            onDismiss = { zoomedImageUrl = null }
        )
    }
}
}

@Composable
fun DynamicBlockRenderer(
    block: ContentBlock,
    isPageActive: Boolean = true,
    onImageClick: (String) -> Unit = {}
) {
    val normalizedType = remember(block.type) {
        block.type.trim().uppercase()
            .replace("-", "_")
            .replace(" ", "_")
    }

    when (normalizedType) {
        "HEADER", "HEADING", "TITLE", "SUBHEADER", "SUB_HEADER" -> {
            val rawText = block.text?.ifBlank { block.title } ?: block.title.orEmpty()
            val hasMath = remember(rawText) {
                rawText.contains("$") || rawText.contains("\\")
            }
            if (hasMath) {
                val headerFontSize = when (block.level) {
                    3 -> 16
                    2 -> 19
                    else -> 22
                }
                MathText(
                    text = rawText,
                    fontSize = headerFontSize,
                    color = Color(0xFF1A237E),
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                Text(
                    text = rawText,
                    style = when (block.level) {
                        3 -> MaterialTheme.typography.titleMedium
                        2 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.headlineSmall
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1A237E),
                    letterSpacing = (-0.5).sp
                )
            }
        }

        "PARAGRAPH" -> {
            val rawText = block.text.orEmpty()
            val needsRichRenderer = remember(rawText) {
                rawText.contains("$") ||
                rawText.contains("\\") ||
                rawText.contains("![")
            }

            if (needsRichRenderer) {
                MathText(
                    text = rawText,
                    fontSize = 15,
                    color = Color(0xFF455A64),
                    textAlign = "justify"
                )
            } else {
                val alignment = remember(rawText) { determineTextAlignment(rawText) }
                val formatted = remember(rawText) {
                    parseFormattedMarkdown(rawText)
                }
                Text(
                    text = formatted,
                    fontFamily = interFontFamily,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = alignment,
                    lineHeight = 24.sp,
                    color = Color(0xFF455A64)
                )
            }
        }

        "BANNER_IMAGE", "BANNER", "IMAGE_BANNER", "HERO_IMAGE", "HERO", "IMAGE" -> {
            if (!block.imageUrl.isNullOrBlank()) {
                val bannerRatio = remember(block.aspectRatio, block.caption, block.badge, block.description, block.level) {
                    when (block.aspectRatio?.trim()?.lowercase()) {
                        "4:3", "4/3", "4_3" -> 4f / 3f
                        "16:9", "16/9", "16_9" -> 16f / 9f
                        else -> {
                            if (block.level == 43 ||
                                block.badge?.contains("4:3") == true ||
                                block.caption?.contains("4:3") == true ||
                                block.description?.contains("4:3") == true
                            ) {
                                4f / 3f
                            } else {
                                16f / 9f
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(bannerRatio)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color(0x14000000),
                                spotColor = Color(0x241E293B)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onImageClick(block.imageUrl) },
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            SubcomposeAsyncImage(
                                model = block.imageUrl,
                                contentDescription = block.title ?: "Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                loading = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .materialShimmer(RoundedCornerShape(0.dp))
                                    )
                                }
                            )

                            // Top-start Badge Pill if present
                            if (!block.badge.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF38BDF8))
                                        )
                                        Text(
                                            text = block.badge.uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            fontFamily = interFontFamily,
                                            letterSpacing = 0.6.sp
                                        )
                                    }
                                }
                            }

                            // Bottom Gradient Scrim & Title if present
                            if (!block.title.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.35f),
                                                    Color.Black.copy(alpha = 0.82f)
                                                )
                                            )
                                        )
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Text(
                                        text = block.title,
                                        color = Color.White,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    // Optional Caption below the banner
                    if (!block.caption.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = block.caption,
                                    fontFamily = interFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        "INDICATOR_LIST", "SIFAT_SIFAT" -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFE3F2FD),
                border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = block.title?.ifBlank { "Sifat-sifat" } ?: "Sifat-sifat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = interFontFamily,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    block.items?.forEachIndexed { index, poin ->
                        Row(
                            modifier = Modifier.padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1976D2),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (poin.contains("$") || poin.contains("\\")) {
                                MathText(
                                    text = poin,
                                    fontSize = 14,
                                    color = Color(0xFF0D47A1),
                                    textAlign = "justify",
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Text(
                                    text = poin,
                                    fontFamily = interFontFamily,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Justify,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF0D47A1),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        "CULTURE_SECTION", "EKSPLORASI_BUDAYA", "CULTURE", "BUDAYA" -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.2.dp, Color(0xFFFED7AA)),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header: Badge on top, Title below
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFC2410C)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = block.badge?.ifBlank { "EKSPLORASI BUDAYA" } ?: "EKSPLORASI BUDAYA",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    fontFamily = interFontFamily,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Text(
                            text = block.title?.ifBlank { "Eksplorasi Budaya" } ?: "Eksplorasi Budaya",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color(0xFF431407)
                        )
                    }

                    if (!block.description.isNullOrBlank()) {
                        val desc = block.description
                        if (desc.contains("$") || desc.contains("\\")) {
                            MathText(
                                text = desc,
                                fontSize = 14,
                                color = Color(0xFF7C2D12),
                                textAlign = "justify"
                            )
                        } else {
                            Text(
                                text = desc,
                                fontFamily = interFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF7C2D12),
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Justify
                            )
                        }
                    }

                    if (!block.imageUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = block.imageUrl,
                            contentDescription = block.title ?: "Eksplorasi Budaya",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onImageClick(block.imageUrl) },
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .materialShimmer(RoundedCornerShape(16.dp))
                                )
                            }
                        )
                    }

                    if (!block.caption.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFEDD5),
                            border = BorderStroke(1.dp, Color(0xFFFDBA74).copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFC2410C),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = block.caption,
                                    fontFamily = interFontFamily,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF9A3412),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        "VIDEO_YOUTUBE", "YOUTUBE" -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!block.title.isNullOrBlank()) {
                    val vTitle = block.title
                    if (vTitle.contains("$") || vTitle.contains("\\")) {
                        MathText(
                            text = vTitle,
                            fontSize = 18,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A237E)
                        )
                    } else {
                        Text(
                            text = vTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFontFamily,
                            color = Color(0xFF1A237E)
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black
                ) {
                    if (isPageActive) {
                        YouTubePlayer(youtubeUrl = block.url.orEmpty())
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }

        "CANVAS_ANNOTATION", "KANVAS_ANOTASI" -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF1F8E9),
                border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val canvasTitle = block.title?.ifBlank { "Waktunya Mencoret!" } ?: "Waktunya Mencoret!"
                    if (canvasTitle.contains("$") || canvasTitle.contains("\\")) {
                        MathText(
                            text = canvasTitle,
                            fontSize = 16,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    } else {
                        Text(
                            text = canvasTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFontFamily,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    DynamicPictureAnnotation(
                        baseImageUrl = block.baseImageUrl.orEmpty(),
                        solutionImageUrl = block.solutionImageUrl.orEmpty(),
                        explanationText = block.explanationText.orEmpty(),
                        instruction = block.instruction
                    )
                }
            }
        }

        "IN_ARTICLE_QUIZ", "KUIS_SINGKAT" -> {
            DynamicInArticleQuiz(block = block)
        }

        "FLIPCARDS", "FLIP_CARD", "FLIP_CARDS" -> {
            DynamicFlipCardsBlock(block = block)
        }

        "FORMULA", "RUMUS_KUNCI", "KEY_FORMULA" -> {
            DynamicFormulaBlock(block = block)
        }

        "WORKED_EXAMPLE", "CONTOH_SOAL", "EXAMPLE", "STEP_BY_STEP" -> {
            DynamicWorkedExampleBlock(block = block)
        }
    }
}

fun determineTextAlignment(text: String): TextAlign {
    return when {
        text.contains("[center]", ignoreCase = true) -> TextAlign.Center
        text.contains("[right]", ignoreCase = true) -> TextAlign.Right
        text.contains("[left]", ignoreCase = true) -> TextAlign.Start
        else -> TextAlign.Justify
    }
}

fun parseFormattedMarkdown(rawText: String): AnnotatedString {
    val clean = rawText
        .replace(Regex("\\[/?(justify|center|left|right)\\]", RegexOption.IGNORE_CASE), "")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .trim()

    val formattedLines = clean.lines().joinToString("\n") { line ->
        val trimmed = line.trimStart()
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("+ ") || trimmed.startsWith("• ")) {
            "  •  " + trimmed.substring(2).trimStart()
        } else {
            line
        }
    }

    return buildAnnotatedString {
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        var lastIndex = 0

        val matches = boldRegex.findAll(formattedLines).toList()
        if (matches.isEmpty()) {
            append(formattedLines)
        } else {
            for (match in matches) {
                val start = match.range.first
                val end = match.range.last + 1
                if (start > lastIndex) {
                    append(formattedLines.substring(lastIndex, start))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                lastIndex = end
            }
            if (lastIndex < formattedLines.length) {
                append(formattedLines.substring(lastIndex))
            }
        }
    }
}

/**
 * Loading Skeleton shimmer untuk DynamicSectionScreen
 */
@Composable
fun DynamicSectionSkeleton(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Header / Title Shimmer
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(28.dp)
                .materialShimmer(RoundedCornerShape(8.dp))
        )

        // 2. Banner / Hero Image Shimmer (16:9)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color(0x14000000),
                    spotColor = Color(0x241E293B)
                )
                .materialShimmer(RoundedCornerShape(22.dp))
        )

        // 3. Paragraph lines Shimmer
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(14.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )
        }

        // 4. Interactive / Cultural / Formula Card Shimmer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.2.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .materialShimmer(CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(18.dp)
                            .materialShimmer(RoundedCornerShape(6.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(13.dp)
                        .materialShimmer(RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(13.dp)
                        .materialShimmer(RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .materialShimmer(RoundedCornerShape(16.dp))
                )
            }
        }

        // 5. Secondary Paragraph / Points Shimmer
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(14.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )
        }

        // 6. Action Button Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .materialShimmer(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(32.dp))
    }
}

