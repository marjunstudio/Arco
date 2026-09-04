package com.app.arco.app

import com.app.arco.core.common.AppNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Compose の外にいるネイティブのタブバーと、バックスタックをつなぐ窓口。
 *
 * 使うのは iOS だけだが、:shared と :iosEntry の両方から見える必要があるので commonMain に置く。
 * Android では誰も購読しないので、[publishSelected] は空振りする。
 *
 * 境界を越える型を String に絞ってあるのは、Swift Export がまだ Alpha だから。ラベルとアイコンは
 * 各 OS が自前で持つので、渡すのは「どのタブか」だけで足りる。
 */
class AppTabBridge internal constructor(
    private val navigator: AppNavigator,
) {
    private val mutableSelectedTabId = MutableStateFlow(AppTab.Explore.id)

    /** 表示順に並んだタブの id。 */
    val tabIds: List<String> = AppTab.entries.map { it.id }

    /** 選択中のタブ。バックスタックが真の持ち主で、これはその射影。 */
    val selectedTabId: StateFlow<String> = mutableSelectedTabId.asStateFlow()

    /** ネイティブのバーがタップされたとき。知らない id は黙って捨てる。 */
    fun select(tabId: String) {
        AppTab.ofId(tabId)?.let { navigator.moveToTop(it.key) }
    }

    /** バックスタックの現在地が変わったとき。呼ぶのは [ArcoApp] だけ。 */
    internal fun publishSelected(tab: AppTab) {
        mutableSelectedTabId.value = tab.id
    }
}
