plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    id(libs.plugins.dokka.get().pluginId)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    jvm()
    iosArm64 {
        binaries.framework {
            baseName = "common"
            isStatic = true
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "common"
            isStatic = true
        }
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.theme)
                api(projects.navigation)
                api(projects.storage)
                implementation(libs.compose.multiplatform.runtime)
                implementation(libs.compose.multiplatform.foundation)
                implementation(libs.compose.multiplatform.material3)
                implementation(libs.compose.multiplatform.ui)
                implementation(libs.compose.multiplatform.components.resources)
                implementation(libs.compose.multiplatform.ui.tooling.preview)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.kotlin.coroutines)
                
                implementation(libs.koin.core)
                implementation(libs.koin.viewmodel)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                api(libs.androidx.lifecycle.viewmodel)
                api(libs.androidx.lifecycle.runtimeCompose)

                api(libs.jetbrains.navigation3)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.client.logging)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlin.coroutines.test)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(libs.ui.test)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.appcompat)
                api(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                implementation(libs.ktor.client.android)
                implementation(libs.sceneview)
                implementation(libs.arsceneview)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.robolectric)
                implementation(libs.compose.ui.test.manifest)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
                implementation(libs.kotlin.coroutines.swing)
            }
        }
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosMain by creating {
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.webviewMultiplatform)
            }
        }
        val iosTest by creating
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

dependencies {
    "debugImplementation"(libs.compose.ui.test.manifest)
}

compose.desktop {
    application {
        mainClass = "template.common.MainKt"
        jvmArgs(
            "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED",
            "--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.nio=ALL-UNNAMED",
            "--add-opens", "java.desktop/sun.java2d=ALL-UNNAMED",
            "--add-opens", "java.desktop/apple.laf=ALL-UNNAMED",
            "--add-opens", "java.desktop/com.apple.laf=ALL-UNNAMED",
            "--add-opens", "java.desktop/com.apple.eawt=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.lwawt=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
            "-Dapple.awt.UIElement=false",
            "-Djava.awt.headless=false",
            "-Dapple.awt.fullWindowContent=true",
            "-Dapple.laf.useScreenMenuBar=true",
            "-Dskiko.renderApi=METAL"
        )
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
            )
            packageName = "template.common"
            packageVersion = "1.0.0"
        }
    }
}

android {
    namespace = "template.common"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("robolectric.enabledSdks", "34")
            }
        }
    }
}

compose.resources {
    packageOfResClass = "template.common.generated.resources"
}
