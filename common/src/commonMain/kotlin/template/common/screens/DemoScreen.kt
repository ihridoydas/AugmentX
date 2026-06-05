package template.common.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import template.common.components.AppBar
import template.common.generated.resources.*
import template.common.screens.demos.ModelViewerDemo
import template.common.screens.demos.GeometryDemo
import template.common.screens.demos.AnimationDemo
import template.common.screens.demos.SceneGalleryDemo
import template.common.screens.demos.LightingDemo
import template.common.screens.demos.MovableLightDemo
import template.common.screens.demos.FogDemo
import template.common.screens.demos.EnvironmentDemo

@Composable
fun DemoScreen(id: String, onBackPress: () -> Unit) {
    when (id) {
        "model-viewer" -> ModelViewerDemo(onBackPress)
        "geometry" -> GeometryDemo(onBackPress)
        "animation" -> AnimationDemo(onBackPress)
        "scene-gallery" -> SceneGalleryDemo(onBackPress)
        // Lighting & Environment
        "lighting" -> LightingDemo(onBackPress)
        "movable-light" -> MovableLightDemo(onBackPress)
        "fog" -> FogDemo(onBackPress)
        "environment" -> EnvironmentDemo(onBackPress)
        // Interaction
        "camera-controls" -> CameraControlsDemo(onBackPress)
        // Content
        "text" -> TextDemo(onBackPress)
        "lines-paths" -> LinesPathsDemo(onBackPress)
        "image" -> ImageDemo(onBackPress)
        "billboard" -> BillboardDemo(onBackPress)
        "video" -> VideoDemo(onBackPress)
        // Interaction
        "gesture-editing" -> GestureEditingDemo(onBackPress)
        "collision" -> CollisionDemo(onBackPress)
        "view-node" -> ViewNodeDemo(onBackPress)
        // Advanced
        "dynamic-sky" -> DynamicSkyDemo(onBackPress)
        "multi-model" -> MultiModelDemo(onBackPress)
        "materials" -> MaterialsDemo(onBackPress)
        "physics" -> PhysicsDemo(onBackPress)
        "double-pendulum" -> DoublePendulumDemo(onBackPress)
        "post-processing" -> PostProcessingDemo(onBackPress)
        "custom-mesh" -> CustomMeshDemo(onBackPress)
        "shape" -> ShapeDemo(onBackPress)
        "reflection-probes" -> ReflectionProbesDemo(onBackPress)
        "secondary-camera" -> SecondaryCameraDemo(onBackPress)
        "debug-overlay" -> DebugOverlayDemo(onBackPress)
        // Augmented Reality
        "ar-placement" -> ARPlacementDemo(onBackPress)
        "ar-image" -> ARImageDemo(onBackPress)
        "ar-video" -> ARVideoDemo(onBackPress)
        "ar-face" -> ARFaceDemo(onBackPress)
        "ar-cloud-anchor" -> ARCloudAnchorDemo(onBackPress)
        "ar-streetscape" -> ARStreetscapeDemo(onBackPress)
        "ar-pose" -> ARPoseDemo(onBackPress)
        "ar-rerun" -> ARRerunDemo(onBackPress)
        "ar-record-playback" -> ARRecordPlaybackDemo(onBackPress)
        "ar-depth-occlusion" -> ARDepthOcclusionDemo(onBackPress)
        "ar-instant-placement" -> ARInstantPlacementDemo(onBackPress)
        "ar-terrain" -> ARTerrainAnchorDemo(onBackPress)
        "ar-rooftop" -> ARRooftopAnchorDemo(onBackPress)
        "ar-image-stabilization" -> ARImageStabilizationDemo(onBackPress)
        "ar-orbital" -> OrbitalARDemo(onBackPress)
        // Fallback
        else -> PlaceholderDemo(id = id, onBack = onBackPress)
    }
}

