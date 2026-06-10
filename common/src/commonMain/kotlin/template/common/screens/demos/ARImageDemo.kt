package template.common.screens.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    
    // Derived purely from registry.json (managedItems)
    val imageTargetsMap = remember(managedItems) {
        managedItems.filter { !it.isVideo }.associate { it.mindUrl to it.contentUrl }
    }
    
    val primaryItem = remember(managedItems) {
        managedItems.firstOrNull { !it.isVideo }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        if (primaryItem != null) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                isAR = true,
                arMode = ARMode.Image,
                imageTargets = imageTargetsMap,
                trackingImage = primaryItem.mindUrl
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No 3D AR targets found. Add some in 'Manage' screen.", color = Color.White)
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

            if (primaryItem != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
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
                                text = "Point camera at target for: ${primaryItem.name}",
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
