package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.FileEntity
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.TagAmber
import com.example.ui.theme.TagGreen
import com.example.ui.theme.TagPurple
import com.example.ui.theme.TagRed

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun FileListItem(
    file: FileEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onFileClick: () -> Unit,
    onFileLongClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onDetailClick: () -> Unit,
    onToggleSync: () -> Unit,
    onToggleOffline: () -> Unit,
    onToggleEncrypt: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val iconData = getCategoryIconAndColor(file)

    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelect() else onFileClick()
                },
                onLongClick = onFileLongClick
            )
            .testTag("file_item_${file.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // Category Icon Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconData.second.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = iconData.first,
                    contentDescription = file.name,
                    tint = iconData.second,
                    modifier = Modifier.size(24.dp)
                )

                if (file.isEncrypted) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(TagRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info & Badges
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = file.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (file.isHidden || file.isSystemFile) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TagAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (file.isHidden) ".HIDDEN" else "SYS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TagAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (file.isDirectory) "Folder" else formatFileSize(file.size),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = file.permissions,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tags flow
                if (file.tags.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        file.tags.split(",").filter { it.isNotBlank() }.forEach { tagName ->
                            TagChip(tagName = tagName.trim())
                        }
                    }
                }
            }

            // Sync / Offline Indicators
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (file.isCloudSynced) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Cloud Synced",
                        tint = Cyan500,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp)
                    )
                } else if (file.syncStatus == "PENDING") {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "Pending Cloud Sync",
                        tint = TagAmber,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp)
                    )
                }

                if (file.isOfflineAvailable && !file.isDirectory) {
                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = "Offline Available",
                        tint = TagGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("file_menu_${file.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Details & Checksum") },
                            onClick = {
                                showMenu = false
                                onDetailClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (file.isCloudSynced) "Remove from Cloud" else "Sync to Cloud") },
                            onClick = {
                                showMenu = false
                                onToggleSync()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (file.isOfflineAvailable) "Disable Offline Access" else "Keep Available Offline") },
                            onClick = {
                                showMenu = false
                                onToggleOffline()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (file.isEncrypted) "Decrypt File" else "Encrypt to Vault") },
                            onClick = {
                                showMenu = false
                                onToggleEncrypt()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = TagRed) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryIconAndColor(file: FileEntity): Pair<ImageVector, Color> {
    if (file.isEncrypted) return Pair(Icons.Default.Lock, TagRed)
    if (file.isDirectory) return Pair(Icons.Default.Folder, Indigo600)
    if (file.isSystemFile || file.isHidden) return Pair(Icons.Default.SettingsSuggest, TagAmber)

    return when (file.category) {
        FileCategory.IMAGE.name -> Pair(Icons.Default.Image, Cyan500)
        FileCategory.DOCUMENT.name -> Pair(Icons.Default.Description, Indigo600)
        FileCategory.VIDEO.name -> Pair(Icons.Default.Movie, TagPurple)
        FileCategory.AUDIO.name -> Pair(Icons.Default.Audiotrack, TagGreen)
        FileCategory.ARCHIVE.name -> Pair(Icons.Default.Archive, TagAmber)
        FileCategory.APP.name -> Pair(Icons.Default.Android, TagGreen)
        else -> Pair(Icons.Default.InsertDriveFile, Indigo600)
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
