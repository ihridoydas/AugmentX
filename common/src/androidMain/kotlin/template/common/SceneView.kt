package template.common

import android.media.MediaPlayer
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.SceneView as LibSceneView
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.VideoNode
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.ModelLoader
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.delay
import java.nio.ByteBuffer

@Composable
fun ARModelInstance(modelLoader: ModelLoader, buffer: ByteBuffer, scale: Float = 0.5f) {
    val modelInstance = remember(buffer) { modelLoader.createModelInstance(buffer) }
    if (modelInstance != null) {
        ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = scale,
            autoAnimate = true
        )
    }
}

@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?,
    modelUrls: List<String>,
    videoUrl: String?,
    isAR: Boolean,
    arMode: ARMode,
    autoRotate: Boolean,
    skyboxUrl: String?,
    onModelLoaded: () -> Unit
) {
    val allUrls = remember(modelUrl, modelUrls) {
        if (modelUrl != null) listOf(modelUrl) + modelUrls else modelUrls
    }
    
    var isLoading by remember(allUrls) { mutableStateOf(allUrls.isNotEmpty()) }
    
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val client = remember { HttpClient(Android) }

    val modelBuffers = remember(allUrls) { mutableStateListOf<ByteBuffer?>() }
    val placedAnchors = remember { mutableStateListOf<Anchor>() }
    var lastFrame by remember { mutableStateOf<Frame?>(null) }

    // Video Player
    val mediaPlayer = remember(videoUrl) {
        if (videoUrl != null) {
            MediaPlayer().apply {
                setDataSource(videoUrl)
                prepareAsync()
                isLooping = true
                setOnPreparedListener { start() }
            }
        } else null
    }

    DisposableEffect(mediaPlayer) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    LaunchedEffect(allUrls) {
        modelBuffers.clear()
        isLoading = allUrls.isNotEmpty()
        
        allUrls.forEach { url ->
            try {
                val bytes = client.get(url).readRawBytes()
                modelBuffers.add(ByteBuffer.wrap(bytes))
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
                sessionConfiguration = { session, config ->
                    when (arMode) {
                        ARMode.Depth -> {
                            if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                                config.depthMode = Config.DepthMode.AUTOMATIC
                            }
                        }
                        ARMode.Instant -> {
                            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                        }
                        ARMode.Face -> {
                            config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
                        }
                        else -> {}
                    }
                },
                onSessionUpdated = { _, frame ->
                    lastFrame = frame
                },
                onTouchEvent = { motionEvent, _ ->
                    if (motionEvent.action == MotionEvent.ACTION_UP) {
                        lastFrame?.let { frame ->
                            val hitResults = frame.hitTest(motionEvent.x, motionEvent.y)
                            hitResults.firstOrNull { 
                                it.trackable.trackingState == TrackingState.TRACKING 
                            }?.let { hitResult ->
                                placedAnchors.add(hitResult.createAnchor())
                            }
                        }
                    }
                    false
                }
            ) {
                // Placed Models
                for (anchor in placedAnchors) {
                    AnchorNode(anchor = anchor) {
                        val buffer = modelBuffers.firstOrNull()
                        if (buffer != null) {
                            ARModelInstance(modelLoader, buffer, 0.5f)
                        }
                    }
                }

                if (mediaPlayer != null) {
                    VideoNode(
                        player = mediaPlayer,
                        position = Float3(0f, 0f, -2f)
                    )
                }
            }
        } else {
            LibSceneView(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
            ) {
                for (buffer in modelBuffers) {
                    if (buffer != null) {
                        ARModelInstance(modelLoader, buffer, 1.0f)
                    }
                }

                if (mediaPlayer != null) {
                    VideoNode(
                        player = mediaPlayer,
                        position = Float3(0f, 0f, -2f)
                    )
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
