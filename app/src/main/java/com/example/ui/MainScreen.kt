package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AppsActionsScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodingAgentScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveVoiceScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.ProjectsTasksScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WebSearchScreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.Screen

data class NavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(viewModel: AssistantViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button on sub-screens
    BackHandler(enabled = currentScreen != Screen.HOME) {
        viewModel.navigateBack()
    }

    val navItems = listOf(
        NavItem(Screen.HOME, "Home", Icons.Default.Home),
        NavItem(Screen.CHAT, "Chat", Icons.Default.ChatBubble),
        NavItem(Screen.LIVE_VOICE, "Live", Icons.Default.Mic),
        NavItem(Screen.CODING_AGENT, "Code", Icons.Default.Code),
        NavItem(Screen.SETTINGS, "Settings", Icons.Default.Settings)
    )

    val showBottomBar = currentScreen in listOf(
        Screen.HOME,
        Screen.CHAT,
        Screen.LIVE_VOICE,
        Screen.CODING_AGENT,
        Screen.SETTINGS
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF090D1C),
                    contentColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (isSelected) NeonPink else Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    color = if (isSelected) Color.White else Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonPink.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(viewModel = viewModel)
                    Screen.CHAT -> ChatScreen(viewModel = viewModel)
                    Screen.LIVE_VOICE -> LiveVoiceScreen(viewModel = viewModel)
                    Screen.CODING_AGENT -> CodingAgentScreen(viewModel = viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    Screen.APPS_ACTIONS -> AppsActionsScreen(viewModel = viewModel)
                    Screen.MEMORY -> MemoryScreen(viewModel = viewModel)
                    Screen.PROJECTS_TASKS -> ProjectsTasksScreen(viewModel = viewModel)
                    Screen.WEB_SEARCH -> WebSearchScreen(viewModel = viewModel)
                }
            }
        }
    }
}
