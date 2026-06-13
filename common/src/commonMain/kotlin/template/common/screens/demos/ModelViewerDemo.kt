package template.common.screens.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import template.common.SceneView
import template.common.components.AppBar

@Composable
fun ModelViewerDemo(onBack: () -> Unit) {
    var modelUrl by remember { mutableStateOf("https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/DamagedHelmet/glTF-Binary/DamagedHelmet.glb") }
    var inputUrl by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                title = "Model Viewer",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = modelUrl,
                autoRotate = true
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("Load Custom GLB URL", color = Color.White) },
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    trailingIcon = {
                        IconButton(onClick = { if (inputUrl.isNotBlank()) modelUrl = inputUrl }) {
                            Icon(Icons.Default.CloudDownload, null, tint = Color.White)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Text("Interactive viewer with orbit controls and auto-rotation.", color = Color.White, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
