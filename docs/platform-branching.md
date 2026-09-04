# OS バージョン分岐の実装

分岐条件は2つだけに絞る（方針は [../AGENTS.md](../AGENTS.md) の「OS バージョンの分岐は2つだけに絞る」）。

| | 分岐条件 | 下位 OS でのふるまい |
|---|--|--|
| iOS | `if #available(iOS 26.0, *)` | `.ultraThinMaterial` による従来のぼかし |
| Android | `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` | 非推奨の `Vibrator` を使う／ダイナミックカラー無効 |

## iOS

`.glassEffect` は iOS 26 の API なので、deployment target 18.0 では**呼び出し箇所がコンパイルエラーになる**。
取りこぼしは起きない。エラーが出たところに `#available` を足していけばいい。

### 見た目の差だけなら ViewModifier に閉じ込める

```swift
private struct GlassBackground: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 26.0, *) {
            content.glassEffect(.regular, in: .capsule)
        } else {
            content.background(.ultraThinMaterial, in: Capsule())
        }
    }
}

extension View {
    func glassBackground() -> some View { modifier(GlassBackground()) }
}
```

### 構造が変わるものは View を分ける

構造が変わるものは modifier では吸収できない。View を分けて入口で選び、iOS 26 専用 API を使う型には
`@available(iOS 26.0, *)` を付ける。**分岐は `RootViewController` の1箇所に集める。**
各画面に `#available` が散ると、iOS 18 側の見た目を誰も把握できなくなる。

### タブバーには分岐が要らない

`RootViewController` が Compose の上に重ねているのは素の `UITabBar`。**Liquid Glass は OS がかけるので、
`#available` は1つも要らない。** `UITabBarController` も要らない。

そのため**いま `iosApp/` に `#available` は1つも無い**。最初に必要になるのは、自前の View に
`.glassEffect` を使い始めたときで、そのときは上の ViewModifier の形に閉じ込める。

### スクロール連動の挙動は使えない

`.tabBarMinimizeBehavior(.onScrollDown)` やスクロールエッジエフェクトは、**構造をどう組んでも効かない**。
Compose は `UIScrollView` を作らずに自前でスクロールを描くため、UIKit 側からはスクロールが観測できない。
「重ね方を変えれば効くのでは」と試す前にここを読むこと。

### 余白

iOS 26 と 18 ではタブバーの高さも透過の有無も違う。**余白を数値でハードコードすると必ず片方でズレる**ので
実測する。`RootViewController.viewDidLayoutSubviews()` でバーが隠している高さを測り、Compose の
view controller の `additionalSafeAreaInsets.bottom` に入れる。Compose 側はそれを通常の safe area
として受け取るので、画面ごとの対応は要らない。

```swift
let hiddenHeight = max(0, view.bounds.maxY - tabBar.frame.minY - view.safeAreaInsets.bottom)
composeViewController.additionalSafeAreaInsets.bottom = hiddenHeight
```

`view.safeAreaInsets.bottom` を引くのは、Compose 側が safe area を別に持っているため。
引かないとホームインジケータの分を二重に確保する。

## Android

振動の取得口だけが API 31 で変わる。

```kotlin
val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
} else {
    @Suppress("DEPRECATION")
    getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
}
```

- `VibrationEffect.createPredefined` は API 29 から使えるので、振動の**表現**そのものは分岐しなくていい。取得口だけが違う
- ダイナミックカラーは API 31 以降。30 以下では固定のカラースキームに落とす
- **Material 3 Expressive はライブラリ側の実装なので OS バージョンに依存しない。** minSdk 29 でもそのまま出る
