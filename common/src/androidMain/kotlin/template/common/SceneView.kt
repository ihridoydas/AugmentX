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
    modelUrls: List<String>,
    isAR: Boolean,
    autoRotate: Boolean,
    skyboxUrl: String?,
    onModelLoaded: () -> Unit
) {
    val allUrls = remember(modelUrl, modelUrls) {
        if (modelUrl != null) listOf(modelUrl) + modelUrls else modelUrls
    }
    
    var loadedCount by remember(allUrls) { mutableStateOf(0) }
    var isLoading by remember(allUrls) { mutableStateOf(allUrls.isNotEmpty()) }
    
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraManipulator = rememberCameraManipulator()
    val client = remember { HttpClient(Android) }

    val modelBuffers = remember(allUrls) { mutableStateListOf<ByteBuffer?>() }
    
    LaunchedEffect(allUrls) {
        modelBuffers.clear()
        loadedCount = 0
        isLoading = allUrls.isNotEmpty()
        
        allUrls.forEach { url ->
            try {
                val bytes = client.get(url).readRawBytes()
                modelBuffers.add(ByteBuffer.wrap(bytes))
                loadedCount++
            } catch (e: Exception) {
                android.util.Log.e("SceneView", "Failed to download model: $url", e)
            }
        }
        isLoading = false
        onModelLoaded()
    }

    // Safety timeout for loader
    LaunchedEffect(allUrls) {
        if (allUrls.isNotEmpty()) {
            delay(10000)
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
            ) {
                modelBuffers.forEach { buffer ->
                    buffer?.let {
                        val modelInstance = remember(it) {
                            modelLoader.createModelInstance(it)
                        }
                        ModelNode(
                            modelInstance = modelInstance,
                            scaleToUnits = 1.0f
                        )
                    }
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
                modelBuffers.forEach { buffer ->
                    buffer?.let {
                        val modelInstance = remember(it) {
                            modelLoader.createModelInstance(it)
                        }
                        ModelNode(
                            modelInstance = modelInstance,
                            scaleToUnits = 1.0f
                        )
                    }
                }
            }
        }

        if (isLoading && allUrls.isNotEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                color = Color(0xFFDAA520),
                trackColor = Color.Transparent
            )
        }
    }
}
