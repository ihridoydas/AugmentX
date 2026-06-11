package template.common.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val createdAt: Long = 0,
    val imageUploaded: Boolean = false,
    val contentUploaded: Boolean = false,
    val mindGenerated: Boolean = false
)

@Serializable
data class CompileResponse(val targetId: String, val mindUrl: String)

class ApiService(private val client: HttpClient) {
    private val baseUrl = "http://localhost:8888"
    
    private val _managedItems = MutableStateFlow<List<ManagedARItem>>(emptyList())
    val managedItems: StateFlow<List<ManagedARItem>> = _managedItems.asStateFlow()

    init {
        // Automatically fetch items when the service is created
        refreshTargets()
    }

    fun refreshTargets() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                println("ApiService: Refreshing targets from backend...")
                val items: List<ManagedARItem> = client.get("$baseUrl/targets").body()
                println("ApiService: Received ${items.size} items from server.")
                _managedItems.value = items
            } catch (e: Exception) {
                println("ApiService: Failed to fetch targets: ${e.message}")
            }
        }
    }

    suspend fun getPosts(): List<Post> = client.get("https://jsonplaceholder.typicode.com/posts").body()

    suspend fun compileMindAR(
        imageBlobUrl: String, 
        contentBlobUrl: String, 
        name: String? = null,
        isVideo: Boolean = false,
        targetId: String? = null
    ): CompileResponse {
        println("ApiService: Starting compilation for $name (isVideo=$isVideo, targetId=$targetId)")
        
        // 1. Determine extensions
        val imageExt = if (imageBlobUrl.contains(".png", ignoreCase = true)) "png" else "jpg"
        val contentExt = if (isVideo) "mp4" else "glb"

        // 2. Fetch local blobs using PlatformUtils
        val imageBytes = try { PlatformUtils.readBytes(imageBlobUrl) } catch (e: Exception) { throw e }
        val contentBytes = try { PlatformUtils.readBytes(contentBlobUrl) } catch (e: Exception) { throw e }

        println("ApiService: Submitting multipart form to $baseUrl/compile")
        return try {
            val response: CompileResponse = client.post("$baseUrl/compile") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("name", name ?: "Unnamed")
                        append("isVideo", isVideo.toString())
                        if (targetId != null) append("targetId", targetId)
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/$imageExt")
                            append(HttpHeaders.ContentDisposition, "filename=\"target.$imageExt\"")
                        })
                        append("content", contentBytes, Headers.build {
                            append(HttpHeaders.ContentType, if (isVideo) "video/mp4" else "application/octet-stream")
                            append(HttpHeaders.ContentDisposition, "filename=\"content.$contentExt\"")
                        })
                    }
                ))
            }.body()
            
            refreshTargets()
            response
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateMindAR(targetId: String, imageBlobUrl: String, contentBlobUrl: String, name: String, isVideo: Boolean): CompileResponse {
        return compileMindAR(imageBlobUrl, contentBlobUrl, name, isVideo, targetId)
    }

    suspend fun deleteMindAR(targetId: String) {
        try {
            client.delete("$baseUrl/uploads/$targetId")
            refreshTargets()
        } catch (e: Exception) {
            println("ApiService: Failed to delete: ${e.message}")
        }
    }
}
