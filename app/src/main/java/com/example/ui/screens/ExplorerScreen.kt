package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileEntity
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.FileListItem
import com.example.ui.components.SystemFileWarningBanner
import com.example.ui.theme.Cyan500
import com.example.ui.theme.TagAmber
import com.example.ui.theme.TagRed
import com.example.ui.viewmodel.FileManagerViewModel
import com.example.ui.viewmodel.ViewMode

@Composable
fun ExplorerScreen(
    viewModel: FileManagerViewModel,
    onFileDetail: (FileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPath by viewModel.currentPath.collectAsState()
    val explorerFiles by viewModel.explorerFiles.collectAsState()
    val showHiddenAndSystem by viewModel.showHiddenAndSystem.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedFileIds by viewModel.selectedFileIds.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var createMode by remember { mutableStateOf("FOLDER") } // "FOLDER" or "FILE"
    var nameInput by remember { mutableStateOf("") }

    val isSelectionMode = selectedFileIds.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top Controls Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedCategoryFilter != null) {
                        IconButton(
                            onClick = { viewModel.filterByCategory(null) },
                            modifier = Modifier.testTag("clear_category_filter")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Text(
                            text = "Filter: ${selectedCategoryFilter?.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.navigateUp() },
                            enabled = currentPath != "/" && currentPath != "/storage/emulated/0",
                            modifier = Modifier.testTag("navigate_up_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Navigate Up"
                            )
                        }
                        Text(
                            text = "File Explorer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // System & Hidden toggle
                    IconButton(
                        onClick = { viewModel.toggleShowHiddenAndSystem() },
                        modifier = Modifier.testTag("toggle_hidden_system_button")
                    ) {
                        Icon(
                            imageVector = if (showHiddenAndSystem) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Hidden System Files",
                            tint = if (showHiddenAndSystem) TagAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Grid / List toggle
                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.List,
                            contentDescription = "Toggle View Mode"
                        )
                    }
                }
            }

            // Breadcrumb Navigation Bar
            if (selectedCategoryFilter == null) {
                BreadcrumbBar(
                    currentPath = currentPath,
                    onPathClick = { target -> viewModel.navigateToFolder(target) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // System file security warning banner if hidden/system mode is on
            if (showHiddenAndSystem) {
                SystemFileWarningBanner(modifier = Modifier.padding(bottom = 8.dp))
            }

            // Batch action toolbar when files are selected
            if (isSelectionMode) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${selectedFileIds.size} selected",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row {
                            IconButton(onClick = { viewModel.selectAllInView() }) {
                                Icon(
                                    imageVector = Icons.Default.SelectAll,
                                    contentDescription = "Select All"
                                )
                            }
                            IconButton(onClick = { viewModel.deleteSelectedFiles() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = TagRed
                                )
                            }
                            TextButton(onClick = { viewModel.clearSelection() }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }

            // File items list or empty state
            if (explorerFiles.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This directory is empty",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap '+' to create a new folder or file",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else if (viewMode == ViewMode.LIST) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(explorerFiles) { file ->
                        FileListItem(
                            file = file,
                            isSelected = selectedFileIds.contains(file.id),
                            isSelectionMode = isSelectionMode,
                            onFileClick = {
                                if (file.isDirectory) {
                                    viewModel.navigateToFolder(file.path)
                                } else {
                                    onFileDetail(file)
                                }
                            },
                            onFileLongClick = { viewModel.toggleFileSelection(file.id) },
                            onToggleSelect = { viewModel.toggleFileSelection(file.id) },
                            onDetailClick = { onFileDetail(file) },
                            onToggleSync = { viewModel.toggleCloudSync(file.id) },
                            onToggleOffline = { viewModel.toggleOfflineAccess(file.id) },
                            onToggleEncrypt = { viewModel.toggleEncryption(file.id) },
                            onDeleteClick = { viewModel.deleteFile(file) }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(explorerFiles) { file ->
                        FileListItem(
                            file = file,
                            isSelected = selectedFileIds.contains(file.id),
                            isSelectionMode = isSelectionMode,
                            onFileClick = {
                                if (file.isDirectory) {
                                    viewModel.navigateToFolder(file.path)
                                } else {
                                    onFileDetail(file)
                                }
                            },
                            onFileLongClick = { viewModel.toggleFileSelection(file.id) },
                            onToggleSelect = { viewModel.toggleFileSelection(file.id) },
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

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("create_item_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Item",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    // Modal Dialog for Create Folder / File
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Create New Item",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Button(
                            onClick = { createMode = "FOLDER" },
                            shape = RoundedCornerShape(10.dp),
                            colors = if (createMode == "FOLDER") androidx.compose.material3.ButtonDefaults.buttonColors()
                            else androidx.compose.material3.ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Folder")
                        }
                        Button(
                            onClick = { createMode = "FILE" },
                            shape = RoundedCornerShape(10.dp),
                            colors = if (createMode == "FILE") androidx.compose.material3.ButtonDefaults.buttonColors()
                            else androidx.compose.material3.ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("File")
                        }
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(if (createMode == "FOLDER") "Folder Name" else "File Name (e.g., notes.txt)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            if (createMode == "FOLDER") {
                                viewModel.createFolder(nameInput.trim())
                            } else {
                                viewModel.createFile(
                                    name = nameInput.trim(),
                                    sizeBytes = 2048,
                                    mimeType = "text/plain",
                                    tags = listOf("New")
                                )
                            }
                            nameInput = ""
                            showCreateDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
