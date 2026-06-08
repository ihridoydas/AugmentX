package template.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun SceneView(
    modifier: Modifier = Modifier,
    modelUrl: String? = null,
    modelUrls: List<String> = emptyList(),
    isAR: Boolean = false,
    autoRotate: Boolean = false,
    skyboxUrl: String? = null,
    onModelLoaded: () -> Unit = {}
)
