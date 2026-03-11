package com.example.llama

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import java.io.File

class ModelDownloader(private val context: Context, private val httpClient: HttpClient) {

    val modelDir = File(context.cacheDir, "model")

    companion object {
        private const val BUFFER_SIZE: Long = 1024 * 1024
    }

    suspend fun ensureModelDownloaded(
        url: String,
        modelName: String,
        onProgress: (progress: Float) -> Unit
    ): File? {

        if (!modelDir.exists()) modelDir.mkdirs()
        val outputFile = File(modelDir, modelName)

        if (outputFile.exists() && outputFile.length() > 0) {
            Log.i(
                "ModelDownloader",
                "Model $modelName already exists at ${outputFile.absolutePath}. Skipping download."
            )
            onProgress(1f)
            return outputFile
        }

        Log.i(
            "ModelDownloader",
            "Model $modelName not found or empty. Starting download from $url."
        )

        return try {
            onProgress(0f)
            download(url, modelName, onProgress)
            Log.i("ModelDownloader", "Download finished for $modelName. Waiting for completion...")

            outputFile
        } catch (e: Exception) {
            Log.e("ModelDownloader", "Error downloading model $modelName: ${e.message}")
            if (outputFile.exists()) {
                outputFile.delete()
            }
            null
        }
    }

    suspend fun download(url: String, modelName: String, onProgress: (progress: Float) -> Unit) {

        val modelDir = File(context.cacheDir, "model")
        if (!modelDir.exists()) modelDir.mkdirs()

        val outputFile = File(modelDir, modelName)
        Log.i("ModelDownloader", "Downloading $url to ${outputFile.absolutePath}")

        httpClient.prepareGet(urlString = url).execute { httpResponse ->

            val channel: ByteReadChannel = httpResponse.body()
            var count = 0L
            outputFile.outputStream().asSink().use { stream ->
                while (!channel.exhausted()) {
                    val chunk = channel.readRemaining(BUFFER_SIZE)
                    count += chunk.remaining

                    chunk.transferTo(sink = stream)

                    val progress = count.toFloat() / (httpResponse.contentLength()?.toFloat() ?: 0f)

                    onProgress(progress)
                }
            }
        }
    }

}

fun doAll(context: Context): File {

    val filename = "theModel.gguf"

    runBlocking {

        val url =
            "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf"

        ModelDownloader(context, HttpClient(CIO) {
            engine {
                requestTimeout = 0
            }
        }).ensureModelDownloaded(
            url = url,
            modelName = filename
        ) { progress ->
            Log.i("ModelDownloadProgress", "Download progress: ${"%.2f".format(progress * 100)}%")
        }
    }

    val modelDir = File(context.cacheDir, "model")
    val outputFile = File(modelDir, filename)

    return outputFile


}