package net.amazingapps.llama.android.core.guidance

import kotlinx.coroutines.test.runTest
import net.amazingapps.llama.android.core.EngineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.time.Duration.Companion.seconds

@RunWith(Parameterized::class)
class ContentFilteringTest(
    private val testCase: FilteringTestCase
) {

    @get:Rule
    val engineRule = EngineRule()

    // The list of topics the user wants to avoid
    private val undesiredTopics = listOf(
        "War and military conflict",
        "Political infighting and elections",
        "Religious arguments or snark",
        "AI-generated content and controversy",
        "United States",
    )

    data class FilteringTestCase(
        val headline: String, val expectedAction: String // "HIDE" or "SHOW"
    ) {
        override fun toString(): String = "${headline.take(15)} -> $expectedAction"
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                // Should HIDE (Matches undesired topics clearly)
                FilteringTestCase("Egypt won't accept Ukrainian wheat exported by Russia, Zelenskyy says", "HIDE"), // War/Conflict
                FilteringTestCase("Elections in California: polls are in", "HIDE"), // Politics
                FilteringTestCase("It's zombie Jesus day!", "HIDE"), // Religious snark
                FilteringTestCase("It's open season for refusing AI", "HIDE"), // AI controversy
                FilteringTestCase("Coalition of the Unwilling", "HIDE"), // Political/War conflict
                FilteringTestCase("If it's not Iran's rocket, then why rocket shaped?", "HIDE"), // Geopolitical conflict/snark

                // Should SHOW (Positive/Neutral/Informative)
                FilteringTestCase("Puppies playing on grass on sunny morning", "SHOW"),
                FilteringTestCase("Oil drops, stock futures surge", "SHOW"),
                FilteringTestCase("Protein folding goes crazy", "SHOW"),
                FilteringTestCase("Artemis II fan art", "SHOW"),
                FilteringTestCase("Hampus wishes you all a happy Easter Sunday", "SHOW"),
                FilteringTestCase("Wood shed repaired and almost fully stocked", "SHOW"),
                FilteringTestCase("Starmaster Ad (1982)", "SHOW"),
                FilteringTestCase("I regret nothing", "SHOW"),
                FilteringTestCase("nominalism gone wrong", "SHOW"),
                FilteringTestCase("lamodalf", "SHOW"),

            ).map { arrayOf(it) }
        }
    }

    @Test
    fun testFiltering() = runTest(timeout = 15.seconds) {
        val result = runFilter(testCase.headline)
        assertEquals("Filtering decision mismatch for: ${testCase.headline}", testCase.expectedAction, result)
    }

    private suspend fun runFilter(headline: String): String {
        val systemPrompt = """
            You are a content filter for a social media app. Your goal is to hide posts that match the user's undesired topics.
            
            Undesired Topics:
            ${undesiredTopics.joinToString("\n") { "- $it" }}
            
            Instructions:
            - If the headline CLEARLY matches an undesired topic, output HIDE.
            - If the headline is safe, output SHOW. 
            - If not sure, output SHOW.
        """.trimIndent()

        engineRule.engine.setSystemPrompt(systemPrompt)

        val userPrompt = """
            Headline: "$headline"
            Decision:
        """.trimIndent()

        // Strict GBNF for binary choice
        GbnfGrammar().wordListToGrammar(listOf("HIDE", "SHOW"))
            .let { engineRule.engine.setConstraint(it) }

        val tokens = mutableListOf<String>()
        engineRule.engine.sendUserPrompt(userPrompt, predictLength = 5).collect {
            tokens.add(it)
        }

        return tokens.joinToString("").trim()
    }
}
