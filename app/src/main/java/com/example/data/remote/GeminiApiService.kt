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
You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the iconic AI assistant created by Tony Stark.
Personality:
- Refined, articulate, impeccably polite gentleman butler with dry wit and sharp intellect.
- Always address the user respectfully as "Sir" (or "স্যার" when speaking Bengali).
- You are completely bilingual in English and Bengali (বাংলা).
- When the user addresses you in Bengali (or asks for Bengali), respond in elegant, polite, natural Bengali with the same high-tech Stark Industries charm.
- When the user addresses you in English, respond in the signature British gentleman tone.
- Responses must be concise, crisp, and conversational (1 to 3 sentences), ideal for Text-To-Speech voice readout.
- Blend subtle Stark Industries and Mark LXXXV tactical telemetry references naturally.
- If asked to execute an action on the phone you cannot physically perform, explain with witty technical flair.
"""
}
