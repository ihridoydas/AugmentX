package template.backend

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import java.io.File
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

fun main() {
    embeddedServer(Netty, port = 8888, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        
        install(CORS) {
            anyHost()
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.Accept)
            allowHeader("X-Requested-With")
            allowNonSimpleContentTypes = true
            allowCredentials = true
            maxAgeInSeconds = 3600
        }

        routing {
            val uploadDir = File("backend/uploads")
            if (!uploadDir.exists()) uploadDir.mkdirs()

            val registryFile = File(uploadDir, "registry.json")
            val jsonSerializer = Json { prettyPrint = true; ignoreUnknownKeys = true }

            fun loadRegistry(): MutableList<ManagedARItem> {
                return if (registryFile.exists()) {
                    try {
                        jsonSerializer.decodeFromString<List<ManagedARItem>>(registryFile.readText()).toMutableList()
                    } catch (e: Exception) {
                        mutableListOf()
                    }
                } else {
                    mutableListOf()
                }
            }

            fun saveRegistry(items: List<ManagedARItem>) {
                try {
                    registryFile.writeText(jsonSerializer.encodeToString(items))
                } catch (e: Exception) {
                    println("Backend: Error saving registry: ${e.message}")
                }
            }

            get("/") {
                call.respondText("AugmentX Backend is Running")
            }

            staticFiles("/uploads", uploadDir)

            get("/targets") {
                val items = loadRegistry()
                call.respond(items)
            }

            post("/compile") {
                println("Backend: POST /compile - Received request")
                try {
                    val multipart = call.receiveMultipart()
                    var targetName = "Unknown"
                    var targetId = UUID.randomUUID().toString()
                    var targetImageUrl = ""
                    var contentUrl = ""
                    var isVideoValue: Boolean? = null
                    
                    val baseUrl = "http://127.0.0.1:8888/uploads"

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                when (part.name) {
                                    "name" -> targetName = part.value
                                    "isVideo" -> isVideoValue = part.value.toBoolean()
                                }
                            }
                            is PartData.FileItem -> {
                                val originalName = part.originalFileName ?: "file"
                                val fileName = if (part.name == "content" && isVideoValue == true && !originalName.contains(".mp4", ignoreCase = true)) {
                                    "${targetId}_content.mp4"
                                } else if (part.name == "content" && isVideoValue == false && !originalName.contains(".glb", ignoreCase = true)) {
                                    "${targetId}_content.glb"
                                } else {
                                    "${targetId}_$originalName"
                                }
                                
                                val file = File(uploadDir, fileName)
                                part.streamProvider().use { input ->
                                    file.outputStream().buffered().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                if (part.name == "image") {
                                    targetImageUrl = "$baseUrl/$fileName"
                                } else if (part.name == "content") {
                                    contentUrl = "$baseUrl/$fileName"
                                    if (isVideoValue == null) {
                                        isVideoValue = fileName.contains(".mp4", ignoreCase = true) || 
                                                      part.contentType?.toString()?.contains("video") == true
                                    }
                                }
                                println("Backend: Saved file $fileName")
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    val isVideo = isVideoValue ?: false
                    val mindFileName = "${targetId}.mind"
                    val mindFile = File(uploadDir, mindFileName)
                    mindFile.writeText("MIND_FILE_CONTENT_FOR_$targetId")
                    val mindUrl = "$baseUrl/$mindFileName"

                    // Persist to registry
                    val items = loadRegistry()
                    val newItem = ManagedARItem(
                        id = targetId,
                        name = targetName,
                        targetImageUrl = targetImageUrl,
                        contentUrl = contentUrl,
                        mindUrl = mindUrl,
                        isVideo = isVideo,
                        createdAt = System.currentTimeMillis(),
                        imageUploaded = targetImageUrl.isNotEmpty(),
                        contentUploaded = contentUrl.isNotEmpty(),
                        mindGenerated = mindUrl.isNotEmpty()
                    )
                    items.add(newItem)
                    saveRegistry(items)

                    call.respond(CompileResponse(targetId = targetId, mindUrl = mindUrl))
                } catch (e: Exception) {
                    println("Backend: ERROR processing /compile: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown Error")
                }
            }

            delete("/uploads/{id}") {
                val id = call.parameters["id"]
                println("Backend: DELETE /uploads/$id")
                
                val items = loadRegistry()
                items.removeAll { it.id == id }
                saveRegistry(items)

                val files = uploadDir.listFiles { _, name -> name.startsWith(id ?: "") }
                files?.forEach { it.delete() }
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}
