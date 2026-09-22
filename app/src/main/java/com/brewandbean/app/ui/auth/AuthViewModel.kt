package com.brewandbean.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewandbean.app.data.model.*
import com.brewandbean.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow<String?>(null)
    val authSuccess: StateFlow<String?> = _authSuccess.asStateFlow()

    private val _needsVerification = MutableStateFlow<String?>(null) // Contains email if verification is needed
    val needsVerification: StateFlow<String?> = _needsVerification.asStateFlow()

    val currentUser = repository.currentUser
    val isLoggedIn = repository.isLoggedIn()

    fun register(request: RegisterRequest, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val response = repository.register(request)
                if (response.success == true) {
                    onSuccess(request.email)
                } else {
                    _authError.value = getLocalizedError(response.error, "Kayit basarisiz")
                }
            } catch (e: Exception) {
                _authError.value = "Baglanti hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyEmail(email: String, code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val response = repository.verifyEmail(VerifyRequest(email, code))
                if (response.success == true && response.token != null) {
                    // Fetch user info using token or just ask them to login.
                    // To keep it simple, after verification we can route them to Login
                    _authSuccess.value = response.message ?: "Basariyla onaylandi"
                    onSuccess()
                } else {
                    _authError.value = getLocalizedError(response.error, "Onay basarisiz")
                }
            } catch (e: Exception) {
                _authError.value = "Baglanti hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(request: LoginRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            _needsVerification.value = null
            try {
                val response = repository.login(request)
                if (response.success == true && response.token != null && response.user != null) {
                    repository.saveSession(response.token, response.user)
                    onSuccess()
                } else if (response.needsVerification == true) {
                    _needsVerification.value = response.email
                    _authError.value = if(com.brewandbean.app.util.LanguageManager.isEnglish.value) "Account not verified, code sent." else if(com.brewandbean.app.util.LanguageManager.isEnglish.value) "Account not verified, code sent." else "Hesabiniz onaylanmamis, kod gonderildi."
                } else {
                    _authError.value = getLocalizedError(response.error, "Giris basarisiz")
                }
            } catch (e: Exception) {
                _authError.value = "Baglanti hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun forgotPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val response = repository.forgotPassword(ForgotPasswordRequest(email))
                if (response.success == true) {
                    onSuccess()
                } else {
                    _authError.value = getLocalizedError(response.error, "Hatali islem")
                }
            } catch (e: Exception) {
                _authError.value = "Baglanti hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(request: ResetPasswordRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val response = repository.resetPassword(request)
                if (response.success == true) {
                    _authSuccess.value = if(com.brewandbean.app.util.LanguageManager.isEnglish.value) "Password successfully updated." else "Sifreniz basariyla guncellendi."
                    onSuccess()
                } else {
                    _authError.value = getLocalizedError(response.error, "Sifre sifirlanamadi")
                }
            } catch (e: Exception) {
                _authError.value = "Baglanti hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(fullName: String, email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val token = repository.getToken() ?: return@launch
                val response = repository.updateProfile(UpdateProfileRequest(token, fullName, email))
                if (response.success == true && response.user != null) {
                    _authSuccess.value = if(com.brewandbean.app.util.LanguageManager.isEnglish.value) "Profile updated" else "Profil guncellendi"
                    // Update local session
                    repository.saveSession(token, response.user)
                    onSuccess()
                } else {
                    _authError.value = getLocalizedError(response.error, "Guncelleme basarisiz")
                }
            } catch (e: Exception) {
                _authError.value = "Baglanti hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePassword(currentPass: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val token = repository.getToken() ?: return@launch
                val response = repository.updatePassword(UpdatePasswordRequest(token, currentPass, newPass))
                if (response.success == true) {
                    _authSuccess.value = if(com.brewandbean.app.util.LanguageManager.isEnglish.value) "Password successfully updated." else "Sifreniz basariyla guncellendi."
                    onSuccess()
                } else {
                    _authError.value = getLocalizedError(response.error, "Sifre guncellenemedi")
                }
            } catch (e: Exception) {
                _authError.value = "Baglanti hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            try {
                val token = repository.getToken() ?: return@launch
                val response = repository.getProfile(GetProfileRequest(token))
                if (response.success == true && response.user != null) {
                    repository.saveSession(token, response.user)
                }
            } catch (e: Exception) {
                // Sessizce basarisiz ol
            }
        }
    }

    fun logout() {
        repository.logout()
    }


    private fun getLocalizedError(error: String?, defaultMsg: String): String {
        val baseMsg = error ?: defaultMsg
        if (!com.brewandbean.app.util.LanguageManager.isEnglish.value) return baseMsg
        return when(baseMsg) {
            "Geçersiz islem", "Gecersiz islem" -> "Invalid operation"
            "Token gerekli" -> "Token required"
            "Gecersiz oturum" -> "Invalid session"
            "Tum alanlari doldurun" -> "Please fill in all fields"
            "Bu e-posta veya kullanici adi zaten kullaniliyor" -> "Email or username already in use"
            "Bu e-posta veya kullanici adi zaten kayitli" -> "Email or username already registered"
            "Kayit olusturulamadi", "Kayit basarisiz" -> "Registration failed"
            "E-posta ve kod gerekli" -> "Email and code required"
            "Gecersiz veya hatali dogrulama kodu" -> "Invalid or incorrect verification code"
            "Onaylama hatasi", "Onay basarisiz" -> "Verification error"
            "Kullanici adi/E-posta ve sifre gerekli" -> "Username/Email and password required"
            "Hatali giris bilgileri" -> "Incorrect login credentials"
            "Giris basarisiz" -> "Login failed"
            "E-posta gerekli" -> "Email required"
            "E-posta, kod ve yeni sifre gerekli" -> "Email, code, and new password required"
            "Gecersiz veya suresi dolmus kod" -> "Invalid or expired code"
            "Sifre guncellenemedi", "Sifre sifirlanamadi" -> "Failed to update password"
            "Eksik bilgi gonderildi" -> "Missing information provided"
            "Bu e-posta baska bir hesaba ait" -> "This email belongs to another account"
            "Guncelleme basarisiz" -> "Update failed"
            "Mevcut sifreniz hatali" -> "Current password incorrect"
            "Boyle bir kullanici bulunamadi" -> "User not found"
            "Kod hatali veya suresi dolmus" -> "Invalid or expired code"
            "Hatali islem" -> "Invalid operation"
            else -> baseMsg
        }
    }

    fun clearError() {
        _authError.value = null
    }

    fun clearSuccess() {
        _authSuccess.value = null
    }
}
