# コーディング規約

> **ステータス: 決定済み・一部未着手**
> テンプレート由来のファイルは削除済み。`shared/src/commonMain/kotlin/com/app/arco/` 配下は
> この規約に沿っている。ただし ViewModel と UiState の実例はまだ無いので、
> **その節はコードで確かめられない。**

層の考え方は [architecture.md](architecture.md)、モジュールの切り方は [modules.md](modules.md)。

---

## パッケージ

ルートは `com.app.arco`。モジュールのパスをそのままパッケージにする。

```
com.app.arco.core.model
com.app.arco.core.domain
com.app.arco.core.data
com.app.arco.core.sensor
com.app.arco.core.designsystem
com.app.arco.feature.explore          ← Composable と ViewModel
com.app.arco.feature.explore.component ← その画面専用の部品
```

- **モジュール名とパッケージ名を一致させる。** どのモジュールのコードか import 行だけで分かるようにする
- モジュールが未分割の間も、**この形でパッケージだけ先に切る**（AGENTS.md「モジュール分割」）。
  後で物理的に移すとき、パッケージ宣言を書き換えずに済む

---

## 命名

| 種類 | 形 | 例 |
|---|--|--|
| UseCase | 動詞 + 名詞 + `UseCase` | `DrawSpotUseCase` `CalculateBearingUseCase` |
| Repository（interface） | 名詞 + `Repository` | `SessionRepository` `SpotRepository` |
| Repository（実装） | `Default` + interface 名 | `DefaultSessionRepository` |
| DataSource | 名詞 + `DataSource` | `LocationDataSource` `StepCountDataSource` |
| ViewModel | 画面名 + `ViewModel` | `ExploreViewModel` |
| UiState | 画面名 + `UiState` | `ExploreUiState` |
| 画面（Stateful） | 画面名 + `Route` | `ExploreRoute` |
| 画面（Stateless） | 画面名 + `Screen` | `ExploreScreen` |
| Koin module | 画面/モジュール名 + `Module` | `exploreModule` `dataModule` |

