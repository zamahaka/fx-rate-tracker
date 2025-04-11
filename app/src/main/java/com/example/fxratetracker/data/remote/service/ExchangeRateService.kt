package com.example.fxratetracker.data.remote.service

import com.example.fxratetracker.data.remote.model.AssetsResponse
import com.example.fxratetracker.data.remote.model.FxRatesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateService {

    @GET("list")
    suspend fun getAssets(): AssetsResponse

    @GET("live")
    suspend fun getFxRates(
        @Query("currencies") currencies: String,
    ): FxRatesResponse

}