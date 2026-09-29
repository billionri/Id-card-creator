package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CardGeneratorScreen
import com.example.ui.screens.HubInfoScreen
import com.example.ui.screens.SavedCardsScreen
import com.example.ui.theme.DtdcGold
import com.example.ui.theme.DtdcNavy
import com.example.ui.theme.DtdcRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.IdCardViewModel

enum class AppNavTab(val title: String) {
    BUILDER("ID Builder"),
    SAVED("Saved Cards"),
    HUB("Hub Info")
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: IdCardViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                var currentTab by remember { mutableStateOf(AppNavTab.BUILDER) }

                BackHandler(enabled = currentTab != AppNavTab.BUILDER) {
                    currentTab = AppNavTab.BUILDER
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "DTDC • Hariom Enterprises",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = DtdcNavy,
                                titleContentColor = Color.White
                            ),
                            actions = {}
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppNavTab.BUILDER,
                                onClick = { currentTab = AppNavTab.BUILDER },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = "Builder",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Builder") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DtdcRed,
                                    selectedTextColor = DtdcNavy,
                                    indicatorColor = Color(0xFFFFEBEE)
                                ),
                                modifier = Modifier.testTag("nav_tab_builder")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavTab.SAVED,
                                onClick = { currentTab = AppNavTab.SAVED },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FolderShared,
                                        contentDescription = "Saved Cards",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Saved Cards") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DtdcRed,
                                    selectedTextColor = DtdcNavy,
                                    indicatorColor = Color(0xFFFFEBEE)
                                ),
                                modifier = Modifier.testTag("nav_tab_saved")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppNavTab.HUB,
                                onClick = { currentTab = AppNavTab.HUB },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = "Hub Info",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("Hub Info") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DtdcRed,
                                    selectedTextColor = DtdcNavy,
                                    indicatorColor = Color(0xFFFFEBEE)
                                ),
                                modifier = Modifier.testTag("nav_tab_hub")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentTab) {
                        AppNavTab.BUILDER -> {
                            CardGeneratorScreen(
                                viewModel = viewModel,
                                snackbarHostState = snackbarHostState,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppNavTab.SAVED -> {
                            SavedCardsScreen(
                                viewModel = viewModel,
                                onEditCard = { currentTab = AppNavTab.BUILDER },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppNavTab.HUB -> {
                            HubInfoScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
