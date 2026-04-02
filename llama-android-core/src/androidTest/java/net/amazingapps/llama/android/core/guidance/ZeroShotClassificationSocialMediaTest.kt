package net.amazingapps.llama.android.core.guidance

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import net.amazingapps.llama.android.core.AiChat
import net.amazingapps.llama.android.core.EngineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

enum class Topic(val label: String) {
    FINANCE("Finance"),
    GOVERNMENT_USA("US Government"),
    WORLD_EVENTS("World Events"),
    SCIENCE_RESEARCH("Science & Research"),
    SPACE_EXPLORATION("Space Exploration"),
    INTERNET_CULTURE("Internet Culture"),
    HEARTWARMING("Heartwarming"),
    TECH_ARTIFICIAL_INTEL("Tech & AI"),
    RETRO_HISTORY("Retro & History"),
    UNSPECIFIED("Unspecified")
}

@RunWith(Parameterized::class)
class ZeroShotClassificationSocialMediaTest(
    private val grammar: IGrammar, private val testCase: ClassificationTestCase
) {

    @get:Rule
    val engineRule = EngineRule()

    private val topicsForClassification = Topic.entries.map { it.label }

    data class ClassificationTestCase(
        val headline: String, val expectedOutput: Topic
    ) {
        override fun toString(): String = "${headline.take(15)} -> ${expectedOutput.label}"
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} - {1}")
        fun data(): Collection<Array<Any>> {

            val grammars = listOf(GbnfGrammar(), LlGuidanceGrammar())

            val testCases = listOf(

                // made up headlines
                "Puppies playing on grass on sunny morning" to Topic.HEARTWARMING,
                "Oil drops, stock futures surge after Iranian announcement on Strait of Hormuz" to Topic.FINANCE,
                "Elections in California: polls are in" to Topic.GOVERNMENT_USA,
                "Meet the candidates for Mayor in NY" to Topic.GOVERNMENT_USA,
                "Protein folding goes crazy" to Topic.SCIENCE_RESEARCH,
                "Artemis II fan art" to Topic.SPACE_EXPLORATION,

                // real headlines from lemmy
                "Egypt won't accept Ukrainian wheat exported by Russia, Zelenskyy says" to Topic.WORLD_EVENTS,
                "It's zombie Jesus day!" to Topic.UNSPECIFIED,
                "True lies" to Topic.UNSPECIFIED,
                "At least the river is rule" to Topic.UNSPECIFIED,
                "Coalition of the Unwilling" to Topic.WORLD_EVENTS,
                "I regret nothing" to Topic.UNSPECIFIED,
                "If it's not Iran's rocket, then why rocket shaped?" to Topic.INTERNET_CULTURE,
                "lamodalf" to Topic.UNSPECIFIED,
                "nominalism gone wrong" to Topic.UNSPECIFIED,
                "It's open season for refusing AI" to Topic.TECH_ARTIFICIAL_INTEL,
                "Hampus wishes you all a happy Easter Sunday" to Topic.HEARTWARMING,
                "Wood shed repaired and almost fully stocked" to Topic.HEARTWARMING,
                "Starmaster Ad (1982)" to Topic.RETRO_HISTORY,
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
    fun testClassification() = runTest(timeout = 20.seconds) {
        val result = runTest(prompt = testCase.headline, grammar = grammar)

        assertEquals("Classification result mismatch", testCase.expectedOutput.label, result)
    }

    private suspend fun runTest(
        prompt: String, grammar: IGrammar
    ): String {

        val systemPrompt = """
            You are a precise classifier. Categorize the headline based on these definitions:
            - Finance: Markets, economy, and money.
            - US Government: Domestic USA politics and elections.
            - World Events: International politics and global news.
            - Science & Research: Biology, medicine, and research.
            - Space Exploration: NASA, rockets, and astronomy.
            - Internet Culture: Memes and social media jokes.
            - Heartwarming: Positive and wholesome content.
            - Tech & AI: Artificial Intelligence and technology.
            - Retro & History: Nostalgia and old media.
            - Unspecified: Headlines that fit nowhere else.
        """.trimIndent()

        engineRule.engine.setSystemPrompt(systemPrompt)

        val fewShotExamples = """
            Headline: "The Federal Reserve signals potential rate cuts"
            Category: Finance
            
            Headline: "Senate debates new infrastructure bill"
            Category: US Government
            
            Headline: "New species of deep-sea jellyfish discovered"
            Category: Science & Research
            
        """.trimIndent()

        val structuredPrompt = """
            $fewShotExamples
            Headline: "$prompt"
            Category:
        """.trimIndent()

        grammar.wordListToGrammar(topicsForClassification)
            .let { engineRule.engine.setConstraint(it) }

        val tokens = mutableListOf<String>()

        // Use collect to gather all tokens
        engineRule.engine.sendUserPrompt(structuredPrompt, predictLength = 10).collect {
            Timber.d("ZeroShotTest: got token $it --  " + tokens.joinToString())
            tokens.add(it)
        }

        val result = tokens.joinToString("").trim()

        return result
    }
}

