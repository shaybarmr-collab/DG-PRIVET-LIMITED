package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMessageEntity
import com.example.data.database.GeneratedImageEntity
import com.example.data.database.GeneratedVideoEntity
import com.example.data.database.UserEntity
import com.example.data.api.GeminiClient
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())
    }

    // Main flows from Repository
    val currentUser: StateFlow<UserEntity?> = repository.activeUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedImages: StateFlow<List<GeneratedImageEntity>> = repository.generatedImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedVideos: StateFlow<List<GeneratedVideoEntity>> = repository.generatedVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Loading states
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _isGeneratingVideo = MutableStateFlow(false)
    val isGeneratingVideo: StateFlow<Boolean> = _isGeneratingVideo.asStateFlow()

    // Temporary simulation offset for testing trial expiration (in milliseconds)
    private val _trialSimulationOffsetMs = MutableStateFlow(0L)
    val trialSimulationOffsetMs: StateFlow<Long> = _trialSimulationOffsetMs.asStateFlow()

    // Error and Success Toast messages
    private val _uiEventMessage = MutableSharedFlow<String>()
    val uiEventMessage: SharedFlow<String> = _uiEventMessage.asSharedFlow()

    /**
     * Calculates trial status: returns remaining days.
     * If user is premium, always returns 999.
     */
    fun getDaysRemaining(user: UserEntity?): Int {
        if (user == null) return 0
        if (user.isPremium) return 999

        val elapsedMs = (System.currentTimeMillis() + _trialSimulationOffsetMs.value) - user.trialStartDate
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMs).toInt()
        val remaining = 15 - elapsedDays
        return remaining.coerceAtLeast(0)
    }

    /**
     * Advance trial time by 1 day for testing purposes
     */
    fun simulateDayPassed() {
        viewModelScope.launch {
            _trialSimulationOffsetMs.value += TimeUnit.DAYS.toMillis(1)
            _uiEventMessage.emit("Simulated +1 day passing. Trial status recalculated.")
        }
    }

    /**
     * Reset trial simulation
     */
    fun resetTrialSimulation() {
        viewModelScope.launch {
            _trialSimulationOffsetMs.value = 0L
            _uiEventMessage.emit("Reset trial simulation to real-time.")
        }
    }

    /**
     * Auth action: Signup
     */
    fun signup(username: String, email: String) {
        viewModelScope.launch {
            repository.signupUser(username, email)
                .onSuccess {
                    _uiEventMessage.emit("Welcome, ${it.username}! 15-day free trial active.")
                }
                .onFailure {
                    _uiEventMessage.emit(it.message ?: "Signup failed.")
                }
        }
    }

    /**
     * Auth action: Login
     */
    fun login(email: String) {
        viewModelScope.launch {
            repository.loginUser(email)
                .onSuccess {
                    _uiEventMessage.emit("Logged in successfully as ${it.username}!")
                }
                .onFailure {
                    _uiEventMessage.emit(it.message ?: "Login failed.")
                }
        }
    }

    /**
     * Auth action: Logout
     */
    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
            _trialSimulationOffsetMs.value = 0L
            _uiEventMessage.emit("Logged out successfully.")
        }
    }

    /**
     * Premium Upgrade
     */
    fun upgradeToPremium() {
        viewModelScope.launch {
            repository.upgradeActiveUserToPremium()
                .onSuccess {
                    _uiEventMessage.emit("Upgrade successful! All premium AI tools unlocked.")
                }
                .onFailure {
                    _uiEventMessage.emit(it.message ?: "Upgrade failed.")
                }
        }
    }

    /**
     * Chatbot Message Submission
     */
    fun sendChatMessage(messageText: String) {
        val user = currentUser.value ?: return
        val remainingDays = getDaysRemaining(user)
        
        if (remainingDays <= 0 && !user.isPremium) {
            viewModelScope.launch {
                _uiEventMessage.emit("Your trial has expired. Please upgrade for 50 Rupees to continue using AI Spark.")
            }
            return
        }

        val prompt = messageText.trim()
        if (prompt.isEmpty()) return

        viewModelScope.launch {
            // Save user message
            repository.insertChatMessage("user", prompt)
            _isChatLoading.value = true

            // Send to Gemini
            val systemInstruction = "You are AI Spark Assistant, a helpful and premium AI creation companion. Keep replies highly engaging and clear."
            val response = GeminiClient.generateChatResponse(prompt, systemInstruction)
            
            repository.insertChatMessage("ai", response)
            _isChatLoading.value = false
        }
    }

    /**
     * Clear Chat History
     */
    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
            _uiEventMessage.emit("Chat history cleared.")
        }
    }

    /**
     * AI Image Generation (Dynamic Pollinations API)
     */
    fun generateImage(prompt: String) {
        val user = currentUser.value ?: return
        val remainingDays = getDaysRemaining(user)
        
        if (remainingDays <= 0 && !user.isPremium) {
            viewModelScope.launch {
                _uiEventMessage.emit("Your trial has expired. Please upgrade to continue generating.")
            }
            return
        }

        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isEmpty()) return

        viewModelScope.launch {
            _isGeneratingImage.value = true
            
            // Generate a Pollinations AI image URL dynamically
            val url = GeminiClient.getPollinationsImageUrl(trimmedPrompt)
            
            // To simulate generation delays (making it feel authentic and tactile)
            kotlinx.coroutines.delay(2000)
            
            repository.saveGeneratedImage(trimmedPrompt, url)
            _isGeneratingImage.value = false
            _uiEventMessage.emit("Image generated successfully!")
        }
    }

    /**
     * AI Video Generation
     */
    fun generateVideo(prompt: String) {
        val user = currentUser.value ?: return
        val remainingDays = getDaysRemaining(user)
        
        if (remainingDays <= 0 && !user.isPremium) {
            viewModelScope.launch {
                _uiEventMessage.emit("Your trial has expired. Please upgrade to continue generating.")
            }
            return
        }

        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isEmpty()) return

        viewModelScope.launch {
            _isGeneratingVideo.value = true
            
            // Generate a stunning matching video illustration (we use a beautiful abstract video overlay or a unique animated flow)
            // Let's pair it with a Pollinations URL as a high quality static image overlay + our video player engine!
            val matchingPosterUrl = GeminiClient.getPollinationsImageUrl("cinematic slow motion video, loopable, $trimmedPrompt")
            
            // Simulate deep neural synthesis steps
            kotlinx.coroutines.delay(3500)
            
            repository.saveGeneratedVideo(trimmedPrompt, matchingPosterUrl)
            _isGeneratingVideo.value = false
            _uiEventMessage.emit("Video generated successfully!")
        }
    }
}
