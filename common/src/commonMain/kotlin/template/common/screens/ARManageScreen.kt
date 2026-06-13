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
import template.common.database.ARLocalDataSource
import template.common.util.PlatformUtils
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape

@Composable
fun ARManageScreen(
    onBack: () -> Unit, 
    onEdit: (ManagedARItem) -> Unit, 
    onAdd: () -> Unit,
    onView: (ManagedARItem) -> Unit
) {
    val apiService: ApiService = koinInject()
    val localDataSource: ARLocalDataSource = koinInject()
    
    val managedItems by apiService.managedItems.collectAsState()
    val localItems by localDataSource.getAllItems().collectAsState(initial = emptyList())
    
    // Distinguish between local and remote for the UI
    val combinedItems = remember(managedItems, localItems) { 
        (managedItems.map { it to false } + localItems.map { it to true })
            .distinctBy { it.first.id }
            .sortedByDescending { it.first.createdAt }
    }
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppBar(
                title = "AR Management",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add New", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        if (combinedItems.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, 
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh, 
                                contentDescription = null, 
                                modifier = Modifier.size(60.dp), 
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Your AR Library is Empty", 
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Create your first augmented reality experience by uploading a target image and content.", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    
                    Button(
                        onClick = onAdd,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(56.dp).fillMaxWidth(0.7f)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Create AR Target")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(combinedItems, key = { it.first.id }) { (item, isLocal) ->
                    ManagedItemCard(
                        item = item,
                        isLocal = isLocal,
                        onEdit = { onEdit(item) },
                        onView = { onView(item) },
                        onDelete = {
                            scope.launch {
                                if (isLocal) localDataSource.deleteItem(item.id)
                                else apiService.deleteMindAR(item.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManagedItemCard(
    item: ManagedARItem, 
    isLocal: Boolean, 
    onEdit: () -> Unit, 
    onView: () -> Unit,
    onDelete: () -> Unit
) {
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // Header Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // Overlay Badges
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLocal) Icons.Default.Storage else Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isLocal) "Local" else "Cloud",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (item.isVideo) "VIDEO AR" else "3D MODEL AR",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = item.name, 
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusBadge(label = "Tracking", isSuccess = item.imageUploaded)
                    StatusBadge(label = "Content", isSuccess = item.contentUploaded)
                    if (!PlatformUtils.isWeb || !isLocal) {
                         StatusBadge(label = "Compiled", isSuccess = item.mindGenerated)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ID: ${item.id.take(8)}", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Row {
                        FilledIconButton(
                            onClick = onView,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, "View", modifier = Modifier.size(20.dp))
                        }

                        Spacer(Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = onEdit,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        
                        FilledIconButton(
                            onClick = onDelete,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, isSuccess: Boolean) {
    Surface(
        color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}
