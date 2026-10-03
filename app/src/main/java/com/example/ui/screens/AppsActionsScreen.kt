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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.util.AppActionManager
import com.example.util.AssistantAction

data class SupportedAppItem(
    val name: String,
    val packageName: String?,
    val fallbackUrl: String,
    val icon: ImageVector,
    val color: Color
)

data class SystemPermissionItem(
    val title: String,
    val hindiTitle: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isGranted: Boolean,
    val action: AssistantAction
)

@Composable
fun AppsActionsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customUrlInput by remember { mutableStateOf("") }
    var notificationTitleInput by remember { mutableStateOf("YESMIN AI Reminder") }
    var notificationMsgInput by remember { mutableStateOf("Hi Jaan! Don't forget your tasks today ♥") }
    var pendingConfirmationAction by remember { mutableStateOf<AssistantAction?>(null) }

    val isOverlayGranted = remember { AppActionManager.isOverlayPermissionGranted(context) }
    val isWriteSettingsGranted = remember { AppActionManager.isWriteSettingsGranted(context) }
    val isNotificationListenerGranted = remember { AppActionManager.isNotificationListenerGranted(context) }
    val isAccessibilityEnabled = remember { AppActionManager.isAccessibilityEnabled(context) }

    val systemPermissions = listOf(
        SystemPermissionItem(
            title = "Accessibility Settings",
            hindiTitle = "1. Accessibility Settings खोलना",
            description = "Enables automated interactions, screen reading, and UI assistance for Jarvis mode.",
            icon = Icons.Default.Accessibility,
            color = NeonPink,
            isGranted = isAccessibilityEnabled,
            action = AssistantAction.OpenAccessibilitySettings
        ),
        SystemPermissionItem(
            title = "Notification Listener Access",
            hindiTitle = "2. Notification Listener Access खोलना",
            description = "Allows the assistant to detect and announce incoming messages, alerts, and calls.",
            icon = Icons.Default.NotificationsActive,
            color = NeonCyan,
            isGranted = isNotificationListenerGranted,
            action = AssistantAction.OpenNotificationListenerSettings
        ),
        SystemPermissionItem(
            title = "Draw Over Other Apps Access",
            hindiTitle = "3. Draw Over Other Apps Access खोलना",
            description = "Allows YESMIN AI floating interactive orb and overlay widget to float over any app.",
            icon = Icons.Default.Layers,
            color = NeonPurple,
            isGranted = isOverlayGranted,
            action = AssistantAction.OpenOverlayPermissionSettings
        ),
        SystemPermissionItem(
            title = "System Settings Write Access",
            hindiTitle = "4. System Settings Write Access खोलना",
            description = "Allows the assistant to adjust system volume, screen brightness, and sound profiles.",
            icon = Icons.Default.SettingsApplications,
            color = Color(0xFFF59E0B),
            isGranted = isWriteSettingsGranted,
            action = AssistantAction.OpenWriteSettingsPermission
        )
    )

    val apps = listOf(
        SupportedAppItem("YouTube", "com.google.android.youtube", "https://youtube.com", Icons.Default.PlayArrow, Color(0xFFEF4444)),
        SupportedAppItem("Gmail", "com.google.android.gm", "https://mail.google.com", Icons.Default.Email, Color(0xFFEA4335)),
        SupportedAppItem("Google Maps", "com.google.android.apps.maps", "https://maps.google.com", Icons.Default.Map, Color(0xFF34A853)),
        SupportedAppItem("WhatsApp", "com.whatsapp", "https://web.whatsapp.com", Icons.AutoMirrored.Filled.Send, Color(0xFF25D366)),
        SupportedAppItem("Chrome", "com.android.chrome", "https://google.com", Icons.Default.Language, Color(0xFF4285F4)),
        SupportedAppItem("Calculator", "com.google.android.calculator", "", Icons.Default.Calculate, NeonCyan),
        SupportedAppItem("Calendar", "com.google.android.calendar", "https://calendar.google.com", Icons.Default.CalendarMonth, NeonPurple),
        SupportedAppItem("Camera", "", "", Icons.Default.CameraAlt, NeonPink)
    )

    // Confirmation dialog for sensitive actions
    if (pendingConfirmationAction != null) {
        val action = pendingConfirmationAction!!
        AlertDialog(
            onDismissRequest = { pendingConfirmationAction = null },
            title = { Text("Confirm Action", color = Color.White) },
            text = { Text("Are you sure you want to open system settings for: ${action.type}?", color = Color(0xFFCBD5E1)) },
            containerColor = Color(0xFF131A33),
            confirmButton = {
                Button(
                    onClick = {
                        AppActionManager.executeAction(context, action)
                        pendingConfirmationAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text("Open Now")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingConfirmationAction = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "NATIVE DEVICE ACTIONS & SETTINGS",
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Apps & System Access Bridge",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: SYSTEM & SPECIAL SETTINGS ACCESS (The 4 user requested intents)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYSTEM SETTINGS & PERMISSIONS",
                        color = NeonPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    StatusPill(text = "Jarvis Access", color = NeonCyan)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enable deep device control and background assistance capabilities:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            items(systemPermissions.size) { index ->
                val perm = systemPermissions[index]
                FuturisticCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = perm.color.copy(alpha = 0.45f)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(perm.color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(perm.icon, contentDescription = perm.title, tint = perm.color, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(perm.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(perm.hindiTitle, color = perm.color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            StatusPill(
                                text = if (perm.isGranted) "Granted" else "Tap to Open",
                                color = if (perm.isGranted) Color(0xFF10B981) else perm.color
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(perm.description, color = Color(0xFFCBD5E1), fontSize = 12.sp, lineHeight = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                AppActionManager.executeAction(context, perm.action)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = perm.color),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open ${perm.title}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Section 2: SUPPORTED NATIVE APPS
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "SUPPORTED NATIVE APPS",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (row in apps.chunked(2)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (app in row) {
                                Box(modifier = Modifier.weight(1f)) {
                                    FuturisticCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        borderColor = app.color.copy(alpha = 0.4f),
                                        onClick = {
                                            val action = AssistantAction.OpenApp(app.name, app.packageName, app.fallbackUrl)
                                            AppActionManager.executeAction(context, action)
                                        }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(app.color.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(app.icon, contentDescription = app.name, tint = app.color, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(app.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                                Text("Launch", color = NeonCyan, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: SEND NOTIFICATION
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "TRIGGER LOCAL NOTIFICATION",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan.copy(alpha = 0.4f)) {
                    Column {
                        OutlinedTextField(
                            value = notificationTitleInput,
                            onValueChange = { notificationTitleInput = it },
                            label = { Text("Notification Title", color = Color(0xFF94A3B8)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notificationMsgInput,
                            onValueChange = { notificationMsgInput = it },
                            label = { Text("Message Content", color = Color(0xFF94A3B8)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                AppActionManager.executeAction(
                                    context,
                                    AssistantAction.SendNotification(notificationTitleInput, notificationMsgInput)
                                )
                                Toast.makeText(context, "Notification Sent!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Local Notification", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 4: OPEN WEB URL
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "OPEN ANY WEB URL",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customUrlInput,
                            onValueChange = { customUrlInput = it },
                            placeholder = { Text("https://github.com", color = Color(0xFF64748B), fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customUrlInput.isNotBlank()) {
                                    AppActionManager.executeAction(context, AssistantAction.OpenUrl(customUrlInput))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
