package com.app.arco.app

import androidx.compose.runtime.Composable

/**
 * ボトムタブと画面本体をまとめる枠。
 *
 * Android は Compose でバーを描き、iOS は Swift 側のネイティブの `UITabBar` が描くので
 * ここでは何も描かない。名前にデザイン言語（Expressive / Glass）を入れないのは、
 * 中で分かれる以上、片方の名前を付けると実体とズレるため。
 */
@Composable
expect fun AppTabScaffold(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    content: @Composable () -> Unit,
)
