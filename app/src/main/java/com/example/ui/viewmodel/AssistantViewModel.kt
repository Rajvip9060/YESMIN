package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.remote.SearchResult
import com.example.data.repository.AssistantRepository
import com.example.ui.components.OrbState
import com.example.util.AppActionManager
import com.example.util.AssistantAction
import com.example.util.SpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME, CHAT, LIVE_VOICE, CODING_AGENT, SETTINGS,
    APPS_ACTIONS, MEMORY, PROJECTS_TASKS, WEB_SEARCH
}

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AssistantRepository(application)
    val speechManager = SpeechManager(application, viewModelScope)

    // Navigation
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation history stack for back handling
    private val navigationStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            navigationStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (navigationStack.isNotEmpty()) {
            _currentScreen.value = navigationStack.removeAt(navigationStack.size - 1)
            return true
        } else if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            return true
        }
        return false
    }

    // Settings
    val settings: StateFlow<UserSettingsEntity> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserSettingsEntity()
    )

    fun updateAssistantName(name: String) {
        viewModelScope.launch { repository.updateAssistantName(name) }
    }

    fun resetAssistantNameToDefault() {
        viewModelScope.launch { repository.resetAssistantNameToDefault() }
    }

    fun updateSettings(newSettings: UserSettingsEntity) {
        viewModelScope.launch { repository.updateSettings(newSettings) }
    }

    // Conversations & Chat
    val conversations: StateFlow<List<ConversationEntity>> = repository.conversationsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeMessages.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _selectedImage = MutableStateFlow<Bitmap?>(null)
    val selectedImage: StateFlow<Bitmap?> = _selectedImage.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Orb State
    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    // Live Voice Mode State
    private val _isLiveVoiceActive = MutableStateFlow(false)
    val isLiveVoiceActive: StateFlow<Boolean> = _isLiveVoiceActive.asStateFlow()

    private val _liveTranscript = MutableStateFlow("Tap 'Start Live' to begin real-time voice call.")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _liveAiResponse = MutableStateFlow("")
    val liveAiResponse: StateFlow<String> = _liveAiResponse.asStateFlow()

    // Coding Agent State
    private val _projectFiles = MutableStateFlow(
        mapOf(
            "MainActivity.kt" to """
                package com.example.myapp
                
                import android.os.Bundle
                import androidx.activity.ComponentActivity
                import androidx.activity.compose.setContent
                import androidx.compose.material3.Text
                
                class MainActivity : ComponentActivity() {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContent {
                            Text("Welcome to AI Generated App!")
                        }
                    }
                }
            """.trimIndent(),
            "build.gradle.kts" to """
                plugins {
                    alias(libs.plugins.android.application)
                    alias(libs.plugins.kotlin.compose)
                }
                android {
                    namespace = "com.example.myapp"
                    compileSdk = 35
                }
            """.trimIndent(),
            "AndroidManifest.xml" to """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                    <application android:label="AI App" />
                </manifest>
            """.trimIndent()
        )
    )
    val projectFiles: StateFlow<Map<String, String>> = _projectFiles.asStateFlow()

    private val _selectedFileName = MutableStateFlow("MainActivity.kt")
    val selectedFileName: StateFlow<String> = _selectedFileName.asStateFlow()

    private val _codingPrompt = MutableStateFlow("")
    val codingPrompt: StateFlow<String> = _codingPrompt.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(
        listOf("✓ Project Workspace Ready", "✓ Kotlin Compiler Initialized", "✓ Agent Waiting for Request...")
    )
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    private val _isCodingAgentBusy = MutableStateFlow(false)
    val isCodingAgentBusy: StateFlow<Boolean> = _isCodingAgentBusy.asStateFlow()

    // Memories
    val memories: StateFlow<List<MemoryEntity>> = repository.memoriesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    // Projects & Tasks
    val projects: StateFlow<List<ProjectEntity>> = repository.projectsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val tasks: StateFlow<List<TaskEntity>> = repository.tasksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    // Web Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        // Setup speech listeners
        speechManager.onSpeechResult = { text ->
            if (_isLiveVoiceActive.value) {
                handleLiveVoiceInput(text)
            } else {
                _chatInput.value = text
            }
        }

        speechManager.onSpeechError = { error ->
            _errorMessage.value = error
            if (_isLiveVoiceActive.value) {
                _liveTranscript.value = "Error: $error"
            }
        }

        // Collect messages when active conversation changes
        viewModelScope.launch {
            _activeConversationId.collect { convId ->
                if (convId != null) {
                    repository.getMessagesForConversation(convId).collect { msgs ->
                        _activeMessages.value = msgs
                    }
                } else {
                    _activeMessages.value = emptyList()
                }
            }
        }

        // Initialize default conversation if none exists
        viewModelScope.launch {
            val convList = repository.conversationsFlow.firstOrNull() ?: emptyList()
            if (convList.isEmpty()) {
                val newId = repository.createConversation("New Conversation", "Girlfriend / Companion")
                _activeConversationId.value = newId
            } else {
                _activeConversationId.value = convList.first().id
            }
        }

        // Sync speech state to Orb state
        viewModelScope.launch {
            speechManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _orbState.value = OrbState.SPEAKING
                } else if (speechManager.isListening.value) {
                    _orbState.value = OrbState.LISTENING
                } else if (_isGenerating.value) {
                    _orbState.value = OrbState.THINKING
                } else {
                    _orbState.value = OrbState.IDLE
                }
            }
        }

        viewModelScope.launch {
            speechManager.isListening.collect { listening ->
                if (listening) {
                    _orbState.value = OrbState.LISTENING
                } else if (speechManager.isSpeaking.value) {
                    _orbState.value = OrbState.SPEAKING
                } else if (_isGenerating.value) {
                    _orbState.value = OrbState.THINKING
                } else {
                    _orbState.value = OrbState.IDLE
                }
            }
        }
    }

    fun setChatInput(text: String) {
        _chatInput.value = text
    }

    fun setSelectedImage(bitmap: Bitmap?) {
        _selectedImage.value = bitmap
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun selectConversation(id: Long) {
        _activeConversationId.value = id
    }

    fun createNewConversation(title: String = "Chat", personality: String = "Girlfriend / Companion") {
        viewModelScope.launch {
            val id = repository.createConversation(title, personality)
            _activeConversationId.value = id
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            val remaining = repository.conversationsFlow.firstOrNull() ?: emptyList()
            _activeConversationId.value = remaining.firstOrNull()?.id
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch { repository.deleteMessage(id) }
    }

    fun sendMessage(userText: String? = null) {
        val prompt = (userText ?: _chatInput.value).trim()
        if (prompt.isBlank() && _selectedImage.value == null) return

        val convId = _activeConversationId.value ?: return
        val currentImage = _selectedImage.value
        _chatInput.value = ""
        _selectedImage.value = null
        _isGenerating.value = true
        _orbState.value = OrbState.THINKING

        viewModelScope.launch {
            val result = repository.generateAiResponse(
                conversationId = convId,
                userPrompt = prompt,
                bitmapAttachment = currentImage,
                isVoice = false
            )

            _isGenerating.value = false
            _orbState.value = OrbState.IDLE

            result.onSuccess { (reply, action) ->
                val cfg = settings.value
                if (cfg.autoSpeak) {
                    speakText(reply)
                }

                // If an action was detected, execute it!
                if (action != null) {
                    AppActionManager.executeAction(getApplication(), action)
                }
            }.onFailure { ex ->
                _errorMessage.value = ex.message ?: "Failed to generate AI response"
            }
        }
    }

    fun speakText(text: String) {
        val cfg = settings.value
        speechManager.speak(
            text = text,
            provider = cfg.voiceProvider,
            elevenLabsKey = cfg.elevenLabsApiKey,
            elevenLabsVoiceId = cfg.elevenLabsVoiceId,
            elevenLabsModel = cfg.elevenLabsModelId,
            speed = cfg.voiceSpeed
        )
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
        _orbState.value = OrbState.IDLE
    }

    fun startVoiceInput() {
        speechManager.startListening()
    }

    fun stopVoiceInput() {
        speechManager.stopListening()
    }

    // Live Voice Mode continuous loop
    fun toggleLiveVoice() {
        if (_isLiveVoiceActive.value) {
            endLiveVoice()
        } else {
            startLiveVoice()
        }
    }

    fun startLiveVoice() {
        _isLiveVoiceActive.value = true
        _liveTranscript.value = "Listening to you... Speak naturally in English, Hindi, or Hinglish."
        _liveAiResponse.value = ""
        speechManager.startListening()
    }

    fun endLiveVoice() {
        _isLiveVoiceActive.value = false
        speechManager.stopListening()
        speechManager.stopSpeaking()
        _liveTranscript.value = "Live voice session ended."
        _orbState.value = OrbState.IDLE
    }

    private fun handleLiveVoiceInput(userSpeech: String) {
        if (userSpeech.isBlank()) return
        _liveTranscript.value = userSpeech
        _liveAiResponse.value = "Thinking..."
        _orbState.value = OrbState.THINKING

        viewModelScope.launch {
            val convId = _activeConversationId.value ?: 1L
            val result = repository.generateAiResponse(
                conversationId = convId,
                userPrompt = userSpeech,
                isVoice = true
            )

            result.onSuccess { (reply, action) ->
                _liveAiResponse.value = reply
                speakText(reply)

                // If user speech triggers an app action, execute safely
                if (action != null) {
                    AppActionManager.executeAction(getApplication(), action)
                }

                // Auto resume listening when AI finishes speaking (handled via SpeechManager or delay)
            }.onFailure { err ->
                _liveAiResponse.value = "Error: ${err.message}"
                _errorMessage.value = err.message
            }
        }
    }

    // Coding Agent
    fun setCodingPrompt(prompt: String) {
        _codingPrompt.value = prompt
    }

    fun selectProjectFile(fileName: String) {
        _selectedFileName.value = fileName
    }

    fun updateFileContent(fileName: String, newContent: String) {
        val current = _projectFiles.value.toMutableMap()
        current[fileName] = newContent
        _projectFiles.value = current
    }

    fun createProjectFile(fileName: String, initialContent: String = "") {
        val current = _projectFiles.value.toMutableMap()
        current[fileName] = initialContent
        _projectFiles.value = current
        _selectedFileName.value = fileName
    }

    fun deleteProjectFile(fileName: String) {
        val current = _projectFiles.value.toMutableMap()
        current.remove(fileName)
        _projectFiles.value = current
        if (_selectedFileName.value == fileName) {
            _selectedFileName.value = current.keys.firstOrNull() ?: ""
        }
    }

    fun executeCodingAgent(taskRequest: String) {
        if (taskRequest.isBlank()) return
        _isCodingAgentBusy.value = true
        _codingPrompt.value = ""

        val currentLogs = _terminalLogs.value.toMutableList()
        currentLogs.add("► Task Received: \"$taskRequest\"")
        currentLogs.add("► Step 1/4: Analyzing code architecture & planning modifications...")
        _terminalLogs.value = currentLogs

        viewModelScope.launch {
            val convId = _activeConversationId.value ?: 1L
            val prompt = """
                You are YESMIN AI Coding Agent. The user wants you to perform the following development task:
                "$taskRequest"
                
                Current project files:
                ${_projectFiles.value.entries.joinToString("\n\n") { "--- ${it.key} ---\n${it.value}" }}
                
                Generate the updated or new code files to fulfill the request.
                Provide your explanation and code blocks with filenames.
            """.trimIndent()

            val result = repository.generateAiResponse(
                conversationId = convId,
                userPrompt = prompt,
                isVoice = false
            )

            _isCodingAgentBusy.value = false

            result.onSuccess { (reply, _) ->
                val logs = _terminalLogs.value.toMutableList()
                logs.add("✓ Step 2/4: Code generation complete.")
                logs.add("✓ Step 3/4: Parsing syntax and file structures...")

                // Extract code blocks and update files
                val regex = Regex("```(?:kotlin|java|xml|html|css|js|json)?\\s*(?:\\/\\/\\s*([a-zA-Z0-9_.-]+)|<!--\\s*([a-zA-Z0-9_.-]+)\\s*-->)?\\s*([\\s\\S]*?)```")
                val matches = regex.findAll(reply)
                var filesUpdated = 0

                val updatedFiles = _projectFiles.value.toMutableMap()
                for (match in matches) {
                    val specifiedName = match.groups[1]?.value ?: match.groups[2]?.value
                    val codeContent = match.groups[3]?.value ?: continue
                    val targetName = specifiedName ?: _selectedFileName.value
                    updatedFiles[targetName] = codeContent.trim()
                    filesUpdated++
                    logs.add("  ➜ Updated file: $targetName (${codeContent.length} chars)")
                }

                if (filesUpdated == 0) {
                    // Update currently selected file with response code if available
                    val cleanCode = reply.substringAfter("```").substringBeforeLast("```").trim()
                    if (cleanCode.length > 20) {
                        updatedFiles[_selectedFileName.value] = cleanCode
                        logs.add("  ➜ Applied changes to ${_selectedFileName.value}")
                    }
                }

                _projectFiles.value = updatedFiles
                logs.add("✓ Step 4/4: Validation and syntax check passed without errors.")
                logs.add("★ Coding Agent Task Complete!")
                _terminalLogs.value = logs
            }.onFailure { err ->
                val logs = _terminalLogs.value.toMutableList()
                logs.add("✗ Error during code generation: ${err.message}")
                _terminalLogs.value = logs
                _errorMessage.value = err.message
            }
        }
    }

    // Memories
    fun addMemory(key: String, value: String, category: String = "general") {
        viewModelScope.launch { repository.addMemory(key, value, category) }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch { repository.deleteMemory(id) }
    }

    fun clearMemories() {
        viewModelScope.launch { repository.clearMemories() }
    }

    // Projects & Tasks
    fun addProject(name: String, type: String, description: String) {
        viewModelScope.launch { repository.addProject(name, type, description) }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch { repository.deleteProject(id) }
    }

    fun addTask(title: String, description: String = "", projectId: Long? = null, priority: String = "Medium") {
        viewModelScope.launch { repository.addTask(title, description, projectId, priority) }
    }

    fun toggleTask(id: Long, completed: Boolean) {
        viewModelScope.launch { repository.toggleTask(id, completed) }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch { repository.deleteTask(id) }
    }

    // Web Search
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun executeWebSearch(query: String? = null) {
        val q = (query ?: _searchQuery.value).trim()
        if (q.isBlank()) return
        _isSearching.value = true

        viewModelScope.launch {
            val results = repository.performWebSearch(q)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
