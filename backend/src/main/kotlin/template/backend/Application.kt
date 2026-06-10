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

@Serializable
data class CompileResponse(val targetId: String, val mindUrl: String)

fun main() {
    embeddedServer(Netty, port = 8888, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        
        install(CORS) {
            // Be very permissive for development to avoid "Fail to fetch"
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

            // Root route for connection testing
            get("/") {
                call.respondText("AugmentX Backend is Running")
            }

            staticFiles("/uploads", uploadDir)

            post("/compile") {
                println("Backend: POST /compile - Received request")
                
                try {
                    val multipart = call.receiveMultipart()
                    var targetName = "Unknown"
                    var targetId = UUID.randomUUID().toString()
                    
                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                if (part.name == "name") targetName = part.value
                            }
                            is PartData.FileItem -> {
                                val fileName = "${targetId}_${part.originalFileName ?: "file"}"
                                val file = File(uploadDir, fileName)
                                part.streamProvider().use { input ->
                                    file.outputStream().buffered().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                println("Backend: Saved file $fileName")
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    // SIMULATED MindAR Compilation
                    val mindFileName = "${targetId}.mind"
                    val mindFile = File(uploadDir, mindFileName)
                    mindFile.writeText("MIND_FILE_CONTENT_FOR_$targetId")

                    // Use localhost for local dev, but in production this would be a real domain
                    val baseUrl = "http://localhost:8888/uploads"
                    call.respond(CompileResponse(
                        targetId = targetId,
                        mindUrl = "$baseUrl/$mindFileName"
                    ))
                } catch (e: Exception) {
                    println("Backend: ERROR processing /compile: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown Error")
                }
            }

            delete("/uploads/{id}") {
                val id = call.parameters["id"]
                println("Backend: DELETE /uploads/$id")
                val files = uploadDir.listFiles { _, name -> name.startsWith(id ?: "") }
                files?.forEach { it.delete() }
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}
