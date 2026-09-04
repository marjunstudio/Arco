package com.app.arco.app

import com.app.arco.core.common.AppNavigator

/**
 * アプリと同じ寿命を持つものの置き場。
 *
 * DI（Koin）を入れるまでの仮の器。各プラットフォームの入口が1つだけ生成して持つ。
 * ここに置いたものは Composable の再生成やタブ移動では消えない。
 */
class ArcoAppGraph {
    val appNavigator: AppNavigator = AppNavigator()

    val appTabBridge: AppTabBridge = AppTabBridge(appNavigator)
}
