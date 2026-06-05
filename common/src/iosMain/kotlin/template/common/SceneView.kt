package template.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import platform.SceneKit.SCNCamera
import platform.SceneKit.SCNLight
import platform.SceneKit.SCNLightTypeAmbient
import platform.SceneKit.SCNLightTypeOmni
import platform.SceneKit.SCNNode
import platform.SceneKit.SCNScene
import platform.SceneKit.SCNSceneSource
import platform.SceneKit.SCNVector3Make
import platform.SceneKit.SCNView
import platform.SceneKit.sceneWithMDLAsset
import platform.UIKit.UIColor
import androidx.compose.ui.interop.UIKitView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?
) {
    val client = remember { HttpClient(Darwin) }
    var scene by remember { mutableStateOf<SCNScene?>(null) }
    var isCheckingNative by remember { mutableStateOf(false) }
    var nativeFailed by remember { mutableStateOf(false) }

    LaunchedEffect(modelUrl) {
        if (modelUrl != null) {
            isCheckingNative = true
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
                } else {
                    nativeFailed = true
                }
                isCheckingNative = false
            } catch (ignored: Exception) {
                nativeFailed = true
                isCheckingNative = false
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isCheckingNative) {
            CircularProgressIndicator(color = Color.White)
        } else if (!nativeFailed && scene != null) {
            UIKitView(
                factory = {
                    SCNView().apply {
                        allowsCameraControl = true
                        backgroundColor = UIColor.blackColor
                        autoenablesDefaultLighting = true
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view -> view.scene = scene }
            )
        } else if (modelUrl != null) {
            val html = remember(modelUrl) {
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
                    <model-viewer src="$modelUrl" auto-rotate camera-controls shadow-intensity="1" ar></model-viewer>
                </body>
                </html>
            """.trimIndent()
            }

            val state = rememberWebViewStateWithHTMLData(html)
            WebView(state = state, modifier = Modifier.fillMaxSize())

            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
