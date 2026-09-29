package com.brewandbean.app.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToVerify: (String) -> Unit = {},
    onNavigateToForgot: () -> Unit = {},
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authSuccess by viewModel.authSuccess.collectAsState(null)
    val infoMessage by viewModel.infoMessage.collectAsState(null)
    val authError by viewModel.authError.collectAsState(null)
    val isLoading by viewModel.isLoading.collectAsState(false)
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(authSuccess) {
        if (authSuccess != null) {
            onLoginSuccess()
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(infoMessage) {
        if (infoMessage != null) {
            snackbarHostState.showSnackbar(infoMessage!!)
            viewModel.clearInfoMessage()
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
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("☕", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Brew & Bean",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF1A1A2E)
                )
                Text(
                    text = "— Est. 2024 —",
                    color = Color(0xFFC8956C),
                    letterSpacing = 2.sp,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (isEn) "Password" else "Şifre") },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.loginWithEmail(email, password) },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8956C))
                    ) {
                        Text(if (isEn) "Login" else "Giriş Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (isEn) "or" else "veya", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    
                                    var activityContext: Context = context
                                    while (activityContext is ContextWrapper) {
                                        if (activityContext is Activity) break
                                        activityContext = activityContext.baseContext
                                    }
                                    
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId("42980989556-rjlklvf3hf3m85jdphodommkoiadpl4l.apps.googleusercontent.com")
                                        .setAutoSelectEnabled(false)
                                        .build()
                                    
                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()
                                        
                                    val result = credentialManager.getCredential(activityContext, request)
                                    val credential = result.credential
                                    
                                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val idToken = googleIdTokenCredential.idToken
                                        viewModel.signInWithGoogle(idToken)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    viewModel.setAuthError(e.localizedMessage ?: if (isEn) "Google sign-in error" else "Google giriş hatası")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFDDDDDD)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔵", fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                            Text(if (isEn) "Sign in with Google" else "Google ile Giriş Yap", color = Color(0xFF1A1A2E), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onNavigateToForgot) {
                        Text(if (isEn) "Forgot Password?" else "Şifremi Unuttum", color = Color(0xFF1A1A2E))
                    }
                    TextButton(onClick = onNavigateToRegister) {
                        Text(if (isEn) "Register" else "Kayıt Ol", color = Color(0xFFC8956C), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onLoginSuccess) {
                    Text(if (isEn) "Continue as Guest" else "Misafir olarak devam et", color = Color(0xFF7A7A7A))
                }
            }
        }
    }
}
