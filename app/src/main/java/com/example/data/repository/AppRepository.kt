package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val appDao: AppDao) {

    // Reactive streams for active user, messages, images, and videos
    val activeUser: Flow<UserEntity?> = appDao.getActiveUserFlow()
    val chatMessages: Flow<List<ChatMessageEntity>> = appDao.getChatMessages()
    val generatedImages: Flow<List<GeneratedImageEntity>> = appDao.getGeneratedImages()
    val generatedVideos: Flow<List<GeneratedVideoEntity>> = appDao.getGeneratedVideos()

    /**
     * Signs up a new user, automatically logging them in and setting active.
     * Starts with a 15-day free trial.
     */
    suspend fun signupUser(username: String, email: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim().lowercase()
            val trimmedUsername = username.trim()

            if (trimmedEmail.isEmpty() || trimmedUsername.isEmpty()) {
                return@withContext Result.failure(Exception("Username and email cannot be empty."))
            }

            // Check if user already exists
            val existing = appDao.getUserByEmail(trimmedEmail)
            if (existing != null) {
                return@withContext Result.failure(Exception("An account with this email already exists. Please login instead."))
            }

            // Clear other active users first
            appDao.clearAllActiveUsers()

            val newUser = UserEntity(
                email = trimmedEmail,
                username = trimmedUsername,
                isPremium = false,
                trialStartDate = System.currentTimeMillis(),
                isActive = true
            )

            appDao.insertUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logs in an existing user by email.
     */
    suspend fun loginUser(email: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail.isEmpty()) {
                return@withContext Result.failure(Exception("Email cannot be empty."))
            }

            val user = appDao.getUserByEmail(trimmedEmail)
                ?: return@withContext Result.failure(Exception("No account found with this email. Please sign up!"))

            // Clear other active users
            appDao.clearAllActiveUsers()

            // Update user as active
            val updatedUser = user.copy(isActive = true)
            appDao.insertUser(updatedUser)

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Log out current user
     */
    suspend fun logoutUser() = withContext(Dispatchers.IO) {
        appDao.clearAllActiveUsers()
        appDao.clearChatHistory()
    }

    /**
     * Upgrades the currently active user to Premium (removes 15-day trial constraint).
     */
    suspend fun upgradeActiveUserToPremium(): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val active = appDao.getActiveUser()
                ?: return@withContext Result.failure(Exception("No active user logged in."))

            val upgraded = active.copy(isPremium = true)
            appDao.updateUser(upgraded)

            Result.success(upgraded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Chat message actions
     */
    suspend fun insertChatMessage(sender: String, message: String) = withContext(Dispatchers.IO) {
        appDao.insertChatMessage(
            ChatMessageEntity(
                sender = sender,
                message = message
            )
        )
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        appDao.clearChatHistory()
    }

    /**
     * Image Generation persistence
     */
    suspend fun saveGeneratedImage(prompt: String, imageUrl: String) = withContext(Dispatchers.IO) {
        appDao.insertGeneratedImage(
            GeneratedImageEntity(
                prompt = prompt,
                imageUrl = imageUrl
            )
        )
    }

    /**
     * Video Generation persistence
     */
    suspend fun saveGeneratedVideo(prompt: String, videoUrl: String) = withContext(Dispatchers.IO) {
        appDao.insertGeneratedVideo(
            GeneratedVideoEntity(
                prompt = prompt,
                videoUrl = videoUrl
            )
        )
    }
}
