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
fun TextDemo(onBack: () -> Unit) {
    var textValue by remember { mutableStateOf("AugmentX 3D") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "3D Text & Labels",
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
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("3D Text Content", color = Color.White) },
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Text(
                        text = "Real-time rendering of dynamic text labels in 3D space.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
