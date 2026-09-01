package com.example.ui.videos

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.MediaItem
import com.example.data.model.VideoSortType

@Composable
fun VideoHubView(
  videos: List<MediaItem>,
  currentSort: VideoSortType,
  onSortChange: (VideoSortType) -> Unit,
  onPlayVideo: (MediaItem) -> Unit,
  onToggleFavorite: (Long) -> Unit,
  onEditVideo: ((MediaItem) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showSortMenu by remember { mutableStateOf(false) }
  var selectedFormatFilter by remember { mutableStateOf("ALL") }
  var selectedItemForInfo by remember { mutableStateOf<MediaItem?>(null) }

  // Filtered by selected format
  val filteredVideos = remember(videos, selectedFormatFilter) {
    if (selectedFormatFilter == "ALL") {
      videos
    } else {
      videos.filter { it.formatBadge.equals(selectedFormatFilter, ignoreCase = true) }
    }
  }

  // Videos with resume progress
  val continueWatchingVideos = remember(videos) {
    videos.filter { it.lastPlaybackPositionMs > 1000L }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    // 1. Header & Sort Control
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "Videos",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Normal,
          letterSpacing = (-0.5).sp,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Universal Player Hub",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Medium
        )
      }

      // Sort Menu
      Box {
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { showSortMenu = true }
            .testTag("sort_videos_dropdown")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Sort,
              contentDescription = "Sort",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currentSort.label,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.SemiBold
            )
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        DropdownMenu(
          expanded = showSortMenu,
          onDismissRequest = { showSortMenu = false }
        ) {
          VideoSortType.entries.forEach { sort ->
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (currentSort == sort) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                  }
                  Text(
                    sort.label,
                    fontWeight = if (currentSort == sort) FontWeight.Bold else FontWeight.Normal
                  )
                }
              },
              onClick = {
                onSortChange(sort)
                showSortMenu = false
              }
            )
          }
        }
      }
    }

    // 2. Format Badges Filter (MKV, MP4, WebM, 3GP, AVI, All)
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
    ) {
      val formats = listOf("ALL", "MP4", "MKV", "WEBM", "MOV", "AVI", "3GP")
      items(formats) { fmt ->
        val isSelected = selectedFormatFilter == fmt
        FilterChip(
          selected = isSelected,
          onClick = { selectedFormatFilter = fmt },
          label = {
            Text(
              text = if (fmt == "ALL") "All Formats" else fmt,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
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
          modifier = Modifier.testTag("video_hub_filter_$fmt")
        )
      }
    }

    // 3. Continue Watching Section (if any video was played)
    if (continueWatchingVideos.isNotEmpty() && selectedFormatFilter == "ALL") {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.History,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Resume Watching",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
      ) {
        items(continueWatchingVideos, key = { "resume_${it.id}" }) { vid ->
          ContinueWatchingCard(
            video = vid,
            onPlay = { onPlayVideo(vid) }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 4. Video List
    if (filteredVideos.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No Videos in Format '$selectedFormatFilter'",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
          .fillMaxSize()
          .testTag("video_hub_list")
      ) {
        items(filteredVideos, key = { it.id }) { video ->
          VideoHubRowCard(
            video = video,
            onPlay = { onPlayVideo(video) },
            onEdit = { onEditVideo?.invoke(video) },
            onShowInfo = { selectedItemForInfo = video }
          )
        }
      }
    }

    // Video Info Dialog
    selectedItemForInfo?.let { infoItem ->
      AlertDialog(
        onDismissRequest = { selectedItemForInfo = null },
        title = {
          Text(
            text = "Video Codec & Specs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Title: ${infoItem.displayName}", fontWeight = FontWeight.Bold)
            Text("Container: ${infoItem.formatBadge}")
            Text("Codec: ${infoItem.videoCodec.ifEmpty { "Universal H.264 / HEVC / VP9" }}")
            Text("Audio: ${infoItem.audioCodec.ifEmpty { "AAC Stereo 48kHz" }}")
            Text("Resolution: ${infoItem.resolutionString}")
            Text("Duration: ${infoItem.formattedDuration}")
            Text("Size: ${infoItem.formattedSize}")
            Text("Path: ${infoItem.path}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        confirmButton = {
          TextButton(onClick = { selectedItemForInfo = null }) {
            Text("Close", color = MaterialTheme.colorScheme.primary)
          }
        }
      )
    }
  }
}

@Composable
fun ContinueWatchingCard(
  video: MediaItem,
  onPlay: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val progress = if (video.durationMs > 0) {
    (video.lastPlaybackPositionMs.toFloat() / video.durationMs).coerceIn(0f, 1f)
  } else 0f

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
      .width(190.dp)
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onPlay)
      .testTag("continue_card_${video.id}")
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
      ) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(video.uri)
            .videoFrameMillis(1000)
            .crossfade(true)
            .build(),
          contentDescription = video.displayName,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Glass Play icon center
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
          contentAlignment = Alignment.Center
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(36.dp)
              .background(Color.White.copy(alpha = 0.25f), CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Resume",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Progress bar at bottom of thumbnail
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .align(Alignment.BottomCenter),
          color = MaterialTheme.colorScheme.primary,
          trackColor = Color.White.copy(alpha = 0.3f)
        )
      }

      Column(modifier = Modifier.padding(10.dp)) {
        Text(
          text = video.displayName,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${video.formatBadge} • ${video.formattedDuration}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun VideoHubRowCard(
  video: MediaItem,
  onPlay: () -> Unit,
  onEdit: (() -> Unit)? = null,
  onShowInfo: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onPlay)
      .testTag("video_row_card_${video.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Thumbnail
      Box(
        modifier = Modifier
          .width(115.dp)
          .aspectRatio(16f / 9f)
          .clip(RoundedCornerShape(14.dp))
      ) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(video.uri)
            .videoFrameMillis(1000)
            .crossfade(true)
            .build(),
          contentDescription = video.displayName,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Overlay Format Badge
        Surface(
          color = Color.Black.copy(alpha = 0.5f),
          shape = RoundedCornerShape(bottomEnd = 6.dp),
          modifier = Modifier.align(Alignment.TopStart)
        ) {
          Text(
            text = video.formatBadge,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
          )
        }

        // Duration
        if (video.formattedDuration.isNotEmpty()) {
          Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(topStart = 6.dp),
            modifier = Modifier.align(Alignment.BottomEnd)
          ) {
            Text(
              text = video.formattedDuration,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Medium,
              fontSize = 9.sp,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Video Meta info
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = video.displayName,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = "${video.resolutionString} • ${video.formattedSize}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Folder: ${video.folderName}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        )
      }

      // Actions
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        onEdit?.let { editAction ->
          IconButton(
            onClick = editAction,
            modifier = Modifier.size(36.dp).testTag("edit_video_icon_${video.id}")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCut,
              contentDescription = "Edit Video",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        IconButton(
          onClick = onShowInfo,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Specs",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }

        IconButton(
          onClick = onPlay,
          modifier = Modifier
            .size(38.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }
  }
}
