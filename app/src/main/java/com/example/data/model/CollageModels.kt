package com.example.data.model

import android.graphics.Color
import android.net.Uri

enum class CollageAspectRatio(val label: String, val ratioWidth: Float, val ratioHeight: Float) {
  SQUARE("1:1 Square", 1f, 1f),
  INSTAGRAM_PORTRAIT("4:5 Portrait", 4f, 5f),
  STORY_VERTICAL("9:16 Story", 9f, 16f),
  LANDSCAPE_CINEMA("16:9 Landscape", 16f, 9f),
  PHOTO_CLASSIC("3:2 Classic", 3f, 2f)
}

enum class CollageBackgroundStyle(val label: String, val primaryColor: Long, val secondaryColor: Long? = null) {
  ELEGANT_CHARCOAL("Charcoal Dark", 0xFF1A1C1E),
  PURE_BLACK("AMOLED Black", 0xFF000000),
  PURE_WHITE("Clean White", 0xFFFFFFFF),
  ICY_BLUE("Icy Blue", 0xFFD1E4FF),
  AURORA_GRADIENT("Aurora Glow", 0xFF1B2A4A, 0xFF00838F),
  SUNSET_GRADIENT("Sunset Amber", 0xFF3E1F47, 0xFFE65100),
  SLATE_GRAY("Slate Gray", 0xFF2D3238),
  TRANSPARENT("Transparent", 0x00000000)
}

data class CollageCell(
  val id: Int,
  val photoItem: MediaItem?,
  // Relative position and size within bounding box (0f to 1f)
  val relativeX: Float,
  val relativeY: Float,
  val relativeWidth: Float,
  val relativeHeight: Float
)

data class CollageTemplate(
  val id: String,
  val name: String,
  val photoCount: Int,
  val cells: List<CollageCell>
)

object CollageTemplates {

