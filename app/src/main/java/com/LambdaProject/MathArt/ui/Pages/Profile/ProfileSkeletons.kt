package com.LambdaProject.MathArt.ui.Pages.Profile

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.LambdaProject.MathArt.ui.Pages.Material.materialShimmer

/**
 * Loading Skeleton untuk ProfilePage (ProfileScreen)
 */
@Composable
fun ProfilePageSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header Skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Placeholder with Ring
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .materialShimmer(CircleShape)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Full Name Placeholder
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(24.dp)
                    .materialShimmer(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Email Placeholder
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(14.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Coins Badge Placeholder
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(32.dp)
                    .materialShimmer(RoundedCornerShape(20.dp))
            )
        }

        // Profile Body Skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Study Duration Hero Card Skeleton
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .materialShimmer(RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(11.dp)
                                    .materialShimmer(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(24.dp)
                                    .materialShimmer(RoundedCornerShape(6.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .materialShimmer(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .materialShimmer(RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section Header: Materi Aktif
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(18.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x Material Tracker Skeleton Cards
            repeat(2) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(16.dp)
                                    .materialShimmer(RoundedCornerShape(4.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(22.dp)
                                    .materialShimmer(RoundedCornerShape(8.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .materialShimmer(CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Header: Pencapaian
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(18.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Achievements Row Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                repeat(3) {
                    Surface(
                        modifier = Modifier
                            .width(88.dp)
                            .height(110.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .materialShimmer(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(11.dp)
                                    .materialShimmer(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section Header: Menu & Pengaturan
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(18.dp)
                    .materialShimmer(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Grouped Menu Card Skeleton
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    repeat(3) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .materialShimmer(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(14.dp)
                                        .materialShimmer(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(11.dp)
                                        .materialShimmer(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                        if (index < 2) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFF8FAFC))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Logout Button Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .materialShimmer(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

/**
 * Loading Skeleton untuk LearningReportPage (LearningReportScreen)
 */
@Composable
fun LearningReportSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FE))
            .verticalScroll(rememberScrollState())
    ) {
        // Study Duration Hero Card Skeleton
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(12.dp)
                                .materialShimmer(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(28.dp)
                                .materialShimmer(RoundedCornerShape(6.dp))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .materialShimmer(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(12.dp)
                            .materialShimmer(RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(12.dp)
                            .materialShimmer(RoundedCornerShape(4.dp))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .materialShimmer(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(11.dp)
                        .materialShimmer(RoundedCornerShape(4.dp))
                )
            }
        }

        // Stats Overview Card Skeleton
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(16.dp)
                        .materialShimmer(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // First Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatItemBoxSkeleton(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    StatItemBoxSkeleton(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Second Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatItemBoxSkeleton(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    StatItemBoxSkeleton(modifier = Modifier.weight(1f))
                }
            }
        }

        // Section Title: Progres Materi
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .width(200.dp)
                .height(18.dp)
                .materialShimmer(RoundedCornerShape(4.dp))
        )

        // 3x Material Trackers
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            repeat(3) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(16.dp)
                                    .materialShimmer(RoundedCornerShape(4.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(20.dp)
                                    .materialShimmer(RoundedCornerShape(8.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .materialShimmer(CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatItemBoxSkeleton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF8F9FE),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .materialShimmer(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(16.dp)
                        .materialShimmer(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(10.dp)
                        .materialShimmer(RoundedCornerShape(3.dp))
                )
            }
        }
    }
}
