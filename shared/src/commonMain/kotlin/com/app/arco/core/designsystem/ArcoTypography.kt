package com.app.arco.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 文字のサイズと太さの段階。
 *
 * フォントファイルは持たず、各 OS のシステムフォントに任せる。日本語を含む
 * フォントは数 MB あり、アプリのサイズと初回表示に効くため、世界観が固まってから入れる。
 *
 * 太さは Medium と Normal の 2 段だけにする。段が増えるほど「どれを使うか」の
 * 判断が要り、画面ごとにばらつく。
 *
 * `displayLarge` は残り距離の数字のために置いてある。この画面で一番大きい要素。
 */
internal val arcoTypography: Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontSize = 56.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.Medium,
            ),
        headlineMedium =
            TextStyle(
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Medium,
            ),
        titleMedium =
            TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
            ),
        bodyLarge =
            TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
            ),
        bodyMedium =
            TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
            ),
        labelLarge =
            TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
        labelSmall =
            TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
            ),
    )
