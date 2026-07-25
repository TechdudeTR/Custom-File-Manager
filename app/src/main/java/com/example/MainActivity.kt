package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AppDatabase
import com.example.data.model.FileEntity
import com.example.data.repository.FileRepository
import com.example.ui.components.FileDetailDialog
import com.example.ui.screens.CloudSyncScreen
import com.example.ui.screens.ExplorerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.TagManagerScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.FileManagerTheme
import com.example.ui.viewmodel.FileManagerViewModel
import com.example.ui.viewmodel.FileManagerViewModelFactory
import com.example.ui.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        val repository = FileRepository(
            fileDao = db.fileDao(),
            tagDao = db.tagDao(),
            syncLogDao = db.syncLogDao(),
            vaultDao = db.vaultDao()
        )
        val factory = FileManagerViewModelFactory(repository)

        setContent {
            FileManagerTheme {
                val viewModel: FileManagerViewModel = viewModel(factory = factory)
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: FileManagerViewModel
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    var inspectingFile by remember { mutableStateOf<FileEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (activeTab) {
                            NavigationTab.HOME -> "File Manager"
                            NavigationTab.EXPLORER -> "Storage Explorer"
                            NavigationTab.CLOUD -> "Cloud Sync Engine"
                            NavigationTab.VAULT -> "Encrypted Vault"
                            NavigationTab.TAGS -> "Tag Manager"
                            NavigationTab.SEARCH -> "Advanced Search"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationItemData.values().forEach { item ->
                    val isSelected = activeTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateToTab(item.tab) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("nav_${item.tab.name.lowercase()}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                NavigationTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onFileDetail = { file -> inspectingFile = file }
                )
                NavigationTab.EXPLORER -> ExplorerScreen(
                    viewModel = viewModel,
                    onFileDetail = { file -> inspectingFile = file }
                )
                NavigationTab.CLOUD -> CloudSyncScreen(
                    viewModel = viewModel
                )
                NavigationTab.VAULT -> VaultScreen(
                    viewModel = viewModel,
                    onFileDetail = { file -> inspectingFile = file }
                )
                NavigationTab.TAGS -> TagManagerScreen(
                    viewModel = viewModel,
                    onFileDetail = { file -> inspectingFile = file }
                )
                NavigationTab.SEARCH -> SearchScreen(
                    viewModel = viewModel,
                    onFileDetail = { file -> inspectingFile = file }
                )
            }
        }
    }

    // File Inspector & Metadata Modal Dialog
    inspectingFile?.let { file ->
        FileDetailDialog(
            file = file,
            availableTags = allTags,
            onSaveTags = { newTags ->
                viewModel.updateFileTags(file.id, newTags)
            },
            onDismiss = { inspectingFile = null }
        )
    }
}

private enum class NavigationItemData(
    val tab: NavigationTab,
    val label: String,
    val icon: ImageVector
) {
    HOME(NavigationTab.HOME, "Home", Icons.Default.Home),
    EXPLORER(NavigationTab.EXPLORER, "Explorer", Icons.Default.Folder),
    CLOUD(NavigationTab.CLOUD, "Cloud", Icons.Default.CloudSync),
    VAULT(NavigationTab.VAULT, "Vault", Icons.Default.Lock),
    TAGS(NavigationTab.TAGS, "Tags", Icons.Default.Label),
    SEARCH(NavigationTab.SEARCH, "Search", Icons.Default.Search)
}
