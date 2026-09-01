package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.model.MediaItem
import com.example.ui.MainAppScreen
import com.example.ui.viewmodel.GalleryViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: GalleryViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIncomingIntent(intent)

    setContent {
      MainAppScreen(viewModel = viewModel)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIncomingIntent(intent)
  }

  private fun handleIncomingIntent(intent: Intent?) {
    if (intent == null) return
    val action = intent.action
    val uri = intent.data ?: intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)

    if (uri != null && (action == Intent.ACTION_VIEW || action == Intent.ACTION_SEND)) {
      val type = intent.type.orEmpty()
      val isVideo = type.startsWith("video", ignoreCase = true) || !type.startsWith("image", ignoreCase = true)
      val fileName = uri.lastPathSegment ?: if (isVideo) "Shared_Video.mp4" else "Shared_Image.jpg"

      viewModel.importUri(uri, isVideo, fileName)

      val openedItem = MediaItem(
        id = System.currentTimeMillis(),
        uri = uri,
        title = fileName,
        displayName = fileName,
        path = uri.toString(),
        mimeType = if (isVideo) "video/mp4" else "image/jpeg",
        size = 10 * 1024 * 1024L,
        dateAdded = System.currentTimeMillis(),
        durationMs = if (isVideo) 60_000L else 0L,
        width = 1920,
        height = 1080,
        isVideo = isVideo,
        folderName = "External"
      )

      if (isVideo) {
        viewModel.openVideo(openedItem)
      } else {
        viewModel.openPhoto(openedItem)
      }
    }
  }
}
