package template.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?,
    modelUrls: List<String>,
    videoUrl: String?,
    isAR: Boolean,
    arMode: ARMode,
    trackingImage: String?,
    imageTargets: Map<String, String>,
    autoRotate: Boolean,
    skyboxUrl: String?,
    onModelLoaded: () -> Unit
) {
    val allUrls = remember(modelUrl, modelUrls, imageTargets) {
        val targets = imageTargets.values.toList()
        (if (modelUrl != null) listOf(modelUrl) else emptyList()) + modelUrls + targets
    }
    
    if (allUrls.isEmpty() && videoUrl == null) return

    var isLoading by remember(allUrls, videoUrl) { mutableStateOf(true) }
    var bounds by remember { mutableStateOf(IntRect.Zero) }
    
    val container = remember(allUrls, videoUrl, isAR, autoRotate) {
        (document.createElement("div") as HTMLElement).apply {
            setAttribute("style", "position:fixed; z-index: 999; pointer-events: auto; display: block; opacity: 0;")
            
            val modelsHtml = allUrls.joinToString("\n") { url ->
                """
                <model-viewer 
                    src="$url" 
                    ${if (autoRotate) "auto-rotate" else ""} 
                    camera-controls 
                    ${if (isAR) "ar ar-modes=\"webxr scene-viewer quick-look\"" else ""} 
                    style="width:100%; height:100%; position:absolute; top:0; left:0;">
                </model-viewer>
                """.trimIndent()
            }
            
            val videoHtml = if (videoUrl != null) {
                "<video src=\"$videoUrl\" autoplay loop muted style=\"width:100%; height:100%; position:absolute; top:0; left:0; object-fit:cover; z-index:-1;\"></video>"
            } else ""
            
            innerHTML = videoHtml + modelsHtml
            
            val mvs = children
            var loadedCount = 0
            if (allUrls.isNotEmpty()) {
                for (i in 0 until mvs.length) {
                    val el = mvs.item(i)
                    if (el?.tagName?.lowercase() == "model-viewer") {
                        el.addEventListener("load", { 
                            loadedCount++
                            if (loadedCount >= allUrls.size) {
                                isLoading = false
                                onModelLoaded()
                            }
                        })
                    }
                }
            } else {
                isLoading = false
            }
        }
    }

    // Force show after a few seconds
    LaunchedEffect(allUrls, videoUrl) {
        kotlinx.coroutines.delay(8000)
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
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                color = Color(0xFFDAA520),
                trackColor = Color.Transparent
            )
        }

        DisposableEffect(container) {
            document.body?.appendChild(container)
            onDispose {
                document.body?.removeChild(container)
            }
        }
    }
}
