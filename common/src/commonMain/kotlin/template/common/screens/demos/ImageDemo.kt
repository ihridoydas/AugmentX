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
fun ImageDemo(onBack: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "3D Image Plane",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb", 
                scale = scale
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Image Scale: ${((scale * 10).toInt() / 10f)}", color = Color.White)
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            valueRange = 0.1f..3f
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = "Rendering 2D textures as dynamic 3D planes.",
                    color = Color.White,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).padding(8.dp)
                )
            }
        }
    }
}
