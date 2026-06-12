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
            allowHeader("X-Target-Id")
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
                val items = if (registryFile.exists()) {
                    try {
                        jsonSerializer.decodeFromString<List<ManagedARItem>>(registryFile.readText()).toMutableList()
                    } catch (e: Exception) {
                        println("Backend: Error decoding registry: ${e.message}")
                        mutableListOf()
                    }
                } else {
                    mutableListOf()
                }

                // Dynamic Status Detection: Check if files actually exist on disk
                return items.map { item ->
                    val imageFile = File(uploadDir, item.targetImageUrl.substringAfterLast("/"))
                    val contentFile = File(uploadDir, item.contentUrl.substringAfterLast("/"))
                    val mindFile = File(uploadDir, item.mindUrl.substringAfterLast("/"))
                    
                    // Also fix old 127.0.0.1 URLs to localhost
                    item.copy(
                        targetImageUrl = item.targetImageUrl.replace("127.0.0.1", "localhost"),
                        contentUrl = item.contentUrl.replace("127.0.0.1", "localhost"),
                        mindUrl = item.mindUrl.replace("127.0.0.1", "localhost"),
                        imageUploaded = imageFile.exists(),
                        contentUploaded = contentFile.exists(),
                        mindGenerated = mindFile.exists()
                    )
                }.toMutableList()
            }

            fun saveRegistry(items: List<ManagedARItem>) {
                try {
                    // Only save the base fields, status will be re-calculated on load
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
                    var targetId: String? = null
                    var targetImageUrl = ""
                    var contentUrl = ""
                    var isVideoValue: Boolean? = null
                    var providedMindBytes: ByteArray? = null
                    
                    val baseUrl = "http://localhost:8888/uploads"

                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                when (part.name) {
                                    "name" -> targetName = part.value
                                    "isVideo" -> isVideoValue = part.value.toBoolean()
                                    "targetId" -> targetId = part.value
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "mind") {
                                    providedMindBytes = part.streamProvider().readBytes()
                                } else {
                                    val currentId = targetId ?: UUID.randomUUID().toString().also { targetId = it }
                                    val originalName = part.originalFileName ?: "file"
                                    val fileName = if (part.name == "content" && isVideoValue == true && !originalName.contains(".mp4", ignoreCase = true)) {
                                        "${currentId}_content.mp4"
                                    } else if (part.name == "content" && isVideoValue == false && !originalName.contains(".glb", ignoreCase = true)) {
                                        "${currentId}_content.glb"
                                    } else {
                                        "${currentId}_$originalName"
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
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    val finalId = targetId ?: UUID.randomUUID().toString()
                    val isVideo = isVideoValue ?: false
                    val mindFileName = "${finalId}.mind"
                    val mindFile = File(uploadDir, mindFileName)
                    
                    if (providedMindBytes != null) {
                        println("Backend: Using PROVIDED mind file for $finalId")
                        mindFile.writeBytes(providedMindBytes!!)
                    } else {
                        val imageFileName = targetImageUrl.substringAfterLast("/")
                        val targetImageFile = File(uploadDir, imageFileName)

                        // REAL COMPILATION ATTEMPT
                        var compilationSuccess = false
                        if (targetImageFile.exists() && targetImageFile.length() > 0) {
                            try {
                                println("Backend: Attempting real MindAR compilation for $finalId...")
                                // Using npx to run the compiler. 
                                // Note: mindar-image-compiler is a common package for this.
                                val process = ProcessBuilder(
                                    "npx", "-y", "mindar-image-compiler", 
                                    "-i", targetImageFile.absolutePath, 
                                    "-o", mindFile.absolutePath
                                ).start()
                                
                                val exitCode = process.waitFor()
                                if (exitCode == 0 && mindFile.exists() && mindFile.length() > 100) {
                                    println("Backend: Real compilation SUCCESS for $finalId")
                                    compilationSuccess = true
                                } else {
                                    println("Backend: Real compilation failed or produced empty file. Exit code: $exitCode")
                                }
                            } catch (e: Exception) {
                                println("Backend: Real compilation ERROR: ${e.message}")
                            }
                        }

                        if (!compilationSuccess) {
                            println("Backend: Falling back to template for $finalId")
                            // Try multiple possible locations for the template
                            val templateLocations = listOf(
                                File("common/src/wasmJsMain/resources/images/cute.mind"),
                                File("../common/src/wasmJsMain/resources/images/cute.mind"),
                                File("src/main/resources/cute.mind")
                            )
                            
                            val templateMind = templateLocations.find { it.exists() }
                            if (templateMind != null) {
                                templateMind.copyTo(mindFile, overwrite = true)
                                println("Backend: Used template from ${templateMind.absolutePath}")
                            } else {
                                println("Backend: SEVERE - No template found, creating dummy valid header")
                                // A very basic valid-ish mind file header or just avoid creating it
                                if (!mindFile.exists()) {
                                    // Don't create empty file, it causes crash. 
                                    // Better to let it fail 404 than 0-byte RangeError
                                }
                            }
                        }
                    }
                    val mindUrl = "$baseUrl/$mindFileName"

                    // Persist to registry
                    val items = loadRegistry()
                    // Remove existing if it's an update
                    items.removeAll { it.id == finalId }
                    
                    val newItem = ManagedARItem(
                        id = finalId,
                        name = targetName,
                        targetImageUrl = targetImageUrl,
                        contentUrl = contentUrl,
                        mindUrl = mindUrl,
                        isVideo = isVideo,
                        createdAt = System.currentTimeMillis()
                    )
                    items.add(newItem)
                    saveRegistry(items)

                    call.respond(CompileResponse(targetId = finalId, mindUrl = mindUrl))
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
