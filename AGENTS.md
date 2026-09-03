# Arco

歩きたい距離だけを指定すると、その範囲のスポットが1つ抽選される探索ナビ。地図もルート案内も出さず、
手がかりは目的地の方角と残り距離のみ。到着するまで行き先の正体は開示しない。

Kotlin Multiplatform + Compose Multiplatform で Android / iOS を1コードベースで作る。
GPS・方位・歩数・触覚が体験の中心にあるため、**エミュレータ／シミュレータでは体験の検証ができない**。

> この文書・`README.md`・`docs/` の記述と実際のコードが食い違っているのを見つけたら、その場で黙って直さない。
> ドキュメント側が正しい（実装が方針違反）ことがあるため、**実装に合わせてドキュメントを書き換えるのを既定にしない**。
> どちらを直すかを判定し、確認をとってから直す。手順は `.claude/skills/doc-drift/SKILL.md`。

## コマンド

```bash
./gradlew :androidApp:installDebug           # Android 実機にインストール
./gradlew :androidApp:assembleDebug          # Android のビルドのみ
./gradlew :shared:allTests                   # 共通ロジックのテスト（全ターゲット集約）
./gradlew :shared:testAndroidHostTest        # JVM 側のテストのみ
./gradlew :shared:iosSimulatorArm64Test      # iOS シミュレータ側のテストのみ
```

- iOS の実機ビルドは Xcode か Android Studio の実行構成から行う。手順は [docs/setup.md](docs/setup.md)
- Kotlin を変更したら shared framework は自動で再生成される。`:shared:embedAndSignAppleFrameworkForXcode` を手で叩く必要は通常ない

## ディレクトリ

| | |
|---|--|
| `shared/` | 画面・ドメイン・センサー抽象。`commonMain` / `androidMain` / `iosMain` |
| `androidApp/` | Android の入口。`MainActivity` のみ |
| `iosApp/` | iOS の入口。Swift で書くのはタブバー周辺のみ |
| `gradle/libs.versions.toml` | 依存バージョンの唯一の真実 |

パッケージは `com.app.arco`。

## 使用技術

- Kotlin Multiplatform / Compose Multiplatform
- Material 3 Expressive（Android のボトムタブ）
- SwiftUI（iOS のボトムタブのみ）
- 主役の UI（距離ダイヤル・レーダー・到着カード）は Canvas による自作描画

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

### UI の共有範囲

- **画面の中身は Compose Multiplatform で共通化する**
- **ボトムタブだけが OS 別** — Android は Material 3 Expressive、iOS は SwiftUI
- iOS 側はさらに OS バージョンで二段に分かれる。Liquid Glass は iOS 26 以降でのみ有効化し、18 では従来表現に落とす

```
commonMain   expect fun AppBottomBar(...)
androidMain  actual → ExpressiveBottomBar（純粋な Compose）
iosMain      actual → IosBottomBar（Swift 側の AppTabBar を呼ぶ）
                        ├ iOS 26+  Liquid Glass
                        └ iOS 18   従来表現
```

**`expect` / `actual` の名前にデザイン言語固有の語（`Glass` `Expressive` `LiquidGlass`）を入れない。**
中で分岐する以上、片方の名前を付けると実体とズレる。その名前が出ていいのは分岐の内側の型だけ。

Swift を書くのは iOS のタブバー周辺のみ。それ以外は Kotlin で完結させる。

### 状態は Composable の外に置く

タブを切り替えると **Compose の root がタブごとに独立する**ため、`remember` に持たせた状態はタブ移動で消える。
探索中の距離や進捗が失われると致命的。

- 状態はシングルトンの StateHolder が持つ
- Composable は描画のみ

好みの問題ではない。この構成では守らないと壊れる。

### 共通 UI は Material に寄せすぎない

共通の Composable は iOS でもそのまま表示される。素の `Button` や `TopAppBar` を多用すると iOS で違和感が出るので、
自前のトークンで組んだ薄いコンポーネントに寄せる。Canvas 描画の部分は OS を問わず同じ見た目で問題ない。

### センサー層は expect / actual

