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
    embeddedServer(Netty, port = 8888, host = "127.0.0.1") {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Get)
            allowNonSimpleContentTypes = true
        }

        routing {
            val uploadDir = File("backend/uploads")
            if (!uploadDir.exists()) uploadDir.mkdirs()

            staticFiles("/uploads", uploadDir)

            post("/compile") {
                println("Backend: Received /compile request")
                val multipart = call.receiveMultipart()
                var targetName = "Unknown"
                var targetId = UUID.randomUUID().toString()
                
                multipart.forEachPart { part ->
                    println("Backend: Processing part: ${part.name}")
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "name") targetName = part.value
                        }
                        is PartData.FileItem -> {
                            val fileName = "${targetId}_${part.originalFileName}"
                            val file = File(uploadDir, fileName)
                            part.streamProvider().use { input ->
                                file.outputStream().buffered().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                // SIMULATED MindAR Compilation
                val mindFileName = "${targetId}.mind"
                val mindFile = File(uploadDir, mindFileName)
                mindFile.writeText("MIND_FILE_CONTENT_FOR_$targetId")

                val baseUrl = "http://127.0.0.1:8888/uploads"
                call.respond(CompileResponse(
                    targetId = targetId,
                    mindUrl = "$baseUrl/$mindFileName"
                ))
            }

            delete("/uploads/{id}") {
                val id = call.parameters["id"]
                val files = uploadDir.listFiles { _, name -> name.startsWith(id ?: "") }
                files?.forEach { it.delete() }
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}
