package template.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import com.google.ar.core.AugmentedImage
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.SceneView as LibSceneView
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.VideoNode
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.arcore.rememberRuntimeAugmentedImageDatabase
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.SceneScope
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Size
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

@Composable
fun SceneScope.ARModelInstance(
    modelLoader: ModelLoader, 
    buffer: ByteBuffer, 
    scale: Float = 0.5f
) {
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
    trackingImage: String?,
    imageTargets: Map<String, String>,
    autoRotate: Boolean,
    skyboxUrl: String?,
    onModelLoaded: () -> Unit
) {
    val context = LocalContext.current
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val client = remember { HttpClient(Android) }
    val scope = rememberCoroutineScope()

    val modelBuffers = remember { mutableStateMapOf<String, ByteBuffer>() }
    val imageBitmaps = remember { mutableStateMapOf<String, Bitmap>() }
    val placedAnchors = remember { mutableStateListOf<Anchor>() }
    var lastFrame by remember { mutableStateOf<Frame?>(null) }
    val detectedImages = remember { mutableStateMapOf<Int, AugmentedImage>() }

    var isLoading by remember { mutableStateOf(false) }
    
    // Official SceneView Runtime Image Database
    val runtimeDatabase = rememberRuntimeAugmentedImageDatabase()

    LaunchedEffect(modelUrl, modelUrls, imageTargets, trackingImage) {
        isLoading = true
        val allModelUrls = (if (modelUrl != null) listOf(modelUrl) else emptyList()) + 
                          modelUrls + 
                          imageTargets.values
        
        val allImagePaths = (if (trackingImage != null) listOf(trackingImage) else emptyList()) + 
                           imageTargets.keys

        // Load Models
        val modelJobs = allModelUrls.distinct().filter { !modelBuffers.containsKey(it) }.map { url ->
            launch {
                try {
                    val bytes = client.get(url).readRawBytes()
                    modelBuffers[url] = ByteBuffer.wrap(bytes)
                    android.util.Log.d("SceneView", "Model loaded: $url")
                } catch (e: Exception) {
                    android.util.Log.e("SceneView", "Failed to download model: $url", e)
                }
            }
        }

        // Load Image Bitmaps and register them immediately if database is ready
        val imageJobs = allImagePaths.distinct().filter { !imageBitmaps.containsKey(it) }.map { path ->
            launch {
                try {
                    val bitmap = if (path.startsWith("http")) {
                        val bytes = client.get(path).readRawBytes()
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } else {
                        context.assets.open(path).use { BitmapFactory.decodeStream(it) }
                    }
                    if (bitmap != null) {
                        imageBitmaps[path] = bitmap
                        // Also add to runtime database
                        runtimeDatabase.addImage(path, bitmap, 0.2f)
                        android.util.Log.d("SceneView", "Image registered: $path")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SceneView", "Failed to load image: $path", e)
                }
            }
        }
        
        (modelJobs + imageJobs).joinAll()
        isLoading = false
        onModelLoaded()
    }

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

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isAR) {
            ARSceneView(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                sessionConfiguration = { session, config ->
                    runtimeDatabase.applyTo(config, session)
                    config.focusMode = Config.FocusMode.AUTO
                    
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
                onSessionCreated = { session ->
                    runtimeDatabase.bind(session)
                },
                onSessionUpdated = { _, frame ->
                    lastFrame = frame
                    if (arMode == ARMode.Image) {
                        frame.getUpdatedTrackables(AugmentedImage::class.java).forEach { image ->
                            if (image.trackingState == TrackingState.TRACKING) {
                                if (!detectedImages.containsKey(image.index)) {
                                    detectedImages[image.index] = image
                                    android.util.Log.d("SceneView", "Tracking Detected: ${image.name}")
                                }
                            } else if (image.trackingState == TrackingState.STOPPED) {
                                detectedImages.remove(image.index)
                            }
                        }
                    }
                },
                onTouchEvent = { motionEvent, _ ->
                    if (motionEvent.action == MotionEvent.ACTION_UP && arMode == ARMode.Plane) {
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
                // Plane Placed Models
                val defaultModelBuffer = modelUrl?.let { modelBuffers[it] } 
                                       ?: modelUrls.firstOrNull()?.let { modelBuffers[it] }

                for (anchor in placedAnchors) {
                    AnchorNode(anchor = anchor) {
                        if (defaultModelBuffer != null) {
                            ARModelInstance(modelLoader, defaultModelBuffer, 0.5f)
                        }
                    }
                }

                // Handle Augmented Images
                if (arMode == ARMode.Image) {
                    for (image in detectedImages.values) {
                        val targetModelUrl = if (image.name == "default") modelUrl else imageTargets[image.name]
                        val buffer = targetModelUrl?.let { modelBuffers[it] }

                        val imageWidth = image.extentX.takeIf { it > 0 } ?: 0.2f
                        val imageHeight = image.extentZ.takeIf { it > 0 } ?: (imageWidth / (16f / 9f))

                        // 🔹 Force video frame to match image frame size exactly
                        val finalWidth = imageWidth
                        val finalHeight = imageHeight
                        
                        if (buffer != null) {
                            AugmentedImageNode(augmentedImage = image, applyImageScale = true) {
                                val modelInstance = remember(buffer) { modelLoader.createModelInstance(buffer) }
                                if (modelInstance != null) {
                                    ModelNode(
                                        modelInstance = modelInstance,
                                        scale = Size(finalWidth, finalHeight),
                                        position = Float3(0f, 0.01f, 0f),
                                        rotation = Float3(-90f, 0f, 0f),
                                        autoAnimate = true
                                    )
                                }
                            }
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
            
            // Debug text for AR Image
            if (arMode == ARMode.Image && detectedImages.isEmpty()) {
                Box(modifier = Modifier.align(Alignment.Center).padding(16.dp)) {
                    Text("Searching for tracking target...", color = Color.White)
                }
            }
        } else {
            LibSceneView(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
            ) {
                val allMainBuffers = (if (modelUrl != null) listOf(modelUrl) else emptyList()) + modelUrls
                for (url in allMainBuffers) {
                    val buffer = modelBuffers[url]
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

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                color = Color(0xFFDAA520),
                trackColor = Color.Transparent
            )
        }
    }
}
