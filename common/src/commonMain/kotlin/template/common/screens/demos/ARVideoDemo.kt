package template.common.screens.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import template.common.ARMode
import template.common.SceneView
import template.common.components.AppBar
import template.common.network.ApiService
import template.common.network.ManagedARItem

@Composable
fun ARVideoDemo(onBack: () -> Unit) {
    val apiService: ApiService = koinInject()
    val managedItems by apiService.managedItems.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    
    val videoItems = remember(managedItems) { 
        val filtered = managedItems.filter { it.isVideo && it.mindUrl.isNotEmpty() && it.contentUrl.isNotEmpty() }
        println("ARVideoDemo: Found ${filtered.size} video items (Total: ${managedItems.size})")
        filtered
    }
    
    var selectedItem by remember { mutableStateOf<ManagedARItem?>(null) }
    
    LaunchedEffect(managedItems) {
        if (managedItems.isNotEmpty() || apiService.managedItems.value.isEmpty()) {
            delay(500)
            isLoading = false
        }
    }
    
    LaunchedEffect(videoItems) {
        if (selectedItem == null && videoItems.isNotEmpty()) {
            selectedItem = videoItems.first()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(if (selectedItem == null) Color(0xFF121212) else Color.Transparent)) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (selectedItem != null) {
            key(selectedItem?.id) {
                SceneView(
                    modifier = Modifier.fillMaxSize(),
                    isAR = true,
                    arMode = ARMode.Image,
                    trackingImage = selectedItem?.mindUrl,
                    videoUrl = selectedItem?.contentUrl
                )
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Videocam, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Text("No Video AR Targets found.\nCreate some in Manage screen!", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }

        // Overlay UI - Use a Box with children to avoid blocking clicks in the center
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f)).align(Alignment.TopCenter)) {
                AppBar(
                    title = "AR Video Player",
                    navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNav = onBack,
                )
            }
            
            if (videoItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                ) {
                    Column(Modifier.padding(vertical = 12.dp)) {
                        Text(
                            "Select Video Target:",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(videoItems) { item ->
                                val isSelected = selectedItem?.id == item.id
                                Surface(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedItem = item },
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            item.name, 
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            "Video: ${item.id.take(4)}",
                                            color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
