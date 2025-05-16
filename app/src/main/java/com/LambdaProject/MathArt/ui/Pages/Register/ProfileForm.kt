package com.LambdaProject.MathArt.ui.Pages.Register

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.LambdaProject.MathArt.Data.*
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
    var isSubmitted by remember { mutableStateOf(false) }
    var expandedJenjang by remember { mutableStateOf(false) }
    var expandedKelas by remember { mutableStateOf(false) }

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
            .consumeWindowInsets(WindowInsets.ime)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(Color(0xFF3490DE))
            )
        }

        IconButton(
            onClick = {
                navController.navigate("register") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .padding(start = 14.dp, top = 21.dp)
                .align(Alignment.TopStart)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali ke Dashboard",
                tint = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = "Tinggal selangkah lagi!",
                fontFamily = interFontFamily,
                fontSize = 35.sp,
                lineHeight = 40.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hanya masukan namamu, jenjang, dan kelas sekarang. Kami akan menyajikan konten yang sesuai dengan preferensimu.",
                fontFamily = interFontFamily,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nama Lengkapmu", fontFamily = interFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedJenjang,
                            onExpandedChange = {
                                expandedJenjang = !expandedJenjang
                            },
                        ) {
                            OutlinedTextField(
                                value = grade,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jenjang", fontFamily = interFontFamily, fontSize = 12.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJenjang) },
                                modifier = Modifier
                                    .width(135.dp)
                                    .menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2196F3),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedJenjang,
                                onDismissRequest = {
                                    expandedJenjang = false
                                },
                                modifier = Modifier
                                    .background(Color.White)
                            ) {
                                grades.forEach { selectedGrade ->
                                    DropdownMenuItem(
                                        text = { Text(selectedGrade) },
                                        onClick = {
                                            grade = selectedGrade
                                            expandedJenjang = false
                                        }
                                    )
                                }
                            }
                        }

                        val kelasOptions = kelasMap[grade] ?: emptyList()

                        ExposedDropdownMenuBox(
                            expanded = expandedKelas,
                            onExpandedChange = { expandedKelas = !expandedKelas }
                        ) {
                            OutlinedTextField(
                                value = kelas,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kelas", fontFamily = interFontFamily, fontSize = 12.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelas) },
                                enabled = grade.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2196F3),
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedKelas,
                                onDismissRequest = { expandedKelas = false },
                                modifier = Modifier
                                    .background(Color.White)
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

                    Spacer(modifier = Modifier.height(8.dp))

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
                        enabled = allValid && registerState !is RegisterViewModel.RegisterState.Loading && !isSubmitted,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        if (registerState is RegisterViewModel.RegisterState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Daftar!",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}