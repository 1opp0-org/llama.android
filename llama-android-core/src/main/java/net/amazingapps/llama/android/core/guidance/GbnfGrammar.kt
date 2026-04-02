package net.amazingapps.llama.android.core.guidance

/**
 * Represents a GBNF grammar.
 * Provides validation and future support for grammar-based sampling.
 */
class GbnfGrammar : IGrammar {

    /**
     * Converts list of words to GBNF grammar.
     * GBNF (GGML BNF) uses `::=` for rules and double quotes for strings.
     * It does not have a lexer/parser distinction.
     * See: https://github.com/ggerganov/llama.cpp/blob/master/grammars/README.md
     *
     * Example:
     *      input = ["hello", "world"]
     *      output = "root ::= \"hello\" | \"world\""
     *
     */
    override fun wordListToGrammar(wordList: List<String>): String {

        return wordList.joinToString(
            prefix = "root ::= \"", separator = "\" | \"", postfix = "\""
        )
    }

    override fun toString(): String {
        return javaClass.simpleName
    }

    /**
     * Validates the grammar string.
     * @return GrammarValidationResult with status and potential error details.
     */
    override fun validate(grammarString: String): GrammarValidationResult {
        return nativeValidate(grammarString)
    }

    private external fun nativeValidate(grammar: String): GrammarValidationResult

    companion object {
        init {
            System.loadLibrary("ai-chat")
        }
    }
}
