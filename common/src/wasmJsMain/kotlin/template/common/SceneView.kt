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
    var arStarted by remember { mutableStateOf(false) }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                callStopWebAR()
            } catch (e: Exception) {
                println("SceneView Dispose Error: ${e.message}")
            }
        }
    }

    if (!isAR || arMode != ARMode.Image) {
        // Standard 3D Viewer (Model Viewer)
        val container = remember { (document.createElement("div") as HTMLElement) }
        val primaryUrl = modelUrl ?: modelUrls.firstOrNull() ?: ""
        
        if (primaryUrl.isNotBlank()) {
            LaunchedEffect(primaryUrl, autoRotate) {
                container.setAttribute("style", "width: 100%; height: 100%; background: transparent; position: absolute; top: 0; left: 0;")
                val autoRotateAttr = if (autoRotate) "auto-rotate" else ""
                container.innerHTML = """
                    <model-viewer 
                        src="$primaryUrl" camera-controls touch-action="pan-y" $autoRotateAttr
                        shadow-intensity="1" environment-image="neutral" exposure="1"
                        style="width:100%; height:100%; background:transparent;">
                    </model-viewer>
                """.trimIndent()
                onModelLoaded()
            }
            DisposableEffect(container) {
                val viewerContainer = document.getElementById("ViewerContainer") as? HTMLElement
                if (viewerContainer != null) {
                    viewerContainer.appendChild(container)
                    viewerContainer.style.display = "block"
                    viewerContainer.style.top = "64px"
                    viewerContainer.style.height = "calc(100dvh - 64px)"
                    viewerContainer.style.zIndex = "20"
                }
                onDispose { 
                    try { container.parentNode?.removeChild(container) } catch(e:Exception) {}
                    if (viewerContainer?.children?.length == 0) viewerContainer.style.display = "none"
                }
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isAR && arMode == ARMode.Image) {
            if (!arStarted) {
                // Initial "Start AR" button to satisfy browser autoplay/camera policies
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black).clickable {
                        // SMART URL RESOLUTION
                        val isAbsolute = trackingImage?.startsWith("http") == true || trackingImage?.startsWith("blob:") == true
                        val mindFile = if (isAbsolute) {
                            trackingImage!!
                        } else {
                            trackingImage?.replace(".jpeg", ".mind")?.replace(".jpg", ".mind")?.let { 
                                if (it.startsWith("/")) it else "/$it" 
                            } ?: "/images/cute.mind"
                        }
                        
                        println("SceneView: Starting AR with MindFile: $mindFile")

                        val modelAssets = mutableListOf<String>()
                        val modelEntities = mutableListOf<String>()

                        // If it's a video-only request (like ARVideoDemo)
                        if (videoUrl != null && imageTargets.isEmpty()) {
                            modelAssets.add("<video id=\"arVideo\" src=\"$videoUrl\" loop=\"true\" crossorigin=\"anonymous\" playsinline webkit-playsinline preload=\"auto\"></video>")
                            modelEntities.add("<a-entity mindar-image-target=\"targetIndex: 0\"><a-video src=\"#arVideo\" width=\"1\" height=\"0.56\" position=\"0 0 0\" material=\"shader: flat; src: #arVideo\"></a-video></a-entity>")
                        } else {
                            // Unified target mapping
                            val allTargets = mutableListOf<String>()
                            trackingImage?.let { allTargets.add(it) }
                            imageTargets.keys.forEach { if (!allTargets.contains(it)) allTargets.add(it) }

                            allTargets.forEachIndexed { index, path ->
                                val content = if (path == trackingImage) (modelUrl ?: imageTargets[path]) else imageTargets[path]
                                if (content != null) {
                                    val id = "asset_$index"
                                    if (content.endsWith(".mp4") || content.contains("video")) {
                                        modelAssets.add("<video id=\"$id\" src=\"$content\" loop=\"true\" crossorigin=\"anonymous\" playsinline webkit-playsinline preload=\"auto\"></video>")
                                        modelEntities.add("<a-entity mindar-image-target=\"targetIndex: $index\"><a-video src=\"#$id\" width=\"1\" height=\"0.56\" position=\"0 0 0\" material=\"shader: flat; src: #$id\"></a-video></a-entity>")
                                    } else {
                                        modelAssets.add("<a-asset-item id=\"$id\" src=\"$content\"></a-asset-item>")
                                        modelEntities.add("<a-entity mindar-image-target=\"targetIndex: $index\"><a-gltf-model src=\"#$id\" scale=\"0.5 0.5 0.5\"></a-gltf-model></a-entity>")
                                    }
                                }
                            }
                        }

                        val html = """
                            <a-scene 
                                mindar-image="imageTargetSrc: $mindFile; autoStart: true; uiScanning: yes; uiLoading: yes;" 
                                embedded="false" renderer="alpha: true; colorManagement: true; antialias: true;"
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
                        Text(text = "Ensure camera permission is granted", color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
