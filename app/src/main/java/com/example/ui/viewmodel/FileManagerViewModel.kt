package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileCategory
import com.example.data.model.FileEntity
import com.example.data.model.SyncLogEntity
import com.example.data.model.TagEntity
import com.example.data.model.VaultConfig
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ViewMode { LIST, GRID }
enum class NavigationTab { HOME, EXPLORER, CLOUD, VAULT, TAGS, SEARCH }

data class StorageStats(
    val totalBytes: Long = 64L * 1024 * 1024 * 1024, // 64 GB
    val usedBytes: Long = 0L,
    val photosBytes: Long = 0L,
    val docsBytes: Long = 0L,
    val systemBytes: Long = 0L,
    val encryptedBytes: Long = 0L,
    val otherBytes: Long = 0L
)

data class AdvancedSearchFilter(
    val query: String = "",
    val category: FileCategory? = null,
    val selectedTag: String? = null,
    val minSizeMb: Long? = null,
    val maxSizeMb: Long? = null,
    val onlyEncrypted: Boolean = false,
    val onlyOffline: Boolean = false,
    val onlyCloud: Boolean = false,
    val onlySystem: Boolean = false
)

class FileManagerViewModel(
    private val repository: FileRepository
) : ViewModel() {

    private val _currentPath = MutableStateFlow("/storage/emulated/0")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _showHiddenAndSystem = MutableStateFlow(false)
    val showHiddenAndSystem: StateFlow<Boolean> = _showHiddenAndSystem.asStateFlow()

    private val _activeTab = MutableStateFlow(NavigationTab.HOME)
    val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _selectedFileIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedFileIds: StateFlow<Set<String>> = _selectedFileIds.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<FileCategory?>(null)
    val selectedCategoryFilter: StateFlow<FileCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _vaultErrorMessage = MutableStateFlow<String?>(null)
    val vaultErrorMessage: StateFlow<String?> = _vaultErrorMessage.asStateFlow()

    private val _isAutoSyncEnabled = MutableStateFlow(true)
    val isAutoSyncEnabled: StateFlow<Boolean> = _isAutoSyncEnabled.asStateFlow()

    private val _isSyncingNow = MutableStateFlow(false)
    val isSyncingNow: StateFlow<Boolean> = _isSyncingNow.asStateFlow()

    private val _isOfflineSimulated = MutableStateFlow(false)
    val isOfflineSimulated: StateFlow<Boolean> = _isOfflineSimulated.asStateFlow()

    private val _searchFilter = MutableStateFlow(AdvancedSearchFilter())
    val searchFilter: StateFlow<AdvancedSearchFilter> = _searchFilter.asStateFlow()

    val allFiles: StateFlow<List<FileEntity>> = repository.allFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<List<TagEntity>> = repository.allTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncLogs: StateFlow<List<SyncLogEntity>> = repository.syncLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultConfig: StateFlow<VaultConfig?> = repository.vaultConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current files in folder/explorer view
    val explorerFiles: StateFlow<List<FileEntity>> = combine(
        repository.allFiles,
        _currentPath,
        _showHiddenAndSystem,
        _selectedCategoryFilter
    ) { files, path, showHidden, category ->
        files.filter { file ->
            val matchPath = if (category != null) {
                file.category == category.name
            } else {
                file.parentPath == path
            }
            val matchHidden = showHidden || (!file.isHidden && !file.isSystemFile)
            matchPath && matchHidden
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Advanced search results
    val searchResults: StateFlow<List<FileEntity>> = combine(
        repository.allFiles,
        _searchFilter,
        _showHiddenAndSystem
    ) { files, filter, showHidden ->
        files.filter { file ->
            val matchQuery = filter.query.isBlank() ||
                    file.name.contains(filter.query, ignoreCase = true) ||
                    file.tags.contains(filter.query, ignoreCase = true) ||
                    file.path.contains(filter.query, ignoreCase = true)

            val matchCategory = filter.category == null || file.category == filter.category.name
            val matchTag = filter.selectedTag == null || file.tags.contains(filter.selectedTag)
            val matchMinSize = filter.minSizeMb == null || (file.size >= filter.minSizeMb * 1024 * 1024)
            val matchMaxSize = filter.maxSizeMb == null || (file.size <= filter.maxSizeMb * 1024 * 1024)
            val matchEncrypted = !filter.onlyEncrypted || file.isEncrypted
            val matchOffline = !filter.onlyOffline || file.isOfflineAvailable
            val matchCloud = !filter.onlyCloud || file.isCloudSynced
            val matchSystem = !filter.onlySystem || (file.isHidden || file.isSystemFile)
            val matchHiddenState = showHidden || (!file.isHidden && !file.isSystemFile) || filter.onlySystem

            matchQuery && matchCategory && matchTag && matchMinSize && matchMaxSize &&
                    matchEncrypted && matchOffline && matchCloud && matchSystem && matchHiddenState
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Storage Statistics
    val storageStats: StateFlow<StorageStats> = repository.allFiles.map { files ->
        var photos = 0L
        var docs = 0L
        var system = 0L
        var encrypted = 0L
        var other = 0L

        files.forEach { f ->
            when (f.category) {
                FileCategory.IMAGE.name, FileCategory.VIDEO.name -> photos += f.size
                FileCategory.DOCUMENT.name, FileCategory.AUDIO.name, FileCategory.ARCHIVE.name -> docs += f.size
                FileCategory.SYSTEM.name -> system += f.size
                FileCategory.VAULT.name -> encrypted += f.size
                else -> other += f.size
            }
        }
        val totalUsed = photos + docs + system + encrypted + other
        StorageStats(
            usedBytes = totalUsed,
            photosBytes = photos,
            docsBytes = docs,
            systemBytes = system,
            encryptedBytes = encrypted,
            otherBytes = other
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageStats())

    fun navigateToTab(tab: NavigationTab) {
        _activeTab.value = tab
    }

    fun navigateToFolder(path: String) {
        _selectedCategoryFilter.value = null
        _currentPath.value = path
    }

    fun navigateUp(): Boolean {
        if (_selectedCategoryFilter.value != null) {
            _selectedCategoryFilter.value = null
            return true
        }
        val current = _currentPath.value
        if (current == "/" || current == "/storage/emulated/0") {
            return false
        }
        val parent = current.substringBeforeLast('/', "/storage/emulated/0")
        _currentPath.value = if (parent.isEmpty()) "/" else parent
        return true
    }

    fun filterByCategory(category: FileCategory?) {
        _selectedCategoryFilter.value = category
        _activeTab.value = NavigationTab.EXPLORER
    }

    fun toggleShowHiddenAndSystem() {
        _showHiddenAndSystem.value = !_showHiddenAndSystem.value
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
    }

    fun toggleFileSelection(fileId: String) {
        val current = _selectedFileIds.value.toMutableSet()
        if (current.contains(fileId)) {
            current.remove(fileId)
        } else {
            current.add(fileId)
        }
        _selectedFileIds.value = current
    }

    fun clearSelection() {
        _selectedFileIds.value = emptySet()
    }

    fun selectAllInView() {
        _selectedFileIds.value = explorerFiles.value.map { it.id }.toSet()
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name, _currentPath.value)
        }
    }

    fun createFile(name: String, sizeBytes: Long, mimeType: String, tags: List<String>) {
        viewModelScope.launch {
            repository.createFile(name, _currentPath.value, sizeBytes, mimeType, tags)
        }
    }

    fun updateFileTags(fileId: String, tags: List<String>) {
        viewModelScope.launch {
            repository.updateFileTags(fileId, tags)
        }
    }

    fun toggleOfflineAccess(fileId: String) {
        viewModelScope.launch {
            repository.toggleOfflineAccess(fileId)
        }
    }

    fun toggleCloudSync(fileId: String) {
        viewModelScope.launch {
            repository.toggleCloudSync(fileId)
        }
    }

    fun toggleEncryption(fileId: String, pinKey: String = "1234") {
        viewModelScope.launch {
            repository.toggleEncryption(fileId, pinKey)
        }
    }

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
            _selectedFileIds.value = _selectedFileIds.value - file.id
        }
    }

    fun deleteSelectedFiles() {
        viewModelScope.launch {
            repository.deleteFilesByIds(_selectedFileIds.value.toList())
            _selectedFileIds.value = emptySet()
        }
    }

    fun createTag(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.createTag(name, colorHex)
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch {
            repository.deleteTag(tag)
        }
    }

    fun updateSearchFilter(filter: AdvancedSearchFilter) {
        _searchFilter.value = filter
    }

    fun setupVaultPin(pin: String) {
        viewModelScope.launch {
            repository.setupVaultPin(pin)
            _isVaultUnlocked.value = true
        }
    }

    fun unlockVault(pin: String) {
        viewModelScope.launch {
            val isCorrect = repository.verifyVaultPin(pin)
            if (isCorrect) {
                _isVaultUnlocked.value = true
                _vaultErrorMessage.value = null
            } else {
                _vaultErrorMessage.value = "Incorrect PIN. Please try again."
            }
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun toggleAutoSync() {
        _isAutoSyncEnabled.value = !_isAutoSyncEnabled.value
    }

    fun triggerCloudSyncNow() {
        viewModelScope.launch {
            _isSyncingNow.value = true
            repository.triggerSyncAll()
            kotlinx.coroutines.delay(1200)
            _isSyncingNow.value = false
        }
    }

    fun toggleSimulatedOfflineMode() {
        _isOfflineSimulated.value = !_isOfflineSimulated.value
    }

    fun clearSyncLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
