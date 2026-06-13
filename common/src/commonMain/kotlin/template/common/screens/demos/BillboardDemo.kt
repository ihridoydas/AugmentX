package template.common.screens.demos

import androidx.compose.foundation.background
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
fun BillboardDemo(onBack: () -> Unit) {
    var billboardEnabled by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                title = "Billboard Nodes",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
                billboard = billboardEnabled,
                textContent = if (billboardEnabled) "Billboard Node" else null
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(12.dp)
                ) {
                    Switch(checked = billboardEnabled, onCheckedChange = { billboardEnabled = it })
                    Spacer(Modifier.width(12.dp))
                    Text("Auto-face Camera", color = Color.White)
                }

                Spacer(Modifier.height(16.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Text(
                        text = "Billboard nodes automatically rotate to always face the camera.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
