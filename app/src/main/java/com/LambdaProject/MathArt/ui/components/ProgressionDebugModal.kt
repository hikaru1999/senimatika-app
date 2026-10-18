package com.LambdaProject.MathArt.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.data.model.*
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.utils.ProgressionDebugManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProgressionDebugModal(
    currentExp: Long,
    currentLevel: Int,
    activeTitle: String,
    activeEmblem: String,
    claimedRewardsCount: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("⚡ EXP & Level", "🎁 Hadiah", "🔄 Multiplier", "🔔 Notifikasi", "👑 Tag")

    var customExpText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f)
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A), // Modern Dev Dark Theme
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header with Master Debug Switch & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6366F1).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF818CF8))
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Progression Debug Suite",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Uji Coba Level, EXP, Multiplier, & Notifikasi",
                                fontFamily = interFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Master Toggle Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (ProgressionDebugManager.isDebugEnabled) Color(0xFF1E293B) else Color(0xFF1E1E24),
                    border = BorderStroke(
                        1.dp,
                        if (ProgressionDebugManager.isDebugEnabled) Color(0xFF10B981) else Color(0xFF475569)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (ProgressionDebugManager.isDebugEnabled) Color(0xFF10B981) else Color(0xFFEF4444),
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (ProgressionDebugManager.isDebugEnabled) "Debug Mode: AKTIF" else "Debug Mode: NONAKTIF",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (ProgressionDebugManager.isDebugEnabled) Color(0xFF34D399) else Color(0xFFF87171)
                            )
                        }

                        Switch(
                            checked = ProgressionDebugManager.isDebugEnabled,
                            onCheckedChange = { isEnabled ->
                                ProgressionDebugManager.isDebugEnabled = isEnabled
                                Toast.makeText(
                                    context,
                                    if (isEnabled) "Mode Debug Diaktifkan!" else "Mode Debug Dinonaktifkan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981),
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current User State HUD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LEVEL $currentLevel",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF60A5FA)
                            )
                            Text(
                                text = "$currentExp EXP Kumulatif",
                                fontFamily = interFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = activeTitle,
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = "Klaim: $claimedRewardsCount reward",
                                fontFamily = interFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF818CF8),
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontFamily = interFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) Color(0xFFA5B4FC) else Color(0xFF64748B)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Contents
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isProcessing) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF818CF8))
                        }
                    } else {
                        when (selectedTab) {
                            0 -> ExpAndLevelTab(
                                userId = userId,
                                currentExp = currentExp,
                                customExpText = customExpText,
                                onCustomExpChanged = { customExpText = it },
                                onProcessingChanged = { isProcessing = it }
                            )
                            1 -> RewardsTab(
                                userId = userId,
                                currentLevel = currentLevel,
                                onProcessingChanged = { isProcessing = it }
                            )
                            2 -> MultipliersTab(
                                userId = userId,
                                onProcessingChanged = { isProcessing = it }
                            )
                            3 -> NotificationsTab(
                                userId = userId,
                                currentLevel = currentLevel,
                                onProcessingChanged = { isProcessing = it }
                            )
                            4 -> TitlesAndEmblemsTab(
                                userId = userId,
                                onProcessingChanged = { isProcessing = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpAndLevelTab(
    userId: String,
    currentExp: Long,
    customExpText: String,
    onCustomExpChanged: (String) -> Unit,
    onProcessingChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Injeksi EXP Cepat (+grantExpToUser)",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100, 300, 500).forEach { amount ->
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onProcessingChanged(true)
                                val res = ProgressionDebugManager.addExp(userId, amount)
                                onProcessingChanged(false)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "+$amount EXP Ditambahkan!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+$amount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1000, 2500, 5000).forEach { amount ->
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onProcessingChanged(true)
                                val res = ProgressionDebugManager.addExp(userId, amount)
                                onProcessingChanged(false)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "+$amount EXP Ditambahkan!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+$amount", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Lompat Langsung ke Level Spesifik",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    1 to "Level 1: Novice (0 EXP)",
                    2 to "Level 2: Apprentice (300 EXP - Unlock Eksplorasi)",
                    5 to "Level 5: Thinker (1,800 EXP)",
                    10 to "Level 10: Expert (6,700 EXP)",
                    15 to "Level 15: The Myth & The Legend (17,500 EXP)"
                ).forEach { (lvl, label) ->
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                onProcessingChanged(true)
                                val res = ProgressionDebugManager.setLevel(userId, lvl)
                                onProcessingChanged(false)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Berhasil melompat ke Level $lvl", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF93C5FD))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Atur Saldo EXP Kustom",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customExpText,
                    onValueChange = onCustomExpChanged,
                    placeholder = { Text("cth: 3200", color = Color(0xFF64748B), fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )
                Button(
                    onClick = {
                        val parsedExp = customExpText.toLongOrNull() ?: return@Button
                        coroutineScope.launch {
                            onProcessingChanged(true)
                            val res = ProgressionDebugManager.setCustomExp(userId, parsedExp)
                            onProcessingChanged(false)
                            if (res.isSuccess) {
                                Toast.makeText(context, "EXP berhasil diatur ke $parsedExp", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Terapkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        onProcessingChanged(true)
                        val res = ProgressionDebugManager.resetProgression(userId)
                        onProcessingChanged(false)
                        if (res.isSuccess) {
                            Toast.makeText(context, "Progres di-reset ke Level 1 (0 EXP)", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Total ke Level 1 (0 EXP)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RewardsTab(
    userId: String,
    currentLevel: Int,
    onProcessingChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Pengujian Hadiah Level Road",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Reset Riwayat Klaim Hadiah",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFFCD34D)
                    )
                    Text(
                        text = "Mengosongkan daftar 'claimedLevelRewards' agar tombol klaim di LevelRoadModal dapat dites kembali dari awal.",
                        fontFamily = interFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onProcessingChanged(true)
                                val res = ProgressionDebugManager.resetClaimedRewards(userId)
                                onProcessingChanged(false)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Riwayat klaim hadiah berhasil dikosongkan!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reset Klaim Hadiah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Klaim Semua Hadiah Tersedia",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF34D399)
                    )
                    Text(
                        text = "Mengklaim seluruh hadiah dari Level 1 sampai Level $currentLevel secara serentak ke akun.",
                        fontFamily = interFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onProcessingChanged(true)
                                val res = ProgressionDebugManager.claimAllAvailableRewards(userId, currentLevel)
                                onProcessingChanged(false)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "${res.getOrNull()} Hadiah berhasil diklaim!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Klaim Semua (Level 1..$currentLevel)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Status Akses Mode Eksplorasi",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (currentLevel >= 2) Color(0xFF34D399) else Color(0xFFF87171)
                    )
                    Text(
                        text = if (currentLevel >= 2) "Terbuka (Level $currentLevel >= 2). ExplorationCard aktif tanpa overlay gembok." else "Terkunci (Level $currentLevel < 2). ExplorationCard menampilkan overlay gembok & required level 2.",
                        fontFamily = interFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun MultipliersTab(
    userId: String,
    onProcessingChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Simulasi Repeat-Play Multipliers",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0)
            )
            Text(
                text = "Pilih sesi pengulangan untuk menguji peluruhan EXP saat menyelesaikan Kuis Online atau Eksplorasi berulang kali.",
                fontFamily = interFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(10.dp))

            listOf(
                0 to "Sesi 1: 100% EXP (Multiplier 1.00x)",
                1 to "Sesi 2: 80% EXP (Multiplier 0.80x)",
                2 to "Sesi 3: 60% EXP (Multiplier 0.60x)",
                3 to "Sesi 4: 40% EXP (Multiplier 0.40x)",
                4 to "Sesi 5+: 30% EXP (Multiplier 0.30x)"
            ).forEach { (count, label) ->
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            onProcessingChanged(true)
                            val res = ProgressionDebugManager.setSimulatedPlayCount(userId, count)
                            onProcessingChanged(false)
                            if (res.isSuccess) {
                                Toast.makeText(context, "Sesi disimulasikan: $label", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA5B4FC))
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        item {
            Button(
                onClick = {
                    coroutineScope.launch {
                        onProcessingChanged(true)
                        val res = ProgressionDebugManager.resetPlayCounts(userId)
                        onProcessingChanged(false)
                        if (res.isSuccess) {
                            Toast.makeText(context, "Riwayat sesi pengulangan di-reset ke Sesi 1 (1.0x)", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Riwayat Sesi (Semua ke 1.00x)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NotificationsTab(
    userId: String,
    currentLevel: Int,
    onProcessingChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Kirim Notifikasi Level Up Uji Coba",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0)
            )
            Text(
                text = "Menyuntikkan dokumen notifikasi kenaikan level ke subcollection users/{userId}/notifications untuk diverifikasi di NotificationScreen.",
                fontFamily = interFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(10.dp))

            listOf(2, 5, 10, 15).forEach { lvl ->
                val info = getLevelInfo(lvl)
                Button(
                    onClick = {
                        coroutineScope.launch {
                            onProcessingChanged(true)
                            val res = ProgressionDebugManager.sendTestLevelNotification(userId, lvl)
                            onProcessingChanged(false)
                            if (res.isSuccess) {
                                Toast.makeText(context, "Notifikasi Level Up Lvl $lvl Terkirim!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF6366F1))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notifikasi Level $lvl (${info.title})", fontSize = 12.sp, color = Color(0xFFE0E7FF))
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        onProcessingChanged(true)
                        val res = ProgressionDebugManager.clearNotifications(userId)
                        onProcessingChanged(false)
                        if (res.isSuccess) {
                            Toast.makeText(context, "Semua notifikasi dibersihkan (${res.getOrNull()} item)", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bersihkan Semua Notifikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TitlesAndEmblemsTab(
    userId: String,
    onProcessingChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Buka & Uji Coba Semua Tag & Emblem",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE2E8F0)
            )
            Text(
                text = "Membuka seluruh 15 Tag dan variasi emblem animasi WordArt agar dapat dipilih di Profil & Dashboard.",
                fontFamily = interFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        onProcessingChanged(true)
                        val res = ProgressionDebugManager.unlockAllTitlesAndEmblems(userId)
                        onProcessingChanged(false)
                        if (res.isSuccess) {
                            Toast.makeText(context, "Semua Tag & Emblem Terbuka!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buka Semua Tag & Emblem", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
        }

        item {
            Text(
                text = "Daftar Tag & Emblem Senimatika (15 Level)",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SENIMATIKA_LEVELS.forEach { info ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lvl ${info.level}: ${info.title}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        if (info.emblemName != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = "Emblem: ${info.emblemName}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFA5B4FC),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
