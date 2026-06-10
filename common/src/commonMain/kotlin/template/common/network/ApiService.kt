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
package template.common.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

import template.common.util.PlatformUtils

@Serializable
data class Post(val userId: Int, val id: Int, val title: String, val body: String)

@Serializable
data class ManagedARItem(
    val id: String,
    val name: String,
    val targetImageUrl: String,
    val contentUrl: String,
    val mindUrl: String,
    val isVideo: Boolean,
    val createdAt: Long = 0
)

@Serializable
data class CompileResponse(val targetId: String, val mindUrl: String)

class ApiService(private val client: HttpClient) {
    // Use 127.0.0.1 explicitly to avoid localhost resolution issues (IPv4 vs IPv6)
    private val baseUrl = "http://127.0.0.1:8888"
    
    // Mock local list for UI persistence
    private val _managedItems = MutableStateFlow<List<ManagedARItem>>(emptyList())
    val managedItems: StateFlow<List<ManagedARItem>> = _managedItems.asStateFlow()

    suspend fun getPosts(): List<Post> = client.get("https://jsonplaceholder.typicode.com/posts").body()

    suspend fun compileMindAR(imageBlobUrl: String, contentBlobUrl: String, name: String? = null): CompileResponse {
        println("ApiService: Starting compilation for $name")
        
        // 1. Fetch local blobs using PlatformUtils (handles blob: URLs correctly on Web)
        val imageBytes = try {
            println("ApiService: Reading image bytes from $imageBlobUrl")
            PlatformUtils.readBytes(imageBlobUrl)
        } catch (e: Exception) {
            println("ApiService: ERROR reading image: ${e.message}")
            throw e
        }

        val contentBytes = try {
            println("ApiService: Reading content bytes from $contentBlobUrl")
            PlatformUtils.readBytes(contentBlobUrl)
        } catch (e: Exception) {
            println("ApiService: ERROR reading content: ${e.message}")
            throw e
        }

        println("ApiService: Submitting multipart form to $baseUrl/compile")
        return try {
            val response: CompileResponse = client.post("$baseUrl/compile") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("name", name ?: "Unnamed")
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"target.jpg\"")
                        })
                        append("content", contentBytes, Headers.build {
                            append(HttpHeaders.ContentType, "application/octet-stream")
                            append(HttpHeaders.ContentDisposition, "filename=\"content.data\"")
                        })
                    }
                ))
            }.body()

            println("ApiService: Successfully compiled! ID: ${response.targetId}")
            val newItem = ManagedARItem(
                id = response.targetId,
                name = name ?: "Unnamed Target",
                targetImageUrl = imageBlobUrl, 
                contentUrl = contentBlobUrl,
                mindUrl = response.mindUrl,
                isVideo = contentBlobUrl.contains(".mp4") || contentBlobUrl.startsWith("blob:video"),
                createdAt = 1700000000000
            )
            _managedItems.value = _managedItems.value + newItem
            response
        } catch (e: Exception) {
            println("ApiService: ERROR submitting form: ${e.message}")
            // Check for CORS or Network error specifically
            if (e.toString().contains("TypeError") || e.toString().contains("Fail to fetch")) {
                println("ApiService: DETECTED FETCH FAILURE. Likely CORS or Server Unreachable.")
                println("ApiService: Ensure backend is running at $baseUrl and CORS allows the frontend origin.")
            }
            throw e
        }
    }

    suspend fun updateMindAR(targetId: String, imageBlobUrl: String, contentBlobUrl: String, name: String): CompileResponse {
        // For simplicity, update just re-compiles and replaces in the local list
        val response = compileMindAR(imageBlobUrl, contentBlobUrl, name)
        deleteMindAR(targetId) 
        return response
    }

    suspend fun deleteMindAR(targetId: String) {
        try {
            client.delete("$baseUrl/uploads/$targetId")
        } catch (e: Exception) {
            // Log error
        }
        _managedItems.value = _managedItems.value.filter { it.id != targetId }
    }
}
