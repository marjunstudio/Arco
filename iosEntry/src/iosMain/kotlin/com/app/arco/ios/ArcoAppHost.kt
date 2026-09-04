package com.app.arco.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.app.arco.app.ArcoApp
import com.app.arco.app.ArcoAppGraph
import kotlinx.coroutines.flow.StateFlow
import platform.UIKit.UIViewController

/**
 * iOS シェルから見える唯一の入口。
 *
 * Swift Export は橋渡しする関数型から `@Composable` を落とすため、Swift が到達する宣言に
 * Compose の型を出さない。Composable も graph も [viewController] の内側に閉じる。
 *
 * タブ関連を `AppTabBridge` ごと公開せずここで包み直しているのは、:shared を
 * `implementation` で抱えているから。境界に出す型は String と StateFlow<String> だけに絞る。
 */
class ArcoAppHost {
    private val graph = ArcoAppGraph()

    fun viewController(): UIViewController = ComposeUIViewController { ArcoApp(graph) }

    /** タブの id を表示順に並べたもの。ラベルとアイコンは Swift 側が持つ。 */
    fun tabIds(): List<String> = graph.appTabBridge.tabIds

    /** 選択中のタブ。Swift からは `asAsyncSequence()` で購読する。 */
    fun selectedTabId(): StateFlow<String> = graph.appTabBridge.selectedTabId

    /** ネイティブのバーがタップされたとき。 */
    fun selectTab(tabId: String) {
        graph.appTabBridge.select(tabId)
    }
}
