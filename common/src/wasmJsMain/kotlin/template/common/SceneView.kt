package template.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@JsFun("(mindFile, htmlContent) => window.startARSession(mindFile, htmlContent)")
external fun callStartWebAR(mindFile: String, htmlContent: String)

@JsFun("() => { if (window.stopARSession) window.stopARSession(); }")
external fun callStopWebAR()

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
    }.filter { it.isNotBlank() }
    
    if (allUrls.isEmpty() && videoUrl == null) return

    var arStarted by remember { mutableStateOf(false) }

    // Ensure we clean up AR when this composable leaves the screen
    DisposableEffect(Unit) {
        onDispose {
            try {
                callStopWebAR()
            } catch (e: Exception) {
                println("SceneView Dispose Error: ${e.message}")
            }
        }
    }

    // Standard 3D Viewer Logic (Non-AR)
    if (!isAR || arMode != ARMode.Image) {
        val container = remember { (document.createElement("div") as HTMLElement) }
        LaunchedEffect(allUrls, autoRotate) {
            val primaryUrl = allUrls.firstOrNull() ?: ""
            println("SceneView: Loading 3D model: $primaryUrl")
            
            container.setAttribute("style", "width: 100%; height: 100%; background: transparent;")
            
            val autoRotateAttr = if (autoRotate) "auto-rotate" else ""
            
            container.innerHTML = """
                <model-viewer 
                    src="$primaryUrl" 
                    camera-controls 
                    touch-action="pan-y" 
                    $autoRotateAttr
                    shadow-intensity="1"
                    environment-image="neutral"
                    exposure="1"
                    interaction-prompt="auto"
                    ar-modes="webxr scene-viewer quick-look"
                    style="width:100%; height:100%; background:transparent; --progress-bar-color: transparent;">
                </model-viewer>
            """.trimIndent()
            
            val mv = container.querySelector("model-viewer")
            mv?.addEventListener("load", {
                println("SceneView: Model loaded successfully")
            })
            mv?.addEventListener("error", {
                println("SceneView: Model failed to load")
            })
            
            onModelLoaded()
        }
        DisposableEffect(container) {
            val viewerContainer = document.getElementById("ViewerContainer") as? HTMLElement
            if (viewerContainer != null) {
                viewerContainer.appendChild(container)
                viewerContainer.style.display = "block"
                // Try higher z-index to ensure it's not hidden by Compose
                viewerContainer.style.zIndex = "20"
            }
            onDispose { 
                try {
                    if (container.parentNode != null) {
                        container.parentNode?.removeChild(container)
                    }
                    if (viewerContainer != null && viewerContainer.children.length == 0) {
                        viewerContainer.style.display = "none"
                    }
                } catch (e: Exception) {
                    println("SceneView: Cleanup error: ${e.message}")
                }
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isAR && arMode == ARMode.Image && !arStarted) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black).clickable {
                    val mindFile = trackingImage?.replace(".jpeg", ".mind")?.replace(".jpg", ".mind")?.let { if (it.startsWith("/")) it else "/$it" } ?: "/images/cute.mind"
                    
                    val modelAssets = mutableListOf<String>()
                    val modelEntities = mutableListOf<String>()
                    val targetImages = mutableListOf<String>()
                    trackingImage?.let { targetImages.add(it) }
                    imageTargets.keys.forEach { if (it != trackingImage) targetImages.add(it) }

                    targetImages.forEachIndexed { index, path ->
                        val url = if (path == trackingImage) (modelUrl ?: imageTargets[path]) else imageTargets[path]
                        if (url != null) {
                            val id = "targetModel$index"
                            modelAssets.add("<a-asset-item id=\"$id\" src=\"$url\"></a-asset-item>")
                            modelEntities.add("<a-entity mindar-image-target=\"targetIndex: $index\"><a-gltf-model src=\"#$id\" scale=\"0.5 0.5 0.5\"></a-gltf-model></a-entity>")
                        }
                    }

                    val html = """
                        <a-scene 
                            mindar-image="imageTargetSrc: $mindFile; autoStart: true; uiScanning: no; uiLoading: no;" 
                            embedded="false"
                            background="transparent: true"
                            renderer="alpha: true; colorManagement: true;"
                            vr-mode-ui="enabled: false" device-orientation-permission-ui="enabled: false"
                            style="width: 100vw; height: 100vh; background: transparent;">
                            <a-assets>${modelAssets.joinToString("")}</a-assets>
                            <a-camera position="0 0 0" look-controls="enabled: false"></a-camera>
                            ${modelEntities.joinToString("")}
                        </a-scene>
                    """.trimIndent()

                    try {
                        callStartWebAR(mindFile, html)
                        arStarted = true
                        onModelLoaded()
                    } catch (e: Exception) {
                        println("SceneView Error: ${e.message}")
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Tap to Start Web AR", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
