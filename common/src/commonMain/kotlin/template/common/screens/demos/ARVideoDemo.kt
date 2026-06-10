package template.common.screens.demos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

    // Derive only from the registry
    val videoItems = remember(managedItems) {
        managedItems.filter { it.isVideo }
    }
    
    // Track all videos as image targets if MindAR supports multiple video layers,
    // otherwise we pick the primary one. 
    val primaryVideo = videoItems.firstOrNull()

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
                SceneView(
                    modifier = Modifier.fillMaxSize(),
                    videoUrl = primaryVideo.contentUrl,
                    isAR = true,
                    arMode = ARMode.Image,
                    trackingImage = primaryVideo.mindUrl
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
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
                                text = "Point at image to play: ${primaryVideo.name}",
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
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Video AR targets found. Add some in 'Manage' screen.", color = Color.White)
                }
            }
        }
    }
}
