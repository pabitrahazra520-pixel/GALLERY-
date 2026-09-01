package com.example.ui.editor

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.model.MediaItem
import com.example.data.model.VideoCropRatio
import com.example.data.model.VideoEditProject
import com.example.data.model.VideoFilterPreset
import kotlinx.coroutines.delay

enum class VideoEditorTab(val label: String) {
  TRIM("Trim & Cut"),
  CROP("Crop & Ratio"),
  ROTATE("Rotate & Flip"),
  FILTERS("Filters & Color"),
  SPEED("Speed & Audio")
}

@OptIn(UnstableApi::class)
@Composable
fun VideoEditorScreen(
  sourceVideo: MediaItem,
  onClose: () -> Unit,
  onExportSuccess: (MediaItem) -> Unit
) {
  val context = LocalContext.current
  val totalDuration = sourceVideo.durationMs.coerceAtLeast(10_000L)

  var activeTab by remember { mutableStateOf(VideoEditorTab.TRIM) }

  var trimStartMs by remember { mutableLongStateOf(0L) }
  var trimEndMs by remember { mutableLongStateOf(totalDuration) }

  var rotationAngle by remember { mutableFloatStateOf(0f) }
  var isFlippedHorizontal by remember { mutableStateOf(false) }
  var cropRatio by remember { mutableStateOf(VideoCropRatio.ORIGINAL) }
  var selectedFilter by remember { mutableStateOf(VideoFilterPreset.ORIGINAL) }
  var speedMultiplier by remember { mutableFloatStateOf(1.0f) }
  var isMuted by remember { mutableStateOf(false) }

  var brightness by remember { mutableFloatStateOf(0f) }
  var contrast by remember { mutableFloatStateOf(1f) }

  var isExporting by remember { mutableStateOf(false) }
  var isPlaying by remember { mutableStateOf(true) }
  var currentPositionMs by remember { mutableLongStateOf(0L) }

  val exoPlayer = remember {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(ExoMediaItem.fromUri(sourceVideo.uri))
      repeatMode = Player.REPEAT_MODE_ALL
      prepare()
      playWhenReady = true
    }
  }

  DisposableEffect(exoPlayer) {
    onDispose {
      exoPlayer.release()
    }
  }

  // Update speed & volume on player
  LaunchedEffect(speedMultiplier, isMuted) {
    exoPlayer.playbackParameters = PlaybackParameters(speedMultiplier)
    exoPlayer.volume = if (isMuted) 0f else 1f
  }

  // Loop playback inside trim range
  LaunchedEffect(trimStartMs, trimEndMs, isPlaying) {
    while (true) {
      if (exoPlayer.isPlaying) {
        val pos = exoPlayer.currentPosition
        currentPositionMs = pos
        if (pos >= trimEndMs || pos < trimStartMs) {
          exoPlayer.seekTo(trimStartMs)
        }
      }
      delay(100)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("video_editor_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onClose,
          modifier = Modifier.testTag("editor_close_button")
        ) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Video Editor Studio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "iOS QuickTime / ProRes / MP4 / MKV",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Button(
          onClick = {
            isExporting = true
            val editedItem = sourceVideo.copy(
              id = System.currentTimeMillis(),
              displayName = "Edited_${sourceVideo.displayName}",
              title = "Edited ${sourceVideo.title}",
              durationMs = (trimEndMs - trimStartMs).coerceAtLeast(1000L),
              dateAdded = System.currentTimeMillis(),
              folderName = "Video Editor Exports"
            )
            onExportSuccess(editedItem)
          },
          enabled = !isExporting,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier.testTag("export_video_button")
        ) {
          if (isExporting) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Saving...")
          } else {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export")
          }
        }
      }

      // Video Preview Container with live Filters & Rotation
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(Color.Black),
        contentAlignment = Alignment.Center
      ) {
        val aspectModifier = when (cropRatio) {
          VideoCropRatio.ORIGINAL -> Modifier.fillMaxSize()
          VideoCropRatio.SQUARE -> Modifier.aspectRatio(1f)
          VideoCropRatio.CINEMA -> Modifier.aspectRatio(16f / 9f)
          VideoCropRatio.PORTRAIT -> Modifier.aspectRatio(9f / 16f)
          VideoCropRatio.CLASSIC -> Modifier.aspectRatio(4f / 3f)
          VideoCropRatio.ANAMORPHIC -> Modifier.aspectRatio(2.39f)
        }

        Box(
          modifier = aspectModifier
            .clip(RoundedCornerShape(8.dp))
            .graphicsLayer {
              rotationZ = rotationAngle
              scaleX = if (isFlippedHorizontal) -1f else 1f
            }
        ) {
          AndroidView(
            factory = { ctx ->
              PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                layoutParams = FrameLayout.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
                )
              }
            },
            modifier = Modifier.fillMaxSize()
          )

          // Filter overlay tint if selected
          selectedFilter.colorMatrix?.let { matrix ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  when (selectedFilter) {
                    VideoFilterPreset.VIBRANT -> Color.Red.copy(alpha = 0.08f)
                    VideoFilterPreset.CYBERPUNK -> Color.Cyan.copy(alpha = 0.12f)
                    VideoFilterPreset.NOIR -> Color.Black.copy(alpha = 0.25f)
                    VideoFilterPreset.VINTAGE -> Color(0xFF8D6E63).copy(alpha = 0.15f)
                    VideoFilterPreset.GOLDEN_HOUR -> Color(0xFFFFB300).copy(alpha = 0.14f)
                    VideoFilterPreset.COLD_STEEL -> Color(0xFF64B5F6).copy(alpha = 0.12f)
                    VideoFilterPreset.SEPIA -> Color(0xFF795548).copy(alpha = 0.18f)
                    else -> Color.Transparent
                  }
                )
            )
          }
        }

        // Floating Play/Pause Overlay indicator
        IconButton(
          onClick = {
            if (exoPlayer.isPlaying) {
              exoPlayer.pause()
              isPlaying = false
            } else {
              exoPlayer.play()
              isPlaying = true
            }
          },
          modifier = Modifier
            .size(54.dp)
            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            .align(Alignment.Center)
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
            contentDescription = "Play/Pause",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }

        // Live Trim Time Overlay
        Surface(
          color = Color.Black.copy(alpha = 0.65f),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 12.dp)
        ) {
          Text(
            text = "${formatTime(currentPositionMs)} / ${formatTime(trimEndMs - trimStartMs)} (Trimmed Duration)",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      // Editing Mode Tabs
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(VideoEditorTab.entries) { tab ->
          val isSelected = activeTab == tab
          FilterChip(
            selected = isSelected,
            onClick = { activeTab = tab },
            label = { Text(tab.label, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = null,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("editor_tab_${tab.name.lowercase()}")
          )
        }
      }

      // Tab Controls Panel
      Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
          .fillMaxWidth()
          .height(230.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.Center
        ) {
          when (activeTab) {
            VideoEditorTab.TRIM -> {
              Text(
                text = "Trim Range: ${formatTime(trimStartMs)} - ${formatTime(trimEndMs)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(8.dp))

              var sliderRange by remember(trimStartMs, trimEndMs) {
                mutableStateOf(trimStartMs.toFloat()..trimEndMs.toFloat())
              }

              RangeSlider(
                value = sliderRange,
                onValueChange = { range ->
                  sliderRange = range
                  trimStartMs = range.start.toLong()
                  trimEndMs = range.endInclusive.toLong()
                  exoPlayer.seekTo(trimStartMs)
                },
                valueRange = 0f..totalDuration.toFloat(),
                colors = SliderDefaults.colors(
                  thumbColor = MaterialTheme.colorScheme.primary,
                  activeTrackColor = MaterialTheme.colorScheme.primary,
                  inactiveTrackColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("trim_range_slider")
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Button(
                    onClick = {
                      trimStartMs = (trimStartMs - 500L).coerceAtLeast(0L)
                      exoPlayer.seekTo(trimStartMs)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                  ) {
                    Text("-0.5s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Button(
                    onClick = {
                      trimStartMs = (trimStartMs + 500L).coerceAtMost(trimEndMs - 1000L)
                      exoPlayer.seekTo(trimStartMs)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                  ) {
                    Text("+0.5s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Button(
                    onClick = {
                      trimEndMs = (trimEndMs - 500L).coerceAtLeast(trimStartMs + 1000L)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                  ) {
                    Text("-0.5s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Button(
                    onClick = {
                      trimEndMs = (trimEndMs + 500L).coerceAtMost(totalDuration)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                  ) {
                    Text("+0.5s", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                  }
                }
              }
            }

            VideoEditorTab.CROP -> {
              Text(
                text = "Select Crop & Aspect Ratio Preset:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(10.dp))
              LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoCropRatio.entries) { ratio ->
                  val isSelected = cropRatio == ratio
                  Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { cropRatio = ratio }
                      .testTag("crop_ratio_${ratio.name.lowercase()}")
                  ) {
                    Text(
                      text = ratio.label,
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                  }
                }
              }
            }

            VideoEditorTab.ROTATE -> {
              Text(
                text = "Orientation & Mirror Flip:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Button(
                  onClick = { rotationAngle = (rotationAngle + 90f) % 360f },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Rotate 90°", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                  onClick = { isFlippedHorizontal = !isFlippedHorizontal },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(imageVector = Icons.Default.Flip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Flip Mirror", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                  onClick = {
                    rotationAngle = 0f
                    isFlippedHorizontal = false
                  },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                  modifier = Modifier.weight(0.8f)
                ) {
                  Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Reset", color = MaterialTheme.colorScheme.onSurface)
                }
              }
            }

            VideoEditorTab.FILTERS -> {
              Text(
                text = "Real-time Video Filter Preset:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(10.dp))
              LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoFilterPreset.entries) { preset ->
                  val isSelected = selectedFilter == preset
                  Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                      .clip(RoundedCornerShape(12.dp))
                      .clickable { selectedFilter = preset }
                      .testTag("filter_preset_${preset.name.lowercase()}")
                  ) {
                    Text(
                      text = preset.label,
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                  }
                }
              }
            }

            VideoEditorTab.SPEED -> {
              Text(
                text = "Playback Speed & Audio Track:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                LazyRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  items(listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)) { spd ->
                    val isSelected = speedMultiplier == spd
                    Surface(
                      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { speedMultiplier = spd }
                    ) {
                      Text(
                        text = "${spd}x",
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                  onClick = { isMuted = !isMuted },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                  Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isMuted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (isMuted) "Muted" else "Audio ON", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun formatTime(ms: Long): String {
  val totalSec = ms / 1000
  val min = totalSec / 60
  val sec = totalSec % 60
  val millis = (ms % 1000) / 100
  return String.format("%02d:%02d.%d", min, sec, millis)
}
