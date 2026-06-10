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
    val createdAt: Long = 0
)

@Serializable
data class CompileResponse(val targetId: String, val mindUrl: String)

class ApiService(private val client: HttpClient) {
    private val baseUrl = "http://127.0.0.1:8888"
    
    private val _managedItems = MutableStateFlow<List<ManagedARItem>>(emptyList())
    val managedItems: StateFlow<List<ManagedARItem>> = _managedItems.asStateFlow()

    init {
        refreshTargets()
    }

    fun refreshTargets() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                println("ApiService: Fetching targets from $baseUrl/targets ...")
                val response = client.get("$baseUrl/targets")
                if (response.status == HttpStatusCode.OK) {
                    val items: List<ManagedARItem> = response.body()
                    _managedItems.value = items
                }
            } catch (e: Exception) {
                println("ApiService: Failed to fetch targets: ${e.message}")
            }
        }
    }

    suspend fun compileMindAR(imageBlobUrl: String, contentBlobUrl: String, isVideo: Boolean, name: String? = null): CompileResponse {
        println("ApiService: Starting compilation for $name (isVideo: $isVideo)")
        
        val imageBytes = PlatformUtils.readBytes(imageBlobUrl)
        val contentBytes = PlatformUtils.readBytes(contentBlobUrl)

        return try {
            val response: CompileResponse = client.post("$baseUrl/compile") {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("name", name ?: "Unnamed")
                        append("isVideo", isVideo.toString())
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"target.jpg\"")
                        })
                        append("content", contentBytes, Headers.build {
                            append(HttpHeaders.ContentType, if (isVideo) "video/mp4" else "model/gltf-binary")
                            append(HttpHeaders.ContentDisposition, "filename=\"content.${if (isVideo) "mp4" else "glb"}\"")
                        })
                    }
                ))
            }.body()

            refreshTargets()
            response
        } catch (e: Exception) {
            println("ApiService: ERROR submitting form: ${e.message}")
            throw e
        }
    }

    suspend fun updateMindAR(targetId: String, imageBlobUrl: String, contentBlobUrl: String, isVideo: Boolean, name: String): CompileResponse {
        val response = compileMindAR(imageBlobUrl, contentBlobUrl, isVideo, name)
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
