package template.common.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap
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

    // Initialize state from existing item
    var targetName by remember(existingItem) { mutableStateOf(existingItem?.name ?: "") }
    val targetImageUrls = remember(existingItem) { 
        val list = mutableStateListOf<String>()
        existingItem?.let { list.add(it.targetImageUrl) }
        list
    }
    var contentUrl by remember(existingItem) { mutableStateOf(existingItem?.contentUrl) }
    var isVideo by remember(existingItem) { mutableStateOf(existingItem?.isVideo ?: false) }
    var isCompiling by remember { mutableStateOf(false) }
    var showAR by remember { mutableStateOf(false) }
    var targetId by remember(existingItem) { mutableStateOf(editId ?: existingItem?.id) }
    var compiledMindUrl by remember(existingItem) { mutableStateOf(existingItem?.mindUrl) }

    var exposure by remember { mutableStateOf(1.0f) }
    var scale by remember { mutableStateOf(1.0f) }
    var liveText by remember(targetName) { mutableStateOf(targetName) }
    var showControls by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    if (showAR && compiledMindUrl != null && contentUrl != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                isAR = true,
                arMode = ARMode.Image,
                trackingImage = if (PlatformUtils.isWeb) compiledMindUrl else targetImageUrls.firstOrNull(),
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
                // Android-specific: Direct URL input
                OutlinedTextField(
                    value = targetImageUrls.firstOrNull() ?: "",
                    onValueChange = { 
                        targetImageUrls.clear()
                        targetImageUrls.add(it)
                    },
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
                // WEB: Ported directly from ARCompilerScreen
                Text("1. Tracking Images", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable {
                            PlatformUtils.pickFile("image/*") { url -> targetImageUrls.add(url) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (targetImageUrls.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("Click to add images", fontWeight = FontWeight.Medium)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(100.dp),
                            modifier = Modifier.heightIn(max = 400.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(targetImageUrls) { index, url ->
                                ARCreatorImagePreviewCard(url) { targetImageUrls.removeAt(index) }
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .clickable { PlatformUtils.pickFile("image/*") { url -> targetImageUrls.add(url) } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Step 2: Content
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ARCreatorStepItem(
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
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Tracking Data Ready", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(compiledMindUrl!!, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                        IconButton(onClick = { 
                            PlatformUtils.downloadFile(compiledMindUrl!!, "${targetName.ifBlank { "target" }}.mind") 
                        }) {
                            Icon(Icons.Default.CloudUpload, "Download")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (compiledMindUrl != null) {
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
                                    // 1. Compile Locally (Same as Working Compiler Screen)
                                    println("ARCreator: Starting final compilation...")
                                    val finalLocalMindUrl = PlatformUtils.compileMindAR(targetImageUrls.toList())
                                    
                                    if (finalLocalMindUrl.isBlank()) {
                                        throw Exception("Compilation failed - No tracking data generated.")
                                    }

                                    // 2. Upload to Backend
                                    println("ARCreator: Uploading with Mind URL: $finalLocalMindUrl")
                                    val response = if (targetId == null) {
                                        apiService.compileMindAR(targetImageUrls.first(), contentUrl!!, targetName, isVideo, finalLocalMindUrl)
                                    } else {
                                        apiService.updateMindAR(targetId!!, targetImageUrls.first(), contentUrl!!, targetName, isVideo, finalLocalMindUrl)
                                    }
                                    
                                    targetId = response.targetId
                                    compiledMindUrl = response.mindUrl
                                    
                                    // 3. Trigger Download (Verification) - USE LOCAL BLOB for guaranteed naming
                                    PlatformUtils.downloadFile(finalLocalMindUrl, "${targetName.ifBlank { "target" }}.mind")
                                    snackbarHostState.showSnackbar("Compilation Finished & Downloaded!")
                                } else {
                                    // Android Local Room Save
                                    val newItem = ManagedARItem(
                                        id = targetId ?: PlatformUtils.generateId(),
                                        name = targetName,
                                        targetImageUrl = targetImageUrls.firstOrNull() ?: "",
                                        contentUrl = contentUrl!!,
                                        mindUrl = compiledMindUrl ?: "", 
                                        isVideo = isVideo,
                                        createdAt = 0L,
                                        imageUploaded = true,
                                        contentUploaded = true,
                                        mindGenerated = false
                                    )
                                    localDataSource.insertItem(newItem)
                                    targetId = newItem.id
                                    apiService.saveAndroidTarget(newItem)
                                    snackbarHostState.showSnackbar("Target Saved Successfully!")
                                }
                            } catch (e: Throwable) {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            } finally {
                                isCompiling = false
                            }
                        }
                    },
                    enabled = targetImageUrls.isNotEmpty() && contentUrl != null && targetName.isNotBlank() && !isCompiling,
                    modifier = Modifier.weight(1.5f).height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCompiling) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Build, null)
                        Spacer(Modifier.width(12.dp))
                        Text(if (targetId == null) "Start Compilation" else "Update & Compile")
                    }
                }
            }
        }
    }
}

@Composable
fun ARCreatorImagePreviewCard(url: String, onRemove: () -> Unit) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        try {
            val bytes = PlatformUtils.readBytes(url)
            bitmap = bytes.decodeToImageBitmap()
        } catch (e: Exception) { /* Ignore */ }
    }
    Box(modifier = Modifier.size(100.dp)) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.2f)))
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ARCreatorStepItem(title: String, description: String, isDone: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .border(2.dp, if (isDone) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = if (isDone) MaterialTheme.colorScheme.primary else Color.Unspecified)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(imageVector = if (isDone) Icons.Default.Check else Icons.Default.Add, contentDescription = null, tint = if (isDone) MaterialTheme.colorScheme.primary else Color.LightGray)
        }
    }
}
