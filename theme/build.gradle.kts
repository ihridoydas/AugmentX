plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    
    jvm()
    
    iosArm64()
    iosSimulatorArm64()
    
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.multiplatform.runtime)
                implementation(libs.compose.multiplatform.foundation)
                implementation(libs.compose.multiplatform.material3)
                implementation(libs.compose.multiplatform.ui)
                implementation(libs.compose.multiplatform.components.resources)
                implementation(libs.compose.multiplatform.ui.tooling.preview)
                
                implementation(libs.kotlin.coroutines)
                api(libs.androidx.lifecycle.viewmodel)
                api(libs.androidx.lifecycle.runtimeCompose)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.appcompat)
                api(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
                
                // XR Support
                implementation(libs.androidx.xr.compose)
                implementation(libs.androidx.xr.compose.material3)
                implementation(libs.androidx.xr.scenecore)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

android {
    namespace = "template.theme"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

compose.resources {
    packageOfResClass = "template.theme.generated.resources"
}

// Fix for build cancellation/hang during resource publication for iOS simulator
tasks.matching { it.name.contains("iosSimulatorArm64ZipMultiplatformResourcesForPublication") }.configureEach {
    enabled = false
}
