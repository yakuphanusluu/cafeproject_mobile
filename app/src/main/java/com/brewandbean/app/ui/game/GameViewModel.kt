package com.brewandbean.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewandbean.app.data.model.*
import com.brewandbean.app.data.repository.AuthRepository
import com.brewandbean.app.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState.IDLE)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _timeLeft = MutableStateFlow(60)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _cards = MutableStateFlow<List<MemoryCard>>(emptyList())
    val cards: StateFlow<List<MemoryCard>> = _cards.asStateFlow()

    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    private val _canPlay = MutableStateFlow<Boolean?>(null)
    val canPlay: StateFlow<Boolean?> = _canPlay.asStateFlow()

    private val _currentStarDust = MutableStateFlow(0)
    val currentStarDust: StateFlow<Int> = _currentStarDust.asStateFlow()

    private val _submitResult = MutableStateFlow<SubmitScoreResponse?>(null)
    val submitResult: StateFlow<SubmitScoreResponse?> = _submitResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var timerJob: Job? = null
    private var firstSelectedCardId: Int? = null
    private var secondSelectedCardId: Int? = null
    private var isProcessingMatch = false

    fun checkCanPlay() {
        val token = authRepository.getToken()
        if (token == null) {
            _isGuest.value = true
            _canPlay.value = false
            return
        }
        _isGuest.value = false
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = gameRepository.canPlay(token)
                _canPlay.value = result.canPlay
                _currentStarDust.value = result.starDust
            } catch (e: Exception) {
                _errorMessage.value = "Bağlantı hatası: ${e.message} (PHP dosyalarını sunucuya yüklediniz mi?)"
                _canPlay.value = true // Sunucu hatası varsa oynamaya geçici olarak izin ver (test için)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startGame() {
        _gameState.value = GameState.PLAYING
        _score.value = 0
        _timeLeft.value = 60
        _submitResult.value = null
        firstSelectedCardId = null
        secondSelectedCardId = null
        isProcessingMatch = false

        // 6 Çift Kart (12 Kart Toplam)
        val pairEmojis = listOf("☕", "🥐", "🍰", "🫘", "🍩", "🧊")
        val deck = (pairEmojis + pairEmojis).shuffled().mapIndexed { i, e ->
            MemoryCard(id = i, emoji = e)
        }
        _cards.value = deck

        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && _gameState.value == GameState.PLAYING) {
                delay(1000)
                _timeLeft.value -= 1
            }
            if (_gameState.value == GameState.PLAYING) {
                endGame() // Süre bittiğinde de oyunu bitir ve olan skoru gönder
            }
        }
    }

    fun onCardClicked(cardId: Int, vibrate: () -> Unit, vibrateError: () -> Unit) {
        if (isProcessingMatch || _gameState.value != GameState.PLAYING) return
        
        val currentCards = _cards.value.toMutableList()
        val cardIndex = currentCards.indexOfFirst { it.id == cardId }
        if (cardIndex == -1) return
        val card = currentCards[cardIndex]

        if (card.isFaceUp || card.isMatched) return

        vibrate() // Kartı çevirme hissi

        // Kartı aç
        currentCards[cardIndex] = card.copy(isFaceUp = true)
        _cards.value = currentCards

        if (firstSelectedCardId == null) {
            firstSelectedCardId = cardId
        } else if (secondSelectedCardId == null) {
            secondSelectedCardId = cardId
            checkMatch(vibrate, vibrateError)
        }
    }

    private fun checkMatch(vibrateSuccess: () -> Unit, vibrateError: () -> Unit) {
        isProcessingMatch = true
        viewModelScope.launch {
            val currentCards = _cards.value.toMutableList()
            val idx1 = currentCards.indexOfFirst { it.id == firstSelectedCardId }
            val idx2 = currentCards.indexOfFirst { it.id == secondSelectedCardId }
            
            val card1 = currentCards[idx1]
            val card2 = currentCards[idx2]

            delay(600) // Eşleşme sonucunu görmek için yarım saniye bekle

            if (card1.emoji == card2.emoji) {
                // Eşleşti!
                vibrateSuccess()
                currentCards[idx1] = card1.copy(isMatched = true)
                currentCards[idx2] = card2.copy(isMatched = true)
                _score.value += 12 // Her eşleşme 12 yıldız tozu
                _cards.value = currentCards

                // Oyun bitti mi kontrol et
                if (currentCards.all { it.isMatched }) {
                    val bonus = _timeLeft.value / 2 // Kalan saniyenin yarısı kadar bonus
                    _score.value += bonus
                    endGame()
                }
            } else {
                // Eşleşmedi, kartları geri kapat
                vibrateError()
                currentCards[idx1] = card1.copy(isFaceUp = false)
                currentCards[idx2] = card2.copy(isFaceUp = false)
                _cards.value = currentCards
            }

            firstSelectedCardId = null
            secondSelectedCardId = null
            isProcessingMatch = false
        }
    }

    private fun endGame() {
        _gameState.value = GameState.ENDED
        timerJob?.cancel()
        submitScore()
    }

    private fun submitScore() {
        val token = authRepository.getToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = gameRepository.submitScore(token, _score.value)
                _submitResult.value = result
                _currentStarDust.value = result.totalStarDust
                authRepository.refreshStars()
            } catch (e: Exception) {
                _errorMessage.value = "Skor kaydedilemedi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetGame() {
        _gameState.value = GameState.IDLE
        _canPlay.value = null
        _submitResult.value = null
        _errorMessage.value = null
        checkCanPlay()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun convertStarDust() {
        val token = authRepository.getToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = gameRepository.convertStarDust(token, 100)
                _currentStarDust.value = result.remainingDust
                authRepository.refreshStars()
                _errorMessage.value = "Yıldız dönüştürüldü!"
            } catch (e: Exception) {
                _errorMessage.value = "Dönüşüm başarısız"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

enum class GameState { IDLE, PLAYING, ENDED }

