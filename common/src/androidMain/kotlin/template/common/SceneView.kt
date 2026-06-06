package template.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.delay
import java.nio.ByteBuffer

@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?,
    isAR: Boolean,
    autoRotate: Boolean,
    skyboxUrl: String?,
    onModelLoaded: () -> Unit
) {
    var isLoading by remember(modelUrl) { mutableStateOf(modelUrl != null) }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraManipulator = rememberCameraManipulator()
    val client = remember { HttpClient(Android) }

    val modelBuffer = produceState<ByteBuffer?>(null, modelUrl) {
        if (modelUrl != null) {
            try {
                val bytes = client.get(modelUrl).readRawBytes()
                value = ByteBuffer.wrap(bytes)
            } catch (e: Exception) {
                android.util.Log.e("SceneView", "Failed to download model: $modelUrl", e)
            } finally {
                isLoading = false
                onModelLoaded()
            }
        }
    }

    // Safety timeout for loader
    LaunchedEffect(modelUrl) {
        if (modelUrl != null) {
            delay(8000)
            isLoading = false
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isAR) {
            ARSceneView(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                planeRenderer = true,
                onSessionUpdated = { _, _ ->
                    // Handle AR session updates
                }
            ) {
                modelBuffer.value?.let { buffer ->
                    val modelInstance = remember(buffer) {
                        modelLoader.createModelInstance(buffer)
                    }
                    ModelNode(
                        modelInstance = modelInstance,
                        scaleToUnits = 1.0f
                    )
                }
            }
        } else {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                cameraManipulator = cameraManipulator,
                autoFitContent = true
            ) {
                modelBuffer.value?.let { buffer ->
                    val modelInstance = remember(buffer) {
                        modelLoader.createModelInstance(buffer)
                    }
                    ModelNode(
                        modelInstance = modelInstance,
                        scaleToUnits = 1.0f
                    )
                }
            }
        }

        if (isLoading && modelUrl != null) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                color = Color(0xFFDAA520),
                trackColor = Color.Transparent
            )
        }
    }
}
