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
fun CameraControlsDemo(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(title = "Camera Controls", navIcon = Icons.AutoMirrored.Filled.ArrowBack, onNav = onBack)
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
                autoRotate = true
            )
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                Text(
                    "Pinch to zoom, swipe to rotate, and drag with two fingers to pan.",
                    color = Color.White, modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
