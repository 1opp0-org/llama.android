package net.amazingapps.llama_android.sample.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.amazingapps.llama_android.sample.app.repository.ModelDownloader
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableUiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = mutableUiState.asStateFlow()

    fun updateUserInput(input: String) {
        mutableUiState.update { it.copy(userInput = input) }
    }

    fun downloadAndLoadModel() {
        // Simple mock for now as requested to revert integration for now
        viewModelScope.launch {
            mutableUiState.update { it.copy(isDownloading = true, status = "Downloading...") }

            // Simulating completion for now
            mutableUiState.update {
                it.copy(
                    isDownloading = false,
                    modelLoaded = true,
                    status = "Model Loaded"
                )
            }
        }
    }

    fun sendPrompt() {
        val currentInput = uiState.value.userInput
        if (currentInput.isNotBlank()) {
            mutableUiState.update {
                it.copy(
                    status = "Generating...",
                    aiResponse = "Processing: $currentInput",
                    userInput = ""
                )
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