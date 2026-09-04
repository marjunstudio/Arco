package com.app.arco.app

import com.app.arco.core.common.AppNavigator
import com.app.arco.feature.explore.ExploreNavKey
import com.app.arco.feature.history.HistoryNavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppTabBridgeTest {
    private fun bridge() = AppTabBridge(AppNavigator())

    @Test
    fun tabIdsAreExposedInDeclarationOrder() {
        assertEquals(listOf("explore", "history"), bridge().tabIds)
    }

    @Test
    fun idsRoundTripToTabs() {
        AppTab.entries.forEach { tab ->
            assertEquals(tab, AppTab.ofId(tab.id))
            assertEquals(tab, AppTab.ofKey(tab.key))
        }
    }

    @Test
    fun unknownIdIsNotResolved() {
        assertNull(AppTab.ofId("nope"))
    }

    @Test
    fun navKeysMapBackToTheirTabs() {
        assertEquals(AppTab.Explore, AppTab.ofKey(ExploreNavKey))
        assertEquals(AppTab.History, AppTab.ofKey(HistoryNavKey))
    }

    @Test
    fun selectedTabStartsAtExplore() {
        assertEquals(AppTab.Explore.id, bridge().selectedTabId.value)
    }

    @Test
    fun publishingUpdatesTheSelectedTab() {
        val bridge = bridge()
        bridge.publishSelected(AppTab.History)
        assertEquals(AppTab.History.id, bridge.selectedTabId.value)
    }

    @Test
    fun selectingAnUnknownIdIsIgnored() {
        // ネイティブ側が知らない id を送ってきても落ちない
        bridge().select("nope")
    }
}
