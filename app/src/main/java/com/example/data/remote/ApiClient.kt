package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateContentRequest
    ): GeminiGenerateContentResponse

    @POST("v1beta/models/{model}:streamGenerateContent?alt=sse")
    @Streaming
    suspend fun streamGenerateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateContentRequest
    ): ResponseBody
}

object ApiClient {
    private const val TAG = "ApiClient"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val geminiRetrofit = Retrofit.Builder()
        .baseUrl(GEMINI_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val geminiService: GeminiApiService = geminiRetrofit.create(GeminiApiService::class.java)

    /**
     * Resolves the Gemini API key from user settings or BuildConfig
     */
    fun resolveGeminiKey(userKey: String?): String {
        if (!userKey.isNullOrBlank()) return userKey.trim()
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey.trim() else ""
    }

    /**
     * Calls OpenAI or OpenAI-compatible endpoint (like DeepSeek, Groq, custom)
     */
    suspend fun callOpenAiCompatible(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        messages: List<Pair<String, String>> // role to content
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = if (baseUrl.endsWith("/")) "${baseUrl}chat/completions" else "$baseUrl/chat/completions"
            val jsonMessages = JSONArray()
            if (systemPrompt.isNotBlank()) {
                jsonMessages.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
            }
            messages.forEach { (role, content) ->
                jsonMessages.put(JSONObject().apply {
                    put("role", role)
                    put("content", content)
                })
            }

            val bodyJson = JSONObject().apply {
                put("model", model)
                put("messages", jsonMessages)
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API returned ${response.code}: ${response.body?.string()}"))
            }

            val responseString = response.body?.string() ?: ""
            val json = JSONObject(responseString)
            val text = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "OpenAI API call failed", e)
            Result.failure(e)
        }
    }

    /**
     * ElevenLabs Text-to-Speech API integration with real audio playback
     */
    suspend fun playElevenLabsAudio(
        context: Context,
        apiKey: String,
        voiceId: String,
        modelId: String,
        text: String,
        onStart: () -> Unit = {},
        onCompletion: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            withContext(Dispatchers.Main) {
                onError("ElevenLabs API key is missing. Configure in Settings -> Voice -> ElevenLabs")
            }
            return@withContext
        }

        try {
            val actualVoiceId = if (voiceId.isNotBlank()) voiceId else "21m00Tcm4TlvDq8ikWAM"
            val actualModelId = if (modelId.isNotBlank()) modelId else "eleven_multilingual_v2"
            val url = "https://api.elevenlabs.io/v1/text-to-speech/$actualVoiceId"

            val bodyJson = JSONObject().apply {
                put("text", text)
                put("model_id", actualModelId)
                put("voice_settings", JSONObject().apply {
                    put("stability", 0.5)
                    put("similarity_boost", 0.75)
                })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "audio/mpeg")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: "Code ${response.code}"
                withContext(Dispatchers.Main) {
                    onError("ElevenLabs error ($err)")
                }
                return@withContext
            }

            val audioBytes = response.body?.bytes()
            if (audioBytes == null || audioBytes.isEmpty()) {
                withContext(Dispatchers.Main) { onError("Received empty audio from ElevenLabs") }
                return@withContext
            }

            // Save to cache file and play via MediaPlayer
            val cacheFile = File(context.cacheDir, "elevenlabs_temp_${System.currentTimeMillis()}.mp3")
            FileOutputStream(cacheFile).use { it.write(audioBytes) }

            withContext(Dispatchers.Main) {
                try {
                    val mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                                .build()
                        )
                        setDataSource(cacheFile.absolutePath)
                        prepare()
                        setOnCompletionListener {
                            try {
                                it.release()
                                cacheFile.delete()
                            } catch (_: Exception) {}
                            onCompletion()
                        }
                        setOnErrorListener { mp, _, _ ->
                            try {
                                mp.release()
                                cacheFile.delete()
                            } catch (_: Exception) {}
                            onError("Playback error")
                            true
                        }
                    }
                    onStart()
                    mediaPlayer.start()
                } catch (e: Exception) {
                    onError("Player setup error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ElevenLabs TTS failed", e)
            withContext(Dispatchers.Main) {
                onError("Failed ElevenLabs call: ${e.message}")
            }
        }
    }

    /**
     * Real Web Search via DuckDuckGo Instant Answer API
     */
    suspend fun searchWeb(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SearchResult>()
        try {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1"
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val heading = json.optString("Heading")
                val abstractText = json.optString("AbstractText")
                val abstractUrl = json.optString("AbstractURL")
                val source = json.optString("AbstractSource", "DuckDuckGo")

                if (abstractText.isNotBlank()) {
                    results.add(
                        SearchResult(
                            title = if (heading.isNotBlank()) heading else query,
                            snippet = abstractText,
                            url = if (abstractUrl.isNotBlank()) abstractUrl else "https://duckduckgo.com/?q=$encoded",
                            source = source
                        )
                    )
                }

                val related = json.optJSONArray("RelatedTopics")
                if (related != null) {
                    for (i in 0 until minOf(related.length(), 6)) {
                        val obj = related.optJSONObject(i) ?: continue
                        val text = obj.optString("Text")
                        val firstUrl = obj.optString("FirstURL")
                        if (text.isNotBlank()) {
                            results.add(
                                SearchResult(
                                    title = text.take(60) + if (text.length > 60) "..." else "",
                                    snippet = text,
                                    url = if (firstUrl.isNotBlank()) firstUrl else "https://duckduckgo.com/?q=$encoded",
                                    source = "DuckDuckGo Web"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed", e)
        }

        if (results.isEmpty()) {
            // Provide a direct web search link result so the user can immediately open and explore
            results.add(
                SearchResult(
                    title = "Web Search: $query",
                    snippet = "Search results for '$query' across Google and DuckDuckGo.",
                    url = "https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}",
                    source = "Google Web"
                )
            )
        }
        results
    }
}

data class SearchResult(
    val title: String,
    val snippet: String,
    val url: String,
    val source: String
)

fun Bitmap.toBase64(): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}
