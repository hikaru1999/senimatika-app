package com.LambdaProject.MathArt.ui.Pages.Material

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Modifier efek Shimmer animasi halus untuk seluruh Skeleton materi & kuis
 */
@Composable
fun Modifier.materialShimmer(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = Color(0xFFE2E8F0),
    highlightColor: Color = Color(0xFFF1F5F9)
): Modifier {
    val transition = rememberInfiniteTransition(label = "materialShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "materialShimmerTranslate"
    )

    return this
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(baseColor, highlightColor, baseColor),
                start = Offset(translateAnim, translateAnim),
                end = Offset(translateAnim + 350f, translateAnim + 350f)
            )
        )
}

/**
 * 1. Loading Skeleton untuk MaterialPage (MaterialScreen)
 */
@Composable
fun MaterialPageSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FE))
    ) {
        // TopAppBar Shimmer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .materialShimmer(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(22.dp)
                        .materialShimmer(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.size(36.dp))
            }
        }

        // Tab Row Shimmer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(36.dp)
                        .materialShimmer(RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(36.dp)
                        .materialShimmer(RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(36.dp)
                        .materialShimmer(RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(36.dp)
                        .materialShimmer(RoundedCornerShape(12.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area Shimmer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
        ) {
            MaterialContentSkeleton()
        }
    }
}

/**
 * 2. Loading Skeleton untuk Konten Materi
 */
@Composable
fun MaterialContentSkeleton() {
    DynamicSectionSkeleton()
}

/**
 * 3. Loading Skeleton untuk QuizScreen
 */
@Composable
fun QuizScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Quiz Header: Question Progress & Timer / Score Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(20.dp)
                    .materialShimmer(RoundedCornerShape(6.dp))
            )
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(28.dp)
                    .materialShimmer(RoundedCornerShape(12.dp))
            )
        }

        // Progress Bar Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .materialShimmer(RoundedCornerShape(4.dp))
        )

        // Question Card Shimmer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).materialShimmer(RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(16.dp).materialShimmer(RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).materialShimmer(RoundedCornerShape(4.dp)))

                // Optional Question Image Shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .materialShimmer(RoundedCornerShape(12.dp))
                )
            }
        }

        // 4 Options Shimmer
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .materialShimmer(CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .materialShimmer(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        // Next / Submit Button Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .materialShimmer(RoundedCornerShape(14.dp))
        )
    }
}

/**
 * 8. Loading Skeleton untuk SummaryScreen
 */
@Composable
fun SummaryScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FE))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Card Shimmer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .materialShimmer(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(24.dp)
                        .materialShimmer(RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(14.dp)
                        .materialShimmer(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats Row Shimmer (3 stats boxes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp)
                            .materialShimmer(RoundedCornerShape(14.dp))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp)
                            .materialShimmer(RoundedCornerShape(14.dp))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp)
                            .materialShimmer(RoundedCornerShape(14.dp))
                    )
                }
            }
        }

        // Summary List Title Shimmer
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(20.dp)
                .materialShimmer(RoundedCornerShape(6.dp))
        )

        // Question Answers Review Items Shimmer
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).materialShimmer(RoundedCornerShape(4.dp)))
                    Box(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp).materialShimmer(RoundedCornerShape(4.dp)))
                }
            }
        }

        // Back to Dashboard Button Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .materialShimmer(RoundedCornerShape(14.dp))
        )
    }
}
