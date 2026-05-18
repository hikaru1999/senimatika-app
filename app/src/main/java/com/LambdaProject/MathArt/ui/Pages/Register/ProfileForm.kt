package com.LambdaProject.MathArt.ui.Pages.Register

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.data.*
import com.LambdaProject.MathArt.ViewModels.RegisterViewModel
import com.LambdaProject.MathArt.interFontFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileForm(
    navController: NavController,
    username: String,
    email: String,
    password: String,
    registerView: RegisterViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    var fullName by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var kelas by remember { mutableStateOf("") }
    var expandedJenjang by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    val registerState by registerView.registerState
    val fullnameValid = fullName.isNotBlank()
    val gradeValid = grade.isNotEmpty()
    val kelasValid = kelas.isNotEmpty()
    val allValid = fullnameValid && gradeValid && kelasValid

    LaunchedEffect(registerState) {
        if (registerState is RegisterViewModel.RegisterState.Success) {
            delay(2000)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                navController.navigate("success/${registerView.username}") {
                    popUpTo("ProfileForm") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3490DE), Color(0xFF1A237E))
                )
            )
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-120).dp, y = (-80).dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 120.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tinggal selangkah lagi!",
                fontSize = 32.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Lengkapi profilmu untuk mendapatkan materi yang paling sesuai dengan jenjang sekolahmu.",
                fontSize = 14.sp,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp)),
                color = Color.White,
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Full Name Field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nama Lengkap", fontFamily = interFontFamily, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFF8F9FE),
                            unfocusedContainerColor = Color(0xFFF8F9FE)
                        )
                    )

                    // Jenjang Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedJenjang,
                        onExpandedChange = { expandedJenjang = !expandedJenjang },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = grade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jenjang Sekolah", fontFamily = interFontFamily, fontSize = 14.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJenjang) },
                            leadingIcon = { Icon(Icons.Default.School, null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp)) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1976D2),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color(0xFFF8F9FE),
                                unfocusedContainerColor = Color(0xFFF8F9FE)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedJenjang,
                            onDismissRequest = { expandedJenjang = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            grades.forEach { selectedGrade ->
                                DropdownMenuItem(
                                    text = { Text(selectedGrade, fontFamily = interFontFamily) },
                                    onClick = {
                                        grade = selectedGrade
                                        expandedJenjang = false
                                        kelas = "" // Reset kelas
                                    }
                                )
                            }
                        }
                    }

                    // Kelas Dropdown
                    val kelasOptions = kelasMap[grade] ?: emptyList()
                    ExposedDropdownMenuBox(
                        expanded = expandedKelas,
                        onExpandedChange = { if(grade.isNotEmpty()) expandedKelas = !expandedKelas },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = kelas,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kelas", fontFamily = interFontFamily, fontSize = 14.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                            enabled = grade.isNotEmpty(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1976D2),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color(0xFFF8F9FE),
                                unfocusedContainerColor = Color(0xFFF8F9FE),
                                disabledBorderColor = Color(0xFFEEEEEE),
                                disabledContainerColor = Color(0xFFF5F5F5),
                                disabledLabelColor = Color.LightGray,
                                disabledTrailingIconColor = Color.LightGray
                            )
                        )
                        if (kelasOptions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expandedKelas,
                                onDismissRequest = { expandedKelas = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                kelasOptions.forEach { selectedKelas ->
                                    DropdownMenuItem(
                                        text = { Text(selectedKelas, fontFamily = interFontFamily) },
                                        onClick = {
                                            kelas = selectedKelas
                                            expandedKelas = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isSubmitted = true
                            if(allValid) {
                                registerView.registerUser(
                                    fullName = fullName,
                                    grade = grade,
                                    kelas = kelas
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 2.dp,
                            disabledElevation = 0.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            /* disabledContainerColor = Color(0xFFE0E0E0),*/
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = allValid && registerState !is RegisterViewModel.RegisterState.Loading && !isSubmitted
                    ) {
                        if (registerState is RegisterViewModel.RegisterState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        } else {
                            Text(
                                text = "SELESAIKAN PENDAFTARAN",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
