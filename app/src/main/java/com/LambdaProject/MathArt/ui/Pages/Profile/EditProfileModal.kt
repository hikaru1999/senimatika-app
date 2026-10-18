package com.LambdaProject.MathArt.ui.Pages.Profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.grades
import com.LambdaProject.MathArt.data.kelasMap
import com.LambdaProject.MathArt.interFontFamily

private enum class EditProfileTab(val label: String) {
    PROFILE("Data Diri"),
    EMAIL("Email"),
    PASSWORD("Kata Sandi")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileModal(
    currentFullName: String,
    currentGrade: String,
    currentKelas: String,
    currentEmail: String,
    isEmailVerified: Boolean,
    onSaveProfile: (fullName: String, grade: String, kelas: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onUpdateEmail: (newEmail: String, currentPassword: String, keepLoggedIn: Boolean, onResult: (Boolean, String) -> Unit) -> Unit,
    onUpdatePassword: (currentPassword: String, newPassword: String, keepLoggedIn: Boolean, onResult: (Boolean, String) -> Unit) -> Unit,
    onOpenEmailVerification: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(EditProfileTab.PROFILE) }

    // Form State: Data Diri
    var fullNameInput by remember { mutableStateOf(currentFullName) }
    var selectedGrade by remember { mutableStateOf(currentGrade.ifEmpty { "SMA" }) }
    var selectedKelas by remember { mutableStateOf(currentKelas.ifEmpty { "11" }) }
    var expandedGradeDropdown by remember { mutableStateOf(false) }
    var expandedKelasDropdown by remember { mutableStateOf(false) }

    // Form State: Email
    var displayedEmail by remember(currentEmail) { mutableStateOf(currentEmail) }
    var newEmailInput by remember { mutableStateOf("") }
    var emailCurrentPasswordInput by remember { mutableStateOf("") }
    var showEmailPassword by remember { mutableStateOf(false) }
    var emailUpdateSuccess by remember { mutableStateOf(false) }
    var keepLoggedInEmail by remember { mutableStateOf(true) }

    // Form State: Password
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var keepLoggedInPassword by remember { mutableStateOf(true) }

    // Status / Feedback State
    var isSubmitting by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }

