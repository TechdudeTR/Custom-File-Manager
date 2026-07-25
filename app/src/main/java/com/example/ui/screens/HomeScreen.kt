package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.FileEntity
import com.example.ui.components.FileListItem
import com.example.ui.components.StorageUsageCard
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.TagAmber
import com.example.ui.theme.TagGreen
import com.example.ui.theme.TagPurple
import com.example.ui.theme.TagRed
import com.example.ui.viewmodel.FileManagerViewModel
import com.example.ui.viewmodel.NavigationTab

@Composable
fun HomeScreen(
    viewModel: FileManagerViewModel,
    onFileDetail: (FileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val storageStats by viewModel.storageStats.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()
    val isSyncingNow by viewModel.isSyncingNow.collectAsState()
    val isAutoSyncEnabled by viewModel.isAutoSyncEnabled.collectAsState()

    val recentFiles = allFiles.take(5)
    val offlineFiles = allFiles.filter { it.isOfflineAvailable && !it.isDirectory }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Storage Usage Overview
        item {
            StorageUsageCard(stats = storageStats)
        }

        // Cloud Synchronization Quick Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
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
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Cyan500)
                        ) {
                            Icon(
                                imageVector = if (isSyncingNow) Icons.Default.Sync else Icons.Default.CloudDone,
                                contentDescription = "Cloud Status",
                                tint = Color.White
                            )
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = if (isSyncingNow) "Cloud Syncing in Progress..." else "Cloud Sync Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAutoSyncEnabled) "Auto-sync enabled • Encrypted Tunnel" else "Manual Sync Mode",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.triggerCloudSyncNow() },
                        enabled = !isSyncingNow,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("home_sync_now_button")
                    ) {
                        Text(text = if (isSyncingNow) "Syncing..." else "Sync Now")
                    }
                }
            }
        }

        // Quick Categories Grid
        item {
            Text(
                text = "FILE CATEGORIES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val categories = listOf(
                Pair(FileCategory.IMAGE, Pair(Icons.Default.Image, Cyan500)),
                Pair(FileCategory.DOCUMENT, Pair(Icons.Default.Description, Indigo600)),
                Pair(FileCategory.VIDEO, Pair(Icons.Default.Movie, TagPurple)),
                Pair(FileCategory.AUDIO, Pair(Icons.Default.Audiotrack, TagGreen)),
                Pair(FileCategory.ARCHIVE, Pair(Icons.Default.Archive, TagAmber)),
                Pair(FileCategory.APP, Pair(Icons.Default.Android, TagGreen)),
                Pair(FileCategory.SYSTEM, Pair(Icons.Default.SettingsSuggest, TagAmber)),
                Pair(FileCategory.VAULT, Pair(Icons.Default.Lock, TagRed))
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { pair ->
                    CategoryCard(
                        category = pair.first,
                        icon = pair.second.first,
                        accentColor = pair.second.second,
                        count = allFiles.count { it.category == pair.first.name },
                        onClick = {
                            if (pair.first == FileCategory.VAULT) {
                                viewModel.navigateToTab(NavigationTab.VAULT)
                            } else {
                                viewModel.filterByCategory(pair.first)
                            }
                        }
                    )
                }
            }
        }

        // Offline Quick Vault Access
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = "Offline Files",
                        tint = TagGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "OFFLINE CACHED FILES (${offlineFiles.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        if (offlineFiles.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No files cached for offline access yet.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(offlineFiles.take(3)) { file ->
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

        // Recent Files Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "RECENT ITEMS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
            }
        }

        items(recentFiles) { file ->
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

@Composable
private fun CategoryCard(
    category: FileCategory,
    icon: ImageVector,
    accentColor: Color,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("category_card_${category.name}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.displayName,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = category.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = "$count items",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
