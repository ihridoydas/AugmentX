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

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?,
    isAR: Boolean,
    autoRotate: Boolean,
    skyboxUrl: String?,
    onModelLoaded: () -> Unit
) {
    val client = remember { HttpClient(Darwin) }
    var scene by remember { mutableStateOf<SCNScene?>(null) }
    var isCheckingNative by remember { mutableStateOf(false) }
    var nativeFailed by remember { mutableStateOf(false) }
    var showLoader by remember(modelUrl) { mutableStateOf(modelUrl != null) }

    LaunchedEffect(modelUrl) {
        if (modelUrl != null) {
            isCheckingNative = true
            showLoader = true
            try {
                val bytes = client.get(modelUrl).readRawBytes()
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
    LaunchedEffect(modelUrl) {
        if (modelUrl != null) {
            delay(8000)
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
                    if (autoRotate) {
                        // iOS specific auto-rotate
                    }
                }
            )
        } else if (modelUrl != null && !isCheckingNative) {
            val html = remember(modelUrl, isAR, autoRotate) {
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <script type="module" src="https://ajax.googleapis.com/ajax/libs/model-viewer/3.4.0/model-viewer.min.js"></script>
                    <style>
                        body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: black; overflow: hidden; }
                        model-viewer { width: 100%; height: 100%; background-color: black; }
                    </style>
                </head>
                <body>
                    <model-viewer 
                        src="$modelUrl" 
                        ${if (autoRotate) "auto-rotate" else ""} 
                        camera-controls 
                        shadow-intensity="1" 
                        ${if (isAR) "ar" else ""}>
                    </model-viewer>
                </body>
                </html>
            """.trimIndent()
            }

            val state = rememberWebViewStateWithHTMLData(html)
            WebView(state = state, modifier = Modifier.fillMaxSize())

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                    color = Color(0xFFDAA520),
                    trackColor = Color.Transparent
                )
            }
        }

        if (showLoader && (isCheckingNative || (nativeFailed && modelUrl != null))) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                color = Color(0xFFDAA520),
                trackColor = Color.Transparent
            )
        }
    }
}
