# Arco

歩きたい距離だけを指定すると、その範囲のスポットが1つ抽選される探索ナビ。地図もルート案内も出さず、
手がかりは目的地の方角と残り距離のみ。到着するまで行き先の正体は開示しない。

Kotlin Multiplatform + Compose Multiplatform で Android / iOS を1コードベースで作る。パッケージは `com.app.arco`。
GPS・方位・歩数・触覚が体験の中心にあるため、**エミュレータ／シミュレータでは体験の検証ができない**。

> この文書・`README.md`・`docs/` の記述と実際のコードが食い違っているのを見つけたら、その場で黙って直さない。
> ドキュメント側が正しい（実装が方針違反）ことがあるため、**実装に合わせてドキュメントを書き換えるのを既定にしない**。
> どちらを直すかを判定し、確認をとってから直す。手順は `.claude/skills/doc-drift/SKILL.md`。

> **機能の実装に入る前に [docs/architecture.md](docs/architecture.md) と [docs/conventions.md](docs/conventions.md) を読む。**
> 層の分け方・状態の置き場所・命名がそこで決まっている。モジュールを触るなら [docs/modules.md](docs/modules.md) も。
> **ただしこの3枚は「決定」であって「現状」ではない。** 書かれているモジュールも層も、まだ1つも存在しない。

## この文書の守備範囲

**ここに書くのは、常に効いていないと壊れるものだけ**——制約・禁止事項・地雷・完了条件。
層の責務、モジュール一覧、命名、分岐の実装といった詳細は `docs/` が原本で、各節の末尾からリンクする。
**同じことを2箇所に書かない。** どこに何を書くかの判断は `.claude/skills/doc-drift/SKILL.md`。
ディレクトリ構成・前提環境・セットアップとドキュメントの一覧は [README.md](README.md)。

## 使用技術

- Kotlin Multiplatform / Compose Multiplatform
- Material 3 Expressive（Android のボトムタブ）
- SwiftUI（iOS のボトムタブのみ）
- 主役の UI（距離ダイヤル・レーダー・到着カード）は Canvas による自作描画
- Koin（DI）— Hilt が KMP に対応していないため。**まだ導入していない**（`gradle/libs.versions.toml` に無い）

**バージョン番号はドキュメントに書かない。`gradle/libs.versions.toml` を読む。**
二重管理になって必ず片方が腐る。

### 動かせない制約

| | 値 | 理由 |
|---|--|--|
| targetSdk / compileSdk | 36 固定 | 2026年8月31日以降、Google Play の必須要件 |
| minSdk | 29 | Android 10 まで対応する |
| `IPHONEOS_DEPLOYMENT_TARGET` | 18.0 | iOS 18 まで対応する。`iosApp/iosApp.xcodeproj/project.pbxproj` の Debug / Release の2箇所 |
| ビルド SDK | 常に最新（iOS 26 SDK / Xcode 26 以降） | 下げると `.glassEffect` がそもそも見えなくなる |
| iOS ターゲット | `iosArm64` と `iosSimulatorArm64` の2つのみ | `iosX64`（Intel シミュレータ）は Compose Multiplatform 1.11 で削除済み |
| jvmTarget | 11 | `androidApp` / `shared` の両方 |

iOS の下限を下げることは技術的には可能だが、**確認すべき OS が増えるだけなので広げない**。

## 設計方針

### アーキテクチャは公式ガイド準拠の MVVM

UI 層 / Domain 層 / Data 層の3層。**依存は上から下の一方向のみ。** Now in Android と同じ形を KMP に移す。

Domain 層は公式ガイドでは optional だが、**このアプリでは省略しない**。抽選・距離計算・到着判定は
実機なしで自動テストできる唯一の部分で、センサーから引き剥がしておく価値がこのプロジェクトでは特に大きい。
**UseCase に `Flow` の購読や権限の分岐を持ち込まない。** 持ち込んだ時点でこの価値は消える。

