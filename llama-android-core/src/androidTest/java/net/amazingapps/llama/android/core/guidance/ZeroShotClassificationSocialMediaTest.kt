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
class ZeroShotClassificationSocialMediaTest(
    private val grammar: IGrammar, private val testCase: ClassificationTestCase
) {

    @get:Rule
    val engineRule = EngineRule()

    private val topicsForClassification =
        listOf(
            "Economy", "Politics", "US Politics",
            "Meme", "Wholesome",
            "Space", "Science", "Biology",
            "Other"
        )

    data class ClassificationTestCase(
        val headline: String, val expectedOutput: String
    ) {
        override fun toString(): String = "${headline.take(15)} -> $expectedOutput"
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} - {1}")
        fun data(): Collection<Array<Any>> {
            val grammars = listOf(GbnfGrammar(), LlGuidanceGrammar())
            val testCases = listOf(

                // made up headlines
                "Puppies playing on grass on sunny morning" to "Wholesome",
                "Oil drops, stock futures surge after Iranian announcement on Strait of Hormuz" to "Economy",
                "Elections in California: polls are in" to "US Politics",
                "Meet the candidates for Mayor in NY" to "US Politics",
                "Protein folding goes crazy" to "Biology",
                "Artemis II fan art" to "Space",

                // real headlines from lemmy
                "Egypt won't accept Ukrainian wheat exported by Russia, Zelenskyy says" to "War",
                "It's zombie Jesus day!" to "Woke",
                "True lies" to "Woke",
                "At least the river is rule" to "Woke",
                "Coalition of the Unwilling" to "Woke",
                "I regret nothing" to "Other",
                "If it's not Iran's rocket, then why rocket shaped?" to "War",
                "lamodalf" to "Woke",
                "nominalism gone wrong" to "Other",
                "It's open season for refusing AI" to "Against AI",
                "Hampus wishes you all a happy Easter Sunday" to "Wholesome",
                "Wood shed repaired and almost fully stocked" to "Wholesome",
                "Starmaster Ad (1982)" to "Nostalgia",
            )
                .map {
                    ClassificationTestCase(it.first, it.second)
                }

            return testCases.flatMap { testCase ->
                grammars.map { grammar ->
                    arrayOf(grammar, testCase)
                }
            }
        }
    }

    @Test
    fun testClassification() = runTest(timeout = 6.seconds) {
        val result = runTest(prompt = testCase.headline, grammar = grammar)

        assertEquals("Classification result mismatch", testCase.expectedOutput, result)
    }

    private suspend fun runTest(
        prompt: String, grammar: IGrammar
    ): String {

        grammar.wordListToGrammar(topicsForClassification)
            .let { engineRule.engine.setConstraint(it) }

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