  fun getTemplatesForCount(count: Int): List<CollageTemplate> {
    val safeCount = count.coerceIn(2, 9)
    return when (safeCount) {
      2 -> listOf(
        CollageTemplate(
          id = "2_split_v",
          name = "Vertical Split",
          photoCount = 2,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.5f, 1f),
            CollageCell(1, null, 0.5f, 0f, 0.5f, 1f)
          )
        ),
        CollageTemplate(
          id = "2_split_h",
          name = "Horizontal Split",
          photoCount = 2,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 1f, 0.5f),
            CollageCell(1, null, 0f, 0.5f, 1f, 0.5f)
          )
        )
      )

      3 -> listOf(
        CollageTemplate(
          id = "3_top_two_bottom",
          name = "1 Top + 2 Bottom",
          photoCount = 3,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 1f, 0.5f),
            CollageCell(1, null, 0f, 0.5f, 0.5f, 0.5f),
            CollageCell(2, null, 0.5f, 0.5f, 0.5f, 0.5f)
          )
        ),
        CollageTemplate(
          id = "3_left_two_right",
          name = "1 Left + 2 Right",
          photoCount = 3,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.5f, 1f),
            CollageCell(1, null, 0.5f, 0f, 0.5f, 0.5f),
            CollageCell(2, null, 0.5f, 0.5f, 0.5f, 0.5f)
          )
        ),
        CollageTemplate(
          id = "3_vertical_strips",
          name = "3 Columns",
          photoCount = 3,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.333f, 1f),
            CollageCell(1, null, 0.333f, 0f, 0.334f, 1f),
            CollageCell(2, null, 0.667f, 0f, 0.333f, 1f)
          )
        )
      )

      4 -> listOf(
        CollageTemplate(
          id = "4_grid_2x2",
          name = "2x2 Classic Grid",
          photoCount = 4,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.5f, 0.5f),
            CollageCell(1, null, 0.5f, 0f, 0.5f, 0.5f),
            CollageCell(2, null, 0f, 0.5f, 0.5f, 0.5f),
            CollageCell(3, null, 0.5f, 0.5f, 0.5f, 0.5f)
          )
        ),
        CollageTemplate(
          id = "4_top_three_bottom",
          name = "1 Hero + 3 Bottom",
          photoCount = 4,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 1f, 0.6f),
            CollageCell(1, null, 0f, 0.6f, 0.333f, 0.4f),
            CollageCell(2, null, 0.333f, 0.6f, 0.334f, 0.4f),
            CollageCell(3, null, 0.667f, 0.6f, 0.333f, 0.4f)
          )
        ),
        CollageTemplate(
          id = "4_left_three_right",
          name = "1 Left + 3 Right",
          photoCount = 4,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.6f, 1f),
            CollageCell(1, null, 0.6f, 0f, 0.4f, 0.333f),
            CollageCell(2, null, 0.6f, 0.333f, 0.4f, 0.334f),
            CollageCell(3, null, 0.6f, 0.667f, 0.4f, 0.333f)
          )
        )
      )

      5 -> listOf(
        CollageTemplate(
          id = "5_two_top_three_bottom",
          name = "2 Top + 3 Bottom",
          photoCount = 5,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.5f, 0.5f),
            CollageCell(1, null, 0.5f, 0f, 0.5f, 0.5f),
            CollageCell(2, null, 0f, 0.5f, 0.333f, 0.5f),
            CollageCell(3, null, 0.333f, 0.5f, 0.334f, 0.5f),
            CollageCell(4, null, 0.667f, 0.5f, 0.333f, 0.5f)
          )
        ),
        CollageTemplate(
          id = "5_hero_left_four_right",
          name = "1 Hero + 4 Grid",
          photoCount = 5,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.5f, 1f),
            CollageCell(1, null, 0.5f, 0f, 0.25f, 0.5f),
            CollageCell(2, null, 0.75f, 0f, 0.25f, 0.5f),
            CollageCell(3, null, 0.5f, 0.5f, 0.25f, 0.5f),
            CollageCell(4, null, 0.75f, 0.5f, 0.25f, 0.5f)
          )
        )
      )

      6 -> listOf(
        CollageTemplate(
          id = "6_grid_3x2",
          name = "3x2 Grid",
          photoCount = 6,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.333f, 0.5f),
            CollageCell(1, null, 0.333f, 0f, 0.334f, 0.5f),
            CollageCell(2, null, 0.667f, 0f, 0.333f, 0.5f),
            CollageCell(3, null, 0f, 0.5f, 0.333f, 0.5f),
            CollageCell(4, null, 0.333f, 0.5f, 0.334f, 0.5f),
            CollageCell(5, null, 0.667f, 0.5f, 0.333f, 0.5f)
          )
        ),
        CollageTemplate(
          id = "6_grid_2x3",
          name = "2x3 Grid",
          photoCount = 6,
          cells = listOf(
            CollageCell(0, null, 0f, 0f, 0.5f, 0.333f),
            CollageCell(1, null, 0.5f, 0f, 0.5f, 0.333f),
            CollageCell(2, null, 0f, 0.333f, 0.5f, 0.334f),
            CollageCell(3, null, 0.5f, 0.333f, 0.5f, 0.334f),
            CollageCell(4, null, 0f, 0.667f, 0.5f, 0.333f),
            CollageCell(5, null, 0.5f, 0.667f, 0.5f, 0.333f)
          )
        )
      )

      else -> listOf(
        CollageTemplate(
          id = "9_grid_3x3",
          name = "3x3 Mosaic Grid",
          photoCount = safeCount,
          cells = (0 until safeCount).map { idx ->
            val row = idx / 3
            val col = idx % 3
            CollageCell(
              id = idx,
              photoItem = null,
              relativeX = col * 0.3333f,
              relativeY = row * 0.3333f,
              relativeWidth = 0.3333f,
              relativeHeight = 0.3333f
            )
          }
        )
      )
    }
  }
}
