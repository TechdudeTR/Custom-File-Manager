package com.example.data.repository

import com.example.data.db.FileDao
import com.example.data.db.SyncLogDao
import com.example.data.db.TagDao
import com.example.data.db.VaultDao
import com.example.data.model.FileCategory
import com.example.data.model.FileEntity
import com.example.data.model.SyncLogEntity
import com.example.data.model.TagEntity
import com.example.data.model.VaultConfig
import com.example.data.security.CryptoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class FileRepository(
    private val fileDao: FileDao,
    private val tagDao: TagDao,
    private val syncLogDao: SyncLogDao,
    private val vaultDao: VaultDao
) {

    val allFiles: Flow<List<FileEntity>> = fileDao.getAllFiles()
    val allTags: Flow<List<TagEntity>> = tagDao.getAllTags()
    val syncLogs: Flow<List<SyncLogEntity>> = syncLogDao.getAllSyncLogs()
    val vaultConfig: Flow<VaultConfig?> = vaultDao.getVaultConfig()

    fun getFilesByParentPath(parentPath: String): Flow<List<FileEntity>> =
        fileDao.getFilesByParentPath(parentPath)

    fun getFilesByCategory(category: String): Flow<List<FileEntity>> =
        fileDao.getFilesByCategory(category)

    fun getHiddenAndSystemFiles(): Flow<List<FileEntity>> =
        fileDao.getHiddenAndSystemFiles()

    fun getEncryptedFiles(): Flow<List<FileEntity>> =
        fileDao.getEncryptedFiles()

    fun getOfflineFiles(): Flow<List<FileEntity>> =
        fileDao.getOfflineFiles()

    fun getCloudSyncedFiles(): Flow<List<FileEntity>> =
        fileDao.getCloudSyncedFiles()

    fun searchFiles(query: String): Flow<List<FileEntity>> =
        fileDao.searchFiles(query)

    suspend fun createFolder(name: String, parentPath: String, isHidden: Boolean = false): FileEntity = withContext(Dispatchers.IO) {
        val folderPath = if (parentPath.endsWith("/")) "$parentPath$name" else "$parentPath/$name"
        val folder = FileEntity(
            id = "f_" + UUID.randomUUID().toString().take(8),
            name = name,
            path = folderPath,
            parentPath = parentPath,
            size = 0,
            mimeType = "resource/folder",
            category = if (isHidden) "SYSTEM" else "FOLDER",
            isDirectory = true,
            isHidden = isHidden || name.startsWith("."),
            isSystemFile = parentPath.startsWith("/system") || parentPath.startsWith("/data")
        )
        fileDao.insertFile(folder)
        folder
    }

    suspend fun createFile(
        name: String,
        parentPath: String,
        sizeBytes: Long = 1024,
        mimeType: String = "text/plain",
        tags: List<String> = emptyList(),
        isEncrypted: Boolean = false
    ): FileEntity = withContext(Dispatchers.IO) {
        val ext = name.substringAfterLast('.', "")
        val isHidden = name.startsWith(".") || parentPath.contains(".sys")
        val isSys = parentPath.startsWith("/system") || parentPath.startsWith("/data")
        val filePath = if (parentPath.endsWith("/")) "$parentPath$name" else "$parentPath/$name"

        val category = FileCategory.fromExtension(ext, isDir = false, isHiddenOrSys = isHidden || isSys, isVault = isEncrypted).name

        val file = FileEntity(
            id = "file_" + UUID.randomUUID().toString().take(8),
            name = name,
            path = filePath,
            parentPath = parentPath,
            size = sizeBytes,
            mimeType = mimeType,
            category = category,
            isDirectory = false,
            isHidden = isHidden,
            isSystemFile = isSys,
            isEncrypted = isEncrypted,
            isCloudSynced = false,
            isOfflineAvailable = true,
            tags = tags.joinToString(","),
            checksum = CryptoEngine.generateChecksum(name + filePath + System.currentTimeMillis())
        )
        fileDao.insertFile(file)
        file
    }

    suspend fun updateFileTags(fileId: String, newTags: List<String>) = withContext(Dispatchers.IO) {
        val file = fileDao.getFileById(fileId) ?: return@withContext
        val updated = file.copy(tags = newTags.joinToString(","))
        fileDao.updateFile(updated)
    }

    suspend fun toggleOfflineAccess(fileId: String) = withContext(Dispatchers.IO) {
        val file = fileDao.getFileById(fileId) ?: return@withContext
        val updated = file.copy(
            isOfflineAvailable = !file.isOfflineAvailable,
            syncStatus = if (!file.isOfflineAvailable) "SYNCED" else file.syncStatus
        )
        fileDao.updateFile(updated)
    }

    suspend fun toggleCloudSync(fileId: String) = withContext(Dispatchers.IO) {
        val file = fileDao.getFileById(fileId) ?: return@withContext
        val isNowSynced = !file.isCloudSynced
        val updated = file.copy(
            isCloudSynced = isNowSynced,
            syncStatus = if (isNowSynced) "SYNCED" else "CLOUD_ONLY"
        )
        fileDao.updateFile(updated)

        // Log sync action
        syncLogDao.insertLog(
            SyncLogEntity(
                fileId = file.id,
                fileName = file.name,
                syncType = if (isNowSynced) "UPLOAD" else "DELETE_REMOTE",
                status = "SUCCESS",
                details = if (isNowSynced) "Uploaded to Cloud Vault" else "Removed from Cloud Vault"
            )
        )
    }

    suspend fun toggleEncryption(fileId: String, pinKey: String) = withContext(Dispatchers.IO) {
        val file = fileDao.getFileById(fileId) ?: return@withContext
        val nowEncrypted = !file.isEncrypted
        val newCategory = if (nowEncrypted) FileCategory.VAULT.name else FileCategory.fromExtension(file.name.substringAfterLast('.', ""), file.isDirectory, file.isHidden || file.isSystemFile, false).name
        val newPath = if (nowEncrypted) "/storage/emulated/0/Vault/${file.name}.enc" else "/storage/emulated/0/Documents/${file.name.replace(".enc", "")}"
        val newParent = if (nowEncrypted) "/storage/emulated/0/Vault" else "/storage/emulated/0/Documents"

        val updated = file.copy(
            isEncrypted = nowEncrypted,
            category = newCategory,
            path = newPath,
            parentPath = newParent,
            name = if (nowEncrypted && !file.name.endsWith(".enc")) "${file.name}.enc" else file.name.replace(".enc", "")
        )
        fileDao.updateFile(updated)
    }

    suspend fun deleteFile(file: FileEntity) = withContext(Dispatchers.IO) {
        fileDao.deleteFile(file)
    }

    suspend fun deleteFilesByIds(ids: List<String>) = withContext(Dispatchers.IO) {
        fileDao.deleteFilesByIds(ids)
    }

    suspend fun createTag(name: String, colorHex: String, iconName: String = "label") = withContext(Dispatchers.IO) {
        val tag = TagEntity(
            id = "tag_" + UUID.randomUUID().toString().take(6),
            name = name,
            colorHex = colorHex,
            iconName = iconName
        )
        tagDao.insertTag(tag)
    }

    suspend fun deleteTag(tag: TagEntity) = withContext(Dispatchers.IO) {
        tagDao.deleteTag(tag)
    }

    suspend fun setupVaultPin(pin: String) = withContext(Dispatchers.IO) {
        val hash = CryptoEngine.hashPin(pin)
        vaultDao.setVaultConfig(VaultConfig(id = 1, pinHash = hash, isSetupComplete = true))
    }

    suspend fun verifyVaultPin(pin: String): Boolean = withContext(Dispatchers.IO) {
        val config = vaultDao.getVaultConfigSync() ?: return@withContext false
        val hash = CryptoEngine.hashPin(pin)
        config.pinHash == hash
    }

    suspend fun triggerSyncAll() = withContext(Dispatchers.IO) {
        val all = fileDao.getFileById("doc1") ?: return@withContext
        syncLogDao.insertLog(
            SyncLogEntity(
                fileId = "batch_sync",
                fileName = "System_Full_Sync",
                syncType = "AUTO_SYNC",
                status = "SUCCESS",
                details = "Synced all offline queues with cloud storage server."
            )
        )
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        syncLogDao.clearLogs()
    }
}
