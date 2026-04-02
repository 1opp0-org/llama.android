package net.amazingapps.llama.android.core

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.runner.RunWith
import timber.log.Timber
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class InferenceEngineTest {

    private val filePath = "test_model.gguf"

    @get:Rule
    val engineRule = EngineRule(filePath = filePath)

    val engine: InferenceEngine
        get() = engineRule.engine


    @Test
    fun testInference() = runTest(timeout = 30.seconds) {

        engine.setSystemPrompt("You are a helpful assistant.")
        engine.sendUserPrompt("Hello, say hi!")
            .take(2) // 2 tokens is enough for us to know that it worked
            .reduce { accumulator, value ->
                val acc = accumulator + value
                Timber.i("Received response: $acc")
                acc
            }
    }

    @Test
    fun testBenchmark() = runTest(timeout = 30.seconds) {
        val benchmarkResult = engine.bench(pp = 1, tg = 1, pl = 1, nr = 1)
        Log.i("EngineTest", "Benchmark result: $benchmarkResult")
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
}
