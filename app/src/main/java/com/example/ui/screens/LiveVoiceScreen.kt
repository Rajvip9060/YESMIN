package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiOrb
import com.example.ui.components.FuturisticCard
import com.example.ui.components.OrbState
import com.example.ui.components.StatusPill
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun LiveVoiceScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isLiveActive by viewModel.isLiveVoiceActive.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val liveAiResponse by viewModel.liveAiResponse.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val speechRms by viewModel.speechManager.speechRms.collectAsState()
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val isSpeaking by viewModel.speechManager.isSpeaking.collectAsState()

    val assistantName = settings.assistantName.ifBlank { "YESMIN AI" }
    val nickname = settings.userNickname.ifBlank { "Jaan" }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startLiveVoice()
        } else {
            Toast.makeText(context, "Microphone permission required for Live Voice", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070A14),
                        Color(0xFF0D1226),
                        Color(0xFF070A14)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.endLiveVoice()
                viewModel.navigateTo(Screen.HOME)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "GEMINI LIVE CALL",
                    color = NeonPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = assistantName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            StatusPill(
                text = if (isLiveActive) "Connected" else "Standby",
                color = if (isLiveActive) Color(0xFF10B981) else Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Animated Live Orb
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AiOrb(
                    state = if (!isLiveActive) OrbState.IDLE else orbState,
                    size = 230.dp,
                    amplitude = speechRms,
                    accentColor = NeonPink
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status text
                Text(
                    text = when {
                        !isLiveActive -> "Ready to connect with $assistantName"
                        isSpeaking -> "$assistantName is speaking..."
                        isListening -> "Listening to you, $nickname..."
                        orbState == OrbState.THINKING -> "$assistantName is thinking..."
                        else -> "Live session active"
                    },
                    color = when {
                        isSpeaking -> NeonPink
                        isListening -> NeonCyan
                        else -> Color.White
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "English • Hindi • Hinglish",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Live Transcript & AI Response Card
        FuturisticCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            borderColor = if (isLiveActive) NeonPurple.copy(alpha = 0.5f) else Color(0xFF263353)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isLiveActive) {
                    Text(
                        text = "You: $liveTranscript",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    if (liveAiResponse.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$assistantName: $liveAiResponse",
                            color = NeonPink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = "Tap 'Start Call' below to speak directly with $assistantName in real-time.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live Call Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Listen toggle
            if (isLiveActive) {
                IconButton(
                    onClick = {
                        if (isListening) viewModel.stopVoiceInput() else viewModel.startVoiceInput()
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFF1E293B) else Color(0xFFEF4444).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF334155), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Mute",
                        tint = if (isListening) Color.White else Color(0xFFEF4444)
                    )
                }

                // Interrupt AI button (Stops speaking instantly and listens)
                IconButton(
                    onClick = {
                        viewModel.stopSpeaking()
                        viewModel.startVoiceInput()
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF3B82F6), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Interrupt",
                        tint = NeonCyan
                    )
                }
            }

            // Primary Start/End Call button
            IconButton(
                onClick = {
                    if (isLiveActive) {
                        viewModel.endLiveVoice()
                    } else {
                        micLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .then(
                        if (isLiveActive) Modifier.background(Color(0xFFDC2626))
                        else Modifier.background(Brush.linearGradient(listOf(NeonPink, NeonPurple)))
                    )
            ) {
                Icon(
                    imageVector = if (isLiveActive) Icons.Default.CallEnd else Icons.Default.Phone,
                    contentDescription = if (isLiveActive) "End Call" else "Start Live Call",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
