package template.common.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.koin.compose.koinInject
import template.common.components.AppBar
import template.common.network.ApiService
import template.common.network.ManagedARItem
import template.common.util.PlatformUtils
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ARManageScreen(onBack: () -> Unit, onEdit: (ManagedARItem) -> Unit, onAdd: () -> Unit) {
    val apiService: ApiService = koinInject()
    val managedItems by apiService.managedItems.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppBar(
                title = "Manage AR Targets",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Add New")
            }
        }
    ) { padding ->
        if (managedItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.CloudOff, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp), 
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Text(
                        "No AR targets found", 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Button(onClick = onAdd) {
                        Text("Create Your First Target")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(managedItems, key = { it.id }) { item ->
                    ManagedItemCard(
                        item = item,
                        onEdit = { onEdit(item) },
                        onDelete = {
                            scope.launch {
                                apiService.deleteMindAR(item.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManagedItemCard(item: ManagedARItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    var imageBitmap by remember(item.targetImageUrl) { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(item.targetImageUrl) {
        try {
            val bytes = PlatformUtils.readBytes(item.targetImageUrl)
            imageBitmap = bytes.decodeToImageBitmap()
        } catch (e: Exception) {
            println("Error loading image for card: ${e.message}")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.name, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuggestionChip(
                        onClick = {},
                        label = { 
                            Text(
                                if (item.isVideo) "Video AR" else "3D AR",
                                fontSize = 10.sp
                            ) 
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ID: ${item.id.take(8)}...", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row {
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }
}
