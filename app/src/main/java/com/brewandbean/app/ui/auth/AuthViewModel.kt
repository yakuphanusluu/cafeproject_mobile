package com.brewandbean.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewandbean.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val gameRepository: com.brewandbean.app.data.repository.GameRepository,
    private val repository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow<String?>(null)
    val authSuccess: StateFlow<String?> = _authSuccess.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    val currentUser = repository.currentUser

    fun refreshStars() {
        repository.refreshStars()
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            val result = repository.signInWithGoogle(idToken)
            if (result.isSuccess) {
                _authSuccess.value = if (com.brewandbean.app.util.LanguageManager.isEnglish.value) "Login successful!" else "Giriş başarılı!"
            } else {
                _authError.value = getErrorMessage(result.exceptionOrNull())
            }
            
            _isLoading.value = false
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            val result = repository.loginWithEmail(email, pass)
            if (result.isSuccess) {
                _authSuccess.value = if (com.brewandbean.app.util.LanguageManager.isEnglish.value) "Login successful!" else "Giriş başarılı!"
            } else {
                _authError.value = getErrorMessage(result.exceptionOrNull())
            }
            _isLoading.value = false
        }
    }

    fun registerWithEmail(email: String, pass: String, fullName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            val result = repository.registerWithEmail(email, pass, fullName)
            if (result.isSuccess) {
                _infoMessage.value = if (com.brewandbean.app.util.LanguageManager.isEnglish.value) {
                    "Registration successful! Please click the link sent to your email to verify your account."
                } else {
                    "Kayıt başarılı! Lütfen e-postanıza gönderilen linke tıklayarak hesabınızı doğrulayın."
                }
            } else {
                _authError.value = getErrorMessage(result.exceptionOrNull())
            }
            _isLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            val result = repository.resetPassword(email)
            if (result.isSuccess) {
                _infoMessage.value = if (com.brewandbean.app.util.LanguageManager.isEnglish.value) {
                    "Password reset email sent!"
                } else {
                    "Åifre sıfırlama e-postası gönderildi!"
                }
            } else {
                _authError.value = getErrorMessage(result.exceptionOrNull())
            }
            _isLoading.value = false
        }
    }

    fun updatePassword(newPass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            
            val result = repository.updatePassword(newPass)
            if (result.isSuccess) {
                _infoMessage.value = if (com.brewandbean.app.util.LanguageManager.isEnglish.value) {
                    "Password updated successfully."
                } else {
                    "Åifreniz başarıyla değiştirildi."
                }
            } else {
                _authError.value = getErrorMessage(result.exceptionOrNull())
            }
            _isLoading.value = false
        }
    }

    private fun getErrorMessage(e: Throwable?): String {
        if (e == null) return if (com.brewandbean.app.util.LanguageManager.isEnglish.value) "An error occurred" else "Bir hata oluştu"
        
        // Kendi oluşturduğumuz e-posta doğrulama hatası (Exception içinde)
        if (e.message?.contains("verify") == true || e.message?.contains("doğrulayın") == true) {
            return e.message ?: ""
        }

        val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value
        return when (e) {
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> 
                if (isEn) "Invalid email or password." else "E-posta veya şifre hatalı."
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> 
                if (isEn) "No account found with this email." else "Bu e-posta adresine ait bir hesap bulunamadı."
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> 
                if (isEn) "This email is already in use." else "Bu e-posta adresi zaten kullanımda."
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> 
                if (isEn) "Password is too weak. Must be at least 6 characters." else "Åifre çok zayıf. En az 6 karakter olmalıdır."
            else -> e.localizedMessage ?: if (isEn) "An unknown error occurred." else "Bilinmeyen bir hata oluştu."
        }
    }

    fun logout() {
        repository.logout()
    }

    fun clearError() {
        _authError.value = null
    }

    fun setAuthError(error: String) {
        _authError.value = error
    }

    fun clearSuccess() {
        _authSuccess.value = null
    }

    fun convertStarDust() {
        val token = repository.getToken() ?: return
        val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = gameRepository.convertStarDust(token, 100)
                if (result.success) {
                    _infoMessage.value = if (isEn) "Star dust converted! 100 ✨ -> 1 ⭐" else "Yıldız dönüştürüldü! 100 ✨ -> 1 ⭐"
                    repository.refreshStars()
                } else {
                    _authError.value = if (isEn) "Conversion failed" else "Dönüşüm başarısız"
                }
            } catch (e: Exception) {
                _authError.value = if (isEn) "Connection error: ${e.message}" else "Bağlantı hatası: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

