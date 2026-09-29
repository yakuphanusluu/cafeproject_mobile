package com.brewandbean.app.ui.customer

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.brewandbean.app.service.OrderTrackingService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brewandbean.app.R
import com.brewandbean.app.data.model.*
import com.brewandbean.app.data.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.brewandbean.app.data.repository.AuthRepository

@HiltViewModel
class CustomerViewModel @Inject constructor(
    application: Application,
    private val repository: MenuRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val currentUser = authRepository.currentUser

    fun refreshStars() {
        authRepository.refreshStars()
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _orderStatus = MutableStateFlow<String?>(null)
    val orderStatus: StateFlow<String?> = _orderStatus.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val locationHelper = com.brewandbean.app.util.LocationHelper(application)

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    fun clearLocationError() {
        _locationError.value = null
    }

    val categories: List<Pair<String, String>> = repository.getCategories()

    var tableNo: Int = 1

    init {
        loadProducts()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentProducts = repository.getProducts(_selectedCategory.value)
                _products.value = currentProducts
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToCart(product: Product, sizeIndex: Int) {
        val cartKey = "${product.id}-${sizeIndex}"
        val currentList = _cartItems.value
        val existingItemIndex = currentList.indexOfFirst { it.cartKey == cartKey }

        if (existingItemIndex != -1) {
            val updatedList = currentList.toMutableList()
            val existing = updatedList[existingItemIndex]
            updatedList[existingItemIndex] = existing.copy(quantity = existing.quantity + 1)
            _cartItems.value = updatedList
        } else {
            _cartItems.value = currentList + CartItem(product, sizeIndex, 1)
        }
        
        val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value
        _toastMessage.value = if (isEn) {
            "${product.name} (${product.sizes[sizeIndex].label}) added to cart!"
        } else {
            "${product.name} (${product.sizes[sizeIndex].label}) sepete eklendi!"
        }
    }

    fun removeFromCart(cartKey: String) {
        _cartItems.value = _cartItems.value.filter { it.cartKey != cartKey }
    }

    fun updateQuantity(cartKey: String, delta: Int) {
        val currentList = _cartItems.value
        val updatedList = currentList.mapNotNull { item ->
            if (item.cartKey == cartKey) {
                val newQuantity = item.quantity + delta
                if (newQuantity <= 0) null else item.copy(quantity = newQuantity)
            } else {
                item
            }
        }
        _cartItems.value = updatedList
    }

    fun placeOrder(
        guestName: String = "",
        guestTableNo: Int = 0,
        note: String = "",
        paymentMethod: String,
        usedStars: Boolean = false
    ) {
        if (_cartItems.value.isEmpty()) return
        
        val user = authRepository.currentUser.value
        val token = authRepository.getToken()
        
        val finalCustomerName = user?.fullName ?: guestName
        val finalTableNo = if (guestTableNo > 0) guestTableNo else this.tableNo
        val finalToken = token ?: ""
        
        viewModelScope.launch {
            _isLoading.value = true
            val isEn = com.brewandbean.app.util.LanguageManager.isEnglish.value
            
            // Konum kontrolü
            try {
                val cafeLocation = repository.getCafeLocation()
                val userLocation = locationHelper.getCurrentLocation()
                if (userLocation == null) {
                    _locationError.value = if (isEn) "Location could not be retrieved. Please grant location permission." else "Konumunuz alınamadı. Lütfen konum izni verin."
                    _isLoading.value = false
                    return@launch
                }
                val distance = com.brewandbean.app.util.LocationHelper.calculateDistance(
                    userLocation.latitude, userLocation.longitude,
                    cafeLocation.latitude, cafeLocation.longitude
                )
                if (distance > cafeLocation.radius) {
                    _locationError.value = if (isEn) "You must be near the cafe to place an order. You are currently ${distance.toInt()} meters away." else "Sipariş verebilmek için kafeye yakın olmanız gerekiyor. Şu an ${distance.toInt()} metre uzaktasınız."
                    _isLoading.value = false
                    return@launch
                }
            } catch (e: Exception) {
                // Konum servisi çalışmıyorsa siparişe izin ver (hata toleransı)
            }

            try {
                val orderItems = _cartItems.value.map { item ->
                    OrderItemRequest(
                        name = item.product.name,
                        emoji = item.product.emoji,
                        sizeLabel = item.size.label,
                        price = item.size.price,
                        qty = item.quantity
                    )
                }
                
                val orderRequest = OrderRequest(
                    customerName = finalCustomerName,
                    phone = user?.email ?: "", 
                    tableNo = finalTableNo,
                    paymentMethod = paymentMethod,
                    note = note,
                    userToken = finalToken,
                    usedStars = usedStars,
                    items = orderItems
                )
                
                val response = repository.placeOrder(orderRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    _cartItems.value = emptyList()
                    _orderStatus.value = "Siparisiniz alindi!"
                    response.body()?.customerToken?.let { token ->
                        startOrderPolling(token, response.body()?.orderNo ?: "")
                    }
                } else {
                    _orderStatus.value = "Hata: ${response.body()?.message ?: "Bilinmeyen hata"}"
                }
            } catch (e: Exception) {
                _orderStatus.value = "Baglanti hatasi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startOrderPolling(token: String, orderNo: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, OrderTrackingService::class.java).apply {
            putExtra(OrderTrackingService.EXTRA_TOKEN, token)
            putExtra("EXTRA_ORDER_NO", orderNo)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun clearOrderStatus() {
        _orderStatus.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
