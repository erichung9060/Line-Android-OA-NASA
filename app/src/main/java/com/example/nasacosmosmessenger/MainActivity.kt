package com.example.nasacosmosmessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nasacosmosmessenger.presentation.MainScreen
import com.example.nasacosmosmessenger.ui.theme.CosmosMessengerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CosmosMessengerTheme {
                MainScreen()
            }
        }
    }
}
