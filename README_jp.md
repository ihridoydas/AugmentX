# AugmentX

**AugmentX** は、強力な **Kotlin Multiplatform (KMP)** 拡張現実 (AR) フレームワークおよびテンプレートです。Android および iOS 向けのクロスプラットフォーム AR アプリケーションを構築するための堅牢な出発点を提供し、アセット管理用の専用 Ktor バックエンドも備えています。

## 主な機能

### 🚀 クロスプラットフォーム AR エンジン
- **Unified SceneView**: Android と iOS でシームレスに動作する強力な `@Composable` AR ビュー。
- **複数の AR モード**: 平面検知、画像トラッキング、フェイストラッキング、デプス、およびインスタント配置をサポート。
- **Compose Multiplatform UI**: AR オーバーレイおよびコントロール用の共有 UI ロジック。

### 📦 コンテンツとレンダリング
- **3D モデルのサポート**: 複雑な GLB/GLTF モデルを簡単にレンダリング。
- **AR ビデオ再生**: AR シーン内での統合された MP4 ビデオサポート。
- **高度な環境 FX**: フォグの密度、スカイボックス/IBL、および露出を動的に制御。
- **ビルボードコンポーネント**: ユーザーの方向をインテリジェントにむく UI および 3D 要素。
- **インタラクティブなジェスチャー**: AR オブジェクトを操作するための組み込みサポート。

### 🖥️ 専用バックエンド (`:backend`)
- **Ktor 駆動**: Ktor と Netty で構築された高性能バックエンド。
- **AR ターゲットレジストリ**: トラッキング画像とそれに関連付けられた 3D/ビデオコンテンツの管理システム。
- **アセット処理**: AR アセットおよび `.mind` トラッキングファイルを提供するためのエンドポイント。
- **永続化**: 展開とテストが容易なファイルベースのレジストリシステム。

### 🏗️ 堅牢なアーキテクチャ
- **クリーンなプロジェクト構造**: `:app`、`:backend`、`:common`、`:navigation`、`:storage`、`:theme` にモジュール化。
- **フルテストスイート**: クロスプラットフォームで動作するロジックおよび UI テスト。[テスト戦略](documentation/Testing.md)を参照。
- **モダンなツール**: Koin、Ktor、Room KMP、および Compose Multiplatform で構成。
- **CI/CD 対応**: 自動品質チェックのための GitHub Actions と Danger の統合。

## スタートガイド

1. **バックエンドのセットアップ**:
   - `backend` ディレクトリに移動します。
   - Ktor サーバーを実行します: `./gradlew :backend:run`
   - サーバーはデフォルトで `http://localhost:8888` で動作します。

2. **アプリの設定**:
   - `buildscripts/setup.gradle` を開き、プロジェクトの詳細を設定します。
   - セットアップコマンドを実行します: `./gradlew renameTemplate`

3. **ビルドと実行**:
   - Android で `:app` を起動するか、iOS 用に共有の `:common` モジュールを使用します。

## 含まれるもの

共有ロジック、コンポーネント、およびドキュメントを確認してください：

- [Essential KMP Tasks](/documentation/EssentialTasks.md) - プラットフォーム固有のコマンド。
- [Demos](/common/src/commonMain/kotlin/template/common/screens/demos/) - フォグ、ジェスチャー、ビルボードなどのビルド済みサンプル。
- [Static Analysis](/documentation/StaticAnalysis.md) - Ktlint, Detekt, および Dokka の設定。
- [Git Hooks](/documentation/GitHooks.md) - プリコミットチェック。

## プロジェクト構造

- `:app`: Android 固有のアプリケーションモジュール。
- `:backend`: AR コンテンツとレジストリを管理するための Ktor サーバー。
- `:common`: 共有 AR エンジン (`SceneView`)、UI コンポーネント、およびビジネスロジック。
- `:navigation`: 共有ナビゲーション設定。
- `:storage`: 共有ローカルデータ処理 (DataStore/Room)。
- `:theme`: 共有 Material 3 デザインシステム。
