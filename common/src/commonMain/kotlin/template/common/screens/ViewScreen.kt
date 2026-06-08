/*
* MIT License
*
* Copyright (c) 2024 Hridoy Chandra Das
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*
*/
package template.common.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import template.common.SceneView
import template.common.components.AppBar
import template.common.generated.resources.Res
import template.common.generated.resources.welcome
import template.common.util.PlatformUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScreen(
    onBackPress: () -> Unit,
    onBackIntercept: @Composable (() -> Boolean) -> Unit = {}
) {
    var showExitDialog by remember { mutableStateOf(false) }

    // Register back interceptor
    onBackIntercept {
        if (!showExitDialog) {
            showExitDialog = true
            true // Handled
        } else {
            true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit AR Session?") },
            text = { Text("Do you want to stop the AR session and return to the home screen? (This will reload the app to ensure a clean state)") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        // Use hard reset for Web to clear MindAR/Camera locks
                        PlatformUtils.hardReset()
                        onBackPress()
                    }
                ) {
                    Text("Exit & Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Forced transparency
        topBar = {
            AppBar(
                title = stringResource(Res.string.welcome),
                navIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNav = { showExitDialog = true },
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
                SceneView(
                    modifier = Modifier.fillMaxSize(),
                    modelUrl = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/DamagedHelmet/glTF-Binary/DamagedHelmet.glb"
                )
            }
        },
    )
}
