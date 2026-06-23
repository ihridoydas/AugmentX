package template.common.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import template.common.components.AppBar
import template.common.generated.resources.*
import template.common.screens.demos.*

import template.common.ui.LanguageDropdown
import template.common.ui.ThemeToggleButton
import template.navigation.Navigator
import template.navigation.ScreenDestinations

@Composable
fun DemoScreen(id: String, navigator: Navigator) {
    val onBack = { 
        if (id.startsWith("ar-")) {
            template.common.util.PlatformUtils.hardReset()
        }
        navigator.goBack() 
    }
    when (id) {
        "model-viewer" -> ModelViewerDemo(onBack)
        "geometry" -> GeometryDemo(onBack)
        "animation" -> AnimationDemo(onBack)
        "scene-gallery" -> SceneGalleryDemo(onBack)
        // Lighting & Environment
        "lighting" -> LightingDemo(onBack)
        "movable-light" -> MovableLightDemo(onBack)
        "fog" -> FogDemo(onBack)
        "environment" -> EnvironmentDemo(onBack)
        // Interaction
        "camera-controls" -> CameraControlsDemo(onBack)
        // Content
        "text" -> TextDemo(onBack)
        "lines-paths" -> LinesPathsDemo(onBack)
        "image" -> ImageDemo(onBack)
        "billboard" -> BillboardDemo(onBack)
        "video" -> VideoDemo(onBack)
        // Interaction
        "gesture-editing" -> GestureEditingDemo(onBack)
        "collision" -> CollisionDemo(onBack)
        "view-node" -> ViewNodeDemo(onBack)
        // Advanced
        "dynamic-sky" -> DynamicSkyDemo(onBack)
        "multi-model" -> MultiModelDemo(onBack)
        "materials" -> MaterialsDemo(onBack)
        "physics" -> PhysicsDemo(onBack)
        "double-pendulum" -> DoublePendulumDemo(onBack)
        "post-processing" -> PostProcessingDemo(onBack)
        "custom-mesh" -> CustomMeshDemo(onBack)
        "shape" -> ShapeDemo(onBack)
        "reflection-probes" -> ReflectionProbesDemo(onBack)
        "secondary-camera" -> SecondaryCameraDemo(onBack)
        "debug-overlay" -> DebugOverlayDemo(onBack)
        // Augmented Reality
        "ar-placement" -> ARPlacementDemo(onBack)
        "ar-image" -> ARImageDemo(onBack)
        "ar-video" -> ARVideoDemo(onBack)
        "ar-face" -> ARFaceDemo(onBack)
        "ar-cloud-anchor" -> ARCloudAnchorDemo(onBack)
        "ar-streetscape" -> ARStreetscapeDemo(onBack)
        "ar-pose" -> ARPoseDemo(onBack)
        "ar-rerun" -> ARRerunDemo(onBack)
        "ar-record-playback" -> ARRecordPlaybackDemo(onBack)
        "ar-depth-occlusion" -> ARDepthOcclusionDemo(onBack)
        "ar-instant-placement" -> ARInstantPlacementDemo(onBack)
        "ar-terrain" -> ARTerrainAnchorDemo(onBack)
        "ar-rooftop" -> ARRooftopAnchorDemo(onBack)
        "ar-image-stabilization" -> ARImageStabilizationDemo(onBack)
        "ar-orbital" -> OrbitalARDemo(onBack)
        // Fallback
        else -> PlaceholderDemo(id = id, onBack = onBack)
    }
}

@Composable
fun PlaceholderDemo(id: String, onBack: () -> Unit) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                title = stringResource(Res.string.demo_title, id),
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.demo_coming_soon, id),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
