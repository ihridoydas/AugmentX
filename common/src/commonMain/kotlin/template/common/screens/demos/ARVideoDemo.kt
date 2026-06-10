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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import template.common.ARMode
import template.common.SceneView
import template.common.components.AppBar
import template.common.network.ApiService

@Composable
fun ARVideoDemo(onBack: () -> Unit) {
    val apiService: ApiService = koinInject()
    val managedItems by apiService.managedItems.collectAsState()
    
    var showGuide by remember { mutableStateOf(true) }

    // Refresh targets when entering the screen
    LaunchedEffect(Unit) {
        apiService.refreshTargets()
    }

    val videoItems = remember(managedItems) { managedItems.filter { it.isVideo } }
    var selectedIndex by remember(videoItems) { mutableIntStateOf(0) }
    
    val primaryVideo = remember(videoItems, selectedIndex) {
        if (videoItems.isNotEmpty()) videoItems[selectedIndex.coerceIn(0, videoItems.lastIndex)] else null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, 
        topBar = {
            AppBar(
                title = "AR Video Tracking",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (primaryVideo != null) {
                key(primaryVideo.id) {
                    SceneView(
                        modifier = Modifier.fillMaxSize(),
                        videoUrl = primaryVideo.contentUrl,
                        isAR = true,
                        arMode = ARMode.Image,
                        trackingImage = primaryVideo.mindUrl
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Smart Video Selector
                    if (videoItems.size > 1) {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .padding(horizontal = 16.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            videoItems.forEachIndexed { index, item ->
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
                                    text = "Scan image to play: ${primaryVideo.name}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    text = "Tap for video guide",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Video AR targets found in registry.json", color = Color.White)
                }
            }
        }
    }
}
