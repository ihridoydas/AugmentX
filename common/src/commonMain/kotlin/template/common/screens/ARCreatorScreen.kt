package template.common.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import template.common.ARMode
import template.common.SceneView
import template.common.components.AppBar
import template.common.network.ApiService
import template.common.util.PlatformUtils

@Composable
fun ARCreatorScreen(editId: String? = null, onBack: () -> Unit) {
    val apiService: ApiService = koinInject()
    val managedItems by apiService.managedItems.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val existingItem = remember(editId, managedItems) { 
        managedItems.find { it.id == editId } 
    }

    var targetName by remember { mutableStateOf(existingItem?.name ?: "") }
    var targetImageUrl by remember { mutableStateOf<String?>(existingItem?.targetImageUrl) }
    var contentUrl by remember { mutableStateOf<String?>(existingItem?.contentUrl) }
    var isVideo by remember { mutableStateOf(existingItem?.isVideo ?: false) }
    var isCompiling by remember { mutableStateOf(false) }
    var showAR by remember { mutableStateOf(false) }
    var targetId by remember { mutableStateOf(existingItem?.id) }
    var compiledMindUrl by remember { mutableStateOf(existingItem?.mindUrl) }
    var localMindBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isGeneratingMind by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val activeMindUrl = compiledMindUrl ?: (if (localMindBytes != null) "In-App Generated" else null)

    LaunchedEffect(existingItem) {
        existingItem?.let {
            targetName = it.name
            targetImageUrl = it.targetImageUrl
            contentUrl = it.contentUrl
            isVideo = it.isVideo
            targetId = it.id
            compiledMindUrl = it.mindUrl
        }
    }

    if (showAR && (localMindBytes != null || compiledMindUrl != null) && contentUrl != null) {
        val trackingUrl = remember(localMindBytes, compiledMindUrl) {
            localMindBytes?.let { 
                val url = PlatformUtils.createUrlFromBytes(it)
                println("ARCreator: Using local blob for testing: $url")
                url
            } ?: compiledMindUrl!!
        }

        Box(modifier = Modifier.fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                isAR = true,
                arMode = ARMode.Image,
                trackingImage = trackingUrl,
                videoUrl = if (isVideo) contentUrl else null,
                modelUrl = if (!isVideo) contentUrl else null,
                onModelLoaded = { /* Ready */ }
            )
            
            Box(modifier = Modifier.align(Alignment.TopCenter).background(Color.Black.copy(alpha = 0.5f))) {
                AppBar(
                    title = "Live AR: $targetName",
                    navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNav = { showAR = false }
                )
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppBar(
                title = if (targetId == null) "Create AR Target" else "Update AR Target",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (targetId == null) "New AR Experience" else "Updating: $targetId",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = targetName,
                onValueChange = { targetName = it },
                label = { Text("Target Name (e.g. My Poster)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Step 1: Target Image
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CreatorStep(
                    title = "1. Select Tracking Image",
                    description = "JPG/PNG image to be tracked. (The 'poster' for your content)",
                    isDone = targetImageUrl != null,
                    onClick = {
                        PlatformUtils.pickFile("image/*") { url -> targetImageUrl = url }
                    }
                )
                if (targetImageUrl != null) {
                    Text("Image ready for upload", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Step 2: Content
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CreatorStep(
                    title = "2. Select AR Content",
                    description = "GLB model or MP4 video.",
                    isDone = contentUrl != null,
                    onClick = {
                        PlatformUtils.pickFile(".glb,.mp4,.mov,video/*") { url -> 
                            contentUrl = url
                            isVideo = url.contains("video", ignoreCase = true) || 
                                     url.contains(".mp4", ignoreCase = true) ||
                                     url.contains(".mov", ignoreCase = true)
                        }
                    }
                )
                if (contentUrl != null) {
                    Text(text = if (isVideo) "Video ready" else "3D Model ready", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Step 3: Generate Mind File In-App
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CreatorStep(
                    title = "3. Generate Tracking Data",
                    description = "Create the .mind file inside the app.",
                    isDone = localMindBytes != null,
                    isLoading = isGeneratingMind,
                    onClick = {
                        if (targetImageUrl != null) {
                            isGeneratingMind = true
                            scope.launch {
                                try {
                                    localMindBytes = PlatformUtils.compileImage(targetImageUrl!!)
                                    snackbarHostState.showSnackbar("Tracking data generated successfully!")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Generation failed: ${e.message}")
                                } finally {
                                    isGeneratingMind = false
                                }
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Select a tracking image first!") }
                        }
                    }
                )
                if (localMindBytes != null) {
                    Text("In-app mind file ready for upload", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (compiledMindUrl != null && localMindBytes == null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Existing Server .mind File:", fontWeight = FontWeight.Bold)
                        Text(compiledMindUrl!!, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isCompiling) {
                CircularProgressIndicator()
                Text(if (targetId == null) "Creating target..." else "Updating target...", fontSize = 14.sp)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (activeMindUrl != null) {
                        OutlinedButton(
                            onClick = { showAR = true },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Test Live")
                        }
                    }
                    
                    Button(
                        onClick = {
                            isCompiling = true
                            scope.launch {
                                try {
                                    val response = if (targetId == null) {
                                        apiService.compileMindAR(targetImageUrl!!, contentUrl!!, targetName, isVideo, mindBytes = localMindBytes)
                                    } else {
                                        apiService.updateMindAR(targetId!!, targetImageUrl!!, contentUrl!!, targetName, isVideo, mindBytes = localMindBytes)
                                    }
                                    targetId = response.targetId
                                    compiledMindUrl = response.mindUrl
                                    localMindBytes = null // Reset after successful upload
                                    snackbarHostState.showSnackbar(
                                        message = if (editId == null) "Target created successfully!" else "Target updated successfully!",
                                        duration = SnackbarDuration.Short
                                    )
                                } catch (e: Throwable) {
                                    println("ARCreator: Error during upload: ${e.message}")
                                    snackbarHostState.showSnackbar(
                                        message = "Error: ${e.message ?: "Failed to upload"}",
                                        duration = SnackbarDuration.Long
                                    )
                                } finally {
                                    isCompiling = false
                                }
                            }
                        },
                        enabled = targetImageUrl != null && contentUrl != null && targetName.isNotBlank() && (localMindBytes != null || compiledMindUrl != null),
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(if (targetId == null) Icons.Default.CloudUpload else Icons.Default.Refresh, null)
                        Spacer(Modifier.width(12.dp))
                        Text(if (targetId == null) "Create & Compile" else "Update & Compile")
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorStep(
    title: String,
    description: String,
    isDone: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, if (isDone) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(16.dp))
            .clickable(enabled = !isLoading) { onClick() },
        color = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = if (isDone) MaterialTheme.colorScheme.primary else Color.Unspecified)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = if (isDone) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isDone) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            }
        }
    }
}
