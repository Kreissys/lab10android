package com.example.lab10.data.product

import com.google.gson.annotations.SerializedName

data class ProductModel(
    @SerializedName("id")
    var id: Int,
    @SerializedName("title")
    var title: String,
    @SerializedName("description")
    var description: String,
    @SerializedName("price")
    var price: Double
)

data class ProductListModel(
    @SerializedName("products")
    var products: List<ProductModel>
)