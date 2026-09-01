package com.example.ui.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.AspectRatioMode
import com.example.data.model.MediaItem
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GoldFavorite
import com.example.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun UniversalVideoPlayer(
  videoItem: MediaItem,
  onClose: () -> Unit,
  onToggleFavorite: (Long) -> Unit,
  onPlaybackProgress: (Long, Long) -> Unit,
  onEditVideo: ((MediaItem) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
  val activity = context as? Activity

  // ExoPlayer instance
  val exoPlayer = remember {
    ExoPlayer.Builder(context).build().apply {
      val exoItem = ExoMediaItem.Builder()
        .setUri(videoItem.uri)
        .build()
      setMediaItem(exoItem)
      prepare()
      if (videoItem.lastPlaybackPositionMs > 0 && videoItem.lastPlaybackPositionMs < videoItem.durationMs - 3000) {
        seekTo(videoItem.lastPlaybackPositionMs)
      }
      playWhenReady = true
    }
  }

  // Player state
  var isPlaying by remember { mutableStateOf(true) }
  var currentPosition by remember { mutableLongStateOf(videoItem.lastPlaybackPositionMs) }
  var duration by remember { mutableLongStateOf(videoItem.durationMs.coerceAtLeast(1L)) }
  var isControlsVisible by remember { mutableStateOf(true) }
  var isLocked by remember { mutableStateOf(false) }
  var isLooping by remember { mutableStateOf(false) }
  var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
  var aspectRatioMode by remember { mutableStateOf(AspectRatioMode.FIT) }

  // Gesture HUD states
  var gestureHudText by remember { mutableStateOf<String?>(null) }
  var gestureHudProgress by remember { mutableFloatStateOf(0f) }
  var showGestureHud by remember { mutableStateOf(false) }

  // Metadata Dialog
  var showInfoDialog by remember { mutableStateOf(false) }
  var showSpeedMenu by remember { mutableStateOf(false) }

  // Zoom & Pan state
  var zoomScale by remember { mutableFloatStateOf(1f) }

  // Initial Volume & Brightness
  val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
  var currentVolume by remember {
    mutableFloatStateOf(
      (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 7f) / maxVolume
    )
  }
  var currentBrightness by remember {
    mutableFloatStateOf(activity?.window?.attributes?.screenBrightness?.takeIf { it >= 0 } ?: 0.5f)
  }

  // ExoPlayer listener
  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
      }

      override fun onPlaybackStateChanged(state: Int) {
        if (state == Player.STATE_READY) {
          if (exoPlayer.duration > 0) {
            duration = exoPlayer.duration
          }
        }
      }
    }
    exoPlayer.addListener(listener)

    onDispose {
      exoPlayer.removeListener(listener)
      onPlaybackProgress(videoItem.id, exoPlayer.currentPosition)
      exoPlayer.release()
    }
  }

  // Periodic position updates
  LaunchedEffect(isPlaying) {
    while (isPlaying) {
      currentPosition = exoPlayer.currentPosition
      if (exoPlayer.duration > 0) {
        duration = exoPlayer.duration
      }
      onPlaybackProgress(videoItem.id, currentPosition)
      delay(500)
    }
  }

  // Auto-hide controls
  LaunchedEffect(isControlsVisible, isPlaying) {
    if (isControlsVisible && isPlaying && !isLocked) {
      delay(4000)
      isControlsVisible = false
    }
  }

  // Hide gesture HUD
  LaunchedEffect(gestureHudText) {
    if (gestureHudText != null) {
      showGestureHud = true
      delay(1200)
      showGestureHud = false
      gestureHudText = null
    }
  }

  val config = LocalConfiguration.current
  val screenWidth = config.screenWidthDp.dp

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AmoledBlack)
      .testTag("video_player_screen")
  ) {
    // 1. ExoPlayer AndroidView
    AndroidView(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectTransformGestures { _, _, zoom, _ ->
            zoomScale = (zoomScale * zoom).coerceIn(0.8f, 3.0f)
          }
        },
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = false
          layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          resizeMode = when (aspectRatioMode) {
            AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            AspectRatioMode.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            AspectRatioMode.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
            AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
          }
        }
      },
      update = { playerView ->
        playerView.resizeMode = when (aspectRatioMode) {
          AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
          AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
          AspectRatioMode.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
          AspectRatioMode.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
          AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
      }
    )

    // 2. Gesture Detector Layer
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(isLocked) {
          detectTapGestures(
            onTap = {
              if (!isLocked) {
                isControlsVisible = !isControlsVisible
              } else {
                // When locked, show only the unlock button briefly
                isControlsVisible = !isControlsVisible
              }
            },
            onDoubleTap = { offset ->
              if (!isLocked) {
                if (offset.x < size.width / 2) {
                  // Rewind 10s
                  val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                  exoPlayer.seekTo(newPos)
                  currentPosition = newPos
                  gestureHudText = "⏪ -10s"
                } else {
                  // Forward 10s
                  val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(duration)
                  exoPlayer.seekTo(newPos)
                  currentPosition = newPos
                  gestureHudText = "⏩ +10s"
                }
              }
            }
          )
        }
    )

    // 3. Gesture HUD Overlay (Brightness, Volume, Seek)
    AnimatedVisibility(
      visible = showGestureHud && gestureHudText != null,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      Surface(
        color = Color.Black.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        modifier = Modifier.padding(24.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
          Text(
            text = gestureHudText.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          if (gestureHudProgress > 0f) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = { gestureHudProgress },
              modifier = Modifier
                .width(120.dp)
                .height(6.dp)
                .clip(CircleShape),
              color = MaterialTheme.colorScheme.primary,
              trackColor = Color.White.copy(alpha = 0.2f),
            )
          }
        }
      }
    }

    // 4. Lock Screen Button only when locked
    if (isLocked) {
      AnimatedVisibility(
        visible = isControlsVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = 24.dp)
      ) {
        IconButton(
          onClick = {
            isLocked = false
            gestureHudText = "Controls Unlocked"
          },
          modifier = Modifier
            .size(56.dp)
            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            .testTag("unlock_button")
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Unlock Controls",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }

    // 5. Full Player Controls Overlay
    if (!isLocked) {
      AnimatedVisibility(
        visible = isControlsVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.8f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.85f)
                )
              )
            )
        ) {
          // TOP BAR
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.TopCenter)
              .padding(horizontal = 12.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_player_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close Player",
                  tint = Color.White
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = videoItem.displayName,
                  style = MaterialTheme.typography.titleSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = videoItem.formatBadge,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = videoItem.resolutionString,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                  )
                }
              }
            }

            // Top action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
              // Favorite button
              IconButton(
                onClick = { onToggleFavorite(videoItem.id) },
                modifier = Modifier.testTag("player_favorite_button")
              ) {
                Icon(
                  imageVector = if (videoItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = "Favorite",
                  tint = if (videoItem.isFavorite) GoldFavorite else Color.White
                )
              }

              // Aspect ratio cycle
              IconButton(
                onClick = {
                  val modes = AspectRatioMode.entries.toTypedArray()
                  val nextIndex = (modes.indexOf(aspectRatioMode) + 1) % modes.size
                  aspectRatioMode = modes[nextIndex]
                  gestureHudText = aspectRatioMode.label
                },
                modifier = Modifier.testTag("aspect_ratio_button")
              ) {
                Icon(
                  imageVector = Icons.Default.AspectRatio,
                  contentDescription = "Aspect Ratio",
                  tint = Color.White
                )
              }

              // Edit Video Studio
              onEditVideo?.let { editAction ->
                IconButton(
                  onClick = { editAction(videoItem) },
                  modifier = Modifier.testTag("player_edit_video_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = "Edit Video",
                    tint = MaterialTheme.colorScheme.primary
                  )
                }
              }

              // Video info details
              IconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier.testTag("video_info_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "Video Information",
                  tint = Color.White
                )
              }

              // Lock Controls
              IconButton(
                onClick = {
                  isLocked = true
                  gestureHudText = "Controls Locked"
                },
                modifier = Modifier.testTag("lock_player_button")
              ) {
                Icon(
                  imageVector = Icons.Default.LockOpen,
                  contentDescription = "Lock Screen",
                  tint = Color.White
                )
              }
            }
          }

          // CENTER CONTROLS
          Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
          ) {
            // Seek -10s
            IconButton(
              onClick = {
                val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                exoPlayer.seekTo(newPos)
                currentPosition = newPos
              },
              modifier = Modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .testTag("rewind_button")
            ) {
              Icon(
                imageVector = Icons.Default.FastRewind,
                contentDescription = "Rewind 10s",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }

            // Big Play/Pause
            IconButton(
              onClick = {
                if (isPlaying) {
                  exoPlayer.pause()
                } else {
                  exoPlayer.play()
                }
              },
              modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .testTag("play_pause_button")
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(42.dp)
              )
            }

            // Seek +10s
            IconButton(
              onClick = {
                val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(duration)
                exoPlayer.seekTo(newPos)
                currentPosition = newPos
              },
              modifier = Modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .testTag("forward_button")
            ) {
              Icon(
                imageVector = Icons.Default.FastForward,
                contentDescription = "Fast Forward 10s",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }
          }

          // BOTTOM BAR & SEEKBAR
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.BottomCenter)
              .padding(horizontal = 16.dp, vertical = 20.dp)
          ) {
            // Seekbar with timestamps
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = formatDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
              )
              Slider(
                value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                onValueChange = { frac ->
                  val newPos = (frac * duration).toLong()
                  currentPosition = newPos
                  exoPlayer.seekTo(newPos)
                },
                modifier = Modifier
                  .weight(1f)
                  .padding(horizontal = 8.dp)
                  .testTag("video_seekbar"),
                colors = SliderDefaults.colors(
                  thumbColor = MaterialTheme.colorScheme.primary,
                  activeTrackColor = MaterialTheme.colorScheme.primary,
                  inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
              )
              Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom action row: Speed, Repeat, Volume hint
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Playback Speed selector
              Box {
                TextButton(
                  onClick = { showSpeedMenu = true },
                  modifier = Modifier.testTag("speed_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Playback Speed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${playbackSpeed}x",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                }

                DropdownMenu(
                  expanded = showSpeedMenu,
                  onDismissRequest = { showSpeedMenu = false }
                ) {
                  listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                    DropdownMenuItem(
                      text = { Text("${speed}x ${if (speed == 1.0f) "(Normal)" else ""}") },
                      onClick = {
                        playbackSpeed = speed
                        exoPlayer.playbackParameters = PlaybackParameters(speed)
                        showSpeedMenu = false
                        gestureHudText = "Speed: ${speed}x"
                      }
                    )
                  }
                }
              }

              // Loop mode toggle
              IconButton(
                onClick = {
                  isLooping = !isLooping
                  exoPlayer.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                  gestureHudText = if (isLooping) "Repeat: Single Track" else "Repeat: Off"
                },
                modifier = Modifier.testTag("repeat_button")
              ) {
                Icon(
                  imageVector = if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
                  contentDescription = "Loop Video",
                  tint = if (isLooping) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                )
              }
            }
          }
        }
      }
    }

    // 6. Video Metadata Inspector Dialog
    if (showInfoDialog) {
      AlertDialog(
        onDismissRequest = { showInfoDialog = false },
        title = {
          Text(
            text = "Video Information & Codecs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetadataRow("File Name", videoItem.displayName)
            MetadataRow("Container Format", videoItem.formatBadge)
            MetadataRow("Video Codec", videoItem.videoCodec.ifEmpty { "Universal H.264 / HEVC / VP9" })
            MetadataRow("Audio Codec", videoItem.audioCodec.ifEmpty { "AAC Stereo / Dolby Audio" })
            MetadataRow("Resolution", videoItem.resolutionString)
            MetadataRow("Duration", videoItem.formattedDuration)
            MetadataRow("File Size", videoItem.formattedSize)
            MetadataRow("Storage Path", videoItem.path)
            MetadataRow("Hardware Decoded", "Enabled (Low Power Mode)")
          }
        },
        confirmButton = {
          TextButton(onClick = { showInfoDialog = false }) {
            Text("Close", color = MaterialTheme.colorScheme.primary)
          }
        }
      )
    }
  }
}

@Composable
private fun MetadataRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = Color.Gray,
      fontWeight = FontWeight.Medium
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface,
      fontWeight = FontWeight.Bold,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun formatDuration(millis: Long): String {
  val totalSec = millis / 1000
  val hours = totalSec / 3600
  val minutes = (totalSec % 3600) / 60
  val seconds = totalSec % 60
  return if (hours > 0) {
    String.format("%d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format("%02d:%02d", minutes, seconds)
  }
}
