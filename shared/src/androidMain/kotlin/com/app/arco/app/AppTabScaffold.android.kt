package com.app.arco.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.arco.core.designsystem.ArcoIcons

/**
 * Android のボトムタブ。Material 3 Expressive の [ShortNavigationBar] を使う。
 *
 * 画面ごとにバーを出し分ける必要が出たら、NavEntry のメタデータを見て
 * `bottomBar` を差し替える。いまは常時表示で足りる。
 */
@Composable
actual fun AppTabScaffold(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = {
            ShortNavigationBar {
                AppTab.entries.forEach { tab ->
                    ShortNavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { onSelectTab(tab) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                        label = { Text(text = tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

private val AppTab.icon: ImageVector
    get() =
        when (this) {
            AppTab.Explore -> ArcoIcons.Navigation
            AppTab.History -> ArcoIcons.History
        }
