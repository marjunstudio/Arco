package com.app.arco.core.common

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * 画面遷移の指示。
 *
 * バックスタックを直接触らせず、コマンドとして流す。こうしておくと Composable の外
 * （iOS のネイティブシェルなど）からも遷移を起こせる。
 */
sealed interface NavCommand {
    data class Push(
        val key: NavKey,
    ) : NavCommand

    data object Pop : NavCommand

    /**
     * すでにバックスタックにあるキーを末尾へ持ち上げる。無ければ積む。
     * ルートのタブ切り替えはこれで表す。
     */
    data class MoveToTop(
        val key: NavKey,
    ) : NavCommand
}

/**
 * 遷移の指示を出す口。適用するのは [NavigatorEffect]。
 *
 * アプリ全体で1つ。Koin に `single` で登録してある（`com.app.arco.di.appModule`）。
 */
class AppNavigator {
    private val commandChannel = Channel<NavCommand>(Channel.BUFFERED)

    val commands: Flow<NavCommand> = commandChannel.receiveAsFlow()

    fun goTo(key: NavKey) {
        commandChannel.trySend(NavCommand.Push(key))
    }

    fun back() {
        commandChannel.trySend(NavCommand.Pop)
    }

    fun moveToTop(key: NavKey) {
        commandChannel.trySend(NavCommand.MoveToTop(key))
    }
}
