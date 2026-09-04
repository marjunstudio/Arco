package com.app.arco.ios

import com.app.arco.di.initKoin

/**
 * iOS の起動時に1度だけ呼ぶ初期化。`iOSApp.swift` の `init()` から呼ぶ。
 *
 * :shared は `implementation` で抱えているため Swift からは見えない。Swift に見せたいものは
 * :iosEntry に薄いラッパを足して露出させる（docs/modules.md「境界に置ける型」）。
 * `initKoin()` を [ArcoAppHost] のコンストラクタに隠さないのは、グローバルな副作用を
 * 入口の1行として見える場所に置くため。
 */
fun initArco() {
    initKoin()
}
