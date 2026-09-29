package com.brewandbean.app.data.api

import com.brewandbean.app.data.model.ApiResponse
import com.brewandbean.app.data.model.OrderRequest
import com.brewandbean.app.data.model.OrderStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class CafeLocationResponse(
    val latitude: Double,
    val longitude: Double,
    val radius: Int = 100
)

interface BrewBeanApi {
    @POST("orders.php")
    suspend fun createOrder(@Body request: OrderRequest): Response<ApiResponse>

    @GET("orders.php")
    suspend fun getOrderStatus(
        @Query("customer_token") token: String,
        @Query("order_no") orderNo: String
    ): Response<OrderStatusResponse>

    @GET("location.php")
    suspend fun getCafeLocation(): CafeLocationResponse
}
