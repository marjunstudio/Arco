package com.app.arco.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

/**
 * iOS のボトムタブは Compose では描かない。
 *
 * iOS 26 の Liquid Glass を得るにはネイティブの `UITabBar` である必要があるため、
 * バーは Swift 側の `RootViewController` が Compose の上に重ねている。選択の受け渡しは
 * [AppTabBridge] が担う。[selectedTab] と [onSelectTab] をここで使わないのは、そのため。
 *
 * バーがコンテンツを隠す高さは `RootViewController` が実測して `additionalSafeAreaInsets`
 * へ入れているので、ノッチや Home Indicator と区別せず safeDrawing として読める。
 */
@Suppress("UNUSED_PARAMETER")
@Composable
actual fun AppTabScaffold(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    content(WindowInsets.safeDrawing.asPaddingValues())
}
