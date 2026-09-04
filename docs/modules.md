# モジュール構成

> **ステータス: 決定済み・未着手**
> **現在のモジュールは `:androidApp` `:shared` `:iosEntry` の3つだけ**（`settings.gradle.kts`）。
> `core` / `feature` の分割はまだ実施しておらず、中身は `:shared` のパッケージとして存在する。
> 以下は分割に着手する際に従う構成であり、現在の説明ではない。
> 「`:core:model` にあるはず」と思って探さないこと。

アーキテクチャの層は [architecture.md](architecture.md)、命名は [conventions.md](conventions.md)。

---

## 構成

`core` をレイヤで切り、`feature` は体験の本流を1つにまとめる。core / feature で計9モジュール、
これに各プラットフォームの入口が付く。

| モジュール | 責務 | 依存 |
|---|--|--|
| `:shared` | **umbrella。** Koin を束ね、`ArcoApp()` の入口を持つ | 全モジュール |
| `:iosEntry` | **iOS の入口。** Swift Export のルート。`ArcoAppHost` と `initArco()` だけを公開する | `:shared` |
| `:core:model` | `Spot` / `Session` / `Bearing` などのドメインモデル | なし |
| `:core:common` | `Result`、ディスパッチャ、共通拡張 | `:core:model` |
| `:core:domain` | UseCase（抽選・距離計算・到着判定・方位ソースの選択） | `:core:model` `:core:common` `:core:data` |
| `:core:data` | Repository の interface と実装、DataSource、永続化 | `:core:model` `:core:common` `:core:sensor` |
| `:core:sensor` | 位置・方位・歩数・触覚の `expect`/`actual` | `:core:model` |
| `:core:designsystem` | デザイントークンと Canvas コンポーネント | Compose のみ |
| `:feature:explore` | ダイヤル → レーダー → 到着（本流） | `:core:domain` `:core:model` `:core:designsystem` |
| `:feature:history` | 履歴タブ | 同上 |

### 置いてはいけないもの

| モジュール | 入れない |
|---|--|
| `:core:model` | **Compose もコルーチンも入れない。** 純粋 Kotlin に保つ。ここが汚れると全モジュールが引きずられる |
| `:core:domain` | `Flow` の購読、権限の分岐、プラットフォーム API |
| `:core:designsystem` | ドメインの型。`Spot` を受け取るコンポーネントは feature 側に置く |
| `:shared` | **ロジック。** 束ねるだけ。ここに書き始めると分割した意味がなくなる |
| `:feature:*` | 他の feature への依存 |

---

## 依存グラフ

```
         :androidApp                    iosApp (Swift)
              │                              │
              │                          :iosEntry       ← Swift Export のルート
              └───────────────┬──────────────┘
                              ▼
                          :shared                        ← umbrella
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
:feature:explore      :feature:history      :core:designsystem
        │                     │
        └──────────┬──────────┘
                   ▼
             :core:domain
                   │
                   ▼
             :core:data ──────────> :core:sensor
                   │                      │
                   ▼                      │
             :core:common                 │
                   │                      │
                   └──────────┬───────────┘
                              ▼
                        :core:model
```

上は主要な依存のみを描いた簡略図。feature は `:core:model` と
`:core:designsystem` にも直接依存する。**正確な依存先は上の表**を見る。

### 依存のルール

- **`:core:model` は何にも依存しない。** 依存を足したくなったら、それは model ではない
- **feature 同士は依存しない。** 共有が必要になったら `core` に落とす。feature 間で `import` した時点で設計が壊れている
- `:core:designsystem` はドメインを知らない。汎用の描画部品だけを置く
- 循環依存はビルドが通らないが、通る形の「実質的な循環」（`:shared` 経由で参照する等）も作らない

### Repository の interface を `:core:data` に置く理由

`:core:domain` が `:core:data` に依存する。クリーンアーキテクチャの純粋形なら
Repository の interface を domain 側に置いて依存を逆転させるところだが、**そうしない**。

Google 公式のアーキテクチャガイドと Now in Android がこの形（Domain → Data）を採っており、
このプロジェクトは「公式に則る」を優先すると決めたため。**判断であって、うっかりではない。**

これを覆したくなったら、覆す理由を先にここへ書く。理由の書かれていない制約は次に必ず破られる。

---

## iOS へは Swift Export で渡す

Swift 側から見えるのは **`:iosEntry` の public API だけ**。`:shared` は framework を吐かない。

```kotlin
// iosEntry/build.gradle.kts
kotlin {
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalSwiftExportDsl::class)
    swiftExport {
        moduleName.set("Shared")
        flattenPackage.set("com.app.arco.ios")   // このパッケージだけ修飾なしで Swift に出る
    }

    sourceSets {
        iosMain.dependencies {
            implementation(project(":shared"))    // api にしない
        }
    }
}
```

### `:shared` を直接ルートにしない理由

