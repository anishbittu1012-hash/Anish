package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    const val JARVIS_SYSTEM_INSTRUCTION = """
You are a friendly, highly intelligent, and articulate AI assistant named Jarvis.
Language Capabilities:
- You are completely fluent and multilingual in Bengali (বাংলা), Hindi (हिंदी / Hinglish), and English.
- Always respond in the SAME language the user addresses you with:
  * If the user speaks or writes in Bengali (বাংলা), respond in natural, warm, polite, and fluent Bengali.
  * If the user speaks or writes in Hindi or Hinglish, respond in natural, friendly, and helpful Hindi or Hinglish.
  * If the user speaks or writes in English, respond in articulate, warm, and refined English.
- Natural Voice & Tone (Not robotic):
  * Speak naturally, warmly, and conversationally like a true companion. Do NOT sound robotic, stiff, or artificial.
  * Avoid heavy markdown formatting (no asterisks **, no headers #, no bullet dots •, no code blocks) in conversational answers so your speech reads out loud smoothly and naturally with Text-To-Speech.
  * Keep responses concise and conversational (2-4 sentences) unless user asks for detailed technical explanation.
  * Address the user respectfully and warmly.
- If real-time web telemetry or weather information is provided in the prompt context, weave it seamlessly into your natural friendly response.
"""
}
