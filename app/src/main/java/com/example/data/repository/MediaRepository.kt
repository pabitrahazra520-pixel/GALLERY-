package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.model.MediaAlbum
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(private val context: Context) {

  private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
  val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

  private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
  val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

  private val _playbackPositions = MutableStateFlow<Map<Long, Long>>(emptyMap())
  val playbackPositions: StateFlow<Map<Long, Long>> = _playbackPositions.asStateFlow()

  private val _importedMediaItems = MutableStateFlow<List<MediaItem>>(emptyList())

  suspend fun loadMedia(forceRefresh: Boolean = false): List<MediaItem> = withContext(Dispatchers.IO) {
    val items = mutableListOf<MediaItem>()

    // 1. Query System MediaStore for Videos
    items.addAll(queryVideos())

    // 2. Query System MediaStore for Images
    items.addAll(queryImages())

    // 3. Add any user-imported media items via SAF / File Picker
    items.addAll(_importedMediaItems.value)

    // 4. If system storage has no media (or permission wasn't granted yet), include built-in offline samples
    if (items.isEmpty()) {
      items.addAll(getOfflineDemoMedia())
    }

    // Sort newest first
    val sorted = items.sortedByDescending { it.dateAdded }.map { item ->
      item.copy(
        isFavorite = _favoriteIds.value.contains(item.id),
        lastPlaybackPositionMs = _playbackPositions.value[item.id] ?: 0L
      )
    }

    _mediaItems.value = sorted
    sorted
  }

  private fun queryVideos(): List<MediaItem> {
    val videoList = mutableListOf<MediaItem>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
      MediaStore.Video.Media._ID,
      MediaStore.Video.Media.DISPLAY_NAME,
      MediaStore.Video.Media.TITLE,
      MediaStore.Video.Media.DATA,
      MediaStore.Video.Media.MIME_TYPE,
      MediaStore.Video.Media.SIZE,
      MediaStore.Video.Media.DATE_ADDED,
      MediaStore.Video.Media.DURATION,
      MediaStore.Video.Media.WIDTH,
      MediaStore.Video.Media.HEIGHT
    )

    try {
      context.contentResolver.query(
        collection,
        projection,
        null,
        null,
        "${MediaStore.Video.Media.DATE_ADDED} DESC"
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
        val titleCol = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
        val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
        val mimeCol = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
        val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
        val dateCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
        val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
        val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
        val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idCol)
          val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
          val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "Video_$id" else "Video_$id"
          val title = if (titleCol >= 0) cursor.getString(titleCol) ?: name else name
          val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
          val mime = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "video/*" else "video/*"
          val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
          val dateAdded = if (dateCol >= 0) cursor.getLong(dateCol) * 1000 else System.currentTimeMillis()
          val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
          val width = if (widthCol >= 0) cursor.getInt(widthCol) else 1920
          val height = if (heightCol >= 0) cursor.getInt(heightCol) else 1080

          val folder = extractFolderName(path)

          videoList.add(
            MediaItem(
              id = id,
              uri = uri,
              title = title,
              displayName = name,
              path = path,
              mimeType = mime,
              size = size,
              dateAdded = dateAdded,
              durationMs = duration,
              width = width,
              height = height,
              isVideo = true,
              folderName = folder
            )
          )
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return videoList
  }

  private fun queryImages(): List<MediaItem> {
    val imageList = mutableListOf<MediaItem>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
      MediaStore.Images.Media._ID,
      MediaStore.Images.Media.DISPLAY_NAME,
      MediaStore.Images.Media.TITLE,
      MediaStore.Images.Media.DATA,
      MediaStore.Images.Media.MIME_TYPE,
      MediaStore.Images.Media.SIZE,
      MediaStore.Images.Media.DATE_ADDED,
      MediaStore.Images.Media.WIDTH,
      MediaStore.Images.Media.HEIGHT
    )

    try {
      context.contentResolver.query(
        collection,
        projection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
      )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameCol = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
        val titleCol = cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
        val dataCol = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
        val mimeCol = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
        val sizeCol = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
        val dateCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
        val widthCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
        val heightCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idCol)
          val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
          val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "Image_$id" else "Image_$id"
          val title = if (titleCol >= 0) cursor.getString(titleCol) ?: name else name
          val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
          val mime = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "image/*" else "image/*"
          val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
          val dateAdded = if (dateCol >= 0) cursor.getLong(dateCol) * 1000 else System.currentTimeMillis()
          val width = if (widthCol >= 0) cursor.getInt(widthCol) else 1920
          val height = if (heightCol >= 0) cursor.getInt(heightCol) else 1080

          val folder = extractFolderName(path)

          imageList.add(
            MediaItem(
              id = id,
              uri = uri,
              title = title,
              displayName = name,
              path = path,
              mimeType = mime,
              size = size,
              dateAdded = dateAdded,
              durationMs = 0L,
              width = width,
              height = height,
              isVideo = false,
              folderName = folder
            )
          )
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return imageList
  }

  private fun extractFolderName(path: String): String {
    if (path.isEmpty()) return "Internal"
    return try {
      val file = File(path)
      val parent = file.parentFile?.name ?: "Internal"
      if (parent.equals("0", ignoreCase = true) || parent.equals("emulated", ignoreCase = true)) {
        "Internal Storage"
      } else {
        parent
      }
    } catch (e: Exception) {
      "Internal"
    }
  }

  fun toggleFavorite(itemId: Long) {
    val current = _favoriteIds.value.toMutableSet()
    if (current.contains(itemId)) {
      current.remove(itemId)
    } else {
      current.add(itemId)
    }
    _favoriteIds.value = current

    _mediaItems.value = _mediaItems.value.map {
      if (it.id == itemId) it.copy(isFavorite = current.contains(itemId)) else it
    }
  }

  fun updatePlaybackPosition(itemId: Long, positionMs: Long) {
    val updated = _playbackPositions.value.toMutableMap()
    updated[itemId] = positionMs
    _playbackPositions.value = updated

    _mediaItems.value = _mediaItems.value.map {
      if (it.id == itemId) it.copy(lastPlaybackPositionMs = positionMs) else it
    }
  }

  fun addMediaItem(item: MediaItem) {
    val current = _importedMediaItems.value.toMutableList()
    current.add(0, item)
    _importedMediaItems.value = current

    val all = _mediaItems.value.toMutableList()
    all.add(0, item)
    _mediaItems.value = all
  }

  fun importMediaUri(uri: Uri, isVideo: Boolean = true, displayName: String? = null) {
    val id = System.currentTimeMillis()
    val name = displayName ?: (if (isVideo) "Imported_Video_${id % 1000}.mp4" else "Imported_Image_${id % 1000}.jpg")
    val newItem = MediaItem(
      id = id,
      uri = uri,
      title = name,
      displayName = name,
      path = uri.toString(),
      mimeType = if (isVideo) "video/mp4" else "image/jpeg",
      size = 25 * 1024 * 1024L,
      dateAdded = System.currentTimeMillis(),
      durationMs = if (isVideo) 120_000L else 0L,
      width = 1920,
      height = 1080,
      isVideo = isVideo,
      folderName = "Imported Media"
    )

    val current = _importedMediaItems.value.toMutableList()
    current.add(0, newItem)
    _importedMediaItems.value = current

    val all = _mediaItems.value.toMutableList()
    all.add(0, newItem)
    _mediaItems.value = all
  }

  fun getAlbums(items: List<MediaItem>): List<MediaAlbum> {
    val grouped = items.groupBy { it.folderName }
    return grouped.map { (folder, mediaInFolder) ->
      val cover = mediaInFolder.firstOrNull()?.uri ?: Uri.EMPTY
      val vCount = mediaInFolder.count { it.isVideo }
      val pCount = mediaInFolder.count { !it.isVideo }
      MediaAlbum(
        id = folder,
        name = folder,
        coverUri = cover,
        itemCount = mediaInFolder.size,
        videoCount = vCount,
        photoCount = pCount,
        relativePath = folder
      )
    }.sortedByDescending { it.itemCount }
  }

  // Pre-configured rich offline demo media for instant experience on any device or emulator
  private fun getOfflineDemoMedia(): List<MediaItem> {
    val now = System.currentTimeMillis()
    val hour = 3600 * 1000L
    val day = 24 * hour

    return listOf(
      // High-performance test video streams / media (ExoPlayer compatible MP4, WebM, MKV trailers)
      MediaItem(
        id = 1001L,
        uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
        title = "Big Buck Bunny 4K Cinematic",
        displayName = "Big_Buck_Bunny_1080p.mp4",
        path = "/storage/emulated/0/Movies/Big_Buck_Bunny_1080p.mp4",
        mimeType = "video/mp4",
        size = 158_000_000L,
        dateAdded = now - (2 * hour),
        durationMs = 596_000L, // 9 min 56 sec
        width = 1920,
        height = 1080,
        isVideo = true,
        folderName = "Movies",
        videoCodec = "H.264 / AVC (High Profile)",
        audioCodec = "AAC-LC 320kbps Stereo",
        frameRate = 60f
      ),
      MediaItem(
        id = 1002L,
        uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
        title = "Elephants Dream (MKV Ultra HD)",
        displayName = "Elephants_Dream_Master.mkv",
        path = "/storage/emulated/0/Movies/Elephants_Dream_Master.mkv",
        mimeType = "video/x-matroska",
        size = 230_000_000L,
        dateAdded = now - (6 * hour),
        durationMs = 653_000L, // 10 min 53 sec
        width = 1920,
        height = 1080,
        isVideo = true,
        folderName = "Movies",
        videoCodec = "HEVC / H.265 (Main 10)",
        audioCodec = "Dolby AC3 5.1 Surround",
        frameRate = 30f
      ),
      MediaItem(
        id = 1003L,
        uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
        title = "For Bigger Blazes (HDR Action)",
        displayName = "For_Bigger_Blazes_60FPS.webm",
        path = "/storage/emulated/0/Camera/For_Bigger_Blazes_60FPS.webm",
        mimeType = "video/webm",
        size = 45_000_000L,
        dateAdded = now - (12 * hour),
        durationMs = 15_000L,
        width = 1920,
        height = 1080,
        isVideo = true,
        folderName = "Camera",
        videoCodec = "VP9 Profile 2",
        audioCodec = "Opus 48kHz",
        frameRate = 60f
      ),
      MediaItem(
        id = 1004L,
        uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
        title = "For Bigger Escapes (Nature Drone 4K)",
        displayName = "Nature_Drone_Escape_4K.mov",
        path = "/storage/emulated/0/DCIM/Nature_Drone_Escape_4K.mov",
        mimeType = "video/quicktime",
        size = 85_000_000L,
        dateAdded = now - (1 * day),
        durationMs = 15_000L,
        width = 3840,
        height = 2160,
        isVideo = true,
        folderName = "Camera",
        videoCodec = "ProRes / H.264",
        audioCodec = "PCM Linear 24-bit",
        frameRate = 60f
      ),
      MediaItem(
        id = 1005L,
        uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
        title = "Tears of Steel (Sci-Fi AVI Clip)",
        displayName = "Tears_Of_Steel_Scene.avi",
        path = "/storage/emulated/0/Downloads/Tears_Of_Steel_Scene.avi",
        mimeType = "video/x-msvideo",
        size = 320_000_000L,
        dateAdded = now - (2 * day),
        durationMs = 734_000L, // 12 min 14 sec
        width = 1920,
        height = 800,
        isVideo = true,
        folderName = "Downloads",
        videoCodec = "MPEG-4 Part 2 / DivX",
        audioCodec = "MP3 320kbps",
        frameRate = 24f
      ),
      MediaItem(
        id = 1006L,
        uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"),
        title = "Sintel Fantasy Animation 3GP/MP4",
        displayName = "Sintel_Official_HD.3gp",
        path = "/storage/emulated/0/WhatsApp/Sintel_Official_HD.3gp",
        mimeType = "video/3gpp",
        size = 120_000_000L,
        dateAdded = now - (3 * day),
        durationMs = 888_000L, // 14 min 48 sec
        width = 1280,
        height = 720,
        isVideo = true,
        folderName = "WhatsApp",
        videoCodec = "H.263 / H.264 Baseline",
        audioCodec = "AMR-NB / AAC",
        frameRate = 24f
      ),
      // Offline Photo gallery assets
      MediaItem(
        id = 2001L,
        uri = Uri.parse("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1200&q=80"),
        title = "Yosemite Valley Sunset Landscape",
        displayName = "IMG_20260831_182045.jpg",
        path = "/storage/emulated/0/DCIM/Camera/IMG_20260831_182045.jpg",
        mimeType = "image/jpeg",
        size = 4_200_000L,
        dateAdded = now - (3 * hour),
        width = 4032,
        height = 3024,
        isVideo = false,
        folderName = "Camera"
      ),
      MediaItem(
        id = 2002L,
        uri = Uri.parse("https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200&q=80"),
        title = "Night Sky Aurora Borealis",
        displayName = "Aurora_Night_Pro.png",
        path = "/storage/emulated/0/Pictures/Screenshots/Aurora_Night_Pro.png",
        mimeType = "image/png",
        size = 6_800_000L,
        dateAdded = now - (8 * hour),
        width = 3840,
        height = 2160,
        isVideo = false,
        folderName = "Screenshots"
      ),
      MediaItem(
        id = 2003L,
        uri = Uri.parse("https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1200&q=80"),
        title = "Morning Fog in Redwood Forest",
        displayName = "Redwoods_Misty.jpg",
        path = "/storage/emulated/0/DCIM/Camera/Redwoods_Misty.jpg",
        mimeType = "image/jpeg",
        size = 3_900_000L,
        dateAdded = now - (1 * day),
        width = 4000,
        height = 3000,
        isVideo = false,
        folderName = "Camera"
      ),
      MediaItem(
        id = 2004L,
        uri = Uri.parse("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200&q=80"),
        title = "Tropical Cyan Beach Paradise",
        displayName = "Beach_Holiday_2026.webp",
        path = "/storage/emulated/0/Download/Beach_Holiday_2026.webp",
        mimeType = "image/webp",
        size = 2_100_000L,
        dateAdded = now - (2 * day),
        width = 2560,
        height = 1440,
        isVideo = false,
        folderName = "Downloads"
      )
    )
  }
}
