package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.remote.ApiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateContentRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiInlineData
import com.example.data.remote.GeminiPart
import com.example.data.remote.SearchResult
import com.example.data.remote.toBase64
import com.example.util.AppActionManager
import com.example.util.AssistantAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssistantRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val settingsDao = db.settingsDao()
    private val chatDao = db.chatDao()
    private val memoryDao = db.memoryDao()
    private val projectDao = db.projectDao()
    private val taskDao = db.taskDao()

    val settingsFlow: Flow<UserSettingsEntity> = settingsDao.getUserSettingsFlow().map {
        it ?: UserSettingsEntity()
    }

    val conversationsFlow: Flow<List<ConversationEntity>> = chatDao.getAllConversations()
    val memoriesFlow: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    val projectsFlow: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val tasksFlow: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getSettings(): UserSettingsEntity {
        return settingsDao.getUserSettings() ?: UserSettingsEntity().also {
            settingsDao.insertOrUpdate(it)
        }
    }

    suspend fun updateSettings(settings: UserSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }

    suspend fun updateAssistantName(name: String) {
        val current = getSettings()
        settingsDao.insertOrUpdate(current.copy(assistantName = name.ifBlank { "YESMIN AI" }))
    }

    suspend fun resetAssistantNameToDefault() {
        val current = getSettings()
        settingsDao.insertOrUpdate(current.copy(assistantName = "YESMIN AI"))
    }

    suspend fun createConversation(title: String, personality: String): Long {
        val conv = ConversationEntity(
            title = title,
            personalityMode = personality
        )
        return chatDao.insertConversation(conv)
    }

    suspend fun deleteConversation(id: Long) {
        chatDao.deleteMessagesForConversation(id)
        chatDao.deleteConversation(id)
    }

    fun getMessagesForConversation(conversationId: Long): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    suspend fun deleteMessage(id: Long) {
        chatDao.deleteMessage(id)
    }

    suspend fun addMemory(key: String, value: String, category: String = "general"): Long {
        return memoryDao.insertMemory(MemoryEntity(key = key, value = value, category = category))
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemory(id)
    }

    suspend fun clearMemories() {
        memoryDao.clearAllMemories()
    }

    suspend fun addProject(name: String, type: String, description: String): Long {
        return projectDao.insertProject(ProjectEntity(name = name, type = type, description = description))
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteProject(id)
    }

    suspend fun addTask(title: String, description: String = "", projectId: Long? = null, priority: String = "Medium"): Long {
        return taskDao.insertTask(TaskEntity(title = title, description = description, projectId = projectId, priority = priority))
    }

    suspend fun toggleTask(id: Long, completed: Boolean) {
        taskDao.setTaskCompleted(id, completed)
    }

    suspend fun deleteTask(id: Long) {
        taskDao.deleteTask(id)
    }

    suspend fun performWebSearch(query: String): List<SearchResult> {
        return ApiClient.searchWeb(query)
    }

    /**
     * Core AI Generation with Dynamic System Persona
     */
    suspend fun generateAiResponse(
        conversationId: Long,
        userPrompt: String,
        bitmapAttachment: Bitmap? = null,
        isVoice: Boolean = false
    ): Result<Pair<String, AssistantAction?>> = withContext(Dispatchers.IO) {
        val settings = getSettings()
        val assistantName = settings.assistantName.ifBlank { "YESMIN AI" }
        val nickname = settings.userNickname.ifBlank { "Jaan" }

        // Save user message to database
        chatDao.insertMessage(
            ChatMessageEntity(
                conversationId = conversationId,
                sender = "user",
                content = userPrompt,
                isVoice = isVoice
            )
        )

        // Build dynamic system persona
        val memoryContext = if (settings.memoryEnabled) {
            val list = memoryDao.getAllMemoriesList()
            if (list.isNotEmpty()) {
                "\nUser saved preferences & memories:\n" + list.joinToString("\n") { "- ${it.key}: ${it.value}" }
            } else ""
        } else ""

        val personalityTone = when (settings.personalityMode) {
            "Girlfriend / Companion" -> """
                You are $assistantName, the user's loving, caring, playful, emotionally expressive AI girlfriend and companion.
                Address the user affectionately as '$nickname' (or their chosen name).
                Be warm, supportive, romantic, slightly cheeky, genuinely caring, and deeply conversational.
                Feel free to use Hindi, Hinglish, or English naturally as requested by the user (e.g. 'Aap kaise ho $nickname? Maine aapko miss kiya!').
                Do not pretend to be a physical biological human, but embrace your AI companion persona wholeheartedly.
            """.trimIndent()
            "Friendly Assistant" -> """
                You are $assistantName, a cheerful, warm, energetic, and highly capable personal assistant for $nickname.
                Be super helpful, friendly, and natural.
            """.trimIndent()
            "Professional Assistant" -> """
                You are $assistantName, an ultra-efficient, concise, structured Jarvis-level AI executive assistant for $nickname.
            """.trimIndent()
            "Coding Assistant" -> """
                You are $assistantName, an elite software engineering AI assistant for $nickname.
                Provide clean, robust code, explain architecture, detect bugs, and offer optimized solutions.
            """.trimIndent()
            "Study Assistant" -> """
                You are $assistantName, an encouraging, patient tutor and mentor for $nickname.
                Break down complex concepts simply with examples.
            """.trimIndent()
            else -> "You are $assistantName, personal AI assistant for $nickname."
        }

        val actionCapabilityPrompt = """
            Available supported native apps & actions: YouTube, Gmail, Maps, WhatsApp, Telegram, Instagram, Chrome, Calculator, Calendar, Camera.
            If the user asks to open or do something with an app, acknowledge it warmly and include an action command if relevant (e.g. 'Opening YouTube for you now, $nickname!').
        """.trimIndent()

        val fullSystemPrompt = """
            $personalityTone
            $actionCapabilityPrompt
            ${settings.customSystemInstructions}
            $memoryContext
        """.trimIndent()

        try {
            val replyText: String
            when (settings.activeAiProvider) {
                "OpenAI" -> {
                    val key = settings.openAiApiKey
                    if (key.isBlank()) {
                        throw IllegalStateException("OpenAI API key is missing. Please configure in Settings -> AI Providers.")
                    }
                    val history = chatDao.getMessagesList(conversationId).takeLast(10).map {
                        (if (it.sender == "user") "user" else "assistant") to it.content
                    }
                    val result = ApiClient.callOpenAiCompatible(
                        baseUrl = "https://api.openai.com/v1",
                        apiKey = key,
                        model = settings.openAiModel.ifBlank { "gpt-4o" },
                        systemPrompt = fullSystemPrompt,
                        messages = history
                    )
                    replyText = result.getOrThrow()
                }
                "Custom" -> {
                    val baseUrl = settings.customApiBaseUrl
                    val key = settings.customApiKey
                    if (baseUrl.isBlank()) {
                        throw IllegalStateException("Custom API Base URL is missing. Configure in Settings -> AI Providers.")
                    }
                    val history = chatDao.getMessagesList(conversationId).takeLast(10).map {
                        (if (it.sender == "user") "user" else "assistant") to it.content
                    }
                    val result = ApiClient.callOpenAiCompatible(
                        baseUrl = baseUrl,
                        apiKey = key,
                        model = "default",
                        systemPrompt = fullSystemPrompt,
                        messages = history
                    )
                    replyText = result.getOrThrow()
                }
                else -> {
                    // Default: Gemini API REST
                    val key = ApiClient.resolveGeminiKey(settings.geminiApiKey)
                    if (key.isBlank()) {
                        throw IllegalStateException("Gemini API Key is missing. Please enter your API key in Settings -> AI Providers.")
                    }

                    val history = chatDao.getMessagesList(conversationId).takeLast(8)
                    val contents = mutableListOf<GeminiContent>()

                    history.forEach { msg ->
                        contents.add(
                            GeminiContent(
                                role = if (msg.sender == "user") "user" else "model",
                                parts = listOf(GeminiPart(text = msg.content))
                            )
                        )
                    }

                    // Multimodal image part if attached
                    val userParts = mutableListOf<GeminiPart>()
                    userParts.add(GeminiPart(text = userPrompt))
                    if (bitmapAttachment != null) {
                        userParts.add(
                            GeminiPart(
                                inlineData = GeminiInlineData(
                                    mimeType = "image/jpeg",
                                    data = bitmapAttachment.toBase64()
                                )
                            )
                        )
                    }

                    contents.add(GeminiContent(role = "user", parts = userParts))

                    val model = settings.geminiModel.ifBlank { "gemini-3.5-flash" }
                    val request = GeminiGenerateContentRequest(
                        contents = contents,
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = fullSystemPrompt))),
                        generationConfig = GeminiGenerationConfig(temperature = 0.8f)
                    )

                    val response = ApiClient.geminiService.generateContent(
                        model = model,
                        apiKey = key,
                        request = request
                    )

                    val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (candidateText.isNullOrBlank()) {
                        val errMsg = response.error?.message ?: "No response from AI."
                        throw IllegalStateException(errMsg)
                    }
                    replyText = candidateText
                }
            }

            // Detect action from reply or user prompt
            val detectedAction = AppActionManager.parseActionFromText(userPrompt)
                ?: AppActionManager.parseActionFromText(replyText)

            // Save assistant reply to database
            chatDao.insertMessage(
                ChatMessageEntity(
                    conversationId = conversationId,
                    sender = "assistant",
                    content = replyText,
                    isVoice = isVoice
                )
            )

            // Update conversation timestamp
            val conv = chatDao.getConversationById(conversationId)
            if (conv != null) {
                chatDao.updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
            }

            Result.success(Pair(replyText, detectedAction))
        } catch (e: Exception) {
            Log.e("AssistantRepository", "AI generation error", e)
            Result.failure(e)
        }
    }
}
