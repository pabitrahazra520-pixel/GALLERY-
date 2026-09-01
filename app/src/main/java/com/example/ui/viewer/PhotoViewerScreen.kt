package com.example.ui.viewer

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GoldFavorite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhotoViewerScreen(
  photoItem: MediaItem,
  onClose: () -> Unit,
  onToggleFavorite: (Long) -> Unit,
  onShare: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var scale by remember { mutableFloatStateOf(1f) }
  var offset by remember { mutableStateOf(Offset.Zero) }
  var isControlsVisible by remember { mutableStateOf(true) }
  var showInfoDialog by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AmoledBlack)
      .testTag("photo_viewer_screen")
  ) {
    // 1. Zoomable Image Container
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(1f, 5f)
            if (scale > 1f) {
              val maxX = (scale - 1f) * size.width / 2f
              val maxY = (scale - 1f) * size.height / 2f
              offset = Offset(
                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
              )
            } else {
              offset = Offset.Zero
            }
          }
        }
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = { isControlsVisible = !isControlsVisible },
            onDoubleTap = {
              if (scale > 1f) {
                scale = 1f
                offset = Offset.Zero
              } else {
                scale = 2.5f
              }
            }
          )
        }
    ) {
      AsyncImage(
        model = ImageRequest.Builder(context)
          .data(photoItem.uri)
          .crossfade(true)
          .build(),
        contentDescription = photoItem.displayName,
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = offset.x,
            translationY = offset.y
          )
      )
    }

    // 2. Top Bar & Bottom Bar Overlays
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
                Color.Black.copy(alpha = 0.75f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.75f)
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
              modifier = Modifier.testTag("close_photo_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = photoItem.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${photoItem.resolutionString} • ${photoItem.formattedSize}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
              )
            }
          }

          Row {
            IconButton(
              onClick = { onToggleFavorite(photoItem.id) },
              modifier = Modifier.testTag("photo_favorite_button")
            ) {
              Icon(
                imageVector = if (photoItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (photoItem.isFavorite) GoldFavorite else Color.White
              )
            }

            IconButton(
              onClick = { showInfoDialog = true },
              modifier = Modifier.testTag("photo_info_button")
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Details",
                tint = Color.White
              )
            }
          }
        }

        // BOTTOM BAR
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 24.dp, vertical = 20.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { onShare(photoItem) },
            modifier = Modifier.testTag("photo_share_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              tint = Color.White
            )
          }
        }
      }
    }

    // 3. Info Dialog
    if (showInfoDialog) {
      val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
      val formattedDate = remember(photoItem.dateAdded) {
        dateFormat.format(Date(photoItem.dateAdded))
      }

      AlertDialog(
        onDismissRequest = { showInfoDialog = false },
        title = {
          Text(
            text = "Photo Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow("File Name", photoItem.displayName)
            InfoRow("Type", photoItem.mimeType)
            InfoRow("Resolution", photoItem.resolutionString)
            InfoRow("File Size", photoItem.formattedSize)
            InfoRow("Date", formattedDate)
            InfoRow("Folder", photoItem.folderName)
            InfoRow("Storage Path", photoItem.path)
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
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = Color.Gray
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
