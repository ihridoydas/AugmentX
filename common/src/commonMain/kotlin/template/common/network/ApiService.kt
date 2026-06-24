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
    private val baseUrl = "http://127.0.0.1:8888"
    
    private val _managedItems = MutableStateFlow<List<ManagedARItem>>(emptyList())
    val managedItems: StateFlow<List<ManagedARItem>> = _managedItems.asStateFlow()

    private val _androidManagedItems = MutableStateFlow<List<ManagedARItem>>(emptyList())
    val androidManagedItems: StateFlow<List<ManagedARItem>> = _androidManagedItems.asStateFlow()

    init {
        // Automatically fetch items when the service is created
        refreshTargets()
    }

    fun refreshTargets() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                println("ApiService: Refreshing targets from backend...")
                val items: List<ManagedARItem> = client.get("$baseUrl/targets").body()
                _managedItems.value = items
                
                val androidItems: List<ManagedARItem> = client.get("$baseUrl/targets/android").body()
                _androidManagedItems.value = androidItems
            } catch (e: Exception) {
                println("ApiService: Failed to fetch targets: ${e.message}")
            }
        }
    }

    suspend fun saveAndroidTarget(item: ManagedARItem) {
        try {
            client.post("$baseUrl/save_android") {
                contentType(ContentType.Application.Json)
                setBody(item)
            }
            refreshTargets()
        } catch (e: Exception) {
            println("ApiService: Failed to save android target: ${e.message}")
            throw e
        }
    }

    suspend fun getPosts(): List<Post> = client.get("https://jsonplaceholder.typicode.com/posts").body()

    suspend fun compileMindAR(
        imageBlobUrl: String, 
        contentBlobUrl: String, 
        name: String? = null,
        isVideo: Boolean = false,
        mindBlobUrl: String? = null,
        targetId: String? = null
    ): CompileResponse {
        println("ApiService: Starting upload prep for $name")
        
        // 1. Fetch all binary data first
        val imageBytes = PlatformUtils.readBytes(imageBlobUrl)
        val contentBytes = PlatformUtils.readBytes(contentBlobUrl)
        val mindBytes = mindBlobUrl?.let { PlatformUtils.readBytes(it) }
        
        println("ApiService: Payloads - Image: ${imageBytes.size}, Content: ${contentBytes.size}, Mind: ${mindBytes?.size ?: 0}")

        if (imageBytes.isEmpty() || contentBytes.isEmpty()) {
            throw Exception("Required assets (image/content) are empty.")
        }

        // 2. Determine extensions
        val imageExt = if (imageBlobUrl.contains(".png", ignoreCase = true)) "png" else "jpg"
        val contentExt = if (isVideo) "mp4" else "glb"

        println("ApiService: Posting to $baseUrl/compile")
        return try {
            val response: CompileResponse = client.post("$baseUrl/compile") {
                setBody(MultiPartFormDataContent(
                    formData {
                        // 1. Metadata first (Crucial for Ktor server side)
                        if (!targetId.isNullOrBlank()) {
                            println("ApiService: Sending ID for update: $targetId")
                            append("id", targetId)
                        }
                        append("name", name ?: "Unnamed")
                        append("isVideo", isVideo.toString())
                        
                        // 2. Binary blobs with explicit content types
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/$imageExt")
                            append(HttpHeaders.ContentDisposition, "filename=\"target.$imageExt\"")
                        })
                        append("content", contentBytes, Headers.build {
                            append(HttpHeaders.ContentType, if (isVideo) "video/mp4" else "application/octet-stream")
                            append(HttpHeaders.ContentDisposition, "filename=\"content.$contentExt\"")
                        })
                        
                        if (mindBytes != null && mindBytes.isNotEmpty()) {
                            println("ApiService: Sending valid MIND part (${mindBytes.size} bytes)")
                            append("mind", mindBytes, Headers.build {
                                append(HttpHeaders.ContentType, "application/octet-stream")
                                append(HttpHeaders.ContentDisposition, "filename=\"target.mind\"")
                            })
                        }
                    }
                ))
            }.body()
            
            refreshTargets()
            response
        } catch (e: Exception) {
            println("ApiService: NETWORK ERROR: ${e.message}")
            throw e
        }
    }

    suspend fun updateMindAR(targetId: String, imageBlobUrl: String, contentBlobUrl: String, name: String, isVideo: Boolean, mindBlobUrl: String? = null): CompileResponse {
        // Pass the targetId to reuse it on the backend
        return compileMindAR(imageBlobUrl, contentBlobUrl, name, isVideo, mindBlobUrl, targetId)
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
