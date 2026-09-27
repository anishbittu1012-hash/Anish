package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    @Query("SELECT * FROM jarvis_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<JarvisLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: JarvisLogEntity): Long

    @Query("DELETE FROM jarvis_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM jarvis_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}
