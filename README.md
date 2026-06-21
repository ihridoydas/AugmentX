# AugmentX

**AugmentX** is a powerful **Kotlin Multiplatform (KMP)** Augmented Reality framework and template. It provides a robust starting point for building cross-platform AR applications for Android and iOS, complete with a dedicated Ktor backend for asset management.

## Key Features

### 🚀 Cross-Platform AR Engine
- **Unified SceneView**: A powerful `@Composable` AR view that works seamlessly across Android and iOS.
- **Multiple AR Modes**: Support for Plane Detection, Image Tracking, Face Tracking, Depth, and Instant Placement.
- **Compose Multiplatform UI**: Shared UI logic for AR overlays and controls.

### 📦 Content & Rendering
- **3D Model Support**: Render complex GLB/GLTF models with ease.
- **AR Video Playback**: Integrated MP4 video support within AR scenes.
- **Advanced Environment FX**: Dynamic control over Fog density, Skybox/IBL, and Exposure.
- **Billboard Components**: UI and 3D elements that intelligently face the user.
- **Interactive Gestures**: Built-in support for manipulating AR objects.

### 🖥️ Dedicated Backend (`:backend`)
- **Ktor-Powered**: High-performance backend built with Ktor and Netty.
- **AR Target Registry**: Management system for tracking images and their associated 3D/video content.
- **Asset Processing**: Endpoints for compiling and serving AR assets and `.mind` tracking files.
- **Persistence**: File-based registry system for easy deployment and testing.

### 🏗️ Robust Architecture
- **Clean Project Structure**: Modularized into `:app`, `:backend`, `:common`, `:navigation`, `:storage`, and `:theme`.
- **Full Testing Suite**: Logic and UI tests run cross-platform. See [Testing Strategy](documentation/Testing.md).
- **Modern Tooling**: Configured with Koin, Ktor, Room KMP, and Compose Multiplatform.
- **CI/CD Ready**: GitHub Actions and Danger integration for automated quality checks.

## Getting Started

1. **Backend Setup**:
   - Navigate to the `backend` directory.
   - Run the Ktor server: `./gradlew :backend:run`.
   - The server defaults to `http://localhost:8888`.

2. **App Configuration**:
   - Open `buildscripts/setup.gradle` to configure your project details.
   - Run the setup command: `./gradlew renameTemplate`.

3. **Build & Run**:
   - Launch the `:app` on Android or use the shared `:common` module for iOS.

## What's Included

Explore shared logic, components, and documentation:

- [Essential KMP Tasks](/documentation/EssentialTasks.md) - Platform-specific commands.
- [Demos](/common/src/commonMain/kotlin/template/common/screens/demos/) - Pre-built examples for Fog, Gestures, Billboards, and more.
- [Static Analysis](/documentation/StaticAnalysis.md) - Ktlint, Detekt, and Dokka configuration.
- [Git Hooks](/documentation/GitHooks.md) - Pre-commit checks.

## Project Structure

- `:app`: Android-specific application module.
- `:backend`: Ktor server for managing AR content and registries.
- `:common`: Shared AR engine (`SceneView`), UI components, and business logic.
- `:navigation`: Shared navigation configuration.
- `:storage`: Shared local data handling (DataStore/Room).
- `:theme`: Shared Material 3 design system.

## CI/CD & Quality

Uses [Danger](https://danger.systems) for PR checks. See [Dangerfile](Dangerfile). Ensure you set up a `DANGER_GITHUB_API_TOKEN` in GitHub Secrets.

## Templates

Includes [Pull Request Template](/.github/pull_request_template.md) for organized PR descriptions.
