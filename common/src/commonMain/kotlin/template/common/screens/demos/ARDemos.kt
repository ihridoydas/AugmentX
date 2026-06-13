package template.common.screens.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import template.common.ARMode
import template.common.SceneView
import template.common.components.AppBar

@Composable
fun ARCloudAnchorDemo(onBack: () -> Unit) {
    var isSyncing by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("AR Cloud Anchor", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true, modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb")
            
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Syncing with cloud...", color = Color.White)
                }
                
                Button(
                    onClick = { isSyncing = !isSyncing },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isSyncing) "Stop Sync" else "Host Cloud Anchor")
                }
                
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Text(
                        "Host and resolve anchors that persist across sessions and devices.",
                        color = Color.White, modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ARStreetscapeDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("AR Streetscape", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true)
            
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Streetscape Geometry", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Recognizing buildings and terrain for realistic urban occlusion.", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun ARPoseDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("AR Pose Tracking", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true)
            
            Card(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Device Pose", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("X: 0.00", color = Color.White.copy(alpha = 0.7f))
                    Text("Y: 0.00", color = Color.White.copy(alpha = 0.7f))
                    Text("Z: 0.00", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun ARRerunDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("AR Rerun", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true)
            
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                Text(
                    "Replaying a recorded AR session for debugging and analysis.",
                    color = Color.White, modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ARRecordPlaybackDemo(onBack: () -> Unit) {
    var isRecording by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Record & Playback", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true)
            
            FloatingActionButton(
                onClick = { isRecording = !isRecording },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord, null)
            }

            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                Text(
                    "Record your AR experience as an MP4 file with camera tracking data.",
                    color = Color.White, modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ARTerrainAnchorDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("AR Terrain Anchor", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true)
            Text("Geospatial anchors accurately placed on terrain.", Modifier.align(Alignment.BottomCenter).padding(24.dp), color = Color.White)
        }
    }
}

@Composable
fun ARRooftopAnchorDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("AR Rooftop Anchor", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true)
            Text("Geospatial anchors locked to building rooftops.", Modifier.align(Alignment.BottomCenter).padding(24.dp), color = Color.White)
        }
    }
}

@Composable
fun ARImageStabilizationDemo(onBack: () -> Unit) {
    var stabilizationLevel by remember { mutableStateOf(0.5f) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("Image Stabilization", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true, arMode = ARMode.Image, trackingImage = "images/cute.jpeg")
            
            Column(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Stabilization Strength", color = Color.White)
                        Slider(value = stabilizationLevel, onValueChange = { stabilizationLevel = it })
                    }
                }
            }
        }
    }
}

@Composable
fun OrbitalARDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AppBar("AR Orbital View", Icons.AutoMirrored.Filled.ArrowBack, onBack) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(modifier = Modifier.fillMaxSize(), isAR = true, modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb")
            Text("Rotating camera around a fixed point in AR.", Modifier.align(Alignment.BottomCenter).padding(24.dp), color = Color.White)
        }
    }
}
