pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        
        exclusiveContent {
            forRepository {
                maven("https://jogamp.org/deployment/maven")
            }
            filter { 
                includeGroup("org.jogamp.jogl")
                includeGroup("org.jogamp.gluegen")
            }
        }
        
        maven("https://jitpack.io")
        
        // For Binaryen (Kotlin/Wasm)
        exclusiveContent {
            forRepository {
                ivy("https://github.com/WebAssembly/binaryen/releases/download") {
                    name = "Binaryen Distributions"
                    patternLayout {
                        artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.github.webassembly") }
        }

        // For Node.js (Kotlin/Wasm/JS)
        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist") {
                    name = "Node.js Distributions"
                    patternLayout {
                        artifact("v[revision]/[artifact]-v[revision]-[classifier].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("org.nodejs") }
        }

        // For Yarn (Kotlin/Wasm/JS)
        exclusiveContent {
            forRepository {
                ivy("https://github.com/yarnpkg/yarn/releases/download") {
                    name = "Yarn Distributions"
                    patternLayout {
                        artifact("v[revision]/[artifact]-v[revision].[ext]")
                    }
                    metadataSources { artifact() }
                }
            }
            filter { includeGroup("com.yarnpkg") }
        }
    }
}
rootProject.name = "AugmentX"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":theme")
include(":navigation")
include(":storage")
include(":common")
include(":backend")
