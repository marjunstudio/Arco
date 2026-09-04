package com.app.arco.app

import androidx.navigation3.runtime.NavKey
import com.app.arco.feature.explore.ExploreNavKey
import com.app.arco.feature.history.HistoryNavKey

/**
 * ボトムタブの根。並び順は宣言順。
 *
 * ラベルだけを持ち、アイコンは持たない。Android は Material のアイコン、iOS は SF Symbols と
 * 素材が違うので、絵はそれぞれのバーの実装が自分で選ぶ。ここが持つのは「どのタブか」だけ。
 */
enum class AppTab(
    val id: String,
    val label: String,
    val key: NavKey,
) {
    Explore(id = "explore", label = "探索", key = ExploreNavKey),
    History(id = "history", label = "履歴", key = HistoryNavKey),
    ;

    companion object {
        fun ofId(id: String): AppTab? = entries.firstOrNull { it.id == id }

        fun ofKey(key: NavKey): AppTab? = entries.firstOrNull { it.key == key }
    }
}
