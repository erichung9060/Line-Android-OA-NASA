package com.example.nasacosmosmessenger.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nasacosmosmessenger.presentation.chat.ChatScreen
import com.example.nasacosmosmessenger.presentation.favorites.FavoritesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NovaRoute,
        modifier = modifier
    ) {
        composable<NovaRoute> {
            ChatScreen()
        }

        composable<FavoritesRoute> {
            FavoritesScreen()
        }
    }
}
