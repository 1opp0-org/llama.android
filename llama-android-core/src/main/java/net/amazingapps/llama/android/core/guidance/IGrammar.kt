package net.amazingapps.llama.android.core.guidance

import net.amazingapps.llama.android.core.InferenceEngine

interface IGrammar {

    /**
     * Validates the grammar.
     * @return [GrammarValidationResult.Success] if the grammar is valid, [GrammarValidationResult.Failure] otherwise.
     */
    fun validate(grammarString: String): GrammarValidationResult

    /**
     * Converts a list of words to a grammar.
     * @return grammar to be used in [InferenceEngine.setConstraint]
     */
    fun wordListToGrammar(wordList: List<String>): String
}


/**
 * Result of a grammar validation.
 */
sealed class GrammarValidationResult {
    /**
     * Indicates that the grammar is valid.
     */
    object Success : GrammarValidationResult()

    /**
     * Indicates that the grammar is invalid.
     * @property errorMessage a human-readable error message.
     * @property errorPosition the zero-based character index where the error was detected, or -1 if unknown.
     */
    data class Failure(
        val errorMessage: String,
        /**
         * Approximate position in characters
         */
        val errorPosition: Int,
    ) : GrammarValidationResult()

    val isValid: Boolean get() = this is Success
}
