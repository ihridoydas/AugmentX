package template.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.sceneview.SceneView
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import java.nio.ByteBuffer

@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?
) {
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
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
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

        if (modelBuffer.value == null && modelUrl != null) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
