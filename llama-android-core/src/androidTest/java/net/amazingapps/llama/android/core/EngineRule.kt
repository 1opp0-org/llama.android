package net.amazingapps.llama.android.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.rules.ExternalResource
import timber.log.Timber
import java.io.File

/**
 * JUnit Rule for managing InferenceEngine lifecycle in tests.
 * Handles initialization, model loading, and cleanup.
 */
class EngineRule(private val filePath: String = "test_model.gguf") : ExternalResource() {

    lateinit var engine: InferenceEngine
        private set

    val context: Context = ApplicationProvider.getApplicationContext()

    override fun before() {

        runBlocking {
            Timber.v("AI Inference Engine Rule - start")
            engine = AiChat.getInferenceEngine(context)

            engine.state.first {
                it is InferenceEngine.State.Initialized || it is InferenceEngine.State.ModelReady
            }

            val output = copyAssetToModelDir(filePath)
            engine.loadModel(output.path)

            Timber.i("AI Inference Engine Rule - model loaded complete")
        }
    }

    override fun after() {
        Timber.v("Initializing AI Inference Engine - cleanup (after)")
        engine.cleanUp()
        engine.destroy()
    }

    /**
     * Copies gguf AI model from android assets into internal storage.
     *
     * If file already exists at destination it is able to skip copying again.
     *
     * There's no validation in case the destination (or source) files are invalid, corrupted etc.
     * In such cases you'll get an error when loading the model.
     *
     */
    private suspend fun copyAssetToModelDir(sourceName: String): File {

        return withContext(Dispatchers.IO) {
            val modelDir = File(context.cacheDir, "model")
            if (!modelDir.exists()) modelDir.mkdirs()

            val outputFile = File(modelDir, sourceName)

            if (!outputFile.exists()) {
                Timber.i("Copying model from assets to cache dir")
                context.assets.open(sourceName).use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            outputFile
        }
    }

    companion object {
        init {
            Timber.plant(Timber.DebugTree())
        }
    }

}
