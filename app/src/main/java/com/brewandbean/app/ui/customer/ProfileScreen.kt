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
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val isEmailPasswordProvider = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.providerData?.any { it.providerId == "password" } == true

    androidx.compose.runtime.LaunchedEffect(Unit) {
        authViewModel.refreshStars()
    }

    val infoMessage by authViewModel.infoMessage.collectAsState()
    val authError by authViewModel.authError.collectAsState()
    val showNotification = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val isErrorNotification = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(infoMessage) {
        if (infoMessage != null) {
            isErrorNotification.value = false
            showNotification.value = infoMessage!!
            kotlinx.coroutines.delay(2500)
            showNotification.value = null
            authViewModel.clearInfoMessage()
        }
    }

    androidx.compose.runtime.LaunchedEffect(authError) {
        if (authError != null) {
            isErrorNotification.value = true
            showNotification.value = authError!!
            kotlinx.coroutines.delay(2500)
            showNotification.value = null
            authViewModel.clearError()
        }
    }

    val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState().value

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
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
                
                                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF3E0), // Açık turuncu
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (isEn) "Star Dust:" else "Yıldız Tozu:",
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                            )
                            Text(
                                "100 ✨ = 1 ⭐",
                                fontSize = 12.sp, color = Color(0xFF7A7A7A)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                "${currentUser?.starDust ?: 0}",
                                fontWeight = FontWeight.Bold, fontSize = 24.sp,
                                color = Color(0xFFF57F17)
                            )
                        }
                    }
                }
                
                if ((currentUser?.starDust ?: 0) >= 100) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            com.brewandbean.app.util.VibrationHelper.vibrate(context, 60)
                            authViewModel.convertStarDust()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC8956C)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (isEn) "⭐ Convert to Star" else "⭐ Yıldıza Dönüştür",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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

        androidx.compose.animation.AnimatedVisibility(
            visible = showNotification.value != null,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            showNotification.value?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isErrorNotification.value) Color(0xFFD32F2F) else Color(0xFF2D8A4E)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 64.dp) // Below top bar
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isErrorNotification.value) "⚠️" else "✨", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(msg.replace("✅ ", ""), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
