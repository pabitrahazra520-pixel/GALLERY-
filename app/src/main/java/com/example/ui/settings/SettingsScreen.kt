package com.example.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantPrimary
import com.example.ui.viewmodel.PlayerSettings

@Composable
fun SettingsScreen(
  currentTheme: AppThemeMode,
  playerSettings: PlayerSettings,
  mediaItems: List<MediaItem>,
  onThemeChange: (AppThemeMode) -> Unit,
  onPlayerSettingsChange: (PlayerSettings) -> Unit,
  onRefreshMedia: () -> Unit,
  onOpenCloudSync: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val totalVideos = mediaItems.count { it.isVideo }
  val totalPhotos = mediaItems.count { !it.isVideo }
  val totalSize = mediaItems.sumOf { it.size }
  val totalSizeFormatted = formatTotalSize(totalSize)

  LazyColumn(
    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("settings_screen")
  ) {
    // 1. Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Performance & Battery Preferences",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    // 2. Battery & Display Theme Card
    item {
      SettingsCard(title = "Theme & Appearance", icon = Icons.Default.DarkMode) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ThemeOptionRow(
            title = "Elegant Dark (Default)",
            subtitle = "Refined charcoal (#1A1C1E) with icy blue accents (#D1E4FF)",
            isSelected = currentTheme == AppThemeMode.ELEGANT_DARK,
            onClick = { onThemeChange(AppThemeMode.ELEGANT_DARK) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          ThemeOptionRow(
            title = "AMOLED Pure Black (Maximum Battery Saver)",
            subtitle = "True 0% OLED pixel power consumption for extended playback",
            isSelected = currentTheme == AppThemeMode.AMOLED_BLACK,
            onClick = { onThemeChange(AppThemeMode.AMOLED_BLACK) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          ThemeOptionRow(
            title = "Slate Dark",
            subtitle = "Modern deep slate background with vibrant accents",
            isSelected = currentTheme == AppThemeMode.SLATE_DARK,
            onClick = { onThemeChange(AppThemeMode.SLATE_DARK) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          ThemeOptionRow(
            title = "Clean Light Mode",
            subtitle = "Crisp daytime gallery aesthetic",
            isSelected = currentTheme == AppThemeMode.LIGHT,
            onClick = { onThemeChange(AppThemeMode.LIGHT) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          ThemeOptionRow(
            title = "Follow System Setting",
            subtitle = "Automatically switch with Android system dark theme",
            isSelected = currentTheme == AppThemeMode.SYSTEM,
            onClick = { onThemeChange(AppThemeMode.SYSTEM) }
          )
        }
      }
    }

    // 3. Universal Video Player Engine
    item {
      SettingsCard(title = "Video Engine & Gestures", icon = Icons.Default.VideoSettings) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          SettingsSwitchRow(
            title = "Touch Gesture Controls",
            subtitle = "Swipe left for Brightness, right for Volume, center for Seek",
            checked = playerSettings.gesturesEnabled,
            onCheckedChange = { onPlayerSettingsChange(playerSettings.copy(gesturesEnabled = it)) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          SettingsSwitchRow(
            title = "Hardware Accelerated Decoding",
            subtitle = "Low-power MediaCodec hardware pipeline for 4K / 60FPS playback",
            checked = playerSettings.hwAcceleration,
            onCheckedChange = { onPlayerSettingsChange(playerSettings.copy(hwAcceleration = it)) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          SettingsSwitchRow(
            title = "Auto-Resume Playback",
            subtitle = "Remember exact timestamp for each video file",
            checked = playerSettings.autoResume,
            onCheckedChange = { onPlayerSettingsChange(playerSettings.copy(autoResume = it)) }
          )
        }
      }
    }

    // 4. Cloud Sync & Multi-Device Vault
    item {
      SettingsCard(title = "Cloud Sync & Multi-Device", icon = Icons.Default.CloudSync) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Backup & synchronize your photos and videos across Pixel, Xiaomi/MIUI, and desktop with battery and Wi-Fi awareness.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Button(
            onClick = { onOpenCloudSync?.invoke() },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("open_cloud_sync_from_settings")
          ) {
            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Cloud Sync Dashboard")
          }
        }
      }
    }

    // 4. Media Storage Stats
    item {
      SettingsCard(title = "Storage & Library Overview", icon = Icons.Default.Storage) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            StorageStatPill("Total Scanned", "${mediaItems.size} items", MaterialTheme.colorScheme.primary)
            StorageStatPill("Videos", "$totalVideos files", MaterialTheme.colorScheme.secondary)
            StorageStatPill("Photos", "$totalPhotos files", MaterialTheme.colorScheme.primary)
          }

          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Total Indexed: $totalSizeFormatted",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
              color = MaterialTheme.colorScheme.surface,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onRefreshMedia)
                .testTag("rescan_storage_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Rescan",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Rescan",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

    // 5. Codec & Universal Format Support Info
    item {
      SettingsCard(title = "Supported Codecs & Containers", icon = Icons.Default.Info) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Full Container & Codec Compatibility (AOSP / MIUI / Modern Android):",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "• Containers: MP4, MKV (Matroska), WebM, AVI, MOV (QuickTime), 3GP, TS, FLV, M4V, WMV\n" +
              "• Video Codecs: H.264 (AVC), H.265 (HEVC), VP8, VP9, AV1, MPEG-4, H.263\n" +
              "• Audio Codecs: AAC, MP3, Opus, FLAC, Vorbis, AC3 Dolby, AMR-NB/WB\n" +
              "• Images: JPEG, PNG, WebP, GIF, HEIF/HEIC, BMP, DNG/RAW",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
          )
        }
      }
    }

    // 6. About & Offline Assurance
    item {
      SettingsCard(title = "Architecture & Privacy", icon = Icons.Default.Security) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Lightweight • 100% Offline • Zero Analytics",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Designed for lightning-fast gallery browsing and smooth media playback with zero network overhead and strict respect for device battery life.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun SettingsCard(
  title: String,
  icon: ImageVector,
  content: @Composable () -> Unit
) {
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 14.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      content()
    }
  }
}

@Composable
private fun ThemeOptionRow(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    if (isSelected) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Selected",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun SettingsSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
        uncheckedTrackColor = MaterialTheme.colorScheme.surface
      )
    )
  }
}

@Composable
private fun StorageStatPill(
  label: String,
  value: String,
  accentColor: Color
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = accentColor
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun formatTotalSize(bytes: Long): String {
  val mb = bytes / (1024.0 * 1024.0)
  val gb = mb / 1024.0
  return if (gb >= 1.0) {
    String.format("%.2f GB", gb)
  } else {
    String.format("%.1f MB", mb)
  }
}
