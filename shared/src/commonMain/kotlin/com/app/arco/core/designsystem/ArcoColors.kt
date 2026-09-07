package com.app.arco.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 夜空。背景と、その上に乗る面
private val NightBase = Color(0xFF0B1020)
private val NightSurface = Color(0xFF151C33)
private val NightSurfaceVariant = Color(0xFF1E2742)
private val NightText = Color(0xFFE8ECF8)
private val NightTextMuted = Color(0xFF8E9AC0)

// 昼。ダークが主役だが OS 設定に追従する必要があるので用意する
private val DayBase = Color(0xFFF7F9FF)
private val DaySurface = Color(0xFFFFFFFF)
private val DaySurfaceVariant = Color(0xFFE6EAF6)
private val DayText = Color(0xFF101528)
private val DayTextMuted = Color(0xFF4A5578)

// レーダーの光。方角・残り距離・到着だけに使う色で、ばら撒くと効果が消える
private val RadarMint = Color(0xFF45E0C8)
private val RadarMintDeep = Color(0xFF0E9E86)
private val OnRadarMintDark = Color(0xFF05221C)

private val Alert = Color(0xFFFF6B6B)
private val AlertDeep = Color(0xFFB3261E)
private val OnAlert = Color(0xFF2B0505)

/**
 * 夜のレーダーを基調にした配色。こちらが主役。
 *
 * 地図を出さないぶん画面に出る要素が少ないので、暗い背景に光る線を置いて
 * 方角と残り距離だけを立たせる。画面側はここを直接見ず、[ArcoTheme] 経由で
 * `MaterialTheme.colorScheme` から取る。
 */
internal val arcoDarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = RadarMint,
        onPrimary = OnRadarMintDark,
        secondary = NightTextMuted,
        onSecondary = NightBase,
        background = NightBase,
        onBackground = NightText,
        surface = NightSurface,
        onSurface = NightText,
        surfaceVariant = NightSurfaceVariant,
        onSurfaceVariant = NightTextMuted,
        outline = NightTextMuted,
        error = Alert,
        onError = OnAlert,
    )

/** 昼の配色。ダークと同じ役割の色を、明るい背景でも読める明度に置き換えたもの。 */
internal val arcoLightColorScheme: ColorScheme =
    lightColorScheme(
        primary = RadarMintDeep,
        // ミントの上は白ではなく暗い文字にする。白だと 3.4:1 で WCAG AA の 4.5:1 に届かない。
        // ダーク側でミントの上に暗い文字を置いているのと同じ扱い
        onPrimary = DayText,
        secondary = DayTextMuted,
        onSecondary = DaySurface,
        background = DayBase,
        onBackground = DayText,
        surface = DaySurface,
        onSurface = DayText,
        surfaceVariant = DaySurfaceVariant,
        onSurfaceVariant = DayTextMuted,
        outline = DayTextMuted,
        error = AlertDeep,
        onError = DaySurface,
    )
