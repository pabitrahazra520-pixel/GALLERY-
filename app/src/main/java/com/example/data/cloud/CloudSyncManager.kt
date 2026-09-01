package com.example.data.cloud

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.database.CloudAccountEntity
import com.example.data.database.CloudSyncEntity
import com.example.data.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CloudProvider(val displayName: String, val description: String) {
  GOOGLE_DRIVE("Google Drive", "Direct Google Drive cloud backup & cross-device photos"),
  NEXTCLOUD("Nextcloud / WebDAV", "Private self-hosted cloud storage & enterprise backup"),
  DROPBOX("Dropbox", "Seamless Dropbox media folder synchronization"),
  CLOUD_VAULT("Secure S3 Cloud Vault", "End-to-end encrypted personal S3 object storage")
}

data class SyncSummary(
  val totalItems: Int = 0,
  val syncedItems: Int = 0,
  val pendingItems: Int = 0,
  val syncingItems: Int = 0,
  val failedItems: Int = 0,
  val totalCloudBytes: Long = 0L,
  val isSyncActive: Boolean = false,
  val lastSyncTimestamp: Long = 0L
)

class CloudSyncManager(private val context: Context) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private val database: AppDatabase = Room.databaseBuilder(
    context.applicationContext,
    AppDatabase::class.java,
    "gallery_cloud_sync.db"
  ).fallbackToDestructiveMigration().build()

  private val dao = database.cloudSyncDao()

  val allSyncRecords: StateFlow<List<CloudSyncEntity>> = dao.getAllSyncRecords()
    .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _activeAccount = MutableStateFlow(
    CloudAccountEntity(
      accountId = "default_google",
      providerName = CloudProvider.GOOGLE_DRIVE.name,
      userEmailOrName = "animamaldal7@gmail.com",
      isConnected = true,
      totalQuotaBytes = 100L * 1024 * 1024 * 1024,
      usedStorageBytes = 32L * 1024 * 1024 * 1024,
      autoSyncEnabled = true,
      wifiOnly = true,
      chargingOnly = false,
      minBatteryPercent = 15
    )
  )
  val activeAccount: StateFlow<CloudAccountEntity> = _activeAccount.asStateFlow()

  private val _syncSummary = MutableStateFlow(SyncSummary())
  val syncSummary: StateFlow<SyncSummary> = _syncSummary.asStateFlow()

  private val _isSyncRunning = MutableStateFlow(false)
  val isSyncRunning: StateFlow<Boolean> = _isSyncRunning.asStateFlow()

  private val _currentSyncingItemName = MutableStateFlow<String?>(null)
  val currentSyncingItemName: StateFlow<String?> = _currentSyncingItemName.asStateFlow()

  init {
    scope.launch {
      dao.saveAccount(_activeAccount.value)
      // Initial seed demo sync records if empty
      val initialCount = dao.getAllSyncRecords()
      allSyncRecords.collect { records ->
        updateSummary(records)
      }
    }
  }

  private fun updateSummary(records: List<CloudSyncEntity>) {
    val total = records.size
    val synced = records.count { it.syncStatus == "SYNCED" || it.syncStatus == "CLOUD_ONLY" }
    val pending = records.count { it.syncStatus == "PENDING" }
    val syncing = records.count { it.syncStatus == "SYNCING" }
    val failed = records.count { it.syncStatus == "FAILED" }
    val totalBytes = records.filter { it.syncStatus == "SYNCED" }.sumOf { it.remoteSizeBytes }
    val lastTime = records.maxOfOrNull { it.lastSyncedTime } ?: 0L

    _syncSummary.value = SyncSummary(
      totalItems = total,
      syncedItems = synced,
      pendingItems = pending,
      syncingItems = syncing,
      failedItems = failed,
      totalCloudBytes = totalBytes,
      isSyncActive = _isSyncRunning.value,
      lastSyncTimestamp = lastTime
    )
  }

  // Battery and Network constraints verification
  fun canPerformSyncNow(): Pair<Boolean, String> {
    val account = _activeAccount.value
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100

    val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
      context.registerReceiver(null, filter)
    }
    val isCharging = batteryStatus?.let { intent ->
      val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
      status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    } ?: false

    if (account.chargingOnly && !isCharging) {
      return Pair(false, "Waiting for charger (Battery Optimization active)")
    }

    if (batteryLevel < account.minBatteryPercent && !isCharging) {
      return Pair(false, "Battery is low (${batteryLevel}%). Charge device to sync.")
    }

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val network = connectivityManager?.activeNetwork
    val capabilities = connectivityManager?.getNetworkCapabilities(network)

    val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
      capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true

    if (account.wifiOnly && !isWifi) {
      return Pair(false, "Wi-Fi Only is enabled. Connect to Wi-Fi to sync.")
    }

    return Pair(true, "Ready to Sync")
  }

  fun updateAccountSettings(
    provider: CloudProvider,
    email: String,
    autoSync: Boolean,
    wifiOnly: Boolean,
    chargingOnly: Boolean,
    minBattery: Int
  ) {
    scope.launch {
      val updated = _activeAccount.value.copy(
        providerName = provider.name,
        userEmailOrName = email,
        autoSyncEnabled = autoSync,
        wifiOnly = wifiOnly,
        chargingOnly = chargingOnly,
        minBatteryPercent = minBattery
      )
      _activeAccount.value = updated
      dao.saveAccount(updated)
    }
  }

  fun enqueueMediaItemForSync(item: MediaItem) {
    scope.launch {
      val existing = dao.getSyncRecord(item.id)
      val record = CloudSyncEntity(
        mediaId = item.id,
        remoteId = "cloud_${item.id}_${item.displayName}",
        provider = _activeAccount.value.providerName,
        syncStatus = "PENDING",
        syncProgressPercent = 0,
        lastSyncedTime = 0L,
        remoteSizeBytes = item.size,
        checksumMd5 = "md5_${item.id}",
        isPinnedOffline = true,
        deviceOriginName = "This Device"
      )
      dao.insertOrUpdate(record)
      startSyncQueue(listOf(item))
    }
  }

  fun syncAllMedia(items: List<MediaItem>) {
    scope.launch {
      val records = items.map { item ->
        val existing = dao.getSyncRecord(item.id)
        if (existing?.syncStatus == "SYNCED") {
          existing
        } else {
          CloudSyncEntity(
            mediaId = item.id,
            remoteId = "cloud_${item.id}_${item.displayName}",
            provider = _activeAccount.value.providerName,
            syncStatus = "PENDING",
            syncProgressPercent = 0,
            lastSyncedTime = existing?.lastSyncedTime ?: 0L,
            remoteSizeBytes = item.size,
            checksumMd5 = "md5_${item.id}",
            isPinnedOffline = true,
            deviceOriginName = "This Device"
          )
        }
      }
      dao.insertOrUpdateAll(records)
      startSyncQueue(items.filter { item ->
        val rec = records.find { it.mediaId == item.id }
        rec?.syncStatus == "PENDING"
      })
    }
  }

  private fun startSyncQueue(itemsToSync: List<MediaItem>) {
    if (_isSyncRunning.value || itemsToSync.isEmpty()) return

    scope.launch {
      val (canSync, reason) = canPerformSyncNow()
      if (!canSync) {
        return@launch
      }

      _isSyncRunning.value = true
      for (item in itemsToSync) {
        val (checkAgain, _) = canPerformSyncNow()
        if (!checkAgain) break

        _currentSyncingItemName.value = item.displayName
        // Update record to SYNCING
        dao.insertOrUpdate(
          CloudSyncEntity(
            mediaId = item.id,
            remoteId = "cloud_${item.id}_${item.displayName}",
            provider = _activeAccount.value.providerName,
            syncStatus = "SYNCING",
            syncProgressPercent = 10,
            lastSyncedTime = 0L,
            remoteSizeBytes = item.size,
            checksumMd5 = "md5_${item.id}",
            isPinnedOffline = true
          )
        )

        // Simulate chunked upload progress
        for (p in 25..100 step 25) {
          delay(120)
          dao.insertOrUpdate(
            CloudSyncEntity(
              mediaId = item.id,
              remoteId = "cloud_${item.id}_${item.displayName}",
              provider = _activeAccount.value.providerName,
              syncStatus = if (p == 100) "SYNCED" else "SYNCING",
              syncProgressPercent = p,
              lastSyncedTime = if (p == 100) System.currentTimeMillis() else 0L,
              remoteSizeBytes = item.size,
              checksumMd5 = "md5_${item.id}",
              isPinnedOffline = true
            )
          )
        }
      }

      _currentSyncingItemName.value = null
      _isSyncRunning.value = false
    }
  }

  suspend fun getSyncStatus(mediaId: Long): CloudSyncEntity? = withContext(Dispatchers.IO) {
    dao.getSyncRecord(mediaId)
  }

  fun toggleOfflinePin(mediaId: Long) {
    scope.launch {
      val record = dao.getSyncRecord(mediaId) ?: return@launch
      val updated = record.copy(isPinnedOffline = !record.isPinnedOffline)
      dao.insertOrUpdate(updated)
    }
  }
}
