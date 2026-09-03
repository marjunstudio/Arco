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
| [docs/setup.md](docs/setup.md) | 環境構築の詳細。TEAM_ID、scheme の共有、Xcode / Android Studio の2経路 |
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
| Xcode | 26 以降。Android Studio から iOS を建てる場合も必須 |
| 実機 | **必須** |

GPS・方位・歩数・触覚が体験の中心にあるため、**エミュレータ／シミュレータでは体験の検証ができない**。
UI のレイアウト確認以外は実機で行う。→ [docs/setup.md](docs/setup.md#実機が必須な理由)

---

## 起動

```bash
git clone https://github.com/marjunstudio/Arco.git
cd Arco
```

**Android** — Android Studio でルートを開いて `androidApp` を実行。CLI からなら:

```bash
./gradlew :androidApp:installDebug
```

**iOS** — 初回は `TEAM_ID` の設定と scheme の共有が必要。→ [docs/setup.md](docs/setup.md#共通の下準備)
済んでいれば `iosApp/iosApp.xcodeproj` を Xcode で開いて実機ビルド、または Android Studio の `iosApp` 実行構成から。

---

## よく使うコマンド

```bash
./gradlew :androidApp:installDebug           # Android 実機にインストール
./gradlew :androidApp:assembleDebug          # Android のビルドのみ
./gradlew :shared:allTests                   # 共通ロジックのテスト（全ターゲット集約）
./gradlew :shared:testAndroidHostTest        # JVM 側のテストのみ
./gradlew :shared:iosSimulatorArm64Test      # iOS シミュレータ側のテストのみ
```

---

## 構成

| | |
|---|--|
| `shared/` | 画面・ドメイン・センサー抽象（`commonMain` / `androidMain` / `iosMain`） |
| `androidApp/` | Android の入口 |
| `iosApp/` | iOS の入口。Swift はタブバー周辺のみ |

モジュール分割は未確定。詳細は [AGENTS.md](AGENTS.md#モジュール分割は未確定)。
