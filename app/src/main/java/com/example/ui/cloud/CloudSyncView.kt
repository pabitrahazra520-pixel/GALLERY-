package com.example.ui.cloud

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.cloud.CloudProvider
import com.example.data.cloud.SyncSummary
import com.example.data.database.CloudAccountEntity
import com.example.data.database.CloudSyncEntity
import com.example.data.model.MediaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncView(
  account: CloudAccountEntity,
  syncSummary: SyncSummary,
  syncRecords: List<CloudSyncEntity>,
  mediaItems: List<MediaItem>,
  currentSyncingName: String?,
  isSyncRunning: Boolean,
  onClose: () -> Unit,
  onSyncAll: () -> Unit,
  onUpdateAccountSettings: (CloudProvider, String, Boolean, Boolean, Boolean, Int) -> Unit,
  onToggleOfflinePin: (Long) -> Unit
) {
  var showProviderMenu by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }

  val quotaPercent = if (account.totalQuotaBytes > 0) {
    (account.usedStorageBytes.toFloat() / account.totalQuotaBytes.toFloat()).coerceIn(0f, 1f)
  } else 0f

  val usedGb = account.usedStorageBytes / (1024.0 * 1024.0 * 1024.0)
  val totalGb = account.totalQuotaBytes / (1024.0 * 1024.0 * 1024.0)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("cloud_sync_screen")
  ) {
    // Top Bar
    TopAppBar(
      title = {
        Column {
          Text(
            text = "Cloud Sync & Multi-Device",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Battery-Aware Background Sync",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
      },
      navigationIcon = {
        IconButton(onClick = onClose, modifier = Modifier.testTag("cloud_sync_back_button")) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground
          )
        }
      },
      actions = {
        IconButton(
          onClick = { showSettingsSheet = !showSettingsSheet },
          modifier = Modifier.testTag("cloud_sync_settings_toggle")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Sync Constraints",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )

    LazyColumn(
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // 1. Cloud Provider Account Card
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                  shape = CircleShape,
                  modifier = Modifier.size(46.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.CloudSync,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(24.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = CloudProvider.entries.find { it.name == account.providerName }?.displayName ?: account.providerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = account.userEmailOrName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Box {
                Surface(
                  color = MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showProviderMenu = true }
                    .testTag("switch_provider_button")
                ) {
                  Text(
                    text = "Switch",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }

                DropdownMenu(
                  expanded = showProviderMenu,
                  onDismissRequest = { showProviderMenu = false }
                ) {
                  CloudProvider.entries.forEach { provider ->
                    DropdownMenuItem(
                      text = {
                        Column {
                          Text(provider.displayName, fontWeight = FontWeight.SemiBold)
                          Text(provider.description, fontSize = 11.sp, color = Color.Gray)
                        }
                      },
                      onClick = {
                        showProviderMenu = false
                        onUpdateAccountSettings(
                          provider,
                          account.userEmailOrName,
                          account.autoSyncEnabled,
                          account.wifiOnly,
                          account.chargingOnly,
                          account.minBatteryPercent
                        )
                      }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage quota progress
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Cloud Storage Quota",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = String.format("%.1f GB of %.0f GB used", usedGb, totalGb),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = { quotaPercent },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surface
            )
          }
        }
      }

      // 2. Sync Action & Live Status Bar
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = if (isSyncRunning) "Synchronizing Media..." else "Sync Status: Up to date",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (isSyncRunning && currentSyncingName != null) {
                    "Uploading $currentSyncingName"
                  } else {
                    "${syncSummary.syncedItems} synced • ${syncSummary.pendingItems} pending"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Button(
                onClick = onSyncAll,
                enabled = !isSyncRunning,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("sync_now_button")
              ) {
                if (isSyncRunning) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Syncing...")
                } else {
                  Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Sync All")
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              SyncStatPill("Synced", "${syncSummary.syncedItems}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
              SyncStatPill("Pending", "${syncSummary.pendingItems}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
              SyncStatPill("Offline Pin", "${syncRecords.count { it.isPinnedOffline }}", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
            }
          }
        }
      }

      // 3. Battery & Network Policy Settings (Expandable)
      item {
        AnimatedVisibility(visible = showSettingsSheet) {
          Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                text = "Battery & Power Constraints",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )

              SyncPolicyRow(
                icon = Icons.Default.Wifi,
                title = "Wi-Fi Only Sync",
                subtitle = "Prevent cellular data usage when uploading videos and photos",
                checked = account.wifiOnly,
                onCheckedChange = { checked ->
                  onUpdateAccountSettings(
                    CloudProvider.valueOf(account.providerName),
                    account.userEmailOrName,
                    account.autoSyncEnabled,
                    checked,
                    account.chargingOnly,
                    account.minBatteryPercent
                  )
                }
              )

              HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

              SyncPolicyRow(
                icon = Icons.Default.BatteryChargingFull,
                title = "Charging Only Sync",
                subtitle = "Only run background cloud upload pipeline when device is plugged into AC/USB charger",
                checked = account.chargingOnly,
                onCheckedChange = { checked ->
                  onUpdateAccountSettings(
                    CloudProvider.valueOf(account.providerName),
                    account.userEmailOrName,
                    account.autoSyncEnabled,
                    account.wifiOnly,
                    checked,
                    account.minBatteryPercent
                  )
                }
              )

              HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

              SyncPolicyRow(
                icon = Icons.Default.CloudQueue,
                title = "Automatic Background Backup",
                subtitle = "Automatically index and upload new camera shots and edited videos",
                checked = account.autoSyncEnabled,
                onCheckedChange = { checked ->
                  onUpdateAccountSettings(
                    CloudProvider.valueOf(account.providerName),
                    account.userEmailOrName,
                    checked,
                    account.wifiOnly,
                    account.chargingOnly,
                    account.minBatteryPercent
                  )
                }
              )
            }
          }
        }
      }

      // 4. Cross-Device Registry
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Connected Cross-Devices",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            DeviceItemRow("Xiaomi 14 Ultra (MIUI / HyperOS)", "This Phone • Full Resolution Backup", isCurrent = true)
            DeviceItemRow("Google Pixel Tablet", "Synced 2 hours ago • Local Cache Ready", isCurrent = false)
            DeviceItemRow("MacBook Pro 16\" / Web", "Active Desktop Session", isCurrent = false)
          }
        }
      }

      // 5. Synced Media Files Header
      item {
        Text(
          text = "Cloud-Backed Media Items (${mediaItems.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      // 6. List of Items with Offline Pin & Sync Badge
      items(mediaItems) { item ->
        val record = syncRecords.find { it.mediaId == item.id }
        val isSynced = record?.syncStatus == "SYNCED" || record?.syncStatus == "CLOUD_ONLY"
        val isSyncing = record?.syncStatus == "SYNCING"
        val isPinned = record?.isPinnedOffline ?: true

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cloud_media_row_${item.id}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isSynced) Icons.Default.CloudDone else if (isSyncing) Icons.Default.Sync else Icons.Default.CloudQueue,
              contentDescription = null,
              tint = if (isSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(24.dp)
            )
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
                text = "${item.formattedSize} • ${if (item.isVideo) "Video (${item.formattedDuration})" else "Photo"} • ${record?.syncStatus ?: "SYNCED"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            IconButton(
              onClick = { onToggleOfflinePin(item.id) },
              modifier = Modifier.testTag("pin_offline_${item.id}")
            ) {
              Icon(
                imageVector = if (isPinned) Icons.Default.DownloadDone else Icons.Default.FileDownload,
                contentDescription = "Pin Offline",
                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SyncStatPill(label: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(14.dp),
    modifier = modifier
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
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

@Composable
private fun SyncPolicyRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
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
    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
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
private fun DeviceItemRow(name: String, details: String, isCurrent: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.PhoneAndroid,
      contentDescription = null,
      tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
      Text(text = details, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (isCurrent) {
      Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
      ) {
        Text(
          text = "ACTIVE",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
    }
  }
}
