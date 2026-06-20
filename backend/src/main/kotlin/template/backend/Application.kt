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
            val androidRegistryFile = File(uploadDir, "registry_android.json")
            val jsonSerializer = Json { prettyPrint = true; ignoreUnknownKeys = true }

            fun loadRegistry(file: File = registryFile): MutableList<ManagedARItem> {
                return if (file.exists()) {
                    try {
                        jsonSerializer.decodeFromString<List<ManagedARItem>>(file.readText()).toMutableList()
                    } catch (e: Exception) {
                        mutableListOf()
                    }
                } else {
                    mutableListOf()
                }
            }

            fun saveRegistry(items: List<ManagedARItem>, file: File = registryFile) {
                try {
                    file.writeText(jsonSerializer.encodeToString(items))
                } catch (e: Exception) {
                    println("Backend: Error saving registry ${file.name}: ${e.message}")
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

            get("/targets/android") {
                val items = loadRegistry(androidRegistryFile)
                call.respond(items)
            }

            post("/save_android") {
                try {
                    val item = call.receive<ManagedARItem>()
                    val items = loadRegistry(androidRegistryFile)
                    items.removeAll { it.id == item.id }
                    items.add(item)
                    saveRegistry(items, androidRegistryFile)
                    call.respond(HttpStatusCode.OK, "Saved to registry_android.json")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Error saving Android target")
                }
            }

            post("/compile") {
                println("\nBackend: POST /compile - NEW REQUEST")
                try {
                    val multipart = call.receiveMultipart()
                    var targetName = "Unknown"
                    var targetId = "" 
                    var targetImageUrl = ""
                    var contentUrl = ""
                    var isVideoValue: Boolean? = null
                    var customMindFile: File? = null
                    
                    val baseUrl = "http://127.0.0.1:8888/uploads"

                    var partCount = 0
                    multipart.forEachPart { part ->
                        partCount++
                        val pName = part.name ?: "unnamed_$partCount"
                        
                        when (part) {
                            is PartData.FormItem -> {
                                when (pName.lowercase()) {
                                    "id" -> targetId = part.value
                                    "name" -> targetName = part.value
                                    "isvideo" -> isVideoValue = part.value.toBoolean()
                                }
                            }
                            is PartData.FileItem -> {
                                // If targetId is still empty, generate a temporary one or wait for FormItem
                                // To be safe, we'll assign a UUID if none was provided in earlier parts
                                if (targetId.isEmpty()) targetId = UUID.randomUUID().toString()
                                
                                val originalName = part.originalFileName ?: "file"
                                val partKey = pName.lowercase()
                                
                                val fileName = when {
                                    partKey == "content" && isVideoValue == true -> "${targetId}_content.mp4"
                                    partKey == "content" && isVideoValue == false -> "${targetId}_content.glb"
                                    partKey == "mind" || originalName.endsWith(".mind") -> "${targetId}_target.mind"
                                    else -> "${targetId}_$originalName"
                                }
                                
                                val file = File(uploadDir, fileName)
                                part.streamProvider().use { input ->
                                    file.outputStream().buffered().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                
                                when (partKey) {
                                    "image" -> targetImageUrl = "$baseUrl/$fileName"
                                    "content" -> {
                                        contentUrl = "$baseUrl/$fileName"
                                        if (isVideoValue == null) {
                                            isVideoValue = fileName.contains(".mp4", ignoreCase = true) || 
                                                          part.contentType?.toString()?.contains("video") == true
                                        }
                                    }
                                    "mind" -> {
                                        if (file.length() > 100) {
                                           customMindFile = file
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    // Final ID check
                    if (targetId.isEmpty()) targetId = UUID.randomUUID().toString()

                    val isVideo = isVideoValue ?: false
                    
                    val mindFile = if (customMindFile != null && customMindFile.exists() && customMindFile.length() > 500) {
                        println("Backend: Finalizing with REAL mind file: ${customMindFile.name} (${customMindFile.length()} bytes)")
                        customMindFile
                    } else {
                        val errorMsg = if (customMindFile == null) "MIND part missing" else "MIND data empty (${customMindFile.length()} bytes)"
                        println("Backend: CRITICAL - $errorMsg. Terminating request.")
                        throw Exception(errorMsg)
                    }
                    val mindUrl = "$baseUrl/${mindFile.name}"

                    // Persist to registry
                    val items = loadRegistry()
                    items.removeAll { it.id == targetId }
                    
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
                        mindGenerated = true
                    )
                    items.add(newItem)
                    saveRegistry(newItem.id.let { items }) // Save entire list

                    println("Backend: Request Complete. ID: $targetId, Mind: ${mindFile.name}\n")
                    call.respond(CompileResponse(targetId = targetId, mindUrl = mindUrl))
                } catch (e: Exception) {
                    println("Backend: FATAL ERROR: ${e.message}")
                    e.printStackTrace()
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
