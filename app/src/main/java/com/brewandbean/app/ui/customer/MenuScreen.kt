package com.brewandbean.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.brewandbean.app.util.LanguageManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.brewandbean.app.data.model.Product
import com.brewandbean.app.data.model.CartItem

val PrimaryColor = Color(0xFF1A1A2E)
val AccentColor = Color(0xFFC8956C)
val AccentLightColor = Color(0xFFF5EBE0)
val BackgroundColor = Color(0xFFFAF8F5)
val SurfaceColor = Color(0xFFFFFFFF)
val TextMutedColor = Color(0xFF7A7A7A)
val BorderColor = Color(0xFFE8E0D8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: CustomerViewModel,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    val authViewModel: com.brewandbean.app.ui.auth.AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(null)
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val categories = viewModel.categories

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refreshStars()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val selectedSizes = remember { mutableStateMapOf<Int, Int>() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "\u2615 Brew & Bean",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    TextButton(
                        onClick = { com.brewandbean.app.util.LanguageManager.setEnglish(!isEn) },
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 36.dp)
                    ) {
                        Text(text = if (isEn) "EN" else "TR", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    if (currentUser != null) {
                        if (currentUser!!.stars > 0) {
                            Surface(
                                color = Color(0xFFFFF9C4),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(end = 4.dp).height(32.dp).align(Alignment.CenterVertically)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text("\u2B50 ${currentUser!!.stars}", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontSize = 14.sp)
                                }
                            }
                        }
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = if(isEn) "Profile" else "Profil",
                                tint = PrimaryColor
                            )
                        }
                    } else {
                        TextButton(onClick = onProfileClick) {
                            Text(if(isEn) "Log In" else "Giri\u015F Yap", color = PrimaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgedBox(
                        badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge(
                                    containerColor = AccentColor,
                                    contentColor = Color.White
                                ) {
                                    Text(cartItems.sumOf { it.quantity }.toString())
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onCartClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = PrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentColor)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Hero Section
                item(span = { GridItemSpan(2) }) {
                    HeroSection()
                }

                if (currentUser == null) {
                    item(span = { GridItemSpan(2) }) {
                        GuestAdCard(onLoginClick = onProfileClick)
                    }
                }

                // Filter Chips
                item(span = { GridItemSpan(2) }) {
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    CategoryFilter(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            viewModel.setCategory(it) 
                        }
                    )
                }

                // Section Header
                item(span = { GridItemSpan(2) }) {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(
                            text = if(isEn) "OUR MENU" else "MEN\u00DCM\u00DCZ",
                            color = AccentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if(isEn) "Discover Our Flavors" else "Lezzetlerimizi Ke\u015Ffedin",
                            fontFamily = FontFamily.Serif,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Normal,
                            color = PrimaryColor
                        )
                    }
                }

                // Product Grid
                items(products) { product ->
                    val sizeIndex = selectedSizes[product.id] ?: 0
                    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                    ProductCard(
                        product = product,
                        selectedSizeIndex = sizeIndex,
                        onSizeSelected = { index ->
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            selectedSizes[product.id] = index
                        },
                        onAddToCart = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.addToCart(product, sizeIndex)
                            val sizeName = product.sizes.getOrNull(sizeIndex)?.label ?: ""
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if(isEn) "\u2705 ${product.nameEn} ($sizeName) added to cart!" else "\u2705 ${product.name} ($sizeName) sepete eklendi!"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HeroSection() {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryColor, AccentColor)
                )
            )
            .padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "\u2014 EST. 2024 \u2014",
                color = AccentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if(isEn) "Every Sip" else "Her Yudumda",
                color = Color.White,
                fontFamily = FontFamily.Serif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = if(isEn) "A Story" else "Bir Hikaye",
                color = AccentColor,
                fontFamily = FontFamily.Serif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if(isEn) "Handcrafted coffees, fresh pastries, and a warm atmosphere." else "El yap\u0131m\u0131 kahveler, taze unlu mamuller ve s\u0131cak bir atmosfer.",
                color = AccentLightColor,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CategoryFilter(
    categories: List<Pair<String, String>>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.first == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) AccentColor else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else BorderColor,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onCategorySelected(category.first) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if(isEn) category.second.split("|").last() else category.second.split("|").first(),
                    color = if (isSelected) Color.White else TextMutedColor,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    selectedSizeIndex: Int,
    onSizeSelected: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Top Section with Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFE8E0D8))
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val imageRequest = coil.request.ImageRequest.Builder(context)
                        .data(product.imageUrl)
                        .addHeader("User-Agent", "BrewAndBean/1.0 (https://brewandbean.com)")
                        .crossfade(true)
                        .build()
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = if(isEn) product.nameEn else product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colors = listOf(Color(product.gradientStart), Color(product.gradientEnd))
                            )
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = product.emoji, fontSize = 64.sp)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if(isEn) product.categoryLabelEn else product.categoryLabel.uppercase(),
                    color = AccentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if(isEn) product.nameEn else product.name,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if(isEn) product.descEn else product.desc,
                    color = TextMutedColor,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Sizes
                if (product.sizes.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        product.sizes.forEachIndexed { index, size ->
                            val isSelected = index == selectedSizeIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) AccentColor else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else BorderColor,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable { onSizeSelected(index) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if(isEn) size.labelEn else size.label,
                                    color = if (isSelected) Color.White else TextMutedColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(34.dp)) // Maintain height if no sizes
                }

                // Footer (Price & Add Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val price = product.sizes.getOrNull(selectedSizeIndex)?.price ?: product.sizes.first().price
                    Text(
                        text = "\u20BA$price",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryColor
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(AccentColor)
                            .clickable { onAddToCart() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to Cart",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuestAdCard(onLoginClick: () -> Unit) {
    val isEn by com.brewandbean.app.util.LanguageManager.isEnglish.collectAsState()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onLoginClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFB74D), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("\u2B50", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if(isEn) "Win Free Coffee!" else "Bedava Kahve Kazan!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if(isEn) "Log in, win 1 free small coffee for every 10 orders. Join now!" else "Giri\u015F yap, her 10 sipari\u015Fte 1 k\u00FC\u00E7\u00FCk boy kahve bedava kazan. Hemen kat\u0131l!",
                    color = Color(0xFFF57C00),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
