package template.common.screens.demos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import template.common.SceneView
import template.common.components.AppBar

@Composable
fun CollisionDemo(onBack: () -> Unit) {
    var collisionDetected by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                title = "Collision Detection",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (collisionDetected) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("COLLISION DETECTED!", modifier = Modifier.padding(8.dp), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }

                Button(onClick = { collisionDetected = !collisionDetected }) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Spawn Collision Object")
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
                    Text(
                        text = "Real-time intersection testing between 3D volumes.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
