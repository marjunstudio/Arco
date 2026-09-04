package com.app.arco.di

import org.koin.core.context.startKoin

/**
 * Koin を立ち上げる。各プラットフォームの入口が起動時に1度だけ呼ぶ。
 *
 * **Compose の外から呼ぶ。** `KoinApplication {}` composable は Koin の寿命を composition に
 * 縛るため、探索セッションをアプリと同じ寿命で持つ前提（docs/architecture.md）が崩れる。
 * Compose 側は `koinInject()` の既定値がグローバルの Koin を指すので、包まなくても解決される。
 *
 * 戻り値を返さないのは、Swift Export の境界に `KoinApplication` を出さないため。
 */
fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
