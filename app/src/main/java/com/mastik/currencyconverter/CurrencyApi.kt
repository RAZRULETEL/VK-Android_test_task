package com.mastik.currencyconverter

import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {
    @GET("latest")
    fun getRates(
        @Query("apikey") apiKey: String,
        @Query("currencies") currencies: String,
        @Query("base_currency") baseCurrency: String
    ): Single<CurrencyResponse>
}
