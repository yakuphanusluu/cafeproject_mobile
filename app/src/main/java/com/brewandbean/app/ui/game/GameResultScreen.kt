package com.brewandbean.app.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewandbean.app.ui.customer.AccentColor
import com.brewandbean.app.ui.customer.BackgroundColor
import com.brewandbean.app.ui.customer.PrimaryColor
import com.brewandbean.app.ui.customer.SurfaceColor

@Composable
fun GameResultScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit,
    isEn: Boolean,
    context: android.content.Context
) {
    val submitResult by viewModel.submitResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentStarDust by viewModel.currentStarDust.collectAsState()

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text(if (errorMessage == "Yıldız dönüştürüldü!") (if (isEn) "Success!" else "Başarılı!") else (if(isEn) "Notice" else "Uyarı")) },
            text = { Text(if (errorMessage == "Yıldız dönüştürüldü!") (if (isEn) "1 Star has been added to your account!" else "Hesabınıza 1 Yıldız eklendi!") else errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val earned = submitResult?.earned ?: 0
        val emoji = when {
            earned > 100 -> "🏆"
            earned > 50 -> "🎉"
            else -> "☕"
        }

        Text(emoji, fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (isEn) "Game Over!" else "Oyun Bitti!",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading && submitResult == null) {
            CircularProgressIndicator(color = AccentColor)
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isEn) "Star Dust Earned:" else "Kazandığın Yıldız Tozu:",
                        color = PrimaryColor,
                        fontSize = 16.sp
                    )
                    Text(
                        "✨ $earned",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = AccentColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Divider(color = Color(0xFFE8E0D8), modifier = Modifier.padding(vertical = 12.dp))
                    Text(
                        if (isEn) "Total Star Dust: ✨ $currentStarDust" else "Toplam Yıldız Tozun: ✨ $currentStarDust",
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryColor
                    )
                    Text(
                        if (isEn) "100 ✨ = 1 ⭐" else "100 ✨ = 1 ⭐",
                        fontSize = 12.sp,
                        color = Color(0xFF7A7A7A),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (currentStarDust >= 100) {
                Button(
                    onClick = {
                        com.brewandbean.app.util.VibrationHelper.vibrate(context, 60)
                        viewModel.convertStarDust()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading
                ) {
                    Text(if (isEn) "⭐ Convert to Star" else "⭐ Yıldıza Dönüştür", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedButton(
                onClick = onBackToMenu,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (isEn) "Back to Menu" else "Ana Menüye Dön", color = PrimaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

