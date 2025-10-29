package com.example.lab10.view

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
import com.example.lab10.data.SerieApiService
import com.example.lab10.data.SerieModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Welcome to the Series App", fontSize = 24.sp)
    }
}

@Composable
fun SeriesListScreen(navController: NavHostController, apiService: SerieApiService) {
    val seriesList = remember { mutableStateListOf<SerieModel>() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

    fun fetchSeries() {
        scope.launch {
            try {
                val response = apiService.getSeries()
                seriesList.clear()
                seriesList.addAll(response)
            } catch (e: Exception) {
                Log.e("SeriesListScreen", "Error fetching series", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchSeries()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ID", modifier = Modifier.weight(0.15f), fontWeight = FontWeight.Bold)
                Text("Name", modifier = Modifier.weight(0.55f), fontWeight = FontWeight.Bold)
                Text("Actions", modifier = Modifier.weight(0.3f), fontWeight = FontWeight.Bold)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        items(seriesList) { serie ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("series/edit/${serie.id}") }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(serie.id.toString(), modifier = Modifier.weight(0.15f))
                Text(serie.name, modifier = Modifier.weight(0.55f))
                Row(modifier = Modifier.weight(0.3f)) {
                    IconButton(onClick = { navController.navigate("series/edit/${serie.id}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = serie.id }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { serieId ->
        DeleteConfirmationDialog(
            onConfirm = {
                scope.launch {
                    try {
                        apiService.deleteSerie(serieId.toString())
                        fetchSeries()
                    } catch (e: Exception) {
                        Log.e("SeriesListScreen", "Error deleting serie", e)
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
fun SeriesEditScreen(navController: NavHostController, apiService: SerieApiService, serieId: String?) {
    var name by remember { mutableStateOf("") }
    var releaseDate by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val isNewSerie = serieId == null
    val scope = rememberCoroutineScope()

    if (!isNewSerie) {
        LaunchedEffect(serieId) {
            scope.launch {
                try {
                    val response = apiService.getSerie(serieId!!)
                    response.body()?.let {
                        name = it.name
                        releaseDate = it.release_date
                        rating = it.rating.toString()
                        category = it.category
                    }
                } catch (e: Exception) {
                    Log.e("SeriesEditScreen", "Error fetching serie details", e)
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
        Text(if (isNewSerie) "Add New Serie" else "Edit Serie", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(value = releaseDate, onValueChange = { releaseDate = it }, label = { Text("Release Date (YYYY-MM-DD)") })
        OutlinedTextField(value = rating, onValueChange = { rating = it }, label = { Text("Rating") })
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
        Button(onClick = {
            scope.launch {
                try {
                    val serie = SerieModel(
                        id = serieId?.toInt() ?: 0,
                        name = name,
                        release_date = releaseDate,
                        rating = rating.toIntOrNull() ?: 0,
                        category = category
                    )
                    if (isNewSerie) {
                        apiService.insertSerie(serie)
                    } else {
                        apiService.updateSerie(serieId!!, serie)
                    }
                    navController.popBackStack()
                } catch (e: Exception) {
                    Log.e("SeriesEditScreen", "Error saving serie", e)
                }
            }
        }) {
            Text("Save")
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete this item?") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}