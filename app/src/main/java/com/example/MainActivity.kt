package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.TvViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val tvViewModel: TvViewModel = viewModel()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(navController = navController, viewModel = tvViewModel)
                        }
                        
                        composable("intro") {
                            IntroScreen(navController = navController, viewModel = tvViewModel)
                        }

                        composable("country_selection") {
                            CountrySelectionScreen(navController = navController, viewModel = tvViewModel)
                        }
                        
                        composable("home") {
                            HomeScreen(navController = navController, viewModel = tvViewModel)
                        }
                        
                        composable(
                            "channels?category={category}",
                            arguments = listOf(navArgument("category") { defaultValue = null; type = NavType.StringType; nullable = true })
                        ) { backStackEntry ->
                            val category = backStackEntry.arguments?.getString("category")
                            LiveChannelsScreen(
                                navController = navController,
                                viewModel = tvViewModel,
                                initialCategory = category
                            )
                        }

                        // Alias route for raw channels screen
                        composable("channels") {
                            LiveChannelsScreen(
                                navController = navController,
                                viewModel = tvViewModel,
                                initialCategory = null
                            )
                        }
                        
                        composable(
                            "player/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val channelId = backStackEntry.arguments?.getString("id") ?: ""
                            ChannelPlayerScreen(
                                navController = navController,
                                viewModel = tvViewModel,
                                channelId = channelId
                            )
                        }
                        
                        composable("movies") {
                            MoviesScreen(navController = navController, viewModel = tvViewModel)
                        }
                        
                        composable(
                            "movie_detail/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val movieId = backStackEntry.arguments?.getString("id") ?: ""
                            MovieDetailScreen(
                                navController = navController,
                                viewModel = tvViewModel,
                                movieId = movieId
                            )
                        }

                        composable(
                            "show_detail/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val showId = backStackEntry.arguments?.getString("id") ?: ""
                            ShowDetailScreen(
                                navController = navController,
                                viewModel = tvViewModel,
                                showId = showId
                            )
                        }
                        
                        composable("trailers") {
                            TrailersScreen(navController = navController, viewModel = tvViewModel)
                        }
                        
                        composable("search") {
                            SearchScreen(navController = navController, viewModel = tvViewModel)
                        }

                        composable("my_list") {
                            MyListScreen(navController = navController, viewModel = tvViewModel)
                        }
                        
                        composable("settings") {
                            SettingsScreen(navController = navController, viewModel = tvViewModel)
                        }
                    }
                }
            }
        }
    }
}
