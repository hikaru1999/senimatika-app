package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.utils.AvatarFrameHelper
import com.LambdaProject.MathArt.utils.AvatarHelper
import com.LambdaProject.MathArt.utils.UserAvatarWithFrame

@Composable
fun AvatarPickerModal(
    currentAvatar: String,
    currentFrame: String = "",
    unlockedFrames: List<String> = emptyList(),
    onAvatarSelected: (String) -> Unit,
    onFrameSelected: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pilih Avatar, 1: Pilih Bingkai
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }
    var selectedFrame by remember { mutableStateOf(currentFrame) }
    var isSaving by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedTab == 0) "Pilih Avatar" else "Pilih Bingkai",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (selectedTab == 0) "Pilih karakter yang mewakili gayamu" else "Pasang bingkai kosmetik pada avatarmu",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs: Avatar vs Bingkai
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == 0) Color.White else Color.Transparent,
                        shadowElevation = if (selectedTab == 0) 2.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Pilih Avatar",
                                fontFamily = interFontFamily,
                                fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = if (selectedTab == 0) Color(0xFF0F172A) else Color(0xFF64748B)
                            )
                        }
                    }

                    Surface(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == 1) Color.White else Color.Transparent,
                        shadowElevation = if (selectedTab == 1) 2.dp else 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Pilih Bingkai",
                                fontFamily = interFontFamily,
                                fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = if (selectedTab == 1) Color(0xFF0F172A) else Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Top Live Combined Preview Card (Avatar + Frame)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(68.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatarWithFrame(
                                avatarName = selectedAvatar,
                                frameId = selectedFrame,
                                avatarSize = 58.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            val activeFrameConfig = AvatarFrameHelper.getFrameConfig(selectedFrame)
                            Text(
                                text = if (selectedTab == 0) "Avatar Terpilih" else "Bingkai Terpilih",
                                fontSize = 11.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3B82F6)
                            )
                            Text(
                                text = if (selectedTab == 0) {
                                    selectedAvatar.replace("_", " ").replaceFirstChar { it.uppercase() }
                                } else {
                                    activeFrameConfig?.name ?: "Tanpa Bingkai"
                                },
                                fontSize = 15.sp,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            if (selectedTab == 1 && activeFrameConfig != null) {
                                Text(
                                    text = "Terbuka di Level ${activeFrameConfig.requiredLevel}",
                                    fontSize = 10.5.sp,
                                    fontFamily = interFontFamily,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content Tabs
                when (selectedTab) {
                    0 -> {
                        // TAB 0: 4 Columns x 8 Rows = 32 Avatars Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(AvatarHelper.AVATAR_LIST) { avatarName ->
                                val isSelected = (avatarName == selectedAvatar)
                                val avatarRes = AvatarHelper.getAvatarDrawableRes(avatarName)

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                                        )
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            selectedAvatar = avatarName
                                        }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = avatarRes),
                                        contentDescription = avatarName,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Checkmark Badge for Selected Avatar
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(18.dp)
                                                .background(Color(0xFF2563EB), CircleShape)
                                                .border(1.5.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Dipilih",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Save Avatar Action Button
                        Button(
                            onClick = {
                                isSaving = true
                                onAvatarSelected(selectedAvatar)
                            },
                            enabled = !isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                disabledContainerColor = Color(0xFF94A3B8)
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Simpan Avatar",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFontFamily,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: Frame Selection List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Option: Tanpa Bingkai (None)
                            item {
                                val isNoneActive = selectedFrame.isBlank() || selectedFrame == "none"
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isNoneActive) Color(0xFFEFF6FF) else Color.White,
                                    border = BorderStroke(1.dp, if (isNoneActive) Color(0xFF3B82F6) else Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFF1F5F9),
                                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                modifier = Modifier.size(46.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Block,
                                                        contentDescription = null,
                                                        tint = Color(0xFF94A3B8),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = "Tanpa Bingkai",
                                                    fontFamily = interFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = "Gunakan avatar dasar tanpa hiasan bingkai",
                                                    fontFamily = interFontFamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        if (isNoneActive) {
                                            Surface(
                                                color = Color(0xFF3B82F6),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Aktif",
                                                    color = Color.White,
                                                    fontFamily = interFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedFrame = ""
                                                    onFrameSelected("")
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Pakai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // 4 Frame Options
                            items(AvatarFrameHelper.FRAME_LIST, key = { it.id }) { frameConfig ->
                                val isUnlocked = unlockedFrames.contains(frameConfig.id)
                                val isActive = selectedFrame == frameConfig.id

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isActive) Color(0xFFEFF6FF) else Color.White,
                                    border = BorderStroke(1.dp, if (isActive) Color(0xFF3B82F6) else Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Live preview with currently selected avatar
                                            Box(
                                                modifier = Modifier.size(52.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                UserAvatarWithFrame(
                                                    avatarName = selectedAvatar,
                                                    frameId = frameConfig.id,
                                                    avatarSize = 44.dp
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = frameConfig.name,
                                                        fontFamily = interFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp,
                                                        color = if (isUnlocked) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                                    )
                                                    Surface(
                                                        color = if (isUnlocked) Color(0xFFEEF2FF) else Color(0xFFF1F5F9),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "LVL ${frameConfig.requiredLevel}",
                                                            fontFamily = interFontFamily,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 9.sp,
                                                            color = if (isUnlocked) Color(0xFF4F46E5) else Color(0xFF94A3B8),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (isUnlocked) frameConfig.description else "Buka di Level ${frameConfig.requiredLevel} (Klaim di Jalur Level)",
                                                    fontFamily = interFontFamily,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        if (isActive) {
                                            Surface(
                                                color = Color(0xFF3B82F6),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Aktif",
                                                    color = Color.White,
                                                    fontFamily = interFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else if (isUnlocked) {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedFrame = frameConfig.id
                                                    onFrameSelected(frameConfig.id)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Pakai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Terkunci",
                                                tint = Color(0xFFCBD5E1),
                                                modifier = Modifier.size(18.dp)
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
    }
}
