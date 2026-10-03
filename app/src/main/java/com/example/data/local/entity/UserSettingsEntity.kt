package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val assistantName: String = "YESMIN AI",
    val userNickname: String = "Jaan",
    val userName: String = "User",
    val personalityMode: String = "Girlfriend / Companion", // "Girlfriend / Companion", "Friendly Assistant", "Professional Assistant", "Coding Assistant", "Study Assistant"
    val greetingStyle: String = "Romantic & Caring",
    val customSystemInstructions: String = "",
    val activeAiProvider: String = "Gemini", // "Gemini", "OpenAI", "Anthropic", "Custom"
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.5-flash",
    val openAiApiKey: String = "",
    val openAiModel: String = "gpt-4o",
    val anthropicApiKey: String = "",
    val customApiBaseUrl: String = "",
    val customApiKey: String = "",
    val voiceProvider: String = "Gemini / Android TTS", // "Gemini / Android TTS", "ElevenLabs"
    val elevenLabsApiKey: String = "",
    val elevenLabsVoiceId: String = "21m00Tcm4TlvDq8ikWAM", // Default Rachel
    val elevenLabsModelId: String = "eleven_multilingual_v2",
    val autoSpeak: Boolean = false,
    val voiceSpeed: Float = 1.0f,
    val darkTheme: Boolean = true,
    val accentColorHex: String = "#EC4899", // Neon pink / purple default
    val memoryEnabled: Boolean = true,
    val avatarType: String = "orb" // "orb", "girl_avatar", "cyber_avatar"
)
