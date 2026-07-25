package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Query("SELECT * FROM files ORDER BY isDirectory DESC, name ASC")
    fun getAllFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE parentPath = :parentPath ORDER BY isDirectory DESC, name ASC")
    fun getFilesByParentPath(parentPath: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE category = :category ORDER BY name ASC")
    fun getFilesByCategory(category: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isHidden = 1 OR isSystemFile = 1 ORDER BY name ASC")
    fun getHiddenAndSystemFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isEncrypted = 1 ORDER BY name ASC")
    fun getEncryptedFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isOfflineAvailable = 1 ORDER BY name ASC")
    fun getOfflineFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE isCloudSynced = 1 ORDER BY name ASC")
    fun getCloudSyncedFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFiles(query: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("DELETE FROM files WHERE id IN (:ids)")
    suspend fun deleteFilesByIds(ids: List<String>)

    @Query("DELETE FROM files")
    suspend fun deleteAllFiles()
}
