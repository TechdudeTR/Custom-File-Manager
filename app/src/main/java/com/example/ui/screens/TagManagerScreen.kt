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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileEntity
import com.example.data.model.TagEntity
import com.example.ui.components.FileListItem
import com.example.ui.theme.TagAmber
import com.example.ui.theme.TagBlue
import com.example.ui.theme.TagCyan
import com.example.ui.theme.TagGreen
import com.example.ui.theme.TagIndigo
import com.example.ui.theme.TagOrange
import com.example.ui.theme.TagPink
import com.example.ui.theme.TagPurple
import com.example.ui.theme.TagRed
import com.example.ui.viewmodel.FileManagerViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagManagerScreen(
    viewModel: FileManagerViewModel,
    onFileDetail: (FileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTags by viewModel.allTags.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()

    var newTagName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#3B82F6") }
    var activeTagFilter by remember { mutableStateOf<String?>(null) }

    val colorPalette = listOf(
        "#EF4444", "#F97316", "#F59E0B", "#10B981", "#3B82F6", "#6366F1", "#8B5CF6", "#EC4899", "#06B6D4"
    )

    val filteredFiles = if (activeTagFilter != null) {
        allFiles.filter { it.tags.contains(activeTagFilter ?: "") }
    } else emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Create Tag Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Tag Management System",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("New Tag Label (e.g. Confidential, Client X)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Tag Color Accent",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorPalette.forEach { colorHex ->
                            val parsedColor = Color(android.graphics.Color.parseColor(colorHex))
                            val isSelected = selectedColorHex == colorHex

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                                    .clickable { selectedColorHex = colorHex }
                                    .then(
                                        if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f))
                                        else Modifier
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (newTagName.isNotBlank()) {
                                viewModel.createTag(newTagName.trim(), selectedColorHex)
                                newTagName = ""
                            }
                        },
                        enabled = newTagName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_tag_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Text("Add Custom Tag", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }

        // Available Tags List
        item {
            Text(
                text = "Configured Tags (${allTags.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allTags.forEach { tag ->
                    val tagColor = try {
                        Color(android.graphics.Color.parseColor(tag.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    val isSelectedFilter = activeTagFilter == tag.name

                    Surface(
                        color = if (isSelectedFilter) tagColor else tagColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                activeTagFilter = if (isSelectedFilter) null else tag.name
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelectedFilter) Color.White else tagColor)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = tag.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelectedFilter) Color.White else tagColor
                            )

                            val count = allFiles.count { it.tags.contains(tag.name) }
                            Text(
                                text = " ($count)",
                                fontSize = 11.sp,
                                color = if (isSelectedFilter) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            IconButton(
                                onClick = { viewModel.deleteTag(tag) },
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(start = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete tag",
                                    tint = if (isSelectedFilter) Color.White else TagRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filtered Files by Tag
        if (activeTagFilter != null) {
            item {
                Text(
                    text = "Files tagged with #${activeTagFilter} (${filteredFiles.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(filteredFiles) { file ->
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