→ 層の責務・UiState の形・エラーの扱い・DI: [docs/architecture.md](docs/architecture.md#層と依存方向)
→ 命名とパッケージ: [docs/conventions.md](docs/conventions.md)

### 状態は Composable の外に置く

タブごとに Compose の root が独立するため、**`remember` に持たせても ViewModel に持たせても、
タブを離れた時点で消える**。探索中の距離や進捗が失われると致命的。

探索セッションの所有者は **data 層のシングルトン `SessionRepository`**。ViewModel はそれを購読して
UiState に変換するだけで、状態の所有者にはならない。Composable は描画のみ。
好みの問題ではない。この構成では守らないと壊れる。

→ 寿命ごとの置き場所とプロセス死からの復帰: [docs/architecture.md](docs/architecture.md#状態をどこに置くか)

### UI の共有範囲

- **画面の中身は Compose Multiplatform で共通化する**
- **ボトムタブだけが OS 別** — Android は Material 3 Expressive、iOS は SwiftUI
- Swift を書くのは iOS のタブバー周辺のみ。それ以外は Kotlin で完結させる

```
commonMain   expect fun AppBottomBar(...)
androidMain  actual → ExpressiveBottomBar（純粋な Compose）
iosMain      actual → IosBottomBar（Swift 側の AppTabBar を呼ぶ）
```

**`expect` / `actual` の名前にデザイン言語固有の語（`Glass` `Expressive` `LiquidGlass`）を入れない。**
中で分岐する以上、片方の名前を付けると実体とズレる。その名前が出ていいのは分岐の内側の型だけ。

**共通 UI は Material に寄せすぎない。** 共通の Composable は iOS でもそのまま表示される。素の `Button` や
`TopAppBar` を多用すると iOS で違和感が出るので、`:core:designsystem` の自前トークンで組んだ薄い
コンポーネントに寄せる。Canvas 描画の部分は OS を問わず同じ見た目で問題ない。

→ Composable の書き方: [docs/conventions.md](docs/conventions.md#composable)

### センサー層は expect / actual

位置・方角・歩数・触覚はプラットフォーム実装が必要。共通側は interface とデータ型だけを持ち、実装を注入する。

- センサーは **data 層の DataSource** として扱う。**ViewModel から直接プラットフォーム API を触らない**
- 歩行中と停止中で方位ソースを切り替える判断は**ドメインのルール**。DataSource ではなく Domain 層に置く

→ [docs/architecture.md](docs/architecture.md#センサーの扱い)

### OS バージョンの分岐は2つだけに絞る

下限を上げて回避するのではなく、**上位 OS でだけ有効化する**方針で通す。分岐条件は
**iOS 26（`#available`）と Android S（`SDK_INT`）の2つだけ**に限る。増やすと確認の組み合わせが爆発する。

→ 分岐条件・下位 OS でのふるまい・実装例: [docs/platform-branching.md](docs/platform-branching.md)

### モジュール構成

**9モジュールに割ると決めてある。ただし分割はまだ実施していない**（現状は `settings.gradle.kts` を読む）。
分割するまでは既存のモジュールに書いて構わないが、**パッケージだけは最終形で切っておく**。
後で物理的に移すときに楽になる。

→ 切り方の意図・一覧・依存グラフ・iOS への `export` の作法・追加手順: [docs/modules.md](docs/modules.md#構成)

## やらないこと

- **`UIDesignRequiresCompatibility` を Info.plist に入れない** — `YES` で置くと iOS 26 でも旧デザインで描画され、アプリ全体で Liquid Glass が丸ごと消える。「iOS 26 なのに glass が出ない」を踏んだら最初にここを見る
- **タブバー周辺の余白を数値でハードコードしない** — iOS 26 と 18 でタブバーの高さも透過の有無も違う。safe area insets から取る
- **`#available` を各画面に散らさない** — 分岐は `AppTabBar` の入口1箇所に集める。散ると iOS 18 側の見た目を誰も把握できなくなる
- **`androidTarget {}` を使わない** — Kotlin 2.3 以降で非推奨。AGP 9 系の `android {}` を使う（既に移行済み）
- **依存ライブラリを勝手に追加しない** — 追加は相談してから
- **依存ライブラリのバージョン番号をドキュメントに書かない** — `gradle/libs.versions.toml` が唯一の真実。JDK・Android Studio・Xcode などの前提環境は README に書いてよい。「いつ削除・非推奨になったか」の記録（`iosX64` は Compose Multiplatform 1.11 で削除、など）は過去の事実で腐らないので書いてよい

## 踏みやすい地雷

1. **iOS 向け framework の `export()` 忘れ** — マルチモジュール化した際、`binaries.framework { export(project(...)) }` を書き、依存を `implementation` ではなく `api` にしないと Swift 側から型が見えない（→ [docs/modules.md](docs/modules.md)）
2. **edge-to-edge** — targetSdk 36 では強制される。加えて iOS 26 のタブバーは半透明で浮くためコンテンツが下に潜る。**safe area insets の対応は最初に片付ける**。後回しにすると全画面で修正することになる
3. **バックグラウンド位置情報** — ポケットに入れて歩く前提なので必須。iOS は常時許可と Background Modes、Android は `ACCESS_BACKGROUND_LOCATION` が要る。審査でも説明を求められるので早めに通す
4. **コンパスの磁気ノイズ** — 都市部では方位が大きくブレる。歩行中は GPS の進行方位、停止中は磁気コンパスに切り替える必要が出る見込み
5. **Xcode と Android Studio でビルド主体が違う** — Android Studio から実行すると `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES` が立ち、framework は Gradle ではなく IDE 側がビルドする。「片方では通るのにもう片方で通らない」を踏んだらここを疑う。片方で Clean してももう一方には効かない

## 動作確認

- **エミュレータ／シミュレータでは歩数センサーが存在せず、方位は固定か無応答、触覚は再現されない。**レイアウト確認以外は実機で行う
- iOS は **26 と 18 の両方を見る**。片方だけでは崩れに気付けない（ランタイムの取得は [README.md](README.md#ios-18-のシミュレータランタイム)）
- テストが通っていない状態で「完了」と報告しない

実装が進んだかどうかは、以下が**実機で**成立するかで判断する。

- [ ] ダイヤルを回すと距離が変わり、触覚が返る
- [ ] 歩くと残り距離が減る
- [ ] 電車や車で移動しても残り距離が減らない
- [ ] 端末を回すと矢印が目的地の方角を指し続ける
- [ ] 目的地に到達すると正体が開示される
- [ ] iOS 26 でタブバーに Liquid Glass がかかる
- [ ] iOS 18 でもタブバーが崩れず、コンテンツが下に潜らない
- [ ] Android 11 以下でも振動が返る（API 31 未満は `Vibrator` にフォールバックしている）
