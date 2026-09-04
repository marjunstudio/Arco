import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.FailOnSeverity
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    // framework は吐かない。iOS 側のバイナリは Swift Export で :iosEntry が生成する。
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.app.arco.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.navigation3.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

// TODO: モジュール分割時に build-logic の convention plugin へ移す（:androidApp と重複）
detekt {
    buildUponDefaultConfig = true
    parallel = true
    failOnSeverity = FailOnSeverity.Warning
}

// KMP には src/main/kotlin が無いため、既定の :shared:detekt は NO-SOURCE になる。
// sourceSet ごとのタスクを check に繋いで commonMain / androidMain / iosMain を検査対象にする
tasks.named("check") {
    dependsOn(tasks.withType<Detekt>().matching { it.name.endsWith("SourceSet") })
}

tasks.withType<Detekt>().configureEach {
    // Compose Resources の生成コードもソースセットに入るため除外する。
    // パターンは各 srcDir からの相対パスに対して照合されるため、"**/build/**" では当たらない
    exclude("**/generated/resources/**")
}

ktlint {
    version = libs.versions.ktlint
    filter {
        // Compose Resources の生成コードもソースセットに入るため除外する。
        // Spec ラムダはビルドスクリプトを掴んで configuration cache に載らないので、Ant パターンで書く。
        exclude("**/generated/resources/**")
    }
}
