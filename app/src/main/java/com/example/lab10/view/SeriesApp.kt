package com.example.lab10.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab10.data.SerieApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // La BASE_URL debe incluir el prefijo "api/"
    private const val BASE_URL = "http://10.0.2.2:8000/api/"

    val instance: SerieApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(SerieApiService::class.java)
    }
}

@Composable
fun SeriesApp() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar(navController) },
        floatingActionButton = { Fab(navController) },
        content = { paddingValues ->
            AppContent(paddingValues, navController, RetrofitClient.instance)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(text = "SERIES APP", color = Color.White, fontWeight = FontWeight.Bold)
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.List, contentDescription = "Series") },
            label = { Text("Series") },
            selected = currentRoute == "series",
            onClick = { navController.navigate("series") }
        )
    }
}

@Composable
fun Fab(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    if (currentRoute == "series") {
        FloatingActionButton(
            onClick = { navController.navigate("series/new") }
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Serie")
        }
    }
}

@Composable
fun AppContent(
    paddingValues: PaddingValues,
    navController: NavHostController,
    apiService: SerieApiService
) {
    Box(modifier = Modifier.padding(paddingValues)) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeScreen() }
            composable("series") { SeriesListScreen(navController, apiService) }
            composable("series/new") {
                SeriesEditScreen(navController, apiService, null)
            }
            composable(
                "series/edit/{serieId}",
                arguments = listOf(navArgument("serieId") { type = NavType.StringType })
            ) { backStackEntry ->
                val serieId = backStackEntry.arguments?.getString("serieId")
                SeriesEditScreen(navController, apiService, serieId)
            }
        }
    }
}