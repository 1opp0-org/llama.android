package net.amazingapps.llama.android.sample.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.amazingapps.llama.android.core.InferenceEngine
import net.amazingapps.llama.android.sample.app.repository.ModelDownloader
import java.io.File

class MainViewModel(
    private val modelDownloader: ModelDownloader,
    private val engine: InferenceEngine
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = mutableUiState.asStateFlow()

    fun updateUserInput(input: String) {
        mutableUiState.update { it.copy(userInput = input) }
    }

    fun downloadAndLoadModel() {
        val url =
            "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf"

        val filename = "smollm2-360m-q8_0.gguf"


        val urlFastInference =
            "https://huggingface.co/yamap59/Qwen3.5-0.8B-Instruct-FT-GGUF/resolve/main/qwen3.5-0.8b-instruct.gguf?download=true"

//        llama-3.2-1b-q4_0.gguf
//        https://huggingface.co/polyverse/Llama-3.2-1B-Q4_0-GGUF/blob/main/llama-3.2-1b-q4_0.gguf
        val urlLlama = "https://huggingface.co/polyverse/Llama-3.2-1B-Q4_0-GGUF/resolve/main/llama-3.2-1b-q4_0.gguf"
//        val urlLlama = "https://huggingface.co/polyverse/Llama-3.2-1B-Q4_0-GGUF/blob/main/llama-3.2-1b-q4_0.gguf"

        viewModelScope.launch {
            val modelFile = downloadModel(urlLlama, "llama.gguf")
//            val modelFile = downloadModel(urlFastInference, "fastInference.gguf")
            if (modelFile != null) {
                loadModel(modelFile.path)
            }
        }
    }

    private suspend fun downloadModel(url: String, filename: String): File? =
        withContext(Dispatchers.IO) {
            mutableUiState.update { it.copy(isDownloading = true, status = "Starting download...") }


            val modelFile = modelDownloader.ensureModelDownloaded(
                url = url,
                modelName = filename
            ) { progress ->
                mutableUiState.update { it.copy(status = "Downloading: ${"%.2f".format(progress * 100)}%") }
            }

            if (modelFile == null) {
                mutableUiState.update {
                    it.copy(
                        isDownloading = false,
                        status = "Download Failed"
                    )
                }
            }
            modelFile
        }

    private suspend fun loadModel(path: String) = withContext(Dispatchers.IO) {
        mutableUiState.update { it.copy(status = "Waiting for engine initialization...") }

        // Wait for engine to be initialized
        engine.state.first {
            it is InferenceEngine.State.Initialized || it is InferenceEngine.State.ModelReady
        }

        mutableUiState.update { it.copy(status = "Loading model...") }
        try {
            engine.loadModel(path)
            mutableUiState.update {
                it.copy(
                    isDownloading = false,
                    modelLoaded = true,
                    status = "Model Loaded"
                )
            }
        } catch (e: Exception) {
            mutableUiState.update {
                it.copy(
                    isDownloading = false,
                    status = "Error loading model: ${e.message}"
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
                    aiResponse = "",
                    userInput = ""
                )
            }

            viewModelScope.launch(Dispatchers.Default) {
                engine.sendUserPrompt(currentInput)
                    .onCompletion {
                        mutableUiState.update { it.copy(status = "Model Loaded") }
                    }
                    .collect { token ->
                        withContext(Dispatchers.Main) {
                            mutableUiState.update { state ->
                                state.copy(aiResponse = state.aiResponse + token)
                            }
                        }
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.destroy()
    }
}
