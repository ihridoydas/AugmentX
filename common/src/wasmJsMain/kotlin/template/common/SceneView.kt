package template.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
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
            val position = coordinates.positionInWindow()
            val size = coordinates.size
            val container = document.getElementById(viewId) as? HTMLElement
            if (container != null) {
                container.style.left = "${position.x / density.density}px"
                container.style.top = "${position.y / density.density}px"
                container.style.width = "${size.width / density.density}px"
                container.style.height = "${size.height / density.density}px"
                container.style.display = "block"
            }
        }
    ) {
        DisposableEffect(modelUrl) {
            val container = document.createElement("div") as HTMLElement
            container.id = viewId
            // Temporary debug: On top of everything with a lime border
            container.setAttribute("style", "position:fixed; z-index: 100; pointer-events: auto; display: none; border: 2px solid lime; background: #111;")
            
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
