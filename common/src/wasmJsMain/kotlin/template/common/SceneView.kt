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

// CORRECT: String parameters in @JsFun for Wasm must be handled as JS types
@JsFun("(mindFile, htmlContent) => window.startARSession(mindFile, htmlContent)")
external fun callStartWebAR(mindFile: String, htmlContent: String)

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
                            loading-screen="enabled: false"
                            renderer="alpha: true; colorManagement: true; antialias: true;"
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
