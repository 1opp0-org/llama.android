package net.amazingapps.llama.android.core

import android.R.attr.path
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import com.arm.aichat.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import org.junit.Rule
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class InferenceEngineTest {

    @JvmField
    @get:Rule
    val context: Context = ApplicationProvider.getApplicationContext()

    private val filePath = "test_model.gguf"

    private suspend fun copyAssetToModelDir(sourceName: String): File {
        return withContext(Dispatchers.IO) {

            val modelDir = File(context.cacheDir, "model")
            if (!modelDir.exists()) modelDir.mkdirs()

            val outputFile = File(modelDir, sourceName)

            context.assets.open(sourceName).use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outputFile
        }
    }

    @Test
    fun testAssetExists() {
        Log.i("EngineTest", "Checking if test model asset exists...")
        val context = InstrumentationRegistry.getInstrumentation().context
        val assetManager = context.assets
        val inputStream = assetManager.open(filePath)
        assertNotNull(inputStream)
        inputStream.close()
    }

    @Test
    fun testInference() = runTest(timeout = 30.seconds) {
        val engine = AiChat.getInferenceEngine(context)

        engine.state.first {
            it is InferenceEngine.State.Initialized || it is InferenceEngine.State.ModelReady
        }

        val output = copyAssetToModelDir(filePath)
        engine.loadModel(output.path)
        engine.sendUserPrompt("Hello, say hi!")
            .take(2) // 2 tokens is enough for us to know that it worked
            .collect { token ->
                Log.i("EngineTest", "Received token: $token")
            }
    }
}
