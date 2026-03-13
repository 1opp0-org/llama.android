package net.amazingapps.llama_android.sample.app

data class MainUiState(
    val status: String = "Idle",
    val modelLoaded: Boolean = false,
    val isDownloading: Boolean = false,
    val userInput: String = "",
    val aiResponse: String = ""
)
