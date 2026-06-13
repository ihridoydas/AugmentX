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

@JsFun("(property, value) => { if (window.updateARProperty) window.updateARProperty(property, value); }")
external fun callUpdateARProperty(property: String, value: String)

@JsFun("(property, value) => { if (window.updateModelViewerProperty) window.updateModelViewerProperty(property, value); }")
external fun callUpdateModelViewerProperty(property: String, value: String)

@JsFun("() => { if (window.stopARSession) window.stopARSession(); }")
external fun callStopWebAR()

@JsFun("() => { window.location.href = window.location.origin; }")
external fun callHardReset()

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
    exposure: Float,
    fogDensity: Float,
    animationSpeed: Float,
    textContent: String?,
    scale: Float,
    billboard: Boolean,
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
        
        // Initial setup
        LaunchedEffect(Unit) {
            container.setAttribute("style", "width: 100%; height: 100%; background: transparent; position: absolute; top: 0; left: 0;")
            val primaryUrl = allUrls.firstOrNull() ?: ""
            container.innerHTML = """
                <model-viewer 
                    src="$primaryUrl" 
                    camera-controls 
                    touch-action="pan-y" 
                    shadow-intensity="1"
                    environment-image="neutral"
                    interaction-prompt="auto"
                    ar-modes="webxr scene-viewer quick-look"
                    style="width:100%; height:100%; background:transparent; --progress-bar-color: transparent;">
                </model-viewer>
            """.trimIndent()
            onModelLoaded()
        }

        // Real-time attribute updates
        LaunchedEffect(exposure, scale, autoRotate, textContent, animationSpeed) {
            val mv = container.querySelector("model-viewer") ?: return@LaunchedEffect
            mv.setAttribute("exposure", exposure.toString())
            mv.setAttribute("scale", "$scale $scale $scale")
            if (autoRotate) mv.setAttribute("auto-rotate", "") else mv.removeAttribute("auto-rotate")
            
            callUpdateModelViewerProperty("timeScale", animationSpeed.toString())

            // Handle text hotspot
            if (!textContent.isNullOrBlank()) {
                val existingHotspot = mv.querySelector("[slot='hotspot-text']")
                if (existingHotspot == null) {
                    val div = document.createElement("div")
                    div.setAttribute("slot", "hotspot-text")
                    div.setAttribute("data-position", "0 1 0")
                    div.setAttribute("style", "background: white; padding: 4px; border-radius: 4px; color: black; font-weight: bold;")
                    div.textContent = textContent
                    mv.appendChild(div)
                } else {
                    existingHotspot.textContent = textContent
                }
            } else {
                mv.querySelector("[slot='hotspot-text']")?.remove()
            }
        }
        
        DisposableEffect(container) {
            val viewerContainer = document.getElementById("ViewerContainer") as? HTMLElement
            if (viewerContainer != null) {
                viewerContainer.appendChild(container)
                viewerContainer.style.display = "block"
                
                // Position the container below the AppBar area (64px) so Back is clickable
                viewerContainer.style.top = "64px"
                viewerContainer.style.height = "calc(100dvh - 64px)"
                viewerContainer.style.zIndex = "20"
                viewerContainer.setAttribute("style", viewerContainer.getAttribute("style") ?: "" + "pointer-events: auto;")
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

    // AR Real-time Updates
    LaunchedEffect(exposure, scale, textContent, arStarted, billboard) {
        if (arStarted) {
            callUpdateARProperty("exposure", exposure.toString())
            callUpdateARProperty("scale", scale.toString())
            callUpdateARProperty("text", if (billboard) (textContent ?: "") else "")
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isAR && arMode == ARMode.Image && !arStarted) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black).clickable {
                    val isAbsolute = trackingImage?.startsWith("http") == true || trackingImage?.startsWith("blob:") == true
                    val mindFile = if (isAbsolute) {
                        trackingImage!!
                    } else {
                        trackingImage?.replace(".jpeg", ".mind")?.replace(".jpg", ".mind")?.let { 
                            if (it.startsWith("/")) it else "/$it" 
                        } ?: ""
                    }
                    
                    if (mindFile.isBlank()) {
                        println("SceneView Error: No tracking image provided")
                        return@clickable
                    }
                    
                    val modelAssets = mutableListOf<String>()
                    val modelEntities = mutableListOf<String>()
                    val targetImages = mutableListOf<String>()
                    trackingImage?.let { targetImages.add(it) }
                    imageTargets.keys.forEach { if (it != trackingImage) targetImages.add(it) }

                    // Handle Video if present
                    videoUrl?.let { url ->
                        // Removed 'muted' and 'autoplay' from tag to control it surgically via JS
                        modelAssets.add("<video id=\"arVideo\" src=\"$url\" loop=\"true\" crossorigin=\"anonymous\" playsinline webkit-playsinline preload=\"auto\"></video>")
                        
                        // Default to index 0 for the video if it's the main AR content
                        modelEntities.add("<a-entity mindar-image-target=\"targetIndex: 0\"><a-video src=\"#arVideo\" width=\"1\" height=\"0.56\" position=\"0 0 0\" material=\"shader: flat; src: #arVideo\"></a-video></a-entity>")
                    }

                    targetImages.forEachIndexed { index, path ->
                        val url = if (path == trackingImage) (modelUrl ?: imageTargets[path]) else imageTargets[path]
                        if (url != null) {
                            val id = "targetModel$index"
                            modelAssets.add("<a-asset-item id=\"$id\" src=\"$url\"></a-asset-item>")
                            modelEntities.add("<a-entity mindar-image-target=\"targetIndex: $index\"><a-gltf-model src=\"#$id\" scale=\"${0.5 * scale} ${0.5 * scale} ${0.5 * scale}\"></a-gltf-model>${if (!textContent.isNullOrBlank()) "<a-text value=\"$textContent\" position=\"0 0.5 0\" align=\"center\"></a-text>" else ""}</a-entity>")
                        }
                    }

                    val html = """
                        <a-scene 
                            mindar-image="imageTargetSrc: $mindFile; autoStart: true; uiScanning: yes; uiLoading: yes;" 
                            embedded="false"
                            background="transparent: true"
                            renderer="alpha: true; colorManagement: true; antialias: true; logarithmicDepthBuffer: true; exposure: $exposure;"
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
