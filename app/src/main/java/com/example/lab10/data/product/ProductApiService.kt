package com.example.lab10.data.product

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductApiService {
    @GET("products")
    suspend fun getProducts(): ProductListModel

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<ProductModel>

    @Headers("Content-Type: application/json")
    @POST("products/add")
    suspend fun insertProduct(@Body product: ProductModel): Response<ProductModel>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: ProductModel): Response<ProductModel>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<ProductModel>
}