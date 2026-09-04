package com.app.arco

import android.app.Application
import com.app.arco.di.initKoin

/**
 * Android の入口。Koin はここで立ち上げる。
 *
 * Activity の再生成をまたいで生き残る必要があるものは Koin の `single` が持つので、
 * Activity 側には何も置かない。
 */
class ArcoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
