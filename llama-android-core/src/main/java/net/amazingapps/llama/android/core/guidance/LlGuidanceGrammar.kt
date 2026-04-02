package net.amazingapps.llama.android.core.guidance

/**
 * Implements [IGrammar] for the variant of Lark (aka EBNF) grammar supported by Llama.cpp's LLGuidance.
 */
class LlGuidanceGrammar : IGrammar {

    /**
     * Converts list of words to LLGuidance grammar.
     *
     * LLGuidance uses a variant of LLGuidance syntax:
     * - Starts with `start: rule`
     * - Rules use `:` instead of `::=`
     * - Terminals can be strings `"..."` or regex `/.../`
     * - Requires `%llguidance` prefix for llama.cpp to route it correctly.
     * See: https://github.com/guidance-ai/llguidance
     *
     * Example:
     *      input = ["hello", "world"]
     *      output = "%llguidance {}\nstart: \"hello\" | \"world\""
     */
    override fun wordListToGrammar(wordList: List<String>): String {

        if (wordList.isEmpty()) {
            return "$LLGUIDANCE_GRAMMAR_PREFIX {}\nstart: \"\""
        }
        val choices = wordList.joinToString(separator = " | ") {
            "\"${it.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        }
        return "$LLGUIDANCE_GRAMMAR_PREFIX {}\nstart: $choices"
    }


    override fun toString(): String {
        return javaClass.simpleName
    }

    override fun validate(grammarString: String): GrammarValidationResult {
        if (!grammarString.startsWith(LLGUIDANCE_GRAMMAR_PREFIX)) {
            return GrammarValidationResult.Failure(
                "LLGuidance grammars must start with $LLGUIDANCE_GRAMMAR_PREFIX prefix",
                0
            )
        }

        return validateGrammarExperimental(grammarString)

    }

    /**
     * This is an experimental validation 100% vibe coded. It will eventually be replaced
     * by a layer of kotlin + jni code, calling llama.cpp llguidance code.
     */
    private fun validateGrammarExperimental(grammarString: String): GrammarValidationResult {
        // 1. Validate first line: %llguidance { ... }
        val lines = grammarString.lines()
        val firstLine = lines.firstOrNull() ?: ""
        val jsonStart = firstLine.indexOf('{')
        val jsonEnd = firstLine.lastIndexOf('}')

        if (jsonStart == -1 || jsonEnd == -1 || jsonEnd < jsonStart) {
            return GrammarValidationResult.Failure(
                "LLGuidance must have a JSON configuration block on the first line (e.g., $LLGUIDANCE_GRAMMAR_PREFIX {})",
                if (jsonStart == -1) 11 else jsonStart
            )
        }

        // 2. Validate rules
        val rulesContent = lines.drop(1).joinToString("\n")
        if (!rulesContent.contains("start:")) {
            return GrammarValidationResult.Failure(
                "LLGuidance grammars must contain a 'start:' rule",
                firstLine.length + 1
            )
        }

        // 3. Scanner-like validation for LLGuidance syntax
        var inQuote = false
        var inRegex = false
        var lastChar = ' '

        grammarString.forEachIndexed { index, c ->
            when {
                c == '"' && lastChar != '\\' && !inRegex -> inQuote = !inQuote
                c == '/' && lastChar != '\\' && !inQuote -> inRegex = !inRegex
                !inQuote && !inRegex -> {
                    // Catch GBNF assignment
                    if (c == ':' && grammarString.getOrNull(index + 1) == ':' && grammarString.getOrNull(
                            index + 2
                        ) == '='
                    ) {
                        return GrammarValidationResult.Failure(
                            "LLGuidance uses ':' for rules, not '::='",
                            index
                        )
                    }
                    // Catch common Lark mistakes or unbalanced chars
                    if (c == '{' && index > firstLine.length) {
                        // Lark doesn't use {} for grouping (it uses ()), though it uses them for repetitions in some dialects
                        // LLGuidance uses {} only for the header.
                    }
                }
            }
            lastChar = c
        }

        if (inQuote) return GrammarValidationResult.Failure(
            "Unclosed string literal",
            grammarString.lastIndexOf('"')
        )
        if (inRegex) return GrammarValidationResult.Failure(
            "Unclosed regex literal",
            grammarString.lastIndexOf('/')
        )

        return GrammarValidationResult.Success
    }

    companion object {

        const val LLGUIDANCE_GRAMMAR_PREFIX = "%llguidance"

        fun isGrammarLLGuidance(grammarString: String?): Boolean {
            return grammarString?.trimStart()?.startsWith(LLGUIDANCE_GRAMMAR_PREFIX) ?: false
        }
    }
}
