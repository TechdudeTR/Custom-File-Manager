package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VaultConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_config WHERE id = 1 LIMIT 1")
    fun getVaultConfig(): Flow<VaultConfig?>

    @Query("SELECT * FROM vault_config WHERE id = 1 LIMIT 1")
    suspend fun getVaultConfigSync(): VaultConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setVaultConfig(config: VaultConfig)

    @Update
    suspend fun updateVaultConfig(config: VaultConfig)
}
