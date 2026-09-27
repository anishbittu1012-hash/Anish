package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jarvis_logs")
data class JarvisLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val response: String,
    val category: String, // "VOICE", "APP_LAUNCH", "SETTINGS", "PROTOCOL", "AI"
    val timestamp: Long = System.currentTimeMillis()
)
