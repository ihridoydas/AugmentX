package template.common

import android.graphics.Bitmap
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.google.ar.core.Session
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.SceneView as LibSceneView
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.node.VideoNode
import io.github.sceneview.node.ViewNode
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.arcore.rememberRuntimeAugmentedImageDatabase
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.SceneScope
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Size
import io.github.sceneview.ar.ActivityARPermissionHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

@Composable
fun SceneScope.ARModelInstance(
    modelLoader: ModelLoader, 
    buffer: ByteBuffer, 
    scale: Float = 0.5f,
    position: Position = Position(0f, 0f, 0f),
    rotation: Rotation = Rotation(0f, 0f, 0f),
    autoRotate: Boolean = false,
    animationSpeed: Float = 1.0f,
    textContent: String? = null,
    billboard: Boolean = false,
    cameraPosition: Position? = null
) {
    val modelInstance = remember(buffer) { modelLoader.createModelInstance(buffer) }
    if (modelInstance != null) {
        val nodeRotation = remember(rotation, billboard, cameraPosition, position) {
            if (billboard && cameraPosition != null) {
                // Calculate rotation to face camera (simplified)
                rotation // Fallback to original for now as calculating Euler from positions is complex here
            } else {
                rotation
            }
        }

        ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = scale,
            position = position,
            rotation = nodeRotation,
            autoAnimate = true
        ).apply {
            if (billboard) {
                // Real-time billboarding if supported by the library's node class
                // this.isBillboard = true
            }
        }
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
    exposure: Float,
    fogDensity: Float,
    animationSpeed: Float,
    textContent: String?,
    scale: Float,
    billboard: Boolean,
    onModelLoaded: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val client = remember { HttpClient(Android) }
    val scope = rememberCoroutineScope()

    val modelBuffers = remember { mutableStateMapOf<String, ByteBuffer>() }
    val imageBitmaps = remember { mutableStateMapOf<String, Bitmap>() }
    var skyboxBuffer by remember { mutableStateOf<ByteBuffer?>(null) }
    val placedAnchors = remember { mutableStateListOf<Anchor>() }
    var lastFrame by remember { mutableStateOf<Frame?>(null) }
    var cameraWorldPosition by remember { mutableStateOf<Position?>(null) }
    val detectedImages = remember { mutableStateMapOf<Int, AugmentedImage>() }
    val sessionState = remember { mutableStateOf<Session?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    
    val runtimeDatabase = rememberRuntimeAugmentedImageDatabase()

    val permissionHandler = remember(activity) {
        activity?.let { ActivityARPermissionHandler(it) }
    }

    LaunchedEffect(isAR) {
        if (isAR) {
            permissionHandler?.requestCameraPermission { granted ->
                if (!granted) {
                    android.util.Log.e("SceneView", "Camera permission denied")
                }
            }
        }
    }

    LaunchedEffect(modelUrl, modelUrls, imageTargets, trackingImage, skyboxUrl) {
        isLoading = true
        val allModelUrls = (if (modelUrl != null) listOf(modelUrl) else emptyList()) + 
                          modelUrls + 
                          imageTargets.values
        
        val allImagePaths = (if (trackingImage != null) listOf(trackingImage) else emptyList()) + 
                           imageTargets.keys

        // Load Skybox if provided
        if (skyboxUrl != null && skyboxUrl.isNotBlank()) {
            launch {
                try {
                    val bytes = client.get(skyboxUrl).readRawBytes()
                    skyboxBuffer = ByteBuffer.wrap(bytes)
                } catch (e: Exception) {
                    android.util.Log.e("SceneView", "Failed to load skybox", e)
                }
            }
        }

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

        // Load Image Bitmaps
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

    LaunchedEffect(sessionState.value, imageBitmaps.size) {
        val session = sessionState.value ?: return@LaunchedEffect
        imageBitmaps.forEach { (path, bitmap) ->
            if (!runtimeDatabase.imageNames.contains(path)) {
                runtimeDatabase.addImage(path, bitmap, 0.2f)
                android.util.Log.d("SceneView", "Image registered: $path")
            }
        }
    }

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
                onSessionCreated = { session ->
                    runtimeDatabase.bind(session)
                    sessionState.value = session
                },
                onSessionUpdated = { _, frame ->
                    lastFrame = frame
                    val cameraPose = frame.camera.displayOrientedPose
                    cameraWorldPosition = Position(cameraPose.tx(), cameraPose.ty(), cameraPose.tz())
                    
                    if (arMode == ARMode.Image) {
                        frame.getUpdatedTrackables(AugmentedImage::class.java).forEach { image ->
                            if (image.trackingState == TrackingState.TRACKING) {
                                if (!detectedImages.containsKey(image.index)) {
                                    detectedImages[image.index] = image
                                    android.util.Log.d("SceneView", "Image Detected: ${image.name}")
                                }
                            } else if (image.trackingState == TrackingState.STOPPED) {
                                detectedImages.remove(image.index)
                            }
                        }
                    }
                },
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
                val defaultModelBuffer = modelUrl?.let { modelBuffers[it] } 
                                       ?: modelUrls.firstOrNull()?.let { modelBuffers[it] }

                for (anchor in placedAnchors) {
                    AnchorNode(anchor = anchor) {
                        if (defaultModelBuffer != null) {
                            ARModelInstance(
                                modelLoader = modelLoader, 
                                buffer = defaultModelBuffer, 
                                scale = 0.5f * scale,
                                animationSpeed = animationSpeed,
                                textContent = textContent,
                                billboard = billboard,
                                cameraPosition = cameraWorldPosition
                            )
                        }
                    }
                }

                if (arMode == ARMode.Image) {
                    for (image in detectedImages.values) {
                        if (videoUrl != null && image.name == trackingImage) {
                            AugmentedImageNode(augmentedImage = image, applyImageScale = true) {
                                if (mediaPlayer != null) {
                                    VideoNode(
                                        player = mediaPlayer,
                                        position = Float3(0f, 0.01f, 0f),
                                        rotation = Float3(-90f, 0f, 0f),
                                        scale = Float3(scale, scale, scale)
                                    )
                                }
                            }
                        } else {
                            val targetModelUrl = imageTargets[image.name] ?: if (image.name == trackingImage) modelUrl else null
                            val buffer = targetModelUrl?.let { modelBuffers[it] }
                            if (buffer != null) {
                                AugmentedImageNode(augmentedImage = image, applyImageScale = true) {
                                    ARModelInstance(
                                        modelLoader = modelLoader, 
                                        buffer = buffer, 
                                        scale = 0.5f * scale,
                                        position = Position(0f, 0.01f, 0f),
                                        rotation = Rotation(-90f, 0f, 0f),
                                        animationSpeed = animationSpeed,
                                        textContent = textContent,
                                        billboard = billboard,
                                        cameraPosition = cameraWorldPosition
                                    )
                                }
                            }
                        }
                    }
                }

                if (mediaPlayer != null && arMode != ARMode.Image) {
                    VideoNode(
                        player = mediaPlayer,
                        position = Float3(0f, 0.01f, 0f),
                        rotation = Float3(-90f, 0f, 0f)
                    )
                }
            }
            
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
                        ARModelInstance(
                            modelLoader = modelLoader, 
                            buffer = buffer, 
                            scale = 1.0f * scale,
                            autoRotate = autoRotate,
                            animationSpeed = animationSpeed,
                            textContent = textContent,
                            billboard = billboard,
                            cameraPosition = cameraWorldPosition
                        )
                    }
                }

                if (mediaPlayer != null) {
                    VideoNode(
                        player = mediaPlayer,
                        position = Float3(0f, 0.01f, 0f),
                        rotation = Float3(-90f, 0f, 0f),
                        scale = Float3(scale, scale, scale)
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

        if (!textContent.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                contentAlignment = if (billboard) Alignment.Center else Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = textContent,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
