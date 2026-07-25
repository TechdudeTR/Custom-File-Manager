package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.FileEntity
import com.example.ui.components.FileListItem
import com.example.ui.viewmodel.AdvancedSearchFilter
import com.example.ui.viewmodel.FileManagerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: FileManagerViewModel,
    onFileDetail: (FileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchFilter by viewModel.searchFilter.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Input Field
        item {
            OutlinedTextField(
                value = searchFilter.query,
                onValueChange = { newQ ->
                    viewModel.updateSearchFilter(searchFilter.copy(query = newQ))
                },
                placeholder = { Text("Search by file name, tag, or extension...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchFilter.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearchFilter(searchFilter.copy(query = "")) }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )
        }

        // Multi-Criteria Filters
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Advanced Search Criteria",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = searchFilter.category == null,
                            onClick = { viewModel.updateSearchFilter(searchFilter.copy(category = null)) },
                            label = { Text("All Categories") }
                        )
                        FileCategory.values().forEach { cat ->
                            FilterChip(
                                selected = searchFilter.category == cat,
                                onClick = {
                                    val target = if (searchFilter.category == cat) null else cat
                                    viewModel.updateSearchFilter(searchFilter.copy(category = target))
                                },
                                label = { Text(cat.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Attribute Toggles
                    Text(
                        text = "Attributes & Security",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = searchFilter.onlyEncrypted,
                            onClick = { viewModel.updateSearchFilter(searchFilter.copy(onlyEncrypted = !searchFilter.onlyEncrypted)) },
                            label = { Text("Encrypted Only") }
                        )
                        FilterChip(
                            selected = searchFilter.onlyOffline,
                            onClick = { viewModel.updateSearchFilter(searchFilter.copy(onlyOffline = !searchFilter.onlyOffline)) },
                            label = { Text("Offline Available") }
                        )
                        FilterChip(
                            selected = searchFilter.onlyCloud,
                            onClick = { viewModel.updateSearchFilter(searchFilter.copy(onlyCloud = !searchFilter.onlyCloud)) },
                            label = { Text("Cloud Synced") }
                        )
                        FilterChip(
                            selected = searchFilter.onlySystem,
                            onClick = { viewModel.updateSearchFilter(searchFilter.copy(onlySystem = !searchFilter.onlySystem)) },
                            label = { Text("System & Hidden") }
                        )
                    }

                    if (allTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Filter by Tag",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allTags.forEach { tag ->
                                FilterChip(
                                    selected = searchFilter.selectedTag == tag.name,
                                    onClick = {
                                        val target = if (searchFilter.selectedTag == tag.name) null else tag.name
                                        viewModel.updateSearchFilter(searchFilter.copy(selectedTag = target))
                                    },
                                    label = { Text("#${tag.name}") }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Results Header
        item {
            Text(
                text = "Search Results (${searchResults.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (searchResults.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No matching files found. Try adjusting your search query or criteria filters.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(searchResults) { file ->
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
