package com.app.arco.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.app.arco.feature.explore.ExploreNavKey
import com.app.arco.feature.explore.ExploreRoute
import com.app.arco.feature.history.HistoryNavKey
import com.app.arco.feature.history.HistoryRoute

/**
 * NavKey と画面の対応表。画面を足すときに触るのはここだけ。
 *
 * [contentPadding] はシステム UI とボトムタブを避けるための余白。画面ごとに避け方が
 * 変わる（背景は端まで、文字は内側）ので、ここで一律にかけず画面へそのまま渡す。
 */
internal fun appEntryProvider(contentPadding: PaddingValues): (NavKey) -> NavEntry<NavKey> =
    entryProvider {
        entry<ExploreNavKey> { ExploreRoute(contentPadding = contentPadding) }
        entry<HistoryNavKey> { HistoryRoute(contentPadding = contentPadding) }
    }
