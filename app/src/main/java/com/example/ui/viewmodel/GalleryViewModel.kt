package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AspectRatioMode
import com.example.data.model.MediaAlbum
import com.example.data.model.MediaFilterType
import com.example.data.model.MediaItem
import com.example.data.model.VideoSortType
import com.example.data.repository.MediaRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GalleryTab(val title: String) {
  TIMELINE("Media"),
  ALBUMS("Albums"),
  VIDEOS("Video Player"),
  SETTINGS("Settings & Battery")
}

data class PlayerSettings(
  val gesturesEnabled: Boolean = true,
  val doubleTapSeekSeconds: Int = 10,
  val defaultAspectRatio: AspectRatioMode = AspectRatioMode.FIT,
  val batterySaverMode: Boolean = false,
  val hwAcceleration: Boolean = true,
  val backgroundAudio: Boolean = false,
  val autoResume: Boolean = true
)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

  val repository = MediaRepository(application.applicationContext)
  val cloudSyncManager = com.example.data.cloud.CloudSyncManager(application.applicationContext)

  private val _selectedTab = MutableStateFlow(GalleryTab.TIMELINE)
  val selectedTab: StateFlow<GalleryTab> = _selectedTab.asStateFlow()

  private val _filterType = MutableStateFlow(MediaFilterType.ALL)
  val filterType: StateFlow<MediaFilterType> = _filterType.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedAlbum = MutableStateFlow<String?>(null)
  val selectedAlbum: StateFlow<String?> = _selectedAlbum.asStateFlow()

  private val _videoSortType = MutableStateFlow(VideoSortType.DATE_DESC)
  val videoSortType: StateFlow<VideoSortType> = _videoSortType.asStateFlow()

  private val _activeVideo = MutableStateFlow<MediaItem?>(null)
  val activeVideo: StateFlow<MediaItem?> = _activeVideo.asStateFlow()

  private val _activePhoto = MutableStateFlow<MediaItem?>(null)
  val activePhoto: StateFlow<MediaItem?> = _activePhoto.asStateFlow()

  private val _activeEditorVideo = MutableStateFlow<MediaItem?>(null)
  val activeEditorVideo: StateFlow<MediaItem?> = _activeEditorVideo.asStateFlow()

  private val _activeCollagePhotos = MutableStateFlow<List<MediaItem>?>(null)
  val activeCollagePhotos: StateFlow<List<MediaItem>?> = _activeCollagePhotos.asStateFlow()

  private val _activeMergeVideos = MutableStateFlow<List<MediaItem>?>(null)
  val activeMergeVideos: StateFlow<List<MediaItem>?> = _activeMergeVideos.asStateFlow()

  private val _isCloudSyncOpen = MutableStateFlow(false)
  val isCloudSyncOpen: StateFlow<Boolean> = _isCloudSyncOpen.asStateFlow()

  private val _selectedItemIds = MutableStateFlow<Set<Long>>(emptySet())
  val selectedItemIds: StateFlow<Set<Long>> = _selectedItemIds.asStateFlow()

  private val _gridColumns = MutableStateFlow(3)
  val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

  private val _themeMode = MutableStateFlow(AppThemeMode.ELEGANT_DARK)
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  private val _playerSettings = MutableStateFlow(PlayerSettings())
  val playerSettings: StateFlow<PlayerSettings> = _playerSettings.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  init {
    refreshMedia()
  }

  fun refreshMedia() {
    viewModelScope.launch {
      _isLoading.value = true
      repository.loadMedia()
      _isLoading.value = false
    }
  }

  // Filtered timeline / gallery items
  val filteredMediaItems: StateFlow<List<MediaItem>> = combine(
    repository.mediaItems,
    _filterType,
    _searchQuery,
    _selectedAlbum
  ) { items, filter, query, album ->
    items.filter { item ->
      // Filter by category
      val matchesCategory = when (filter) {
        MediaFilterType.ALL -> true
        MediaFilterType.VIDEOS -> item.isVideo
        MediaFilterType.PHOTOS -> !item.isVideo
        MediaFilterType.FAVORITES -> item.isFavorite
        MediaFilterType.MP4 -> item.formatBadge.equals("MP4", ignoreCase = true)
        MediaFilterType.MKV -> item.formatBadge.equals("MKV", ignoreCase = true)
        MediaFilterType.WEBM -> item.formatBadge.equals("WEBM", ignoreCase = true) || item.formatBadge.equals("AVI", ignoreCase = true)
      }

      // Filter by album if selected
      val matchesAlbum = album == null || item.folderName.equals(album, ignoreCase = true)

      // Filter by query
      val matchesQuery = query.isBlank() ||
        item.displayName.contains(query, ignoreCase = true) ||
        item.title.contains(query, ignoreCase = true) ||
        item.folderName.contains(query, ignoreCase = true)

      matchesCategory && matchesAlbum && matchesQuery
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dedicated Video Hub items sorted
  val videoHubItems: StateFlow<List<MediaItem>> = combine(
    repository.mediaItems,
    _videoSortType,
    _searchQuery
  ) { items, sort, query ->
    val videos = items.filter { it.isVideo && (query.isBlank() || it.displayName.contains(query, ignoreCase = true) || it.title.contains(query, ignoreCase = true)) }
    when (sort) {
      VideoSortType.DATE_DESC -> videos.sortedByDescending { it.dateAdded }
      VideoSortType.DATE_ASC -> videos.sortedBy { it.dateAdded }
      VideoSortType.SIZE_DESC -> videos.sortedByDescending { it.size }
      VideoSortType.DURATION_DESC -> videos.sortedByDescending { it.durationMs }
      VideoSortType.NAME_ASC -> videos.sortedBy { it.displayName.lowercase() }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val albumsList: StateFlow<List<MediaAlbum>> = repository.mediaItems.combine(_searchQuery) { items, query ->
    val albums = repository.getAlbums(items)
    if (query.isBlank()) albums else albums.filter { it.name.contains(query, ignoreCase = true) }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setTab(tab: GalleryTab) {
    _selectedTab.value = tab
    _selectedItemIds.value = emptySet()
  }

  fun setFilter(filter: MediaFilterType) {
    _filterType.value = filter
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSelectedAlbum(albumName: String?) {
    _selectedAlbum.value = albumName
  }

  fun setVideoSort(sort: VideoSortType) {
    _videoSortType.value = sort
  }

  fun openVideo(item: MediaItem) {
    _activeVideo.value = item
  }

  fun closeVideo() {
    _activeVideo.value = null
  }

  fun openPhoto(item: MediaItem) {
    _activePhoto.value = item
  }

  fun closePhoto() {
    _activePhoto.value = null
  }

  fun openVideoEditor(item: MediaItem) {
    _activeEditorVideo.value = item
  }

  fun closeVideoEditor() {
    _activeEditorVideo.value = null
  }

  fun openCollage(photos: List<MediaItem>) {
    _activeCollagePhotos.value = photos
  }

  fun closeCollage() {
    _activeCollagePhotos.value = null
  }

  fun openCollageMaker(photos: List<MediaItem>) {
    openCollage(photos)
  }

  fun closeCollageMaker() {
    closeCollage()
  }

  fun openMerge(videos: List<MediaItem>) {
    _activeMergeVideos.value = videos
  }

  fun closeMerge() {
    _activeMergeVideos.value = null
  }

  fun openVideoMerge(videos: List<MediaItem>) {
    openMerge(videos)
  }

  fun closeVideoMerge() {
    closeMerge()
  }

  fun openCloudSync() {
    _isCloudSyncOpen.value = true
  }

  fun closeCloudSync() {
    _isCloudSyncOpen.value = false
  }

  fun saveEditedMediaItem(item: MediaItem) {
    onCreatedMedia(item)
  }

  fun onCreatedMedia(item: MediaItem) {
    repository.addMediaItem(item)
    // Automatically register for cloud backup if autoSync is active
    if (cloudSyncManager.activeAccount.value.autoSyncEnabled) {
      cloudSyncManager.enqueueMediaItemForSync(item)
    }
  }

  fun toggleFavorite(itemId: Long) {
    repository.toggleFavorite(itemId)
    // Also update active items if open
    if (_activeVideo.value?.id == itemId) {
      _activeVideo.value = _activeVideo.value?.copy(isFavorite = !(_activeVideo.value?.isFavorite ?: false))
    }
    if (_activePhoto.value?.id == itemId) {
      _activePhoto.value = _activePhoto.value?.copy(isFavorite = !(_activePhoto.value?.isFavorite ?: false))
    }
  }

  fun updatePlaybackPosition(itemId: Long, positionMs: Long) {
    repository.updatePlaybackPosition(itemId, positionMs)
  }

  fun setGridColumns(cols: Int) {
    _gridColumns.value = cols.coerceIn(2, 5)
  }

  fun setThemeMode(mode: AppThemeMode) {
    _themeMode.value = mode
  }

  fun updatePlayerSettings(settings: PlayerSettings) {
    _playerSettings.value = settings
  }

  fun toggleItemSelection(id: Long) {
    val current = _selectedItemIds.value.toMutableSet()
    if (current.contains(id)) current.remove(id) else current.add(id)
    _selectedItemIds.value = current
  }

  fun clearSelection() {
    _selectedItemIds.value = emptySet()
  }

  fun selectAll(items: List<MediaItem>) {
    _selectedItemIds.value = items.map { it.id }.toSet()
  }

  fun importUri(uri: Uri, isVideo: Boolean, name: String? = null) {
    repository.importMediaUri(uri, isVideo, name)
  }

  fun shareMediaItem(context: Context, item: MediaItem) {
    try {
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = item.mimeType
        putExtra(Intent.EXTRA_STREAM, item.uri)
        putExtra(Intent.EXTRA_TITLE, item.title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Share ${item.displayName}"))
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
