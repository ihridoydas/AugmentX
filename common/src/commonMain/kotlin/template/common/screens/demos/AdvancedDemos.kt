package template.common.screens.demos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import template.common.SceneView
import template.common.components.AppBar

@Composable
fun LinesPathsDemo(onBack: () -> Unit) {
    var showPaths by remember { mutableStateOf(true) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Lines & Paths", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(), 
                modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb"
            )
            
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = showPaths, onCheckedChange = { showPaths = it })
                        Spacer(Modifier.width(12.dp))
                        Text("Visualize 3D Trajectory", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialsDemo(onBack: () -> Unit) {
    var selectedVariant by remember { mutableStateOf(0) }
    var exposure by remember { mutableStateOf(1.0f) }
    val variants = listOf("Default", "Golden", "Midnight", "Arctic")
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("PBR Materials", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(), 
                modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/MaterialsVariantsShoe/glTF-Binary/MaterialsVariantsShoe.glb",
                autoRotate = true,
                exposure = exposure
            )
            
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Environment Exposure", color = Color.White)
                        Slider(value = exposure, onValueChange = { exposure = it }, valueRange = 0f..3f)
                        
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.3f))

                        Text("Material Variants", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            variants.forEachIndexed { index, name ->
                                FilterChip(
                                    selected = selectedVariant == index,
                                    onClick = { selectedVariant = index },
                                    label = { Text(name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        labelColor = Color.White,
                                        selectedContainerColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoublePendulumDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Double Pendulum", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/BrainStem/glTF-Binary/BrainStem.glb")
            Text("Complex physics simulation: Double Pendulum.", Modifier.align(Alignment.BottomCenter).padding(24.dp))
        }
    }
}

@Composable
fun PostProcessingDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Post-Processing", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb")
            Text("Bloom, Tone Mapping, and Color Correction.", Modifier.align(Alignment.BottomCenter).padding(24.dp))
        }
    }
}

@Composable
fun CustomMeshDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Custom Mesh", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb")
            Text("Generating 3D geometry from code.", Modifier.align(Alignment.BottomCenter).padding(24.dp))
        }
    }
}

@Composable
fun ShapeDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Shape", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb")
            Text("Primitive shapes: Spheres, Cubes, and Cylinders.", Modifier.align(Alignment.BottomCenter).padding(24.dp))
        }
    }
}

@Composable
fun ReflectionProbesDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Reflection Probes", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/DamagedHelmet/glTF-Binary/DamagedHelmet.glb")
            Text("Real-time reflections using probes.", Modifier.align(Alignment.BottomCenter).padding(24.dp))
        }
    }
}

@Composable
fun SecondaryCameraDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Secondary Camera", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb")
            Text("Picture-in-picture or multiple viewports.", Modifier.align(Alignment.BottomCenter).padding(24.dp))
        }
    }
}

@Composable
fun DebugOverlayDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Debug Overlay", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb")
            Text("Scene stats, wireframes, and bounds.", Modifier.align(Alignment.BottomCenter).padding(24.dp))
        }
    }
}
