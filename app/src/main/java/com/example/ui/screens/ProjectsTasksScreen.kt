package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.viewmodel.AssistantViewModel
import com.example.util.NotificationHelper

@Composable
fun ProjectsTasksScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Projects, 1: Tasks
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    var newProjectName by remember { mutableStateOf("") }
    var newProjectType by remember { mutableStateOf("Android App") }
    var newProjectDesc by remember { mutableStateOf("") }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDesc by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "WORKSPACES & REMINDERS",
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Projects & Tasks",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = {
                if (selectedTab == 0) showAddProjectDialog = !showAddProjectDialog
                else showAddTaskDialog = !showAddTaskDialog
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = NeonCyan)
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0C1020),
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
                text = { Text("Projects (${projects.size})", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Tasks & Reminders (${tasks.size})", fontSize = 13.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                // Add Project Form
                if (showAddProjectDialog) {
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan) {
                            Column {
                                Text("NEW PROJECT WORKSPACE", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newProjectName,
                                    onValueChange = { newProjectName = it },
                                    label = { Text("Project Name", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newProjectType,
                                    onValueChange = { newProjectType = it },
                                    label = { Text("Type (Android, Website, AI Assistant, Study)", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newProjectDesc,
                                    onValueChange = { newProjectDesc = it },
                                    label = { Text("Description", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (newProjectName.isNotBlank()) {
                                            viewModel.addProject(newProjectName.trim(), newProjectType.trim(), newProjectDesc.trim())
                                            newProjectName = ""
                                            newProjectDesc = ""
                                            showAddProjectDialog = false
                                            Toast.makeText(context, "Project Created!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Create Project", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (projects.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No projects created yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                            Text("Create an Android App, Website, or Study Workspace.", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                } else {
                    items(projects) { proj ->
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(proj.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusPill(text = proj.type, color = NeonPurple)
                                    }
                                    if (proj.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(proj.description, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteProject(proj.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // Tasks Tab
                if (showAddTaskDialog) {
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPink) {
                            Column {
                                Text("NEW TASK OR REMINDER", color = NeonPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newTaskTitle,
                                    onValueChange = { newTaskTitle = it },
                                    label = { Text("Task Title", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = newTaskDesc,
                                    onValueChange = { newTaskDesc = it },
                                    label = { Text("Details & Notes", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (newTaskTitle.isNotBlank()) {
                                            viewModel.addTask(newTaskTitle.trim(), newTaskDesc.trim())
                                            NotificationHelper.showNotification(context, "Task Created", newTaskTitle)
                                            newTaskTitle = ""
                                            newTaskDesc = ""
                                            showAddTaskDialog = false
                                            Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Add Task & Set Reminder")
                                }
                            }
                        }
                    }
                }

                if (tasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No tasks added yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        }
                    }
                } else {
                    items(tasks) { task ->
                        FuturisticCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (task.isCompleted) Color(0xFF10B981).copy(alpha = 0.3f) else NeonPink.copy(alpha = 0.3f),
                            onClick = { viewModel.toggleTask(task.id, !task.isCompleted) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle status",
                                    tint = if (task.isCompleted) Color(0xFF10B981) else NeonPink,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        color = if (task.isCompleted) Color(0xFF64748B) else Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    if (task.description.isNotBlank()) {
                                        Text(task.description, color = Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
