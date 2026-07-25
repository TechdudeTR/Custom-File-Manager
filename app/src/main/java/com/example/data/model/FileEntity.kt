package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val path: String,
    val parentPath: String,
    val size: Long,
    val mimeType: String,
    val category: String,
    val isDirectory: Boolean = false,
    val isHidden: Boolean = false,
    val isSystemFile: Boolean = false,
    val isEncrypted: Boolean = false,
    val isCloudSynced: Boolean = false,
    val isOfflineAvailable: Boolean = true,
    val syncStatus: String = "SYNCED", // SYNCED, PENDING, CLOUD_ONLY, ERROR
    val tags: String = "", // Comma-separated tag names
    val lastModified: Long = System.currentTimeMillis(),
    val checksum: String = "",
    val permissions: String = "-rw-r--r--"
)
