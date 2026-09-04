# アーキテクチャ

> **ステータス: 決定済み・未着手**
> 現在のコードは KMP ウィザードのテンプレートのまま（`Greeting` / `Platform` / `App.kt`）で、
> ここに書かれた層はまだ1つも存在しない。**以下は実装に着手する際に従う設計であり、現在のコードの説明ではない。**
> 現状を知りたいときは `settings.gradle.kts` と `shared/src/` を読む。

Android 公式のアプリアーキテクチャガイドに則った MVVM。UI 層・Domain 層・Data 層の3層で、
Now in Android と同じ形を KMP に移す。モジュールの切り方は [modules.md](modules.md)、命名は [conventions.md](conventions.md)。

---

## 層と依存方向

```
UI 層          Composable ──> ViewModel
  │                              │
  ▼                              ▼
Domain 層                    UseCase
  │                              │
  ▼                              ▼
Data 層        Repository ──> DataSource ──> センサー / 永続化 / ネットワーク
```

- **依存は上から下の一方向のみ。** Data 層が Domain 層を知ることはないし、Domain 層が Composable を知ることもない
- 下の層は上の層の存在を知らない。Data 層は「誰がこの Flow を購読しているか」を前提にしない

| 層 | 置くもの | 置かないもの |
|---|--|--|
| UI | Composable、ViewModel、UiState | ビジネスルール、センサー API の直接呼び出し |
| Domain | UseCase。**副作用のない純粋関数に寄せる** | Compose、プラットフォーム API |
| Data | Repository、DataSource、`expect`/`actual` の実体 | UiState、画面の都合 |

### Domain 層を省略しない理由

公式ガイドでは Domain 層は optional とされている。Arco では**入れる**。

