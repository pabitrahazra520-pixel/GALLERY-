package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.VideoTransition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoMergeScreen(
  selectedVideos: List<MediaItem>,
  onClose: () -> Unit,
  onMergeSuccess: (MediaItem) -> Unit
) {
  var clipList by remember { mutableStateOf(selectedVideos.toMutableList()) }
  var selectedTransition by remember { mutableStateOf(VideoTransition.CROSS_FADE) }
  var isMerging by remember { mutableStateOf(false) }

  val totalDurationMs = clipList.sumOf { it.durationMs.coerceAtLeast(5000L) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("video_merge_screen")
  ) {
    TopAppBar(
      title = {
        Column {
          Text(
            text = "Merge Video Clips",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "${clipList.size} Clips • Total ${formatDuration(totalDurationMs)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
      },
      navigationIcon = {
        IconButton(onClick = onClose, modifier = Modifier.testTag("close_merge_screen")) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
        }
      },
      actions = {
        Button(
          onClick = {
            if (clipList.isNotEmpty()) {
              isMerging = true
              val mergedItem = clipList.first().copy(
                id = System.currentTimeMillis(),
                displayName = "Merged_Story_${clipList.size}_Clips.mp4",
                title = "Merged Video Project",
                durationMs = totalDurationMs,
                dateAdded = System.currentTimeMillis(),
                folderName = "Merged Videos"
              )
              onMergeSuccess(mergedItem)
            }
          },
          enabled = clipList.size >= 2 && !isMerging,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier
            .padding(end = 8.dp)
            .testTag("execute_merge_button")
        ) {
          if (isMerging) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Merging...")
          } else {
            Icon(imageVector = Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Merge (${clipList.size})")
          }
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )

    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // Transition Picker
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Transition Between Clips:",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(VideoTransition.entries) { transition ->
                val isSelected = selectedTransition == transition
                Surface(
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { selectedTransition = transition }
                ) {
                  Text(
                    text = transition.label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Clip Sequence Header
      item {
        Text(
          text = "Sequence Order (Drag / Shift Clips):",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      // Ordered Clips
      itemsIndexed(clipList) { index, item ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("merge_clip_row_$index")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = MaterialTheme.colorScheme.primary,
              shape = CircleShape,
              modifier = Modifier.size(28.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "${index + 1}",
                  color = MaterialTheme.colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
            ) {
              AsyncImage(
                model = item.uri,
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Duration: ${item.formattedDuration} • ${item.resolutionString}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Up / Down reordering
            IconButton(
              onClick = {
                if (index > 0) {
                  val updated = clipList.toMutableList()
                  val temp = updated[index]
                  updated[index] = updated[index - 1]
                  updated[index - 1] = temp
                  clipList = updated
                }
              },
              enabled = index > 0
            ) {
              Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(
              onClick = {
                if (index < clipList.size - 1) {
                  val updated = clipList.toMutableList()
                  val temp = updated[index]
                  updated[index] = updated[index + 1]
                  updated[index + 1] = temp
                  clipList = updated
                }
              },
              enabled = index < clipList.size - 1
            ) {
              Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(
              onClick = {
                val updated = clipList.toMutableList()
                updated.removeAt(index)
                clipList = updated
              }
            ) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  }
}

private fun formatDuration(ms: Long): String {
  val totalSeconds = ms / 1000
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return String.format("%02d:%02d", minutes, seconds)
}
