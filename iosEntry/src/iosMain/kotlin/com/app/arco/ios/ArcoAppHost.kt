package com.app.arco.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.app.arco.app.AppTabBridge
import com.app.arco.app.ArcoApp
import kotlinx.coroutines.flow.StateFlow
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

/**
 * iOS シェルから見える唯一の入口。
 *
 * Swift Export は橋渡しする関数型から `@Composable` を落とすため、Swift が到達する宣言に
 * Compose の型を出さない。Composable は [viewController] の内側に閉じる。
 *
 * タブ関連を `AppTabBridge` ごと公開せずここで包み直しているのは、:shared を
 * `implementation` で抱えているから。境界に出す型は String と StateFlow<String> だけに絞る。
 *
 * 生成より先に [initArco] が呼ばれている必要がある。
 */
class ArcoAppHost {
    // Compose の外から使うので koinInject() は使えない。グローバルの Koin から直接引く。
    // KoinComponent を継承しないのは、Swift Export の公開 API に Koin の型を出さないため。
    private val tabBridge: AppTabBridge = KoinPlatform.getKoin().get()

    fun viewController(): UIViewController = ComposeUIViewController { ArcoApp() }

    /** タブの id を表示順に並べたもの。ラベルとアイコンは Swift 側が持つ。 */
    fun tabIds(): List<String> = tabBridge.tabIds

    /** 選択中のタブ。Swift からは `asAsyncSequence()` で購読する。 */
    fun selectedTabId(): StateFlow<String> = tabBridge.selectedTabId

    /** ネイティブのバーがタップされたとき。 */
    fun selectTab(tabId: String) {
        tabBridge.select(tabId)
    }
}
