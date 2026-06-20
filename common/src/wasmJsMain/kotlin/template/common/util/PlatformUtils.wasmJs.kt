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

import kotlin.js.Promise

actual object PlatformUtils {
    actual val isWeb: Boolean = true

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

    actual fun pickFile(allowedTypes: String, onPicked: (String) -> Unit) {
        val input = document.createElement("input") as org.w3c.dom.HTMLInputElement
        input.type = "file"
        input.accept = allowedTypes
        input.onchange = {
            val file = input.files?.item(0)
            if (file != null) {
                val url = org.w3c.dom.url.URL.createObjectURL(file)
                // Append original filename and type as a fragment hint for type detection
                val hint = "#filename=${file.name}&type=${file.type}"
                onPicked(url + hint)
            }
        }
        input.click()
    }

    actual fun generateId(): String = (0..1000000).random().toString() // Simple fallback for now

    actual suspend fun readBytes(url: String): ByteArray {
        val cleanUrl = url.split("#")[0]
        println("PlatformUtils: Reading bytes from $cleanUrl")
        return try {
            val jsUint8Array = callReadBlobAsUint8Array(cleanUrl).await<Uint8Array>()
            val len = jsUint8Array.length
            val bytes = ByteArray(len)
            for (i in 0 until len) {
                bytes[i] = jsUint8Array[i]
            }
            println("PlatformUtils: READ SUCCESS: $len bytes from $cleanUrl")
            bytes
        } catch (e: Exception) {
            println("PlatformUtils: READ FAILED for $cleanUrl: ${e.message}")
            ByteArray(0)
        }
    }

    actual suspend fun compileMindAR(imageUrls: List<String>): String {
        println("PlatformUtils: Requesting JS compilation for ${imageUrls.size} images")
        return try {
            val jsArray = jsArrayOf()
            imageUrls.forEachIndexed { index, url -> 
                 jsArraySet(jsArray, index, url.toJsString())
            }
            
            val jsObj = callCompileMindAR(jsArray).await()
            val result = (jsObj as JsString).toString()
            println("PlatformUtils: Compilation result: $result")
            result
        } catch (e: Exception) {
            println("PlatformUtils: Compilation FAILED: ${e.message}")
            throw e
        }
    }

    actual fun downloadFile(url: String, fileName: String) {
        triggerDownload(url, fileName)
    }
}

@JsFun("(arr, index, value) => { arr[index] = value; }")
external fun jsArraySet(arr: JsArray<JsString>, index: Int, value: JsString)

@JsFun("() => []")
external fun jsArrayOf(): JsArray<JsString>

@JsFun("(url) => fetch(url).then(r => r.arrayBuffer()).then(b => new Uint8Array(b))")
external fun callReadBlobAsUint8Array(url: String): Promise<Uint8Array>

@JsFun("(imageUrls) => window.compileMindAR(imageUrls)")
external fun callCompileMindAR(imageUrls: JsArray<JsString>): Promise<JsAny>

@JsFun("(url, name) => { " +
    "if (url.startsWith('blob:')) {" +
    "  const a = document.createElement('a'); a.href = url; a.download = name; a.click();" +
    "} else {" +
    "  fetch(url).then(r => r.blob()).then(b => {" +
    "    const a = document.createElement('a'); a.href = URL.createObjectURL(b); a.download = name; a.click();" +
    "  });" +
    "}" +
"}")
external fun triggerDownload(url: String, name: String)

@JsFun("() => { window.location.href = window.location.origin; }")
external fun triggerHardReset()

