package com.app.arco.feature.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
fun ExploreRoute(modifier: Modifier = Modifier) {
    ExploreScreen(modifier = modifier)
}

@Composable
fun ExploreScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "探索", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview
@Composable
private fun ExploreScreenPreview() {
    ExploreScreen()
}
