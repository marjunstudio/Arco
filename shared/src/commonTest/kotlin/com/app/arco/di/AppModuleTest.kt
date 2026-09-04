package com.app.arco.di

import com.app.arco.app.AppTabBridge
import com.app.arco.core.common.AppNavigator
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertSame

/**
 * Koin は実行時解決なので、登録漏れはコンパイルでは捕まらない。
 * 依存を足したらここで一度解決して、起動時まで持ち越さない。
 */
class AppModuleTest {
    private fun startArcoKoin(): Koin = startKoin { modules(appModule) }.koin

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun everyDefinitionResolves() {
        val koin = startArcoKoin()
        koin.get<AppNavigator>()
        koin.get<AppTabBridge>()
    }

    @Test
    fun appScopedDependenciesAreSingletons() {
        val koin = startArcoKoin()
        assertSame(koin.get<AppNavigator>(), koin.get<AppNavigator>())
        assertSame(koin.get<AppTabBridge>(), koin.get<AppTabBridge>())
    }
}
