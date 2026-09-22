package com.brewandbean.app.ui.customer

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
import com.brewandbean.app.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: com.brewandbean.app.ui.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()

    var fullName by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    
    var showPasswordDialog by remember { mutableStateOf(false) }

    val clrPrimary = Color(0xFF1a1a2e)
    val clrAccent = Color(0xFFd4a373)
    val clrBg = Color(0xFFfaf8f5)

    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    androidx.compose.material3.TextButton(onClick = { com.brewandbean.app.util.LanguageManager.setEnglish(!isEn) }) {
                        androidx.compose.material3.Text(text = if (isEn) "EN" else "TR", color = Color(0xFF1A1A2E), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                },
                title = { Text(if(isEn) "My Profile" else "Profilim", fontWeight = FontWeight.Bold, color = clrPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            Spacer(modifier = Modifier.height(20.dp))
            
            // YILDIZ KARTI
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if(isEn) "Loyalty Stars" else "Sadakat Yildizlari", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontSize = 16.sp)
                        Text(if(isEn) "1 Small Coffee Free for 10 Stars!" else "10 Yildiza 1 Kucuk Kahve Bedava!", fontSize = 12.sp, color = Color(0xFFF57F17))
                    }
                    Text("🌟 ${currentUser?.stars ?: 0}", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if(isEn) "Your Info" else "Bilgileriniz", fontWeight = FontWeight.Bold, color = clrPrimary, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
                    
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(if(isEn) "Full Name" else "Ad Soyad") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if(isEn) "Email Address" else "E-posta Adresi") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    if (authError != null && !showPasswordDialog) {
                        Text(authError!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                    if (authSuccess != null && !showPasswordDialog) {
                        Text(authSuccess!!, color = Color(0xFF2d8a4e), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.updateProfile(fullName, email, {}) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = clrPrimary),
                        enabled = !isLoading
                    ) {
                        Text(if(isEn) "Update Info" else "Bilgileri Guncelle")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = clrAccent)
            ) {
                Text(if(isEn) "Change Password" else "Sifremi Degistir", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text(if(isEn) "Log Out" else "Cikis Yap", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            viewModel = viewModel,
            onDismiss = { showPasswordDialog = false; viewModel.clearError(); viewModel.clearSuccess() }
        )
    }
}

@Composable
fun ChangePasswordDialog(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if(isEn) "Change Password" else "Sifre Degistir", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                
                OutlinedTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    label = { Text(if(isEn) "Current Password" else "Mevcut Sifre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text(if(isEn) "New Password" else "Yeni Sifre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                if (authError != null) {
                    Text(authError!!, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }
                if (authSuccess != null) {
                    Text(authSuccess!!, color = Color(0xFF2d8a4e), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text(if(isEn) "Close" else "Kapat")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.updatePassword(currentPass, newPass) {} },
                        enabled = !isLoading && currentPass.isNotBlank() && newPass.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1a1a2e))
                    ) {
                        Text(if(isEn) "Change" else "Degistir")
                    }
                }
            }
        }
    }
}
