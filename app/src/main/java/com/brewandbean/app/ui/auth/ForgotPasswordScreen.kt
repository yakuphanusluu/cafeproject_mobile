package com.brewandbean.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.brewandbean.app.util.LanguageManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewandbean.app.data.model.ResetPasswordRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()

    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    
    var step by remember { mutableStateOf(1) } // 1: Email, 2: Code & New Password

    val clrPrimary = Color(0xFF1a1a2e)
    val clrBg = Color(0xFFfaf8f5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(isEn) "Forgot Password" else "Sifremi Unuttum", fontWeight = FontWeight.Bold, color = clrPrimary) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.clearError(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = if(isEn) "Back" else "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = clrBg)
            )
        },
        containerColor = clrBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            if (step == 1) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if(isEn) "Email Address" else "E-posta Adresi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (authError != null) {
                    Text(authError!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            viewModel.forgotPassword(email) {
                                step = 2
                                viewModel.clearError()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = clrPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if(isEn) "Send Reset Code" else "Sifirlama Kodu Gonder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it },
                    label = { Text(if(isEn) "6-Digit Code from Email" else "E-postadaki 6 Haneli Kod") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(if(isEn) "New Password" else "Yeni Sifre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                if (authError != null) {
                    Text(authError!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
                }
                if (authSuccess != null) {
                    Text(authSuccess!!, color = Color(0xFF2d8a4e), fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (code.length == 6 && newPassword.isNotBlank()) {
                            viewModel.resetPassword(ResetPasswordRequest(email, code, newPassword)) {
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = clrPrimary),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if(isEn) "Reset Password" else "Sifremi Yenile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
