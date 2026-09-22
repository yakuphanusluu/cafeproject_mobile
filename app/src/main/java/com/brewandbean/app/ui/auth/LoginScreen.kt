package com.brewandbean.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.brewandbean.app.data.model.LoginRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    onNavigateToForgot: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val needsVerification by viewModel.needsVerification.collectAsState()

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val clrPrimary = Color(0xFF1a1a2e)
    val clrAccent = Color(0xFFd4a373)
    val clrBg = Color(0xFFfaf8f5)

    LaunchedEffect(needsVerification) {
        needsVerification?.let { email ->
            onNavigateToVerify(email)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(clrBg), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Brew & Bean", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = clrPrimary)
            Text(if(isEn) "Welcome to the Coffee Shop" else "Kahve Dukkanina Hosgeldiniz", fontSize = 16.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text(if(isEn) if(isEn) "Email or Username" else "Email or Username" else "E-posta veya Kullanici Adi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = clrAccent,
                    focusedLabelColor = clrAccent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if(isEn) "Password" else "Sifre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = clrAccent,
                    focusedLabelColor = clrAccent
                )
            )

            if (authError != null && needsVerification == null) {
                Text(authError!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                if(isEn) "Forgot Password" else "Sifremi Unuttum", 
                color = clrAccent, 
                fontSize = 14.sp, 
                modifier = Modifier.align(Alignment.End).clickable { onNavigateToForgot() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    if (login.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(LoginRequest(login, password), onLoginSuccess)
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
                    Text(if(isEn) "Log In" else "Giris Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(if(isEn) "Don't have an account? " else "Hesabiniz yok mu? ", color = Color.Gray)
                Text(if(isEn) "Register" else "Kayit Ol", color = clrAccent, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = onLoginSuccess,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = clrPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, clrPrimary)
            ) {
                Text(if(isEn) "Continue as Guest" else "Misafir Olarak Devam Et", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
