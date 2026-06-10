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
@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("MatchingDeclarationName")

package template.common.util

import kotlin.js.ExperimentalWasmJsInterop
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.ArrayBuffer

actual object PlatformUtils {
    actual fun changeLanguage(code: String) {
        val lang = if (code.isEmpty()) "en" else code
        document.documentElement?.setAttribute("lang", lang)
        println("PlatformUtils Web: Language set to '$lang'")
    }

    actual fun changeTheme(isDark: Boolean) {
        println("PlatformUtils Web: changeTheme to isDark=$isDark")
    }

    actual fun hardReset() {
        println("PlatformUtils Web: Hard Resetting...")
        triggerHardReset()
    }

    actual fun pickFile(allowedTypes: String, onPicked: (url: String, name: String) -> Unit) {
        val input = document.createElement("input") as org.w3c.dom.HTMLInputElement
        input.type = "file"
        input.accept = allowedTypes
        input.onchange = {
            val file = input.files?.item(0)
            if (file != null) {
                val url = org.w3c.dom.url.URL.createObjectURL(file)
                onPicked(url, file.name)
            }
        }
        input.click()
    }

    actual suspend fun readBytes(url: String): ByteArray {
        val response = window.fetch(url).await()
        val buffer = response.arrayBuffer().await()
        val uint8Array = Uint8Array(buffer)
        return ByteArray(uint8Array.length) { i -> uint8Array[i] }
    }
}

@JsFun("() => { window.location.href = window.location.origin; }")
external fun triggerHardReset()

