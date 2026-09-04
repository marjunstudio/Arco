package com.app.arco.app

import androidx.compose.runtime.Composable

/**
 * iOS のボトムタブは Compose では描かない。
 *
 * iOS 26 の Liquid Glass を得るにはネイティブの `UITabBar` である必要があるため、
 * バーは Swift 側の `RootViewController` が Compose の上に重ねている。選択の受け渡しは
 * [AppTabBridge] が担い、バーに隠れる高さは Swift が `additionalSafeAreaInsets` へ入れるので、
 * 画面側は通常の safe area の inset として受け取れる。
 *
 * [selectedTab] と [onSelectTab] をここで使わないのは、そのため。
 */
@Suppress("UNUSED_PARAMETER")
@Composable
actual fun AppTabScaffold(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    content: @Composable () -> Unit,
) {
    content()
}
