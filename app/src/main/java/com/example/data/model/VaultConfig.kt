package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_config")
data class VaultConfig(
    @PrimaryKey val id: Int = 1,
    val pinHash: String = "",
    val isBiometricEnabled: Boolean = false,
    val vaultSalt: String = "SALT_123456",
    val isSetupComplete: Boolean = false
)
