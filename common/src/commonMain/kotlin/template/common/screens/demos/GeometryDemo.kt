package template.common.screens.demos

import androidx.compose.foundation.layout.*
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
fun GeometryDemo(onBack: () -> Unit) {
    var selectedShape by remember { mutableStateOf("Box") }
    val shapes = listOf("Box", "Sphere", "Cylinder")
    
    var scale by remember { mutableStateOf(1.0f) }
    var exposure by remember { mutableStateOf(1.0f) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                title = "3D Geometry",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = when (selectedShape) {
                    "Sphere" -> "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/MetalRoughSpheres/glTF-Binary/MetalRoughSpheres.glb"
                    "Cylinder" -> "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb" // Placeholder
                    else -> "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
                },
                scale = scale,
                exposure = exposure
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            shapes.forEach { shape ->
                                FilterChip(
                                    selected = selectedShape == shape,
                                    onClick = { selectedShape = shape },
                                    label = { Text(shape) },
                                    colors = FilterChipDefaults.filterChipColors(labelColor = Color.White)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text("Scale: ${((scale * 10).toInt() / 10.0)}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.1f..3.0f)
                        
                        Text("Exposure: ${((exposure * 10).toInt() / 10.0)}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Slider(value = exposure, onValueChange = { exposure = it }, valueRange = 0.1f..2.0f)
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
                    Text(
                        text = "Procedural generation and primitive shape management.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
