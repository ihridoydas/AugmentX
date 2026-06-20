package template.common.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import template.common.components.AppBar
import template.common.util.PlatformUtils

@Composable
fun ARCompilerScreen(onBack: () -> Unit) {
    var imageUrls = remember { mutableStateListOf<String>() }
    var isCompiling by remember { mutableStateOf(false) }
    var compiledMindUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppBar(
                title = "MindAR Compiler",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Image Targets Compiler",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Select one or more images to generate a .mind tracking file.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Image Grid / Drop Zone area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable {
                        PlatformUtils.pickFile("image/*") { url -> imageUrls.add(url) }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUrls.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Click to add images", fontWeight = FontWeight.Medium)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(imageUrls) { index, url ->
                            ImagePreviewCard(url) {
                                imageUrls.removeAt(index)
                                compiledMindUrl = null // Reset if list changes
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .clickable { PlatformUtils.pickFile("image/*") { url -> imageUrls.add(url) } },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
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
                            Text("Compilation Success!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("The .mind file is ready for download.", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { PlatformUtils.downloadFile(compiledMindUrl!!, "targets.mind") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Download")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { imageUrls.clear(); compiledMindUrl = null },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = imageUrls.isNotEmpty()
                ) {
                    Text("Clear All")
                }

                Button(
                    onClick = {
                        isCompiling = true
                        scope.launch {
                            try {
                                compiledMindUrl = PlatformUtils.compileMindAR(imageUrls.toList())
                                snackbarHostState.showSnackbar("Compilation Finished!")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            } finally {
                                isCompiling = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1.5f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = imageUrls.isNotEmpty() && !isCompiling
                ) {
                    if (isCompiling) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Build, null)
                        Spacer(Modifier.width(12.dp))
                        Text("Start Compilation")
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePreviewCard(url: String, onRemove: () -> Unit) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(url) {
        try {
            val bytes = PlatformUtils.readBytes(url)
            bitmap = bytes.decodeToImageBitmap()
        } catch (e: Exception) {
            println("Preview Error: ${e.message}")
        }
    }

    Box(modifier = Modifier.size(100.dp)) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(20.dp))
            }
        }
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}
