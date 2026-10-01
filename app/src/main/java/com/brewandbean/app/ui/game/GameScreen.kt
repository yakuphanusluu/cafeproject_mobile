package com.brewandbean.app.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brewandbean.app.data.model.MemoryCard
import com.brewandbean.app.ui.customer.AccentColor
import com.brewandbean.app.ui.customer.BackgroundColor
import com.brewandbean.app.ui.customer.PrimaryColor
import com.brewandbean.app.ui.customer.SurfaceColor

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val canPlay by viewModel.canPlay.collectAsState()
    val context = LocalContext.current
    val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.checkCanPlay()
    }

    when (gameState) {
        GameState.IDLE -> {
            IdleScreen(
                canPlay = canPlay,
                onStart = { viewModel.startGame() },
                onBack = onBack,
                isEn = isEn
            )
        }
        GameState.PLAYING -> {
            PlayingScreen(viewModel, context, isEn)
        }
        GameState.ENDED -> {
            GameResultScreen(
                viewModel = viewModel,
                onBackToMenu = onBack,
                isEn = isEn,
                context = context
            )
        }
    }
}

@Composable
fun IdleScreen(canPlay: Boolean?, onStart: () -> Unit, onBack: () -> Unit, isEn: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🃏", fontSize = 100.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (isEn) "Coffee Match" else "Kahve Eşleştirme",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            textAlign = TextAlign.Center
        )
        Text(
            if (isEn) "Find the matching pairs!" else "Aynı olan kartları eşleştir!",
            color = PrimaryColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(if (isEn) "Rules:" else "Kurallar:", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryColor, modifier = Modifier.padding(bottom = 12.dp))
                Text("⏱️ 60 ${if (isEn) "Seconds" else "Saniye"}")
                Text("✨ ${if (isEn) "12 Star Dust per match" else "Eşleşme başına 12 Toz"}")
                Text("🏆 ${if (isEn) "Finish fast for bonus!" else "Hızlı bitir bonus kazan!"}")
                Text("☕ ${if (isEn) "No stress, just chill." else "Stres yok, hafıza var."}")
            }
        }

        if (canPlay == true) {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (isEn) "Start Game" else "Oyunu Başlat", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else if (canPlay == false) {
            Button(
                onClick = { },
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(if (isEn) "You have played today" else "Bugünkü hakkınızı kullandınız", fontSize = 16.sp)
            }
        } else {
            CircularProgressIndicator(color = AccentColor)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Text(if (isEn) "Back to Menu" else "Menüye Dön", color = PrimaryColor)
        }
    }
}

@Composable
fun PlayingScreen(viewModel: GameViewModel, context: android.content.Context, isEn: Boolean) {
    val cards by viewModel.cards.collectAsState()
    val score by viewModel.score.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        // HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("✨ $score", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("⏱️ $timeLeft", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Grid
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val isLandscape = maxWidth > maxHeight
            val columnsCount = if (isLandscape) 4 else 3
            val rowsCount = if (isLandscape) 3 else 4
            
            val spacing = 12.dp
            val totalVerticalSpacing = spacing * (rowsCount - 1)
            val cardHeight = (maxHeight - totalVerticalSpacing) / rowsCount
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false // Sığacağı için kaydırmayı kapat
            ) {
                items(cards) { card ->
                    MemoryCardView(
                        card = card,
                        cardHeight = cardHeight,
                        onClick = {
                            viewModel.onCardClicked(
                                cardId = card.id,
                                vibrate = { com.brewandbean.app.util.VibrationHelper.vibrate(context, 20) },
                                vibrateError = { com.brewandbean.app.util.VibrationHelper.vibrate(context, 80) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryCardView(card: MemoryCard, cardHeight: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(400),
        label = "cardFlip"
    )
    val isBackVisible = rotation <= 90f

    Card(
        modifier = Modifier
            .height(cardHeight)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFaceUp && !card.isMatched) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBackVisible) AccentColor else SurfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (card.isMatched) 0.dp else 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isBackVisible) {
                // Kartın arkası
                Text("☕", fontSize = 32.sp)
            } else {
                // Kartın önü
                Text(
                    text = card.emoji,
                    fontSize = 40.sp,
                    modifier = Modifier.graphicsLayer {
                        rotationY = 180f // Emojinin ters dönmesini engellemek için
                        alpha = if (card.isMatched) 0.5f else 1f
                    }
                )
            }
        }
    }
}
