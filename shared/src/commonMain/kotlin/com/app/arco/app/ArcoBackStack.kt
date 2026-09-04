package com.app.arco.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.app.arco.feature.explore.ExploreNavKey
import com.app.arco.feature.history.HistoryNavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * バックスタックの生成。プロセスが死んでも復元できるよう、保存経路を持たせる。
 *
 * NavKey は interface なので、保存は polymorphic serializer 経由になる。実装を
 * ここに登録しないと、書き出しの時点で実行時に落ちる（コンパイルでは捕まらない）。
 * 画面を足したら [navKeySerializers] にも足す。
 */
@Composable
internal fun rememberArcoBackStack(): NavBackStack<NavKey> {
    val configuration = remember {
        SavedStateConfiguration { serializersModule = navKeySerializers }
    }
    return rememberNavBackStack(configuration = configuration, ExploreNavKey)
}

private val navKeySerializers = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(ExploreNavKey::class, ExploreNavKey.serializer())
        subclass(HistoryNavKey::class, HistoryNavKey.serializer())
    }
}
