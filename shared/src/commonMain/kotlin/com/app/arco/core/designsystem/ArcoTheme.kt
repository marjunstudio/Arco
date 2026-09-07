package com.app.arco.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable

/**
 * アプリ共通のテーマ。
 *
 * 配色・タイポグラフィ・角丸をここで束ねる。画面側が `MaterialExpressiveTheme` を
 * 直接呼ばないよう、この 1 枚を必ず通す。色や文字サイズを変えるときはここから辿れる。
 *
 * 夜のレーダーを基調にしたダークが主役だが、OS の設定には追従する。
 * [darkTheme] を明示できるのは Preview で両方を並べて確認するため。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArcoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) arcoDarkColorScheme else arcoLightColorScheme,
        typography = arcoTypography,
        shapes = arcoShapes,
        content = content,
    )
}
