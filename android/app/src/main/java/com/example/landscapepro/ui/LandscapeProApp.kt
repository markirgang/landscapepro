package com.example.landscapepro.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.landscapepro.data.GardenState
import com.example.landscapepro.ui.tabs.GalleryTab
import com.example.landscapepro.ui.tabs.LandscaperTab
import com.example.landscapepro.ui.tabs.SetupTab

enum class AppTab(val title: String, val icon: ImageVector) {
    SETUP("Setup", Icons.Default.Settings),
    LANDSCAPER("Landscaper", Icons.Default.PlayArrow),
    GALLERY("Gallery", Icons.Default.List)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeProApp() {
    var state by remember { mutableStateOf(GardenState()) }
    var currentTab by remember { mutableStateOf(AppTab.SETUP) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LandscapePro AI") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentTab) {
            AppTab.SETUP -> SetupTab(
                state = state,
                onStateChanged = { state = it },
                modifier = modifier
            )
            AppTab.LANDSCAPER -> LandscaperTab(
                state = state,
                onStateChanged = { state = it },
                modifier = modifier
            )
            AppTab.GALLERY -> GalleryTab(
                state = state,
                onStateChanged = { state = it },
                modifier = modifier
            )
        }
    }
}
