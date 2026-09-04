package com.app.arco

import androidx.compose.ui.window.ComposeUIViewController

// Swift から MainViewControllerKt.MainViewController() として呼ぶため、名前は変えられない
@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController() = ComposeUIViewController { App() }
