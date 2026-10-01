package com.brewandbean.app.data.repository

import com.brewandbean.app.data.api.BrewBeanApi
import com.brewandbean.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val api: BrewBeanApi
) {
    suspend fun canPlay(token: String): CanPlayResponse {
        return api.canPlay(token = token)
    }

    suspend fun submitScore(token: String, score: Int): SubmitScoreResponse {
        return api.submitScore(SubmitScoreRequest(token, score))
    }

    suspend fun convertStarDust(token: String, amount: Int = 100): ConvertDustResponse {
        return api.convertStarDust(ConvertDustRequest(token, amount))
    }
}
