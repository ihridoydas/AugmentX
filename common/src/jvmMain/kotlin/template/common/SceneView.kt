package template.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import dev.datlag.kcef.KCEF
import java.io.File

private var kcefInitialized = false

@OptIn(dev.datlag.kcef.KCEFAcknowledge::class)
@Composable
actual fun SceneView(
    modifier: Modifier,
    modelUrl: String?
) {
    var initialized by remember { mutableStateOf(kcefInitialized) }
    var downloadProgress by remember { mutableStateOf(-1) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!initialized) {
            try {
                println("Initializing KCEF Engine (Stable Version)...")
                KCEF.init(
                    builder = {
                        installDir(File(System.getProperty("user.home"), ".augmentx-kcef-stable"))
                        addArgs("--no-sandbox")
                        addArgs("--enable-webgl")
                        
                        settings {
                            cachePath = File(System.getProperty("user.home"), ".augmentx-kcef-cache-stable").absolutePath
                        }
                        
                        progress {
                            onDownloading { progress ->
                                downloadProgress = progress.toInt()
                            }
                            onInitialized { 
                                kcefInitialized = true
                                initialized = true 
                            }
                        }
                    },
                    onError = { e ->
                        error = e?.message ?: "Engine Initialization Error"
                        println("KCEF Error: ${e?.message}")
                    },
                    onRestartRequired = {
                        error = "Restart required."
                        println("KCEF: Restart Required")
                    }
                )
            } catch (e: Exception) {
                if (e.message?.contains("already initialized") == true) {
                    kcefInitialized = true
                    initialized = true
                } else {
                    error = e.message
                    println("KCEF Exception: ${e.message}")
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (initialized && modelUrl != null) {
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <script type="module" src="https://ajax.googleapis.com/ajax/libs/model-viewer/3.5.0/model-viewer.min.js"></script>
                    <style>
                        body, html { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #111111; overflow: hidden; display: flex; justify-content: center; align-items: center; }
                        model-viewer { width: 100%; height: 100%; background-color: #000000; }
                    </style>
                </head>
                <body>
                    <model-viewer 
                        src="$modelUrl" 
                        auto-rotate 
                        camera-controls 
                        shadow-intensity="1" 
                        environment-image="neutral"
                        exposure="1.0"
                        alt="3D Model Viewer"
                        style="width: 100%; height: 100%;">
                    </model-viewer>
                </body>
                </html>
            """.trimIndent()
            
            val state = rememberWebViewStateWithHTMLData(html)
            WebView(state = state, modifier = Modifier.fillMaxSize())
        } else if (error != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Text("Viewer Error: $error", color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                Text("Please check the terminal for details.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFDAA520))
                Text(
                    text = if (downloadProgress >= 0) "Preparing Engine: ${downloadProgress}%" else "Starting 3D Viewer...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFDAA520),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
