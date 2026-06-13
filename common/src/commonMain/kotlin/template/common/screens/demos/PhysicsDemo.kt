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
fun PhysicsDemo(onBack: () -> Unit) {
    var gravityEnabled by remember { mutableStateOf(false) }
    var isSimulating by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                title = "3D Physics",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb",
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { gravityEnabled = !gravityEnabled }) {
                        Text(if (gravityEnabled) "Disable Gravity" else "Enable Gravity")
                    }
                    Button(onClick = { isSimulating = !isSimulating }) {
                        Text(if (isSimulating) "Reset" else "Apply Impulse")
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Rigid body physics with collision detection and gravity simulation.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
