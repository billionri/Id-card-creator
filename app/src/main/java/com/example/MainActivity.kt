package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.CardGeneratorScreen
import com.example.ui.screens.SavedCardsScreen
import com.example.ui.theme.HariomIDCardTheme
import com.example.ui.viewmodel.IdCardViewModel

enum class Screen {
    GENERATOR,
    SAVED
}

class MainActivity : ComponentActivity() {

    private val viewModel: IdCardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HariomIDCardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: IdCardViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.GENERATOR) }

    when (currentScreen) {
        Screen.GENERATOR -> {
            CardGeneratorScreen(
                viewModel = viewModel,
                onNavigateToSaved = { currentScreen = Screen.SAVED }
            )
        }
        Screen.SAVED -> {
            SavedCardsScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = Screen.GENERATOR }
            )
        }
    }
}
