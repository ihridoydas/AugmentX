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
import template.common.SceneView
import template.common.components.AppBar

@Composable
fun ARPlacementDemo(onBack: () -> Unit) {
    var showGuide by remember { mutableStateOf(true) }
    var scale by remember { mutableStateOf(0.5f) }
    var exposure by remember { mutableStateOf(1.0f) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Transparent for Web camera visibility
        topBar = {
            AppBar(
                title = "AR Placement",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
                isAR = true,
                scale = scale,
                exposure = exposure,
                textContent = "Astronaut AR"
            )

            Column(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth()) {
                // Controls Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Scale: ${((scale * 10).toInt() / 10.0)}", color = Color.White)
                        Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.1f..2.0f)
                        
                        Text("Exposure: ${((exposure * 10).toInt() / 10.0)}", color = Color.White)
                        Slider(value = exposure, onValueChange = { exposure = it }, valueRange = 0.1f..2.0f)
                    }
                }

                // Simplified Bottom Guide
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
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
                                text = "Scan the floor and tap to place the model",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "Tap for placement guide",
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