位置・方角・歩数・触覚はプラットフォーム実装が必要。共通側は interface とデータ型だけを持ち、実装を注入する。

### OS バージョンの分岐は2つだけに絞る

下限を上げて回避するのではなく、**上位 OS でだけ有効化する**方針で通す。分岐条件は以下の2つに限る。
増やすと確認の組み合わせが爆発する。

| | 分岐条件 | 下位 OS でのふるまい |
|---|--|--|
| iOS | `if #available(iOS 26.0, *)` | `.ultraThinMaterial` による従来のぼかし |
| Android | `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` | 非推奨の `Vibrator` を使う／ダイナミックカラー無効 |

実装例は [docs/platform-branching.md](docs/platform-branching.md)

### モジュール分割は未確定

現時点では KMP ウィザードが生成した構成のまま。feature 単位で切るか layer 単位で切るかは、実装を少し進めてから決める。
それまでは既存のモジュールに書いて構わないが、**後で移動しやすいようにパッケージだけは責務ごとに分けておく**。

## やらないこと

- **`UIDesignRequiresCompatibility` を Info.plist に入れない** — `YES` で置くと iOS 26 でも旧デザインで描画され、アプリ全体で Liquid Glass が丸ごと消える。「iOS 26 なのに glass が出ない」を踏んだら最初にここを見る
- **タブバー周辺の余白を数値でハードコードしない** — iOS 26 と 18 でタブバーの高さも透過の有無も違う。safe area insets から取る
- **`#available` を各画面に散らさない** — 分岐は `AppTabBar` の入口1箇所に集める。散ると iOS 18 側の見た目を誰も把握できなくなる
- **`androidTarget {}` を使わない** — Kotlin 2.3 以降で非推奨。AGP 9 系の `android {}` を使う（既に移行済み）
- **依存ライブラリを勝手に追加しない** — 追加は相談してから
- **バージョン番号をドキュメントに書かない**

## 踏みやすい地雷

1. **iOS 向け framework の `export()` 忘れ** — マルチモジュール化した際、`binaries.framework { export(project(...)) }` を書き、依存を `implementation` ではなく `api` にしないと Swift 側から型が見えない
2. **edge-to-edge** — targetSdk 36 では強制される。加えて iOS 26 のタブバーは半透明で浮くためコンテンツが下に潜る。**safe area insets の対応は最初に片付ける**。後回しにすると全画面で修正することになる
3. **バックグラウンド位置情報** — ポケットに入れて歩く前提なので必須。iOS は常時許可と Background Modes、Android は `ACCESS_BACKGROUND_LOCATION` が要る。審査でも説明を求められるので早めに通す
4. **コンパスの磁気ノイズ** — 都市部では方位が大きくブレる。歩行中は GPS の進行方位、停止中は磁気コンパスに切り替える必要が出る見込み
5. **Xcode と Android Studio でビルド主体が違う** — Android Studio から実行すると `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES` が立ち、framework は Gradle ではなく IDE 側がビルドする。「片方では通るのにもう片方で通らない」を踏んだらここを疑う（→ [docs/setup.md](docs/setup.md)）

## 動作確認

- **エミュレータ／シミュレータでは歩数センサーが存在せず、方位は固定か無応答、触覚は再現されない。**レイアウト確認以外は実機で行う
- iOS は **26 と 18 の両方を見る**。片方だけでは崩れに気付けない（ランタイムの取得は [docs/setup.md](docs/setup.md)）
- テストが通っていない状態で「完了」と報告しない

実装が進んだかどうかは、以下が**実機で**成立するかで判断する。

- [ ] ダイヤルを回すと距離が変わり、触覚が返る
- [ ] 歩くと残り距離が減る
- [ ] 電車や車で移動しても残り距離が減らない
- [ ] 端末を回すと矢印が目的地の方角を指し続ける
- [ ] 目的地に到達すると正体が開示される
- [ ] iOS 26 でタブバーに Liquid Glass がかかる
- [ ] iOS 18 でもタブバーが崩れず、コンテンツが下に潜らない
- [ ] Android 11 以下でも振動が返る（`VibratorManager` の分岐が効いている）
