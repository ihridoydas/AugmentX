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
        isVideo: Boolean = false
    ): CompileResponse {
        println("ApiService: Starting compilation for $name (isVideo=$isVideo)")
        
        // 1. Determine extensions
        val imageExt = if (imageBlobUrl.contains(".png", ignoreCase = true)) "png" else "jpg"
        val contentExt = if (isVideo) "mp4" else {
            if (contentBlobUrl.contains(".glb", ignoreCase = true)) "glb" else "glb" // Default to glb for models
        }

        // 2. Fetch local blobs using PlatformUtils
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
                        append("isVideo", isVideo.toString())
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

            println("ApiService: Successfully compiled! ID: ${response.targetId}")
            
            // Refresh the list from the server to get the permanent URLs
            refreshTargets()
            
            response
        } catch (e: Exception) {
            println("ApiService: ERROR submitting form: ${e.message}")
            if (e.toString().contains("TypeError") || e.toString().contains("Fail to fetch")) {
                println("ApiService: DETECTED FETCH FAILURE. Likely CORS or Server Unreachable.")
            }
            throw e
        }
    }

    suspend fun updateMindAR(targetId: String, imageBlobUrl: String, contentBlobUrl: String, name: String, isVideo: Boolean): CompileResponse {
        val response = compileMindAR(imageBlobUrl, contentBlobUrl, name, isVideo)
        deleteMindAR(targetId) 
        return response
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
