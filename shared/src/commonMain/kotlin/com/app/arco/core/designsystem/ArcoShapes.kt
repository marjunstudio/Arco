package com.app.arco.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 角丸の段階。
 *
 * 到着カードのような大きい面ほど丸みを強くする。数値を画面側に直接書かせないために、
 * 使う側は `MaterialTheme.shapes.large` のように段で指定する。
 */
internal val arcoShapes: Shapes =
    Shapes(
        extraSmall = RoundedCornerShape(size = 4.dp),
        small = RoundedCornerShape(size = 8.dp),
        medium = RoundedCornerShape(size = 12.dp),
        large = RoundedCornerShape(size = 20.dp),
        extraLarge = RoundedCornerShape(size = 28.dp),
    )
