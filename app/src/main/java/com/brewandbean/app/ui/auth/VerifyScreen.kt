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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(
    email: String,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()

    var code by remember { mutableStateOf("") }

    val clrPrimary = Color(0xFF1a1a2e)
    val clrBg = Color(0xFFfaf8f5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(isEn) "Verification" else "Dogrulama", fontWeight = FontWeight.Bold, color = clrPrimary) },
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
            
            Text(if(isEn) "We sent a 6-digit code to your email." else "E-posta adresinize 6 haneli bir kod gonderdik.", textAlign = TextAlign.Center, fontSize = 16.sp)
            Text(email, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                label = { Text(if(isEn) "Verification Code" else "Dogrulama Kodu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                    if (code.length == 6) {
                        viewModel.verifyEmail(email, code) {
                            onVerifySuccess()
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
                    Text(if(isEn) "Confirm" else "Onayla", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
