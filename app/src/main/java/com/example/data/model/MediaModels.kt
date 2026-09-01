package com.example.data.model

import android.net.Uri

data class MediaItem(
  val id: Long,
  val uri: Uri,
  val title: String,
  val displayName: String,
  val path: String,
  val mimeType: String,
  val size: Long,
  val dateAdded: Long,
  val durationMs: Long = 0L,
  val width: Int = 0,
  val height: Int = 0,
  val isVideo: Boolean = false,
  val folderName: String = "Internal",
  val isFavorite: Boolean = false,
  val lastPlaybackPositionMs: Long = 0L,
  val videoCodec: String = "",
  val audioCodec: String = "",
  val frameRate: Float = 0f
) {
  val formattedDuration: String
    get() {
      if (!isVideo || durationMs <= 0) return ""
      val totalSeconds = durationMs / 1000
      val hours = totalSeconds / 3600
      val minutes = (totalSeconds % 3600) / 60
      val seconds = totalSeconds % 60
      return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
      } else {
        String.format("%02d:%02d", minutes, seconds)
      }
    }

  val formattedSize: String
    get() {
      val kb = size / 1024.0
      val mb = kb / 1024.0
      val gb = mb / 1024.0
      return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.0f KB", kb)
        else -> "$size B"
      }
    }

  val formatBadge: String
    get() {
      if (!isVideo) {
        val ext = displayName.substringAfterLast('.', "").uppercase()
        return if (ext.isNotEmpty()) ext else "IMAGE"
      }
      val ext = displayName.substringAfterLast('.', "").uppercase()
      return when {
        ext.isNotEmpty() -> ext
        mimeType.contains("mp4", ignoreCase = true) -> "MP4"
        mimeType.contains("mkv", ignoreCase = true) || mimeType.contains("matroska", ignoreCase = true) -> "MKV"
        mimeType.contains("webm", ignoreCase = true) -> "WEBM"
        mimeType.contains("avi", ignoreCase = true) -> "AVI"
        mimeType.contains("mov", ignoreCase = true) || mimeType.contains("quicktime", ignoreCase = true) -> "MOV"
        mimeType.contains("3gp", ignoreCase = true) -> "3GP"
        mimeType.contains("flv", ignoreCase = true) -> "FLV"
        mimeType.contains("ts", ignoreCase = true) -> "TS"
        else -> "VIDEO"
      }
    }

  val resolutionString: String
    get() {
      return if (width > 0 && height > 0) {
        when {
          width >= 3840 || height >= 2160 -> "4K (${width}x${height})"
          width >= 1920 || height >= 1080 -> "1080p FHD (${width}x${height})"
          width >= 1280 || height >= 720 -> "720p HD (${width}x${height})"
          else -> "${width}x${height}"
        }
      } else {
        "Auto"
      }
    }
}

data class MediaAlbum(
  val id: String,
  val name: String,
  val coverUri: Uri,
  val itemCount: Int,
  val videoCount: Int,
  val photoCount: Int,
  val relativePath: String
)

enum class AspectRatioMode(val label: String) {
  FIT("Fit to Screen"),
  FILL("Fill / Crop"),
  SIXTEEN_NINE("16:9 Cinema"),
  FOUR_THREE("4:3 Standard"),
  ORIGINAL("Original 100%")
}

enum class MediaFilterType(val label: String) {
  ALL("All Media"),
  VIDEOS("Videos Only"),
  PHOTOS("Photos Only"),
  FAVORITES("Favorites"),
  MP4("MP4"),
  MKV("MKV"),
  WEBM("WebM / AVI")
}

enum class VideoSortType(val label: String) {
  DATE_DESC("Newest First"),
  DATE_ASC("Oldest First"),
  SIZE_DESC("Largest File"),
  DURATION_DESC("Longest Video"),
  NAME_ASC("Name (A-Z)")
}
