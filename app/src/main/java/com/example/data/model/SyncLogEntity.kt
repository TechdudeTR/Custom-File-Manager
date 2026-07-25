package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: String,
    val fileName: String,
    val syncType: String, // UPLOAD, DOWNLOAD, DELETE, AUTO_SYNC
    val status: String,   // SUCCESS, FAILED, IN_PROGRESS
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)
