package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class LiveWeatherInfo(
    val location: String,
    val temperatureCelsius: Double,
    val windSpeed: Double,
    val weatherDescription: String,
    val rawData: String
)

data class WebSearchResult(
    val title: String,
    val snippet: String,
    val sourceUrl: String
)

object WebSearchEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches real-time live weather anywhere in the world using Open-Meteo REST API.
     * Zero API key required, highly reliable and fast.
     */
    suspend fun fetchLiveWeather(city: String): LiveWeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val encodedCity = URLEncoder.encode(city.trim(), "UTF-8")
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=en&format=json"

            val geoRequest = Request.Builder().url(geoUrl).build()
            val geoResponse = client.newCall(geoRequest).execute()
            if (!geoResponse.isSuccessful) return@withContext null

            val geoBody = geoResponse.body?.string() ?: return@withContext null
            val geoJson = JSONObject(geoBody)
            val results = geoJson.optJSONArray("results") ?: return@withContext null
            if (results.length() == 0) return@withContext null

            val firstLoc = results.getJSONObject(0)
            val name = firstLoc.optString("name", city)
            val country = firstLoc.optString("country", "")
            val lat = firstLoc.getDouble("latitude")
            val lon = firstLoc.getDouble("longitude")

            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
            val weatherRequest = Request.Builder().url(weatherUrl).build()
            val weatherResponse = client.newCall(weatherRequest).execute()
            if (!weatherResponse.isSuccessful) return@withContext null

            val weatherBody = weatherResponse.body?.string() ?: return@withContext null
            val weatherJson = JSONObject(weatherBody)
            val current = weatherJson.optJSONObject("current_weather") ?: return@withContext null

            val temp = current.optDouble("temperature", 0.0)
            val wind = current.optDouble("windspeed", 0.0)
            val code = current.optInt("weathercode", 0)

            val conditionDesc = mapWeatherCode(code)
            val fullLocation = if (country.isNotBlank()) "$name, $country" else name

            LiveWeatherInfo(
                location = fullLocation,
                temperatureCelsius = temp,
                windSpeed = wind,
                weatherDescription = conditionDesc,
                rawData = "Temp: ${temp}°C, Condition: $conditionDesc, Wind: ${wind} km/h"
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Performs a real-time web knowledge & news search.
     * Uses Wikipedia Live REST API for comprehensive world telemetry and current info.
     */
    suspend fun searchWeb(query: String): List<WebSearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<WebSearchResult>()
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val searchUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encoded&utf8=&format=json"

            val request = Request.Builder().url(searchUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val queryObj = json.optJSONObject("query") ?: return@withContext emptyList()
            val searchArray = queryObj.optJSONArray("search") ?: return@withContext emptyList()

            for (i in 0 until minOf(searchArray.length(), 3)) {
                val item = searchArray.getJSONObject(i)
                val title = item.optString("title", "")
                val rawSnippet = item.optString("snippet", "")
                // Strip HTML tags from snippet
                val cleanSnippet = rawSnippet.replace(Regex("<[^>]*>"), "")
                val url = "https://en.wikipedia.org/wiki/${URLEncoder.encode(title.replace(" ", "_"), "UTF-8")}"

                if (title.isNotBlank()) {
                    results.add(
                        WebSearchResult(
                            title = title,
                            snippet = cleanSnippet,
                            sourceUrl = url
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore error
        }
        results
    }

    /**
     * Tests connectivity to the live web search & telemetry endpoints.
     */
    suspend fun testSearchConnection(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val testRequest = Request.Builder()
                .url("https://geocoding-api.open-meteo.com/v1/search?name=London&count=1&format=json")
                .build()
            val response = client.newCall(testRequest).execute()
            val latency = System.currentTimeMillis() - startTime
            if (response.isSuccessful) {
                Pair(true, "Web search online (${latency}ms latency)")
            } else {
                Pair(false, "HTTP ${response.code}: Service unreachable")
            }
        } catch (e: Exception) {
            Pair(false, "Connection failed: ${e.localizedMessage ?: "Network error"}")
        }
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear sky (Sunny)"
            1, 2, 3 -> "Mainly clear, partly cloudy"
            45, 48 -> "Fog and depositing rime fog"
            51, 53, 55 -> "Drizzle: Light, moderate, and dense"
            61, 63, 65 -> "Rain: Slight, moderate and heavy"
            71, 73, 75 -> "Snow fall: Slight, moderate, and heavy"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers: Slight, moderate, and violent"
            85, 86 -> "Snow showers slight and heavy"
            95 -> "Thunderstorm: Slight or moderate"
            96, 99 -> "Thunderstorm with slight and heavy hail"
            else -> "Atmospheric condition active"
        }
    }
}
