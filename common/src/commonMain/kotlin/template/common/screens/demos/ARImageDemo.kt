package template.common.screens.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import template.common.ARMode
import template.common.SceneView
import template.common.components.AppBar

@Composable
fun ARImageDemo(onBack: () -> Unit) {
    var showGuide by remember { mutableStateOf(true) }
    
    // Define image-to-model mapping
    val imageTargetsMap = mapOf(
        "images/earth.jpg" to "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb",
        "images/cute.jpeg" to "https://modelviewer.dev/shared-assets/models/Astronaut.glb"
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            AppBar(
                title = "AR Image Mapping",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                isAR = true,
                arMode = ARMode.Image,
                imageTargets = imageTargetsMap
            )

            // "What to scan" guide
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .clickable { showGuide = !showGuide },
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                if (showGuide) {
                    Column(
                        modifier = Modifier.padding(16.dp).width(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Point at this image",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Reference Preview
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Cute Chibi", color = Color.DarkGray, fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "The Astronaut will appear on top of the cute.jpeg character.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show target guide", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Bottom Instructions
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "Ensure the image is well-lit and move the camera slowly.",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
