package template.common.screens.demos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun SceneGalleryDemo(onBack: () -> Unit) {
    val models = listOf(
        "Astronaut" to "https://modelviewer.dev/shared-assets/models/Astronaut.glb",
        "Helmet" to "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/DamagedHelmet/glTF-Binary/DamagedHelmet.glb",
        "Box" to "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb",
        "Lantern" to "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Lantern/glTF-Binary/Lantern.glb"
    )
    var currentModelUrl by remember { mutableStateOf(models[0].second) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                title = "Scene Gallery",
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = onBack,
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                modelUrl = currentModelUrl,
                autoRotate = true
            )

            LazyRow(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(models) { (name, url) ->
                    Button(
                        onClick = { currentModelUrl = url },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentModelUrl == url) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(name)
                    }
                }
            }
        }
    }
}
