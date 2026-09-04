package com.app.arco.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.app.arco.core.common.AppNavigator
import com.app.arco.core.common.NavigatorEffect
import com.app.arco.core.designsystem.ArcoTheme
import org.koin.compose.koinInject

/**
 * アプリの根。プラットフォームの入口はここだけを呼ぶ。
 *
 * 依存は Koin から取る。`koinInject()` は `startKoin()` 済みのグローバルな Koin を見るので、
 * 各プラットフォームの入口が起動時に [com.app.arco.di.initKoin] を呼んでいることが前提。
 */
@Composable
fun ArcoApp() {
    val navigator: AppNavigator = koinInject()
    val tabBridge: AppTabBridge = koinInject()
    val backStack = rememberArcoBackStack()
    val entryProvider = remember { appEntryProvider() }

    // タブの上に詳細画面を積んでも「どのタブにいるか」は変わらないので、
    // 先頭ではなく、末尾から見て最初に見つかるタブの根を現在地とする。
    val selectedTab = backStack.asReversed().firstNotNullOfOrNull(AppTab::ofKey) ?: AppTab.Explore

    // ネイティブのタブバーへ現在地を渡す。Android では誰も購読しない。
    LaunchedEffect(tabBridge, selectedTab) { tabBridge.publishSelected(selectedTab) }

    ArcoTheme {
        NavigatorEffect(navigator = navigator, backStack = backStack)
        AppTabScaffold(
            selectedTab = selectedTab,
            onSelectTab = { navigator.moveToTop(it.key) },
        ) {
            ArcoNavDisplay(
                backStack = backStack,
                onBack = navigator::back,
                entryProvider = entryProvider,
            )
        }
    }
}
