package com.app.arco.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 配色のコントラストを守る。
 *
 * 色は見て決めるものだが、文字が読めるかどうかは計算で決まる。実機を出さずに
 * 確かめられる数少ない部分なので、ここで固定しておく。
 *
 * 基準は WCAG 2.2 の AA、通常サイズの文字で 4.5:1。
 */
class ArcoColorSchemeTest {
    @Test
    fun darkSchemeIsReadable() {
        assertReadable(arcoDarkColorScheme)
    }

    @Test
    fun lightSchemeIsReadable() {
        assertReadable(arcoLightColorScheme)
    }

    private fun assertReadable(scheme: ColorScheme) {
        assertContrast("onPrimary / primary", scheme.onPrimary, scheme.primary)
        assertContrast("onSecondary / secondary", scheme.onSecondary, scheme.secondary)
        assertContrast("onBackground / background", scheme.onBackground, scheme.background)
        assertContrast("onSurface / surface", scheme.onSurface, scheme.surface)
        assertContrast("onSurfaceVariant / surfaceVariant", scheme.onSurfaceVariant, scheme.surfaceVariant)
        assertContrast("onError / error", scheme.onError, scheme.error)
    }

    private fun assertContrast(
        name: String,
        foreground: Color,
        background: Color,
    ) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            ratio >= MINIMUM_CONTRAST,
            "$name is $ratio:1, below the required $MINIMUM_CONTRAST:1",
        )
    }

    private fun contrastRatio(
        foreground: Color,
        background: Color,
    ): Double {
        val first = relativeLuminance(foreground)
        val second = relativeLuminance(background)
        return (maxOf(first, second) + OFFSET) / (minOf(first, second) + OFFSET)
    }

    private fun relativeLuminance(color: Color): Double =
        RED_WEIGHT * linearize(color.red) +
            GREEN_WEIGHT * linearize(color.green) +
            BLUE_WEIGHT * linearize(color.blue)

    private fun linearize(component: Float): Double {
        val value = component.toDouble()
        return if (value <= LINEAR_THRESHOLD) value / LINEAR_DIVISOR else ((value + 0.055) / 1.055).pow(2.4)
    }

    private companion object {
        const val MINIMUM_CONTRAST = 4.5
        const val OFFSET = 0.05
        const val RED_WEIGHT = 0.2126
        const val GREEN_WEIGHT = 0.7152
        const val BLUE_WEIGHT = 0.0722
        const val LINEAR_THRESHOLD = 0.03928
        const val LINEAR_DIVISOR = 12.92
    }
}
