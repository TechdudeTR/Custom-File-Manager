package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileEntity
import com.example.ui.components.FileListItem
import com.example.ui.theme.Cyan500
import com.example.ui.theme.TagRed
import com.example.ui.viewmodel.FileManagerViewModel

@Composable
fun VaultScreen(
    viewModel: FileManagerViewModel,
    onFileDetail: (FileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val vaultConfig by viewModel.vaultConfig.collectAsState()
    val vaultErrorMessage by viewModel.vaultErrorMessage.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()

    val encryptedFiles = allFiles.filter { it.isEncrypted }

    var pinInput by remember { mutableStateOf("") }

    if (!isVaultUnlocked) {
        // Locked Vault Keypad View
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(TagRed.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted Vault",
                            tint = TagRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (vaultConfig?.isSetupComplete == true) "AES-256 Encrypted Vault" else "Setup Security PIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (vaultConfig?.isSetupComplete == true) "Enter your PIN to access protected files" else "Create a 4-digit security PIN for file encryption",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) pinInput = it },
                        label = { Text("Security PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = vaultErrorMessage != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_pin_input")
                    )

                    if (vaultErrorMessage != null) {
                        Text(
                            text = vaultErrorMessage ?: "",
                            color = TagRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (vaultConfig?.isSetupComplete == true) {
                                viewModel.unlockVault(pinInput)
                            } else if (pinInput.length >= 4) {
                                viewModel.setupVaultPin(pinInput)
                            }
                        },
                        enabled = pinInput.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = TagRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unlock_vault_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(if (vaultConfig?.isSetupComplete == true) "Unlock Vault" else "Save & Set PIN")
                    }
                }
            }
        }
    } else {
        // Unlocked Vault View
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Unlocked Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = TagRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = TagRed,
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = "Vault Unlocked (AES-256 Active)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${encryptedFiles.size} encrypted files stored",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.lockVault() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("lock_vault_button")
                        ) {
                            Text("Lock Vault", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Encrypted Vault Files",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (encryptedFiles.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Vault is empty. You can encrypt any file from Explorer by tapping 'Encrypt to Vault'.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(encryptedFiles) { file ->
                    FileListItem(
                        file = file,
                        isSelected = false,
                        isSelectionMode = false,
                        onFileClick = { onFileDetail(file) },
                        onFileLongClick = {},
                        onToggleSelect = {},
                        onDetailClick = { onFileDetail(file) },
                        onToggleSync = { viewModel.toggleCloudSync(file.id) },
                        onToggleOffline = { viewModel.toggleOfflineAccess(file.id) },
                        onToggleEncrypt = { viewModel.toggleEncryption(file.id) },
                        onDeleteClick = { viewModel.deleteFile(file) }
                    )
                }
            }
        }
    }
}
