package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiOrb
import com.example.ui.components.FuturisticCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.Screen

data class QuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val screen: Screen
)

@Composable
fun HomeScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    val assistantName = settings.assistantName.ifBlank { "YESMIN AI" }
    val nickname = settings.userNickname.ifBlank { "Jaan" }

    val quickActions = listOf(
        QuickAction("Chat", "AI Companion", Icons.Default.ChatBubble, NeonPink, Screen.CHAT),
        QuickAction("Live Voice", "Real-time call", Icons.Default.Mic, NeonCyan, Screen.LIVE_VOICE),
        QuickAction("Coding Agent", "Code & Dev", Icons.Default.Code, NeonPurple, Screen.CODING_AGENT),
        QuickAction("Apps & Actions", "Launch & control", Icons.Default.Apps, Color(0xFF10B981), Screen.APPS_ACTIONS),
        QuickAction("Web Search", "Real-time answers", Icons.Default.Search, Color(0xFFF59E0B), Screen.WEB_SEARCH),
        QuickAction("Memory Hub", "Preferences & facts", Icons.Default.Psychology, Color(0xFFEC4899), Screen.MEMORY),
        QuickAction("Projects & Tasks", "Workspaces", Icons.Default.Folder, Color(0xFF3B82F6), Screen.PROJECTS_TASKS),
        QuickAction("Settings", "Configure AI", Icons.Default.Settings, Color(0xFF94A3B8), Screen.SETTINGS)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Hero Header & AI Orb Section
        item {
            FuturisticCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonPurple.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hi, $nickname ♥",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "I'm $assistantName",
                                fontSize = 16.sp,
                                color = NeonPink,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        StatusPill(
                            text = orbState.name,
                            color = when (orbState) {
                                com.example.ui.components.OrbState.IDLE -> Color(0xFF10B981)
                                com.example.ui.components.OrbState.LISTENING -> NeonCyan
                                com.example.ui.components.OrbState.THINKING -> Color(0xFFF59E0B)
                                com.example.ui.components.OrbState.SPEAKING -> NeonPink
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Glowing Orb
                    AiOrb(
                        state = orbState,
                        size = 150.dp,
                        accentColor = NeonPink,
                        onClick = { viewModel.navigateTo(Screen.LIVE_VOICE) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "How can I help you today, $nickname?",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Personality Mode Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(NeonPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Mode: ${settings.personalityMode}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "QUICK ACTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Quick Action Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in quickActions.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (action in row) {
                            Box(modifier = Modifier.weight(1f)) {
                                FuturisticCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    borderColor = action.color.copy(alpha = 0.4f),
                                    onClick = { viewModel.navigateTo(action.screen) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(action.color.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = action.icon,
                                                contentDescription = action.title,
                                                tint = action.color,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = action.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = action.subtitle,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Tasks / Reminders
        if (tasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TASKS & REMINDERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "View all",
                        fontSize = 12.sp,
                        color = NeonCyan,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.PROJECTS_TASKS) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.take(3).forEach { task ->
                        FuturisticCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (task.isCompleted) Color(0xFF10B981).copy(alpha = 0.3f) else NeonPink.copy(alpha = 0.3f),
                            onClick = { viewModel.toggleTask(task.id, !task.isCompleted) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        color = if (task.isCompleted) Color(0xFF94A3B8) else Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    if (task.description.isNotBlank()) {
                                        Text(
                                            text = task.description,
                                            color = Color(0xFF64748B),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                StatusPill(
                                    text = if (task.isCompleted) "Done" else task.priority,
                                    color = if (task.isCompleted) Color(0xFF10B981) else NeonPink
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Conversations
        if (conversations.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT CONVERSATIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Open Chat",
                        fontSize = 12.sp,
                        color = NeonPink,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.CHAT) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    conversations.take(3).forEach { conv ->
                        FuturisticCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.selectConversation(conv.id)
                                viewModel.navigateTo(Screen.CHAT)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubble,
                                        contentDescription = null,
                                        tint = NeonPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = conv.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = conv.personalityMode.take(12),
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
