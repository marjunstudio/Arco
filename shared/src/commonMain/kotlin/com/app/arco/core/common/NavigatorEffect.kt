package com.app.arco.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * [AppNavigator] に流れてきたコマンドをバックスタックへ適用する。
 *
 * バックスタックを書き換えるのはここだけにする。書き換え口が散ると、
 * 「今どうしてこの画面にいるのか」を追えなくなる。
 */
@Composable
fun NavigatorEffect(
    navigator: AppNavigator,
    backStack: NavBackStack<NavKey>,
) {
    LaunchedEffect(navigator, backStack) {
        navigator.commands.collect { command ->
            when (command) {
                is NavCommand.Push -> backStack.add(command.key)

                // 最後の1枚は残す。空にすると NavDisplay が描くものを失う
                NavCommand.Pop -> if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)

                is NavCommand.MoveToTop -> {
                    backStack.remove(command.key)
                    backStack.add(command.key)
                }
            }
        }
    }
}
