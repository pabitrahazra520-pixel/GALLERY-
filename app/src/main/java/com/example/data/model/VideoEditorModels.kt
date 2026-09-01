package com.example.data.model

import android.net.Uri

enum class VideoFilterPreset(val label: String, val colorMatrix: FloatArray?) {
  ORIGINAL("Original", null),
  VIBRANT("Vivid Warmth", floatArrayOf(
    1.2f, 0f, 0f, 0f, 10f,
    0f, 1.15f, 0f, 0f, 5f,
    0f, 0f, 0.95f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f
  )),
  CYBERPUNK("Cyberpunk", floatArrayOf(
    0.8f, 0f, 0.2f, 0f, 10f,
    0f, 1.2f, 0.4f, 0f, 20f,
    0.1f, 0.2f, 1.4f, 0f, 30f,
    0f, 0f, 0f, 1f, 0f
  )),
  NOIR("B&W Noir", floatArrayOf(
    0.33f, 0.59f, 0.11f, 0f, 0f,
    0.33f, 0.59f, 0.11f, 0f, 0f,
    0.33f, 0.59f, 0.11f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f
  )),
  VINTAGE("70s Vintage", floatArrayOf(
    0.9f, 0.1f, 0f, 0f, 25f,
    0.1f, 0.8f, 0.1f, 0f, 15f,
    0f, 0.1f, 0.7f, 0f, -10f,
    0f, 0f, 0f, 1f, 0f
  )),
  GOLDEN_HOUR("Golden Hour", floatArrayOf(
    1.3f, 0.1f, 0f, 0f, 15f,
    0.1f, 1.1f, 0f, 0f, 10f,
    0f, 0f, 0.8f, 0f, -15f,
    0f, 0f, 0f, 1f, 0f
  )),
  COLD_STEEL("Cold Steel", floatArrayOf(
    0.85f, 0f, 0.1f, 0f, -10f,
    0f, 0.95f, 0.15f, 0f, 0f,
    0.1f, 0.2f, 1.3f, 0f, 25f,
    0f, 0f, 0f, 1f, 0f
  )),
  SEPIA("Sepia Classic", floatArrayOf(
    0.393f, 0.769f, 0.189f, 0f, 0f,
    0.349f, 0.686f, 0.168f, 0f, 0f,
    0.272f, 0.534f, 0.131f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f
  ))
}

enum class VideoCropRatio(val label: String, val ratio: Float?) {
  ORIGINAL("Original", null),
  SQUARE("1:1 (Square)", 1.0f),
  CINEMA("16:9 (Cinema)", 16f / 9f),
  PORTRAIT("9:16 (Reels/Shorts)", 9f / 16f),
  CLASSIC("4:3 (Standard)", 4f / 3f),
  ANAMORPHIC("2.39:1 (Widescreen)", 2.39f)
}

enum class VideoTransition(val label: String) {
  CUT("Direct Cut"),
  CROSS_FADE("Cross Fade"),
  FADE_BLACK("Fade through Black")
}

data class VideoEditProject(
  val sourceItem: MediaItem,
  val trimStartMs: Long = 0L,
  val trimEndMs: Long = sourceItem.durationMs.coerceAtLeast(10_000L),
  val rotationDegrees: Int = 0, // 0, 90, 180, 270
  val isHorizontallyFlipped: Boolean = false,
  val cropRatio: VideoCropRatio = VideoCropRatio.ORIGINAL,
  val filterPreset: VideoFilterPreset = VideoFilterPreset.ORIGINAL,
  val playbackSpeed: Float = 1.0f,
  val muteAudio: Boolean = false,
  val brightnessBoost: Float = 0f,
  val contrastBoost: Float = 1f
)

data class VideoMergeClip(
  val item: MediaItem,
  val startMs: Long = 0L,
  val endMs: Long = item.durationMs.coerceAtLeast(5000L),
  val transition: VideoTransition = VideoTransition.CROSS_FADE
)
