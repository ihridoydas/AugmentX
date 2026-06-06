package template.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun SceneView(
    modifier: Modifier = Modifier,
    modelUrl: String? = null
)
