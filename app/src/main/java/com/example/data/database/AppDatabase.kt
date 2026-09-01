package com.example.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cloud_sync_records")
data class CloudSyncEntity(
  @PrimaryKey val mediaId: Long,
  val remoteId: String = "",
  val provider: String = "GOOGLE_DRIVE", // GOOGLE_DRIVE, NEXTCLOUD_WEBDAV, DROPBOX, S3, CLOUD_VAULT
  val syncStatus: String = "LOCAL_ONLY", // LOCAL_ONLY, PENDING, SYNCING, SYNCED, CLOUD_ONLY, FAILED
  val syncProgressPercent: Int = 0,
  val lastSyncedTime: Long = 0L,
  val remoteSizeBytes: Long = 0L,
  val checksumMd5: String = "",
  val isPinnedOffline: Boolean = true,
  val deviceOriginName: String = "This Device"
)

@Entity(tableName = "cloud_accounts")
data class CloudAccountEntity(
  @PrimaryKey val accountId: String,
  val providerName: String,
  val userEmailOrName: String,
  val isConnected: Boolean = true,
  val totalQuotaBytes: Long = 100L * 1024 * 1024 * 1024, // 100 GB
  val usedStorageBytes: Long = 24L * 1024 * 1024 * 1024, // 24 GB
  val autoSyncEnabled: Boolean = true,
  val wifiOnly: Boolean = true,
  val chargingOnly: Boolean = false,
  val minBatteryPercent: Int = 20
)

@Dao
interface CloudSyncDao {
  @Query("SELECT * FROM cloud_sync_records")
  fun getAllSyncRecords(): Flow<List<CloudSyncEntity>>

  @Query("SELECT * FROM cloud_sync_records WHERE mediaId = :mediaId")
  suspend fun getSyncRecord(mediaId: Long): CloudSyncEntity?

  @Query("SELECT * FROM cloud_sync_records WHERE syncStatus = :status")
  fun getRecordsByStatus(status: String): Flow<List<CloudSyncEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(record: CloudSyncEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateAll(records: List<CloudSyncEntity>)

  @Query("DELETE FROM cloud_sync_records WHERE mediaId = :mediaId")
  suspend fun deleteRecord(mediaId: Long)

  @Query("SELECT * FROM cloud_accounts")
  fun getAllAccounts(): Flow<List<CloudAccountEntity>>

  @Query("SELECT * FROM cloud_accounts WHERE accountId = :id")
  suspend fun getAccount(id: String): CloudAccountEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveAccount(account: CloudAccountEntity)
}

@Database(
  entities = [CloudSyncEntity::class, CloudAccountEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun cloudSyncDao(): CloudSyncDao
}