Swift Export は橋渡しする関数型から `@Composable` を落とす。`:shared` をルートにすると
`ArcoApp()` が「引数なしの普通の関数」として Swift の公開 API に出てしまい、Swift から
呼べてしまう。`:iosEntry` を1枚挟み、`ArcoAppHost` の内側に Composable を閉じ込める。

### 境界に置ける型

`implementation` で抱えた `:shared` の型は Swift から見えない。**モジュールを増やしても
`export()` に相当する操作は無く、見せたいものは `:iosEntry` に薄いラッパを足して露出させる。**

橋渡しが確認できている型:

| 種類 | Swift 側 |
|---|--|
| `String` / 数値 / `Boolean` | そのまま |
| `List<String>` | `[Swift.String]` |
| `StateFlow<String>` | `any KotlinTypedStateFlow<Swift.String>`（`asAsyncSequence()` で購読できる） |
| `UIViewController` | `UIKit.UIViewController` |

`flattenPackage` の下に置いた **top-level 関数はグローバル関数として出る**（`initArco()` で確認済み）。

Swift Export は Alpha なので、**橋渡しできるかどうかは生成物を読んで確かめる**。
`iosEntry/build/SwiftExport/<target>/<config>/files/Shared/Shared.swift` に実際の署名が出る。

### Xcode 側の設定

ビルドフェーズは `./gradlew :iosEntry:embedSwiftExportForXcode` を叩く。ターゲットの Build Settings に
以下が要る（**`-ObjC` を落とすと起動時に落ちる**。→ [../AGENTS.md](../AGENTS.md#踏みやすい地雷)）。

```
LIBRARY_SEARCH_PATHS = ("$(inherited)", "$(BUILT_PRODUCTS_DIR)")
OTHER_LDFLAGS        = ("$(inherited)", "-ObjC", "-lShared")
SWIFT_INCLUDE_PATHS  = ("$(inherited)", "$(BUILT_PRODUCTS_DIR)/**")
```

---

## Compose リソースはモジュールごとにパッケージが分かれる

現在の `arco.shared.generated.resources.Res` は `:shared` に紐づいた生成物。
モジュールを分けると生成先も分かれるため、**リソースを持つモジュールでは生成パッケージを明示する**。

```kotlin
// core/designsystem/build.gradle.kts
compose.resources {
    publicResClass = true                                        // 他モジュールから参照する場合
    packageOfResClass = "com.app.arco.core.designsystem.resources"
}
```

- 明示しないと、モジュール名から自動で決まった名前になる。後で変えると全 `import` が動く
- **既定は `internal`。** モジュールをまたいで使うリソースは `publicResClass = true` が要る
- 同じ画像を複数モジュールに置かない。共有するものは `:core:designsystem` に集約する

---

## 新規モジュールを追加する手順

1. ディレクトリを作る（`core/foo/` または `feature/foo/`）
2. `settings.gradle.kts` に `include(":core:foo")` を足す
3. `build.gradle.kts` を書く（convention plugin があればそれを適用する。→ 次節）
4. `android { namespace = "com.app.arco.core.foo" }` を設定する。**namespace の重複はビルドエラーになる**
5. パッケージを `com.app.arco.core.foo` で切る（→ [conventions.md](conventions.md)）
6. 依存を足す側の `build.gradle.kts` に `implementation(project(":core:foo"))` を書く
7. **Swift から見せる必要があるか判断する。** 必要なら `:iosEntry` にラッパを足す（`export()` は無い）
8. このファイルの表と依存グラフを更新する

Gradle Sync が通っただけでは iOS 側は検証できない。**Xcode でビルドが通るところまで見る。**

---

## convention plugin（`build-logic`）

> **未着手。モジュール分割と同時に入れる。**

9モジュール分の `build.gradle.kts` を手でコピペすると、`compileSdk` や `jvmTarget` が
必ずどこかで食い違う。**モジュールを増やす前に `build-logic` を用意する。**

- `build-logic/convention` に KMP ライブラリ用・Compose 用・feature 用のプラグインを定義する
- 各モジュールの `build.gradle.kts` は `plugins { id("arco.kmp.library") }` の数行で済ませる
- **バージョン番号は `gradle/libs.versions.toml` にのみ書く**という既存ルールは変わらない
  （[../AGENTS.md](../AGENTS.md#使用技術)）

後から入れると9モジュール全部を書き直すことになる。順序を逆にしない。

---

## まだ決まっていないこと

分割に着手するまで、以下は**推測で書かない**。

| | 決め方 |
|---|--|
| 分割後の Gradle タスク名 | `./gradlew :shared:tasks --all` と `./gradlew projects` で実際に引いてから、README.md と AGENTS.md のコマンド表を直す |
| テストの集約タスク | 同上。現在の `:shared:allTests` が何を拾うようになるかは、組んでから確認する |
| 永続化の手段 | DataStore / SQLDelight / Room KMP のいずれか未選定（→ [architecture.md](architecture.md#プロセスが死んだときの復帰)） |
| スポットのデータソース | 未選定。`:core:data` の中身はこれが決まらないと書けない |
| `:feature:explore` をさらに割るか | 画面が増えて本当に苦しくなってから。先に割らない |
