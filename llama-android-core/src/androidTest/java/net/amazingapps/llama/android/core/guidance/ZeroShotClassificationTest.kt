package net.amazingapps.llama.android.core.guidance

import kotlinx.coroutines.test.runTest
import net.amazingapps.llama.android.core.EngineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@RunWith(Parameterized::class)
class ZeroShotClassificationTest(
    private val grammar: IGrammar, private val testCase: ClassificationTestCase
) {

    @get:Rule
    val engineRule = EngineRule()

    data class ClassificationTestCase(
        val prompt: String, val wordList: List<String>, val expectedOutput: String
    ) {
        override fun toString(): String = prompt.take(14)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} - {1}")
        fun data(): Collection<Array<Any>> {
            val grammars = listOf(GbnfGrammar(), LlGuidanceGrammar())
            val testCases = listOf(
                ClassificationTestCase(
                    prompt = "What color is the sky?",
                    wordList = listOf("Black", "Red", "Blue"),
                    expectedOutput = "Blue"
                ),
                ClassificationTestCase(
                    prompt = "What color is the snowy mountain?",
                    wordList = listOf("Black", "Red", "Blue", "White"),
                    expectedOutput = "White"
                ),
                ClassificationTestCase(
                    prompt = """
                        What is the capital of Sweden? Answer with the correct letter.

                        A) Helsinki
                        B) Reykjavík
                        C) Stockholm
                        D) Oslo
                    """.trimIndent(), wordList = listOf("A", "B", "C", "D"), expectedOutput = "C"
                ),
                ClassificationTestCase(
                    prompt = "What is the capital of Sweden?",
                    wordList = listOf("Helsinki", "Reykjavík", "Stockholm", "Oslo"),
                    expectedOutput = "Stockholm"
                ),
            )

            return testCases.flatMap { testCase ->
                grammars.map { grammar ->
                    arrayOf(grammar, testCase)
                }
            }
        }
    }

    @Test
    fun testClassification() = runTest(timeout = 6.seconds) {
        val result = runTest(
            prompt = testCase.prompt, labels = testCase.wordList, grammar = grammar
        )

        assertEquals("Classification result mismatch", testCase.expectedOutput, result)
    }

    private suspend fun runTest(
        prompt: String, labels: List<String>, grammar: IGrammar
    ): String {

        grammar.wordListToGrammar(labels).let { engineRule.engine.setConstraint(it) }

        val tokens = mutableListOf<String>()

        // Use collect to gather all tokens
        engineRule.engine.sendUserPrompt(prompt, predictLength = 10).collect {
            Timber.d("ZeroShotTest: got token $it --  " + tokens.joinToString())
            tokens.add(it)
        }

        val result = tokens.joinToString("").trim()

        return result
    }
}

