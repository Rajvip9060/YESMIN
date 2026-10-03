package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.util.AppActionManager
import com.example.util.AssistantAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }
    // 0: Profile, 1: Permissions (अनुमतियाँ), 2: AI Providers, 3: Voice, 4: Services, 5: About

    // Form inputs initialized from settings
    var customAssistantName by remember(settings.assistantName) { mutableStateOf(settings.assistantName) }
    var userNickname by remember(settings.userNickname) { mutableStateOf(settings.userNickname) }
    var selectedPersonality by remember(settings.personalityMode) { mutableStateOf(settings.personalityMode) }
    var customInstructions by remember(settings.customSystemInstructions) { mutableStateOf(settings.customSystemInstructions) }

    var geminiApiKey by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var openAiApiKey by remember(settings.openAiApiKey) { mutableStateOf(settings.openAiApiKey) }
    var customBaseUrl by remember(settings.customApiBaseUrl) { mutableStateOf(settings.customApiBaseUrl) }

    var elevenLabsApiKey by remember(settings.elevenLabsApiKey) { mutableStateOf(settings.elevenLabsApiKey) }
    var elevenLabsVoiceId by remember(settings.elevenLabsVoiceId) { mutableStateOf(settings.elevenLabsVoiceId) }
    var voiceSpeed by remember(settings.voiceSpeed) { mutableFloatStateOf(settings.voiceSpeed) }

    var personalityDropdownExpanded by remember { mutableStateOf(false) }
    val personalities = listOf(
        "Girlfriend / Companion",
        "Friendly Assistant",
        "Professional Assistant",
        "Coding Assistant",
        "Study Assistant"
    )

    // Permission status checks
    val isOverlayGranted = AppActionManager.isOverlayPermissionGranted(context)
    val isWriteSettingsGranted = AppActionManager.isWriteSettingsGranted(context)
    val isNotificationListenerGranted = AppActionManager.isNotificationListenerGranted(context)
    val isAccessibilityEnabled = AppActionManager.isAccessibilityEnabled(context)

    // Permission launchers
    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(context, if (granted) "Microphone permission granted!" else "Permission denied", Toast.LENGTH_SHORT).show()
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(context, if (granted) "Notification permission granted!" else "Permission denied", Toast.LENGTH_SHORT).show()
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
                    text = "SETTINGS & SYSTEM PERMISSIONS",
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Configure ${settings.assistantName}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Section Tabs with dedicated Permissions tab
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = Color(0xFF0C1020),
            contentColor = Color.White,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = NeonPink
                )
            }
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Profile", fontSize = 12.sp) })
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = if (activeTab == 1) NeonPink else Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Permissions (अनुमतियाँ)", fontSize = 12.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("AI Providers", fontSize = 12.sp) })
            Tab(selected = activeTab == 3, onClick = { activeTab = 3 }, text = { Text("Voice", fontSize = 12.sp) })
            Tab(selected = activeTab == 4, onClick = { activeTab = 4 }, text = { Text("Services", fontSize = 12.sp) })
            Tab(selected = activeTab == 5, onClick = { activeTab = 5 }, text = { Text("About", fontSize = 12.sp) })
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // TAB 0: PROFILE & ASSISTANT NAME
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPink.copy(alpha = 0.5f)) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ASSISTANT NAME",
                                        color = NeonPink,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    StatusPill(text = "Live Preview", color = NeonCyan)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = customAssistantName,
                                    onValueChange = { customAssistantName = it },
                                    label = { Text("Assistant Name", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Live Preview Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF090D1C))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Live Preview: \"Hi $userNickname! I'm ${if (customAssistantName.isNotBlank()) customAssistantName else "YESMIN AI"}, your personalized AI companion!\"",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.updateAssistantName(customAssistantName.trim())
                                            Toast.makeText(context, "Assistant name updated to '${customAssistantName.trim()}'!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save Name")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.resetAssistantNameToDefault()
                                            customAssistantName = "YESMIN AI"
                                            Toast.makeText(context, "Reset to default: YESMIN AI", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reset to Default", color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // User Nickname Card
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("USER NICKNAME", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = userNickname,
                                    onValueChange = { userNickname = it },
                                    label = { Text("What should ${settings.assistantName} call you? (e.g. Jaan, Master, Boss)", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(userNickname = userNickname.trim()))
                                        Toast.makeText(context, "Nickname updated to '$userNickname'", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save Nickname")
                                }
                            }
                        }
                    }

                    // Personality Mode Card
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("AI PERSONALITY MODE", color = NeonPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                ExposedDropdownMenuBox(
                                    expanded = personalityDropdownExpanded,
                                    onExpandedChange = { personalityDropdownExpanded = !personalityDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = selectedPersonality,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personalityDropdownExpanded) },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = personalityDropdownExpanded,
                                        onDismissRequest = { personalityDropdownExpanded = false }
                                    ) {
                                        personalities.forEach { p ->
                                            DropdownMenuItem(
                                                text = { Text(p) },
                                                onClick = {
                                                    selectedPersonality = p
                                                    viewModel.updateSettings(settings.copy(personalityMode = p))
                                                    personalityDropdownExpanded = false
                                                    Toast.makeText(context, "Personality mode set to $p", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("CUSTOM PERSONALITY INSTRUCTIONS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = customInstructions,
                                    onValueChange = { customInstructions = it },
                                    placeholder = { Text("E.g. Speak cute Hindi/Hinglish, always greet with warmth, help me study...", color = Color(0xFF64748B)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(customSystemInstructions = customInstructions))
                                        Toast.makeText(context, "Custom instructions saved!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save Personality Instructions")
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: DEDICATED PERMISSIONS SECTION (अनुरोध के अनुसार अलग परमिशन सेक्शन)
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPink) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ANDROID SYSTEM PERMISSIONS",
                                        color = NeonPink,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    StatusPill(text = "Settings Manager", color = NeonCyan)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Yahan se aap assistant ke liye jaruri permissions ko Android settings me jakar ON kar sakte hain.",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // 1. Accessibility Settings
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPink.copy(alpha = 0.5f)) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(NeonPink.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Accessibility, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("1. Accessibility Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("एक्सेसिबिलिटी सेटिंग्स खोलना", color = NeonPink, fontSize = 11.sp)
                                        }
                                    }
                                    StatusPill(
                                        text = if (isAccessibilityEnabled) "Active (चालू)" else "Off (बंद)",
                                        color = if (isAccessibilityEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Jarvis automation, screen reading, aur assistive UI gestures ke liye accessibility ko enable karein.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { AppActionManager.executeAction(context, AssistantAction.OpenAccessibilitySettings) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Accessibility Settings (सेटिंग्स खोलें और ON करें)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 2. Notification Listener Access
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan.copy(alpha = 0.5f)) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(NeonCyan.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("2. Notification Listener Access", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("नोटिफिकेशन लिसनर एक्सेस खोलना", color = NeonCyan, fontSize = 11.sp)
                                        }
                                    }
                                    StatusPill(
                                        text = if (isNotificationListenerGranted) "Active (चालू)" else "Off (बंद)",
                                        color = if (isNotificationListenerGranted) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Assistant ko aane wale messages, WhatsApp alerts aur calls ko detect karke bolkar sunane ke liye access.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { AppActionManager.executeAction(context, AssistantAction.OpenNotificationListenerSettings) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Notification Listener (नोटिफिकेशन एक्सेस चालू करें)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 3. Draw Over Other Apps Access
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPurple.copy(alpha = 0.5f)) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(NeonPurple.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Layers, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("3. Draw Over Other Apps Access", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("अन्य ऐप्स के ऊपर डिस्प्ले परमिशन", color = NeonPurple, fontSize = 11.sp)
                                        }
                                    }
                                    StatusPill(
                                        text = if (isOverlayGranted) "Active (चालू)" else "Off (बंद)",
                                        color = if (isOverlayGranted) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "YESMIN AI ke animated floating core orb ko kisi bhi dusre app ya home screen par display karne ke liye.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { AppActionManager.executeAction(context, AssistantAction.OpenOverlayPermissionSettings) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Overlay Permission (डिस्प्ले परमिशन ON करें)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 4. System Settings Write Access
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = Color(0xFFF59E0B).copy(alpha = 0.5f)) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.SettingsApplications, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("4. System Settings Write Access", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("सिस्टम सेटिंग्स राइट एक्सेस खोलना", color = Color(0xFFF59E0B), fontSize = 11.sp)
                                        }
                                    }
                                    StatusPill(
                                        text = if (isWriteSettingsGranted) "Active (चालू)" else "Off (बंद)",
                                        color = if (isWriteSettingsGranted) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Assistant dwara device ki brightness, volume level aur sound modes ko directly modify karne ke liye.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { AppActionManager.executeAction(context, AssistantAction.OpenWriteSettingsPermission) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Write Settings (सिस्टम सेटिंग्स राइट चालू करें)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 5. Standard Permissions & Full App Info Shortcut
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("APP INFO & DEVICE PERMISSIONS", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { micLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mic Permission", color = Color.White, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, tint = NeonPink, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Notifications", color = Color.White, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Full App Info Settings (सभी अनुमतियाँ देखें)", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: AI PROVIDERS
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ACTIVE AI PROVIDER", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    StatusPill(text = settings.activeAiProvider, color = NeonCyan)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Gemini", "OpenAI", "Custom").forEach { provider ->
                                        val isSelected = settings.activeAiProvider == provider
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else Color(0xFF1E293B))
                                                .clickable {
                                                    viewModel.updateSettings(settings.copy(activeAiProvider = provider))
                                                    Toast.makeText(context, "Switched provider to $provider", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = provider,
                                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("GEMINI API CONFIGURATION", color = NeonPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = geminiApiKey,
                                    onValueChange = { geminiApiKey = it },
                                    label = { Text("Gemini API Key (optional if configured in AI Studio)", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = settings.geminiModel,
                                    onValueChange = { viewModel.updateSettings(settings.copy(geminiModel = it)) },
                                    label = { Text("Model Name (e.g. gemini-3.5-flash)", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(geminiApiKey = geminiApiKey.trim()))
                                        Toast.makeText(context, "Gemini configuration saved!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save Gemini Settings")
                                }
                            }
                        }
                    }

                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("OPENAI / CUSTOM API CONFIGURATION", color = NeonPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = openAiApiKey,
                                    onValueChange = { openAiApiKey = it },
                                    label = { Text("OpenAI API Key", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customBaseUrl,
                                    onValueChange = { customBaseUrl = it },
                                    label = { Text("Custom API Base URL (e.g. https://api.groq.com/openai/v1/)", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateSettings(
                                            settings.copy(
                                                openAiApiKey = openAiApiKey.trim(),
                                                customApiBaseUrl = customBaseUrl.trim()
                                            )
                                        )
                                        Toast.makeText(context, "OpenAI / Custom API saved!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Save Provider Settings")
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: VOICE & ELEVENLABS
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonPink) {
                            Column {
                                Text("VOICE PROVIDER", color = NeonPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Gemini / Android TTS", "ElevenLabs").forEach { prov ->
                                        val isSel = settings.voiceProvider == prov
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) NeonPink.copy(alpha = 0.25f) else Color(0xFF1E293B))
                                                .clickable {
                                                    viewModel.updateSettings(settings.copy(voiceProvider = prov))
                                                    Toast.makeText(context, "Voice provider: $prov", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = prov,
                                                color = if (isSel) Color.White else Color(0xFF94A3B8),
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Auto Speak Responses", color = Color.White, fontSize = 14.sp)
                                    Switch(
                                        checked = settings.autoSpeak,
                                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoSpeak = it)) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = NeonPink)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Voice Speed: ${"%.1f".format(voiceSpeed)}x", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Slider(
                                    value = voiceSpeed,
                                    onValueChange = {
                                        voiceSpeed = it
                                        viewModel.updateSettings(settings.copy(voiceSpeed = it))
                                    },
                                    valueRange = 0.5f..2.0f,
                                    colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPurple)
                                )
                            }
                        }
                    }

                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan) {
                            Column {
                                Text("ELEVENLABS INTEGRATION", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = elevenLabsApiKey,
                                    onValueChange = { elevenLabsApiKey = it },
                                    label = { Text("ElevenLabs API Key", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = elevenLabsVoiceId,
                                    onValueChange = { elevenLabsVoiceId = it },
                                    label = { Text("Voice ID (e.g. 21m00Tcm4TlvDq8ikWAM - Rachel)", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.updateSettings(
                                                settings.copy(
                                                    elevenLabsApiKey = elevenLabsApiKey.trim(),
                                                    elevenLabsVoiceId = elevenLabsVoiceId.trim()
                                                )
                                            )
                                            Toast.makeText(context, "ElevenLabs Settings Saved!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.speakText("Hi $userNickname! This is a test of my voice synthesized for you.")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test Voice", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: API & SERVICES MANAGER
                    val services = listOf(
                        "1. Gemini AI" to "Connected via Google GenAI REST",
                        "2. OpenAI" to "GPT-4o & ChatGPT Completions",
                        "3. Anthropic" to "Claude 3.5 Sonnet Integration",
                        "4. ElevenLabs" to "Neural Voice Synthesis",
                        "5. Speech-to-Text" to "Android SpeechRecognizer Engine",
                        "6. Text-to-Speech" to "Android TTS & ElevenLabs Player",
                        "7. Web Search" to "DuckDuckGo Instant Discovery",
                        "8. Image Generation" to "Gemini Multimodal Vision API",
                        "9. Android Bridge" to "Native Intent & Package Launcher",
                        "10. Coding Sandbox" to "Virtual IDE File System",
                        "11. Local Storage" to "SQLite Room DB with KSP",
                        "12. Notifications" to "Android System Notification Manager"
                    )

                    items(services.size) { idx ->
                        val (name, desc) = services[idx]
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(desc, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                                StatusPill(text = "Active", color = Color(0xFF10B981))
                            }
                        }
                    }
                }

                5 -> {
                    // TAB 5: ABOUT & PRIVACY
                    item {
                        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("YESMIN AI PLATFORM", color = NeonPink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Version 1.0.0 • AI Girlfriend + Personal Jarvis Assistant\n" +
                                            "Engineered with Jetpack Compose, Room Database, Gemini 3.5, and Native Android App Bridge.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        viewModel.createNewConversation("Chat with ${settings.assistantName}", settings.personalityMode)
                                        Toast.makeText(context, "Created fresh conversation!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Start Fresh Chat Session")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
