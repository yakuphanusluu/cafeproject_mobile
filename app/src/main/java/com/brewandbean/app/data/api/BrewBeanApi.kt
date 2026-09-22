package com.brewandbean.app.data.api

import com.brewandbean.app.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BrewBeanApi {
    @POST("orders.php")
    suspend fun createOrder(@Body orderRequest: OrderRequest): ApiResponse

    @GET("orders.php")
    suspend fun getOrderStatus(@Query("customer_token") customerToken: String): OrderStatusResponse

    @POST("auth.php?action=register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth.php?action=verify_email")
    suspend fun verifyEmail(@Body request: VerifyRequest): AuthResponse

    @POST("auth.php?action=login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth.php?action=forgot_password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): AuthResponse

    @POST("auth.php?action=reset_password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): AuthResponse

    @POST("auth.php?action=update_profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): AuthResponse

    @POST("auth.php?action=update_password")
    suspend fun updatePassword(@Body request: UpdatePasswordRequest): AuthResponse

    @POST("auth.php?action=get_profile")
    suspend fun getProfile(@Body request: GetProfileRequest): AuthResponse
}
