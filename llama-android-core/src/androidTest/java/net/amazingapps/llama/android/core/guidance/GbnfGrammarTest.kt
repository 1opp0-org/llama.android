package net.amazingapps.llama.android.core.guidance

import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(Parameterized::class)
class GbnfGrammarValidTest(private val grammar: String) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = listOf(
            "root ::= \"hello\"",
            "root ::= \"yes\" | \"no\"",
            "root ::= [a-z]+",
            "root ::= (\"a\" | \"b\")?",
            "root ::= greeting name\ngreeting ::= \"Hello \"\nname ::= \"World\"",
            "root ::= [0-9] [A-Z] [a-z]",
            """
                root ::= greeting name
                greeting ::= "Hello "
                name ::= "World"
            """.trimIndent(),
        ).map { arrayOf(it) }
    }

    @Test
    fun testValidGrammar() {
        val result = GbnfGrammar().validate(grammar)
        assertEquals(
            actual = result,
            expected = GrammarValidationResult.Success,
            message = "Grammar should be valid: $grammar"
        )
    }
}

@RunWith(Parameterized::class)
class GbnfGrammarInvalidTest(
    private val grammar: String,
    private val expectedMessage: String,
    private val expectedPosition: Int
) {

    companion object {
        @Suppress("TYPE_INTERSECTION_AS_REIFIED_WARNING")
        @JvmStatic
        @Parameterized.Parameters(name = "{1} at {2}")
        fun data() = listOf(
            arrayOf(
                "root \"hello\"",           // Missing ::=
                "expecting ::=",
                5
            ),
            arrayOf(
                "myrule ::= \"hello\"",      // Missing root
                "Grammar is missing 'root' rule",
                0
            ),
            arrayOf(
                "root ::= \"hello",         // Unbalanced quotes
                "unexpected end of input",
                -1
            ),
            arrayOf(
                "root ::= [a-z",            // Unbalanced brackets
                "unexpected end of input",
                -1
            ),
            arrayOf(
                "root ::= undefined_rule",   // Undefined rule
                "expecting newline or end",
                18
            ),
            arrayOf(
                "root: \"hello\"",          // Invalid operator (LLGuidance style)
                "expecting ::=",
                4
            ),
            arrayOf(
                "root ::= root \"a\"",        // Left recursion
                "Validation failed (possible left recursion)",
                -1
            ),
            arrayOf(
                """
                    root ::= greeting name
                    greeting ::= "Hello 
                    name ::= "World"
                """.trimIndent(),
                "unexpected end of input",
                -1
            ),
        )
    }

    @Test
    fun testInvalidGrammar() {
        val expected = GrammarValidationResult.Failure(expectedMessage, expectedPosition)
        val result = GbnfGrammar().validate(grammar)

        when {
            result is GrammarValidationResult.Failure -> {
                assertEquals(actual = result.errorMessage, expected = expected.errorMessage)
                assertEquals(actual = result.errorPosition, expected = expected.errorPosition)
            }

            else -> assertEquals(actual = result, expected = expected)
        }
    }
}

@RunWith(AndroidJUnit4::class)
class GbnfGrammarUtilsTest {
    @Test
    fun testWordListToGBNFGrammar() {
        val words = listOf("apple", "banana", "cherry")
        val grammar = GbnfGrammar().wordListToGrammar(words)

        val result = GbnfGrammar().validate(grammar)
        assertEquals(
            actual = result,
            expected = GrammarValidationResult.Success,
            message = "Generated grammar should be valid"
        )
        assertTrue(
            actual = grammar.contains("\"apple\""),
            message = "Grammar should contain apple"
        )
    }
}
