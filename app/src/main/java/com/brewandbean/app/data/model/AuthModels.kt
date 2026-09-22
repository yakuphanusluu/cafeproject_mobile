package com.brewandbean.app.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val success: Boolean?,
    val error: String?,
    val message: String?,
    val token: String?,
    @SerializedName("needs_verification") val needsVerification: Boolean?,
    val email: String?,
    val user: UserData?
)

data class UserData(
    @SerializedName("full_name") val fullName: String,
    val username: String,
    val email: String,
    val stars: Int = 0
)

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    val username: String,
    val email: String,
    val password: String
)

data class VerifyRequest(
    val email: String,
    val code: String
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    @SerializedName("new_password") val newPassword: String
)

data class UpdateProfileRequest(
    val token: String,
    @SerializedName("full_name") val fullName: String,
    val email: String
)

data class UpdatePasswordRequest(
    val token: String,
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class GetProfileRequest(
    val token: String
)
