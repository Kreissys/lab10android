package com.example.lab10.view.product

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.product.ProductApiService
import com.example.lab10.data.product.ProductModel
import com.example.lab10.view.DeleteConfirmationDialog
import kotlinx.coroutines.launch

@Composable
fun ProductListScreen(navController: NavHostController, apiService: ProductApiService) {
    val productList = remember { mutableStateListOf<ProductModel>() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

    fun fetchProducts() {
        scope.launch {
            try {
                val response = apiService.getProducts()
                productList.clear()
                productList.addAll(response.products)
            } catch (e: Exception) {
                Log.e("ProductListScreen", "Error fetching products", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchProducts()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ID", modifier = Modifier.weight(0.15f), fontWeight = FontWeight.Bold)
                Text("Title", modifier = Modifier.weight(0.55f), fontWeight = FontWeight.Bold)
                Text("Actions", modifier = Modifier.weight(0.3f), fontWeight = FontWeight.Bold)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(productList) { product ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("products/edit/${product.id}") }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(product.id.toString(), modifier = Modifier.weight(0.15f))
                Text(product.title, modifier = Modifier.weight(0.55f))
                Row(modifier = Modifier.weight(0.3f)) {
                    IconButton(onClick = { navController.navigate("products/edit/${product.id}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = product.id }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { productId ->
        DeleteConfirmationDialog(
            onConfirm = {
                scope.launch {
                    try {
                        apiService.deleteProduct(productId.toString())
                        fetchProducts()
                    } catch (e: Exception) {
                        Log.e("ProductListScreen", "Error deleting product", e)
                    } finally {
                        showDeleteDialog = null
                    }
                }
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
fun ProductEditScreen(navController: NavHostController, apiService: ProductApiService, productId: String?) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    val isNewProduct = productId == null
    val scope = rememberCoroutineScope()

    if (!isNewProduct) {
        LaunchedEffect(productId) {
            scope.launch {
                try {
                    val response = apiService.getProduct(productId!!)
                    response.body()?.let {
                        title = it.title
                        description = it.description
                        price = it.price.toString()
                    }
                } catch (e: Exception) {
                    Log.e("ProductEditScreen", "Error fetching product details", e)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (isNewProduct) "Add New Product" else "Edit Product", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
        Button(onClick = {
            scope.launch {
                try {
                    val product = ProductModel(
                        id = productId?.toInt() ?: 0,
                        title = title,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0
                    )
                    if (isNewProduct) {
                        apiService.insertProduct(product)
                    } else {
                        apiService.updateProduct(productId!!, product)
                    }
                    navController.popBackStack()
                } catch (e: Exception) {
                    Log.e("ProductEditScreen", "Error saving product", e)
                }
            }
        }) {
            Text("Save")
        }
    }
}