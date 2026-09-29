package com.brewandbean.app.data.repository

import com.brewandbean.app.data.api.BrewBeanApi
import com.brewandbean.app.data.model.OrderRequest
import com.brewandbean.app.data.model.Product
import com.brewandbean.app.data.model.ProductSize
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor(
    private val api: BrewBeanApi
) {

    companion object MenuData {
        val allProducts: List<Product> = listOf(
            Product(
                id = 1,
                name = "Türk Kahvesi",
                desc = "Geleneksel yöntemlerle pişirilen otantik Türk kahvesi",
                nameEn = "Turkish Coffee",
                descEn = "Authentic Turkish coffee brewed with traditional methods",
                category = "sicak-kahve",
                categoryLabel = "Sıcak Kahve", categoryLabelEn = "Hot Coffee",
                emoji = "☕",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/b/b5/T%C3%BCrk_Kahvesi_-_Bakir_Cezve.jpg",
                gradientStart = 0xFF3E2723,
                gradientEnd = 0xFF5D4037,
                sizes = listOf(ProductSize("Tek", "Single", 45), ProductSize("Çift", "Double", 65))
            ),
            Product(
                id = 2,
                name = "Espresso",
                desc = "Yoğun ve güçlü tek atımlık İtalyan klasiği",
                nameEn = "Espresso",
                descEn = "Intense and strong single-shot Italian classic",
                category = "sicak-kahve",
                categoryLabel = "Sıcak Kahve", categoryLabelEn = "Hot Coffee",
                emoji = "⚡",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/a/a5/Tazzina_di_caff%C3%A8_a_Ventimiglia.jpg",
                gradientStart = 0xFF1B0000,
                gradientEnd = 0xFF4E342E,
                sizes = listOf(ProductSize("Single", "Single", 40), ProductSize("Double", "Double", 55), ProductSize("Triple", "Triple", 70))
            ),
            Product(
                id = 3,
                name = "Cappuccino",
                desc = "Kremsi süt köpüğü ile dengelenmiş espresso",
                nameEn = "Cappuccino",
                descEn = "Espresso balanced with creamy milk foam",
                category = "sicak-kahve",
                categoryLabel = "Sıcak Kahve", categoryLabelEn = "Hot Coffee",
                emoji = "🤎",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/7/70/Cappuccino_in_original.jpg",
                gradientStart = 0xFF4E342E,
                gradientEnd = 0xFF8D6E63,
                sizes = listOf(ProductSize("S", "S", 55), ProductSize("M", "M", 65), ProductSize("L", "L", 75))
            ),
            Product(
                id = 4,
                name = "Latte",
                desc = "Yumuşak süt ile buluşan hafif kahve lezzeti",
                nameEn = "Latte",
                descEn = "Light coffee flavor meeting smooth milk",
                category = "sicak-kahve",
                categoryLabel = "Sıcak Kahve", categoryLabelEn = "Hot Coffee",
                emoji = "🥛",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/98/Latte_with_winged_tulip_art.jpg",
                gradientStart = 0xFF6D4C41,
                gradientEnd = 0xFFA1887F,
                sizes = listOf(ProductSize("S", "S", 55), ProductSize("M", "M", 65), ProductSize("L", "L", 75))
            ),
            Product(
                id = 5,
                name = "Americano",
                desc = "Sıcak su ile uzatılmış espresso — sade ve temiz",
                nameEn = "Americano",
                descEn = "Espresso extended with hot water — simple and clean",
                category = "sicak-kahve",
                categoryLabel = "Sıcak Kahve", categoryLabelEn = "Hot Coffee",
                emoji = "🇺🇸",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/4/41/Espresso_Americano.jpeg",
                gradientStart = 0xFF212121,
                gradientEnd = 0xFF616161,
                sizes = listOf(ProductSize("S", "S", 45), ProductSize("M", "M", 55), ProductSize("L", "L", 65))
            ),
            Product(
                id = 6,
                name = "Flat White",
                desc = "Avustralya usulü, kadifemsi mikro köpüklü kahve",
                nameEn = "Flat White",
                descEn = "Australian style coffee with velvety micro-foam",
                category = "sicak-kahve",
                categoryLabel = "Sıcak Kahve", categoryLabelEn = "Hot Coffee",
                emoji = "🍵",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/6/6b/Flat_white_coffee_with_pretty_feather_pattern.jpg",
                gradientStart = 0xFF5D4037,
                gradientEnd = 0xFFBCAAA4,
                sizes = listOf(ProductSize("S", "S", 60), ProductSize("M", "M", 70))
            ),
            Product(
                id = 7,
                name = "Iced Latte",
                desc = "Buz gibi soğuk süt ve espresso birleşimi",
                nameEn = "Iced Latte",
                descEn = "Ice-cold milk combined with espresso",
                category = "soguk-kahve",
                categoryLabel = "Soğuk Kahve", categoryLabelEn = "Cold Coffee",
                emoji = "🧊",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/8/8d/Iced_latte.jpg",
                gradientStart = 0xFF81D4FA,
                gradientEnd = 0xFFB3E5FC,
                sizes = listOf(ProductSize("M", "M", 65), ProductSize("L", "L", 75))
            ),
            Product(
                id = 8,
                name = "Cold Brew",
                desc = "18 saat soğuk demleme yöntemiyle hazırlanan özel kahve",
                nameEn = "Cold Brew",
                descEn = "Special coffee prepared with 18-hour cold brew method",
                category = "soguk-kahve",
                categoryLabel = "Soğuk Kahve", categoryLabelEn = "Cold Coffee",
                emoji = "💧",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/2/2c/Ice_coffee_image.jpg",
                gradientStart = 0xFF0D47A1,
                gradientEnd = 0xFF42A5F5,
                sizes = listOf(ProductSize("M", "M", 70), ProductSize("L", "L", 85))
            ),
            Product(
                id = 9,
                name = "Frappuccino",
                desc = "Buzlu karışık kahve, krema ve çikolata soslu",
                nameEn = "Frappuccino",
                descEn = "Iced blended coffee with cream and chocolate sauce",
                category = "soguk-kahve",
                categoryLabel = "Soğuk Kahve", categoryLabelEn = "Cold Coffee",
                emoji = "🥤",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/2/2c/Strawberry_Delight_Frappuccino.JPG",
                gradientStart = 0xFF4E342E,
                gradientEnd = 0xFFA1887F,
                sizes = listOf(ProductSize("M", "M", 75), ProductSize("L", "L", 90))
            ),
            Product(
                id = 10,
                name = "Iced Americano",
                desc = "Sade ve ferahlatıcı buzlu americano",
                nameEn = "Iced Americano",
                descEn = "Simple and refreshing iced americano",
                category = "soguk-kahve",
                categoryLabel = "Soğuk Kahve", categoryLabelEn = "Cold Coffee",
                emoji = "❄️",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/b/b9/Espresso_and_tonic%2C_COFFEE_CITEN%2C_Akiha%2C_Niigata%2C_Niigata%2C_Japan%2C_May_2023.jpg",
                gradientStart = 0xFF37474F,
                gradientEnd = 0xFF78909C,
                sizes = listOf(ProductSize("M", "M", 55), ProductSize("L", "L", 65))
            ),
            Product(
                id = 11,
                name = "Caramel Macchiato",
                desc = "Karamel sos, vanilya şurubu ve sütlü espresso",
                nameEn = "Caramel Macchiato",
                descEn = "Caramel sauce, vanilla syrup, and milky espresso",
                category = "ozel-kahve",
                categoryLabel = "Özel Kahve", categoryLabelEn = "Specialty Coffee",
                emoji = "🍯",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/6/61/Latte_macchiato_with_coffee_beans.jpg",
                gradientStart = 0xFFF57F17,
                gradientEnd = 0xFFFFB74D,
                sizes = listOf(ProductSize("M", "M", 80), ProductSize("L", "L", 95))
            ),
            Product(
                id = 12,
                name = "Mocha",
                desc = "Çikolata ve espressonun muhteşem uyumu",
                nameEn = "Mocha",
                descEn = "The magnificent harmony of chocolate and espresso",
                category = "ozel-kahve",
                categoryLabel = "Özel Kahve", categoryLabelEn = "Specialty Coffee",
                emoji = "🍫",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/7/7e/Mocha_coffee.jpg",
                gradientStart = 0xFF3E2723,
                gradientEnd = 0xFF795548,
                sizes = listOf(ProductSize("M", "M", 75), ProductSize("L", "L", 90))
            ),
            Product(
                id = 13,
                name = "Lavanta Latte",
                desc = "Lavanta çiçeği özütü ile aromatik latte",
                nameEn = "Lavender Latte",
                descEn = "Aromatic latte with lavender flower extract",
                category = "ozel-kahve",
                categoryLabel = "Özel Kahve", categoryLabelEn = "Specialty Coffee",
                emoji = "💜",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/98/Latte_with_winged_tulip_art.jpg",
                gradientStart = 0xFF7B1FA2,
                gradientEnd = 0xFFCE93D8,
                sizes = listOf(ProductSize("M", "M", 85), ProductSize("L", "L", 100))
            ),
            Product(
                id = 14,
                name = "Matcha Latte",
                desc = "Japon usulü yeşil çay tozu ile hazırlanan latte",
                nameEn = "Matcha Latte",
                descEn = "Latte prepared with Japanese style green tea powder",
                category = "ozel-kahve",
                categoryLabel = "Özel Kahve", categoryLabelEn = "Specialty Coffee",
                emoji = "🍵",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d9/Matcha_Scoop.jpg",
                gradientStart = 0xFF2E7D32,
                gradientEnd = 0xFF81C784,
                sizes = listOf(ProductSize("M", "M", 80), ProductSize("L", "L", 95))
            ),
            Product(
                id = 15,
                name = "Affogato",
                desc = "Sıcak espresso üzerine vanilyalı dondurma",
                nameEn = "Affogato",
                descEn = "Vanilla ice cream topped with hot espresso",
                category = "ozel-kahve",
                categoryLabel = "Özel Kahve", categoryLabelEn = "Specialty Coffee",
                emoji = "🍨",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/1/17/Vinoteca%2C_Smithfield%2C_London_%284485849609%29.jpg",
                gradientStart = 0xFF4E342E,
                gradientEnd = 0xFFD7CCC8,
                sizes = listOf(ProductSize("Tek", "Single", 75), ProductSize("Çift", "Double", 110))
            ),
            Product(
                id = 16,
                name = "Tiramisu",
                desc = "İtalyan usulü mascarpone kremalı kahveli tatlı",
                nameEn = "Tiramisu",
                descEn = "Italian style coffee dessert with mascarpone cream",
                category = "tatli",
                categoryLabel = "Tatlı", categoryLabelEn = "Dessert",
                emoji = "🍰",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/5/58/Tiramisu_-_Raffaele_Diomede.jpg",
                gradientStart = 0xFFD7CCC8,
                gradientEnd = 0xFFEFEBE9,
                sizes = listOf(ProductSize("Dilim", "Slice", 90))
            ),
            Product(
                id = 17,
                name = "Cheesecake",
                desc = "New York usulü kremalı cheesecake",
                nameEn = "Cheesecake",
                descEn = "New York style creamy cheesecake",
                category = "tatli",
                categoryLabel = "Tatlı", categoryLabelEn = "Dessert",
                emoji = "🧁",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/e/ea/Baked_cheesecake_with_raspberries_and_blueberries.jpg",
                gradientStart = 0xFFFFF9C4,
                gradientEnd = 0xFFFFFDE7,
                sizes = listOf(ProductSize("Dilim", "Slice", 85))
            ),
            Product(
                id = 18,
                name = "Brownie",
                desc = "Yoğun çikolatalı, fındıklı sıcak brownie",
                nameEn = "Brownie",
                descEn = "Warm brownie with intense chocolate and hazelnuts",
                category = "tatli",
                categoryLabel = "Tatlı", categoryLabelEn = "Dessert",
                emoji = "🍫",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/6/68/Chocolatebrownie.JPG",
                gradientStart = 0xFF3E2723,
                gradientEnd = 0xFF6D4C41,
                sizes = listOf(ProductSize("Tek", "Single", 65), ProductSize("A la Mode", "A la Mode", 85))
            ),
            Product(
                id = 19,
                name = "Croissant",
                desc = "Tereyağlı, kat kat açılmış Fransız kruvasanı",
                nameEn = "Croissant",
                descEn = "Buttery, flaky French croissant",
                category = "atistirmalik",
                categoryLabel = "Atıştırmalık", categoryLabelEn = "Snack",
                emoji = "🥐",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/2/2a/Croissant-Petr_Kratochvil.jpg",
                gradientStart = 0xFFF9A825,
                gradientEnd = 0xFFFDD835,
                sizes = listOf(ProductSize("Sade", "Plain", 45), ProductSize("Çikolatalı", "Chocolate", 55))
            ),
            Product(
                id = 20,
                name = "Sandviç",
                desc = "Taze sebzeler ve peynirli ev yapımı sandviç",
                nameEn = "Sandwich",
                descEn = "Homemade sandwich with fresh vegetables and cheese",
                category = "atistirmalik",
                categoryLabel = "Atıştırmalık", categoryLabelEn = "Snack",
                emoji = "🥪",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/0b/Bacon%2C_lettuce%2C_tomato%2C_and_avocado.jpg",
                gradientStart = 0xFF8BC34A,
                gradientEnd = 0xFFC5E1A5,
                sizes = listOf(ProductSize("Normal", "Regular", 70), ProductSize("Büyük", "Large", 90))
            ),
            Product(
                id = 21,
                name = "Cookie",
                desc = "Damla çikolatalı yumuşak kurabiye",
                nameEn = "Cookie",
                descEn = "Soft cookie with chocolate chips",
                category = "atistirmalik",
                categoryLabel = "Atıştırmalık", categoryLabelEn = "Snack",
                emoji = "🍪",
                imageUrl = "https://upload.wikimedia.org/wikipedia/commons/8/8e/ChocChip.jpg",
                gradientStart = 0xFFD7A86E,
                gradientEnd = 0xFFF0D9B5,
                sizes = listOf(ProductSize("Tek", "Single", 30), ProductSize("3'lü", "3-Pack", 75))
            )
        )
    }

    fun getCategories(): List<Pair<String, String>> {
        return listOf(
            Pair("all", "Tümü|All"),
            Pair("sicak-kahve", "Sıcak Kahveler|Hot Coffees"),
            Pair("soguk-kahve", "Soğuk Kahveler|Cold Coffees"),
            Pair("ozel-kahve", "Özel Kahveler|Specialty Coffees"),
            Pair("tatli", "Tatlılar|Desserts"),
            Pair("atistirmalik", "Atıştırmalık|Snacks")
        )
    }

    fun getProducts(category: String): List<Product> {
        return if (category == "all") {
            allProducts
        } else {
            allProducts.filter { it.category == category }
        }
    }

    suspend fun placeOrder(orderRequest: OrderRequest) = api.createOrder(orderRequest)

    suspend fun getOrderStatus(customerToken: String, orderNo: String) = api.getOrderStatus(customerToken, orderNo)

    suspend fun getCafeLocation() = api.getCafeLocation()
}


