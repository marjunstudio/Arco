package com.app.arco.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * アプリで使うアイコン。
 *
 * Material のアイコンセット（material-icons-core）は依存に入れていないので自前で持つ。
 * 数が増えるようなら依存の追加を検討するが、いまは2つなので描いたほうが軽い。
 * 塗りは [androidx.compose.material3.Icon] が tint で上書きするため、ここでの色は意味を持たない。
 */
object ArcoIcons {
    /** 進行方向を指す矢印。探索タブ。 */
    val Navigation: ImageVector by lazy {
        icon(name = "Navigation") {
            moveTo(12f, 2f)
            lineTo(20.5f, 21f)
            lineTo(12f, 17.2f)
            lineTo(3.5f, 21f)
            close()
        }
    }

    /** 横棒3本のリスト。履歴タブ。 */
    val History: ImageVector by lazy {
        icon(name = "History") {
            bar(top = 5.5f)
            bar(top = 11f)
            bar(top = 16.5f)
        }
    }
}

private const val ICON_VIEWPORT = 24f

// 履歴アイコンの横棒。viewport 24 の中での左端・右端・太さ
private const val BAR_START = 4f
private const val BAR_END = 20f
private const val BAR_THICKNESS = 2f

private fun icon(
    name: String,
    pathBuilder: PathBuilder.() -> Unit,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = ICON_VIEWPORT.dp,
            defaultHeight = ICON_VIEWPORT.dp,
            viewportWidth = ICON_VIEWPORT,
            viewportHeight = ICON_VIEWPORT,
        ).path(fill = SolidColor(Color.Black), pathBuilder = pathBuilder)
        .build()

private fun PathBuilder.bar(top: Float) {
    moveTo(BAR_START, top)
    lineTo(BAR_END, top)
    lineTo(BAR_END, top + BAR_THICKNESS)
    lineTo(BAR_START, top + BAR_THICKNESS)
    close()
}
