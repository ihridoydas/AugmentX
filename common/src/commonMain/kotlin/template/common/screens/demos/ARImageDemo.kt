package template.common.screens.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import template.common.ARMode
import template.common.SceneView
import template.common.components.AppBar
import template.common.network.ApiService

@Composable
fun ARImageDemo(onBack: () -> Unit) {
    val apiService: ApiService = koinInject()
    val managedItems by apiService.managedItems.collectAsState()
    
    var showGuide by remember { mutableStateOf(false) }
    
    // Force a refresh from registry.json on enter
    LaunchedEffect(Unit) {
        apiService.refreshTargets()
    }
    
    val imageItems = remember(managedItems) { managedItems.filter { !it.isVideo } }
    var selectedIndex by remember(imageItems) { mutableIntStateOf(0) }
    
    val primaryItem = remember(imageItems, selectedIndex) {
        if (imageItems.isNotEmpty()) imageItems[selectedIndex.coerceIn(0, imageItems.lastIndex)] else null
    }

    val imageTargetsMap = remember(primaryItem) {
        if (primaryItem != null) mapOf(primaryItem.mindUrl to primaryItem.contentUrl) else emptyMap()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        if (primaryItem != null) {
            // Re-key SceneView so it restarts the MindAR engine for the new .mind file
            key(primaryItem.id) {
                SceneView(
                    modifier = Modifier.fillMaxSize(),
                    isAR = true,
                    arMode = ARMode.Image,
                    imageTargets = imageTargetsMap,
                    trackingImage = primaryItem.mindUrl
                )
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No 3D AR targets found in registry.json", color = Color.White)
            }
        }

        // Overlay UI
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f))) {
                AppBar(
                    title = "AR 3D Tracking",
                    navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNav = onBack,
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Smart Target Selector
                if (imageItems.size > 1) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .padding(horizontal = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        imageItems.forEachIndexed { index, item ->
                            FilterChip(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                label = { Text(item.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.Black.copy(alpha = 0.5f),
                                    labelColor = Color.White,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (primaryItem != null) {
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .clickable { showGuide = !showGuide },
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (showGuide) {
                                Text(
                                    text = "Scan target for: ${primaryItem.name}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    text = "Tap for tracking guide",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
