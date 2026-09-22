package com.brewandbean.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.brewandbean.app.data.api.BrewBeanApi
import com.brewandbean.app.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: BrewBeanApi,
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserData?>(loadUserFromPrefs())
    val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()

    fun isLoggedIn(): Boolean {
        return prefs.getString("token", null) != null
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    private fun loadUserFromPrefs(): UserData? {
        val fullName = prefs.getString("full_name", null)
        val username = prefs.getString("username", null)
        val email = prefs.getString("email", null)
        val stars = prefs.getInt("stars", 0)
        if (fullName != null && username != null && email != null) {
            return UserData(fullName, username, email, stars)
        }
        return null
    }

    fun saveSession(token: String, user: UserData) {
        prefs.edit()
            .putString("token", token)
            .putString("full_name", user.fullName)
            .putString("username", user.username)
            .putString("email", user.email)
            .putInt("stars", user.stars)
            .apply()
        _currentUser.value = user
    }

    fun logout() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        return api.register(request)
    }

    suspend fun verifyEmail(request: VerifyRequest): AuthResponse {
        return api.verifyEmail(request)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return api.login(request)
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): AuthResponse {
        return api.forgotPassword(request)
    }

    suspend fun resetPassword(request: ResetPasswordRequest): AuthResponse {
        return api.resetPassword(request)
    }

    suspend fun updateProfile(request: UpdateProfileRequest): AuthResponse {
        return api.updateProfile(request)
    }

    suspend fun updatePassword(request: UpdatePasswordRequest): AuthResponse {
        return api.updatePassword(request)
    }

    suspend fun getProfile(request: GetProfileRequest): AuthResponse {
        return api.getProfile(request)
    }

    suspend fun fetchAndSaveProfile() {
        val token = getToken() ?: return
        try {
            val response = getProfile(GetProfileRequest(token))
            if (response.success == true && response.user != null) {
                saveSession(token, response.user)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
