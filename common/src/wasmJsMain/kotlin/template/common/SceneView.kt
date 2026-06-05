package template.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.round
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?
) {
    if (modelUrl == null) return

    var isLoading by remember(modelUrl) { mutableStateOf(true) }
    var bounds by remember { mutableStateOf(IntRect.Zero) }
    
    val container = remember(modelUrl) {
        (document.createElement("div") as HTMLElement).apply {
            setAttribute("style", "position:fixed; z-index: 999; pointer-events: auto; display: block; opacity: 0;")
            innerHTML = "<model-viewer src=\"$modelUrl\" auto-rotate camera-controls style=\"width:100%; height:100%;\"></model-viewer>"
            
            val mv = firstElementChild as? HTMLElement
            mv?.addEventListener("load", { isLoading = false })
            mv?.addEventListener("error", { isLoading = false })
        }
    }

    // Force show after a few seconds
    LaunchedEffect(modelUrl) {
        kotlinx.coroutines.delay(5000)
        isLoading = false
    }

    // Synchronize DOM element with Compose state
    SideEffect {
        val d = window.devicePixelRatio
        if (bounds != IntRect.Zero) {
            container.style.left = "${bounds.left / d}px"
            container.style.top = "${bounds.top / d}px"
            container.style.width = "${bounds.width / d}px"
            container.style.height = "${bounds.height / d}px"
            container.style.opacity = if (isLoading) "0" else "1"
        }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInWindow().round()
            val size = coordinates.size
            bounds = IntRect(position, size)
        },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        }

        DisposableEffect(container) {
            document.body?.appendChild(container)
            onDispose {
                document.body?.removeChild(container)
            }
        }
    }
}
