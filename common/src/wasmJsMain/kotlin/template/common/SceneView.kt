package template.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    var arStarted by remember { mutableStateOf(false) }
    
    val container = remember(allUrls, videoUrl, isAR, arMode, trackingImage) {
        (document.createElement("div") as HTMLElement).apply {
            setAttribute("style", "position:fixed; z-index: 5; pointer-events: auto; display: block; opacity: 0; background: transparent;")
            
            if (isAR && arMode == ARMode.Image) {
                val mindFile = trackingImage?.replace(".jpeg", ".mind")?.replace(".jpg", ".mind") ?: "images/targets.mind"
                
                val modelEntity = if (modelUrl != null) {
                    """<a-entity mindar-image-target="targetIndex: 0">
                        <a-gltf-model src="#arModel" scale="0.5 0.5 0.5" position="0 0 0" rotation="0 0 0"></a-gltf-model>
                       </a-entity>"""
                } else ""
                
                val videoEntity = if (videoUrl != null) {
                    """<a-entity mindar-image-target="targetIndex: 0">
                        <a-video src="#arVideo" width="1" height="0.55" position="0 0 0"></a-video>
                       </a-entity>"""
                } else ""

                innerHTML = """
                    <a-scene 
                        mindar-image="imageTargetSrc: $mindFile; autoStart: true; uiScanning: no; uiLoading: no;" 
                        embedded color-space="sRGB" 
                        renderer="colorManagement: true, physicallyCorrectLights" 
                        vr-mode-ui="enabled: false" 
                        device-orientation-permission-ui="enabled: false"
                        style="width: 100%; height: 100%; background: transparent;">
                        
                        <a-assets>
                            ${if (modelUrl != null) "<a-asset-item id=\"arModel\" src=\"$modelUrl\"></a-asset-item>" else ""}
                            ${if (videoUrl != null) "<video id=\"arVideo\" src=\"$videoUrl\" loop=\"true\" crossorigin=\"anonymous\"></video>" else ""}
                        </a-assets>

                        <a-camera position="0 0 0" look-controls="enabled: false"></a-camera>
                        
                        $modelEntity
                        $videoEntity
                    </a-scene>
                """.trimIndent()
                
                isLoading = false
                onModelLoaded()
            } else {
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

        // Web AR Image Start Button
        if (isAR && arMode == ARMode.Image && !arStarted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { 
                        arStarted = true
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Start Camera",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to Start Web AR",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Requires camera permission",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (isAR && arMode == ARMode.Image && arStarted) {
            DisposableEffect(container) {
                document.body?.appendChild(container)
                onDispose {
                    document.body?.removeChild(container)
                }
            }
        } else if (!isAR || arMode != ARMode.Image) {
            DisposableEffect(container) {
                document.body?.appendChild(container)
                onDispose {
                    document.body?.removeChild(container)
                }
            }
        }
    }
}
