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
import org.jetbrains.compose.resources.stringResource
import template.common.SceneView
import template.common.components.AppBar
import template.common.generated.resources.Res
import template.common.generated.resources.category_environment

@Composable
fun LightingDemo(onBack: () -> Unit) {
    var exposure by remember { mutableStateOf(1f) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                title = "Scene Lighting",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/DamagedHelmet/glTF-Binary/DamagedHelmet.glb",
                exposure = exposure
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth()
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Environment Exposure: ${((exposure * 10).toInt() / 10f)}", color = Color.White)
                        Slider(
                            value = exposure,
                            onValueChange = { exposure = it },
                            valueRange = 0.1f..5f
                        )
                    }
                }
            }
        }
    }
}
