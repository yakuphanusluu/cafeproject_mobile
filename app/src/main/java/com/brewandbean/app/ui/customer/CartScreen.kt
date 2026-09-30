package com.brewandbean.app.ui.customer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.brewandbean.app.util.LanguageManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewandbean.app.data.model.CartItem
import com.brewandbean.app.data.model.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
private val colorPrimary = Color(0xFF1A1A2E)
private val colorAccent = Color(0xFFC8956C)
private val colorAccentLight = Color(0xFFF5EBE0)
private val colorBackground = Color(0xFFFAF8F5)
private val colorSurface = Color(0xFFFFFFFF)
private val colorTextMuted = Color(0xFF7A7A7A)
private val colorSuccess = Color(0xFF2D8A4E)
private val colorDanger = Color(0xFFD32F2F)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState(emptyList())
    val orderStatus by viewModel.orderStatus.collectAsState(null)
    val isLoading by viewModel.isLoading.collectAsState(false)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    val currentUser by viewModel.currentUser.collectAsState(null)
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    
    val locationError by viewModel.locationError.collectAsState(null)
    val context = androidx.compose.ui.platform.LocalContext.current
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> }

    LaunchedEffect(Unit) {
        if (!com.brewandbean.app.util.LocationHelper(context).hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    if (locationError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearLocationError() },
            title = { Text(if(isEn) "Location Warning" else "Konum Uyarısı") },
            text = { Text(locationError ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearLocationError() }) {
                    Text("Tamam")
                }
            }
        )
    }

    val canUseStars = remember(currentUser, cartItems, isEn) {
        val userStars = currentUser?.stars ?: 0
        if (userStars >= 10 && cartItems.size == 1) {
            val item = cartItems[0]
            if (item.quantity == 1 && item.product.category.contains("kahve")) {
                val s = if(isEn) item.size.labelEn else item.size.label.lowercase()
                s == "tek" || s == "s" || s == "single" || s == "k\u00FC\u00E7\u00FCk"
            } else false
        } else false
    }
    LaunchedEffect(orderStatus) {
        orderStatus?.let { status ->
            if (status.startsWith("Success") || status.startsWith("Sipari") || status.startsWith("Sipari\u015F")) {
                showSuccessDialog = true
                delay(2000)
                showSuccessDialog = false
                viewModel.clearOrderStatus()
                onBack()
            } else {
                snackbarHostState.showSnackbar(status)
                viewModel.clearOrderStatus()
            }
        }
    }
        if (showSuccessDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF4CAF50), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("\u2705", fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if(isEn) "Order Received!" else "Sipari\u015Finiz Al\u0131nd\u0131!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorPrimary
                    )
                }
            }
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            isGuest = currentUser == null,
            onDismiss = { showCheckoutDialog = false },
            onConfirm = { guestName, note, payment, useStars ->
                showCheckoutDialog = false
                viewModel.placeOrder(
                    guestName = guestName,
                    note = note,
                    paymentMethod = payment,
                    usedStars = useStars
                )
            },
            totalPrice = cartItems.sumOf { it.totalPrice },
            canUseStars = canUseStars
        )
    }
    Scaffold(
        containerColor = colorBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                actions = {
                    androidx.compose.material3.TextButton(onClick = { com.brewandbean.app.util.LanguageManager.setEnglish(!isEn) }) {
                        androidx.compose.material3.Text(text = if (isEn) "EN" else "TR", color = colorPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        text = if(isEn) "Your Cart" else "Sepetiniz",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colorPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = if(isEn) "Back" else "Geri",
                            tint = colorPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colorPrimary,
                    navigationIconContentColor = colorPrimary
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
    val totalPrice = cartItems.sumOf { it.totalPrice }
                Surface(
                    color = colorSurface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if(isEn) "Total" else "Toplam",
                                fontSize = 18.sp,
                                color = colorTextMuted
                            )
                            Text(
                        text = "\u20BA${totalPrice}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorPrimary
                            )
                        }
                        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                        Button(
                            onClick = { 
                                com.brewandbean.app.util.VibrationHelper.vibrate(context, 60)
                                showCheckoutDialog = true 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorAccent,
                                contentColor = colorSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = colorSurface,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if(isEn) "Place Order" else "Sipari\u015F Ver",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cartItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "\uD83D\uDED2",
                        fontSize = 80.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                            text = if(isEn) "Your cart is empty" else "Sepetiniz bo\u015F",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = if(isEn) "Return to menu and discover our flavors" else "Men\u00FCye d\u00F6n\u00FCp lezzetlerimizi ke\u015Ffedin",
                        fontSize = 16.sp,
                        color = colorTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems, key = { it.cartKey }) { item ->
                        CartItemCard(
                            item = item,
                            onUpdateQuantity = { delta ->
                                viewModel.updateQuantity(item.cartKey, delta)
                            },
                            onRemove = {
                                viewModel.removeFromCart(item.cartKey)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
            
        }
    }
}
@Composable
fun CartItemCard(
    item: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(item.product.gradientStart),
                                    Color(item.product.gradientEnd)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.product.emoji, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if(isEn) item.product.nameEn else item.product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorPrimary
                    )
                    Text(
                        text = if(isEn) "Size: ${item.size.labelEn}" else "Boy: ${item.size.label}",
                        fontSize = 12.sp,
                        color = colorTextMuted
                    )
                    Text(
                        text = "\u20BA${item.size.price}",
                        fontSize = 14.sp,
                        color = colorPrimary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(colorAccentLight, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colorSurface)
                            .clickable { 
                                com.brewandbean.app.util.VibrationHelper.vibrate(context, 30)
                                onUpdateQuantity(-1) 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = colorPrimary, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = item.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold,
                        color = colorPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colorAccent)
                            .clickable { 
                                com.brewandbean.app.util.VibrationHelper.vibrate(context, 30)
                                onUpdateQuantity(1) 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = colorSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRemove,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if(isEn) "Remove" else "Kald\u0131r",
                        color = colorDanger,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                        text = "\u20BA${item.size.price * item.quantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    color = colorPrimary
                )
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    isGuest: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (guestName: String, note: String, payment: String, useStars: Boolean) -> Unit,
    totalPrice: Int,
    canUseStars: Boolean
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    var guestName by remember { mutableStateOf("") }
    var showNameError by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("kart") }
    var useStars by remember { mutableStateOf(false) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if(isEn) "Payment Preference" else "Ödeme Tercihi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (isGuest) {
                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { 
                            guestName = it
                            showNameError = false 
                        },
                        label = { Text(if(isEn) "Full Name" else "Adınız Soyadınız") },
                        singleLine = true,
                        isError = showNameError && guestName.isBlank(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    if (showNameError && guestName.isBlank()) {
                        Text(
                            text = if(isEn) "Please enter your name" else "Lütfen adınızı girin",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                if (canUseStars) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("⭐ 10 Yıldız Kullan", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                                Text(if(isEn) "1 Small Coffee Free!" else "1 Küçük Kahve Bedava!", fontSize = 12.sp, color = Color(0xFFF57F17))
                            }
                            Switch(
                                checked = useStars,
                                onCheckedChange = { useStars = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF57F17), checkedTrackColor = Color(0xFFFFD54F))
                            )
                        }
                    }
                }
                if (!useStars) {
                    Text(if(isEn) "Payment Method" else "Ödeme Yöntemi", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentOptionCard(
                            modifier = Modifier.weight(1f),
                            title = if(isEn) "Card" else "Kart",
                            icon = "💳",
                            isSelected = paymentMethod == "kart",
                            onClick = { paymentMethod = "kart" }
                        )
                        PaymentOptionCard(
                            modifier = Modifier.weight(1f),
                            title = if(isEn) "Cash" else "Nakit",
                            icon = "💵",
                            isSelected = paymentMethod == "nakit",
                            onClick = { paymentMethod = "nakit" }
                        )
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if(isEn) "Order Note (Optional)" else "Sipariş Notu (İsteğe Bağlı)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    minLines = 2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if(useStars) (if(isEn) "Total: ₺0" else "Toplam: ₺0") else (if(isEn) "Total: ₺${totalPrice}" else "Toplam: ₺${totalPrice}"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Button(
                        onClick = {
                            if (isGuest && guestName.isBlank()) {
                                showNameError = true
                            } else {
                                onConfirm(guestName.trim(), note.trim(), if(useStars) "yildiz" else paymentMethod, useStars)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if(isEn) "Confirm" else "Onayla", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
@Composable
fun PaymentOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFEBE9) else Color(0xFFFAFAFA)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF8D6E63) else Color(0xFFEEEEEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF4E342E) else Color.Gray
            )
        }
    }
}
