/*
* MIT License
*
* Copyright (c) 2026 Hridoy Chandra Das
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
package template.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import template.common.screens.HomeScreen
import template.common.screens.ViewScreen
import template.common.screens.DemoScreen
import template.navigation.Navigator
import template.navigation.ScreenDestinations
import template.theme.components.SpatialWrapper
import androidx.compose.runtime.DisposableEffect

@Composable
fun MainAnimationNavHost(onBackPressedRegister: ((() -> Unit) -> Unit)? = null) {
    val backStack = remember { mutableStateListOf<ScreenDestinations>(ScreenDestinations.HomeScreen) }
    val interceptors = remember { mutableStateListOf<() -> Boolean>() }

    val navigator = remember {
        object : Navigator {
            override fun navigate(route: ScreenDestinations) {
                backStack.add(route)
            }

            override fun goBack() {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
            }
        }
    }

    LaunchedEffect(onBackPressedRegister) {
        onBackPressedRegister?.invoke {
            // Check interceptors in reverse order (last added first)
            val handled = interceptors.toList().reversed().any { it() }
            if (!handled) {
                navigator.goBack()
            }
        }
    }

    SpatialWrapper {
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            NavDisplay(
                backStack = backStack,
                onBack = { 
                    val handled = interceptors.toList().reversed().any { it() }
                    if (!handled) navigator.goBack() 
                },
            ) { key ->
                when (key) {
                    ScreenDestinations.HomeScreen -> NavEntry(key) {
                        HomeScreen(navigator = navigator)
                    }

                    ScreenDestinations.ViewScreen -> NavEntry(key) {
                        ViewScreen(
                            onBackPress = { navigator.goBack() },
                            onBackIntercept = { interceptor ->
                                DisposableEffect(interceptor) {
                                    interceptors.add(interceptor)
                                    onDispose { interceptors.remove(interceptor) }
                                }
                            }
                        )
                    }

                    is ScreenDestinations.DemoScreen -> NavEntry(key) {
                        DemoScreen(
                            id = key.id,
                            navigator = navigator,
                        )
                    }

                    ScreenDestinations.ARCreator -> NavEntry(key) {
                        template.common.screens.ARCreatorScreen(
                            onBack = { navigator.goBack() }
                        )
                    }
                }
            }
        }
    }
}
