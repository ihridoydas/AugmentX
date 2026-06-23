package template.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class ARMode {
    Plane,
    Image,
    Face,
    Depth,
    Instant
}

@Composable
expect fun SceneView(
    modifier: Modifier = Modifier,
    modelUrl: String? = null,
    modelUrls: List<String> = emptyList(),
    videoUrl: String? = null,
    isAR: Boolean = false,
    arMode: ARMode = ARMode.Plane,
    trackingImage: String? = null,
    imageTargets: Map<String, String> = emptyMap(),
    autoRotate: Boolean = false,
    skyboxUrl: String? = null,
    exposure: Float = 1.0f,
    fogDensity: Float = 0.0f,
    animationSpeed: Float = 1.0f,
    textContent: String? = null,
    scale: Float = 1.0f,
    billboard: Boolean = false,
    onModelLoaded: () -> Unit = {}
)
