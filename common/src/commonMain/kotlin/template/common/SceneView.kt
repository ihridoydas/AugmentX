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
    autoRotate: Boolean = false,
    skyboxUrl: String? = null,
    onModelLoaded: () -> Unit = {}
)
