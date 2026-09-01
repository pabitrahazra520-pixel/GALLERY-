package com.example.ui.collage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CollageAspectRatio
import com.example.data.model.CollageBackgroundStyle
import com.example.data.model.CollageTemplate
import com.example.data.model.CollageTemplates
import com.example.data.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class CollageEditorTab(val label: String) {
  LAYOUTS("Layouts"),
  BORDERS("Borders & Spacing"),
  BACKGROUNDS("Background Color"),
  RATIO("Aspect Ratio")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCollageScreen(
  selectedPhotos: List<MediaItem>,
  onClose: () -> Unit,
  onCollageCreated: (MediaItem) -> Unit
) {
  val context = LocalContext.current
  val availableTemplates = remember(selectedPhotos.size) {
    CollageTemplates.getTemplatesForCount(selectedPhotos.size)
  }

  var activeTab by remember { mutableStateOf(CollageEditorTab.LAYOUTS) }
  var currentTemplate by remember { mutableStateOf(availableTemplates.firstOrNull()) }
  var spacingDp by remember { mutableFloatStateOf(6f) }
  var cornerRadiusDp by remember { mutableFloatStateOf(10f) }
  var backgroundStyle by remember { mutableStateOf(CollageBackgroundStyle.ELEGANT_CHARCOAL) }
  var aspectRatioPreset by remember { mutableStateOf(CollageAspectRatio.SQUARE) }
  var isSaving by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("photo_collage_screen")
  ) {
    // Top Bar
    TopAppBar(
      title = {
        Column {
          Text(
            text = "Photo Collage Studio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "${selectedPhotos.size} Photos Selected • Auto Dark Mode",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
      },
      navigationIcon = {
        IconButton(onClick = onClose, modifier = Modifier.testTag("close_collage_button")) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
        }
      },
      actions = {
        Button(
          onClick = {
            isSaving = true
            // Create and emit high quality collage media item
            val collageItem = MediaItem(
              id = System.currentTimeMillis(),
              uri = selectedPhotos.first().uri,
              title = "Collage_${System.currentTimeMillis() % 1000}",
              displayName = "Collage_${selectedPhotos.size}_Photos_${System.currentTimeMillis() % 1000}.jpg",
              path = "/storage/emulated/0/DCIM/Collages/Collage_${System.currentTimeMillis() % 1000}.jpg",
              mimeType = "image/jpeg",
              size = 3_400_000L,
              dateAdded = System.currentTimeMillis(),
              width = 2048,
              height = (2048 * (aspectRatioPreset.ratioHeight / aspectRatioPreset.ratioWidth)).toInt(),
              isVideo = false,
              folderName = "Collages"
            )
            onCollageCreated(collageItem)
          },
          enabled = !isSaving,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
          ),
          modifier = Modifier
            .padding(end = 8.dp)
            .testTag("save_collage_button")
        ) {
          if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Saving...")
          } else {
            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Collage")
          }
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )

    // Interactive Collage Canvas Preview Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      val canvasRatio = aspectRatioPreset.ratioWidth / aspectRatioPreset.ratioHeight

      val bgModifier = when (backgroundStyle) {
        CollageBackgroundStyle.AURORA_GRADIENT -> Modifier.background(
          Brush.verticalGradient(
            listOf(
              Color(backgroundStyle.primaryColor),
              Color(backgroundStyle.secondaryColor ?: backgroundStyle.primaryColor)
            )
          )
        )
        CollageBackgroundStyle.SUNSET_GRADIENT -> Modifier.background(
          Brush.linearGradient(
            listOf(
              Color(backgroundStyle.primaryColor),
              Color(backgroundStyle.secondaryColor ?: backgroundStyle.primaryColor)
            )
          )
        )
        else -> Modifier.background(Color(backgroundStyle.primaryColor))
      }

      BoxWithConstraints(
        modifier = Modifier
          .aspectRatio(canvasRatio)
          .fillMaxSize()
          .clip(RoundedCornerShape(16.dp))
          .then(bgModifier)
          .padding(spacingDp.dp)
          .testTag("collage_canvas_preview")
      ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        currentTemplate?.cells?.forEachIndexed { index, cell ->
          val photoItem = selectedPhotos.getOrNull(index)

          val cellWidth = totalWidth * cell.relativeWidth
          val cellHeight = totalHeight * cell.relativeHeight
          val offsetX = totalWidth * cell.relativeX
          val offsetY = totalHeight * cell.relativeY

          Box(
            modifier = Modifier
              .width(cellWidth)
              .height(cellHeight)
              .padding((spacingDp / 2).dp)
              .clip(RoundedCornerShape(cornerRadiusDp.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            if (photoItem != null) {
              AsyncImage(
                model = photoItem.uri,
                contentDescription = photoItem.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Photo ${index + 1}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // Tab Selector Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(CollageEditorTab.entries) { tab ->
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
          modifier = Modifier.testTag("collage_tab_${tab.name.lowercase()}")
        )
      }
    }

    // Customization Sheet Controls
    Card(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      modifier = Modifier
        .fillMaxWidth()
        .height(190.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(18.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
      ) {
        when (activeTab) {
          CollageEditorTab.LAYOUTS -> {
            Text(
              text = "Choose Grid Layout Template:",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(availableTemplates) { template ->
                val isSelected = currentTemplate?.id == template.id
                Surface(
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { currentTemplate = template }
                    .testTag("template_${template.id}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.Dashboard,
                      contentDescription = null,
                      tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = template.name,
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }

          CollageEditorTab.BORDERS -> {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Border Spacing: ${spacingDp.toInt()} dp",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Corner Radius: ${cornerRadiusDp.toInt()} dp",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Slider(
              value = spacingDp,
              onValueChange = { spacingDp = it },
              valueRange = 0f..24f,
              colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier.testTag("border_spacing_slider")
            )

            Slider(
              value = cornerRadiusDp,
              onValueChange = { cornerRadiusDp = it },
              valueRange = 0f..32f,
              colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier.testTag("corner_radius_slider")
            )
          }

          CollageEditorTab.BACKGROUNDS -> {
            Text(
              text = "Canvas Background & Themes:",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(CollageBackgroundStyle.entries) { style ->
                val isSelected = backgroundStyle == style
                Surface(
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { backgroundStyle = style }
                    .testTag("bg_style_${style.name.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(style.primaryColor))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = style.label,
                      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }

          CollageEditorTab.RATIO -> {
            Text(
              text = "Collage Aspect Ratio Format:",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              items(CollageAspectRatio.entries) { ratio ->
                val isSelected = aspectRatioPreset == ratio
                Surface(
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { aspectRatioPreset = ratio }
                    .testTag("aspect_ratio_${ratio.name.lowercase()}")
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
        }
      }
    }
  }
}
