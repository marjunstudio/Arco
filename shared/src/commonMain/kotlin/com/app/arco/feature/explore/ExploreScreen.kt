package com.app.arco.feature.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * 探索画面。
 *
 * ViewModel はまだ無いので Stateless 側へ素通しする。ダイヤル・レーダー・到着は
 * ExploreUiState.Phase の状態遷移として、この画面の中で表現する。
 */
@Composable
fun ExploreRoute(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    ExploreScreen(contentPadding = contentPadding, modifier = modifier)
}

@Composable
fun ExploreScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // 画面の枠は端まで広げ、中身だけを避けるべき余白の内側へ寄せる。レーダーとダイヤルが
    // 入ったら padding の適用先はこの Box から、端まで描かない部品それぞれへ下りる
    Box(
        modifier = modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "探索", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview
@Composable
private fun ExploreScreenPreview() {
    ExploreScreen(contentPadding = PaddingValues())
}
