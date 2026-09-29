package com.brewandbean.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewandbean.app.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isEmailPasswordProvider = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.providerData?.any { it.providerId == "password" } == true

    val infoMessage by authViewModel.infoMessage.collectAsState()
    val authError by authViewModel.authError.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(infoMessage) {
        if (infoMessage != null) {
            snackbarHostState.showSnackbar(infoMessage!!)
            authViewModel.clearInfoMessage()
        }
    }

    androidx.compose.runtime.LaunchedEffect(authError) {
        if (authError != null) {
            snackbarHostState.showSnackbar(authError!!)
            authViewModel.clearError()
        }
    }

    val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState().value

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEn) "My Profile" else "Profilim") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = if (isEn) "Back" else "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
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
                Text("👤", fontSize = 60.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = currentUser?.fullName ?: if (isEn) "Guest" else "Misafir",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF1A1A2E)
                )
                
                Text(
                    text = currentUser?.email ?: "",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isEn) "Your Stars:" else "Yıldız Puanınız:", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                            Text("${currentUser?.stars ?: 0}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFC8956C))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))

                if (isEmailPasswordProvider) {
                    OutlinedButton(
                        onClick = { 
                            currentUser?.email?.let { email ->
                                authViewModel.resetPassword(email)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A2E))
                    ) {
                        Text(if (isEn) "Change Password" else "Şifre Değiştir", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Button(
                    onClick = {
                        authViewModel.logout()
                        onLogoutSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(if (isEn) "Log out" else "Çıkış Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
