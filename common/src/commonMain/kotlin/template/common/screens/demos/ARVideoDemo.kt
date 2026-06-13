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
import template.common.database.ARLocalDataSource
import template.common.network.ApiService
import template.common.util.PlatformUtils

@Composable
fun ARVideoDemo(onBack: () -> Unit) {
    val apiService: ApiService = koinInject()
    val localDataSource: ARLocalDataSource = koinInject()
    
    val managedItems by apiService.managedItems.collectAsState()
    val localItems by localDataSource.getAllItems().collectAsState(initial = emptyList())
    
    // Find the first available item that IS a video
    val sampleItem = remember(managedItems, localItems) {
        (managedItems + localItems).firstOrNull { it.isVideo }
    }

    var showGuide by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Transparent for Web camera visibility
        topBar = {
            AppBar(
                title = "AR Image Video",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (sampleItem != null) {
                SceneView(
                    modifier = Modifier.fillMaxSize(),
                    videoUrl = sampleItem.contentUrl,
                    isAR = true,
                    arMode = ARMode.Image,
                    trackingImage = if (PlatformUtils.isWeb) sampleItem.mindUrl else sampleItem.targetImageUrl
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Video AR target found in database.", color = Color.White)
                }
            }

            // Simplified Bottom Guide
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
                            text = "Point at the target image to play ${sampleItem?.name ?: "video"} ${if (PlatformUtils.isWeb) "(Web)" else "(Native)"}.",
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
    }
}
