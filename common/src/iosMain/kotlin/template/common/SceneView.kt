package template.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToURL
import platform.ModelIO.MDLAsset
import platform.SceneKit.*
import platform.UIKit.UIColor
import androidx.compose.ui.interop.UIKitView
import kotlinx.coroutines.delay
import platform.WebKit.WKWebView
import platform.WebKit.WKAudiovisualMediaTypeNone

@OptIn(ExperimentalForeignApi::class)
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
    }
    
    val client = remember { HttpClient(Darwin) }
    var scene by remember { mutableStateOf<SCNScene?>(null) }
    var isCheckingNative by remember { mutableStateOf(false) }
    var nativeFailed by remember { mutableStateOf(false) }
    var showLoader by remember(allUrls) { mutableStateOf(allUrls.isNotEmpty()) }

    LaunchedEffect(allUrls) {
        if (allUrls.isNotEmpty()) {
            isCheckingNative = true
            showLoader = true
            try {
                // For iOS native, we currently only support the first model
                val firstUrl = allUrls.first()
                val bytes = client.get(firstUrl).readRawBytes()
                val nsData = bytes.usePinned { pinned ->
                    NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
                }
                
                val tempDir = NSTemporaryDirectory()
                val fileName = "model.glb"
                val filePath = tempDir + fileName
                val fileURL = NSURL.fileURLWithPath(filePath)
                nsData.writeToURL(fileURL, true)
                
                val source = SCNSceneSource.sceneSourceWithData(nsData, null)
                var newScene = source?.sceneWithOptions(options = null, error = null)
                
                if (newScene == null || newScene.rootNode.childNodes.isEmpty()) {
                    val asset = MDLAsset(uRL = fileURL)
                    newScene = SCNScene.sceneWithMDLAsset(asset)
                }

                if (newScene == null || newScene.rootNode.childNodes.isEmpty()) {
                    newScene = SCNScene.sceneWithURL(fileURL, null, null)
                }
                
                val finalScene = newScene ?: SCNScene()
                
                if (finalScene.rootNode.childNodes.isNotEmpty()) {
                    // Lights and Camera setup
                    val lightNode = SCNNode()
                    lightNode.light = SCNLight().apply { type = SCNLightTypeOmni; intensity = 3500.0 }
                    lightNode.position = SCNVector3Make(5.0f, 10.0f, 5.0f)
                    finalScene.rootNode.addChildNode(lightNode)
                    
                    val ambientLightNode = SCNNode()
                    ambientLightNode.light = SCNLight().apply {
                        type = SCNLightTypeAmbient
                        color = UIColor.whiteColor
                        intensity = 1000.0
                    }
                    finalScene.rootNode.addChildNode(ambientLightNode)
                    
                    val cameraNode = SCNNode()
                    cameraNode.camera = SCNCamera().apply { automaticallyAdjustsZRange = true }
                    cameraNode.position = SCNVector3Make(0.0f, 0.0f, 3.5f)
                    finalScene.rootNode.addChildNode(cameraNode)
                    
                    scene = finalScene
                    nativeFailed = false
                    showLoader = false
                    onModelLoaded()
                } else {
                    nativeFailed = true
                }
            } catch (ignored: Exception) {
                nativeFailed = true
            } finally {
                isCheckingNative = false
            }
        }
    }

    // Safety timeout for loader
    LaunchedEffect(allUrls) {
        if (allUrls.isNotEmpty()) {
            delay(10000)
            showLoader = false
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (!isCheckingNative && !nativeFailed && scene != null && !isAR) {
            UIKitView(
                factory = {
                    SCNView().apply {
                        allowsCameraControl = true
                        backgroundColor = UIColor.blackColor
                        autoenablesDefaultLighting = true
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view -> 
                    view.scene = scene 
                }
            )
        } else if (allUrls.isNotEmpty() || videoUrl != null) {
            val html = remember(allUrls, videoUrl, isAR, arMode, trackingImage, imageTargets, autoRotate) {
                if (isAR && arMode == ARMode.Image) {
                    val mindFile = trackingImage?.replace(".jpeg", ".mind")?.replace(".jpg", ".mind")?.let { 
                        if (it.startsWith("http")) it else if (it.startsWith("/")) "https://raw.githubusercontent.com/ihridoydas/ARSceneViewComposeSample/feature/default/app/src/main/assets$it" else "https://raw.githubusercontent.com/ihridoydas/ARSceneViewComposeSample/feature/default/app/src/main/assets/$it"
                    } ?: "https://raw.githubusercontent.com/ihridoydas/ARSceneViewComposeSample/feature/default/app/src/main/assets/images/cute.mind"

                    val modelAssets = mutableListOf<String>()
                    val modelEntities = mutableListOf<String>()
                    val targetImages = mutableListOf<String>()
                    trackingImage?.let { targetImages.add(it) }
                    imageTargets.keys.forEach { if (it != trackingImage) targetImages.add(it) }

                    videoUrl?.let { url ->
                        modelAssets.add("<video id=\"arVideo\" src=\"$url\" loop=\"true\" crossorigin=\"anonymous\" muted playsinline webkit-playsinline preload=\"auto\"></video>")
                        modelEntities.add("<a-entity mindar-image-target=\"targetIndex: 0\"><a-video src=\"#arVideo\" width=\"1\" height=\"0.56\" position=\"0 0 0\" material=\"shader: flat; src: #arVideo\"></a-video></a-entity>")
                    }

                    targetImages.forEachIndexed { index, path ->
                        val url = if (path == trackingImage) (modelUrl ?: imageTargets[path]) else imageTargets[path]
                        if (url != null) {
                            val id = "targetModel$index"
                            modelAssets.add("<a-asset-item id=\"$id\" src=\"$url\"></a-asset-item>")
                            modelEntities.add("<a-entity mindar-image-target=\"targetIndex: $index\"><a-gltf-model src=\"#$id\" scale=\"0.5 0.5 0.5\"></a-gltf-model>${if (!textContent.isNullOrBlank()) "<a-text value=\"$textContent\" position=\"0 0.5 0\" align=\"center\"></a-text>" else ""}</a-entity>")
                        }
                    }

                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                        <script>
                            // Force compatibility for iOS WebView
                            window.addEventListener('load', () => {
                                if (!navigator.mediaDevices) {
                                    console.log("Polyfilling mediaDevices");
                                    navigator.mediaDevices = {};
                                }
                            });
                        </script>
                        <script src="https://aframe.io/releases/1.4.2/aframe.min.js"></script>
                        <script src="https://cdn.jsdelivr.net/npm/mind-ar@1.2.5/dist/mindar-image-aframe.prod.js"></script>
                        <style>
                            body, html { margin: 0; padding: 0; width: 100%; height: 100%; background: transparent; overflow: hidden; }
                            #start-overlay {
                                position: fixed; top: 0; left: 0; width: 100%; height: 100%;
                                background: rgba(0,0,0,0.8); color: white; display: flex;
                                flex-direction: column; justify-content: center; align-items: center;
                                z-index: 1000; font-family: sans-serif; cursor: pointer;
                            }
                        </style>
                    </head>
                    <body>
                        <div id="start-overlay" onclick="startAR()">
                            <div style="font-size: 50px; margin-bottom: 20px;">📷</div>
                            <div style="font-size: 20px;">Tap to Start iOS AR</div>
                        </div>
                        <a-scene 
                            mindar-image="imageTargetSrc: $mindFile; autoStart: false; uiScanning: yes; uiLoading: yes;" 
                            embedded="false" color-management="true" renderer="colorManagement: true, physicallyCorrectLights: true"
                            vr-mode-ui="enabled: false" device-orientation-permission-ui="enabled: false">
                            <a-assets>${modelAssets.joinToString("")}</a-assets>
                            <a-camera position="0 0 0" look-controls="enabled: false"></a-camera>
                            ${modelEntities.joinToString("")}
                        </a-scene>
                        <script>
                            function startAR() {
                                const overlay = document.getElementById('start-overlay');
                                overlay.innerHTML = "<div>Initializing...</div>";
                                
                                const sceneEl = document.querySelector('a-scene');
                                
                                // Ensure MindAR system exists before starting
                                const checkSystem = setInterval(() => {
                                    if (sceneEl.systems && sceneEl.systems['mindar-image-system']) {
                                        clearInterval(checkSystem);
                                        overlay.style.display = 'none';
                                        sceneEl.systems['mindar-image-system'].start();
                                    }
                                }, 100);
                                
                                sceneEl.addEventListener('targetFound', () => {
                                    const video = document.getElementById('arVideo');
                                    if (video) video.play().catch(e => console.log("Video error", e));
                                });
                                sceneEl.addEventListener('targetLost', () => {
                                    const video = document.getElementById('arVideo');
                                    if (video) video.pause();
                                });
                            }
                        </script>
                    </body>
                    </html>
                    """.trimIndent()
                } else {
                    val modelsHtml = allUrls.joinToString("\n") { url ->
                        """<model-viewer src="$url" ${if (autoRotate) "auto-rotate" else ""} camera-controls shadow-intensity="1" style="width:100%; height:100%; position:absolute; top:0; left:0;"></model-viewer>"""
                    }
                    val videoHtml = if (videoUrl != null) {
                        """<video src="$videoUrl" autoplay loop muted style="width:100%; height:100%; position:absolute; top:0; left:0; object-fit:cover; z-index:-1;"></video>"""
                    } else ""
                    
                    """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <script type="module" src="https://ajax.googleapis.com/ajax/libs/model-viewer/3.4.0/model-viewer.min.js"></script>
                        <style>
                            body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: black; overflow: hidden; }
                            #container { width: 100%; height: 100%; position: relative; }
                        </style>
                    </head>
                    <body>
                        <div id="container">
                            $videoHtml
                            $modelsHtml
                        </div>
                    </body>
                    </html>
                    """.trimIndent()
                }
            }

            val state = rememberWebViewStateWithHTMLData(html, baseUrl = "https://localhost")
            WebView(
                state = state, 
                modifier = Modifier.fillMaxSize(),
                onCreated = { webView ->
                    (webView as? WKWebView)?.configuration?.let { config ->
                        config.setAllowsInlineMediaPlayback(true)
                        config.mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
                    }
                }
            )

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                    color = Color(0xFFDAA520),
                    trackColor = Color.Transparent
                )
            }
        }

        if (showLoader && (isCheckingNative || (nativeFailed && allUrls.isNotEmpty()))) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                color = Color(0xFFDAA520),
                trackColor = Color.Transparent
            )
        }
    }
}
