package template.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?,
    isAR: Boolean,
    autoRotate: Boolean,
    skyboxUrl: String?,
    onModelLoaded: () -> Unit
) {
    var isLoading by remember(modelUrl) { mutableStateOf(modelUrl != null) }
    
    if (modelUrl != null) {
        LaunchedEffect(modelUrl) {
            delay(1000) // Simulate loading delay for the placeholder
            isLoading = false
            onModelLoaded()
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                color = Color(0xFFDAA520),
                trackColor = Color.Transparent
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val scale = min(size.width, size.height) / 5

                val vertices = arrayOf(
                    floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, -1f, -1f),
                    floatArrayOf(1f, 1f, -1f), floatArrayOf(-1f, 1f, -1f),
                    floatArrayOf(-1f, -1f, 1f), floatArrayOf(1f, -1f, 1f),
                    floatArrayOf(1f, 1f, 1f), floatArrayOf(-1f, 1f, 1f)
                )

                val edges = arrayOf(
                    intArrayOf(0, 1), intArrayOf(1, 2), intArrayOf(2, 3), intArrayOf(3, 0),
                    intArrayOf(4, 5), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(7, 4),
                    intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7)
                )

                val projected = vertices.map { v ->
                    // Rotate around Y
                    var x = v[0] * cos(angle) - v[2] * sin(angle)
                    val y = v[1]
                    var z = v[0] * sin(angle) + v[2] * cos(angle)

                    // Rotate around X
                    val rotX = angle * 0.5f
                    val tempY = y * cos(rotX) - z * sin(rotX)
                    z = y * sin(rotX) + z * cos(rotX)
                    
                    // Perspective projection
                    val p = 4 / (z + 4)
                    Offset(centerX + x * p * scale, centerY + tempY * p * scale)
                }

                edges.forEach { edge ->
                    drawLine(
                        color = Color(0xFFDAA520),
                        start = projected[edge[0]],
                        end = projected[edge[1]],
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}
