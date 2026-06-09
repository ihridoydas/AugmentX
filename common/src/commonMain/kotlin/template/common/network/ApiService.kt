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
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class Post(val userId: Int, val id: Int, val title: String, val body: String)

@Serializable
data class CompileResponse(val targetId: String, val mindUrl: String)

class ApiService(private val client: HttpClient) {
    suspend fun getPosts(): List<Post> = client.get("https://jsonplaceholder.typicode.com/posts").body()

    suspend fun compileMindAR(imageBlobUrl: String, contentBlobUrl: String, name: String? = null): CompileResponse {
        // In a real scenario, we'd fetch the blobs and upload them as multipart
        // For this implementation, we simulate the backend call
        kotlinx.coroutines.delay(3000)
        return CompileResponse(
            targetId = "target_${name ?: "gen"}_${kotlin.random.Random.nextInt(1000)}",
            mindUrl = "https://example.com/targets/compiled.mind" 
        )
    }

    suspend fun updateMindAR(targetId: String, imageBlobUrl: String, contentBlobUrl: String): CompileResponse {
        kotlinx.coroutines.delay(2000)
        return CompileResponse(
            targetId = targetId,
            mindUrl = "https://example.com/targets/updated_$targetId.mind"
        )
    }
}
