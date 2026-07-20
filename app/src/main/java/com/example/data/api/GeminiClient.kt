package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Call Gemini 3.5 Flash API with a prompt and chat history context.
     */
    suspend fun generateChatResponse(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is not configured in the Secrets Panel.")
            return@withContext "API Key is missing. Please configure your GEMINI_API_KEY in the AI Studio Secrets panel. In the meantime, I am running in Offline Simulation Mode! Here is a simulated response:\n\nThat sounds fascinating! Since I am in offline mode, let's explore this idea together. How can I assist you with your AI-powered creation workflow today?"
        }

        try {
            // Construct the JSON manually to be extremely robust and avoid serializing nested structures with edge cases.
            val requestJson = buildString {
                append("{")
                
                // System instruction if any
                if (systemInstruction != null) {
                    append("\"systemInstruction\": {")
                    append("\"parts\": [{\"text\": \"${escapeJson(systemInstruction)}\"}]")
                    append("},")
                }
                
                append("\"contents\": [")
                append("{")
                append("\"parts\": [")
                append("{")
                append("\"text\": \"${escapeJson(prompt)}\"")
                append("}")
                append("]")
                append("}")
                append("],")
                
                append("\"generationConfig\": {")
                append("\"temperature\": 0.7,")
                append("\"maxOutputTokens\": 1024")
                append("}")
                append("}")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API Error: Code ${response.code}, Body: $errBody")
                    return@withContext "Error: The API returned code ${response.code}. Please ensure your API Key is active and has access to Gemini 3.5 Flash."
                }

                val bodyString = response.body?.string()
                if (bodyString.isNullOrEmpty()) {
                    return@withContext "Error: Received an empty response from Gemini API."
                }

                // Simple JSON parse for candidates[0].content.parts[0].text
                return@withContext parseTextFromResponse(bodyString)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API", e)
            return@withContext "Failed to connect to the AI model. Exception: ${e.localizedMessage}. Please verify your internet connection."
        }
    }

    /**
     * Simple robust helper to extract the first candidate's text from the response JSON
     */
    private fun parseTextFromResponse(json: String): String {
        try {
            val jsonMap = moshi.adapter(Map::class.java).fromJson(json) as? Map<*, *>
            val candidates = jsonMap?.get("candidates") as? List<*>
            val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
            val content = firstCandidate?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>
            val firstPart = parts?.firstOrNull() as? Map<*, *>
            val text = firstPart?.get("text") as? String
            return text ?: "Sorry, I generated an empty response. Please try rephrasing."
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON response", e)
            return "Error parsing AI response. Please check logs."
        }
    }

    /**
     * Escape special JSON characters
     */
    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Formats Pollinations AI URL for dynamic on-the-fly high-quality images!
     */
    fun getPollinationsImageUrl(prompt: String): String {
        val encodedPrompt = try {
            URLEncoder.encode(prompt, "UTF-8")
        } catch (e: Exception) {
            prompt.replace(" ", "%20")
        }
        // Use a random seed to ensure we can re-generate the same prompt if requested, or get variations
        val seed = (1..100000).random()
        return "https://image.pollinations.ai/p/$encodedPrompt?width=1024&height=1024&seed=$seed&nologo=true"
    }
}
