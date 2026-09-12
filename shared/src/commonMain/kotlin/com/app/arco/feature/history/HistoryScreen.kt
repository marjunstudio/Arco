package com.app.arco.feature.history

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

/** 履歴画面。中身の仕様は未確定なので、いまはタブが切り替わることだけを示す。 */
@Composable
fun HistoryRoute(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    HistoryScreen(contentPadding = contentPadding, modifier = modifier)
}

@Composable
fun HistoryScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "履歴", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview
@Composable
private fun HistoryScreenPreview() {
    HistoryScreen(contentPadding = PaddingValues())
}
