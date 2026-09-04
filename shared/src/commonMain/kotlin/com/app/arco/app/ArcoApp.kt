package com.app.arco.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.app.arco.core.common.NavigatorEffect
import com.app.arco.core.designsystem.ArcoTheme

/**
 * アプリの根。プラットフォームの入口はここだけを呼ぶ。
 */
@Composable
fun ArcoApp(graph: ArcoAppGraph) {
    val backStack = rememberArcoBackStack()
    val entryProvider = remember { appEntryProvider() }

    // タブの上に詳細画面を積んでも「どのタブにいるか」は変わらないので、
    // 先頭ではなく、末尾から見て最初に見つかるタブの根を現在地とする。
    val selectedTab = backStack.asReversed().firstNotNullOfOrNull(AppTab::ofKey) ?: AppTab.Explore

    // ネイティブのタブバーへ現在地を渡す。Android では誰も購読しない。
    LaunchedEffect(selectedTab) { graph.appTabBridge.publishSelected(selectedTab) }

    ArcoTheme {
        NavigatorEffect(navigator = graph.appNavigator, backStack = backStack)
        AppTabScaffold(
            selectedTab = selectedTab,
            onSelectTab = { graph.appNavigator.moveToTop(it.key) },
        ) {
            ArcoNavDisplay(
                backStack = backStack,
                onBack = graph.appNavigator::back,
                entryProvider = entryProvider,
            )
        }
    }
}
