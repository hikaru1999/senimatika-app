package com.LambdaProject.MathArt.ui.Screen

import com.LambdaProject.MathArt.Data.registerUser
import com.LambdaProject.MathArt.Data.checkPasswordStrength
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.interFontFamily

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordStrength by remember { mutableStateOf(0) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Yuk Daftar!",
            fontFamily = interFontFamily,
            fontSize = 26.sp,
            color = Color(0xFF053892),
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registrasi terlebih dahulu untuk melihat konten pembelajaran!",
            fontSize = 14.sp,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF444444))

        OutlinedTextField(
            value = username,
            onValueChange = {
                if (it.text.length <= 15 && !it.text.contains(" ")) {
                    username = it
                }
            },
            label = {
                Text("Masukkan username", fontFamily = interFontFamily, fontWeight = FontWeight.Normal)
            },
            isError = isSubmitted && (username.text.isEmpty() || username.text.length > 15 || username.text.contains(" ")),
            modifier = Modifier
                .fillMaxWidth(),
            supportingText = {
                when {
                    isSubmitted && username.text.isEmpty() ->
                        Text("Username tidak boleh kosong",
                            fontFamily = interFontFamily,
                            color = Color.Red,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp)
                    username.text.length > 15 ->
                        Text("Username tidak boleh lebih dari 15 karakter",
                            fontFamily = interFontFamily,
                            color = Color.Red,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp)
                    username.text.contains(" ") ->
                        Text("Username tidak boleh mengandung spasi",
                            fontFamily = interFontFamily,
                            color = Color.Red,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                }
            },
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.copy(text = it.text.replace(Regex("\\s"), "")) },
            label = {
                Text("Email", fontFamily = interFontFamily, fontWeight = FontWeight.Normal) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            isError = emailError,
            modifier = Modifier
                .fillMaxWidth(),
            supportingText = {
                if (emailError) {
                    Text("Email tidak boleh kosong",
                        fontFamily = interFontFamily,
                        color = Color.Red,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp)
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column {
                Text("Ketentuan Password:",
                    fontSize = 12.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Normal,
                    color = Color(0xFF1E88E5)
                )
                Text("- Password minimal terdiri dari 8 karakter dan maksimal 25 karakter",
                    fontSize = 12.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray
                )
                Text("- Password harus memuat kombinasi simbol, angka, dan huruf kapital",
                    fontSize = 12.sp,
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it.copy(text = it.text.replace(Regex("\\s"), ""))
                passwordStrength = checkPasswordStrength(it.text)
            },
            label = { Text("Masukkan Password", fontFamily = interFontFamily, fontWeight = FontWeight.Normal) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                val image = if (passwordVisible) painterResource(id = R.drawable.ic_eye_open)
                else painterResource(id = R.drawable.ic_eye_close)
                IconButton(onClick = { passwordVisible = !passwordVisible}) {
                    Icon(painter = image, contentDescription = "Toggle Password Visibility", modifier = Modifier.size(20.dp))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = when {
                isSubmitted && password.text.isEmpty() -> true
                password.text.length > 20 -> true
                password.text.isNotEmpty() && passwordStrength == 0 -> true
                else -> false
            },
            supportingText = {
                when {
                    isSubmitted && password.text.isEmpty() ->
                        Text(text = "Password tidak boleh kosong",
                            fontFamily = interFontFamily,
                            color = Color.Red,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp)
                    password.text.length > 20 ->
                        Text("Password tidak boleh lebih dari 20 karakter",
                            fontFamily = interFontFamily,
                            color = Color.Red,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp)
                    password.text.isNotEmpty() && passwordStrength == 0 ->
                        Text("Password terlalu lemah",
                            fontFamily = interFontFamily,
                            color = Color.Red,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = when {
                    password.text.isEmpty() -> Color.Blue
                    passwordStrength == - 1 -> Color.Red
                    else -> Color.Blue
                },
                unfocusedBorderColor = when {
                    password.text.isEmpty() -> Color.Gray
                    passwordStrength == - 1 -> Color.Red
                    else -> Color.Gray
                }
            )
        )

        if(password.text.isNotEmpty() && passwordStrength != - 1) {
            PasswordStrengthIndicator(passwordStrength)
        }

        OutlinedTextField(value = confirmPassword, onValueChange = {
            confirmPassword = it.copy(text = it.text.replace(Regex("\\s"), ""))
            confirmPasswordError = isSubmitted && it.text.isEmpty() || it.text != password.text
        },
            label = { Text("Masukkan Ulang Password", fontFamily = interFontFamily, fontWeight = FontWeight.Normal) },
            isError = confirmPasswordError,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            trailingIcon = {
                val image = if (confirmPasswordVisible) painterResource(id = R.drawable.ic_eye_open)
                else painterResource(id = R.drawable.ic_eye_close)
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible}) {
                    Icon(painter = image, contentDescription = "Toggle Password Visibility", modifier = Modifier.size(20.dp))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                when {
                    isSubmitted && confirmPassword.text.isEmpty()
                        -> Text(text = "Konfirmasi password tidak boleh kosong",
                        fontFamily = interFontFamily,
                        color = Color.Red,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp)
                    confirmPassword.text != password.text ->
                        Text(text = "Password tidak sesuai",
                            fontFamily = interFontFamily,
                            color = Color.Red,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp)
                }
            }
        )

        Button(
            onClick = {
                isSubmitted = true
                usernameError = username.text.isEmpty() || username.text.length > 15 || username.text.contains(" ")
                emailError = email.text.isEmpty()
                passwordError = password.text.isEmpty()
                confirmPasswordError = confirmPassword.text.isEmpty() || confirmPassword.text != password.text

                if (usernameError || emailError || passwordError || confirmPasswordError) {
                    return@Button
                }

                isRegistering = true
                registerUser(
                    context = context,
                    auth = auth,
                    db = db,
                    email = email.text,
                    password = password.text,
                    username = username.text,
                    confirmPasswordError = confirmPasswordError,
                    usernameError = usernameError,
                    passwordStrength = passwordStrength
                ) { message ->
                    successMessage = message
                    isRegistering = false
                    navController.navigate("login?message=Selamat akunmu telah dibuat. Yuk coba login!")
                }},

            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        ) {
            if (isRegistering) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(text = "Daftar!", fontFamily = interFontFamily, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal, fontSize = 15.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(text = "Sudah punya akun? ",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Normal,
                color = Color.DarkGray,
                fontSize = 14.sp)

            Text(
                text = "Log In",
                color = Color(0xFF1E88E5),
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navController.navigate("login")
                }
            )
        }
    }
}

@Composable
fun PasswordStrengthIndicator(strength: Int) {
    val strengthColor by animateColorAsState(
        targetValue = when (strength) {
            0 -> Color(0xFFED0012)
            1 -> Color(0xFFF5CC1C)
            else -> Color(0xFF00B926)
        },
        label = "strengthColor"
    )
    val progress by animateFloatAsState(
        targetValue = when (strength) {
            0 -> 0.3f
            1 -> 0.6f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = strengthColor,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("Kekuatan Password: ")
                }
                withStyle(style = SpanStyle(color = strengthColor)) {
                    append(
                        when (strength) {
                            0 -> "Lemah"
                            1 -> "Sedang"
                            else -> "Kuat"
                        }
                    )
                }
            },
            fontSize = 12.sp
        )
    }
}