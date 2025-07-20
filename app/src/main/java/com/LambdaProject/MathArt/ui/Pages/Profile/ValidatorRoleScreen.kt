package com.LambdaProject.MathArt.ui.Pages.Profile

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.Data.infoSenimatika
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.ViewModels.ValidatorViewModel
import com.LambdaProject.MathArt.dataStore
import com.LambdaProject.MathArt.interFontFamily
import com.LambdaProject.MathArt.model.ValidatorRole
import com.LambdaProject.MathArt.model.ValidatorSession
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidatorRoleScreen(
    onSelectMediaExpert: () -> Unit,
    onSelectMaterialExpert: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var isLoadingNilai by remember { mutableStateOf(false) }
    var isLoadingMateri by remember { mutableStateOf(false) }
    var isLoadingMedia by remember { mutableStateOf(false) }
    var isLoadingConfirm by remember { mutableStateOf(false) }
    var isLoadingCont by remember { mutableStateOf(false) }

    val viewModel: ValidatorViewModel = hiltViewModel()
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val cachedSessions = viewModel.cachedSessions
    val context = LocalContext.current
    val submittedRole by viewModel.submittedRole.collectAsState()
    val showResetDialog = remember { mutableStateOf<ValidatorRole?>(null) }

    if (showResetDialog.value != null) {
        AlertDialog(
            onDismissRequest = { showResetDialog.value = null },
            title = {
                Text(
                    text = "Mulai Ulang Penilaian?",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Penilaian sebelumnya sebagai ahli ${showResetDialog.value!!.name.lowercase()} akan dihapus. Lanjutkan?",
                    fontFamily = interFontFamily
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            confirmButton = {
                val roleToReset = showResetDialog.value!!
                TextButton(
                    onClick = {
                        isLoadingConfirm = true
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(3000)
                            viewModel.clearCachedSession(userId, roleToReset)
                            showResetDialog.value = null

                            if (roleToReset == ValidatorRole.MATERI) {
                                onSelectMediaExpert()
                            } else {
                                onSelectMaterialExpert()
                            }
                            isLoadingConfirm = false
                        }
                    }
                ) {
                    if(isLoadingConfirm) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 4.dp
                        )
                    } else {
                        Text(
                            text = "Ya, Hapus & Lanjut",
                            fontFamily = interFontFamily,
                            color = Color.Black
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog.value = null }) {
                    Text(
                        text = "Batal",
                        fontFamily = interFontFamily,
                        color = Color.Gray
                    )
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        delay(300)
        viewModel.checkCachedSessions(context, userId)
    }

    LaunchedEffect(userId) {
        viewModel.checkSubmittedRole(userId)
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Beri Penilaian Aplikasi",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_thank_you),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Halo!\n"
                        + "Terima kasih sudah mencoba Senimatika sebagai partner belajar Anda. Demi meningkatkan kualitas aplikasi ini, Anda diundang untuk menilai aplikasi kami.\n\n"
                        + "Silakan pilih peran Anda dan mulai mengisi. Kami sangat menghargai masukan Anda.",
                textAlign = TextAlign.Justify,
                fontFamily = interFontFamily,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Saya akan menilai aplikasi sebagai?",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (submittedRole != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0XFFE8F9FE))
                        .padding(16.dp),
                ) {
                    Column {
                        Text(
                            text = "Anda telah memberikan penilaian terhadap aplikasi Senimatika. Segenap tim pengembang mengucapkan terima kasih telah mencoba dan memberikan penilaian. Respons Anda sangat penting bagi kami untuk menyempurnakan Senimatika.",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2BA2FF),
                            textAlign = TextAlign.Justify
                        )

                        Button(
                            onClick = {
                                isLoadingNilai = true
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(2000)
                                    viewModel.loadOwnKuesionerResult { role ->
                                        navController.navigate("validation_summary/${role.name.lowercase()}")
                                    }
                                    isLoadingNilai = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E60DD)),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .widthIn(min = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingNilai) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 4.dp
                                    )
                                } else {
                                    Text(
                                        text = "Lihat Penilaian",
                                        color = Color.White,
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column {
                    Button(
                        onClick = { selectedRole = "validator" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == "validator") Color(0xFF032D70) else Color(0xFF0E60DD)
                        ),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(text = "Validator",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xffFFFFFF)
                        )
                    }

                    if (selectedRole == "validator") {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Pilih validator:",
                            fontFamily = interFontFamily
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isLoadingMateri = true
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(1500)
                                        val hasOtherRoleCache = viewModel.cachedSessions.containsKey(ValidatorRole.MEDIA)
                                        if (hasOtherRoleCache) {
                                            showResetDialog.value = ValidatorRole.MEDIA
                                        } else if (viewModel.cachedSessions.containsKey(ValidatorRole.MATERI)) {
                                            showResetDialog.value = ValidatorRole.MATERI
                                        } else {
                                            onSelectMaterialExpert()
                                        }
                                        isLoadingMateri = false
                                    }
                                },
                                border = BorderStroke(1.dp, Color.Black),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isLoadingMateri) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 4.dp
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_content),
                                            contentDescription = "Content Assessment",
                                            modifier = Modifier.height(30.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Ahli Materi", fontFamily = interFontFamily)
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    isLoadingMedia = true
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(1500)
                                        val hasOtherRoleCache = viewModel.cachedSessions.containsKey(ValidatorRole.MATERI)
                                        if (hasOtherRoleCache) {
                                            showResetDialog.value = ValidatorRole.MATERI
                                        } else if (viewModel.cachedSessions.containsKey(ValidatorRole.MEDIA)) {
                                            showResetDialog.value = ValidatorRole.MEDIA
                                        } else {
                                            onSelectMediaExpert()
                                        }
                                        isLoadingMedia = false
                                    }
                                },
                                border = BorderStroke(1.dp, Color.Black),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isLoadingMedia) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 4.dp
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_technic),
                                            contentDescription = "Media Assessment",
                                            modifier = Modifier.height(30.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Ahli Media", fontFamily = interFontFamily)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        cachedSessions.forEach { (role, session) ->
                            Text(
                                text = "Progress pengisian terdeteksi:",
                                fontFamily = interFontFamily
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    isLoadingCont = true
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(2000)
                                        navController.navigate("kuesioner_screen/${role.name}")
                                        isLoadingCont = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(5.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)
                                ),
                            ) {
                                if (isLoadingCont) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 4.dp
                                    )
                                } else {
                                    Text(
                                        text = "Lanjutkan Penilaian Sebagai Ahli ${role.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                        fontFamily = interFontFamily
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { selectedRole = "student" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E60DD)),
                        shape = RoundedCornerShape(5.dp),
                        enabled = false,
                    ) {
                        Text(text = "Siswa/Pembelajar",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xffFFFFFF)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}