    fun clearFeedback() {
        feedbackMessage = null
        isFeedbackError = false
    }

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
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(28.dp)),
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
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
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ManageAccounts,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Edit Profil & Akun",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Kelola identitas & keamanan akun",
                                fontFamily = interFontFamily,
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Tabs
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EditProfileTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedTab = tab
                                        clearFeedback()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color.White else Color.Transparent,
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab.label,
                                        fontFamily = interFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inline Feedback Alert (Error / Success)
                AnimatedVisibility(visible = feedbackMessage != null) {
                    feedbackMessage?.let { msg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFeedbackError) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                            border = BorderStroke(
                                1.dp,
                                if (isFeedbackError) Color(0xFFFECACA) else Color(0xFFBBF7D0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFeedbackError) Icons.Default.Info else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isFeedbackError) Color(0xFFDC2626) else Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    fontFamily = interFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isFeedbackError) Color(0xFF991B1B) else Color(0xFF166534),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        EditProfileTab.PROFILE -> {
                            // TAB 1: DATA DIRI
                            Text(
                                text = "Informasi Pribadi",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Perubahan nama dan kelas akan langsung disinkronkan ke laporan belajar.",
                                fontFamily = interFontFamily,
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nama Lengkap
                            OutlinedTextField(
                                value = fullNameInput,
                                onValueChange = {
                                    fullNameInput = it
                                    clearFeedback()
                                },
                                label = { Text("Nama Lengkap", fontFamily = interFontFamily, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dropdown Jenjang Sekolah
                            ExposedDropdownMenuBox(
                                expanded = expandedGradeDropdown,
                                onExpandedChange = { expandedGradeDropdown = !expandedGradeDropdown },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedGrade,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Jenjang Sekolah", fontFamily = interFontFamily, fontSize = 13.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGradeDropdown) },
                                    leadingIcon = {
                                        Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2563EB),
                                        unfocusedBorderColor = Color(0xFFE2E8F0),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedGradeDropdown,
                                    onDismissRequest = { expandedGradeDropdown = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    grades.forEach { gradeOption ->
                                        DropdownMenuItem(
                                            text = { Text(gradeOption, fontFamily = interFontFamily, fontSize = 13.sp) },
                                            onClick = {
                                                selectedGrade = gradeOption
                                                expandedGradeDropdown = false
                                                val available = kelasMap[gradeOption] ?: emptyList()
                                                if (!available.contains(selectedKelas)) {
                                                    selectedKelas = available.firstOrNull() ?: ""
                                                }
                                                clearFeedback()
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dropdown Kelas
                            val kelasOptions = kelasMap[selectedGrade] ?: emptyList()
                            ExposedDropdownMenuBox(
                                expanded = expandedKelasDropdown,
                                onExpandedChange = { expandedKelasDropdown = !expandedKelasDropdown },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = if (selectedKelas.isNotEmpty()) "Kelas $selectedKelas" else "Pilih Kelas",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Kelas", fontFamily = interFontFamily, fontSize = 13.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKelasDropdown) },
                                    leadingIcon = {
                                        Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2563EB),
                                        unfocusedBorderColor = Color(0xFFE2E8F0),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC)
                                    )
                                )
                                if (kelasOptions.isNotEmpty()) {
                                    ExposedDropdownMenu(
                                        expanded = expandedKelasDropdown,
                                        onDismissRequest = { expandedKelasDropdown = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        kelasOptions.forEach { kelasOpt ->
                                            DropdownMenuItem(
                                                text = { Text("Kelas $kelasOpt", fontFamily = interFontFamily, fontSize = 13.sp) },
                                                onClick = {
                                                    selectedKelas = kelasOpt
                                                    expandedKelasDropdown = false
                                                    clearFeedback()
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Button Simpan Profil
                            Button(
                                onClick = {
                                    if (fullNameInput.isBlank()) {
                                        feedbackMessage = "Nama lengkap tidak boleh kosong."
                                        isFeedbackError = true
                                        return@Button
                                    }
                                    isSubmitting = true
                                    clearFeedback()
                                    onSaveProfile(fullNameInput.trim(), selectedGrade, selectedKelas) { success, msg ->
                                        isSubmitting = false
                                        feedbackMessage = msg
                                        isFeedbackError = !success
                                    }
                                },
                                enabled = !isSubmitting && fullNameInput.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    disabledContainerColor = Color(0xFF2563EB).copy(alpha = 0.85f),
                                    disabledContentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, trackColor = Color.Transparent, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simpan Perubahan Profil", fontFamily = interFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        EditProfileTab.EMAIL -> {
                            // TAB 2: EMAIL
                            Text(
                                text = "Ganti Alamat Email",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Email baru akan memerlukan verifikasi OTP untuk menjaga keamanan akun.",
                                fontFamily = interFontFamily,
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Email Saat Ini Info Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Email Aktif Saat Ini",
                                            fontFamily = interFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = displayedEmail,
                                            fontFamily = interFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isEmailVerified) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = if (isEmailVerified) "Terverifikasi" else "Belum Verifikasi",
                                            fontFamily = interFontFamily,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEmailVerified) Color(0xFF15803D) else Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Input Email Baru
                            OutlinedTextField(
                                value = newEmailInput,
                                onValueChange = {
                                    newEmailInput = it
                                    clearFeedback()
                                },
                                label = { Text("Alamat Email Baru", fontFamily = interFontFamily, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Input Kata Sandi Konfirmasi
                            OutlinedTextField(
                                value = emailCurrentPasswordInput,
                                onValueChange = {
                                    emailCurrentPasswordInput = it
                                    clearFeedback()
                                },
                                label = { Text("Kata Sandi Saat Ini (Konfirmasi)", fontFamily = interFontFamily, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showEmailPassword = !showEmailPassword }) {
                                        Icon(
                                            imageVector = if (showEmailPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (showEmailPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Toggle Tetap Masuk (Keep Logged In)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { keepLoggedInEmail = !keepLoggedInEmail },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Pertahankan status login",
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = if (keepLoggedInEmail) "Sesi diperbarui otomatis tanpa perlu login ulang" else "Anda akan logout dan diminta login ulang demi keamanan",
                                            fontFamily = interFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = keepLoggedInEmail,
                                        onCheckedChange = { keepLoggedInEmail = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF2563EB)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Button Ganti Email
                            Button(
                                onClick = {
                                    if (newEmailInput.isBlank()) {
                                        feedbackMessage = "Alamat email baru tidak boleh kosong."
                                        isFeedbackError = true
                                        return@Button
                                    }
                                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmailInput.trim()).matches()) {
                                        feedbackMessage = "Format email baru tidak valid."
                                        isFeedbackError = true
                                        return@Button
                                    }
                                    if (emailCurrentPasswordInput.isBlank()) {
                                        feedbackMessage = "Masukkan kata sandi saat ini untuk keamanan akun."
                                        isFeedbackError = true
                                        return@Button
                                    }

                                    isSubmitting = true
                                    clearFeedback()
                                    onUpdateEmail(newEmailInput.trim(), emailCurrentPasswordInput, keepLoggedInEmail) { success, msg ->
                                        isSubmitting = false
                                        feedbackMessage = msg
                                        isFeedbackError = !success
                                        if (success) {
                                            emailUpdateSuccess = true
                                            displayedEmail = newEmailInput.trim().lowercase()
                                            newEmailInput = ""
                                            emailCurrentPasswordInput = ""
                                        }
                                    }
                                },
                                enabled = !isSubmitting && newEmailInput.isNotBlank() && emailCurrentPasswordInput.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    disabledContainerColor = Color(0xFF2563EB).copy(alpha = 0.85f),
                                    disabledContentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, trackColor = Color.Transparent, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Perbarui Email & Kirim OTP", fontFamily = interFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            // Shortcut to Open Verification Modal if updated
                            if (emailUpdateSuccess) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = onOpenEmailVerification,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Color(0xFF16A34A)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A))
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verifikasi Email Sekarang", fontFamily = interFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        EditProfileTab.PASSWORD -> {
                            // TAB 3: PASSWORD
                            Text(
                                text = "Ganti Kata Sandi",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gunakan kombinasi minimal 6 karakter untuk menjaga keamanan akun Anda.",
                                fontFamily = interFontFamily,
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Password Lama
                            OutlinedTextField(
                                value = currentPasswordInput,
                                onValueChange = {
                                    currentPasswordInput = it
                                    clearFeedback()
                                },
                                label = { Text("Kata Sandi Saat Ini", fontFamily = interFontFamily, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                        Icon(
                                            imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Password Baru
                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = {
                                    newPasswordInput = it
                                    clearFeedback()
                                },
                                label = { Text("Kata Sandi Baru (Min. 6 Karakter)", fontFamily = interFontFamily, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                        Icon(
                                            imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Konfirmasi Password Baru
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    clearFeedback()
                                },
                                label = { Text("Ulangi Kata Sandi Baru", fontFamily = interFontFamily, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                        Icon(
                                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Toggle Tetap Masuk (Keep Logged In)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { keepLoggedInPassword = !keepLoggedInPassword },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Pertahankan status login",
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = if (keepLoggedInPassword) "Sesi diperbarui otomatis tanpa perlu login ulang" else "Anda akan logout dan diminta login ulang demi keamanan",
                                            fontFamily = interFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = keepLoggedInPassword,
                                        onCheckedChange = { keepLoggedInPassword = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF2563EB)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Button Simpan Password Baru
                            Button(
                                onClick = {
                                    if (currentPasswordInput.isBlank()) {
                                        feedbackMessage = "Kata sandi saat ini harus diisi."
                                        isFeedbackError = true
                                        return@Button
                                    }
                                    if (newPasswordInput.length < 6) {
                                        feedbackMessage = "Kata sandi baru minimal harus 6 karakter."
                                        isFeedbackError = true
                                        return@Button
                                    }
                                    if (newPasswordInput != confirmPasswordInput) {
                                        feedbackMessage = "Konfirmasi kata sandi baru tidak cocok."
                                        isFeedbackError = true
                                        return@Button
                                    }
                                    if (currentPasswordInput == newPasswordInput) {
                                        feedbackMessage = "Kata sandi baru tidak boleh sama dengan kata sandi lama."
                                        isFeedbackError = true
                                        return@Button
                                    }

                                    isSubmitting = true
                                    clearFeedback()
                                    onUpdatePassword(currentPasswordInput, newPasswordInput, keepLoggedInPassword) { success, msg ->
                                        isSubmitting = false
                                        feedbackMessage = msg
                                        isFeedbackError = !success
                                        if (success) {
                                            currentPasswordInput = ""
                                            newPasswordInput = ""
                                            confirmPasswordInput = ""
                                        }
                                    }
                                },
                                enabled = !isSubmitting &&
                                        currentPasswordInput.isNotBlank() &&
                                        newPasswordInput.isNotBlank() &&
                                        confirmPasswordInput.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    disabledContainerColor = Color(0xFF2563EB).copy(alpha = 0.85f),
                                    disabledContentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, trackColor = Color.Transparent, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Perbarui Kata Sandi", fontFamily = interFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
