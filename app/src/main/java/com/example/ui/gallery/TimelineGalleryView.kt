package com.example.ui.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.MediaFilterType
import com.example.data.model.MediaItem
import com.example.ui.theme.ElegantAccentBlue
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantDarkCard
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantPrimary
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.ui.theme.GoldFavorite

@Composable
fun TimelineGalleryView(
  mediaItems: List<MediaItem>,
  selectedFilter: MediaFilterType,
  searchQuery: String,
  selectedAlbum: String?,
  gridColumns: Int,
  selectedItemIds: Set<Long>,
  onFilterChange: (MediaFilterType) -> Unit,
  onSearchChange: (String) -> Unit,
  onClearAlbum: () -> Unit,
  onGridColumnsChange: (Int) -> Unit,
  onItemClick: (MediaItem) -> Unit,
  onItemLongClick: (MediaItem) -> Unit,
  onToggleFavorite: (Long) -> Unit,
  onImportUri: (Uri, Boolean, String?) -> Unit,
  onShareItem: (MediaItem) -> Unit,
  onOpenCloudSync: (() -> Unit)? = null,
  onOpenCollage: ((List<MediaItem>) -> Unit)? = null,
  onOpenMerge: ((List<MediaItem>) -> Unit)? = null,
  onOpenEditor: ((MediaItem) -> Unit)? = null,
  onClearSelection: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isSearchVisible by remember { mutableStateOf(false) }

  // Media Picker Launcher for external files
  val mediaPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      onImportUri(uri, true, "Imported_Media_${System.currentTimeMillis() % 10000}")
    }
  }

  // Generic document picker for universal video files (.mkv, .avi, .flv, .mov, etc.)
  val documentPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    if (uri != null) {
      val isVid = uri.toString().contains("video", ignoreCase = true) || !uri.toString().contains("image", ignoreCase = true)
      onImportUri(uri, isVid, "Imported_File_${System.currentTimeMillis() % 10000}")
    }
  }

  Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Header with Album drill-down title or Search bar
      if (selectedAlbum != null) {
        Surface(
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onClearAlbum,
              modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .testTag("back_to_all_albums")
            ) {
              Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back to Albums",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = selectedAlbum,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "${mediaItems.size} items",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // 2. Search & Controls Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        if (isSearchVisible) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
              Text(
                "Search media...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 14.sp
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            },
            trailingIcon = {
              IconButton(onClick = {
                onSearchChange("")
                isSearchVisible = false
              }) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close Search",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("gallery_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = MaterialTheme.colorScheme.onSurface,
              unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
          )
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Gallery",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Normal,
              letterSpacing = (-0.5).sp,
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = "${mediaItems.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Search Button (Circular #333538)
            IconButton(
              onClick = { isSearchVisible = true },
              modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .testTag("open_search_button")
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
              )
            }

            // Grid density toggle
            IconButton(
              onClick = {
                val nextCols = if (gridColumns >= 4) 2 else gridColumns + 1
                onGridColumnsChange(nextCols)
              },
              modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .testTag("toggle_grid_columns_button")
            ) {
              Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Grid Columns (${gridColumns})",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
              )
            }

            // Cloud Sync Icon Button
            onOpenCloudSync?.let { openSync ->
              IconButton(
                onClick = openSync,
                modifier = Modifier
                  .size(40.dp)
                  .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                  .testTag("open_cloud_sync_button")
              ) {
                Icon(
                  imageVector = Icons.Default.CloudSync,
                  contentDescription = "Cloud Sync",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            // Elegant Dark Avatar indicator (JD / User pill)
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
              Text(
                text = "HD",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
              )
            }
          }
        }
      }

      // 3. Filter Chips Row (Elegant Dark Pill Styling)
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(MediaFilterType.entries.toTypedArray()) { filter ->
          val isSelected = selectedFilter == filter
          FilterChip(
            selected = isSelected,
            onClick = { onFilterChange(filter) },
            label = {
              Text(
                text = filter.label,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = null,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("filter_chip_${filter.name}")
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 4. Media Grid
      if (mediaItems.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.VideoLibrary,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No Media Found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap 'Open File' to add videos or photos in any format.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(gridColumns),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .fillMaxSize()
            .testTag("media_grid")
        ) {
          items(mediaItems, key = { it.id }) { item ->
            val isSelected = selectedItemIds.contains(item.id)
            val isInSelectionMode = selectedItemIds.isNotEmpty()

            MediaGridCard(
              item = item,
              isSelected = isSelected,
              isInSelectionMode = isInSelectionMode,
              onClick = {
                if (isInSelectionMode) {
                  onItemLongClick(item)
                } else {
                  onItemClick(item)
                }
              },
              onLongClick = { onItemLongClick(item) },
              onToggleFavorite = { onToggleFavorite(item.id) }
            )
          }
        }
      }
    }

    // 5. Selection Action Bar
    if (selectedItemIds.isNotEmpty()) {
      val selectedItemsList = mediaItems.filter { selectedItemIds.contains(it.id) }
      val selectedPhotos = selectedItemsList.filter { !it.isVideo }
      val selectedVideos = selectedItemsList.filter { it.isVideo }

      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 8.dp,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
          .testTag("selection_action_bar")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onClearSelection?.invoke() }) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Selection", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
              text = "${selectedItemIds.size} selected",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // Collage Button (if 2 to 9 photos selected)
            if (selectedPhotos.size in 2..9) {
              IconButton(
                onClick = { onOpenCollage?.invoke(selectedPhotos) },
                modifier = Modifier.testTag("selection_collage_button")
              ) {
                Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Collage", tint = MaterialTheme.colorScheme.primary)
              }
            }

            // Video Merge Button (if 2+ videos selected)
            if (selectedVideos.size >= 2) {
              IconButton(
                onClick = { onOpenMerge?.invoke(selectedVideos) },
                modifier = Modifier.testTag("selection_merge_button")
              ) {
                Icon(imageVector = Icons.Default.CallMerge, contentDescription = "Merge Videos", tint = MaterialTheme.colorScheme.primary)
              }
            }

            // Video Edit Button (if 1 video selected)
            if (selectedVideos.size == 1 && selectedItemsList.size == 1) {
              IconButton(
                onClick = { onOpenEditor?.invoke(selectedVideos.first()) },
                modifier = Modifier.testTag("selection_edit_video_button")
              ) {
                Icon(imageVector = Icons.Default.ContentCut, contentDescription = "Edit Video", tint = MaterialTheme.colorScheme.primary)
              }
            }

            // Share
            if (selectedItemsList.size == 1) {
              IconButton(
                onClick = { onShareItem(selectedItemsList.first()) },
                modifier = Modifier.testTag("selection_share_button")
              ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
              }
            }
          }
        }
      }
    } else {
      // 6. Floating Action Button for importing videos/photos
      FloatingActionButton(
        onClick = {
          try {
            documentPickerLauncher.launch(arrayOf("video/*", "image/*"))
          } catch (e: Exception) {
            mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
          }
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 20.dp, bottom = 24.dp)
          .testTag("import_media_fab")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Import Media",
            tint = MaterialTheme.colorScheme.onPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Open File",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimary
          )
        }
      }
    }
  }
}

