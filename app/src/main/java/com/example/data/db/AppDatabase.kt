package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FileEntity
import com.example.data.model.SyncLogEntity
import com.example.data.model.TagEntity
import com.example.data.model.VaultConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FileEntity::class, TagEntity::class, SyncLogEntity::class, VaultConfig::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fileDao(): FileDao
    abstract fun tagDao(): TagDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "file_manager_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaultData(database)
                    }
                }
            }
        }

        private suspend fun seedDefaultData(db: AppDatabase) {
            // Seed Default Tags
            val tags = listOf(
                TagEntity("t1", "Work", "#3B82F6", "work"),
                TagEntity("t2", "Financial", "#10B981", "payments"),
                TagEntity("t3", "Personal", "#EC4899", "person"),
                TagEntity("t4", "Confidential", "#EF4444", "security"),
                TagEntity("t5", "Important", "#F59E0B", "star"),
                TagEntity("t6", "Project A", "#8B5CF6", "folder")
            )
            db.tagDao().insertTags(tags)

            // Seed Sample Folders and Files
            val now = System.currentTimeMillis()
            val files = listOf(
                // Root Folders
                FileEntity("f_docs", "Documents", "/storage/emulated/0/Documents", "/storage/emulated/0", 0, "resource/folder", "FOLDER", isDirectory = true, isCloudSynced = true, isOfflineAvailable = true),
                FileEntity("f_photos", "Photos", "/storage/emulated/0/Photos", "/storage/emulated/0", 0, "resource/folder", "FOLDER", isDirectory = true, isCloudSynced = true, isOfflineAvailable = true),
                FileEntity("f_system", "System Partition", "/system", "/", 0, "resource/folder", "SYSTEM", isDirectory = true, isHidden = false, isSystemFile = true, permissions = "drwxr-xr-x"),
                FileEntity("f_vault", "Encrypted Vault", "/storage/emulated/0/Vault", "/storage/emulated/0", 0, "resource/folder", "VAULT", isDirectory = true, isEncrypted = true, permissions = "drwx------"),

                // Documents inside /storage/emulated/0/Documents
                FileEntity("doc1", "Q3_Financial_Audit.pdf", "/storage/emulated/0/Documents/Q3_Financial_Audit.pdf", "/storage/emulated/0/Documents", 2457600, "application/pdf", "DOCUMENT", isCloudSynced = true, isOfflineAvailable = true, tags = "Financial,Confidential,Work", checksum = "A9F1B2C3"),
                FileEntity("doc2", "Project_Proposal_v2.docx", "/storage/emulated/0/Documents/Project_Proposal_v2.docx", "/storage/emulated/0/Documents", 1280000, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "DOCUMENT", isCloudSynced = true, isOfflineAvailable = true, tags = "Work,Project A", checksum = "77B901C1"),
                FileEntity("doc3", "Tax_Return_2025.pdf", "/storage/emulated/0/Documents/Tax_Return_2025.pdf", "/storage/emulated/0/Documents", 3890122, "application/pdf", "DOCUMENT", isCloudSynced = false, isOfflineAvailable = true, syncStatus = "PENDING", tags = "Financial,Personal", checksum = "44C091A8"),

                // Photos inside /storage/emulated/0/Photos
                FileEntity("img1", "Team_Offsite_Group.jpg", "/storage/emulated/0/Photos/Team_Offsite_Group.jpg", "/storage/emulated/0/Photos", 4500000, "image/jpeg", "IMAGE", isCloudSynced = true, isOfflineAvailable = true, tags = "Personal,Work", checksum = "9901CBAF"),
                FileEntity("img2", "Product_Design_Mockup.png", "/storage/emulated/0/Photos/Product_Design_Mockup.png", "/storage/emulated/0/Photos", 3200000, "image/png", "IMAGE", isCloudSynced = true, isOfflineAvailable = false, syncStatus = "CLOUD_ONLY", tags = "Work,Project A", checksum = "889A312F"),

                // Hidden & System Files
                FileEntity("sys1", "build.prop", "/system/build.prop", "/system", 18450, "text/plain", "SYSTEM", isHidden = false, isSystemFile = true, permissions = "-rw-r--r--", checksum = "FC0912AA"),
                FileEntity("sys2", ".nomedia", "/storage/emulated/0/.nomedia", "/storage/emulated/0", 0, "application/x-nomedia", "SYSTEM", isHidden = true, isSystemFile = true, permissions = "-rw-r--r--"),
                FileEntity("sys3", ".sys_cache_index", "/storage/emulated/0/.sys_cache_index", "/storage/emulated/0", 120400, "application/octet-stream", "SYSTEM", isHidden = true, isSystemFile = true, permissions = "-rw-rw----"),
                FileEntity("sys4", "packages.xml", "/data/system/packages.xml", "/system", 584000, "text/xml", "SYSTEM", isHidden = false, isSystemFile = true, permissions = "-rw-r--r--"),

                // Vault encrypted items
                FileEntity("v1", "Private_Keys_Backup.enc", "/storage/emulated/0/Vault/Private_Keys_Backup.enc", "/storage/emulated/0/Vault", 10240, "application/octet-stream", "VAULT", isEncrypted = true, tags = "Confidential", checksum = "E2F1C09A")
            )
            db.fileDao().insertFiles(files)

            // Seed Initial Cloud Sync Log
            val logs = listOf(
                SyncLogEntity(fileId = "doc1", fileName = "Q3_Financial_Audit.pdf", syncType = "UPLOAD", status = "SUCCESS", details = "Uploaded to Google Cloud Vault"),
                SyncLogEntity(fileId = "img1", fileName = "Team_Offsite_Group.jpg", syncType = "AUTO_SYNC", status = "SUCCESS", details = "Synced 4.5 MB photo")
            )
            logs.forEach { db.syncLogDao().insertLog(it) }

            // Default Vault Config
            db.vaultDao().setVaultConfig(VaultConfig())
        }
    }
}
