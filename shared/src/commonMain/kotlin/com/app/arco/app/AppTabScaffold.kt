package com.app.arco.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

/**
 * ボトムタブと画面本体をまとめる枠。
 *
 * Android は Compose でバーを描き、iOS は Swift 側のネイティブの `UITabBar` が描くので
 * ここでは何も描かない。名前にデザイン言語（Expressive / Glass）を入れないのは、
 * 中で分かれる以上、片方の名前を付けると実体とズレるため。
 *
 * [content] には「コンテンツが避けるべき余白」を渡す。バーの高さもシステム UI の inset も
 * 出どころは OS ごとに違うが、受け取る側から見た形は両 OS で同じ [PaddingValues] に揃える。
 *
 * ここで包んで padding をかけずに下へ渡すのは、レーダーとダイヤルを画面の端まで描くため。
 * 背景と Canvas は全面に敷いたまま、文字と操作部だけを内側へ寄せられる形にしておく。
 */
@Composable
expect fun AppTabScaffold(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
)
