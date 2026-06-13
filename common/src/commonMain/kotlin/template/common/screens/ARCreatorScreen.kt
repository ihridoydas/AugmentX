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
import template.common.database.ARLocalDataSource
import template.common.network.ApiService
import template.common.network.ManagedARItem
import template.common.util.PlatformUtils

@Composable
fun ARCreatorScreen(editId: String? = null, onBack: () -> Unit) {
    val apiService: ApiService = koinInject()
    val localDataSource: ARLocalDataSource = koinInject()
    val managedItems by apiService.managedItems.collectAsState()
    val localItems by localDataSource.getAllItems().collectAsState(initial = emptyList())
    
    val combinedItems = remember(managedItems, localItems) { managedItems + localItems }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    val existingItem = remember(editId, combinedItems) { 
        combinedItems.find { it.id == editId }
    }

    var targetName by remember { mutableStateOf(existingItem?.name ?: "") }
    var targetImageUrl by remember { mutableStateOf<String?>(existingItem?.targetImageUrl) }
    var contentUrl by remember { mutableStateOf<String?>(existingItem?.contentUrl) }
    var isVideo by remember { mutableStateOf(existingItem?.isVideo ?: false) }
    var isCompiling by remember { mutableStateOf(false) }
    var showAR by remember { mutableStateOf(false) }
    var targetId by remember { mutableStateOf(existingItem?.id) }
    var compiledMindUrl by remember { mutableStateOf(existingItem?.mindUrl) }

    var exposure by remember { mutableStateOf(1.0f) }
    var scale by remember { mutableStateOf(1.0f) }
    var liveText by remember { mutableStateOf("") }
    var showControls by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(existingItem) {
        existingItem?.let {
            targetName = it.name
            targetImageUrl = it.targetImageUrl
            contentUrl = it.contentUrl
            isVideo = it.isVideo
            targetId = it.id
            compiledMindUrl = it.mindUrl
            liveText = it.name
        }
    }

    if (showAR && compiledMindUrl != null && contentUrl != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                isAR = true,
                arMode = ARMode.Image,
                trackingImage = if (PlatformUtils.isWeb) compiledMindUrl else targetImageUrl,
                videoUrl = if (isVideo) contentUrl else null,
                modelUrl = if (!isVideo) contentUrl else null,
                exposure = exposure,
                scale = scale,
                textContent = liveText.ifBlank { null },
                onModelLoaded = { /* Ready */ }
            )
            
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.5f))) {
                    AppBar(
                        title = "Live AR: $targetName",
                        navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        onNav = { showAR = false }
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (showControls) {
                    Card(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Real-time Controls", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                IconButton(onClick = { showControls = false }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Minimize", tint = Color.White)
                                }
                            }
                            
                            Text("Scale: ${((scale * 10).toInt() / 10.0)}", color = Color.White, fontSize = 12.sp)
                            Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.1f..3.0f)

                            Text("Exposure: ${((exposure * 10).toInt() / 10.0)}", color = Color.White, fontSize = 12.sp)
                            Slider(value = exposure, onValueChange = { exposure = it }, valueRange = 0.1f..2.0f)

                            OutlinedTextField(
                                value = liveText,
                                onValueChange = { liveText = it },
                                label = { Text("Overlay Text", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color.White
                                )
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                        FloatingActionButton(onClick = { showControls = true }, containerColor = MaterialTheme.colorScheme.primary) {
                            Icon(Icons.Default.Add, contentDescription = "Show Controls")
                        }
                    }
                }
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

            if (!PlatformUtils.isWeb) {
                // Android-specific: Direct URL inputs
                OutlinedTextField(
                    value = targetImageUrl ?: "",
                    onValueChange = { targetImageUrl = it },
                    label = { Text("Tracking Image URL (JPG/PNG)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("https://example.com/target.jpg") }
                )

                OutlinedTextField(
                    value = contentUrl ?: "",
                    onValueChange = {
                        contentUrl = it
                        isVideo = it.lowercase().endsWith(".mp4")
                    },
                    label = { Text("AR Content URL (GLB/MP4)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("https://example.com/model.glb") }
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isVideo, onCheckedChange = { isVideo = it })
                    Text("Is Video Content?")
                }
            } else {
                // Web/Default: File Pickers
                // Step 1: Target Image
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CreatorStep(
                        title = "1. Select Tracking Image",
                        description = "JPG/PNG image to be tracked.",
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
                            PlatformUtils.pickFile(".glb,.mp4,video/*") { url -> 
                                contentUrl = url
                                isVideo = url.contains("video", ignoreCase = true) || 
                                         url.contains(".mp4", ignoreCase = true)
                            }
                        }
                    )
                    if (contentUrl != null) {
                        Text(text = if (isVideo) "Video ready" else "3D Model ready", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (compiledMindUrl != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Current .mind File:", fontWeight = FontWeight.Bold)
                        Text(compiledMindUrl!!, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isCompiling) {
                CircularProgressIndicator()
                val loadingText = if (PlatformUtils.isWeb) {
                    if (targetId == null) "Creating .mind file..." else "Updating .mind file..."
                } else {
                    if (targetId == null) "Saving to database..." else "Updating database..."
                }
                Text(loadingText, fontSize = 14.sp)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (compiledMindUrl != null || (!PlatformUtils.isWeb && targetImageUrl != null && contentUrl != null)) {
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
                                    if (PlatformUtils.isWeb) {
                                        val response = if (targetId == null) {
                                            apiService.compileMindAR(targetImageUrl!!, contentUrl!!, targetName, isVideo)
                                        } else {
                                            apiService.updateMindAR(targetId!!, targetImageUrl!!, contentUrl!!, targetName, isVideo)
                                        }
                                        targetId = response.targetId
                                        compiledMindUrl = response.mindUrl
                                    } else {
                                        // Android Local Room Save
                                        val newItem = ManagedARItem(
                                            id = targetId ?: PlatformUtils.generateId(),
                                            name = targetName,
                                            targetImageUrl = targetImageUrl!!,
                                            contentUrl = contentUrl!!,
                                            mindUrl = compiledMindUrl ?: "", 
                                            isVideo = isVideo,
                                            createdAt = 0L,
                                            imageUploaded = true,
                                            contentUploaded = true,
                                            mindGenerated = false // Mind files not needed for Native Android
                                        )
                                        localDataSource.insertItem(newItem)
                                        targetId = newItem.id
                                        
                                        // Save to backend registry_android.json
                                        try {
                                            apiService.saveAndroidTarget(newItem)
                                        } catch (e: Exception) {
                                            println("ARCreator: Backend save failed, but saved locally: ${e.message}")
                                        }
                                    }
                                    snackbarHostState.showSnackbar(
                                        message = if (editId == null) "Target created successfully!" else "Target updated successfully!",
                                        duration = SnackbarDuration.Short
                                    )
                                } catch (e: Throwable) {
                                    println("ARCreator: Error during save: ${e.message}")
                                    snackbarHostState.showSnackbar(
                                        message = "Error: ${e.message ?: "Failed to process request"}",
                                        duration = SnackbarDuration.Long
                                    )
                                } finally {
                                    isCompiling = false
                                }
                            }
                        },
                        enabled = targetImageUrl != null && contentUrl != null && targetName.isNotBlank(),
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val icon = if (targetId == null) {
                            if (PlatformUtils.isWeb) Icons.Default.CloudUpload else Icons.Default.Check
                        } else {
                            Icons.Default.Refresh
                        }
                        Icon(icon, null)
                        Spacer(Modifier.width(12.dp))
                        val buttonText = if (PlatformUtils.isWeb) {
                            if (targetId == null) "Create & Compile" else "Update & Compile"
                        } else {
                            if (targetId == null) "Save to Database" else "Update Target"
                        }
                        Text(buttonText)
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
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, if (isDone) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(16.dp))
            .clickable { onClick() },
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
            Icon(
                imageVector = if (isDone) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                tint = if (isDone) MaterialTheme.colorScheme.primary else Color.LightGray
            )
        }
    }
}
