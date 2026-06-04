package template.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?
) {
    if (modelUrl == null) return

    val viewId = remember { "online-model-viewer-${modelUrl.hashCode()}" }

    // Inject online model-viewer script
    SideEffect {
        if (document.getElementById("model-viewer-script") == null) {
            val script = document.createElement("script")
            script.id = "model-viewer-script"
            script.setAttribute("type", "module")
            script.setAttribute("src", "https://ajax.googleapis.com/ajax/libs/model-viewer/4.0.0/model-viewer.min.js")
            document.head?.appendChild(script)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Simple loading indicator for web
        CircularProgressIndicator(color = Color.Gray)

        DisposableEffect(modelUrl) {
            val container = document.createElement("div")
            container.id = viewId
            container.setAttribute("style", "width:100%; height:100%; position:absolute; top:0; left:0; background:black;")
            
            val modelViewer = document.createElement("model-viewer")
            modelViewer.setAttribute("src", modelUrl)
            modelViewer.setAttribute("auto-rotate", "true")
            modelViewer.setAttribute("camera-controls", "true")
            modelViewer.setAttribute("ar", "true") // Support for Online AR
            modelViewer.setAttribute("ar-modes", "webxr scene-viewer quick-look")
            modelViewer.setAttribute("shadow-intensity", "1")
            modelViewer.setAttribute("style", "width:100%; height:100%;")
            
            container.appendChild(modelViewer)
            document.body?.appendChild(container)
            
            onDispose {
                document.body?.removeChild(container)
            }
        }
    }
}
