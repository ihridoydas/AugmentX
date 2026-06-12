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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import template.common.ARMode
import template.common.SceneView
import template.common.components.AppBar

@Composable
fun ARImageDemo(onBack: () -> Unit) {
    var showGuide by remember { mutableStateOf(false) }
    
    // Define image-to-model mapping
    val imageTargetsMap = mapOf(
        "images/cute.jpeg" to "https://modelviewer.dev/shared-assets/models/Astronaut.glb"
    )
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            isAR = true,
            arMode = ARMode.Image,
            imageTargets = imageTargetsMap,
            trackingImage = "images/cute.jpeg"
        )

        // Overlay UI
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f))) {
                AppBar(
                    title = "AR Image Mapping",
                    navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNav = onBack,
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))

            // Simplified Bottom Guide
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Chibi", color = Color.White, fontSize = 10.sp)
                            }
                            Text(
                                text = "Point at cute.jpeg to see the Astronaut",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
