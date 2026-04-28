# Perplexity-based Classification

## Overview

Perplexity-based classification (also known as Log-Likelihood classification) is an alternative to generative classification. Instead of asking the model to "generate" the next token and constraining it with GBNF, we evaluate the probability of each candidate label directly.

## How it works

1.  **Prompting**: You provide the prompt (e.g., a news headline).
2.  **Scoring**: For each possible category (e.g., "Economy", "Politics", "Space"):
    *   Append the category to the prompt.
    *   Calculate the log-probability of the category tokens given the prompt.
    *   The total score for a category is the sum of the log-probabilities of its constituent tokens.
3.  **Selection**: The category with the highest total log-probability (or lowest perplexity) is selected as the winner.

## Why use it for Small Models (0.5B - 1.5B)?

1.  **Eliminates "Generation Drift"**: Small models often want to generate tokens that aren't in your target list (e.g., "News" instead of "Politics"). While GBNF forces them back into the list, the model's "internal preference" might be diluted. Perplexity forces the model to focus only on the candidates you provide.
2.  **Multi-token Robustness**: For categories that consist of multiple tokens (e.g., "US Politics"), perplexity naturally handles the transition between tokens by summing their probabilities. Generative models sometimes "trip" after the first token if the second token is less expected.
3.  **No Temperature Sensitivity**: Since you are looking at raw log-probabilities, you don't need to worry about temperature, top-p, or other sampling parameters.
4.  **Calibration**: It is easier to "calibrate" perplexity. If a model has a natural bias towards the word "Other", you can calculate the "null probability" of "Other" (using a blank prompt) and subtract it from your scores to get a fairer classification.

## Implementation in llama.cpp

In `llama.cpp`, this is typically done by:
1.  Processing the prompt tokens (`llama_decode`).
2.  For each label:
    *   Tokenizing the label.
    *   Evaluating the logits of the next tokens.
    *   Extracting the log-softmax value for the specific token ID of the label.

## Comparison

| Feature | Generative (GBNF) | Perplexity-based |
| :--- | :--- | :--- |
| **Speed** | Faster (1 forward pass) | Slower (N passes, where N = labels)* |
| **Accuracy** | Good for large models | **Better for small models** |
| **Reliability** | May fail if prompt is poor | Very robust |
| **Constraint** | Forces format | Evaluates intent |

*\* Note: With KV-cache optimization, the prompt only needs to be processed once. Each label evaluation only requires processing the label tokens, which is very fast.*
