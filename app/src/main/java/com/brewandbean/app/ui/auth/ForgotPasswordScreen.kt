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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    val infoMessage by viewModel.infoMessage.collectAsState(null)
    val authError by viewModel.authError.collectAsState(null)
    val isLoading by viewModel.isLoading.collectAsState(false)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(infoMessage) {
        if (infoMessage != null) {
            onBack()
        }
    }

    LaunchedEffect(authError) {
        if (authError != null) {
            snackbarHostState.showSnackbar(authError!!)
            viewModel.clearError()
        }
    }

    val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEn) "Forgot Password" else "Şifremi Unuttum") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = if (isEn) "Back" else "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF8F5))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (isEn) "Enter your email to reset your password. We'll send you a reset link." else "Şifrenizi sıfırlamak için e-posta adresinizi girin. Size bir sıfırlama bağlantısı göndereceğiz.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFC8956C))
                } else {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (isEn) "Email" else "E-posta") },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { viewModel.resetPassword(email) },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8956C))
                    ) {
                        Text(if (isEn) "Send Reset Link" else "Sıfırlama Bağlantısı Gönder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
