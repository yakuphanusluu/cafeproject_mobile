package com.brewandbean.app.data.model

import com.google.gson.annotations.SerializedName

// API responses
data class CanPlayResponse(
    @SerializedName("can_play") val canPlay: Boolean,
    @SerializedName("star_dust") val starDust: Int
)

data class SubmitScoreResponse(
    val success: Boolean,
    val earned: Int,
    @SerializedName("total_star_dust") val totalStarDust: Int
)

data class ConvertDustResponse(
    val success: Boolean,
    @SerializedName("stars_added") val starsAdded: Int,
    @SerializedName("remaining_dust") val remainingDust: Int,
    @SerializedName("total_stars") val totalStars: Int
)

data class SubmitScoreRequest(
    val token: String,
    val score: Int
)

data class ConvertDustRequest(
    val token: String,
    val amount: Int = 100
)

// YENİ: Hafıza Oyunu Modelleri
data class MemoryCard(
    val id: Int,
    val emoji: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)
