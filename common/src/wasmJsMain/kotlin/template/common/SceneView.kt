package template.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?
) {
    if (modelUrl == null) return

    val viewId = remember { "online-model-viewer-${modelUrl.hashCode()}" }
    val density = LocalDensity.current

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
            val size = coordinates.size
            val container = document.getElementById(viewId) as? HTMLElement
            if (container != null) {
                container.style.left = "${position.x / density.density}px"
                container.style.top = "${position.y / density.density}px"
                container.style.width = "${size.width / density.density}px"
                container.style.height = "${size.height / density.density}px"
                container.style.opacity = "1"
                println("SceneView Web: Positioned $viewId")
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Text("3D Engine Initializing...", color = Color.Gray)

        DisposableEffect(modelUrl) {
            val container = document.createElement("div") as HTMLElement
            container.id = viewId
            // On top of Compose for debugging
            container.setAttribute("style", "position:fixed; z-index: 20; pointer-events: auto; opacity: 0; background: #222; width: 100px; height: 100px;")
            
            val modelViewer = document.createElement("model-viewer")
            modelViewer.setAttribute("src", modelUrl)
            modelViewer.setAttribute("auto-rotate", "")
            modelViewer.setAttribute("camera-controls", "")
            modelViewer.setAttribute("style", "width:100%; height:100%;")
            
            container.appendChild(modelViewer)
            document.body?.appendChild(container)
            
            onDispose {
                document.body?.removeChild(container)
            }
        }
    }
}