@Composable
fun PlaceholderDemo(id: String, onBack: () -> Unit) {
    Scaffold(
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

// Temporary placeholders for demos to avoid compilation errors
@Composable fun CameraControlsDemo(onBack: () -> Unit) = PlaceholderDemo("camera-controls", onBack)
@Composable fun TextDemo(onBack: () -> Unit) = PlaceholderDemo("text", onBack)
@Composable fun LinesPathsDemo(onBack: () -> Unit) = PlaceholderDemo("lines-paths", onBack)
@Composable fun ImageDemo(onBack: () -> Unit) = PlaceholderDemo("image", onBack)
@Composable fun BillboardDemo(onBack: () -> Unit) = PlaceholderDemo("billboard", onBack)
@Composable fun VideoDemo(onBack: () -> Unit) = PlaceholderDemo("video", onBack)
@Composable fun GestureEditingDemo(onBack: () -> Unit) = PlaceholderDemo("gesture-editing", onBack)
@Composable fun CollisionDemo(onBack: () -> Unit) = PlaceholderDemo("collision", onBack)
@Composable fun ViewNodeDemo(onBack: () -> Unit) = PlaceholderDemo("view-node", onBack)
@Composable fun DynamicSkyDemo(onBack: () -> Unit) = PlaceholderDemo("dynamic-sky", onBack)
@Composable fun MultiModelDemo(onBack: () -> Unit) = PlaceholderDemo("multi-model", onBack)
@Composable fun MaterialsDemo(onBack: () -> Unit) = PlaceholderDemo("materials", onBack)
@Composable fun PhysicsDemo(onBack: () -> Unit) = PlaceholderDemo("physics", onBack)
@Composable fun DoublePendulumDemo(onBack: () -> Unit) = PlaceholderDemo("double-pendulum", onBack)
@Composable fun PostProcessingDemo(onBack: () -> Unit) = PlaceholderDemo("post-processing", onBack)
@Composable fun CustomMeshDemo(onBack: () -> Unit) = PlaceholderDemo("custom-mesh", onBack)
@Composable fun ShapeDemo(onBack: () -> Unit) = PlaceholderDemo("shape", onBack)
@Composable fun ReflectionProbesDemo(onBack: () -> Unit) = PlaceholderDemo("reflection-probes", onBack)
@Composable fun SecondaryCameraDemo(onBack: () -> Unit) = PlaceholderDemo("secondary-camera", onBack)
@Composable fun DebugOverlayDemo(onBack: () -> Unit) = PlaceholderDemo("debug-overlay", onBack)
@Composable fun ARPlacementDemo(onBack: () -> Unit) = PlaceholderDemo("ar-placement", onBack)
@Composable fun ARImageDemo(onBack: () -> Unit) = PlaceholderDemo("ar-image", onBack)
@Composable fun ARVideoDemo(onBack: () -> Unit) = PlaceholderDemo("ar-video", onBack)
@Composable fun ARFaceDemo(onBack: () -> Unit) = PlaceholderDemo("ar-face", onBack)
@Composable fun ARCloudAnchorDemo(onBack: () -> Unit) = PlaceholderDemo("ar-cloud-anchor", onBack)
@Composable fun ARStreetscapeDemo(onBack: () -> Unit) = PlaceholderDemo("ar-streetscape", onBack)
@Composable fun ARPoseDemo(onBack: () -> Unit) = PlaceholderDemo("ar-pose", onBack)
@Composable fun ARRerunDemo(onBack: () -> Unit) = PlaceholderDemo("ar-rerun", onBack)
@Composable fun ARRecordPlaybackDemo(onBack: () -> Unit) = PlaceholderDemo("ar-record-playback", onBack)
@Composable fun ARDepthOcclusionDemo(onBack: () -> Unit) = PlaceholderDemo("ar-depth-occlusion", onBack)
@Composable fun ARInstantPlacementDemo(onBack: () -> Unit) = PlaceholderDemo("ar-instant-placement", onBack)
@Composable fun ARTerrainAnchorDemo(onBack: () -> Unit) = PlaceholderDemo("ar-terrain", onBack)
@Composable fun ARRooftopAnchorDemo(onBack: () -> Unit) = PlaceholderDemo("ar-rooftop", onBack)
@Composable fun ARImageStabilizationDemo(onBack: () -> Unit) = PlaceholderDemo("ar-image-stabilization", onBack)
@Composable fun OrbitalARDemo(onBack: () -> Unit) = PlaceholderDemo("ar-orbital", onBack)
