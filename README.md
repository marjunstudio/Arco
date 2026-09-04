# アルコ / Arco

距離だけ決めて歩き出す、マイクロ探索ナビ。

歩きたい距離を指定すると、その範囲にあるスポットが1つだけ抽選される。地図もルート案内も出さず、
手がかりは目的地の方角と残り距離だけ。着くまで行き先の正体は分からない。

「散歩したいけど、どこに行くか決められない」「一歩が踏み出せない」人が対象。

Kotlin Multiplatform + Compose Multiplatform（Android / iOS）。

---

## ドキュメント

| | 中身 |
|---|--|
| [AGENTS.md](AGENTS.md) | **使用技術・設計方針・実装の決めごと・禁止事項。実装に入る前にここを読む** |
| [docs/architecture.md](docs/architecture.md) | MVVM の層と依存方向、UiState の形、状態をどこに置くか |
| [docs/modules.md](docs/modules.md) | モジュール構成と依存グラフ、iOS への Swift Export の作法 |
| [docs/conventions.md](docs/conventions.md) | パッケージ・命名・Composable の書き方・ドメイン用語 |
| [docs/platform-branching.md](docs/platform-branching.md) | iOS 26/18・API 31 の分岐実装 |
| `gradle/libs.versions.toml` | 依存バージョンの唯一の真実。ドキュメントに数字は書かない |

`CLAUDE.md` は先頭で `@AGENTS.md` を import しているだけ（Claude Code の公式な参照方法）。
エージェント向けの指示は **`AGENTS.md` 側に書く**。`CLAUDE.md` に置くのは Claude Code 固有の指示のみ。

---

## 前提環境

| | |
|---|--|
| JDK | 21（無ければ Gradle が自動取得する） |
| Android Studio | Quail 4 2026.1.4 |
| Xcode | 26 以降。iOS を建てるなら必須 |
| Kotlin Multiplatform プラグイン | Android Studio から iOS を建てるなら必須 |

GPS・方位・歩数・触覚が体験の中心にある。**歩数センサーはエミュレータに存在せず、方位は固定か無応答、
触覚は再現されない。** レイアウトの確認以外は実機で行う。

---

## セットアップ

### Android

Android Studio でルートを開き、Gradle Sync を通して `androidApp` を実行する。CLI からなら:

```bash
./gradlew :androidApp:installDebug
```

### iOS（シミュレータ）

1. **Kotlin Multiplatform プラグインを入れる**
   Settings → Plugins → Marketplace で「Kotlin Multiplatform」（JetBrains）を導入し、IDE を再起動する。
   plugin id が `com.jetbrains.kmm` なのは旧称の名残で、これが現行のもの。
   IDE ビルドに固定されているので、Android Studio を上げたらプラグインも上げる。

2. **Preflight checks を通す**
   Project Environment Preflight Checks が Xcode の場所・ライセンス同意・command line tools・
   シミュレータ検出を検査する。指摘が消えるまで先に進まない。引っかかったら大抵これで済む:

   ```bash
   sudo xcodebuild -license accept
   sudo xcode-select -s /Applications/Xcode.app
   ```

3. **実行構成から流す**
   Gradle Sync 後に `iosApp` 実行構成が現れる。シミュレータを選んで Run。
   Xcode から建てるなら `iosApp/iosApp.xcodeproj` を開く。

Kotlin を変更したら iOS 側のバイナリは自動で再生成される。

### iOS 18 のシミュレータランタイム

iOS 26 と 18 は両方見ないと崩れに気付けない。Xcode 26 には 26 系しか同梱されていない。

```bash
xcrun simctl list runtimes                            # 入っているものを確認
xcodebuild -downloadPlatform iOS -buildVersion 18.5   # 不足分を取得
```

---

## 構成

| | |
|---|--|
| `shared/` | 画面・ドメイン・センサー抽象（`commonMain` / `androidMain` / `iosMain`） |
| `androidApp/` | Android の入口 |
| `iosEntry/` | iOS へ渡す Kotlin の入口。Swift Export のルート |
| `iosApp/` | iOS の入口。Swift はタブバー周辺のみ |

マルチモジュール構成（core をレイヤで切り、feature は本流を1つに）は決定済みだが、
**分割はまだ実施していない**。現在のモジュールは `:shared` と `:androidApp` の2つだけ。
決めた構成は [docs/modules.md](docs/modules.md)。
