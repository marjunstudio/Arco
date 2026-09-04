package com.app.arco.core.designsystem

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable

/**
 * アプリ共通のテーマ。
 *
 * いまは Material 3 Expressive の既定値をそのまま使う薄い層でしかない。
 * 自前のトークン（配色・タイポ・図形）はここに集約する予定で、画面側が
 * `MaterialExpressiveTheme` を直接呼ばないよう、最初からこの名前で挟んでおく。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArcoTheme(content: @Composable () -> Unit) {
    MaterialExpressiveTheme(content = content)
}
