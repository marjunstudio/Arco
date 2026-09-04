package com.app.arco.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay

@Composable
internal fun ArcoNavDisplay(
    backStack: NavBackStack<NavKey>,
    onBack: () -> Unit,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    // ここより上に背景を塗るものが無いため、画面が塗り残した部分に素の白が出る。
    Surface(color = MaterialTheme.colorScheme.background) {
        NavDisplay(
            backStack = backStack,
            onBack = { onBack() },
            entryProvider = entryProvider,
        )
    }
}
