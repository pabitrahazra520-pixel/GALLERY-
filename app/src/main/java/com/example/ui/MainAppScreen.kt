package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.albums.AlbumsView
import com.example.ui.cloud.CloudSyncView
import com.example.ui.collage.PhotoCollageScreen
import com.example.ui.editor.VideoEditorScreen
import com.example.ui.editor.VideoMergeScreen
import com.example.ui.gallery.TimelineGalleryView
import com.example.ui.player.UniversalVideoPlayer
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.videos.VideoHubView
import com.example.ui.viewmodel.GalleryTab
import com.example.ui.viewmodel.GalleryViewModel
import com.example.ui.viewer.PhotoViewerScreen

@Composable
fun MainAppScreen(
  viewModel: GalleryViewModel = viewModel()
) {
  val context = LocalContext.current
  val themeMode by viewModel.themeMode.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val filterType by viewModel.filterType.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedAlbum by viewModel.selectedAlbum.collectAsState()
  val gridColumns by viewModel.gridColumns.collectAsState()
  val selectedItemIds by viewModel.selectedItemIds.collectAsState()
  val playerSettings by viewModel.playerSettings.collectAsState()

  val filteredMedia by viewModel.filteredMediaItems.collectAsState()
  val videoHubList by viewModel.videoHubItems.collectAsState()
  val albumsList by viewModel.albumsList.collectAsState()
  val videoSortType by viewModel.videoSortType.collectAsState()
  val allMediaItems by viewModel.repository.mediaItems.collectAsState()

  val activeVideo by viewModel.activeVideo.collectAsState()
  val activePhoto by viewModel.activePhoto.collectAsState()
  val activeEditorVideo by viewModel.activeEditorVideo.collectAsState()
  val activeCollagePhotos by viewModel.activeCollagePhotos.collectAsState()
  val activeMergeVideos by viewModel.activeMergeVideos.collectAsState()
  val isCloudSyncOpen by viewModel.isCloudSyncOpen.collectAsState()

  val cloudAccount by viewModel.cloudSyncManager.activeAccount.collectAsState()
  val syncSummary by viewModel.cloudSyncManager.syncSummary.collectAsState()
  val syncRecords by viewModel.cloudSyncManager.allSyncRecords.collectAsState()
  val currentSyncingName by viewModel.cloudSyncManager.currentSyncingItemName.collectAsState()
  val isSyncRunning by viewModel.cloudSyncManager.isSyncRunning.collectAsState()

  // Permission Launcher
  val permissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { _ ->
    viewModel.refreshMedia()
  }

  LaunchedEffect(Unit) {
    val permissionsToRequest = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
      }
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
      }
    } else {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
      }
    }
    if (permissionsToRequest.isNotEmpty()) {
      permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }
  }

  MyApplicationTheme(themeMode = themeMode) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      Scaffold(
        bottomBar = {
          if (activeVideo == null && activePhoto == null) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 0.dp,
              modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .testTag("main_navigation_bar")
            ) {
              val navColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
              )

              // 1. Photos & Videos Media
              NavigationBarItem(
                selected = selectedTab == GalleryTab.TIMELINE,
                onClick = { viewModel.setTab(GalleryTab.TIMELINE) },
                icon = {
                  Icon(
                    imageVector = if (selectedTab == GalleryTab.TIMELINE) Icons.Filled.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                    contentDescription = "Media"
                  )
                },
                label = {
                  Text(
                    text = "Media",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == GalleryTab.TIMELINE) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = navColors,
                modifier = Modifier.testTag("nav_media_tab")
              )

              // 2. Albums & Folders
              NavigationBarItem(
                selected = selectedTab == GalleryTab.ALBUMS,
                onClick = { viewModel.setTab(GalleryTab.ALBUMS) },
                icon = {
                  Icon(
                    imageVector = if (selectedTab == GalleryTab.ALBUMS) Icons.Filled.Folder else Icons.Filled.FolderOpen,
                    contentDescription = "Albums"
                  )
                },
                label = {
                  Text(
                    text = "Albums",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == GalleryTab.ALBUMS) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = navColors,
                modifier = Modifier.testTag("nav_albums_tab")
              )

              // 3. Video Hub Player
              NavigationBarItem(
                selected = selectedTab == GalleryTab.VIDEOS,
                onClick = { viewModel.setTab(GalleryTab.VIDEOS) },
                icon = {
                  Icon(
                    imageVector = if (selectedTab == GalleryTab.VIDEOS) Icons.Filled.PlayCircle else Icons.Filled.PlayCircleOutline,
                    contentDescription = "Video Hub"
                  )
                },
                label = {
                  Text(
                    text = "Video Hub",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == GalleryTab.VIDEOS) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = navColors,
                modifier = Modifier.testTag("nav_videohub_tab")
              )

              // 4. Settings & Battery
              NavigationBarItem(
                selected = selectedTab == GalleryTab.SETTINGS,
                onClick = { viewModel.setTab(GalleryTab.SETTINGS) },
                icon = {
                  Icon(
                    imageVector = if (selectedTab == GalleryTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                  )
                },
                label = {
                  Text(
                    text = "Settings",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == GalleryTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = navColors,
                modifier = Modifier.testTag("nav_settings_tab")
              )
            }
          }
        }
      ) { innerPadding ->
        AnimatedContent(
          targetState = selectedTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "TabTransition",
          modifier = Modifier.padding(innerPadding)
        ) { targetTab ->
          when (targetTab) {
            GalleryTab.TIMELINE -> TimelineGalleryView(
              mediaItems = filteredMedia,
              selectedFilter = filterType,
              searchQuery = searchQuery,
              selectedAlbum = selectedAlbum,
              gridColumns = gridColumns,
              selectedItemIds = selectedItemIds,
              onFilterChange = viewModel::setFilter,
              onSearchChange = viewModel::setSearchQuery,
              onClearAlbum = { viewModel.setSelectedAlbum(null) },
              onGridColumnsChange = viewModel::setGridColumns,
              onItemClick = { item ->
                if (item.isVideo) {
                  viewModel.openVideo(item)
                } else {
                  viewModel.openPhoto(item)
                }
              },
              onItemLongClick = { item -> viewModel.toggleItemSelection(item.id) },
              onToggleFavorite = viewModel::toggleFavorite,
              onImportUri = viewModel::importUri,
              onShareItem = { item -> viewModel.shareMediaItem(context, item) },
              onOpenCloudSync = { viewModel.openCloudSync() },
              onOpenCollage = { photos -> viewModel.openCollage(photos) },
              onOpenMerge = { videos -> viewModel.openMerge(videos) },
              onOpenEditor = { video -> viewModel.openVideoEditor(video) },
              onClearSelection = { viewModel.clearSelection() }
            )

            GalleryTab.ALBUMS -> AlbumsView(
              albums = albumsList,
              onAlbumClick = { albumName ->
                viewModel.setSelectedAlbum(albumName)
                viewModel.setTab(GalleryTab.TIMELINE)
              }
            )

            GalleryTab.VIDEOS -> VideoHubView(
              videos = videoHubList,
              currentSort = videoSortType,
              onSortChange = viewModel::setVideoSort,
              onPlayVideo = viewModel::openVideo,
              onToggleFavorite = viewModel::toggleFavorite,
              onEditVideo = { video -> viewModel.openVideoEditor(video) }
            )

            GalleryTab.SETTINGS -> SettingsScreen(
              currentTheme = themeMode,
              playerSettings = playerSettings,
              mediaItems = allMediaItems,
              onThemeChange = viewModel::setThemeMode,
              onPlayerSettingsChange = viewModel::updatePlayerSettings,
              onRefreshMedia = viewModel::refreshMedia,
              onOpenCloudSync = { viewModel.openCloudSync() }
            )
          }
        }
      }

      // Immersive Video Player Overlay
      activeVideo?.let { videoItem ->
        UniversalVideoPlayer(
          videoItem = videoItem,
          onClose = viewModel::closeVideo,
          onToggleFavorite = viewModel::toggleFavorite,
          onPlaybackProgress = viewModel::updatePlaybackPosition,
          onEditVideo = { item -> viewModel.openVideoEditor(item) }
        )
      }

      // Immersive Photo Viewer Overlay
      activePhoto?.let { photoItem ->
        PhotoViewerScreen(
          photoItem = photoItem,
          onClose = viewModel::closePhoto,
          onToggleFavorite = viewModel::toggleFavorite,
          onShare = { item -> viewModel.shareMediaItem(context, item) }
        )
      }

      // Cloud Sync Dashboard Overlay
      if (isCloudSyncOpen) {
        CloudSyncView(
          account = cloudAccount,
          syncSummary = syncSummary,
          syncRecords = syncRecords,
          mediaItems = allMediaItems,
          currentSyncingName = currentSyncingName,
          isSyncRunning = isSyncRunning,
          onClose = { viewModel.closeCloudSync() },
          onSyncAll = {
            viewModel.cloudSyncManager.syncAllMedia(allMediaItems)
          },
          onUpdateAccountSettings = { provider, email, autoSync, wifiOnly, chargingOnly, minBattery ->
            viewModel.cloudSyncManager.updateAccountSettings(provider, email, autoSync, wifiOnly, chargingOnly, minBattery)
          },
          onToggleOfflinePin = { mediaId ->
            viewModel.cloudSyncManager.toggleOfflinePin(mediaId)
          }
        )
      }

      // Advanced Video Editor Studio Overlay
      activeEditorVideo?.let { editorItem ->
        VideoEditorScreen(
          sourceVideo = editorItem,
          onClose = { viewModel.closeVideoEditor() },
          onExportSuccess = { editedItem ->
            viewModel.saveEditedMediaItem(editedItem)
          }
        )
      }

      // Photo Collage Maker Overlay
      activeCollagePhotos?.let { collagePhotos ->
        PhotoCollageScreen(
          selectedPhotos = collagePhotos,
          onClose = { viewModel.closeCollage() },
          onCollageCreated = { collageItem ->
            viewModel.saveEditedMediaItem(collageItem)
          }
        )
      }

      // Video Merge Studio Overlay
      activeMergeVideos?.let { mergeVideos ->
        VideoMergeScreen(
          selectedVideos = mergeVideos,
          onClose = { viewModel.closeMerge() },
          onMergeSuccess = { mergedItem ->
            viewModel.saveEditedMediaItem(mergedItem)
          }
        )
      }
    }
  }
}
