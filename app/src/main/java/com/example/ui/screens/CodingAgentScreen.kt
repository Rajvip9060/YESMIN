package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CodingAgentScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projectFiles by viewModel.projectFiles.collectAsState()
    val selectedFileName by viewModel.selectedFileName.collectAsState()
    val codingPrompt by viewModel.codingPrompt.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val isBusy by viewModel.isCodingAgentBusy.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Editor, 1: Terminal, 2: Planner/Tasks
    var newFileNameInput by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }

    val currentContent = projectFiles[selectedFileName] ?: ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1528))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(Screen.HOME) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "AUTONOMOUS CODING AGENT",
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Virtual IDE & Sandbox",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row {
                IconButton(onClick = {
                    Toast.makeText(context, "Exporting project workspace as ZIP...", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Export Project", tint = NeonCyan)
                }
            }
        }

        // Quick task request bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0E1D))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = codingPrompt,
                onValueChange = { viewModel.setCodingPrompt(it) },
                placeholder = {
                    Text("Ask agent (e.g. 'Create Firebase login page')", color = Color(0xFF64748B), fontSize = 13.sp)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color(0xFF24304D),
                    focusedContainerColor = Color(0xFF131B34),
                    unfocusedContainerColor = Color(0xFF131B34),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { viewModel.executeCodingAgent(codingPrompt) },
                enabled = !isBusy && codingPrompt.isNotBlank(),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isBusy) Color(0xFF334155) else NeonCyan)
            ) {
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Execute", tint = Color.Black)
                }
            }
        }

        // File Explorer Tabs (Horizontal scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF10162B))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            projectFiles.keys.forEach { fileName ->
                val isSelected = fileName == selectedFileName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonPurple.copy(alpha = 0.3f) else Color.Transparent)
                        .border(1.dp, if (isSelected) NeonPurple else Color(0xFF263353), RoundedCornerShape(8.dp))
                        .clickable { viewModel.selectProjectFile(fileName) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = if (isSelected) NeonCyan else Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = fileName,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // New File Button
            IconButton(
                onClick = { showNewFileDialog = true },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add File", tint = NeonCyan)
            }
        }

        // New File Creation Row if open
        if (showNewFileDialog) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141C38))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newFileNameInput,
                    onValueChange = { newFileNameInput = it },
                    placeholder = { Text("filename.kt", color = Color(0xFF64748B), fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = {
                    if (newFileNameInput.isNotBlank()) {
                        viewModel.createProjectFile(newFileNameInput.trim(), "// New file\n")
                        newFileNameInput = ""
                        showNewFileDialog = false
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Create", tint = NeonCyan)
                }
            }
        }

        // View Mode Tabs (Code Editor vs Terminal Logs)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0D1224),
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonPink
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Editor: $selectedFileName", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Terminal & Logs (${terminalLogs.size})", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // Main Editor or Terminal Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF080C18))
                .padding(12.dp)
        ) {
            if (selectedTab == 0) {
                // Code Editor with line editing
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FILE: $selectedFileName",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { viewModel.deleteProjectFile(selectedFileName) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete File", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF060914))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        BasicTextField(
                            value = currentContent,
                            onValueChange = { viewModel.updateFileContent(selectedFileName, it) },
                            textStyle = TextStyle(
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 19.sp
                            ),
                            cursorBrush = SolidColor(NeonPink),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                // Terminal & Autonomous Planner Output
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF030712))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(terminalLogs) { log ->
                        val isError = log.contains("Error", ignoreCase = true) || log.contains("✗")
                        val isSuccess = log.contains("✓") || log.contains("★")
                        val isStep = log.contains("►")
                        Text(
                            text = log,
                            color = when {
                                isError -> Color(0xFFEF4444)
                                isSuccess -> Color(0xFF10B981)
                                isStep -> NeonCyan
                                else -> Color(0xFF94A3B8)
                            },
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Quick Preset Tasks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1528))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Create Firebase login page",
                "Add Room database entity",
                "Implement dark neon theme",
                "Fix syntax & compile errors",
                "Generate Unit Tests"
            ).forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { viewModel.executeCodingAgent(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = preset, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
            }
        }
    }
}
