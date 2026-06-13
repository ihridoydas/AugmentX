package template.common.screens

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
import template.common.ARMode
import template.common.SceneView
import template.common.components.AppBar

@Composable
fun ARViewerScreen(
    trackingImage: String,
    modelUrl: String? = null,
    videoUrl: String? = null,
    isVideo: Boolean = false,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.4f))) {
                AppBar(
                    title = "AR Experience",
                    navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNav = onBack,
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            SceneView(
                modifier = Modifier.fillMaxSize(),
                isAR = true,
                arMode = ARMode.Image,
                trackingImage = trackingImage,
                modelUrl = if (!isVideo) modelUrl else null,
                videoUrl = if (isVideo) videoUrl else null,
                onModelLoaded = { /* Ready */ }
            )

            // Guidance Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Point your camera at the tracking image to see the ${if (isVideo) "video" else "3D model"}.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
