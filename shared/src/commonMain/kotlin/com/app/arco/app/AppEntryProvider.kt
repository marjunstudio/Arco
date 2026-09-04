package com.app.arco.app

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.app.arco.feature.explore.ExploreNavKey
import com.app.arco.feature.explore.ExploreRoute
import com.app.arco.feature.history.HistoryNavKey
import com.app.arco.feature.history.HistoryRoute

/**
 * NavKey と画面の対応表。画面を足すときに触るのはここだけ。
 */
internal fun appEntryProvider(): (NavKey) -> NavEntry<NavKey> =
    entryProvider {
        entry<ExploreNavKey> { ExploreRoute() }
        entry<HistoryNavKey> { HistoryRoute() }
    }
