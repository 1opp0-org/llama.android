package net.amazingapps.llama.android.core.guidance

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(Parameterized::class)
class LlGuidanceGrammarValidTest(private val grammar: String) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = listOf(
            "%llguidance {}\nstart: \"hello\"",
            "%llguidance {}\nstart: \"yes\" | \"no\"",
            "%llguidance {}\nstart: /[a-z]+/",
            "%llguidance {}\nstart: (A | B)?\nA: \"a\"\nB: \"b\"",
            "%llguidance {}\nstart: greeting name\ngreeting: \"Hello \"\nname: \"World\"",
            "%llguidance {\"node\": \"test\"}\nstart: /[0-9]/ /[A-Z]/ /[a-z]/"
        ).map { arrayOf(it) }
    }

    @Test
    fun testValidGrammar() {
        val result = LlGuidanceGrammar().validate(grammar)
        assertEquals(
            actual = result,
            expected = GrammarValidationResult.Success,
            message = "Grammar should be valid: $grammar"
        )
    }
}

@RunWith(Parameterized::class)
class LlGuidanceGrammarInvalidTest(
    private val grammar: String,
    private val expectedMessage: String,
    private val expectedPosition: Int
) {

    companion object {
        @Suppress("TYPE_INTERSECTION_AS_REIFIED_WARNING")
        @JvmStatic
        @Parameterized.Parameters(name = "Grammar: {0}, Error: {1}")
        fun data() = listOf(
            arrayOf(
                "start: \"hello\"",
                "LLGuidance grammars must start with %llguidance prefix",
                0
            ),
            arrayOf(
                "%llguidance\nstart: \"hello\"",
                "LLGuidance must have a JSON configuration block on the first line (e.g., %llguidance {})",
                11
            ),
            arrayOf(
                "%llguidance {}\nroot: \"hello\"",
                "LLGuidance grammars must contain a 'start:' rule",
                15
            ),
            arrayOf(
                "%llguidance {}\nstart ::= \"hello\"",
                "LLGuidance grammars must contain a 'start:' rule",
                15
            ),
            arrayOf("%llguidance {}\nstart: \"hello", "Unclosed string literal", 22),
            arrayOf("%llguidance {}\nstart: /hello", "Unclosed regex literal", 22),
            arrayOf("", "LLGuidance grammars must start with %llguidance prefix", 0)
        )
    }

    @Test
    fun testInvalidGrammar() {
        val result = LlGuidanceGrammar().validate(grammar)
        assertTrue(
            message = "Result should be Failure for: $grammar",
            actual = result is GrammarValidationResult.Failure
        )
        val failure = result as GrammarValidationResult.Failure
        assertEquals(
            actual = failure.errorMessage,
            expected = expectedMessage,
            message = "Error message mismatch for $grammar"
        )
        assertEquals(
            actual = failure.errorPosition,
            expected = expectedPosition,
            message = "Error position mismatch for $grammar"
        )
    }
}

@RunWith(AndroidJUnit4::class)
class LlGuidanceGrammarUtilsTest {
    @Test
    fun testWordListToLLGuidanceGrammar() {
        val words = listOf("apple", "banana", "cherry")
        val expectedGrammar =
            """
             %llguidance {}
             start: "apple" | "banana" | "cherry"
         """.trimIndent()

        val grammar = LlGuidanceGrammar().wordListToGrammar(words)

        val result = LlGuidanceGrammar().validate(grammar)

        assertEquals(actual = grammar, expected = expectedGrammar)
        assertEquals(actual = result, expected = GrammarValidationResult.Success)

    }
}
