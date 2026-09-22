package com.brewandbean.app.data.model

data class Product(
    val id: Int,
    val name: String,
    val nameEn: String = "",
    val desc: String,
    val descEn: String = "",
    val category: String,
    val categoryLabel: String,
    val categoryLabelEn: String = "",
    val emoji: String,
    val imageUrl: String,
    val gradientStart: Long, // Color as ARGB long
    val gradientEnd: Long,
    val sizes: List<ProductSize>
)

data class ProductSize(
    val label: String,
    val labelEn: String = "",
    val price: Int
)

data class CartItem(
    val product: Product,
    val selectedSizeIndex: Int = 0,
    var quantity: Int = 1
) {
    val size: ProductSize get() = product.sizes[selectedSizeIndex]
    val totalPrice: Int get() = size.price * quantity
    val cartKey: String get() = "${product.id}-${selectedSizeIndex}"
}

data class OrderRequest(
    @com.google.gson.annotations.SerializedName("customer_name") val customerName: String,
    val phone: String,
    @com.google.gson.annotations.SerializedName("table_no") val tableNo: Int,
    @com.google.gson.annotations.SerializedName("payment_method") val paymentMethod: String,
    val note: String,
    @com.google.gson.annotations.SerializedName("user_token") val userToken: String? = null,
    @com.google.gson.annotations.SerializedName("used_stars") val usedStars: Boolean = false,
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val name: String,
    val emoji: String,
    @com.google.gson.annotations.SerializedName("size_label") val sizeLabel: String,
    val price: Int,
    val qty: Int
)

data class ApiResponse(
    val success: Boolean?,
    val status: String?,
    val message: String?,
    @com.google.gson.annotations.SerializedName("order_no") val orderNo: String?,
    @com.google.gson.annotations.SerializedName("customer_token") val customerToken: String?
)

data class OrderStatusResponse(
    val id: Int?,
    @com.google.gson.annotations.SerializedName("order_no") val orderNo: String?,
    @com.google.gson.annotations.SerializedName("customer_name") val customerName: String?,
    val status: String?,
    val error: String?
)

