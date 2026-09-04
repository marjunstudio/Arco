import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

// iOS シェルから見える唯一のモジュール。Swift Export のルートはここで、:shared は
// implementation で抱える。:shared を直接ルートにすると @Composable fun App() が
// 公開 API として Swift に出てしまう（Swift Export は関数型から @Composable を落とす）。
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    // Composable を含まないが、Compose Gradle プラグインは依存グラフ全体の Compose リソースを
    // Swift Export のバイナリを宣言したモジュールの Xcode 入口に同期させるため、ここに要る。
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalSwiftExportDsl::class)
    swiftExport {
        moduleName.set("Shared")
        // com.app.arco.ios のパッケージ修飾を Swift 側で外す。他パッケージの型は
        // ExportedKotlinPackages 経由になるので、Swift に見せるものはこの下に置く。
        flattenPackage.set("com.app.arco.ios")
    }

    sourceSets {
        iosMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.ui)
        }
    }
}