- 実装クラスに `Impl` を付けない。`Default` を使う（Now in Android に合わせる）
- `Manager` `Helper` `Util` は使わない。何をするものか名前から分からなくなる
- **`expect`/`actual` の名前にデザイン言語固有の語を入れない。**
  対象の語と理由は [../AGENTS.md](../AGENTS.md#ui-の共有範囲)

### UseCase は `operator fun invoke` にする

```kotlin
class DrawSpotUseCase(
    private val spotRepository: SpotRepository,
) {
    suspend operator fun invoke(origin: Coordinate, radiusMeters: Int): Result<Spot> = ...
}

// 呼ぶ側
val spot = drawSpot(origin, radiusMeters)
```

1 UseCase につき公開するのは1つの関数だけ。増やしたくなったら UseCase を分ける。

---

## ファイルの置き方

- **1ファイル1公開型**を基本にする。`ExploreUiState.kt` に `ExploreUiState` を置く
- ただし `sealed interface` とその実装は同じファイルに置く（分けると全体像が読めない）
- 拡張関数は、拡張される型と同じファイルか、`XxxExtensions.kt` に集める

---

## Composable

### Stateful と Stateless を分ける

```kotlin
// Stateful — ViewModel と繋ぐだけ。ロジックを書かない
@Composable
fun ExploreRoute(viewModel: ExploreViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExploreScreen(
        uiState = uiState,
        onDistanceChange = viewModel::changeDistance,
        onStartClick = viewModel::start,
    )
}

// Stateless — 状態と callback だけを受ける。ViewModel を知らない
@Composable
fun ExploreScreen(
    uiState: ExploreUiState,
    onDistanceChange: (Int) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) { ... }
```

- **`@Preview` は Stateless 側に付ける。** Stateful に付けると ViewModel ごと必要になって動かない
- `Modifier` は末尾の必須引数の後、既定値 `Modifier` で受ける。Compose の慣習に合わせる
- callback は `onXxx` で、動詞を含める（`onStartClick`。`onClick` だけにしない）

### 共通 UI は Material に寄せすぎない

置き場所は `:core:designsystem`。規則そのものは [../AGENTS.md](../AGENTS.md#ui-の共有範囲)

### 余白を数値でハードコードしない

タブバー周辺は特に禁止。safe area insets から取る。→ [../AGENTS.md](../AGENTS.md#やらないこと)

---

## Flow の扱い

```kotlin
val uiState: StateFlow<ExploreUiState> =
    sessionRepository.session
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExploreUiState.initial,
        )
```

- ViewModel が公開するのは `StateFlow`。`Flow` のまま渡さない
- `WhileSubscribed(5_000)` を使う。画面回転や短時間の離脱で購読が切れて再取得が走るのを防ぐ
- UI 側は `collectAsStateWithLifecycle()` で受ける。`collectAsState()` は使わない
- **Data 層は「誰が購読しているか」を前提にしない。** 購読が無い間に値を捨てる／貯める判断は Data 層の中で完結させる

### センサーの Flow は必ず止まる形にする

位置も方位も歩数も、購読が切れたらリスナーを解除する。`callbackFlow` + `awaitClose` を使う。
**ポケットに入れて歩き続けるアプリなので、リスナーの解除漏れはそのままバッテリーに出る。**

---

## ディスパッチャ

```kotlin
class DefaultSpotRepository(
    private val ioDispatcher: CoroutineDispatcher,   // ← 注入する
) { ... }
```

- **`Dispatchers.IO` をクラスの中に直接書かない。** テストで差し替えられなくなる
- 注入の口は `:core:common` に置き、Koin で束ねる
- `Dispatchers.Main` に依存する処理を Domain / Data 層に書かない

---

## エラー

- Domain 層は例外を投げず `Result` で返す。→ [architecture.md](architecture.md#エラーとローディング)
- `catch (e: Exception)` で握り潰さない。握るなら何を握ったか型に残す
- 「圏外」「権限がない」「範囲内にスポットが無い」は想定内の状態。例外として扱わない

---

## ドメイン用語

**日本語と識別子の対応をここで固定する。** 同じものが `Place` と `Spot` と `Destination` で書かれると、
検索もリファクタも効かなくなる。

| 日本語 | 識別子 | 意味 |
|---|--|--|
| スポット | `Spot` | 抽選の結果選ばれた目的地。到着するまで正体を開示しない |
| 探索セッション | `Session` | 距離を決めてから到着するまでの1回分。アプリ全体で1つ |
| 指定距離 | `targetDistance` | ユーザーがダイヤルで決めた、歩きたい距離 |
| 残り距離 | `remainingDistance` | 現在地からスポットまでの距離 |
| 方角 | `Bearing` | スポットの方角。度で持つ |
| 抽選 | `draw` | 範囲内から1つ選ぶこと。`random` や `pick` を使わない |
| 到着 | `arrival` / `Arrived` | スポットに到達した状態 |
| 現在地 | `Coordinate` | 緯度経度。`Location` はプラットフォーム型と紛らわしいので使わない |

- 距離は **メートルの `Int`** で持つ。`Double` の km と混ざると必ず事故る。変換は表示の直前でやる
- 角度は **度（`Float`、0〜360）** で持つ。ラジアンは計算の内側だけ

> 仕様が固まっていない語（履歴、実績、通知など）はまだ書かない。決まってから足す。

---

## 書かないもの

- **バージョン番号。** `gradle/libs.versions.toml` が唯一の真実。→ [../AGENTS.md](../AGENTS.md#使用技術)
- **同じ内容を2箇所に。** 片方が必ず腐る。書く先の判断は `.claude/skills/doc-drift/SKILL.md`
- **`#available` を各画面に。** OS 分岐は入口1箇所に集める。→ [platform-branching.md](platform-branching.md)
- **意味を説明しないコメント。** 何をしているかはコードを読めば分かる。**なぜそうしたかを書く**