@Composable
fun MediaGridCard(
  item: MediaItem,
  isSelected: Boolean,
  isInSelectionMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .testTag("media_card_${item.id}")
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Media Thumbnail using Coil
      AsyncImage(
        model = ImageRequest.Builder(context)
          .data(item.uri)
          .videoFrameMillis(1000)
          .crossfade(true)
          .build(),
        contentDescription = item.displayName,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // Gradient scrim for bottom caption and top overlays
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 0.4f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.75f)
              )
            )
          )
      )

      // Format Badge (Top-Left)
      Surface(
        color = Color.Black.copy(alpha = 0.45f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(8.dp)
      ) {
        Text(
          text = item.formatBadge,
          color = if (item.isVideo) MaterialTheme.colorScheme.primary else Color.White,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
      }

      // Video Duration Badge (Top-Right font-mono)
      if (item.isVideo && item.formattedDuration.isNotEmpty()) {
        Surface(
          color = Color.Black.copy(alpha = 0.5f),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
        ) {
          Text(
            text = item.formattedDuration,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      } else if (item.isFavorite) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "Favorite",
          tint = GoldFavorite,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .size(18.dp)
        )
      }

      // Center Glass Play Icon for Videos
      if (item.isVideo) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(38.dp)
            .align(Alignment.Center)
            .background(Color.White.copy(alpha = 0.22f), CircleShape)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      // Bottom Title Caption (Truncated)
      Text(
        text = item.displayName,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth(0.85f)
          .padding(start = 10.dp, bottom = 8.dp)
      )

      // Selection Checkbox
      if (isInSelectionMode) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f))
        ) {
          Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (isSelected) "Selected" else "Not Selected",
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
              .size(24.dp)
          )
        }
      }
    }
  }
}
