import dev.detekt.gradle.extensions.FailOnSeverity
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.app.arco"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.app.arco"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    lint {
        // AGP 9.0.1 時点で :shared（com.android.kotlin.multiplatform.library）は lint タスクも
        // main variant の lint model も持たず、checkDependencies を立てても androidMain は解析されない
        // （API 31 のクラス参照を置いて NewApi が出ないことを実測済み）。AGP が対応したら効くよう残す。
        checkDependencies = true
        warningsAsErrors = true
        abortOnError = true
        sarifReport = true
        disable +=
            setOf(
                // 「もっと新しい版がある」系。コードを変えていなくても時間の経過だけで
                // ビルドが赤くなるため外す。更新の追跡は libs.versions.toml を直接見る
                "NewerVersionAvailable",
                "GradleDependency",
                "AndroidGradlePluginVersion",
                // targetSdk 36 は AGENTS.md の「動かせない制約」で固定している
                "OldTargetApi",
            )
        informational +=
            setOf(
                // adaptive icon のテーマアイコン用レイヤー。テンプレートのランチャーアイコンを
                // 差し替えるときに一緒に用意する
                "MonochromeLauncherIcon",
            )
    }
}

// TODO: モジュール分割時に build-logic の convention plugin へ移す（:shared と重複）
detekt {
    buildUponDefaultConfig = true
    parallel = true
    // 既定は Error のみ。ルールの大半は Warning なので、それも失敗扱いにする
    failOnSeverity = FailOnSeverity.Warning
}

ktlint {
    version = libs.versions.ktlint
    filter {
        // Compose Resources の生成コードもソースセットに入るため除外する。
        // Spec ラムダはビルドスクリプトを掴んで configuration cache に載らないので、Ant パターンで書く。
        exclude("**/generated/resources/**")
    }
}