このアプリの検証コストは異常に高い。歩数も方位も触覚もエミュレータでは動かず、
歩行を伴う確認は屋外に出るしかない（[../AGENTS.md](../AGENTS.md#動作確認)）。
その中で **抽選・距離計算・到着判定だけは、実機なしで自動テストできる**。

この部分をセンサーや Compose から引き剥がして純粋関数として隔離しておくことに、
このプロジェクト固有の価値がある。逆に言えば、**Domain 層に `Flow` の購読や権限の分岐を持ち込んだ時点で
その価値は消える**。UseCase は引数を受けて値を返すだけにする。

---

## 単方向データフロー（UDF）

```
状態は下る    ViewModel が公開する UiState を Composable が描画する
イベントは上る Composable は ViewModel のメソッドを呼ぶだけ。自分で状態を書き換えない
```

- Composable から状態を書き戻さない。`onDistanceChange(meters)` のようにイベントとして渡す
- ViewModel は UI の型（`Dp`、`Color`、`Painter`）を持たない。表示への変換は Composable 側

## UiState の形

```kotlin
data class ExploreUiState(
    val distanceMeters: Int,
    val phase: Phase,
) {
    sealed interface Phase {
        data object Idle : Phase
        data object Drawing : Phase
        data class Navigating(val bearing: Float, val remainingMeters: Int) : Phase
        data class Arrived(val spot: Spot) : Phase
        data class Failed(val reason: FailureReason) : Phase
    }
}
```

- **immutable な data class**。`var` も `MutableList` も持たせない
- **排他的な状態は `sealed interface` で表す。** `isLoading: Boolean` と `error: String?` を並べると、
  「ロード中かつエラー」という存在しない組み合わせが型として作れてしまう
- 1画面に1つの UiState。画面が増えたら UiState も増やす

---

## 状態をどこに置くか

**この節がこの文書でいちばん重要。** ここを外すと Arco では体験が壊れる。

### 前提：タブを切り替えると Compose の root が独立する

ボトムタブは OS 別で、iOS 側は SwiftUI の `TabView` が器になる（[platform-branching.md](platform-branching.md)）。
このため **タブごとに Compose の root が別々に立つ**。結果として:

```
タブごとに Compose root が独立する
  → その配下の ViewModel のスコープもタブごとに独立する
  → 「探索セッション」を ViewModel に持たせると、タブを離れた時点で消える
```

探索中に履歴タブを覗いて戻ったら目的地が消えていた、という壊れ方をする。
これは設計の好みではなく、この構成では避けられない事実として扱う。

### 決着のさせ方

**寿命の違うものを、違う場所に置く。**

| 状態 | 置き場所 | 寿命 |
|---|--|--|
| 画面固有の表示状態（ダイヤルの一時的な角度、カードの展開） | ViewModel の UiState | 画面と同じ |
| **探索セッション**（目的地・残り距離・進捗・到着したか） | **Data 層のシングルトン Repository が `StateFlow` で保持** | アプリと同じ |
| センサーの生値（位置・方位・歩数） | Data 層の DataSource が `Flow` で流す。誰も保持しない | 購読している間だけ |

ViewModel は Repository の `StateFlow` を購読して UiState に変換するだけ。**状態の所有者にはならない。**

```kotlin
class ExploreViewModel(
    sessionRepository: SessionRepository,
) : ViewModel() {
    val uiState: StateFlow<ExploreUiState> =
        sessionRepository.session
            .map { it.toUiState() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreUiState.initial)
}
```

こうすると ViewModel がタブ移動で破棄されても、セッションは Repository 側に残る。
戻ってきた ViewModel は同じ `StateFlow` を購読し直すだけで復帰する。

AGENTS.md の **「状態はシングルトンの StateHolder が持つ」の StateHolder とは、この Repository のこと**。
Composable の外に置くという要件は、この形で満たされる。「ViewModel を使うな」という意味ではない。

### 公式ガイドと矛盾しないこと

公式ガイドでも、**画面をまたいで共有される状態は Data 層が持つ**のが本筋で、
ViewModel は画面の状態ホルダーに徹する。Arco の事情から出発しても着地は同じところになる。

### プロセスが死んだときの復帰

シングルトンはプロセスが死ねば消える。歩行中にバックグラウンドで落とされる可能性がある以上、
**セッションは永続化から復元できる必要がある**。Repository の背後に永続化の DataSource を置く前提で設計する。

> 永続化の手段（DataStore / SQLDelight / Room KMP）は未選定。決めるまでは `SessionRepository` の
> interface だけを固定し、実装の差し替えで済む形にしておく。

---

## センサーの扱い

位置・方位・歩数・触覚は **Data 層の DataSource** として扱う。

- 共通側は interface と `Flow` だけを持ち、実体は `expect`/`actual` で注入する（→ [modules.md](modules.md)）
- **ViewModel からプラットフォーム API を直接触らない。** `CLLocationManager` も `SensorManager` も
  Data 層より上には出てこない
- 権限の確認と要求も Data 層の責務。UseCase に権限の分岐を持ち込まない

### 方位の切り替えはドメインの判断

都市部では磁気コンパスが大きくブレるため、歩行中は GPS の進行方位、停止中は磁気コンパスに切り替える必要が出る見込み
（[../AGENTS.md](../AGENTS.md#踏みやすい地雷)）。

**この切り替えは「どちらのセンサーを信じるか」というルールなので、Domain 層に置く。**
DataSource は両方の値をそのまま流し、判断は上でやる。DataSource 側で賢く切り替えると、
挙動がプラットフォーム実装ごとに分岐してテストできなくなる。

---

## エラーとローディング

- **Domain 層は例外を投げず、`Result` で返す。** 失敗は戻り値として扱う
- UI 層で `Result` を UiState に落とす。`Failed(reason)` のように、UI が分岐できる型に変換する
- 「圏外」「権限がない」「範囲内にスポットがない」はアプリとして想定内の状態であって、例外ではない

---

## iOS 側の位置づけ

- **ViewModel も UiState も Kotlin 側にある。** Swift 側に状態を持たない
- Swift を書くのはタブバー周辺のみ。タブバーは「どのタブが選ばれているか」以上の状態を持たない
- Swift から Kotlin の型を見るには framework への `export` が要る（→ [modules.md](modules.md)）

---

## DI（Koin）

> **未導入。** `gradle/libs.versions.toml` に Koin はまだ入っていない。以下は導入時の方針。

Hilt は KMP に対応していないため、公式サンプルの構成をそのまま持ってこられない。Koin を使う。

- **モジュールごとに Koin module を定義し、`:shared` で束ねる。** 依存の定義をそのモジュールの中に閉じる
- ViewModel は `koinViewModel()` で取る
- iOS 側の初期化エントリ（`initKoin()`）は `:shared` に置き、`iOSApp.swift` から呼ぶ
- **セッションを持つ Repository は `single` で登録する。** ここを `factory` にすると、
  ViewModel ごとに別インスタンスが生まれてこの文書の前提が崩れる
- `Dispatchers.IO` を直接書かず、ディスパッチャも注入する（→ [conventions.md](conventions.md)）

Koin は実行時解決なので、**依存の登録漏れはコンパイルでは捕まらず起動時に落ちる**。
新しい依存を足したら、必ず一度アプリを起動して確認する。
