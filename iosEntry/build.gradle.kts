import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.FailOnSeverity
import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

// iOS シェルから見える唯一のモジュール。Swift Export のルートはここで、:shared は
// implementation で抱える。:shared を直接ルートにすると @Composable fun ArcoApp() が
// 公開 API として Swift に出てしまう（Swift Export は関数型から @Composable を落とす）。
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    // Composable を含まないが、Compose Gradle プラグインは依存グラフ全体の Compose リソースを
    // Swift Export のバイナリを宣言したモジュールの Xcode 入口に同期させるため、ここに要る。
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
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

// TODO: モジュール分割時に build-logic の convention plugin へ移す（:shared / :androidApp と重複）
detekt {
    buildUponDefaultConfig = true
    parallel = true
    failOnSeverity = FailOnSeverity.Warning
}

// KMP には src/main/kotlin が無いため、既定の :iosEntry:detekt は NO-SOURCE になる。
// sourceSet ごとのタスクを check に繋いで iosMain を検査対象にする
tasks.named("check") {
    dependsOn(tasks.withType<Detekt>().matching { it.name.endsWith("SourceSet") })
}

ktlint {
    version = libs.versions.ktlint
}
