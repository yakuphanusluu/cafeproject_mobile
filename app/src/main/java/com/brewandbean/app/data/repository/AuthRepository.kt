package com.brewandbean.app.data.repository

import com.brewandbean.app.data.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: com.brewandbean.app.data.api.BrewBeanApi
) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()

    private val coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                coroutineScope.launch {
                    try {
                        val starsRes = api.getStars(user.uid)
                        _currentUser.value = user.toUserData(starsRes.stars)
                    } catch (e: Exception) {
                        _currentUser.value = user.toUserData(0)
                    }
                }
            } else {
                _currentUser.value = null
            }
        }
    }

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    fun getToken(): String? = firebaseAuth.currentUser?.uid

    fun getCurrentFirebaseUser() = firebaseAuth.currentUser

    suspend fun signInWithGoogle(idToken: String): Result<UserData> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                val stars = try { api.getStars(user.uid).stars } catch(e: Exception) { 0 }
                val userData = user.toUserData(stars)
                _currentUser.value = userData
                Result.success(userData)
            } else {
                Result.failure(Exception("Kullanıcı bilgisi alınamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, pass: String): Result<UserData> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) {
                if (!user.isEmailVerified) {
                    firebaseAuth.signOut()
                    _currentUser.value = null
                    val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value
                    val msg = if (isEn) "Please verify your email address. (Check your spam folder)" else "Lütfen e-posta adresinizi doğrulayın. (Spam/Gereksiz klasörünü kontrol edin)"
                    return Result.failure(Exception(msg))
                }
                
                val stars = try { api.getStars(user.uid).stars } catch(e: Exception) { 0 }
                val userData = user.toUserData(stars)
                _currentUser.value = userData
                Result.success(userData)
            } else {
                Result.failure(Exception("Kullanıcı bilgisi alınamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(newPass: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.updatePassword(newPass).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Kullanıcı oturumu bulunamadı."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(email: String, pass: String, fullName: String): Result<Unit> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) {
                // Update profile with full name
                val profileUpdates = userProfileChangeRequest {
                    displayName = fullName
                }
                user.updateProfile(profileUpdates).await()
                
                // Doğrulama e-postası gönder
                try {
                    user.sendEmailVerification().await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                // Güvenlik: E-posta doğrulanana kadar oturumu kapat
                firebaseAuth.signOut()
                _currentUser.value = null
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Kayıt işlemi başarısız"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    private fun FirebaseUser.toUserData(fetchedStars: Int = 0): UserData {
        return UserData(
            fullName = displayName ?: "Misafir",
            username = email?.substringBefore("@") ?: "",
            email = email ?: "",
            stars = fetchedStars
        )
    }
}
