package com.example.lab10.data.product

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ProductRetrofitClient {
    private const val BASE_URL = "https://dummyjson.com/"

    val instance: ProductApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ProductApiService::class.java)
    }
}